/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockState
 */
package appeng.debug;

import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IManagedGridNode;
import appeng.blockentity.grid.AENetworkedBlockEntity;
import appeng.me.helpers.BlockEntityNodeListener;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class PhantomNodeBlockEntity
extends AENetworkedBlockEntity {
    private IManagedGridNode proxy = null;
    private boolean crashMode = false;

    public PhantomNodeBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
    }

    @Override
    public IGridNode getGridNode(Direction dir) {
        if (!this.crashMode) {
            return super.getGridNode(dir);
        }
        return this.proxy.getNode();
    }

    @Override
    public void onReady() {
        super.onReady();
        this.proxy = GridHelper.createManagedNode(this, BlockEntityNodeListener.INSTANCE).setInWorldNode(true).setVisualRepresentation((ItemLike)this.getItemFromBlockEntity());
        this.proxy.create(this.level, this.worldPosition);
        this.crashMode = true;
    }

    void triggerCrashMode() {
        if (this.proxy != null) {
            this.crashMode = true;
            this.proxy.setExposedOnSides(EnumSet.allOf(Direction.class));
        }
    }
}

