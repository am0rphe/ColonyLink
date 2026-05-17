/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.data.PackOutput
 *  net.minecraft.data.recipes.RecipeCategory
 *  net.minecraft.data.recipes.RecipeOutput
 *  net.minecraft.data.recipes.SimpleCookingRecipeBuilder
 *  net.minecraft.world.item.crafting.Ingredient
 *  net.minecraft.world.level.ItemLike
 */
package appeng.datagen.providers.recipes;

import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.datagen.providers.recipes.AE2RecipeProvider;
import appeng.datagen.providers.tags.ConventionTags;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

public class SmeltingRecipes
extends AE2RecipeProvider {
    private static final int DEFAULT_SMELTING_TIME = 200;

    public SmeltingRecipes(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    public String getName() {
        return "AE2 Smelting Recipes";
    }

    public void buildRecipes(RecipeOutput consumer) {
        SimpleCookingRecipeBuilder.smelting((Ingredient)Ingredient.of(ConventionTags.CERTUS_QUARTZ_DUST), (RecipeCategory)RecipeCategory.MISC, AEItems.SILICON, (float)0.35f, (int)200).unlockedBy("has_certus_quartz_dust", SmeltingRecipes.has(ConventionTags.CERTUS_QUARTZ_DUST)).save(consumer, AppEng.makeId("smelting/silicon_from_certus_quartz_dust"));
        SimpleCookingRecipeBuilder.blasting((Ingredient)Ingredient.of(ConventionTags.CERTUS_QUARTZ_DUST), (RecipeCategory)RecipeCategory.MISC, AEItems.SILICON, (float)0.35f, (int)100).unlockedBy("has_certus_quartz_dust", SmeltingRecipes.has(ConventionTags.CERTUS_QUARTZ_DUST)).save(consumer, AppEng.makeId("blasting/silicon_from_certus_quartz_dust"));
        SimpleCookingRecipeBuilder.smelting((Ingredient)Ingredient.of((ItemLike[])new ItemLike[]{AEBlocks.SKY_STONE_BLOCK}), (RecipeCategory)RecipeCategory.MISC, AEBlocks.SMOOTH_SKY_STONE_BLOCK, (float)0.35f, (int)200).unlockedBy("has_sky_stone_block", SmeltingRecipes.has(AEBlocks.SKY_STONE_BLOCK)).save(consumer, AppEng.makeId("smelting/smooth_sky_stone_block"));
        SimpleCookingRecipeBuilder.smelting((Ingredient)Ingredient.of((ItemLike[])new ItemLike[]{AEItems.SKY_DUST}), (RecipeCategory)RecipeCategory.MISC, AEBlocks.SKY_STONE_BLOCK, (float)0.0f, (int)200).unlockedBy("has_sky_stone_dust", SmeltingRecipes.has(AEItems.SKY_DUST)).save(consumer, AppEng.makeId("blasting/sky_stone_block"));
    }
}

