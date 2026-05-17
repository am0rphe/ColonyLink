/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockState
 */
package appeng.blockentity.networking;

import appeng.api.config.Actionable;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.GridFlags;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.events.GridControllerChange;
import appeng.api.networking.events.GridPowerStorageStateChanged;
import appeng.api.networking.pathing.ControllerState;
import appeng.api.util.AECableType;
import appeng.block.networking.ControllerBlock;
import appeng.blockentity.grid.AENetworkedPoweredBlockEntity;
import appeng.util.Platform;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class ControllerBlockEntity
extends AENetworkedPoweredBlockEntity {
    public ControllerBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
        this.setInternalMaxPower(8000.0);
        this.setInternalPublicPowerStorage(true);
        this.getMainNode().setIdlePowerUsage(3.0);
        this.getMainNode().setFlags(GridFlags.CANNOT_CARRY, GridFlags.DENSE_CAPACITY);
    }

    @Override
    public AECableType getCableConnectionType(Direction dir) {
        return AECableType.DENSE_SMART;
    }

    @Override
    public void onReady() {
        super.onReady();
        this.updateState();
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        this.updateState();
    }

    public void updateState() {
        if (!this.getMainNode().isReady()) {
            return;
        }
        ControllerBlock.ControllerBlockState metaState = ControllerBlock.ControllerBlockState.offline;
        IGrid grid = this.getMainNode().getGrid();
        if (grid != null) {
            if (grid.getEnergyService().isNetworkPowered()) {
                metaState = ControllerBlock.ControllerBlockState.online;
                if (grid.getPathingService().getControllerState() == ControllerState.CONTROLLER_CONFLICT) {
                    metaState = ControllerBlock.ControllerBlockState.conflicted;
                }
            }
        } else {
            metaState = ControllerBlock.ControllerBlockState.offline;
        }
        if (this.checkController(this.worldPosition) && this.level.getBlockState(this.worldPosition).getValue(ControllerBlock.CONTROLLER_STATE) != metaState) {
            this.level.setBlock(this.worldPosition, (BlockState)this.level.getBlockState(this.worldPosition).setValue(ControllerBlock.CONTROLLER_STATE, (Comparable)((Object)metaState)), 2);
        }
    }

    @Override
    protected double getFunnelPowerDemand(double maxReceived) {
        IGrid grid = this.getMainNode().getGrid();
        if (grid != null) {
            return grid.getEnergyService().getEnergyDemand(maxReceived);
        }
        return super.getFunnelPowerDemand(maxReceived);
    }

    @Override
    protected double funnelPowerIntoStorage(double power, Actionable mode) {
        IGrid grid = this.getMainNode().getGrid();
        if (grid != null) {
            return grid.getEnergyService().injectPower(power, mode);
        }
        return super.funnelPowerIntoStorage(power, mode);
    }

    @Override
    protected void emitPowerStateEvent(GridPowerStorageStateChanged.PowerEventType type) {
        this.getMainNode().ifPresent(grid -> grid.postEvent(new GridPowerStorageStateChanged(this, type)));
    }

    @Override
    public InternalInventory getInternalInventory() {
        return InternalInventory.empty();
    }

    private boolean checkController(BlockPos pos) {
        return Platform.getTickingBlockEntity(this.getLevel(), pos) instanceof ControllerBlockEntity;
    }

    static {
        GridHelper.addNodeOwnerEventHandler(GridControllerChange.class, ControllerBlockEntity.class, ControllerBlockEntity::updateState);
    }
}

