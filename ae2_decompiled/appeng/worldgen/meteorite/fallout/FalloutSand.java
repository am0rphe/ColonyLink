/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.level.LevelAccessor
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.state.BlockState
 */
package appeng.worldgen.meteorite.fallout;

import appeng.worldgen.meteorite.MeteoriteBlockPutter;
import appeng.worldgen.meteorite.fallout.FalloutCopy;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class FalloutSand
extends FalloutCopy {
    private static final float GLASS_THRESHOLD = 0.66f;
    private final MeteoriteBlockPutter putter;

    public FalloutSand(LevelAccessor level, BlockPos pos, MeteoriteBlockPutter putter, BlockState skyStone, RandomSource random) {
        super(level, pos, putter, skyStone, random);
        this.putter = putter;
    }

    @Override
    public int adjustCrater() {
        return 2;
    }

    @Override
    public void getOther(LevelAccessor level, BlockPos pos, float a) {
        if (a > 0.66f) {
            this.putter.put(level, pos, Blocks.GLASS.defaultBlockState());
        }
    }
}

