/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.data.PackOutput
 *  net.neoforged.neoforge.common.data.ExistingFileHelper
 */
package appeng.datagen.providers.models;

import appeng.core.definitions.AEBlocks;
import appeng.datagen.providers.models.AE2BlockStateProvider;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class DecorationModelProvider
extends AE2BlockStateProvider {
    public DecorationModelProvider(PackOutput packOutput, ExistingFileHelper exFileHelper) {
        super(packOutput, "ae2", exFileHelper);
    }

    protected void registerStatesAndModels() {
        this.stairsBlock(AEBlocks.CHISELED_QUARTZ_STAIRS, "block/chiseled_quartz_block_top", "block/chiseled_quartz_block_side", "block/chiseled_quartz_block_top");
        this.slabBlock(AEBlocks.CHISELED_QUARTZ_SLAB, AEBlocks.CHISELED_QUARTZ_BLOCK, "block/chiseled_quartz_block_top", "block/chiseled_quartz_block_side", "block/chiseled_quartz_block_top");
        this.wall(AEBlocks.CHISELED_QUARTZ_WALL, "block/chiseled_quartz_block_side");
        this.stairsBlock(AEBlocks.FLUIX_STAIRS, AEBlocks.FLUIX_BLOCK);
        this.slabBlock(AEBlocks.FLUIX_SLAB, AEBlocks.FLUIX_BLOCK);
        this.wall(AEBlocks.FLUIX_WALL, "block/fluix_block");
        this.stairsBlock(AEBlocks.QUARTZ_STAIRS, AEBlocks.QUARTZ_BLOCK);
        this.slabBlock(AEBlocks.QUARTZ_SLAB, AEBlocks.QUARTZ_BLOCK);
        this.wall(AEBlocks.QUARTZ_WALL, "block/quartz_block");
        this.stairsBlock(AEBlocks.CUT_QUARTZ_STAIRS, AEBlocks.CUT_QUARTZ_BLOCK);
        this.slabBlock(AEBlocks.CUT_QUARTZ_SLAB, AEBlocks.CUT_QUARTZ_BLOCK);
        this.wall(AEBlocks.CUT_QUARTZ_WALL, "block/cut_quartz_block");
        this.simpleBlockAndItem(AEBlocks.SMOOTH_QUARTZ_BLOCK);
        this.stairsBlock(AEBlocks.SMOOTH_QUARTZ_STAIRS, AEBlocks.SMOOTH_QUARTZ_BLOCK);
        this.slabBlock(AEBlocks.SMOOTH_QUARTZ_SLAB, AEBlocks.SMOOTH_QUARTZ_BLOCK);
        this.wall(AEBlocks.SMOOTH_QUARTZ_WALL, "block/smooth_quartz_block");
        this.simpleBlockAndItem(AEBlocks.QUARTZ_BRICKS);
        this.stairsBlock(AEBlocks.QUARTZ_BRICK_STAIRS, AEBlocks.QUARTZ_BRICKS);
        this.slabBlock(AEBlocks.QUARTZ_BRICK_SLAB, AEBlocks.QUARTZ_BRICKS);
        this.wall(AEBlocks.QUARTZ_BRICK_WALL, "block/quartz_bricks");
        this.stairsBlock(AEBlocks.QUARTZ_PILLAR_STAIRS, "block/quartz_pillar_top", "block/quartz_pillar_side", "block/quartz_pillar_top");
        this.slabBlock(AEBlocks.QUARTZ_PILLAR_SLAB, AEBlocks.QUARTZ_PILLAR, "block/quartz_pillar_top", "block/quartz_pillar_side", "block/quartz_pillar_top");
        this.wall(AEBlocks.QUARTZ_PILLAR_WALL, "block/quartz_pillar_side");
        this.simpleBlockAndItem(AEBlocks.SKY_STONE_BLOCK);
        this.stairsBlock(AEBlocks.SKY_STONE_STAIRS, AEBlocks.SKY_STONE_BLOCK);
        this.slabBlock(AEBlocks.SKY_STONE_SLAB, AEBlocks.SKY_STONE_BLOCK);
        this.wall(AEBlocks.SKY_STONE_WALL, "block/sky_stone_block");
        this.simpleBlockAndItem(AEBlocks.SKY_STONE_SMALL_BRICK);
        this.stairsBlock(AEBlocks.SKY_STONE_SMALL_BRICK_STAIRS, AEBlocks.SKY_STONE_SMALL_BRICK);
        this.slabBlock(AEBlocks.SKY_STONE_SMALL_BRICK_SLAB, AEBlocks.SKY_STONE_SMALL_BRICK);
        this.wall(AEBlocks.SKY_STONE_SMALL_BRICK_WALL, "block/sky_stone_small_brick");
        this.simpleBlockAndItem(AEBlocks.SKY_STONE_BRICK);
        this.stairsBlock(AEBlocks.SKY_STONE_BRICK_STAIRS, AEBlocks.SKY_STONE_BRICK);
        this.slabBlock(AEBlocks.SKY_STONE_BRICK_SLAB, AEBlocks.SKY_STONE_BRICK);
        this.wall(AEBlocks.SKY_STONE_BRICK_WALL, "block/sky_stone_brick");
        this.stairsBlock(AEBlocks.SMOOTH_SKY_STONE_STAIRS, AEBlocks.SMOOTH_SKY_STONE_BLOCK);
        this.slabBlock(AEBlocks.SMOOTH_SKY_STONE_SLAB, AEBlocks.SMOOTH_SKY_STONE_BLOCK);
        this.wall(AEBlocks.SMOOTH_SKY_STONE_WALL, "block/smooth_sky_stone_block");
    }
}

