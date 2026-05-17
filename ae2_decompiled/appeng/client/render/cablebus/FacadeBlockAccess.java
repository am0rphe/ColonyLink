/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.world.level.BlockAndTintGetter
 *  net.minecraft.world.level.ColorResolver
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.lighting.LevelLightEngine
 *  net.minecraft.world.level.material.FluidState
 *  org.jetbrains.annotations.Nullable
 */
package appeng.client.render.cablebus;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;

public class FacadeBlockAccess
implements BlockAndTintGetter {
    private final BlockAndTintGetter level;
    private final BlockPos pos;
    private final Direction side;
    private final BlockState state;

    public FacadeBlockAccess(BlockAndTintGetter level, BlockPos pos, Direction side, BlockState state) {
        this.level = level;
        this.pos = pos;
        this.side = side;
        this.state = state;
    }

    @Nullable
    public BlockEntity getBlockEntity(BlockPos pos) {
        return this.level.getBlockEntity(pos);
    }

    public BlockState getBlockState(BlockPos pos) {
        if (this.pos == pos) {
            return this.state;
        }
        return this.level.getBlockState(pos);
    }

    public FluidState getFluidState(BlockPos pos) {
        return this.level.getFluidState(pos);
    }

    public float getShade(Direction p_230487_1_, boolean p_230487_2_) {
        return this.level.getShade(p_230487_1_, p_230487_2_);
    }

    public LevelLightEngine getLightEngine() {
        return this.level.getLightEngine();
    }

    public int getBlockTint(BlockPos blockPosIn, ColorResolver colorResolverIn) {
        return this.level.getBlockTint(blockPosIn, colorResolverIn);
    }

    public int getHeight() {
        return this.level.getHeight();
    }

    public int getMinBuildHeight() {
        return this.level.getMinBuildHeight();
    }
}

