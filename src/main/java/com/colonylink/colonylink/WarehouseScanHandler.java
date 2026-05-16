package com.colonylink.colonylink;

import appeng.api.implementations.blockentities.IWirelessAccessPoint;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.stacks.AEItemKey;
import com.ldtteam.domumornamentum.block.IMateriallyTexturedBlock;
import com.ldtteam.domumornamentum.block.IMateriallyTexturedBlockComponent;
import com.ldtteam.domumornamentum.client.model.data.MaterialTextureData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.core.colony.buildings.AbstractBuildingStructureBuilder;
import com.minecolonies.core.colony.buildings.utils.BuildingBuilderResource;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Gère le scan à la demande du stock Warehouse MineColonies.
 *
 * Flow :
 *  1. Le joueur clique "Check Warehouse" dans ColonyLinkScreen (wand GUI)
 *  2. WarehouseCheckPacket arrive ici
 *  3. On vérifie le cooldown 400 ticks (par joueur, stocké dans une map statique)
 *  4. On scanne tous les racks de la Warehouse
 *  5. Pour chaque ressource builder manquante, on calcule :
 *     - inWarehouse : quantité directement présente dans les racks
 *     - viaCraft : quantité supplémentaire accessible en résolvant récursivement
 *       les patterns AE2 dont les composants sont présents en warehouse
 *  6. La réservation est appliquée (stock warehouse partagé entre ressources)
 *  7. Le résultat est envoyé via WarehouseResultPacket
 */
public class WarehouseScanHandler
{
    /** Cooldown en ticks entre deux scans (400 ticks = 20 secondes). */
    private static final int SCAN_COOLDOWN_TICKS = 400;

    /** Profondeur max de récursion pour la résolution des patterns AE2. */
    private static final int MAX_RECURSION_DEPTH = 10;

    /** Dernier tick de scan par UUID joueur. */
    private static final Map<java.util.UUID, Long> lastScanTick = new HashMap<>();

    // ────────────────────────────────────────────────────────────────────────
    // Point d'entrée
    // ────────────────────────────────────────────────────────────────────────

    public static void handleWarehouseCheck(ServerPlayer player, BlockPos builderPos)
    {
        long currentTick = player.serverLevel().getGameTime();
        java.util.UUID uuid = player.getUUID();

        // Cooldown check
        Long lastTick = lastScanTick.get(uuid);
        if (lastTick != null && currentTick - lastTick < SCAN_COOLDOWN_TICKS)
        {
            long remaining = SCAN_COOLDOWN_TICKS - (currentTick - lastTick);
            player.sendSystemMessage(Component.literal(
                    "§6[ColonyLink] Warehouse scan on cooldown — " + (remaining / 20) + "s remaining."));
            return;
        }

        lastScanTick.put(uuid, currentTick);

        // Récupère la wand et le réseau AE2
        ItemStack wandStack = findWandInInventory(player);
        if (wandStack == null || !isWandLinked(wandStack))
        {
            player.sendSystemMessage(Component.literal("§c[ColonyLink] Clipboard not linked to a network!"));
            sendFailure(player, currentTick);
            return;
        }

        ServerLevel level = player.serverLevel();

        // Vérifie que le redirector a bien une WarehouseLinkCard
        BlockPos redirectorPos = getActiveRedirectorPos(wandStack);
        if (redirectorPos == null)
        {
            player.sendSystemMessage(Component.literal("§cNo Redirector linked to this Clipboard!"));
            sendFailure(player, currentTick);
            return;
        }

        var be = level.getBlockEntity(redirectorPos);
        boolean hasCard = false;
        if (be instanceof ColonyLinkRedirectorBlockEntity redirector) hasCard = redirector.hasWarehouseCard();
        if (!hasCard)
        {
            player.sendSystemMessage(Component.literal(
                    "§c[ColonyLink] No Warehouse Link Card in the Redirector!"));
            sendFailure(player, currentTick);
            return;
        }

        // Récupère la colonie et le builder
        IColony colony = IColonyManager.getInstance().getClosestColony(level, builderPos);
        if (colony == null)
        {
            player.sendSystemMessage(Component.literal("§c[ColonyLink] No colony found!"));
            sendFailure(player, currentTick);
            return;
        }

        AbstractBuildingStructureBuilder builderBuilding = findBuilderBuilding(colony, builderPos);
        if (builderBuilding == null)
        {
            player.sendSystemMessage(Component.literal("§c[ColonyLink] Builder's Hut not found!"));
            sendFailure(player, currentTick);
            return;
        }

        // Récupère le service de craft AE2
        ICraftingService craftingService = null;
        {
            IWirelessAccessPoint wap = getWap(wandStack, level);
            if (wap == null)
            {
                player.sendSystemMessage(Component.literal("§cCannot connect to AE2 network!"));
                sendFailure(player, currentTick);
                return;
            }
            IGrid grid = wap.getGrid();
            if (grid == null)
            {
                sendFailure(player, currentTick);
                return;
            }
            craftingService = grid.getCraftingService();
        }

        // ── Scan des racks warehouse ─────────────────────────────────────────
        // Stock warehouse agrégé : Item → quantité totale dans les racks
        // On travaille en unités "item brut" pour pouvoir faire la réservation
        // Stock standard (agrégé par Item) + stock DO finaux (par ItemStack avec NBT)
        Map<Item, Long> warehouseStock = new HashMap<>();
        List<ItemStack> warehouseDomumStock = new ArrayList<>();
        scanWarehouseRacks(colony, level, warehouseStock, warehouseDomumStock);

        if (warehouseStock.isEmpty() && warehouseDomumStock.isEmpty())
        {
            player.sendSystemMessage(Component.literal(
                    "§e[ColonyLink] Warehouse found but no items detected in racks."));
        }

        // Stock de réservation : copie mutable sur laquelle on déduit au fur et à mesure
        Map<Item, Long> reservedStock = new HashMap<>(warehouseStock);

        // ── Calcul par ressource builder ─────────────────────────────────────
        Map<String, BuildingBuilderResource> neededResources = builderBuilding.getNeededResources();
        List<WarehouseResultPacket.WarehouseEntry> resultEntries = new ArrayList<>();

        if (neededResources != null)
        {
            for (BuildingBuilderResource resource : neededResources.values())
            {
                ItemStack stack = resource.getItemStack();
                int needed = resource.getAmount();
                int available = resource.getAvailable();
                int missing = needed - available;
                if (missing <= 0) continue;

                List<String> tooltipLines = new ArrayList<>();

                if (DomumCraftHandler.isDomumItem(stack))
                {
                    // Cas Domum : vérifie d'abord si l'item final est en warehouse,
                    // sinon résout les composants via warehouse
                    WarehouseEntry entry = computeDomumWarehouseEntry(
                            stack, missing, reservedStock, warehouseDomumStock, craftingService, tooltipLines);
                    resultEntries.add(new WarehouseResultPacket.WarehouseEntry(
                            stack.copy(), entry.inWarehouse(), entry.viaCraft(), tooltipLines));
                }
                else
                {
                    // Cas standard
                    WarehouseEntry entry = computeStandardWarehouseEntry(
                            stack, missing, reservedStock, craftingService, tooltipLines, 0, new HashSet<>());
                    resultEntries.add(new WarehouseResultPacket.WarehouseEntry(
                            stack.copy(), entry.inWarehouse(), entry.viaCraft(), tooltipLines));
                }
            }
        }

        PacketDistributor.sendToPlayer(player,
                new WarehouseResultPacket(resultEntries, currentTick, true));

        player.sendSystemMessage(Component.literal(
                "§a[ColonyLink] Warehouse scan complete — "
                        + warehouseStock.size() + " item type(s) found in racks."));
    }

    // ────────────────────────────────────────────────────────────────────────
    // Scan des racks
    // ────────────────────────────────────────────────────────────────────────

    /**
     * Parcourt tous les racks de la Warehouse et agrège les quantités par Item.
     * On utilise Item (pas ItemStack) comme clé pour rester simple ;
     * les items avec NBT custom (Domum) sont gérés séparément via computeDomumWarehouseEntry.
     */
    /**
     * Scanne les racks de la Warehouse et remplit deux stocks :
     * - stock       : items standard agrégés par Item (clé simple)
     * - domumStock  : items DO finaux conservés comme ItemStack complets
     *                 pour permettre la comparaison par isSameItemSameComponents
     */
    private static void scanWarehouseRacks(
            IColony colony,
            ServerLevel level,
            Map<Item, Long> stock,
            List<ItemStack> domumStock)
    {
        for (IBuilding building : colony.getServerBuildingManager().getBuildings().values())
        {
            if (!building.getClass().getSimpleName().contains("WareHouse")
                    && !building.getClass().getSimpleName().contains("Warehouse"))
                continue;

            try
            {
                var containerList = building.getContainers();
                if (containerList == null) continue;

                for (BlockPos rackPos : containerList)
                {
                    IItemHandler handler = level.getCapability(
                            Capabilities.ItemHandler.BLOCK, rackPos, null);
                    if (handler == null) continue;

                    for (int slot = 0; slot < handler.getSlots(); slot++)
                    {
                        ItemStack inSlot = handler.getStackInSlot(slot);
                        if (inSlot.isEmpty()) continue;

                        if (DomumCraftHandler.isDomumItem(inSlot))
                        {
                            // Item DO final : stocké entier pour comparaison NBT exacte
                            boolean merged = false;
                            for (ItemStack existing : domumStock)
                            {
                                if (ItemStack.isSameItemSameComponents(existing, inSlot))
                                {
                                    existing.grow(inSlot.getCount());
                                    merged = true;
                                    break;
                                }
                            }
                            if (!merged)
                                domumStock.add(inSlot.copy());
                        }
                        else
                        {
                            stock.merge(inSlot.getItem(), (long) inSlot.getCount(), Long::sum);
                        }
                    }
                }
            }
            catch (Exception e)
            {
                ColonyLink.LOGGER.debug("[ColonyLink] Error scanning warehouse rack: {}", e.getMessage());
            }
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // Calcul standard (récursif)
    // ────────────────────────────────────────────────────────────────────────

    /**
     * Calcule pour un item standard :
     * - inWarehouse : quantité directement dans les racks (déduite du reservedStock)
     * - viaCraft    : quantité supplémentaire craftable si les composants AE2
     *                 sont eux-mêmes disponibles en warehouse (résolution récursive)
     *
     * @param visitedItems  cycle guard : items déjà visités dans la branche récursive courante
     */
    private static WarehouseEntry computeStandardWarehouseEntry(
            ItemStack stack,
            int needed,
            Map<Item, Long> reservedStock,
            ICraftingService craftingService,
            List<String> tooltipLines,
            int depth,
            Set<Item> visitedItems)
    {
        Item item = stack.getItem();
        String itemName = stack.getDisplayName().getString();

        // ── 1. Stock direct en warehouse ────────────────────────────────────
        long directStock = reservedStock.getOrDefault(item, 0L);
        long directUsed = Math.min(directStock, needed);
        if (directUsed > 0)
        {
            reservedStock.put(item, directStock - directUsed);
            tooltipLines.add("§a  §fWarehouse (direct): §a+" + directUsed + "x " + itemName);
        }

        long remainingNeeded = needed - directUsed;
        if (remainingNeeded <= 0)
            return new WarehouseEntry(directUsed, 0L);

        // ── 2. Résolution récursive via patterns AE2 ────────────────────────
        if (depth >= MAX_RECURSION_DEPTH || visitedItems.contains(item))
            return new WarehouseEntry(directUsed, 0L);

        if (craftingService == null || !craftingService.isCraftable(AEItemKey.of(stack)))
            return new WarehouseEntry(directUsed, 0L);

        // Récupère le pattern AE2 pour cet item
        var patterns = craftingService.getCraftingFor(AEItemKey.of(stack));
        if (patterns == null || patterns.isEmpty())
            return new WarehouseEntry(directUsed, 0L);

        var pattern = patterns.iterator().next();

        // Calcule le batch size (sortie du pattern)
        long batchOutput = 1;
        for (var output : pattern.getOutputs())
        {
            if (output.what() instanceof AEItemKey outKey && outKey.getItem() == item)
            {
                batchOutput = output.amount();
                break;
            }
        }

        // Cycle guard
        Set<Item> newVisited = new HashSet<>(visitedItems);
        newVisited.add(item);

        // Pour chaque batch nécessaire, vérifie si tous les composants
        // sont disponibles en warehouse (via récursion)
        long batchesNeeded = (long) Math.ceil((double) remainingNeeded / batchOutput);
        long craftableFromWarehouse = 0;

        // On simule les batches un par un jusqu'à épuisement du stock warehouse
        // On utilise une copie du stock de réservation pour pouvoir rollback
        // si un batch n'est pas entièrement satisfait
        Map<Item, Long> stockSnapshot = new HashMap<>(reservedStock);
        List<String> craftTooltip = new ArrayList<>();

        for (long batch = 0; batch < batchesNeeded; batch++)
        {
            boolean batchOk = true;
            Map<Item, Long> batchReservations = new HashMap<>();
            List<String> batchTooltip = new ArrayList<>();

            for (var input : pattern.getInputs())
            {
                if (input == null) continue;

                // Récupère le premier item du pattern input via getPossibleInputs()
                ItemStack inputStack = null;
                long inputNeeded = 1;
                for (var possible : input.getPossibleInputs())
                {
                    if (possible.what() instanceof AEItemKey inputKey)
                    {
                        inputStack = inputKey.toStack(1);
                        inputNeeded = possible.amount();
                        break;
                    }
                }
                if (inputStack == null || inputStack.isEmpty()) continue;
                Item inputItem = inputStack.getItem();

                // Récupère le stock disponible pour ce composant
                // en tenant compte des réservations déjà faites dans ce batch
                long alreadyReserved = batchReservations.getOrDefault(inputItem, 0L);
                long effectiveStock = reservedStock.getOrDefault(inputItem, 0L) - alreadyReserved;

                if (effectiveStock >= inputNeeded)
                {
                    // Disponible directement en warehouse
                    batchReservations.merge(inputItem, inputNeeded, Long::sum);
                    batchTooltip.add("§7    §f" + inputStack.getDisplayName().getString()
                            + ": §a+" + inputNeeded + "x (warehouse)");
                }
                else
                {
                    // Pas assez directement → tente récursion
                    long stillNeeded = inputNeeded - Math.max(0, effectiveStock);
                    if (effectiveStock > 0)
                        batchReservations.merge(inputItem, effectiveStock, Long::sum);

                    // Récursion : est-ce que les composants de ce composant sont en warehouse ?
                    List<String> subTooltip = new ArrayList<>();
                    WarehouseEntry subEntry = computeStandardWarehouseEntry(
                            inputStack.copyWithCount((int) Math.min(stillNeeded, Integer.MAX_VALUE)),
                            (int) Math.min(stillNeeded, Integer.MAX_VALUE),
                            reservedStock,
                            craftingService,
                            subTooltip,
                            depth + 1,
                            newVisited);

                    long totalFromSub = subEntry.inWarehouse() + subEntry.viaCraft();
                    if (totalFromSub < stillNeeded)
                    {
                        batchOk = false;
                        break;
                    }
                    batchTooltip.addAll(subTooltip);
                }
            }

            if (batchOk)
            {
                // Applique les réservations du batch
                for (var entry : batchReservations.entrySet())
                    reservedStock.merge(entry.getKey(), -entry.getValue(), Long::sum);

                craftableFromWarehouse += batchOutput;
                craftTooltip.addAll(batchTooltip);
            }
            else
            {
                // Un batch échoue → on s'arrête (stock épuisé)
                break;
            }
        }

        if (craftableFromWarehouse > 0)
        {
            tooltipLines.add("§e  §fVia craft (warehouse components): §e+" + craftableFromWarehouse + "x " + itemName);
            tooltipLines.addAll(craftTooltip);
        }

        return new WarehouseEntry(directUsed, craftableFromWarehouse);
    }

    // ────────────────────────────────────────────────────────────────────────
    // Calcul Domum Ornamentum
    // ────────────────────────────────────────────────────────────────────────

    /**
     * Pour un item Domum, vérifie si les composants (matériaux textures) sont en warehouse.
     * On résout aussi récursivement les composants craftables depuis la warehouse.
     */
    private static WarehouseEntry computeDomumWarehouseEntry(
            ItemStack stack,
            int needed,
            Map<Item, Long> reservedStock,
            List<ItemStack> domumStock,
            ICraftingService craftingService,
            List<String> tooltipLines)
    {
        Item item = stack.getItem();
        if (!(item instanceof BlockItem blockItem)) return new WarehouseEntry(0L, 0L);
        Block block = blockItem.getBlock();
        if (!(block instanceof IMateriallyTexturedBlock texturedBlock)) return new WarehouseEntry(0L, 0L);

        MaterialTextureData textureData = MaterialTextureData.readFromItemStack(stack);

        // ── 1. Vérifie si l'item DO final est directement en warehouse ────────
        long directDomum = 0;
        for (ItemStack domumInStock : domumStock)
        {
            if (ItemStack.isSameItemSameComponents(domumInStock, stack))
            {
                directDomum = domumInStock.getCount();
                break;
            }
        }

        if (directDomum >= needed)
        {
            tooltipLines.add("§a[DO] §fDirect in Warehouse: §a" + directDomum + "/" + needed + " ✔");
            return new WarehouseEntry(Math.min(directDomum, needed), 0L);
        }

        // Item DO pas (assez) en warehouse → résout les composants
        tooltipLines.add("§b[DO] §7Components needed for §f" + needed + "x §7" + stack.getDisplayName().getString() + ":");

        boolean allComponentsSatisfied = true;
        long satisfiedSets = needed; // combien de sets complets on peut faire

        for (IMateriallyTexturedBlockComponent component : texturedBlock.getComponents())
        {
            Block materialBlock = textureData.getTexturedComponents().get(component.getId());
            if (materialBlock == null)
            {
                if (!component.isOptional())
                {
                    tooltipLines.add("§c  - " + component.getId().getPath() + ": §4NOT DEFINED");
                    allComponentsSatisfied = false;
                }
                continue;
            }

            ItemStack materialStack = new ItemStack(materialBlock, needed);
            String matName = materialStack.getDisplayName().getString();

            List<String> componentTooltip = new ArrayList<>();
            WarehouseEntry componentEntry = computeStandardWarehouseEntry(
                    materialStack, needed, reservedStock, craftingService,
                    componentTooltip, 0, new HashSet<>());

            long totalForComponent = componentEntry.inWarehouse() + componentEntry.viaCraft();
            long setsFromComponent = totalForComponent; // 1 composant par set Domum

            if (setsFromComponent < needed)
            {
                allComponentsSatisfied = false;
                satisfiedSets = Math.min(satisfiedSets, setsFromComponent);
                tooltipLines.add("§c  - " + matName + ": §c" + totalForComponent + "/" + needed + " available");
            }
            else
            {
                tooltipLines.add("§a  - " + matName + ": §a" + totalForComponent + "/" + needed + " ✔");
            }

            tooltipLines.addAll(componentTooltip);
        }

        if (!allComponentsSatisfied && satisfiedSets == 0)
            return new WarehouseEntry(0L, 0L);

        // Pour Domum, on ne peut pas "avoir" l'item directement en warehouse
        // (c'est un craft virtuel), donc tout passe en viaCraft
        return new WarehouseEntry(0L, satisfiedSets);
    }

    // ────────────────────────────────────────────────────────────────────────
    // Helpers
    // ────────────────────────────────────────────────────────────────────────

    private static void sendFailure(ServerPlayer player, long timestamp)
    {
        PacketDistributor.sendToPlayer(player,
                new WarehouseResultPacket(new ArrayList<>(), timestamp, false));
    }

    private static AbstractBuildingStructureBuilder findBuilderBuilding(IColony colony, BlockPos pos)
    {
        for (IBuilding b : colony.getServerBuildingManager().getBuildings().values())
        {
            if (b.getPosition().equals(pos) && b instanceof AbstractBuildingStructureBuilder builder)
                return builder;
        }
        return null;
    }

    /**
     * Cherche d'abord la wand AE2, puis la wand RS2.
     * Retourne la première trouvée, ou null.
     */
    private static ItemStack findWandInInventory(ServerPlayer player)
    {
        for (ItemStack stack : player.getInventory().items)
            if (stack.getItem() instanceof ColonyLinkWand) return stack;
        return null;
    }

    private static boolean isWandLinked(ItemStack wand)
    {
        return ColonyLinkWandLinkableHandler.isLinked(wand);
    }

    private static BlockPos getActiveRedirectorPos(ItemStack wand)
    {
        return ColonyLinkWandLinkableHandler.getActiveRedirectorPos(wand);
    }

    private static IWirelessAccessPoint getWap(ItemStack wandStack, ServerLevel level)
    {
        net.minecraft.core.GlobalPos linkedPos = ColonyLinkWandLinkableHandler.getLinkedPos(wandStack);
        if (linkedPos == null) return null;
        ServerLevel targetLevel = level.getServer().getLevel(linkedPos.dimension());
        if (targetLevel == null) return null;
        var be = targetLevel.getBlockEntity(linkedPos.pos());
        if (be instanceof IWirelessAccessPoint wap) return wap;
        return null;
    }

    // ────────────────────────────────────────────────────────────────────────
    // Record interne (usage uniquement dans ce fichier)
    // ────────────────────────────────────────────────────────────────────────

    private record WarehouseEntry(long inWarehouse, long viaCraft) {}
}