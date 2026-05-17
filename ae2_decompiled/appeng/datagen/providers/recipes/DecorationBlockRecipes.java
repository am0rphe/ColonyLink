/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.data.PackOutput
 *  net.minecraft.data.recipes.RecipeCategory
 *  net.minecraft.data.recipes.RecipeOutput
 *  net.minecraft.data.recipes.ShapedRecipeBuilder
 *  net.minecraft.data.recipes.SimpleCookingRecipeBuilder
 *  net.minecraft.data.recipes.SingleItemRecipeBuilder
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.item.crafting.Ingredient
 *  net.minecraft.world.level.ItemLike
 */
package appeng.datagen.providers.recipes;

import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;
import appeng.core.definitions.BlockDefinition;
import appeng.core.definitions.ItemDefinition;
import appeng.datagen.providers.recipes.AE2RecipeProvider;
import appeng.datagen.providers.recipes.RecipeCriteria;
import appeng.datagen.providers.tags.ConventionTags;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.data.recipes.SingleItemRecipeBuilder;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

public class DecorationBlockRecipes
extends AE2RecipeProvider {
    public DecorationBlockRecipes(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    public String getName() {
        return "AE2 Decoration Blocks";
    }

    public void buildRecipes(RecipeOutput consumer) {
        this.crystalBlock(consumer, AEItems.CERTUS_QUARTZ_CRYSTAL, AEBlocks.QUARTZ_BLOCK);
        this.crystalBlock(consumer, AEItems.FLUIX_CRYSTAL, AEBlocks.FLUIX_BLOCK);
        ShapedRecipeBuilder.shaped((RecipeCategory)RecipeCategory.MISC, AEBlocks.SKY_STONE_BRICK, (int)4).pattern("aa").pattern("aa").define(Character.valueOf('a'), AEBlocks.SMOOTH_SKY_STONE_BLOCK).unlockedBy(RecipeCriteria.criterionName(AEBlocks.SMOOTH_SKY_STONE_BLOCK), DecorationBlockRecipes.has(AEBlocks.SMOOTH_SKY_STONE_BLOCK)).save(consumer, AppEng.makeId("decorative/sky_stone_brick"));
        ShapedRecipeBuilder.shaped((RecipeCategory)RecipeCategory.MISC, AEBlocks.SKY_STONE_SMALL_BRICK, (int)4).pattern("aa").pattern("aa").define(Character.valueOf('a'), AEBlocks.SKY_STONE_BRICK).unlockedBy(RecipeCriteria.criterionName(AEBlocks.SKY_STONE_BRICK), DecorationBlockRecipes.has(AEBlocks.SKY_STONE_BRICK)).save(consumer, AppEng.makeId("decorative/sky_stone_small_brick"));
        SingleItemRecipeBuilder.stonecutting((Ingredient)Ingredient.of((ItemLike[])new ItemLike[]{AEBlocks.SMOOTH_SKY_STONE_BLOCK}), (RecipeCategory)RecipeCategory.MISC, AEBlocks.SKY_STONE_BRICK).unlockedBy(RecipeCriteria.criterionName(AEBlocks.SMOOTH_SKY_STONE_BLOCK), DecorationBlockRecipes.has(AEBlocks.SMOOTH_SKY_STONE_BLOCK)).save(consumer, AppEng.makeId("decorative/sky_stone_brick_from_stonecutting"));
        SingleItemRecipeBuilder.stonecutting((Ingredient)Ingredient.of((ItemLike[])new ItemLike[]{AEBlocks.SMOOTH_SKY_STONE_BLOCK}), (RecipeCategory)RecipeCategory.MISC, AEBlocks.SKY_STONE_SMALL_BRICK).unlockedBy(RecipeCriteria.criterionName(AEBlocks.SMOOTH_SKY_STONE_BLOCK), DecorationBlockRecipes.has(AEBlocks.SMOOTH_SKY_STONE_BLOCK)).save(consumer, AppEng.makeId("decorative/sky_stone_small_brick_from_stonecutting"));
        ShapedRecipeBuilder.shaped((RecipeCategory)RecipeCategory.MISC, AEBlocks.CUT_QUARTZ_BLOCK, (int)4).pattern("aa").pattern("aa").define(Character.valueOf('a'), AEBlocks.QUARTZ_BLOCK).unlockedBy(RecipeCriteria.criterionName(AEBlocks.QUARTZ_BLOCK), DecorationBlockRecipes.has(AEBlocks.QUARTZ_BLOCK)).save(consumer, AppEng.makeId("decorative/cut_quartz_block"));
        ShapedRecipeBuilder.shaped((RecipeCategory)RecipeCategory.MISC, AEBlocks.QUARTZ_BRICKS, (int)4).pattern("aa").pattern("aa").define(Character.valueOf('a'), AEBlocks.CUT_QUARTZ_BLOCK).unlockedBy(RecipeCriteria.criterionName(AEBlocks.CUT_QUARTZ_BLOCK), DecorationBlockRecipes.has(AEBlocks.CUT_QUARTZ_BLOCK)).save(consumer, AppEng.makeId("decorative/certus_quartz_bricks"));
        ShapedRecipeBuilder.shaped((RecipeCategory)RecipeCategory.MISC, AEBlocks.QUARTZ_PILLAR, (int)2).pattern("a").pattern("a").define(Character.valueOf('a'), AEBlocks.CUT_QUARTZ_BLOCK).unlockedBy(RecipeCriteria.criterionName(AEBlocks.CUT_QUARTZ_BLOCK), DecorationBlockRecipes.has(AEBlocks.CUT_QUARTZ_BLOCK)).save(consumer, AppEng.makeId("decorative/certus_quartz_pillar"));
        ShapedRecipeBuilder.shaped((RecipeCategory)RecipeCategory.MISC, AEBlocks.CHISELED_QUARTZ_BLOCK, (int)1).pattern("a").pattern("a").define(Character.valueOf('a'), AEBlocks.CUT_QUARTZ_SLAB).unlockedBy(RecipeCriteria.criterionName(AEBlocks.CUT_QUARTZ_SLAB), DecorationBlockRecipes.has(AEBlocks.CUT_QUARTZ_SLAB)).save(consumer, AppEng.makeId("decorative/chiseled_quartz_block"));
        SimpleCookingRecipeBuilder.smelting((Ingredient)Ingredient.of((ItemLike[])new ItemLike[]{AEBlocks.CUT_QUARTZ_BLOCK}), (RecipeCategory)RecipeCategory.MISC, AEBlocks.SMOOTH_QUARTZ_BLOCK, (float)0.1f, (int)200).unlockedBy(RecipeCriteria.criterionName(AEBlocks.CUT_QUARTZ_BLOCK), DecorationBlockRecipes.has(AEBlocks.CUT_QUARTZ_BLOCK)).save(consumer, AppEng.makeId("decorative/smooth_quartz_block"));
        SingleItemRecipeBuilder.stonecutting((Ingredient)Ingredient.of((ItemLike[])new ItemLike[]{AEBlocks.QUARTZ_BLOCK}), (RecipeCategory)RecipeCategory.MISC, AEBlocks.CUT_QUARTZ_BLOCK).unlockedBy(RecipeCriteria.criterionName(AEBlocks.QUARTZ_BLOCK), DecorationBlockRecipes.has(AEBlocks.QUARTZ_BLOCK)).save(consumer, AppEng.makeId("decorative/cut_quartz_block_from_stonecutting"));
        SingleItemRecipeBuilder.stonecutting((Ingredient)Ingredient.of((ItemLike[])new ItemLike[]{AEBlocks.CUT_QUARTZ_BLOCK}), (RecipeCategory)RecipeCategory.MISC, AEBlocks.QUARTZ_BRICKS).unlockedBy(RecipeCriteria.criterionName(AEBlocks.CUT_QUARTZ_BLOCK), DecorationBlockRecipes.has(AEBlocks.CUT_QUARTZ_BLOCK)).save(consumer, AppEng.makeId("decorative/certus_quartz_bricks_from_stonecutting"));
        SingleItemRecipeBuilder.stonecutting((Ingredient)Ingredient.of((ItemLike[])new ItemLike[]{AEBlocks.CUT_QUARTZ_BLOCK}), (RecipeCategory)RecipeCategory.MISC, AEBlocks.QUARTZ_PILLAR).unlockedBy(RecipeCriteria.criterionName(AEBlocks.CUT_QUARTZ_BLOCK), DecorationBlockRecipes.has(AEBlocks.CUT_QUARTZ_BLOCK)).save(consumer, AppEng.makeId("decorative/certus_quartz_pillar_from_stonecutting"));
        SingleItemRecipeBuilder.stonecutting((Ingredient)Ingredient.of((ItemLike[])new ItemLike[]{AEBlocks.CUT_QUARTZ_BLOCK}), (RecipeCategory)RecipeCategory.MISC, AEBlocks.CHISELED_QUARTZ_BLOCK).unlockedBy(RecipeCriteria.criterionName(AEBlocks.CUT_QUARTZ_BLOCK), DecorationBlockRecipes.has(AEBlocks.CUT_QUARTZ_BLOCK)).save(consumer, AppEng.makeId("decorative/chiseled_quartz_block_from_stonecutting"));
        ShapedRecipeBuilder.shaped((RecipeCategory)RecipeCategory.MISC, AEBlocks.LIGHT_DETECTOR).pattern("ab").define(Character.valueOf('a'), ConventionTags.ALL_NETHER_QUARTZ).define(Character.valueOf('b'), AEParts.CABLE_ANCHOR).unlockedBy("has_nether_quartz", DecorationBlockRecipes.has(ConventionTags.ALL_NETHER_QUARTZ)).unlockedBy("has_iron_ingot", DecorationBlockRecipes.has((ItemLike)Items.IRON_INGOT)).save(consumer, AppEng.makeId("decorative/light_detector"));
        ShapedRecipeBuilder.shaped((RecipeCategory)RecipeCategory.MISC, AEBlocks.QUARTZ_FIXTURE, (int)2).pattern("ab").define(Character.valueOf('a'), AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED).define(Character.valueOf('b'), (ItemLike)Items.IRON_INGOT).unlockedBy(RecipeCriteria.criterionName(AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED), DecorationBlockRecipes.has(AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED)).save(consumer, AppEng.makeId("decorative/quartz_fixture"));
        ShapedRecipeBuilder.shaped((RecipeCategory)RecipeCategory.MISC, AEBlocks.QUARTZ_FIXTURE, (int)2).pattern("ab").define(Character.valueOf('a'), AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED).define(Character.valueOf('b'), AEParts.CABLE_ANCHOR).unlockedBy(RecipeCriteria.criterionName(AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED), DecorationBlockRecipes.has(AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED)).save(consumer, AppEng.makeId("decorative/quartz_fixture_from_anchors"));
        ShapedRecipeBuilder.shaped((RecipeCategory)RecipeCategory.MISC, AEBlocks.QUARTZ_GLASS, (int)4).pattern("aba").pattern("bab").pattern("aba").define(Character.valueOf('a'), ConventionTags.ALL_QUARTZ_DUST).define(Character.valueOf('b'), ConventionTags.GLASS_CHEAP).unlockedBy("has_quartz_dust", DecorationBlockRecipes.has(ConventionTags.ALL_QUARTZ_DUST)).save(consumer, AppEng.makeId("decorative/quartz_glass"));
        ShapedRecipeBuilder.shaped((RecipeCategory)RecipeCategory.MISC, AEBlocks.QUARTZ_VIBRANT_GLASS).pattern("aba").define(Character.valueOf('a'), (ItemLike)Items.GLOWSTONE_DUST).define(Character.valueOf('b'), AEBlocks.QUARTZ_GLASS).unlockedBy(RecipeCriteria.criterionName(AEBlocks.QUARTZ_GLASS), DecorationBlockRecipes.has(AEBlocks.QUARTZ_GLASS)).save(consumer, AppEng.makeId("decorative/quartz_vibrant_glass"));
    }

    private void crystalBlock(RecipeOutput consumer, ItemDefinition<?> crystal, BlockDefinition<?> block) {
        ShapedRecipeBuilder.shaped((RecipeCategory)RecipeCategory.MISC, block).pattern("aa").pattern("aa").define(Character.valueOf('a'), crystal).unlockedBy(RecipeCriteria.criterionName(crystal), DecorationBlockRecipes.has(crystal)).save(consumer, AppEng.makeId("decorative/" + block.id().getPath()));
    }
}

