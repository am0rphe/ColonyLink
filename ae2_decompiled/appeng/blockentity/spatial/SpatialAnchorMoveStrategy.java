/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockState
 *  org.jetbrains.annotations.Nullable
 */
package appeng.blockentity.spatial;

import appeng.api.movable.DefaultBlockEntityMoveStrategy;
import appeng.blockentity.spatial.SpatialAnchorBlockEntity;
import appeng.core.definitions.AEBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class SpatialAnchorMoveStrategy
extends DefaultBlockEntityMoveStrategy {
    @Override
    public boolean canHandle(BlockEntityType<?> type) {
        return type == AEBlockEntities.SPATIAL_ANCHOR.get();
    }

    @Override
    @Nullable
    public CompoundTag beginMove(BlockEntity blockEntity, HolderLookup.Provider registries) {
        CompoundTag result = super.beginMove(blockEntity, registries);
        if (result != null && blockEntity instanceof SpatialAnchorBlockEntity) {
            SpatialAnchorBlockEntity spatialAnchor = (SpatialAnchorBlockEntity)blockEntity;
            spatialAnchor.releaseAll();
        }
        return result;
    }

    @Override
    public boolean completeMove(BlockEntity blockEntity, BlockState state, CompoundTag savedData, Level newLevel, BlockPos newPosition) {
        if (!super.completeMove(blockEntity, state, savedData, newLevel, newPosition)) {
            return false;
        }
        BlockEntity blockEntity2 = newLevel.getBlockEntity(newPosition);
        if (blockEntity2 instanceof SpatialAnchorBlockEntity) {
            SpatialAnchorBlockEntity spatialAnchor = (SpatialAnchorBlockEntity)blockEntity2;
            spatialAnchor.doneMoving();
        }
        return true;
    }
}

