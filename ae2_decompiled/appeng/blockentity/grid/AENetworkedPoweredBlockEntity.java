/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockState
 */
package appeng.blockentity.grid;

import appeng.api.config.Actionable;
import appeng.api.config.PowerUnit;
import appeng.api.implementations.blockentities.ICrankable;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.energy.IAEPowerStorage;
import appeng.api.orientation.BlockOrientation;
import appeng.api.util.AECableType;
import appeng.blockentity.powersink.AEBasePoweredBlockEntity;
import appeng.me.helpers.BlockEntityNodeListener;
import appeng.me.helpers.IGridConnectedBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class AENetworkedPoweredBlockEntity
extends AEBasePoweredBlockEntity
implements IGridConnectedBlockEntity {
    private final IManagedGridNode mainNode = this.createMainNode().setVisualRepresentation((ItemLike)this.getItemFromBlockEntity()).addService(IAEPowerStorage.class, this).setInWorldNode(true).setTagName("proxy");

    public AENetworkedPoweredBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
        this.onGridConnectableSidesChanged();
    }

    protected IManagedGridNode createMainNode() {
        return GridHelper.createManagedNode(this, BlockEntityNodeListener.INSTANCE);
    }

    @Override
    public void loadTag(CompoundTag data, HolderLookup.Provider registries) {
        super.loadTag(data, registries);
        this.getMainNode().loadFromNBT(data);
    }

    @Override
    public void saveAdditional(CompoundTag data, HolderLookup.Provider registries) {
        super.saveAdditional(data, registries);
        this.getMainNode().saveToNBT(data);
    }

    @Override
    public final IManagedGridNode getMainNode() {
        return this.mainNode;
    }

    @Override
    public AECableType getCableConnectionType(Direction dir) {
        return AECableType.SMART;
    }

    public void clearRemoved() {
        super.clearRemoved();
        this.scheduleInit();
    }

    public void setRemoved() {
        super.setRemoved();
        this.getMainNode().destroy();
    }

    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        this.getMainNode().destroy();
    }

    @Override
    public void onReady() {
        super.onReady();
        this.getMainNode().create(this.getLevel(), this.getBlockEntity().getBlockPos());
    }

    @Override
    protected void onOrientationChanged(BlockOrientation orientation) {
        super.onOrientationChanged(orientation);
        this.onGridConnectableSidesChanged();
    }

    protected final void onGridConnectableSidesChanged() {
        this.getMainNode().setExposedOnSides(this.getGridConnectableSides(this.getOrientation()));
    }

    public final class Crankable
    implements ICrankable {
        @Override
        public boolean canTurn() {
            return AENetworkedPoweredBlockEntity.this.getInternalCurrentPower() < AENetworkedPoweredBlockEntity.this.getInternalMaxPower();
        }

        @Override
        public void applyTurn() {
            AENetworkedPoweredBlockEntity.this.injectExternalPower(PowerUnit.AE, 160.0, Actionable.MODULATE);
        }
    }
}

