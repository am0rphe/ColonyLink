/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.state.BlockState
 *  org.jetbrains.annotations.Nullable
 */
package appeng.api.movable;

import appeng.api.movable.IBlockEntityMoveStrategy;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public abstract class DefaultBlockEntityMoveStrategy
implements IBlockEntityMoveStrategy {
    @Override
    @Nullable
    public CompoundTag beginMove(BlockEntity blockEntity, HolderLookup.Provider registries) {
        return blockEntity.saveWithId(registries);
    }

    @Override
    public boolean completeMove(BlockEntity blockEntity, BlockState state, CompoundTag savedData, Level newLevel, BlockPos newPosition) {
        BlockEntity be = BlockEntity.loadStatic((BlockPos)newPosition, (BlockState)state, (CompoundTag)savedData, (HolderLookup.Provider)newLevel.registryAccess());
        if (be != null) {
            newLevel.setBlockEntity(be);
            return true;
        }
        return false;
    }
}

