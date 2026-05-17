/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.data.PackOutput
 *  net.minecraft.data.recipes.RecipeCategory
 *  net.minecraft.data.recipes.RecipeOutput
 *  net.minecraft.data.recipes.ShapedRecipeBuilder
 *  net.minecraft.data.recipes.SingleItemRecipeBuilder
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.crafting.Ingredient
 *  net.minecraft.world.level.ItemLike
 */
package appeng.datagen.providers.recipes;

import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.BlockDefinition;
import appeng.datagen.providers.recipes.AE2RecipeProvider;
import appeng.datagen.providers.recipes.RecipeCriteria;
import appeng.datagen.providers.tags.ConventionTags;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.SingleItemRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

public class DecorationRecipes
extends AE2RecipeProvider {
    BlockDefinition<?>[][] blocks = new BlockDefinition[][]{{AEBlocks.SKY_STONE_BLOCK, AEBlocks.SKY_STONE_SLAB, AEBlocks.SKY_STONE_STAIRS, AEBlocks.SKY_STONE_WALL}, {AEBlocks.SMOOTH_SKY_STONE_BLOCK, AEBlocks.SMOOTH_SKY_STONE_SLAB, AEBlocks.SMOOTH_SKY_STONE_STAIRS, AEBlocks.SMOOTH_SKY_STONE_WALL}, {AEBlocks.SKY_STONE_BRICK, AEBlocks.SKY_STONE_BRICK_SLAB, AEBlocks.SKY_STONE_BRICK_STAIRS, AEBlocks.SKY_STONE_BRICK_WALL}, {AEBlocks.SKY_STONE_SMALL_BRICK, AEBlocks.SKY_STONE_SMALL_BRICK_SLAB, AEBlocks.SKY_STONE_SMALL_BRICK_STAIRS, AEBlocks.SKY_STONE_SMALL_BRICK_WALL}, {AEBlocks.FLUIX_BLOCK, AEBlocks.FLUIX_SLAB, AEBlocks.FLUIX_STAIRS, AEBlocks.FLUIX_WALL}, {AEBlocks.QUARTZ_BLOCK, AEBlocks.QUARTZ_SLAB, AEBlocks.QUARTZ_STAIRS, AEBlocks.QUARTZ_WALL}, {AEBlocks.CUT_QUARTZ_BLOCK, AEBlocks.CUT_QUARTZ_SLAB, AEBlocks.CUT_QUARTZ_STAIRS, AEBlocks.CUT_QUARTZ_WALL}, {AEBlocks.SMOOTH_QUARTZ_BLOCK, AEBlocks.SMOOTH_QUARTZ_SLAB, AEBlocks.SMOOTH_QUARTZ_STAIRS, AEBlocks.SMOOTH_QUARTZ_WALL}, {AEBlocks.QUARTZ_BRICKS, AEBlocks.QUARTZ_BRICK_SLAB, AEBlocks.QUARTZ_BRICK_STAIRS, AEBlocks.QUARTZ_BRICK_WALL}, {AEBlocks.CHISELED_QUARTZ_BLOCK, AEBlocks.CHISELED_QUARTZ_SLAB, AEBlocks.CHISELED_QUARTZ_STAIRS, AEBlocks.CHISELED_QUARTZ_WALL}, {AEBlocks.QUARTZ_PILLAR, AEBlocks.QUARTZ_PILLAR_SLAB, AEBlocks.QUARTZ_PILLAR_STAIRS, AEBlocks.QUARTZ_PILLAR_WALL}};

    public DecorationRecipes(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    public void buildRecipes(RecipeOutput consumer) {
        for (BlockDefinition<?>[] block : this.blocks) {
            this.slabRecipe(consumer, block[0], block[1]);
            this.stairRecipe(consumer, block[0], block[2]);
            this.wallRecipe(consumer, block[0], block[3]);
        }
        ShapedRecipeBuilder.shaped((RecipeCategory)RecipeCategory.MISC, AEBlocks.NOT_SO_MYSTERIOUS_CUBE, (int)4).pattern("ScS").pattern("eCl").pattern("SsS").define(Character.valueOf('S'), AEBlocks.SMOOTH_SKY_STONE_BLOCK).define(Character.valueOf('C'), AEBlocks.CONTROLLER).define(Character.valueOf('c'), AEItems.CALCULATION_PROCESSOR_PRESS).define(Character.valueOf('e'), AEItems.ENGINEERING_PROCESSOR_PRESS).define(Character.valueOf('l'), AEItems.LOGIC_PROCESSOR_PRESS).define(Character.valueOf('s'), AEItems.SILICON_PRESS).unlockedBy("press", DecorationRecipes.has(ConventionTags.INSCRIBER_PRESSES)).save(consumer, AppEng.makeId("shaped/not_so_mysterious_cube"));
    }

    private void slabRecipe(RecipeOutput consumer, BlockDefinition<?> block, BlockDefinition<?> slabs) {
        Object inputBlock = block.block();
        Object outputBlock = slabs.block();
        ShapedRecipeBuilder.shaped((RecipeCategory)RecipeCategory.MISC, outputBlock, (int)6).pattern("###").define(Character.valueOf('#'), inputBlock).unlockedBy(RecipeCriteria.criterionName(block), DecorationRecipes.has(inputBlock)).save(consumer, this.prefix("shaped/slabs/", block.id()));
        SingleItemRecipeBuilder.stonecutting((Ingredient)Ingredient.of((ItemLike[])new ItemLike[]{inputBlock}), (RecipeCategory)RecipeCategory.MISC, outputBlock, (int)2).unlockedBy(RecipeCriteria.criterionName(block), DecorationRecipes.has(inputBlock)).save(consumer, this.prefix("block_cutter/slabs/", slabs.id()));
    }

    private void stairRecipe(RecipeOutput consumer, BlockDefinition<?> block, BlockDefinition<?> stairs) {
        Object inputBlock = block.block();
        Object outputBlock = stairs.block();
        ShapedRecipeBuilder.shaped((RecipeCategory)RecipeCategory.MISC, outputBlock, (int)4).pattern("#  ").pattern("## ").pattern("###").define(Character.valueOf('#'), inputBlock).unlockedBy(RecipeCriteria.criterionName(block), DecorationRecipes.has(inputBlock)).save(consumer, this.prefix("shaped/stairs/", block.id()));
        SingleItemRecipeBuilder.stonecutting((Ingredient)Ingredient.of((ItemLike[])new ItemLike[]{inputBlock}), (RecipeCategory)RecipeCategory.MISC, outputBlock).unlockedBy(RecipeCriteria.criterionName(block), DecorationRecipes.has(inputBlock)).save(consumer, this.prefix("block_cutter/stairs/", stairs.id()));
    }

    private void wallRecipe(RecipeOutput consumer, BlockDefinition<?> block, BlockDefinition<?> wall) {
        Object inputBlock = block.block();
        Object outputBlock = wall.block();
        ShapedRecipeBuilder.shaped((RecipeCategory)RecipeCategory.MISC, outputBlock, (int)6).pattern("###").pattern("###").define(Character.valueOf('#'), inputBlock).unlockedBy(RecipeCriteria.criterionName(block), DecorationRecipes.has(inputBlock)).save(consumer, this.prefix("shaped/walls/", block.id()));
        SingleItemRecipeBuilder.stonecutting((Ingredient)Ingredient.of((ItemLike[])new ItemLike[]{inputBlock}), (RecipeCategory)RecipeCategory.MISC, outputBlock).unlockedBy(RecipeCriteria.criterionName(block), DecorationRecipes.has(inputBlock)).save(consumer, this.prefix("block_cutter/walls/", wall.id()));
    }

    private ResourceLocation prefix(String prefix, ResourceLocation id) {
        return ResourceLocation.fromNamespaceAndPath((String)id.getNamespace(), (String)(prefix + id.getPath()));
    }

    public String getName() {
        return "Applied Energistics 2 Decorative Blocks";
    }
}

