package com.colonylink.colonylink;

import appeng.api.implementations.blockentities.IWirelessAccessPoint;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.stacks.AEItemKey;
import com.ldtteam.domumornamentum.block.IMateriallyTexturedBlock;
import com.ldtteam.domumornamentum.block.IMateriallyTexturedBlockComponent;
import com.ldtteam.domumornamentum.client.model.data.MaterialTextureData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.core.colony.buildings.AbstractBuildingStructureBuilder;
import com.minecolonies.core.colony.buildings.utils.BuildingBuilderResource;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
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
 * Handles the on-demand scan of the MineColonies Warehouse stock.
 *
 * Flow:
 *  1. Player clicks "Check Warehouse" in ColonyLinkScreen (wand GUI)
 *  2. WarehouseCheckPacket arrives here
 *  3. Check the cooldown (per player, stored in a static map)
 *  4. Scan all the Warehouse racks
 *  5. For each missing builder resource, compute:
 *     - inWarehouse: quantity directly present in the racks
 *     - viaCraft: extra quantity reachable by recursively resolving the AE2 patterns
 *       whose components are present in the warehouse
 *  6. Apply the reservation (warehouse stock shared between resources)
 *  7. Send the result via WarehouseResultPacket
 */
public class WarehouseScanHandler
{
    // v1.6.0 — the scan cooldown is now driven by the same config value the
    // client uses for snapshot expiry (warehouse_snapshot_validity_ticks);
    // the previous hardcoded 400 silently ignored the config.
    private static int scanCooldownTicks()
    {
        return ColonyLinkConfig.WAREHOUSE_SNAPSHOT_VALIDITY_TICKS.get();
    }

    /** Max recursion depth for resolving AE2 patterns. */
    private static final int MAX_RECURSION_DEPTH = 10;

    /** Last scan tick per player UUID. */
    private static final Map<java.util.UUID, Long> lastScanTick = new HashMap<>();

    /** v1.7.1 — called from ColonyLinkServerTicker.onServerStopping to prevent leaks. */
    public static void clearScanHistory() { lastScanTick.clear(); }

    // Entry point

    public static void handleWarehouseCheck(ServerPlayer player, BlockPos builderPos)
    {
        long currentTick = player.serverLevel().getGameTime();
        java.util.UUID uuid = player.getUUID();

        // Cooldown check
        int cooldown = scanCooldownTicks();

        // v1.6.0 — opportunistic purge of long-expired entries so the static
        // per-player map cannot grow forever. Deliberately NOT tied to logout:
        // clearing on logout would let a relog reset the cooldown.
        long purgeBefore = currentTick - (long) cooldown * 10;
        lastScanTick.values().removeIf(t -> t < purgeBefore);

        Long lastTick = lastScanTick.get(uuid);
        if (lastTick != null && currentTick - lastTick < cooldown)
        {
            long remaining = cooldown - (currentTick - lastTick);
            player.sendSystemMessage(Component.translatable("colonylink.whs.cooldown", (remaining / 20)));
            return;
        }
        // v1.6.0 — the cooldown is armed just before the actual scan (below),
        // so a failed validation no longer burns the full cooldown.

        // Get the wand and the AE2 network
        ItemStack wandStack = findWandInInventory(player);
        if (wandStack == null || !isWandLinked(wandStack))
        {
            player.sendSystemMessage(Component.translatable("colonylink.whs.clipboard_not_linked"));
            sendFailure(player, currentTick);
            return;
        }

        ServerLevel level = player.serverLevel();

        // v1.4.9 — cross-dimension: the Clipboard cannot reach a colony in another
        // dimension. Clean refusal (null = legacy entry -> let it through).
        ResourceKey<Level> builderDim =
                ColonyLinkWandLinkableHandler.getBuilderDimension(wandStack, builderPos);
        if (builderDim != null && !level.dimension().equals(builderDim))
        {
            player.sendSystemMessage(Component.translatable("colonylink.handler.cross_dimension", builderDim.location().getPath()));
            sendFailure(player, currentTick);
            return;
        }

        // Verify the redirector actually has a WarehouseLinkCard
        BlockPos redirectorPos = getActiveRedirectorPos(wandStack);
        if (redirectorPos == null)
        {
            player.sendSystemMessage(Component.translatable("colonylink.handler.no_redirector"));
            sendFailure(player, currentTick);
            return;
        }

        // 1.7.0-beta.4 — active entry's Redirector, by UUID first (follows a moved Redirector).
        var be = RedirectorRegistry.forEntry(level, ColonyLinkWandLinkableHandler.getActiveEntry(wandStack));
        boolean hasCard = false;
        if (be instanceof ColonyLinkRedirectorBlockEntity redirector) hasCard = redirector.hasWarehouseCard();
        if (!hasCard)
        {
            player.sendSystemMessage(Component.translatable("colonylink.whs.no_card"));
            sendFailure(player, currentTick);
            return;
        }

        // Get the colony and the builder
        IColony colony = IColonyManager.getInstance().getClosestColony(level, builderPos);
        if (colony == null)
        {
            player.sendSystemMessage(Component.translatable("colonylink.whs.no_colony"));
            sendFailure(player, currentTick);
            return;
        }

        // 1.7.0 — colony permission (the scan reads the colony's Warehouse racks),
        // same action as the other Clipboard handlers.
        if (!colony.getPermissions().hasPermission(player, Action.ACCESS_HUTS))
        {
            player.sendSystemMessage(Component.translatable("colonylink.wand.msg.no_permission"));
            sendFailure(player, currentTick);
            return;
        }

        // 1.7.0-beta.2 — the Warehouse comes from the card, which must belong to the
        // Redirector's Builder colony, and to the colony of the Builder being scanned.
        CardLinks.Resolved card = CardLinks.resolveForRedirector(level, (ColonyLinkRedirectorBlockEntity) be, player);
        if (card.ok() && card.colony().getID() != colony.getID())
            card = new CardLinks.Resolved(CardLinks.Status.OTHER_COLONY, card.link(), null, null);
        if (!card.ok())
        {
            player.sendSystemMessage(card.message());
            sendFailure(player, currentTick);
            return;
        }
        com.minecolonies.core.colony.buildings.workerbuildings.BuildingWareHouse cardWarehouse = card.warehouse();

        AbstractBuildingStructureBuilder builderBuilding = findBuilderBuilding(colony, builderPos);
        if (builderBuilding == null)
        {
            player.sendSystemMessage(Component.translatable("colonylink.handler.hut_not_found"));
            sendFailure(player, currentTick);
            return;
        }

        // Get the AE2 crafting service
        ICraftingService craftingService = null;
        {
            IWirelessAccessPoint wap = getWap(wandStack, level);
            if (wap == null)
            {
                player.sendSystemMessage(Component.translatable("colonylink.handler.no_wap"));
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

        // Scan the warehouse racks
        // Aggregated warehouse stock: Item -> total quantity in the racks.
        // Work in "raw item" units to allow reservation.
        // Standard stock (aggregated by Item) + final DO stock (by ItemStack with NBT).
        // v1.4.9 — strict fail-off: the warehouse must be FULLY loaded.
        // Scanning racks in unloaded chunks would silently return incomplete stock.
        if (!ColonyLinkChunkUtil.warehouseFullyLoaded(level, cardWarehouse))
        {
            player.sendSystemMessage(Component.translatable("colonylink.whs.warehouse_unloaded"));
            sendFailure(player, currentTick);
            return;
        }

        // All validations passed — arm the cooldown and run the scan.
        lastScanTick.put(uuid, currentTick);

        Map<Item, Long> warehouseStock = new HashMap<>();
        List<ItemStack> warehouseDomumStock = new ArrayList<>();
        scanWarehouseRacks(cardWarehouse, level, warehouseStock, warehouseDomumStock);

        if (warehouseStock.isEmpty() && warehouseDomumStock.isEmpty())
        {
            player.sendSystemMessage(Component.translatable("colonylink.whs.no_items"));
        }

        // Reservation stock: a mutable copy that is deducted as we go
        Map<Item, Long> reservedStock = new HashMap<>(warehouseStock);

        // Per-builder-resource computation
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

                List<Component> tooltipLines = new ArrayList<>();

                if (DomumCraftHandler.isDomumItem(stack))
                {
                    // Domum case: check the final item in the warehouse first,
                    // otherwise resolve the components via the warehouse
                    WarehouseEntry entry = computeDomumWarehouseEntry(
                            stack, missing, reservedStock, warehouseDomumStock, craftingService, tooltipLines);
                    resultEntries.add(new WarehouseResultPacket.WarehouseEntry(
                            stack.copy(), entry.inWarehouse(), entry.viaCraft(), tooltipLines));
                }
                else
                {
                    // Standard case
                    WarehouseEntry entry = computeStandardWarehouseEntry(
                            stack, missing, reservedStock, craftingService, tooltipLines, 0, new HashSet<>());
                    resultEntries.add(new WarehouseResultPacket.WarehouseEntry(
                            stack.copy(), entry.inWarehouse(), entry.viaCraft(), tooltipLines));
                }
            }
        }

        PacketDistributor.sendToPlayer(player,
                new WarehouseResultPacket(resultEntries, currentTick, true));

        player.sendSystemMessage(Component.translatable("colonylink.whs.scan_complete", warehouseStock.size()));
    }

    // Rack scan

    /**
     * Scans the Warehouse racks and fills two stocks:
     * - stock       : standard items aggregated by Item (simple key)
     * - domumStock  : final DO items kept as full ItemStacks so they can be compared
     *                 by isSameItemSameComponents
     * Item (not ItemStack) is used as the key for simplicity; NBT items (Domum) are
     * handled separately via computeDomumWarehouseEntry.
     */
    private static void scanWarehouseRacks(
            com.minecolonies.core.colony.buildings.workerbuildings.BuildingWareHouse warehouse,
            ServerLevel level,
            Map<Item, Long> stock,
            List<ItemStack> domumStock)
    {
        // 1.7.0-beta.2 — only the card's Warehouse is scanned (it used to be every
        // building of the colony whose class name contains "WareHouse"), so the scan
        // shows the same Warehouse that Send and Craft pull from.
        for (IBuilding building : List.<IBuilding>of(warehouse))
        {
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
                            // Final DO item: kept whole for exact NBT comparison
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

    // Standard computation (recursive)

    /**
     * For a standard item, computes:
     * - inWarehouse : quantity directly in the racks (deducted from reservedStock)
     * - viaCraft    : extra quantity craftable if the AE2 components are themselves
     *                 available in the warehouse (recursive resolution)
     *
     * @param visitedItems  cycle guard: items already visited in the current recursion branch
     */
    private static WarehouseEntry computeStandardWarehouseEntry(
            ItemStack stack,
            int needed,
            Map<Item, Long> reservedStock,
            ICraftingService craftingService,
            List<Component> tooltipLines,
            int depth,
            Set<Item> visitedItems)
    {
        Item item = stack.getItem();
        String itemName = stack.getDisplayName().getString();

        // 1. Direct warehouse stock
        long directStock = reservedStock.getOrDefault(item, 0L);
        long directUsed = Math.min(directStock, needed);
        if (directUsed > 0)
        {
            reservedStock.put(item, directStock - directUsed);
            tooltipLines.add(Component.translatable("colonylink.wh_avail.direct", directUsed, itemName));
        }

        long remainingNeeded = needed - directUsed;
        if (remainingNeeded <= 0)
            return new WarehouseEntry(directUsed, 0L);

        // 2. Recursive resolution via AE2 patterns
        if (depth >= MAX_RECURSION_DEPTH || visitedItems.contains(item))
            return new WarehouseEntry(directUsed, 0L);

        if (craftingService == null || !craftingService.isCraftable(AEItemKey.of(stack)))
            return new WarehouseEntry(directUsed, 0L);

        // Get the AE2 pattern for this item
        var patterns = craftingService.getCraftingFor(AEItemKey.of(stack));
        if (patterns == null || patterns.isEmpty())
            return new WarehouseEntry(directUsed, 0L);

        var pattern = patterns.iterator().next();

        // Compute the batch size (pattern output)
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

        // For each needed batch, check whether all components are available
        // in the warehouse (via recursion)
        long batchesNeeded = (long) Math.ceil((double) remainingNeeded / batchOutput);
        long craftableFromWarehouse = 0;

        // Simulate batches one by one until the warehouse stock is exhausted, using a copy
        // of the reservation stock so a partially satisfied batch can be rolled back.
        Map<Item, Long> stockSnapshot = new HashMap<>(reservedStock);
        List<Component> craftTooltip = new ArrayList<>();

        for (long batch = 0; batch < batchesNeeded; batch++)
        {
            boolean batchOk = true;
            Map<Item, Long> batchReservations = new HashMap<>();
            List<Component> batchTooltip = new ArrayList<>();

            for (var input : pattern.getInputs())
            {
                if (input == null) continue;

                // Get the first item of the pattern input via getPossibleInputs()
                // v1.7.1 — multiply by getMultiplier() for correctness with addon patterns.
                ItemStack inputStack = null;
                long inputNeeded = 1;
                for (var possible : input.getPossibleInputs())
                {
                    if (possible.what() instanceof AEItemKey inputKey)
                    {
                        inputStack = inputKey.toStack(1);
                        inputNeeded = possible.amount() * input.getMultiplier();
                        break;
                    }
                }
                if (inputStack == null || inputStack.isEmpty()) continue;
                Item inputItem = inputStack.getItem();

                // Available stock for this component, accounting for reservations
                // already made in this batch
                long alreadyReserved = batchReservations.getOrDefault(inputItem, 0L);
                long effectiveStock = reservedStock.getOrDefault(inputItem, 0L) - alreadyReserved;

                if (effectiveStock >= inputNeeded)
                {
                    // Available directly in the warehouse
                    batchReservations.merge(inputItem, inputNeeded, Long::sum);
                    batchTooltip.add(Component.translatable("colonylink.wh_avail.batch", inputStack.getDisplayName(), inputNeeded));
                }
                else
                {
                    // Not enough directly -> try recursion
                    long stillNeeded = inputNeeded - Math.max(0, effectiveStock);
                    if (effectiveStock > 0)
                        batchReservations.merge(inputItem, effectiveStock, Long::sum);

                    // Recursion: are this component's components in the warehouse?
                    List<Component> subTooltip = new ArrayList<>();
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
                // Apply the batch reservations
                for (var entry : batchReservations.entrySet())
                    reservedStock.merge(entry.getKey(), -entry.getValue(), Long::sum);

                craftableFromWarehouse += batchOutput;
                craftTooltip.addAll(batchTooltip);
            }
            else
            {
                // A batch fails -> stop (stock exhausted)
                break;
            }
        }

        if (craftableFromWarehouse > 0)
        {
            tooltipLines.add(Component.translatable("colonylink.wh_avail.via_craft", craftableFromWarehouse, itemName));
            tooltipLines.addAll(craftTooltip);
        }

        return new WarehouseEntry(directUsed, craftableFromWarehouse);
    }

    // Domum Ornamentum computation

    /**
     * For a Domum item, checks whether the components (texture materials) are in the warehouse,
     * also resolving craftable components recursively from the warehouse.
     */
    private static WarehouseEntry computeDomumWarehouseEntry(
            ItemStack stack,
            int needed,
            Map<Item, Long> reservedStock,
            List<ItemStack> domumStock,
            ICraftingService craftingService,
            List<Component> tooltipLines)
    {
        Item item = stack.getItem();
        if (!(item instanceof BlockItem blockItem)) return new WarehouseEntry(0L, 0L);
        Block block = blockItem.getBlock();
        if (!(block instanceof IMateriallyTexturedBlock texturedBlock)) return new WarehouseEntry(0L, 0L);

        MaterialTextureData textureData = MaterialTextureData.readFromItemStack(stack);

        // 1. Check whether the final DO item is directly in the warehouse
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
            tooltipLines.add(Component.translatable("colonylink.wh_avail.do_direct", directDomum, needed));
            return new WarehouseEntry(Math.min(directDomum, needed), 0L);
        }

        // DO item not (sufficiently) in warehouse -> resolve the components
        tooltipLines.add(Component.translatable("colonylink.wh_avail.do_components", needed, stack.getDisplayName()));

        boolean allComponentsSatisfied = true;
        long satisfiedSets = needed; // how many complete sets can be made

        for (IMateriallyTexturedBlockComponent component : texturedBlock.getComponents())
        {
            Block materialBlock = textureData.getTexturedComponents().get(component.getId());
            if (materialBlock == null)
            {
                if (!component.isOptional())
                {
                    tooltipLines.add(Component.translatable("colonylink.wh_avail.do_not_defined", component.getId().getPath()));
                    allComponentsSatisfied = false;
                }
                continue;
            }

            ItemStack materialStack = new ItemStack(materialBlock, needed);
            String matName = materialStack.getDisplayName().getString();

            List<Component> componentTooltip = new ArrayList<>();
            WarehouseEntry componentEntry = computeStandardWarehouseEntry(
                    materialStack, needed, reservedStock, craftingService,
                    componentTooltip, 0, new HashSet<>());

            long totalForComponent = componentEntry.inWarehouse() + componentEntry.viaCraft();
            long setsFromComponent = totalForComponent; // 1 component per Domum set

            if (setsFromComponent < needed)
            {
                allComponentsSatisfied = false;
                satisfiedSets = Math.min(satisfiedSets, setsFromComponent);
                tooltipLines.add(Component.translatable("colonylink.wh_avail.do_avail", matName, totalForComponent, needed));
            }
            else
            {
                tooltipLines.add(Component.translatable("colonylink.wh_avail.do_ok", matName, totalForComponent, needed));
            }

            tooltipLines.addAll(componentTooltip);
        }

        if (!allComponentsSatisfied && satisfiedSets == 0)
            return new WarehouseEntry(0L, 0L);

        // For Domum the item cannot be "had" directly in the warehouse (it's a virtual
        // craft), so everything goes to viaCraft
        return new WarehouseEntry(0L, satisfiedSets);
    }

    // Helpers

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
     * Looks for the AE2 wand first, then the RS2 wand. Returns the first found, or null.
     */
    private static ItemStack findWandInInventory(ServerPlayer player)
    {
        // Delegate to the shared implementation that also checks Curios slots.
        return ColonyLinkServerTicker.findWandInInventory(player);
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

    // Internal record (used only in this file)

    private record WarehouseEntry(long inWarehouse, long viaCraft) {}
}