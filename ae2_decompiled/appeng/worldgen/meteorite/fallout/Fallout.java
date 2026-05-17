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
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class Fallout {
    private final MeteoriteBlockPutter putter;
    private final BlockState skyStone;
    protected final RandomSource random;

    public Fallout(MeteoriteBlockPutter putter, BlockState skyStone, RandomSource random) {
        this.putter = putter;
        this.skyStone = skyStone;
        this.random = random;
    }

    public int adjustCrater() {
        return 0;
    }

    public void getRandomFall(LevelAccessor level, BlockPos pos) {
        float a = this.random.nextFloat();
        if (a > 0.9f) {
            this.putter.put(level, pos, Blocks.STONE.defaultBlockState());
        } else if (a > 0.8f) {
            this.putter.put(level, pos, Blocks.COBBLESTONE.defaultBlockState());
        } else if (a > 0.7f) {
            this.putter.put(level, pos, Blocks.DIRT.defaultBlockState());
        } else {
            this.putter.put(level, pos, Blocks.GRAVEL.defaultBlockState());
        }
    }

    public void getRandomInset(LevelAccessor level, BlockPos pos) {
        float a = this.random.nextFloat();
        if (a > 0.9f) {
            this.putter.put(level, pos, Blocks.COBBLESTONE.defaultBlockState());
        } else if (a > 0.8f) {
            this.putter.put(level, pos, Blocks.STONE.defaultBlockState());
        } else if (a > 0.7f) {
            this.putter.put(level, pos, Blocks.GRASS_BLOCK.defaultBlockState());
        } else if (a > 0.6f) {
            this.putter.put(level, pos, this.skyStone);
        } else if (a > 0.5f) {
            this.putter.put(level, pos, Blocks.GRAVEL.defaultBlockState());
        } else {
            this.putter.put(level, pos, Blocks.AIR.defaultBlockState());
        }
    }
}

