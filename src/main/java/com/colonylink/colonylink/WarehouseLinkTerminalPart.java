package com.colonylink.colonylink;

import appeng.api.config.Actionable;
import appeng.api.networking.GridFlags;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.parts.BusSupport;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.api.stacks.AEItemKey;
import appeng.api.storage.MEStorage;
import appeng.api.util.AECableType;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingWareHouse;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Warehouse Link Terminal — AE2 Part.
 *
 * Placed on a cable bus face, exactly like the ME Crafting Terminal.
 * Consumes 1 AE2 channel, 8 AE/t idle power.
 *
 * Architecture:
 *   - IPart          : AE2 part lifecycle (addToWorld / removeFromWorld / setPartHostInfo)
 *   - IActionHost    : ME operations attributed to this part
 *   - MenuProvider   : opens the GUI when right-clicked
 *
 * The grid node is created in addToWorld() and destroyed in removeFromWorld(),
 * following the same pattern as all native AE2 terminal parts.
 *
 * The Warehouse Link Card is stored in a 1-slot ItemStackHandler persisted in NBT.
 *
 * v1.3.0 fixes:
 *   - Cable arm rendering: setExposedOnSides() is NOT called in setPartHostInfo.
 *     AE2's AEBasePart leaves the node with EnumSet.noneOf(Direction.class)
 *     and the CableBusContainer host handles internal connection to the cable.
 *     Forcing setExposedOnSides(side) was breaking the cable arm rendering.
 *   - NBT persistence: readFromNBT/writeToNBT now use the correct IPart
 *     signature (CompoundTag, HolderLookup.Provider). The Warehouse Link Card
 *     now persists across chunk reloads.
 */
public class WarehouseLinkTerminalPart implements IPart, IActionHost, MenuProvider
{
    // ── Scan interval ─────────────────────────────────────────────────────────

    private static final int SCAN_INTERVAL = 40; // ticks

    // ── Part host info (set by setPartHostInfo) ───────────────────────────────

    @Nullable private Direction       side            = null;
    @Nullable private IPartHost       partHost        = null;
    @Nullable private BlockEntity     hostBlockEntity = null;

    // ── Client-side cached visual state ───────────────────────────────────────
    //
    // On the client, gridNode.getNode() returns null — so isActive() / isPowered()
    // can't query the grid directly. Instead, the server sends the current state
    // via writeToStream() and the client caches it in these fields. This mirrors
    // exactly what AEBasePart does for native AE2 parts.
    //
    // CRITICAL for cable arm rendering: AE2 only draws the cable arm towards
    // a Part when the Part reports isActive() == true. If the client always
    // sees OFF state, no cable arm will ever be drawn.

    private boolean clientSidePowered        = false;
    private boolean clientSideMissingChannel = false;

    // ── Warehouse Link Card slot ───────────────────────────────────────────────

    private final ItemStackHandler warehouseCardSlot = new ItemStackHandler(1)
    {
        @Override
        public boolean isItemValid(int slot, ItemStack stack)
        { return stack.getItem() instanceof WarehouseLinkCard; }

        @Override
        protected void onContentsChanged(int slot)
        {
            saveChanges();
            if (hostBlockEntity != null && hostBlockEntity.getLevel() != null)
                hostBlockEntity.getLevel().sendBlockUpdated(
                        hostBlockEntity.getBlockPos(),
                        hostBlockEntity.getBlockState(),
                        hostBlockEntity.getBlockState(), 3);
        }
    };

    // ── Snapshot cache ────────────────────────────────────────────────────────

    private final List<WarehouseTerminalSyncPacket.WarehouseItemEntry> warehouseSnapshot
            = new ArrayList<>();

    private int     tickCounter = 0;
    private boolean guiOpen     = false;

    // ── Reference to the part item ────────────────────────────────────────────

    private final WarehouseLinkTerminalItem partItem;

    // ── AE2 grid node ─────────────────────────────────────────────────────────
    //
    // Pattern mirrors AEBasePart constructor:
    //   - Node is created via GridHelper.createManagedNode(this, listener)
    //   - setExposedOnSides is NEVER called — the CableBusContainer handles
    //     internal cable connection. AEBasePart leaves it as EnumSet.noneOf(...).
    //   - REQUIRE_CHANNEL flag: consumes 1 channel
    //   - idlePowerUsage(8.0): matches ME Crafting Terminal

    private final IManagedGridNode gridNode = GridHelper.createManagedNode(this,
                    new IGridNodeListener<WarehouseLinkTerminalPart>()
                    {
                        @Override
                        public void onSaveChanges(WarehouseLinkTerminalPart owner, IGridNode node)
                        { owner.saveChanges(); }

                        @Override
                        public void onStateChanged(WarehouseLinkTerminalPart owner, IGridNode node,
                                                   IGridNodeListener.State state)
                        { owner.onGridStateChanged(); }
                    })
            .setFlags(GridFlags.REQUIRE_CHANNEL)
            .setIdlePowerUsage(8.0)
            .setVisualRepresentation(ColonyLinkRegistry.WAREHOUSE_LINK_TERMINAL_ITEM.get());

    // ── Constructor ───────────────────────────────────────────────────────────

    public WarehouseLinkTerminalPart(WarehouseLinkTerminalItem item)
    {
        this.partItem = item;
    }

    // ── IPart — host info ─────────────────────────────────────────────────────

    @Override
    public void setPartHostInfo(@Nullable Direction side, IPartHost host, BlockEntity blockEntity)
    {
        this.side            = side;
        this.partHost        = host;
        this.hostBlockEntity = blockEntity;
        // NOTE: setExposedOnSides is NOT called here. AE2's AEBasePart never
        // calls it after construction either. The CableBusContainer manages
        // internal connection between the Part node and the cable bus node.
        // Forcing setExposedOnSides(EnumSet.of(side)) breaks cable arm rendering.
    }

    // ── IPart — lifecycle ─────────────────────────────────────────────────────

    @Override
    public void addToWorld()
    {
        // Create the grid node using the host block entity's level and position.
        // This is the correct place to create the node for Parts (NOT in the constructor).
        if (hostBlockEntity != null && hostBlockEntity.getLevel() != null
                && !hostBlockEntity.getLevel().isClientSide())
        {
            gridNode.create(hostBlockEntity.getLevel(), hostBlockEntity.getBlockPos());
        }
    }

    @Override
    public void removeFromWorld()
    {
        gridNode.destroy();
    }

    // ── IPart — grid node ─────────────────────────────────────────────────────

    @Override
    @Nullable
    public IGridNode getGridNode()
    {
        return gridNode.getNode();
    }

    // ── IActionHost ───────────────────────────────────────────────────────────

    @Override
    public IGridNode getActionableNode()
    {
        return gridNode.getNode();
    }

    // ── IPart — part item ─────────────────────────────────────────────────────

    @Override
    public IPartItem<?> getPartItem()
    {
        return partItem;
    }

    // ── IPart — interaction ───────────────────────────────────────────────────

    @Override
    public boolean onUseWithoutItem(Player player, Vec3 pos)
    {
        if (player.level().isClientSide()) return true;
        if (!(player instanceof ServerPlayer sp)) return false;
        sp.openMenu(this, buf -> buf.writeBlockPos(hostBlockEntity.getBlockPos())
                .writeByte(side != null ? side.ordinal() : 0));
        return true;
    }

    // ── IPart — hitbox ────────────────────────────────────────────────────────

    @Override
    public void getBoxes(IPartCollisionHelper bch)
    {
        // Flat panel, 2px thick — same as AE2 terminal parts
        bch.addBox(2, 2, 14, 14, 14, 16);
    }

    @Override
    public float getCableConnectionLength(AECableType cable)
    {
        // Same value as AEBasePart.getCableConnectionLength — determines
        // how far the cable arm extends from the cable centre towards this Part.
        return 3.0f;
    }

    // ── IPart — cable placement ───────────────────────────────────────────────

    @Override
    public boolean canBePlacedOn(BusSupport what)
    {
        return what == BusSupport.CABLE || what == BusSupport.DENSE_CABLE;
    }

    // ── IPart — models ────────────────────────────────────────────────────────
    //
    // 3-state pattern matching CraftingTerminalPart (AE2 19.2.17):
    //
    //   MODELS_OFF         = display_base + warehouse_link_terminal_off + display_status_off
    //   MODELS_ON          = display_base + warehouse_link_terminal_on  + display_status_on
    //   MODELS_HAS_CHANNEL = display_base + warehouse_link_terminal_on  + display_status_has_channel
    //
    // - display_base : 3D panel shell (sides, back, status dot) — supplied by AE2
    // - warehouse_link_terminal_off/on : our custom face, inherits from ae2:part/display_off
    //   which contains the 3 stacked cubes with tintindex 1/2/3 (the LED illumination layers).
    //   The JSON overrides lightsBright/lightsMedium/lightsDark — they all point to our
    //   single bright/dark texture for v1.3.0 (multi-texture variant planned for v1.3.x+).
    // - display_status_* : the small LED indicator
    //
    // All 6 ResourceLocations are registered in ColonyLink.commonSetup
    // via PartModels.registerModels(...).

    private static final ResourceLocation MODEL_BASE =
            ResourceLocation.fromNamespaceAndPath("ae2", "part/display_base");
    private static final ResourceLocation MODEL_STATUS_OFF =
            ResourceLocation.fromNamespaceAndPath("ae2", "part/display_status_off");
    private static final ResourceLocation MODEL_STATUS_ON =
            ResourceLocation.fromNamespaceAndPath("ae2", "part/display_status_on");
    private static final ResourceLocation MODEL_STATUS_HAS_CHANNEL =
            ResourceLocation.fromNamespaceAndPath("ae2", "part/display_status_has_channel");
    private static final ResourceLocation MODEL_FACE_ON =
            ResourceLocation.fromNamespaceAndPath("colonylink", "part/warehouse_link_terminal_on");
    private static final ResourceLocation MODEL_FACE_OFF =
            ResourceLocation.fromNamespaceAndPath("colonylink", "part/warehouse_link_terminal_off");

    // IPartModel implemented as anonymous class — avoids depending on appeng.parts.PartModel
    // (which is not part of the public API). Order matches CraftingTerminalPart:
    //   new PartModel(MODEL_BASE, MODEL_FACE_*, MODEL_STATUS_*)
    private static final List<ResourceLocation> LIST_OFF =
            List.of(MODEL_BASE, MODEL_FACE_OFF, MODEL_STATUS_OFF);
    private static final List<ResourceLocation> LIST_ON =
            List.of(MODEL_BASE, MODEL_FACE_ON, MODEL_STATUS_ON);
    private static final List<ResourceLocation> LIST_HAS_CHANNEL =
            List.of(MODEL_BASE, MODEL_FACE_ON, MODEL_STATUS_HAS_CHANNEL);

    private static final IPartModel MODELS_OFF = new IPartModel()
    {
        @Override
        public List<ResourceLocation> getModels() { return LIST_OFF; }
    };
    private static final IPartModel MODELS_ON = new IPartModel()
    {
        @Override
        public List<ResourceLocation> getModels() { return LIST_ON; }
    };
    private static final IPartModel MODELS_HAS_CHANNEL = new IPartModel()
    {
        @Override
        public List<ResourceLocation> getModels() { return LIST_HAS_CHANNEL; }
    };

    @Override
    public IPartModel getStaticModels()
    {
        // Mirrors AbstractReportingPart.selectModel:
        //   if (isActive())       → HAS_CHANNEL (powered + has channel)
        //   else if (isPowered()) → ON          (powered, no channel)
        //   else                  → OFF         (no power)
        if (isAe2Active()) return MODELS_HAS_CHANNEL;
        if (isPowered())   return MODELS_ON;
        return MODELS_OFF;
    }

    // ── Power state — MUST be client-aware ───────────────────────────────────
    //
    // On the client, gridNode.getNode() returns null. We rely on the cached
    // clientSidePowered / clientSideMissingChannel flags synced from the server
    // via writeToStream / readFromStream. This mirrors AEBasePart exactly.
    //
    // Without this, the client would always see OFF state, which means:
    //   1. getStaticModels() always returns MODELS_OFF (wrong visuals)
    //   2. AE2 never draws the cable arm (it only draws for active parts)

    /** True if the part has power (regardless of channel availability). */
    public boolean isPowered()
    {
        if (isClientSide())
            return clientSidePowered;
        var node = gridNode.getNode();
        return node != null && node.isPowered();
    }

    /** True if the part is powered but waiting for a channel. */
    public boolean isMissingChannel()
    {
        if (isClientSide())
            return clientSideMissingChannel;
        var node = gridNode.getNode();
        return node == null || !node.meetsChannelRequirements();
    }

    /** True if the part is fully online (powered AND has channel). */
    public boolean isAe2Active()
    {
        return isPowered() && !isMissingChannel();
    }

    /** True when running on the client side (level == null is also treated as client). */
    private boolean isClientSide()
    {
        return hostBlockEntity == null
                || hostBlockEntity.getLevel() == null
                || hostBlockEntity.getLevel().isClientSide();
    }

    // ── IPart — NBT persistence ───────────────────────────────────────────────
    //
    // Correct signature per IPart interface (AE2 19.2.17):
    //   void readFromNBT(CompoundTag data, HolderLookup.Provider registries);
    //   void writeToNBT(CompoundTag data, HolderLookup.Provider registries);

    @Override
    public void readFromNBT(CompoundTag data, HolderLookup.Provider registries)
    {
        if (data.contains("warehouse_card"))
            warehouseCardSlot.deserializeNBT(registries, data.getCompound("warehouse_card"));
    }

    @Override
    public void writeToNBT(CompoundTag data, HolderLookup.Provider registries)
    {
        data.put("warehouse_card", warehouseCardSlot.serializeNBT(registries));
    }

    // ── IPart — network sync ──────────────────────────────────────────────────
    //
    // Protocol mirrors AEBasePart exactly:
    //   1 byte = bit flags  (bit 0 = powered, bit 1 = missingChannel)
    //
    // This is critical: the client needs these flags to correctly compute
    // isPowered() / isAe2Active(), which in turn drives getStaticModels()
    // and the cable arm rendering.

    @Override
    public void writeToStream(RegistryFriendlyByteBuf buf)
    {
        // Capture current state from the live node and cache it so subsequent
        // markForUpdateIfClientFlagsChanged() comparisons work correctly.
        clientSidePowered        = isPowered();
        clientSideMissingChannel = isMissingChannel();

        int flags = 0;
        if (clientSidePowered)        flags |= 1;
        if (clientSideMissingChannel) flags |= 2;
        buf.writeByte(flags);
    }

    @Override
    public boolean readFromStream(RegistryFriendlyByteBuf buf)
    {
        byte flags = buf.readByte();
        boolean wasPowered         = clientSidePowered;
        boolean wasMissingChannel  = clientSideMissingChannel;
        clientSidePowered        = (flags & 1) != 0;
        clientSideMissingChannel = (flags & 2) != 0;
        // Re-render if either flag changed
        return clientSidePowered != wasPowered
                || clientSideMissingChannel != wasMissingChannel;
    }

    // ── MenuProvider ──────────────────────────────────────────────────────────

    @Override
    public Component getDisplayName()
    {
        return Component.translatable("item.colonylink.warehouse_link_terminal");
    }

    @Override
    @Nullable
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player)
    {
        return new WarehouseLinkTerminalMenu(containerId, playerInventory, this);
    }

    // ── Server tick (called manually from the menu's serverTick) ─────────────

    public void onServerTick()
    {
        if (!guiOpen) return;
        tickCounter++;
        if (tickCounter >= SCAN_INTERVAL)
        {
            tickCounter = 0;
            refreshWarehouseSnapshot();
            broadcastSnapshots();
        }
    }

    public void requestImmediateSync()
    {
        guiOpen = true;
        refreshWarehouseSnapshot();
        broadcastSnapshots();
    }

    public void onGuiClosed()
    {
        guiOpen     = false;
        tickCounter = 0;
    }

    // ── Warehouse scanning ────────────────────────────────────────────────────

    private void refreshWarehouseSnapshot()
    {
        warehouseSnapshot.clear();
        if (!hasWarehouseCard() || hostBlockEntity == null) return;

        var level = hostBlockEntity.getLevel();
        if (level == null || level.isClientSide()) return;

        BlockPos pos = hostBlockEntity.getBlockPos();
        IColony colony = IColonyManager.getInstance().getClosestColony(level, pos);
        if (colony == null) return;

        java.util.Map<AEItemKey, Long> aggregate = new java.util.LinkedHashMap<>();

        for (IBuilding building : colony.getServerBuildingManager().getBuildings().values())
        {
            if (!(building instanceof BuildingWareHouse wh)) continue;
            var containers = wh.getContainers();
            if (containers == null) continue;

            for (BlockPos rackPos : containers)
            {
                IItemHandler rack = level.getCapability(
                        Capabilities.ItemHandler.BLOCK, rackPos, null);
                if (rack == null) continue;

                for (int slot = 0; slot < rack.getSlots(); slot++)
                {
                    ItemStack stack = rack.getStackInSlot(slot);
                    if (stack.isEmpty()) continue;
                    AEItemKey key = AEItemKey.of(stack);
                    aggregate.merge(key, (long) stack.getCount(), Long::sum);
                }
            }
        }

        for (var entry : aggregate.entrySet())
            warehouseSnapshot.add(new WarehouseTerminalSyncPacket.WarehouseItemEntry(
                    entry.getKey().toStack(1), entry.getValue()));
    }

    private void broadcastSnapshots()
    {
        if (hostBlockEntity == null) return;
        var level = hostBlockEntity.getLevel();
        if (!(level instanceof ServerLevel serverLevel)) return;

        var whPacket = new WarehouseTerminalSyncPacket(
                new ArrayList<>(warehouseSnapshot), hasWarehouseCard(),
                hostBlockEntity.getBlockPos());

        TerminalMeSyncPacket mePacket = null;
        if (isAe2Active())
        {
            var node = gridNode.getNode();
            if (node != null)
                mePacket = TerminalMeSyncPacket.fromGrid(node.getGrid(),
                        hostBlockEntity.getBlockPos());
        }

        final TerminalMeSyncPacket finalMe = mePacket;

        for (var player : serverLevel.players())
        {
            if (player.containerMenu instanceof WarehouseLinkTerminalMenu menu
                    && menu.getPart() == this)
            {
                net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player, whPacket);
                if (finalMe != null)
                    net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player, finalMe);
            }
        }
    }

    // ── Transfer methods ──────────────────────────────────────────────────────

    public int transferWarehouseToMe(Player player, ItemStack template, int count)
    {
        if (!isAe2Active() || !hasWarehouseCard()) return 0;
        IColony colony = getColony();
        if (colony == null) return 0;

        var node = gridNode.getNode();
        if (node == null) return 0;
        var grid = node.getGrid();

        IActionSource src = IActionSource.ofPlayer(player, this);
        MEStorage storage = grid.getStorageService().getInventory();
        AEItemKey aeKey   = AEItemKey.of(template);
        if (aeKey == null) return 0;

        var level = hostBlockEntity.getLevel();
        int remaining = count, totalSent = 0;

        outer:
        for (IBuilding building : colony.getServerBuildingManager().getBuildings().values())
        {
            if (!(building instanceof BuildingWareHouse wh)) continue;
            var containers = wh.getContainers();
            if (containers == null) continue;
            for (BlockPos rackPos : containers)
            {
                if (remaining <= 0) break outer;
                IItemHandler rack = level.getCapability(
                        Capabilities.ItemHandler.BLOCK, rackPos, null);
                if (rack == null) continue;
                for (int slot = 0; slot < rack.getSlots() && remaining > 0; slot++)
                {
                    ItemStack inSlot = rack.getStackInSlot(slot);
                    if (inSlot.isEmpty() || !ItemStack.isSameItemSameComponents(inSlot, template))
                        continue;
                    int take = Math.min(remaining, inSlot.getCount());
                    ItemStack extracted = rack.extractItem(slot, take, false);
                    if (extracted.isEmpty()) continue;
                    long inserted = storage.insert(aeKey, extracted.getCount(),
                            Actionable.MODULATE, src);
                    totalSent += (int) inserted;
                    remaining -= (int) inserted;
                    if (inserted < extracted.getCount())
                    {
                        ItemStack refund = extracted.copy();
                        refund.setCount((int)(extracted.getCount() - inserted));
                        insertIntoHandler(rack, refund);
                        break outer;
                    }
                }
            }
        }
        return totalSent;
    }

    public int transferMeToWarehouse(Player player, ItemStack template, int count)
    {
        if (!isAe2Active() || !hasWarehouseCard()) return 0;
        IColony colony = getColony();
        if (colony == null) return 0;

        var node = gridNode.getNode();
        if (node == null) return 0;
        var grid = node.getGrid();

        IActionSource src = IActionSource.ofPlayer(player, this);
        MEStorage storage = grid.getStorageService().getInventory();
        AEItemKey aeKey   = AEItemKey.of(template);
        if (aeKey == null) return 0;

        long available = grid.getStorageService().getCachedInventory().get(aeKey);
        int toExtract  = (int) Math.min(count, available);
        if (toExtract <= 0) return 0;

        var level = hostBlockEntity.getLevel();
        int remaining = toExtract, totalInserted = 0;

        outer:
        for (IBuilding building : colony.getServerBuildingManager().getBuildings().values())
        {
            if (!(building instanceof BuildingWareHouse wh)) continue;
            var containers = wh.getContainers();
            if (containers == null) continue;
            for (BlockPos rackPos : containers)
            {
                if (remaining <= 0) break outer;
                IItemHandler rack = level.getCapability(
                        Capabilities.ItemHandler.BLOCK, rackPos, null);
                if (rack == null) continue;
                while (remaining > 0)
                {
                    int batch = Math.min(remaining, 64);
                    long extracted = storage.extract(aeKey, batch, Actionable.MODULATE, src);
                    if (extracted <= 0) break outer;
                    ItemStack toInsert = aeKey.toStack((int) extracted);
                    ItemStack leftOver = insertIntoHandler(rack, toInsert);
                    int sent = (int) extracted - leftOver.getCount();
                    totalInserted += sent;
                    remaining     -= sent;
                    if (!leftOver.isEmpty())
                    {
                        storage.insert(aeKey, leftOver.getCount(), Actionable.MODULATE, src);
                        break outer;
                    }
                }
            }
        }
        return totalInserted;
    }

    public int transferMeToPlayer(Player player, ItemStack template, int count)
    {
        if (!isAe2Active()) return 0;
        var node = gridNode.getNode();
        if (node == null) return 0;
        var grid = node.getGrid();

        IActionSource src = IActionSource.ofPlayer(player, this);
        MEStorage storage = grid.getStorageService().getInventory();
        AEItemKey aeKey   = AEItemKey.of(template);
        if (aeKey == null) return 0;

        long available = grid.getStorageService().getCachedInventory().get(aeKey);
        int toExtract  = (int) Math.min(count, available);
        if (toExtract <= 0) return 0;

        long extracted = storage.extract(aeKey, toExtract, Actionable.MODULATE, src);
        if (extracted <= 0) return 0;

        ItemStack toGive = aeKey.toStack((int) extracted);
        if (player.getInventory().add(toGive)) return (int) extracted;
        storage.insert(aeKey, extracted, Actionable.MODULATE, src);
        return 0;
    }

    public int transferWarehouseToPlayer(Player player, ItemStack template, int count)
    {
        if (!hasWarehouseCard()) return 0;
        IColony colony = getColony();
        if (colony == null) return 0;

        var level = hostBlockEntity.getLevel();
        int remaining = count, totalGiven = 0;

        outer:
        for (IBuilding building : colony.getServerBuildingManager().getBuildings().values())
        {
            if (!(building instanceof BuildingWareHouse wh)) continue;
            var containers = wh.getContainers();
            if (containers == null) continue;
            for (BlockPos rackPos : containers)
            {
                if (remaining <= 0) break outer;
                IItemHandler rack = level.getCapability(
                        Capabilities.ItemHandler.BLOCK, rackPos, null);
                if (rack == null) continue;
                for (int slot = 0; slot < rack.getSlots() && remaining > 0; slot++)
                {
                    ItemStack inSlot = rack.getStackInSlot(slot);
                    if (inSlot.isEmpty() || !ItemStack.isSameItemSameComponents(inSlot, template))
                        continue;
                    int take = Math.min(remaining, inSlot.getCount());
                    ItemStack extracted = rack.extractItem(slot, take, false);
                    if (extracted.isEmpty()) continue;
                    if (player.getInventory().add(extracted))
                    { totalGiven += extracted.getCount(); remaining -= extracted.getCount(); }
                    else { insertIntoHandler(rack, extracted); break outer; }
                }
            }
        }
        return totalGiven;
    }

    // ── State helpers ─────────────────────────────────────────────────────────

    public boolean hasWarehouseCard()
    {
        return !warehouseCardSlot.getStackInSlot(0).isEmpty();
    }

    public ItemStackHandler getWarehouseCardSlot()
    {
        return warehouseCardSlot;
    }

    public List<WarehouseTerminalSyncPacket.WarehouseItemEntry> getWarehouseSnapshot()
    {
        return warehouseSnapshot;
    }

    public IManagedGridNode getManagedGridNode()
    {
        return gridNode;
    }

    @Nullable
    public BlockEntity getHostBlockEntity()
    {
        return hostBlockEntity;
    }

    @Nullable
    public Direction getSide()
    {
        return side;
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void onGridStateChanged()
    {
        // Only the server cares about grid state changes — re-sync to clients.
        if (isClientSide()) return;

        // Check if our cached client flags would actually change.
        // If yes, ask the host to re-broadcast the part state to viewers.
        // This mirrors AEBasePart.markForUpdateIfClientFlagsChanged().
        boolean nowPowered        = isPowered();
        boolean nowMissingChannel = isMissingChannel();
        if (nowPowered != clientSidePowered
                || nowMissingChannel != clientSideMissingChannel)
        {
            if (partHost != null) partHost.markForUpdate();
        }
    }

    private void saveChanges()
    {
        if (hostBlockEntity != null)
            hostBlockEntity.setChanged();
    }

    @Nullable
    private IColony getColony()
    {
        if (hostBlockEntity == null) return null;
        var level = hostBlockEntity.getLevel();
        if (level == null) return null;
        return IColonyManager.getInstance().getClosestColony(level, hostBlockEntity.getBlockPos());
    }

    private static ItemStack insertIntoHandler(IItemHandler handler, ItemStack stack)
    {
        ItemStack rem = stack.copy();
        for (int i = 0; i < handler.getSlots() && !rem.isEmpty(); i++)
            rem = handler.insertItem(i, rem, false);
        return rem;
    }
}