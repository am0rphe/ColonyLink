/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.NonNullList
 *  net.minecraft.data.recipes.RecipeOutput
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.crafting.Ingredient
 *  net.minecraft.world.item.crafting.Recipe
 *  net.minecraft.world.level.ItemLike
 */
package appeng.recipes.transform;

import appeng.recipes.transform.TransformCircumstance;
import appeng.recipes.transform.TransformRecipe;
import java.util.Collections;
import net.minecraft.core.NonNullList;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;

public class TransformRecipeBuilder {
    public static void transform(RecipeOutput consumer, ResourceLocation id, ItemLike output, int count, TransformCircumstance circumstance, ItemLike ... inputs) {
        NonNullList ingredients = NonNullList.createWithCapacity((int)inputs.length);
        for (ItemLike input : inputs) {
            ingredients.add((Object)Ingredient.of((ItemLike[])new ItemLike[]{input}));
        }
        TransformRecipe recipe = new TransformRecipe((NonNullList<Ingredient>)ingredients, TransformRecipeBuilder.toStack(output, count), circumstance);
        consumer.accept(id, (Recipe)recipe, null);
    }

    public static void transform(RecipeOutput consumer, ResourceLocation id, ItemLike output, int count, TransformCircumstance circumstance, Ingredient ... inputs) {
        NonNullList ingredients = NonNullList.createWithCapacity((int)inputs.length);
        Collections.addAll(ingredients, inputs);
        TransformRecipe recipe = new TransformRecipe((NonNullList<Ingredient>)ingredients, TransformRecipeBuilder.toStack(output, count), circumstance);
        consumer.accept(id, (Recipe)recipe, null);
    }

    private static ItemStack toStack(ItemLike item, int count) {
        ItemStack stack = item.asItem().getDefaultInstance();
        stack.setCount(count);
        return stack;
    }
}

