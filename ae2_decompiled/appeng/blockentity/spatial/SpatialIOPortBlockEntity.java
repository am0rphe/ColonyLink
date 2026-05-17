/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockState
 */
package appeng.blockentity.spatial;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.YesNo;
import appeng.api.implementations.items.ISpatialStorageCell;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.events.GridSpatialEvent;
import appeng.api.networking.spatial.ISpatialService;
import appeng.api.util.AECableType;
import appeng.blockentity.grid.AENetworkedInvBlockEntity;
import appeng.hooks.ticking.TickHandler;
import appeng.util.ILevelRunnable;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.FilteredInternalInventory;
import appeng.util.inv.filter.IAEItemFilter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class SpatialIOPortBlockEntity
extends AENetworkedInvBlockEntity {
    private final AppEngInternalInventory inv = new AppEngInternalInventory(this, 2);
    private final InternalInventory invExt = new FilteredInternalInventory(this.inv, new SpatialIOFilter());
    private YesNo lastRedstoneState = YesNo.UNDECIDED;
    private final ILevelRunnable transitionCallback = level -> this.transition();
    private boolean isActive = false;

    public SpatialIOPortBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
        this.getMainNode().setFlags(GridFlags.REQUIRE_CHANNEL);
    }

    @Override
    public void saveAdditional(CompoundTag data, HolderLookup.Provider registries) {
        super.saveAdditional(data, registries);
        data.putInt("lastRedstoneState", this.lastRedstoneState.ordinal());
    }

    @Override
    public void loadTag(CompoundTag data, HolderLookup.Provider registries) {
        super.loadTag(data, registries);
        if (data.contains("lastRedstoneState")) {
            this.lastRedstoneState = YesNo.values()[data.getInt("lastRedstoneState")];
        }
    }

    @Override
    protected void writeToStream(RegistryFriendlyByteBuf data) {
        super.writeToStream(data);
        data.writeBoolean(this.isActive());
    }

    @Override
    protected boolean readFromStream(RegistryFriendlyByteBuf data) {
        boolean ret = super.readFromStream(data);
        boolean isActive = data.readBoolean();
        ret = isActive != this.isActive || ret;
        this.isActive = isActive;
        return ret;
    }

    public boolean getRedstoneState() {
        if (this.lastRedstoneState == YesNo.UNDECIDED) {
            this.updateRedstoneState();
        }
        return this.lastRedstoneState == YesNo.YES;
    }

    public void updateRedstoneState() {
        YesNo currentState;
        YesNo yesNo = currentState = this.level.getBestNeighborSignal(this.worldPosition) != 0 ? YesNo.YES : YesNo.NO;
        if (this.lastRedstoneState != currentState) {
            this.lastRedstoneState = currentState;
            if (this.lastRedstoneState == YesNo.YES) {
                this.triggerTransition();
            }
        }
    }

    public boolean isActive() {
        if (this.level != null && !this.level.isClientSide) {
            return this.getMainNode().isOnline();
        }
        return this.isActive;
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        if (reason != IGridNodeListener.State.GRID_BOOT) {
            this.markForUpdate();
        }
    }

    private void triggerTransition() {
        ItemStack cell;
        if (!this.isClientSide() && this.isSpatialCell(cell = this.inv.getStackInSlot(0))) {
            TickHandler.instance().addCallable(null, this.transitionCallback);
        }
    }

    private boolean isSpatialCell(ItemStack cell) {
        Item item;
        if (!cell.isEmpty() && (item = cell.getItem()) instanceof ISpatialStorageCell) {
            ISpatialStorageCell sc = (ISpatialStorageCell)item;
            return sc.isSpatialStorage(cell);
        }
        return false;
    }

    private void transition() {
        Level level = this.level;
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel)level;
        ItemStack cell = this.inv.getStackInSlot(0);
        if (!this.isSpatialCell(cell) || !this.inv.getStackInSlot(1).isEmpty()) {
            return;
        }
        ISpatialStorageCell sc = (ISpatialStorageCell)cell.getItem();
        if (!this.getMainNode().isActive()) {
            return;
        }
        this.getMainNode().ifPresent((grid, node) -> {
            GridSpatialEvent evt;
            double req;
            ISpatialService spc = grid.getSpatialService();
            if (!spc.hasRegion() || !spc.isValidRegion()) {
                return;
            }
            IEnergyService energy = grid.getEnergyService();
            double pr = energy.extractAEPower(req = (double)spc.requiredPower(), Actionable.SIMULATE, PowerMultiplier.CONFIG);
            if (Math.abs(pr - req) < req * 0.001 && !(evt = grid.postEvent(new GridSpatialEvent(this.getLevel(), this.getBlockPos(), req))).isTransitionPrevented()) {
                int playerId = node.getOwningPlayerId();
                boolean success = sc.doSpatialTransition(cell, serverLevel, spc.getMin(), spc.getMax(), playerId);
                if (success) {
                    energy.extractAEPower(req, Actionable.MODULATE, PowerMultiplier.CONFIG);
                    this.inv.setItemDirect(0, ItemStack.EMPTY);
                    this.inv.setItemDirect(1, cell);
                }
            }
        });
    }

    @Override
    public AECableType getCableConnectionType(Direction dir) {
        return AECableType.SMART;
    }

    @Override
    protected InternalInventory getExposedInventoryForSide(Direction side) {
        return this.invExt;
    }

    @Override
    public InternalInventory getInternalInventory() {
        return this.inv;
    }

    private class SpatialIOFilter
    implements IAEItemFilter {
        private SpatialIOFilter() {
        }

        @Override
        public boolean allowExtract(InternalInventory inv, int slot, int amount) {
            return slot == 1;
        }

        @Override
        public boolean allowInsert(InternalInventory inv, int slot, ItemStack stack) {
            return slot == 0 && SpatialIOPortBlockEntity.this.isSpatialCell(stack);
        }
    }
}

