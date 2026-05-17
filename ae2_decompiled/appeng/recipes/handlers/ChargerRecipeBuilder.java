/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.data.recipes.RecipeOutput
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.tags.TagKey
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.crafting.Ingredient
 *  net.minecraft.world.item.crafting.Recipe
 *  net.minecraft.world.level.ItemLike
 */
package appeng.recipes.handlers;

import appeng.recipes.handlers.ChargerRecipe;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;

public class ChargerRecipeBuilder {
    public static void charge(RecipeOutput consumer, ResourceLocation id, ItemLike input, ItemLike output) {
        consumer.accept(id, (Recipe)new ChargerRecipe(Ingredient.of((ItemLike[])new ItemLike[]{input}), output.asItem().getDefaultInstance()), null);
    }

    public static void charge(RecipeOutput consumer, ResourceLocation id, TagKey<Item> input, ItemLike output) {
        consumer.accept(id, (Recipe)new ChargerRecipe(Ingredient.of(input), output.asItem().getDefaultInstance()), null);
    }

    public static void charge(RecipeOutput consumer, ResourceLocation id, Ingredient input, ItemLike output) {
        consumer.accept(id, (Recipe)new ChargerRecipe(input, output.asItem().getDefaultInstance()), null);
    }
}

