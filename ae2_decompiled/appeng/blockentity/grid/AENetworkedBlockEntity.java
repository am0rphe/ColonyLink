/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockState
 */
package appeng.blockentity.grid;

import appeng.api.networking.GridHelper;
import appeng.api.networking.IManagedGridNode;
import appeng.api.orientation.BlockOrientation;
import appeng.api.util.AECableType;
import appeng.block.AEBaseEntityBlock;
import appeng.blockentity.AEBaseBlockEntity;
import appeng.me.helpers.BlockEntityNodeListener;
import appeng.me.helpers.IGridConnectedBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class AENetworkedBlockEntity
extends AEBaseBlockEntity
implements IGridConnectedBlockEntity {
    private final IManagedGridNode mainNode = this.createMainNode().setVisualRepresentation((ItemLike)this.getItemFromBlockEntity()).setInWorldNode(true).setTagName("proxy");

    public AENetworkedBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
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

    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        this.getMainNode().destroy();
    }

    @Override
    public void onReady() {
        AEBaseEntityBlock block;
        BlockState newState;
        super.onReady();
        this.getMainNode().create(this.getLevel(), this.getBlockEntity().getBlockPos());
        BlockState currentState = this.getBlockState();
        Block block2 = currentState.getBlock();
        if (block2 instanceof AEBaseEntityBlock && currentState != (newState = (block = (AEBaseEntityBlock)block2).getBlockEntityBlockState(currentState, this))) {
            this.markForUpdate();
        }
    }

    @Override
    protected void onOrientationChanged(BlockOrientation orientation) {
        super.onOrientationChanged(orientation);
        this.onGridConnectableSidesChanged();
    }

    protected final void onGridConnectableSidesChanged() {
        this.getMainNode().setExposedOnSides(this.getGridConnectableSides(this.getOrientation()));
    }

    public void setRemoved() {
        super.setRemoved();
        this.getMainNode().destroy();
    }

    public void clearRemoved() {
        super.clearRemoved();
        this.scheduleInit();
    }
}

