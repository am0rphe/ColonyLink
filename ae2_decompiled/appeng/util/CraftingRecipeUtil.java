/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Preconditions
 *  net.minecraft.core.NonNullList
 *  net.minecraft.world.item.crafting.Ingredient
 *  net.minecraft.world.item.crafting.Recipe
 *  net.minecraft.world.item.crafting.ShapedRecipe
 *  net.minecraft.world.item.crafting.SmithingTransformRecipe
 *  net.minecraft.world.item.crafting.SmithingTrimRecipe
 */
package appeng.util;

import com.google.common.base.Preconditions;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import net.minecraft.world.item.crafting.SmithingTrimRecipe;

public final class CraftingRecipeUtil {
    private CraftingRecipeUtil() {
    }

    public static NonNullList<Ingredient> ensure3by3CraftingMatrix(Recipe<?> recipe) {
        NonNullList<Ingredient> ingredients = CraftingRecipeUtil.getIngredients(recipe);
        NonNullList expandedIngredients = NonNullList.withSize((int)9, (Object)Ingredient.EMPTY);
        Preconditions.checkArgument((ingredients.size() <= 9 ? 1 : 0) != 0);
        if (recipe instanceof ShapedRecipe) {
            ShapedRecipe shapedRecipe = (ShapedRecipe)recipe;
            int width = shapedRecipe.getWidth();
            int height = shapedRecipe.getHeight();
            Preconditions.checkArgument((width <= 3 && height <= 3 ? 1 : 0) != 0);
            for (int h = 0; h < height; ++h) {
                for (int w = 0; w < width; ++w) {
                    int source = w + h * width;
                    int target = w + h * 3;
                    Ingredient i = (Ingredient)ingredients.get(source);
                    expandedIngredients.set(target, (Object)i);
                }
            }
        } else {
            for (int i = 0; i < ingredients.size(); ++i) {
                expandedIngredients.set(i, (Object)((Ingredient)ingredients.get(i)));
            }
        }
        return expandedIngredients;
    }

    public static NonNullList<Ingredient> getIngredients(Recipe<?> recipe) {
        if (recipe instanceof SmithingTrimRecipe) {
            SmithingTrimRecipe trimRecipe = (SmithingTrimRecipe)recipe;
            NonNullList ingredients = NonNullList.withSize((int)3, (Object)Ingredient.EMPTY);
            ingredients.set(0, (Object)trimRecipe.template);
            ingredients.set(1, (Object)trimRecipe.base);
            ingredients.set(2, (Object)trimRecipe.addition);
            return ingredients;
        }
        if (recipe instanceof SmithingTransformRecipe) {
            SmithingTransformRecipe transformRecipe = (SmithingTransformRecipe)recipe;
            NonNullList ingredients = NonNullList.withSize((int)3, (Object)Ingredient.EMPTY);
            ingredients.set(0, (Object)transformRecipe.template);
            ingredients.set(1, (Object)transformRecipe.base);
            ingredients.set(2, (Object)transformRecipe.addition);
            return ingredients;
        }
        return recipe.getIngredients();
    }
}

