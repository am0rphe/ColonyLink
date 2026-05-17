/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.data.PackOutput
 *  net.minecraft.data.recipes.RecipeCategory
 *  net.minecraft.data.recipes.RecipeOutput
 *  net.minecraft.data.recipes.SmithingTransformRecipeBuilder
 *  net.minecraft.tags.TagKey
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.crafting.Ingredient
 *  net.minecraft.world.level.ItemLike
 */
package appeng.datagen.providers.recipes;

import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.ItemDefinition;
import appeng.datagen.providers.recipes.AE2RecipeProvider;
import appeng.datagen.providers.tags.ConventionTags;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.SmithingTransformRecipeBuilder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

public class SmithingRecipes
extends AE2RecipeProvider {
    public SmithingRecipes(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    public void buildRecipes(RecipeOutput consumer) {
        this.fluixSmithing(consumer, ConventionTags.QUARTZ_AXE, AEItems.FLUIX_AXE);
        this.fluixSmithing(consumer, ConventionTags.QUARTZ_HOE, AEItems.FLUIX_HOE);
        this.fluixSmithing(consumer, ConventionTags.QUARTZ_PICK, AEItems.FLUIX_PICK);
        this.fluixSmithing(consumer, ConventionTags.QUARTZ_SHOVEL, AEItems.FLUIX_SHOVEL);
        this.fluixSmithing(consumer, ConventionTags.QUARTZ_SWORD, AEItems.FLUIX_SWORD);
    }

    private void fluixSmithing(RecipeOutput consumer, TagKey<Item> quartzTool, ItemDefinition<?> fluixTool) {
        SmithingTransformRecipeBuilder.smithing((Ingredient)Ingredient.of((ItemLike[])new ItemLike[]{AEItems.FLUIX_UPGRADE_SMITHING_TEMPLATE}), (Ingredient)Ingredient.of(quartzTool), (Ingredient)Ingredient.of((ItemLike[])new ItemLike[]{AEBlocks.FLUIX_BLOCK}), (RecipeCategory)RecipeCategory.MISC, fluixTool.asItem()).unlocks("has_crystals/fluix", SmithingRecipes.has(ConventionTags.ALL_FLUIX)).save(consumer, AppEng.makeId("tools/" + SmithingRecipes.getItemName(fluixTool)));
    }

    public String getName() {
        return "AE2 Smithing Recipes";
    }
}

