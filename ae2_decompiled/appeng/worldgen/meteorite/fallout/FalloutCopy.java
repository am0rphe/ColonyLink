/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Holder
 *  net.minecraft.tags.BiomeTags
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.level.LevelAccessor
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.state.BlockState
 *  net.neoforged.neoforge.common.Tags$Biomes
 */
package appeng.worldgen.meteorite.fallout;

import appeng.worldgen.meteorite.MeteoriteBlockPutter;
import appeng.worldgen.meteorite.fallout.Fallout;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.Tags;

public class FalloutCopy
extends Fallout {
    private static final float SPECIFIED_BLOCK_THRESHOLD = 0.9f;
    private static final float AIR_BLOCK_THRESHOLD = 0.8f;
    private static final float BLOCK_THRESHOLD_STEP = 0.1f;
    private final BlockState block;
    private final MeteoriteBlockPutter putter;

    public FalloutCopy(LevelAccessor level, BlockPos pos, MeteoriteBlockPutter putter, BlockState skyStone, RandomSource random) {
        super(putter, skyStone, random);
        this.putter = putter;
        Holder biome = level.getBiome(pos);
        this.block = biome.is(BiomeTags.IS_BADLANDS) ? Blocks.TERRACOTTA.defaultBlockState() : (biome.is(Tags.Biomes.IS_SNOWY) ? Blocks.SNOW_BLOCK.defaultBlockState() : (biome.is(BiomeTags.IS_BEACH) || biome.is(Tags.Biomes.IS_SANDY) ? Blocks.SAND.defaultBlockState() : (biome.is(Tags.Biomes.IS_PLAINS) || biome.is(BiomeTags.IS_FOREST) ? Blocks.DIRT.defaultBlockState() : Blocks.COBBLESTONE.defaultBlockState())));
    }

    @Override
    public void getRandomFall(LevelAccessor level, BlockPos pos) {
        float a = this.random.nextFloat();
        if (a > 0.9f) {
            this.putter.put(level, pos, this.block);
        } else {
            this.getOther(level, pos, a);
        }
    }

    public void getOther(LevelAccessor level, BlockPos pos, float a) {
    }

    @Override
    public void getRandomInset(LevelAccessor level, BlockPos pos) {
        float a = this.random.nextFloat();
        if (a > 0.9f) {
            this.putter.put(level, pos, this.block);
        } else if (a > 0.8f) {
            this.putter.put(level, pos, Blocks.AIR.defaultBlockState());
        } else {
            this.getOther(level, pos, a - 0.1f);
        }
    }
}

