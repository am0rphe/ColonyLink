package com.colonylink.colonylink;

import appeng.api.config.Actionable;
import appeng.api.networking.IManagedGridNode;
import appeng.menu.ISubMenu;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageService;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.KeyCounter;
import appeng.items.parts.PartModels;
import appeng.parts.PartModel;
import appeng.parts.reporting.AbstractTerminalPart;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingWareHouse;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Warehouse Link Terminal — Part v1.3.9 (hybride méta/racks).
 *
 * v1.3.9 :
 *  - Fix double-count : la lecture du warehouse passe par le méta-handler
 *    exposé par MineColonies (un handler agrégé de tous les racks, identifié
 *    par getSlots() > 54). Plus rapide, plus juste, et MineColonies gère
 *    pour nous la dédup rack double / rack simple / mix.
 *  - Insertions/extractions inchangées dans leur logique, mais filtrent
 *    désormais explicitement le méta-handler (skip si getSlots() > 54).
 *    Le méta n'est touché que pour la lecture, jamais pour écrire.
 *  - Fallback : si aucun méta-handler n'est trouvé (cas dégénéré, mod
 *    conflict), la lecture retombe sur l'ancien scan des racks individuels
 *    avec la limite RACK_HALF_SLOTS (comportement v1.3.0).
 *
 * Hérite de AbstractTerminalPart pour :
 *  - Rendu AE2 natif (cable arm, canal, ModelData SPIN)
 *  - NBT view cells
 *  - Intégration réseau AE2 (1 canal, nom dans Network Status)
 *
 * Fonctionnalités :
 *  - Slot carte Warehouse Link Card (NBT persisté)
 *  - Scan warehouse MineColonies → snapshot client (via méta-handler)
 *  - Scan ME storage → snapshot client
 *  - Transfers : WH↔ME, WH↔Player, ME↔Player (via racks individuels)
 *  - Pick/deposit : via le carried vanilla (setCarried/getCarried)
 *  - Permissions : owner ou officer de la colonie liée à la card
 *  - Gestion des viewers actifs (sync périodique)
 */
public class WarehouseLinkTerminalPart extends AbstractTerminalPart
{
    // ── Modèles ───────────────────────────────────────────────────────────────

    @PartModels
    public static final ResourceLocation MODEL_OFF =
            ResourceLocation.fromNamespaceAndPath("colonylink", "part/warehouse_link_terminal_off");
    @PartModels
    public static final ResourceLocation MODEL_ON =
            ResourceLocation.fromNamespaceAndPath("colonylink", "part/warehouse_link_terminal_on");

    public static final IPartModel MODELS_OFF         = new PartModel(MODEL_BASE, MODEL_OFF, MODEL_STATUS_OFF);
    public static final IPartModel MODELS_ON          = new PartModel(MODEL_BASE, MODEL_ON,  MODEL_STATUS_ON);
    public static final IPartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE, MODEL_ON,  MODEL_STATUS_HAS_CHANNEL);

    // ── Constantes ────────────────────────────────────────────────────────────

    /** Rayon max pour détecter la colonie depuis la position du Part (blocks). */
    private static final int COLONY_SEARCH_RADIUS = 100;

    /**
     * Nombre de slots locaux d'une moitié de rack MineColonies.
     * - Rack simple : 27 slots
     * - Rack double : 54 slots (27 locaux + 27 miroir de l'autre moitié)
     * Quand on scanne les racks individuels (insert/extract), on s'arrête à 27
     * pour ne pas doublonner les miroirs.
     */
    private static final int RACK_HALF_SLOTS = 27;

    /**
     * Seuil au-dessus duquel un IItemHandler retourné par warehouse.getContainers()
     * est considéré comme le méta-handler du warehouse (agrégat de tous les racks),
     * et non un rack physique.
     *
     * Un rack physique MineColonies a au maximum 54 slots (rack double).
     * Au-delà, c'est forcément le handler agrégé du building (observé empiriquement
     * avec ~1323 slots = ~49 racks × 27 dans un warehouse en cours de remplissage).
     *
     * Utilisé pour :
     *  - LECTURE : on cible CE handler exclusivement (snapshot rapide et déjà
     *              dédupliqué par MineColonies).
     *  - ÉCRITURE : on SKIP ce handler. On n'écrit que dans les racks physiques.
     */
    // META_HANDLER_MIN_SLOTS -- plus utilise pour le filtrage depuis v1.4.7.
    // Le meta-handler est desormais identifie par position (warehouse.getPosition())
    // dans findMetaHandler(), ce qui permet d'inclure le Warehouse Hut (63 slots)
    // dans le scan des racks sans ambiguite.
    @SuppressWarnings("unused")
    private static final int META_HANDLER_MIN_SLOTS = 55;

    // ── État ──────────────────────────────────────────────────────────────────

    /** Slot NBT-persisté pour la Warehouse Link Card. */
    private final ItemStackHandler warehouseCardSlot = new ItemStackHandler(1)
    {
        @Override
        public boolean isItemValid(int slot, ItemStack stack)
        { return stack.getItem() instanceof WarehouseLinkCard; }

        @Override
        protected void onContentsChanged(int slot)
        { getHost().markForSave(); }
    };

    /** Slot NBT-persisté pour l'item Domum cible (onglet Cutter). Limité à 1 item. */
    final ItemStackHandler domumTargetSlot = new ItemStackHandler(1)
    {
        @Override
        public int getSlotLimit(int slot) { return 1; }

        @Override
        public boolean isItemValid(int slot, ItemStack stack)
        { return stack.isEmpty() || DomumCraftHandler.isDomumItem(stack); }

        @Override
        protected void onContentsChanged(int slot)
        { getHost().markForSave(); }
    };

    /** Slot NBT-persisté pour le Blank Pattern AE2 (onglet Cutter). */
    final ItemStackHandler blankPatternSlot = new ItemStackHandler(1)
    {
        @Override
        public boolean isItemValid(int slot, ItemStack stack)
        {
            if (stack.isEmpty()) return true;
            net.minecraft.resources.ResourceLocation id =
                    net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem());
            return id != null && id.getPath().equals("blank_pattern");
        }

        @Override
        protected void onContentsChanged(int slot)
        { getHost().markForSave(); }
    };

    /** Slot output preview (non-persisté, recalculé à l'ouverture). */
    final ItemStackHandler domumOutputSlot = new ItemStackHandler(1)
    {
        @Override public boolean isItemValid(int slot, ItemStack stack) { return false; }
    };

    /** Grille craft 3×3 persistée (9 slots). */
    final ItemStackHandler craftingGridSlot = new ItemStackHandler(9)
    {
        @Override
        protected void onContentsChanged(int slot)
        { getHost().markForSave(); }
    };

    /** Joueurs ayant le GUI ouvert — reçoivent les syncs périodiques. */
    private final Set<UUID> viewers = ConcurrentHashMap.newKeySet();

    /**
     * Queue des items Domum en attente d'encodage — partagée par colonie.
     * Persistée en NBT, synchronisée vers tous les viewers à chaque changement.
     * Une entrée est retirée automatiquement quand AE2 la considère craftable.
     */
    private final List<ItemStack> domumQueue = new java.util.ArrayList<>();

    // ── Diff/throttle v1.4.6 ───────────────────────────────────────────────────
    // Signature du dernier snapshot effectivement envoyé. Si la signature
    // recalculée au tick est identique, on n'envoie rien (zéro alloc au repos).
    private long lastWarehouseSig = Long.MIN_VALUE;
    private long lastMeSig        = Long.MIN_VALUE;

    // ── Constructeur ──────────────────────────────────────────────────────────

    public WarehouseLinkTerminalPart(IPartItem<?> partItem)
    {
        super(partItem);
    }

    // ── Modèle ────────────────────────────────────────────────────────────────

    /**
     * Droppe la Warehouse Link Card quand le terminal est retiré (wrench ou destruction).
     * addAdditionalDrops est la méthode AE2 (IPart) pour les drops additionnels d'une Part.
     * @MustBeInvokedByOverriders → appel super obligatoire.
     */
    @Override
    public void addAdditionalDrops(java.util.List<ItemStack> drops, boolean wrenched)
    {
        super.addAdditionalDrops(drops, wrenched);
        ItemStack card = warehouseCardSlot.getStackInSlot(0);
        if (!card.isEmpty()) drops.add(card.copy());
        ItemStack target = domumTargetSlot.getStackInSlot(0);
        if (!target.isEmpty()) drops.add(target.copy());
        ItemStack blank = blankPatternSlot.getStackInSlot(0);
        if (!blank.isEmpty()) drops.add(blank.copy());
        for (int i = 0; i < craftingGridSlot.getSlots(); i++)
        {
            ItemStack cg = craftingGridSlot.getStackInSlot(i);
            if (!cg.isEmpty()) drops.add(cg.copy());
        }
    }

    @Override
    public IPartModel getStaticModels()
    {
        return this.selectModel(MODELS_OFF, MODELS_ON, MODELS_HAS_CHANNEL);
    }

    // ── NBT ───────────────────────────────────────────────────────────────────

    @Override
    public void writeToNBT(CompoundTag data, HolderLookup.Provider registries)
    {
        super.writeToNBT(data, registries);
        data.put("WarehouseCard", warehouseCardSlot.serializeNBT(registries));
        data.put("DomumTarget", domumTargetSlot.serializeNBT(registries));
        data.put("BlankPattern", blankPatternSlot.serializeNBT(registries));
        // domumOutputSlot intentionnellement NON persisté — slot preview temporaire
        data.put("CraftingGrid", craftingGridSlot.serializeNBT(registries));

        // Queue Domum — liste d'ItemStack sérialisés
        net.minecraft.nbt.ListTag queueTag = new net.minecraft.nbt.ListTag();
        for (ItemStack stack : domumQueue)
        {
            net.minecraft.nbt.CompoundTag stackTag = (net.minecraft.nbt.CompoundTag)
                    ItemStack.CODEC.encodeStart(
                                    registries.createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE),
                                    stack)
                            .resultOrPartial(e -> ColonyLink.LOGGER.error("[WarehouseTerminal] queue save error: {}", e))
                            .orElse(new net.minecraft.nbt.CompoundTag());
            if (!stackTag.isEmpty()) queueTag.add(stackTag);
        }
        data.put("DomumQueue", queueTag);
    }

    @Override
    public void readFromNBT(CompoundTag data, HolderLookup.Provider registries)
    {
        super.readFromNBT(data, registries);
        if (data.contains("WarehouseCard"))
            warehouseCardSlot.deserializeNBT(registries, data.getCompound("WarehouseCard"));
        if (data.contains("DomumTarget"))
            domumTargetSlot.deserializeNBT(registries, data.getCompound("DomumTarget"));
        if (data.contains("BlankPattern"))
            blankPatternSlot.deserializeNBT(registries, data.getCompound("BlankPattern"));
        // domumOutputSlot toujours vide à l'ouverture — pas de désérialisation
        if (data.contains("CraftingGrid"))
            craftingGridSlot.deserializeNBT(registries, data.getCompound("CraftingGrid"));

        // Queue Domum
        domumQueue.clear();
        if (data.contains("DomumQueue", net.minecraft.nbt.Tag.TAG_LIST))
        {
            net.minecraft.nbt.ListTag queueTag =
                    data.getList("DomumQueue", net.minecraft.nbt.Tag.TAG_COMPOUND);
            for (int i = 0; i < queueTag.size(); i++)
            {
                ItemStack.CODEC.parse(
                                registries.createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE),
                                queueTag.getCompound(i))
                        .resultOrPartial(e -> ColonyLink.LOGGER.error("[WarehouseTerminal] queue load error: {}", e))
                        .ifPresent(domumQueue::add);
            }
        }
    }

    // ── Activation (clic droit) ───────────────────────────────────────────────

    @Override
    public boolean onUseWithoutItem(Player player, Vec3 pos)
    {
        if (this.getLevel() == null || this.getLevel().isClientSide()) return true;
        if (!(player instanceof ServerPlayer sp)) return true;

        if (!checkPermission(sp))
        {
            sp.sendSystemMessage(Component.literal(
                    "§c[Terminal] You are not owner or officer of this colony."));
            return true;
        }

        BlockEntity hostBe = this.getBlockEntity();
        if (hostBe == null) return true;

        BlockPos hostPos = hostBe.getBlockPos();
        int sideOrd = this.getSide() != null ? this.getSide().ordinal() : 0;

        sp.openMenu(
                new net.minecraft.world.MenuProvider()
                {
                    @Override
                    public Component getDisplayName()
                    { return Component.literal("Warehouse Link Terminal"); }

                    @Override
                    public net.minecraft.world.inventory.AbstractContainerMenu createMenu(
                            int id, net.minecraft.world.entity.player.Inventory inv, Player p)
                    { return new WarehouseLinkTerminalMenu(id, inv, WarehouseLinkTerminalPart.this); }
                },
                buf -> { buf.writeBlockPos(hostPos); buf.writeByte(sideOrd); }
        );
        return true;
    }

    // ── Tick ──────────────────────────────────────────────────────────────────

    @Override
    public void addToWorld()
    {
        super.addToWorld();
        ColonyLinkServerTicker.registerTerminalPartGlobal(this);
    }

    @Override
    public void removeFromWorld()
    {
        super.removeFromWorld();
        ColonyLinkServerTicker.unregisterTerminalPartGlobal(this);
        ColonyLinkServerTicker.unregisterTerminalPart(this);
    }

    /**
     * Appelé par ColonyLinkServerTicker à chaque fois que le ticker global fire.
     * Envoie le snapshot warehouse + ME à tous les viewers actifs.
     */
    public void serverTick()
    {
        if (viewers.isEmpty()) return;
        if (!(this.getLevel() instanceof ServerLevel level)) return;

        long whSig = computeWarehouseSig(level);
        if (whSig != lastWarehouseSig)
        {
            WarehouseTerminalSyncPacket whPacket = buildWarehouseSnapshot(level);
            for (UUID uid : viewers)
            {
                ServerPlayer sp = level.getServer().getPlayerList().getPlayer(uid);
                if (sp != null)
                    net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(sp, whPacket);
            }
            lastWarehouseSig = whSig;
        }

        long meSig = computeMeSig();
        if (meSig != lastMeSig)
        {
            TerminalMeSyncPacket mePacket = buildMeSnapshot(level);
            for (UUID uid : viewers)
            {
                ServerPlayer sp = level.getServer().getPlayerList().getPlayer(uid);
                if (sp != null)
                    net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(sp, mePacket);
            }
            lastMeSig = meSig;
        }

        // ── Auto-retrait queue Domum quand craftable dans AE2 ─────────────────
        tickDomumQueueCraftableCheck();
    }

    // ── GUI open / close ──────────────────────────────────────────────────────

    public void onGuiOpened(ServerPlayer player)
    {
        viewers.add(player.getUUID());
        ColonyLinkServerTicker.registerTerminalPart(this);
        if (this.getLevel() instanceof ServerLevel level)
        {
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player, buildWarehouseSnapshot(level));
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player, buildMeSnapshot(level));
            // Envoie la queue Domum initiale
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player,
                    new DomumQueueSyncPacket(new java.util.ArrayList<>(domumQueue)));
            // Le snapshot vient d'être envoyé : aligner la signature pour
            // éviter un double envoi au prochain tick.
            lastWarehouseSig = computeWarehouseSig(level);
            lastMeSig        = computeMeSig();
        }
    }

    public void onGuiClosed() {}

    public void onGuiClosed(ServerPlayer player)
    {
        viewers.remove(player.getUUID());
        if (viewers.isEmpty())
            ColonyLinkServerTicker.unregisterTerminalPart(this);
    }

    public void requestImmediateSync()
    {
        // Force le prochain serverTick() à tout renvoyer (le contenu vient de changer).
        lastWarehouseSig = Long.MIN_VALUE;
        lastMeSig        = Long.MIN_VALUE;
        if (viewers.isEmpty()) return;
        if (!(this.getLevel() instanceof ServerLevel level)) return;

        WarehouseTerminalSyncPacket whPacket = buildWarehouseSnapshot(level);
        TerminalMeSyncPacket mePacket = buildMeSnapshot(level);

        for (UUID uid : viewers)
        {
            ServerPlayer sp = level.getServer().getPlayerList().getPlayer(uid);
            if (sp == null) continue;
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(sp, whPacket);
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(sp, mePacket);
        }
    }

    // ── Signatures de contenu (diff/throttle v1.4.6) ──────────────────────────

    /**
     * Signature légère d'un ItemStack : combine l'identité de l'Item (singleton)
     * et ses DataComponents (Domum, NBT…) avec la quantité.
     * N'alloue pas de String.
     */
    private static long stackSig(ItemStack stack, long count)
    {
        int h = Item.getId(stack.getItem());          // int stable, pas d'alloc
        h = 31 * h + stack.getComponents().hashCode();
        return ((long) h << 20) ^ count;
    }

    /**
     * Signature du contenu warehouse. Reproduit le même traversal que
     * buildWarehouseSnapshot (méta-handler ou fallback racks) mais folde un
     * long (FNV-1a) sans allouer de map/liste.
     */
    private long computeWarehouseSig(ServerLevel level)
    {
        if (!hasWarehouseCard()) return 0L;

        IColony colony = findColony(level);
        if (colony == null) return 1L;

        BuildingWareHouse warehouse = findWarehouse(colony);
        if (warehouse == null) return 2L;

        long sig = 0xcbf29ce484222325L; // FNV offset basis
        final long FNV_PRIME = 0x100000001b3L;

        IItemHandler metaHandler = findMetaHandler(level, warehouse);
        if (metaHandler != null)
        {
            for (int s = 0; s < metaHandler.getSlots(); s++)
            {
                ItemStack inSlot = metaHandler.getStackInSlot(s);
                if (inSlot.isEmpty()) continue;
                sig = (sig ^ stackSig(inSlot, inSlot.getCount())) * FNV_PRIME;
            }
        }
        else
        {
            java.util.Set<BlockPos> visitedPos = new java.util.HashSet<>();
            for (BlockPos rackPos : warehouse.getContainers())
            {
                if (!visitedPos.add(rackPos)) continue;
                IItemHandler rack = level.getCapability(Capabilities.ItemHandler.BLOCK, rackPos, null);
                if (rack == null) continue;
                int slots = Math.min(rack.getSlots(), RACK_HALF_SLOTS);
                for (int s = 0; s < slots; s++)
                {
                    ItemStack inSlot = rack.getStackInSlot(s);
                    if (inSlot.isEmpty()) continue;
                    sig = (sig ^ stackSig(inSlot, inSlot.getCount())) * FNV_PRIME;
                }
            }
        }
        return sig;
    }

    /**
     * Signature du contenu ME. Même accès réseau que buildMeSnapshot : folde le
     * stock (clé + count) et le nombre de craftables.
     */
    private long computeMeSig()
    {
        if (!isActive()) return -1L;
        var node = this.getMainNode().getNode();
        if (node == null) return -1L;
        var grid = node.getGrid();

        long sig = 0xcbf29ce484222325L;
        final long FNV_PRIME = 0x100000001b3L;

        // Stock
        appeng.api.stacks.KeyCounter inv = grid.getStorageService().getCachedInventory();
        for (var entry : inv)
        {
            if (!(entry.getKey() instanceof AEItemKey k)) continue;
            sig = (sig ^ ((long) k.hashCode() << 20 ^ entry.getLongValue())) * FNV_PRIME;
        }

        // Craftables (taille du set suffit à détecter un changement)
        try
        {
            ICraftingService crafting = grid.getCraftingService();
            long craftCount = crafting.getCraftables(k -> true).size();
            sig = (sig ^ craftCount) * FNV_PRIME;
        }
        catch (Exception ignored) {}

        return sig;
    }

    // ── Snapshots ─────────────────────────────────────────────────────────────

    /**
     * Construit le snapshot warehouse pour le client.
     *
     * v1.3.9 — Stratégie hybride :
     *
     *  1. On scanne getContainers() à la recherche du méta-handler (le handler
     *     agrégé exposé par le building, identifié par getSlots() >= 55).
     *
     *  2. Si trouvé : on lit UNIQUEMENT le méta-handler. C'est MineColonies qui
     *     gère pour nous la dédup rack double et l'agrégation des stacks
     *     identiques répartis sur plusieurs racks. Aucun risque de
     *     double-comptage.
     *
     *  3. Si non trouvé (cas dégénéré : warehouse atypique, mod conflict, etc.) :
     *     fallback sur l'ancien scan des racks individuels avec la limite
     *     RACK_HALF_SLOTS=27 par handler. Comportement v1.3.0.
     */
    private WarehouseTerminalSyncPacket buildWarehouseSnapshot(ServerLevel level)
    {
        BlockPos hostPos = this.getBlockEntity() != null ? this.getBlockEntity().getBlockPos() : BlockPos.ZERO;
        boolean hasCard = hasWarehouseCard();

        if (!hasCard)
            return new WarehouseTerminalSyncPacket(List.of(), false, hostPos, "");

        IColony colony = findColony(level);
        if (colony == null)
            return new WarehouseTerminalSyncPacket(List.of(), true, hostPos, "No colony found");

        BuildingWareHouse warehouse = findWarehouse(colony);
        if (warehouse == null)
            return new WarehouseTerminalSyncPacket(List.of(), true, hostPos, "No warehouse nearby");

        java.util.Map<String, WarehouseTerminalSyncPacket.WarehouseItemEntry> aggregated = new java.util.LinkedHashMap<>();

        // Étape 1 : chercher le méta-handler.
        IItemHandler metaHandler = findMetaHandler(level, warehouse);

        if (metaHandler != null)
        {
            // Lecture via méta : scan unique de tous les slots, sans dédup.
            int slots = metaHandler.getSlots();
            for (int s = 0; s < slots; s++)
            {
                ItemStack inSlot = metaHandler.getStackInSlot(s);
                if (inSlot.isEmpty()) continue;
                aggregateItem(aggregated, inSlot);
            }
        }
        else
        {
            // Fallback : scan des racks individuels (comportement v1.3.0).
            // Déduplication par position (rack double = 2 BlockPos) et limite
            // RACK_HALF_SLOTS pour ne pas re-lire les slots miroir.
            java.util.Set<BlockPos> visitedPos = new java.util.HashSet<>();
            for (BlockPos rackPos : warehouse.getContainers())
            {
                if (!visitedPos.add(rackPos)) continue;
                IItemHandler rack = level.getCapability(Capabilities.ItemHandler.BLOCK, rackPos, null);
                if (rack == null) continue;
                // Sécurité : si un méta-handler arrive ici, on le saute (il sera
                // beaucoup trop gros et fausserait le total). En pratique on
                // n'arrive ici que si findMetaHandler() a retourné null, donc
                // ce filtre est défensif.
                int slots = Math.min(rack.getSlots(), RACK_HALF_SLOTS);
                for (int s = 0; s < slots; s++)
                {
                    ItemStack inSlot = rack.getStackInSlot(s);
                    if (inSlot.isEmpty()) continue;
                    aggregateItem(aggregated, inSlot);
                }
            }
        }

        List<WarehouseTerminalSyncPacket.WarehouseItemEntry> entries = new ArrayList<>(aggregated.values());
        entries.sort((a, b) -> Long.compare(b.count(), a.count()));

        return new WarehouseTerminalSyncPacket(entries, true, hostPos, "");
    }

    /**
     * Helper : fusionne un ItemStack dans la map d'agrégation par identité d'item
     * (item + components). Utilisé par les deux branches de buildWarehouseSnapshot.
     */
    private static void aggregateItem(
            java.util.Map<String, WarehouseTerminalSyncPacket.WarehouseItemEntry> aggregated,
            ItemStack inSlot)
    {
        String key = net.minecraft.core.registries.BuiltInRegistries.ITEM
                .getKey(inSlot.getItem()).toString()
                + inSlot.getComponents().toString();
        aggregated.merge(key,
                new WarehouseTerminalSyncPacket.WarehouseItemEntry(inSlot.copyWithCount(1), inSlot.getCount()),
                (a, b) -> new WarehouseTerminalSyncPacket.WarehouseItemEntry(a.stack(), a.count() + b.count()));
    }

    /**
     * Cherche le méta-handler exposé par le warehouse MineColonies.
     *
     * MineColonies expose dans getContainers() à la fois :
     *  - tous les racks individuels (27 ou 54 slots chacun)
     *  - un handler agrégé "warehouse-as-a-whole" (généralement 100+ slots)
     *
     * Le méta-handler est identifié par sa taille : >= 55 slots.
     * Au plus un seul méta-handler par warehouse en pratique.
     *
     * @return le méta-handler trouvé, ou null s'il n'y en a pas (fallback).
     */
    /**
     * Identifie le meta-handler MineColonies par la position du bloc Warehouse Hut.
     * Le meta-handler est expose a warehouse.getPosition() (position du bloc batiment),
     * pas a la position d'un rack. Cette approche est independante du nombre de slots
     * et fonctionne quelle que soit la taille du warehouse.
     *
     * L'inventaire interne du bloc Warehouse Hut (9x7 = 63 slots) est traite
     * separement dans buildWarehouseSnapshot/pickupFromWarehouse.
     */
    private static IItemHandler findMetaHandler(ServerLevel level, BuildingWareHouse warehouse)
    {
        // Le meta-handler MineColonies est toujours expose a la position du batiment
        BlockPos hutPos = warehouse.getPosition();
        IItemHandler h = level.getCapability(Capabilities.ItemHandler.BLOCK, hutPos, null);
        // Verifier que ce handler a plus de slots qu'un rack simple (27 ou 54 slots)
        // pour s'assurer que c'est bien le meta-handler et non un rack place sur le hut
        if (h != null && h.getSlots() > RACK_HALF_SLOTS)
            return h;
        return null;
    }

    private TerminalMeSyncPacket buildMeSnapshot(ServerLevel level)
    {
        BlockPos hostPos = this.getBlockEntity() != null ? this.getBlockEntity().getBlockPos() : BlockPos.ZERO;
        if (!isActive()) return new TerminalMeSyncPacket(List.of(), hostPos);
        var node = this.getMainNode().getNode();
        if (node == null) return new TerminalMeSyncPacket(List.of(), hostPos);
        var grid = node.getGrid();
        return TerminalMeSyncPacket.fromGrid(grid, hostPos);
    }

    // ── PICK / DEPOSIT (via carried vanilla) ──────────────────────────────────


    /**
     * Deposit depuis le carried vers le WH.
     * Si count < carried.count : dépose seulement count items (clic droit).
     * Si count >= carried.count ou count == -1 : dépose tout.
     */
    public void depositToWarehouse(ServerPlayer player, int count)
    {
        if (!checkPermission(player)) return;

        ItemStack carried = player.containerMenu.getCarried();
        if (carried.isEmpty()) return;

        ServerLevel level = player.serverLevel();
        IColony colony = findColony(level);
        if (colony == null) { msg(player, "§cNo colony found."); return; }
        BuildingWareHouse wh = findWarehouse(colony);
        if (wh == null) { msg(player, "§cNo warehouse found."); return; }
        // v1.4.9 — fail-off strict : warehouse entièrement chargé requis.
        if (!ColonyLinkChunkUtil.warehouseFullyLoaded(level, wh))
        {
            msg(player, "§c[Terminal] Warehouse is in unloaded chunks — move closer or use a chunk loader.");
            return;
        }

        int toDeposit = (count <= 0 || count >= carried.getCount()) ? carried.getCount() : count;
        ItemStack toInsert = carried.copyWithCount(toDeposit);

        int inserted = 0;
        java.util.Set<BlockPos> visitedPos = new java.util.HashSet<>();
        java.util.Set<IItemHandler> visited = java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
        for (BlockPos rackPos : wh.getContainers())
        {
            if (toInsert.isEmpty()) break;
            if (!visitedPos.add(rackPos)) continue;
            IItemHandler rack = level.getCapability(Capabilities.ItemHandler.BLOCK, rackPos, null);
            if (rack == null || !visited.add(rack)) continue;
            // v1.3.9 — skip méta-handler : on n'écrit jamais via l'agrégat.
            ItemStack before = toInsert.copy();
            toInsert = insertIntoHandler(rack, toInsert);
            inserted += before.getCount() - toInsert.getCount();
        }

        if (inserted > 0)
        {
            // Réduire le carried
            carried.shrink(inserted);
            player.containerMenu.setCarried(carried.isEmpty() ? ItemStack.EMPTY : carried);
            player.containerMenu.broadcastChanges();
        }
        else
            msg(player, "§c[Terminal] Warehouse full.");
    }

    /**
     * Deposit depuis le carried vers le ME.
     */
    public void depositToMe(ServerPlayer player, int count)
    {
        if (!checkPermission(player)) return;

        ItemStack carried = player.containerMenu.getCarried();
        if (carried.isEmpty()) return;

        var node = this.getMainNode().getNode();
        if (node == null) { msg(player, "§cAE2 network unavailable."); return; }
        var storage = node.getGrid().getStorageService().getInventory();
        IActionSource src = IActionSource.ofMachine(this);
        AEItemKey key = AEItemKey.of(carried);

        int toDeposit = (count <= 0 || count >= carried.getCount()) ? carried.getCount() : count;
        long inserted = storage.insert(key, toDeposit, Actionable.MODULATE, src);

        if (inserted > 0)
        {
            carried.shrink((int) inserted);
            player.containerMenu.setCarried(carried.isEmpty() ? ItemStack.EMPTY : carried);
            player.containerMenu.broadcastChanges();
        }
        else
            msg(player, "§c[Terminal] ME full.");
    }

    // ── Transferts sélection (flèches centrales) ──────────────────────────────

    // ── PICKUP/PUT (style AE2 PICKUP_OR_SET_DOWN) ─────────────────────────────

    public void pickupFromWarehouse(ServerPlayer player, ItemStack template, int requestCount)
    {
        if (!checkPermission(player)) return;
        ServerLevel level = player.serverLevel();
        IColony colony = findColony(level);
        if (colony == null) { msg(player, "§cNo colony found."); return; }
        BuildingWareHouse wh = findWarehouse(colony);
        if (wh == null) { msg(player, "§cNo warehouse found."); return; }
        // v1.4.9 — fail-off strict : warehouse entièrement chargé requis.
        if (!ColonyLinkChunkUtil.warehouseFullyLoaded(level, wh))
        {
            msg(player, "§c[Terminal] Warehouse is in unloaded chunks — move closer or use a chunk loader.");
            return;
        }

        ItemStack carried = player.containerMenu.getCarried();

        // Item différent dans le curseur → dépose d'abord dans le WH
        if (!carried.isEmpty() && !ItemStack.isSameItemSameComponents(carried, template))
        {
            int deposited = transferPlayerToWarehouse(player, carried.copy());
            if (deposited > 0) carried.shrink(deposited);
            player.containerMenu.setCarried(carried.isEmpty() ? ItemStack.EMPTY : carried);
            syncCarried(player);
            return;
        }

        int maxCarried = template.getMaxStackSize();
        int alreadyCarried = carried.isEmpty() ? 0 : carried.getCount();
        int canPick = Math.min(requestCount, maxCarried - alreadyCarried);
        if (canPick <= 0) { syncCarried(player); return; }

        int remaining = canPick;
        int totalExtracted = 0;
        java.util.Set<BlockPos> visitedPos = new java.util.HashSet<>();
        java.util.Set<IItemHandler> visited =
                java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
        BlockPos hutPos = wh.getPosition();
        for (BlockPos rackPos : wh.getContainers())
        {
            if (remaining <= 0) break;
            if (!visitedPos.add(rackPos)) continue;
            IItemHandler rack = level.getCapability(Capabilities.ItemHandler.BLOCK, rackPos, null);
            if (rack == null || !visited.add(rack)) continue;
            // Pour le bloc Warehouse Hut (position du batiment), scanner tous les slots.
            // Pour les racks normaux, limiter a RACK_HALF_SLOTS (moitie exposee).
            int slots = rackPos.equals(hutPos) ? rack.getSlots()
                    : Math.min(rack.getSlots(), RACK_HALF_SLOTS);
            for (int s = 0; s < slots && remaining > 0; s++)
            {
                ItemStack inSlot = rack.getStackInSlot(s);
                if (inSlot.isEmpty() || !ItemStack.isSameItemSameComponents(inSlot, template)) continue;
                int toGet = Math.min(remaining, inSlot.getCount());
                ItemStack got = rack.extractItem(s, toGet, false);
                if (!got.isEmpty()) { totalExtracted += got.getCount(); remaining -= got.getCount(); }
            }
        }

        if (totalExtracted == 0) { syncCarried(player); return; }

        if (carried.isEmpty())
            player.containerMenu.setCarried(template.copyWithCount(totalExtracted));
        else { carried.grow(totalExtracted); player.containerMenu.setCarried(carried); }

        syncCarried(player);
    }

    public void pickupFromMe(ServerPlayer player, ItemStack template, int requestCount)
    {
        if (!checkPermission(player)) return;
        var node = this.getMainNode().getNode();
        if (node == null) { msg(player, "§cAE2 network unavailable."); return; }
        var storage = node.getGrid().getStorageService().getInventory();
        IActionSource src = IActionSource.ofMachine(this);
        AEItemKey key = AEItemKey.of(template);
        if (key == null) return;

        ItemStack carried = player.containerMenu.getCarried();

        // Item différent dans le curseur → dépose d'abord dans ME
        if (!carried.isEmpty() && !ItemStack.isSameItemSameComponents(carried, template))
        {
            AEItemKey carriedKey = AEItemKey.of(carried);
            if (carriedKey != null)
            {
                long ins = storage.insert(carriedKey, carried.getCount(), Actionable.MODULATE, src);
                carried.shrink((int) ins);
            }
            player.containerMenu.setCarried(carried.isEmpty() ? ItemStack.EMPTY : carried);
            syncCarried(player);
            return;
        }

        int maxCarried = template.getMaxStackSize();
        int alreadyCarried = carried.isEmpty() ? 0 : carried.getCount();
        int canPick = Math.min(requestCount, maxCarried - alreadyCarried);
        if (canPick <= 0) { syncCarried(player); return; }

        long extracted = storage.extract(key, canPick, Actionable.MODULATE, src);
        if (extracted <= 0) { syncCarried(player); return; }

        if (carried.isEmpty())
            player.containerMenu.setCarried(key.toStack((int) extracted));
        else { carried.grow((int) extracted); player.containerMenu.setCarried(carried); }

        syncCarried(player);
    }

    public void putCarriedIntoWarehouse(ServerPlayer player)
    {
        ItemStack carried = player.containerMenu.getCarried();
        if (carried.isEmpty()) return;
        if (!checkPermission(player)) return;
        int inserted = transferPlayerToWarehouse(player, carried.copy());
        carried.shrink(inserted);
        player.containerMenu.setCarried(carried.isEmpty() ? ItemStack.EMPTY : carried);
        syncCarried(player);
    }

    public void putCarriedIntoMe(ServerPlayer player)
    {
        ItemStack carried = player.containerMenu.getCarried();
        if (carried.isEmpty()) return;
        var node = this.getMainNode().getNode();
        if (node == null) return;
        var storage = node.getGrid().getStorageService().getInventory();
        IActionSource src = IActionSource.ofMachine(this);
        AEItemKey key = AEItemKey.of(carried);
        if (key == null) return;
        long inserted = storage.insert(key, carried.getCount(), Actionable.MODULATE, src);
        carried.shrink((int) inserted);
        player.containerMenu.setCarried(carried.isEmpty() ? ItemStack.EMPTY : carried);
        syncCarried(player);
    }


    /**
     * WH → ME (flèches centrales / sélection).
     */
    public int transferWarehouseToMe(ServerPlayer player, ItemStack template, int count)
    {
        if (!checkPermission(player)) return 0;
        ServerLevel level = player.serverLevel();
        IColony colony = findColony(level);
        if (colony == null) { msg(player, "§cNo colony found."); return 0; }
        BuildingWareHouse wh = findWarehouse(colony);
        if (wh == null) { msg(player, "§cNo warehouse found."); return 0; }
        // v1.4.9 — fail-off strict : warehouse entièrement chargé requis.
        if (!ColonyLinkChunkUtil.warehouseFullyLoaded(level, wh))
        {
            msg(player, "§c[Terminal] Warehouse is in unloaded chunks — move closer or use a chunk loader.");
            return 0;
        }

        var node = this.getMainNode().getNode();
        if (node == null) { msg(player, "§cAE2 network unavailable."); return 0; }
        var storage = node.getGrid().getStorageService().getInventory();
        IActionSource src = IActionSource.ofMachine(this);
        AEItemKey key = AEItemKey.of(template);

        List<ItemStack> extracted = new ArrayList<>();
        int remaining = count;
        java.util.Set<BlockPos> visitedPos = new java.util.HashSet<>();
        java.util.Set<IItemHandler> visited = java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
        for (BlockPos rackPos : wh.getContainers())
        {
            if (remaining <= 0) break;
            if (!visitedPos.add(rackPos)) continue;
            IItemHandler rack = level.getCapability(Capabilities.ItemHandler.BLOCK, rackPos, null);
            if (rack == null || !visited.add(rack)) continue;
            // v1.3.9 — skip méta-handler.
            for (int s = 0; s < Math.min(rack.getSlots(), RACK_HALF_SLOTS) && remaining > 0; s++)
            {
                ItemStack inSlot = rack.getStackInSlot(s);
                if (inSlot.isEmpty() || !ItemStack.isSameItemSameComponents(inSlot, template)) continue;
                int toGet = Math.min(remaining, inSlot.getCount());
                ItemStack got = rack.extractItem(s, toGet, false);
                if (!got.isEmpty()) { extracted.add(got); remaining -= got.getCount(); }
            }
        }

        if (extracted.isEmpty()) { msg(player, "§c[Terminal] Item not found in warehouse."); return 0; }

        int transferred = 0;
        List<ItemStack> toReturn = new ArrayList<>();
        for (ItemStack stack : extracted)
        {
            long inserted = storage.insert(key, stack.getCount(), Actionable.MODULATE, src);
            transferred += (int) inserted;
            long notInserted = stack.getCount() - inserted;
            if (notInserted > 0) toReturn.add(stack.copyWithCount((int) notInserted));
        }

        java.util.Set<IItemHandler> retVisited = java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
        for (ItemStack ret : toReturn)
        {
            for (BlockPos rackPos : wh.getContainers())
            {
                if (ret.isEmpty()) break;
                IItemHandler rack = level.getCapability(Capabilities.ItemHandler.BLOCK, rackPos, null);
                if (rack == null || !retVisited.add(rack)) continue;
                // v1.3.9 — skip méta-handler.
                ret = insertIntoHandler(rack, ret);
            }
        }

        if (transferred == 0) msg(player, "§c[Terminal] ME full or item not found.");
        return transferred;
    }

    /**
     * WH → Inventaire joueur (flèches centrales / sélection).
     */
    public int transferWarehouseToInventory(ServerPlayer player, ItemStack template, int count)
    {
        if (!checkPermission(player)) return 0;
        ServerLevel level = player.serverLevel();
        IColony colony = findColony(level);
        if (colony == null) { msg(player, "§cNo colony found."); return 0; }
        BuildingWareHouse wh = findWarehouse(colony);
        if (wh == null) { msg(player, "§cNo warehouse found."); return 0; }
        // v1.4.9 — fail-off strict : warehouse entièrement chargé requis.
        if (!ColonyLinkChunkUtil.warehouseFullyLoaded(level, wh))
        {
            msg(player, "§c[Terminal] Warehouse is in unloaded chunks — move closer or use a chunk loader.");
            return 0;
        }

        List<ItemStack> extracted = new ArrayList<>();
        int remaining = count;
        java.util.Set<BlockPos> visitedPos = new java.util.HashSet<>();
        java.util.Set<IItemHandler> visited = java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
        for (BlockPos rackPos : wh.getContainers())
        {
            if (remaining <= 0) break;
            if (!visitedPos.add(rackPos)) continue;
            IItemHandler rack = level.getCapability(Capabilities.ItemHandler.BLOCK, rackPos, null);
            if (rack == null || !visited.add(rack)) continue;
            // v1.3.9 — skip méta-handler.
            for (int s = 0; s < Math.min(rack.getSlots(), RACK_HALF_SLOTS) && remaining > 0; s++)
            {
                ItemStack inSlot = rack.getStackInSlot(s);
                if (inSlot.isEmpty() || !ItemStack.isSameItemSameComponents(inSlot, template)) continue;
                int toGet = Math.min(remaining, inSlot.getCount());
                ItemStack got = rack.extractItem(s, toGet, false);
                if (!got.isEmpty()) { extracted.add(got); remaining -= got.getCount(); }
            }
        }

        if (extracted.isEmpty()) { msg(player, "§c[Terminal] Item not found in warehouse."); return 0; }

        ItemStack toGive = extracted.get(0).copy();
        for (int i = 1; i < extracted.size(); i++) toGive.grow(extracted.get(i).getCount());

        boolean added = player.getInventory().add(toGive.copy());
        if (!added)
        {
            // Remettre dans WH
            java.util.Set<IItemHandler> retVis = java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
            for (BlockPos rp : wh.getContainers())
            {
                if (toGive.isEmpty()) break;
                IItemHandler rack = level.getCapability(Capabilities.ItemHandler.BLOCK, rp, null);
                if (rack == null || !retVis.add(rack)) continue;
                // v1.3.9 — skip méta-handler.
                toGive = insertIntoHandler(rack, toGive);
            }
            msg(player, "§e[Terminal] Inventory full.");
            return 0;
        }
        return toGive.getCount();
    }

    /**
     * ME → WH (flèches centrales / sélection).
     */
    public int transferMeToWarehouse(ServerPlayer player, ItemStack template, int count)
    {
        if (!checkPermission(player)) return 0;
        ServerLevel level = player.serverLevel();
        IColony colony = findColony(level);
        if (colony == null) { msg(player, "§cNo colony found."); return 0; }
        BuildingWareHouse wh = findWarehouse(colony);
        if (wh == null) { msg(player, "§cNo warehouse found."); return 0; }
        // v1.4.9 — fail-off strict : warehouse entièrement chargé requis.
        if (!ColonyLinkChunkUtil.warehouseFullyLoaded(level, wh))
        {
            msg(player, "§c[Terminal] Warehouse is in unloaded chunks — move closer or use a chunk loader.");
            return 0;
        }

        var node = this.getMainNode().getNode();
        if (node == null) { msg(player, "§cAE2 network unavailable."); return 0; }
        var storage = node.getGrid().getStorageService().getInventory();
        IActionSource src = IActionSource.ofMachine(this);
        AEItemKey key = AEItemKey.of(template);

        long extracted = storage.extract(key, count, Actionable.MODULATE, src);
        if (extracted <= 0) { msg(player, "§c[Terminal] Item not found in ME."); return 0; }

        ItemStack remaining = key.toStack((int) extracted);
        java.util.Set<BlockPos> visitedPos = new java.util.HashSet<>();
        java.util.Set<IItemHandler> visited = java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
        for (BlockPos rackPos : wh.getContainers())
        {
            if (remaining.isEmpty()) break;
            if (!visitedPos.add(rackPos)) continue;
            IItemHandler rack = level.getCapability(Capabilities.ItemHandler.BLOCK, rackPos, null);
            if (rack == null || !visited.add(rack)) continue;
            // v1.3.9 — skip méta-handler.
            remaining = insertIntoHandler(rack, remaining);
        }

        int inserted = (int)(extracted - remaining.getCount());
        if (!remaining.isEmpty())
        {
            storage.insert(key, remaining.getCount(), Actionable.MODULATE, src);
            if (inserted == 0) msg(player, "§c[Terminal] Warehouse full.");
            else msg(player, "§e[Terminal] Warehouse full — only " + inserted + " inserted.");
        }
        return inserted;
    }

    /**
     * ME → Inventaire joueur (flèches centrales / sélection).
     */
    public int transferMeToInventory(ServerPlayer player, ItemStack template, int count)
    {
        if (!checkPermission(player)) return 0;

        var node = this.getMainNode().getNode();
        if (node == null) { msg(player, "§cAE2 network unavailable."); return 0; }
        var storage = node.getGrid().getStorageService().getInventory();
        IActionSource src = IActionSource.ofMachine(this);
        AEItemKey key = AEItemKey.of(template);

        long extracted = storage.extract(key, count, Actionable.MODULATE, src);
        if (extracted <= 0) { msg(player, "§c[Terminal] Item not found in ME."); return 0; }

        ItemStack toGive = key.toStack((int) extracted);
        boolean added = player.getInventory().add(toGive.copy());
        if (!added)
        {
            storage.insert(key, extracted, Actionable.MODULATE, src);
            msg(player, "§e[Terminal] Inventory full.");
            return 0;
        }
        return (int) extracted;
    }

    /**
     * Vide la grille de craft vers une destination.
     * @param dest 0=WH, 1=Player, 2=ME
     */
    public int transferCraftGridContents(ServerPlayer player,
                                         net.minecraft.world.inventory.TransientCraftingContainer grid,
                                         int dest)
    {
        int total = 0;
        for (int i = 0; i < grid.getContainerSize(); i++)
        {
            ItemStack stack = grid.getItem(i);
            if (stack.isEmpty()) continue;
            int sent = switch (dest)
            {
                case 0 -> transferPlayerToWarehouse(player, stack.copy());
                case 1 -> { boolean ok = player.getInventory().add(stack.copy()); yield ok ? stack.getCount() : 0; }
                case 2 -> transferPlayerToMe(player, stack.copy());
                default -> 0;
            };
            if (sent > 0)
            {
                stack.shrink(sent);
                if (stack.isEmpty()) grid.setItem(i, ItemStack.EMPTY);
                total += sent;
            }
        }
        return total;
    }

    /** Insère un item depuis l'inventaire du joueur vers le WH. */
    public int transferPlayerToWarehouse(ServerPlayer player, ItemStack stack)
    {
        if (!checkPermission(player)) return 0;
        ServerLevel level = player.serverLevel();
        IColony colony = findColony(level);
        if (colony == null) return 0;
        BuildingWareHouse wh = findWarehouse(colony);
        if (wh == null) return 0;
        // v1.4.9 — fail-off strict : warehouse entièrement chargé requis.
        if (!ColonyLinkChunkUtil.warehouseFullyLoaded(level, wh))
        {
            msg(player, "§c[Terminal] Warehouse is in unloaded chunks — move closer or use a chunk loader.");
            return 0;
        }

        int original = stack.getCount();
        java.util.Set<BlockPos> visitedPos = new java.util.HashSet<>();
        java.util.Set<IItemHandler> visited = java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
        for (BlockPos rackPos : wh.getContainers())
        {
            if (stack.isEmpty()) break;
            if (!visitedPos.add(rackPos)) continue;
            IItemHandler rack = level.getCapability(Capabilities.ItemHandler.BLOCK, rackPos, null);
            if (rack == null || !visited.add(rack)) continue;
            // v1.3.9 — skip méta-handler.
            stack = insertIntoHandler(rack, stack);
        }
        return original - stack.getCount();
    }

    /** Insère un item dans le ME. */
    public int transferPlayerToMe(ServerPlayer player, ItemStack stack)
    {
        var node = this.getMainNode().getNode();
        if (node == null) return 0;
        var storage = node.getGrid().getStorageService().getInventory();
        IActionSource src = IActionSource.ofMachine(this);
        AEItemKey key = AEItemKey.of(stack);
        long inserted = storage.insert(key, stack.getCount(), Actionable.MODULATE, src);
        if (inserted < stack.getCount())
            msg(player, "§e[Terminal] ME full — " + (stack.getCount()-inserted) + "x not inserted.");
        return (int) inserted;
    }

    /**
     * Rouvre notre WarehouseLinkTerminalMenu quand l'utilisateur clique
     * "Retour" depuis le CraftAmountMenu ou CraftConfirmMenu AE2.
     */
    @Override
    public void returnToMainMenu(net.minecraft.world.entity.player.Player player,
                                 ISubMenu subMenu)
    {
        if (this.getLevel() == null || this.getLevel().isClientSide()) return;
        if (!(player instanceof net.minecraft.server.level.ServerPlayer sp)) return;
        BlockEntity hostBe = this.getBlockEntity();
        if (hostBe == null) return;
        net.minecraft.core.BlockPos hostPos = hostBe.getBlockPos();
        int sideOrd = this.getSide() != null ? this.getSide().ordinal() : 0;
        sp.openMenu(
                new net.minecraft.world.MenuProvider()
                {
                    @Override public net.minecraft.network.chat.Component getDisplayName()
                    { return net.minecraft.network.chat.Component.literal("Warehouse Link Terminal"); }
                    @Override public net.minecraft.world.inventory.AbstractContainerMenu createMenu(
                            int id, net.minecraft.world.entity.player.Inventory inv,
                            net.minecraft.world.entity.player.Player p)
                    { return new WarehouseLinkTerminalMenu(id, inv, WarehouseLinkTerminalPart.this); }
                },
                buf -> { buf.writeBlockPos(hostPos); buf.writeByte(sideOrd); }
        );
    }

    // ── Queue Domum ───────────────────────────────────────────────────────────

    /**
     * Ajoute un item Domum à la queue d'encodage.
     * Persiste le Part et broadcast la queue mise à jour à tous les viewers.
     */
    public void addToDomumQueue(ItemStack stack)
    {
        domumQueue.add(stack.copyWithCount(1));
        getHost().markForSave();
        broadcastDomumQueue();
    }

    /**
     * Retire un item de la queue par index.
     * Utilisé par DomumEncodePatternPacket après encodage réussi.
     */
    public void removeFromDomumQueue(int index)
    {
        if (index < 0 || index >= domumQueue.size()) return;
        domumQueue.remove(index);
        getHost().markForSave();
        broadcastDomumQueue();
    }

    /**
     * Vérifie si un ItemStack Domum exact est déjà dans la queue (dédup).
     * Comparaison stricte : item + DataComponents (bloc + MaterialTextureData + BLOCK_STATE).
     */
    public boolean isDomumQueued(ItemStack stack)
    {
        for (ItemStack queued : domumQueue)
            if (ItemStack.isSameItemSameComponents(queued, stack)) return true;
        return false;
    }

    /** Retourne une vue non-modifiable de la queue (pour le menu / screen). */
    public List<ItemStack> getDomumQueue()
    {
        return java.util.Collections.unmodifiableList(domumQueue);
    }

    /**
     * Vérifie à chaque serverTick si des items de la queue sont devenus craftables
     * dans AE2 → les retire automatiquement.
     */
    private void tickDomumQueueCraftableCheck()
    {
        if (domumQueue.isEmpty()) return;
        if (!isActive()) return;
        var node = this.getMainNode().getNode();
        if (node == null) return;

        ICraftingService crafting;
        try { crafting = node.getGrid().getCraftingService(); }
        catch (Exception e) { return; }

        boolean changed = false;
        for (int i = domumQueue.size() - 1; i >= 0; i--)
        {
            AEItemKey key = AEItemKey.of(domumQueue.get(i));
            if (key != null && crafting.isCraftable(key))
            {
                domumQueue.remove(i);
                changed = true;
            }
        }
        if (changed)
        {
            getHost().markForSave();
            broadcastDomumQueue();
        }
    }

    /**
     * Envoie la queue complète à tous les viewers actifs.
     */
    private void broadcastDomumQueue()
    {
        if (viewers.isEmpty()) return;
        if (!(this.getLevel() instanceof net.minecraft.server.level.ServerLevel level)) return;
        DomumQueueSyncPacket packet = new DomumQueueSyncPacket(new java.util.ArrayList<>(domumQueue));
        for (UUID uid : viewers)
        {
            net.minecraft.server.level.ServerPlayer sp =
                    level.getServer().getPlayerList().getPlayer(uid);
            if (sp != null)
                net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(sp, packet);
        }
    }

    // ── Getters publics ───────────────────────────────────────────────────────

    public boolean isAe2Active()     { return this.isActive(); }
    public IManagedGridNode getManagedGridNode() { return this.getMainNode(); }
    public BlockEntity getHostBlockEntity()      { return this.getBlockEntity(); }
    public ItemStackHandler getWarehouseCardSlot() { return warehouseCardSlot; }
    public boolean hasWarehouseCard() { return !warehouseCardSlot.getStackInSlot(0).isEmpty(); }

    // ── Helpers privés ────────────────────────────────────────────────────────


    /**
     * Synchronise le carried item (curseur) vers le client.
     * Équivalent de ServerPlayer.broadcastCarriedItem() qui n'existe pas en 1.21.1.
     * Envoie ClientboundContainerSetSlotPacket(containerId=-1, slot=-1, carried).
     */
    private static void syncCarried(net.minecraft.server.level.ServerPlayer player)
    {
        player.connection.send(new net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket(
                -1,
                player.containerMenu.getStateId(),
                -1,
                player.containerMenu.getCarried()));
    }

    private boolean checkPermission(ServerPlayer player)
    {
        if (!hasWarehouseCard()) return true;
        ServerLevel level = player.serverLevel();
        IColony colony = findColony(level);
        if (colony == null) return true;
        return colony.getPermissions().hasPermission(player, Action.ACCESS_HUTS);
    }

    private IColony findColony(ServerLevel level)
    {
        BlockPos pos = this.getBlockEntity() != null ? this.getBlockEntity().getBlockPos() : BlockPos.ZERO;
        return IColonyManager.getInstance().getClosestColony(level, pos);
    }

    private BuildingWareHouse findWarehouse(IColony colony)
    {
        for (IBuilding b : colony.getServerBuildingManager().getBuildings().values())
            if (b instanceof BuildingWareHouse wh) return wh;
        return null;
    }

    private static ItemStack insertIntoHandler(IItemHandler handler, ItemStack stack)
    {
        ItemStack remaining = stack.copy();
        for (int s = 0; s < handler.getSlots() && !remaining.isEmpty(); s++)
            remaining = handler.insertItem(s, remaining, false);
        return remaining;
    }

    private static void msg(ServerPlayer player, String text)
    {
        player.sendSystemMessage(Component.literal(text));
    }
}