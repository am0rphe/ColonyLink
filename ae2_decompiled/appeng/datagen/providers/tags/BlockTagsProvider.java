/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.data.PackOutput
 *  net.minecraft.data.tags.IntrinsicHolderTagsProvider
 *  net.minecraft.tags.BlockTags
 *  net.minecraft.tags.TagKey
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.neoforged.neoforge.common.Tags$Blocks
 *  net.neoforged.neoforge.common.data.ExistingFileHelper
 */
package appeng.datagen.providers.tags;

import appeng.api.ids.AETags;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.BlockDefinition;
import appeng.datagen.providers.IAE2DataProvider;
import appeng.datagen.providers.tags.ConventionTags;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class BlockTagsProvider
extends IntrinsicHolderTagsProvider<Block>
implements IAE2DataProvider {
    private static final BlockDefinition<?>[] SKY_STONE_BLOCKS = new BlockDefinition[]{AEBlocks.SKY_STONE_BLOCK, AEBlocks.SMOOTH_SKY_STONE_BLOCK, AEBlocks.SKY_STONE_BRICK, AEBlocks.SKY_STONE_SMALL_BRICK, AEBlocks.SKY_STONE_CHEST, AEBlocks.SMOOTH_SKY_STONE_CHEST, AEBlocks.SKY_STONE_STAIRS, AEBlocks.SMOOTH_SKY_STONE_STAIRS, AEBlocks.SKY_STONE_BRICK_STAIRS, AEBlocks.SKY_STONE_SMALL_BRICK_STAIRS, AEBlocks.SKY_STONE_WALL, AEBlocks.SMOOTH_SKY_STONE_WALL, AEBlocks.SKY_STONE_BRICK_WALL, AEBlocks.SKY_STONE_SMALL_BRICK_WALL, AEBlocks.SKY_STONE_SLAB, AEBlocks.SMOOTH_SKY_STONE_SLAB, AEBlocks.SKY_STONE_BRICK_SLAB, AEBlocks.SKY_STONE_SMALL_BRICK_SLAB};

    public BlockTagsProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> registries, ExistingFileHelper existingFileHelper) {
        super(packOutput, Registries.BLOCK, registries, block -> block.builtInRegistryHolder().key(), "ae2", existingFileHelper);
    }

    protected void addTags(HolderLookup.Provider registries) {
        this.tag(AETags.SPATIAL_BLACKLIST).add((Object)Blocks.BEDROCK).addOptionalTag(ConventionTags.IMMOVABLE_BLOCKS.location());
        this.tag(AETags.ANNIHILATION_PLANE_BLOCK_BLACKLIST);
        this.tag(AETags.FACADE_BLOCK_WHITELIST).add((Object[])new Block[]{AEBlocks.QUARTZ_GLASS.block(), AEBlocks.QUARTZ_VIBRANT_GLASS.block(), Blocks.CHISELED_BOOKSHELF, Blocks.JUKEBOX, Blocks.FURNACE, Blocks.BLAST_FURNACE, Blocks.DROPPER, Blocks.DISPENSER, Blocks.CRAFTER, Blocks.BARREL, Blocks.BEE_NEST, Blocks.BEEHIVE, Blocks.SCULK_CATALYST, Blocks.SOUL_SAND, Blocks.HONEY_BLOCK, AEBlocks.CONTROLLER.block(), AEBlocks.CRAFTING_STORAGE_1K.block(), AEBlocks.CRAFTING_STORAGE_4K.block(), AEBlocks.CRAFTING_STORAGE_16K.block(), AEBlocks.CRAFTING_STORAGE_64K.block(), AEBlocks.CRAFTING_STORAGE_256K.block(), AEBlocks.CRAFTING_MONITOR.block(), AEBlocks.CRAFTING_UNIT.block(), AEBlocks.CRAFTING_ACCELERATOR.block()}).addOptionalTag(ConventionTags.GLASS_BLOCK.location());
        this.tag(AETags.GROWTH_ACCELERATABLE).add((Object[])new Block[]{Blocks.BAMBOO_SAPLING, Blocks.BAMBOO, Blocks.SUGAR_CANE, Blocks.SUGAR_CANE, Blocks.VINE, Blocks.TWISTING_VINES, Blocks.WEEPING_VINES, Blocks.CAVE_VINES, Blocks.SWEET_BERRY_BUSH, Blocks.NETHER_WART, Blocks.KELP, Blocks.COCOA}).addOptionalTag(ConventionTags.CROPS.location()).addOptionalTag(ConventionTags.SAPLINGS.location()).addTag(ConventionTags.BUDDING_BLOCKS_BLOCKS);
        this.tag(ConventionTags.BUDDING_BLOCKS_BLOCKS).add((Object)AEBlocks.FLAWLESS_BUDDING_QUARTZ.block()).add((Object)AEBlocks.FLAWED_BUDDING_QUARTZ.block()).add((Object)AEBlocks.CHIPPED_BUDDING_QUARTZ.block()).add((Object)AEBlocks.DAMAGED_BUDDING_QUARTZ.block());
        this.tag(ConventionTags.BUDS_BLOCKS).add((Object)AEBlocks.SMALL_QUARTZ_BUD.block()).add((Object)AEBlocks.MEDIUM_QUARTZ_BUD.block()).add((Object)AEBlocks.LARGE_QUARTZ_BUD.block());
        this.tag(ConventionTags.CLUSTERS_BLOCKS).add((Object)AEBlocks.QUARTZ_CLUSTER.block());
        this.tag(ConventionTags.CERTUS_QUARTZ_STORAGE_BLOCK_BLOCK).add((Object)AEBlocks.QUARTZ_BLOCK.block());
        this.tag(Tags.Blocks.STORAGE_BLOCKS).addTag(ConventionTags.CERTUS_QUARTZ_STORAGE_BLOCK_BLOCK);
        this.tag(BlockTags.WALLS).add((Object[])new Block[]{AEBlocks.SKY_STONE_WALL.block(), AEBlocks.SMOOTH_SKY_STONE_WALL.block(), AEBlocks.SKY_STONE_BRICK_WALL.block(), AEBlocks.SKY_STONE_SMALL_BRICK_WALL.block(), AEBlocks.FLUIX_WALL.block(), AEBlocks.QUARTZ_WALL.block(), AEBlocks.CUT_QUARTZ_WALL.block(), AEBlocks.SMOOTH_QUARTZ_WALL.block(), AEBlocks.QUARTZ_BRICK_WALL.block(), AEBlocks.CHISELED_QUARTZ_WALL.block(), AEBlocks.QUARTZ_PILLAR_WALL.block()});
        this.tag(Tags.Blocks.CHESTS).add((Object[])new Block[]{AEBlocks.SKY_STONE_CHEST.block(), AEBlocks.SMOOTH_SKY_STONE_CHEST.block()});
        this.tag(ConventionTags.GLASS_BLOCK).add((Object[])new Block[]{AEBlocks.QUARTZ_GLASS.block(), AEBlocks.QUARTZ_VIBRANT_GLASS.block()});
        this.tag(BlockTags.WALL_POST_OVERRIDE).add((Object[])new Block[]{AEBlocks.QUARTZ_FIXTURE.block(), AEBlocks.LIGHT_DETECTOR.block()});
        this.addEffectiveTools();
    }

    private void addEffectiveTools() {
        HashMap specialTags = new HashMap();
        for (BlockDefinition<?> skyStoneBlock : SKY_STONE_BLOCKS) {
            specialTags.put(skyStoneBlock, List.of(BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.NEEDS_IRON_TOOL));
        }
        List<TagKey> defaultTags = List.of(BlockTags.MINEABLE_WITH_PICKAXE);
        for (BlockDefinition<?> block : AEBlocks.getBlocks()) {
            for (TagKey desiredTag : specialTags.getOrDefault(block, defaultTags)) {
                this.tag(desiredTag).add(block.block());
            }
        }
    }
}

