/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.crafting.Ingredient
 *  net.minecraft.world.item.crafting.RecipeHolder
 *  net.minecraft.world.level.Level
 *  org.jetbrains.annotations.Nullable
 */
package appeng.blockentity.misc;

import appeng.api.ids.AEComponents;
import appeng.core.definitions.AEItems;
import appeng.recipes.AERecipeTypes;
import appeng.recipes.handlers.InscriberProcessType;
import appeng.recipes.handlers.InscriberRecipe;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public final class InscriberRecipes {
    private InscriberRecipes() {
    }

    public static Iterable<RecipeHolder<InscriberRecipe>> getRecipes(Level level) {
        return level.getRecipeManager().byType(AERecipeTypes.INSCRIBER);
    }

    @Nullable
    public static InscriberRecipe findRecipe(Level level, ItemStack input, ItemStack plateA, ItemStack plateB, boolean supportNamePress) {
        if (supportNamePress) {
            boolean isNameA = AEItems.NAME_PRESS.is(plateA);
            boolean isNameB = AEItems.NAME_PRESS.is(plateB);
            if (isNameA && isNameB || isNameA && plateB.isEmpty()) {
                return InscriberRecipes.makeNamePressRecipe(input, plateA, plateB);
            }
            if (plateA.isEmpty() && isNameB) {
                return InscriberRecipes.makeNamePressRecipe(input, plateB, plateA);
            }
        }
        for (RecipeHolder<InscriberRecipe> holder : InscriberRecipes.getRecipes(level)) {
            boolean matchB;
            InscriberRecipe recipe = (InscriberRecipe)holder.value();
            boolean matchA = recipe.getTopOptional().test(plateA) && recipe.getBottomOptional().test(plateB);
            boolean bl = matchB = recipe.getTopOptional().test(plateB) && recipe.getBottomOptional().test(plateA);
            if (!matchA && !matchB || !recipe.getMiddleInput().test(input)) continue;
            return recipe;
        }
        return null;
    }

    private static InscriberRecipe makeNamePressRecipe(ItemStack input, ItemStack plateA, ItemStack plateB) {
        Component plateBName;
        Component plateAName;
        Object name = null;
        if (!plateA.isEmpty() && (plateAName = (Component)plateA.get(AEComponents.NAME_PRESS_NAME)) != null) {
            name = plateAName;
        }
        if (!plateB.isEmpty() && (plateBName = (Component)plateB.get(AEComponents.NAME_PRESS_NAME)) != null) {
            name = name == null ? plateBName : name.copy().append(" ").append(plateBName);
        }
        Ingredient startingItem = Ingredient.of((ItemStack[])new ItemStack[]{input.copy()});
        ItemStack renamedItem = input.copyWithCount(1);
        if (name != null) {
            renamedItem.set(DataComponents.CUSTOM_NAME, name);
        } else {
            renamedItem.remove(DataComponents.CUSTOM_NAME);
        }
        InscriberProcessType type = InscriberProcessType.INSCRIBE;
        return new InscriberRecipe(startingItem, renamedItem, plateA.isEmpty() ? Ingredient.EMPTY : Ingredient.of((ItemStack[])new ItemStack[]{plateA}), plateB.isEmpty() ? Ingredient.EMPTY : Ingredient.of((ItemStack[])new ItemStack[]{plateB}), type);
    }

    public static boolean isValidOptionalIngredientCombination(Level level, ItemStack pressA, ItemStack pressB) {
        for (RecipeHolder<InscriberRecipe> holder : InscriberRecipes.getRecipes(level)) {
            InscriberRecipe recipe = (InscriberRecipe)holder.value();
            if ((!recipe.getTopOptional().test(pressA) || !recipe.getBottomOptional().test(pressB)) && (!recipe.getTopOptional().test(pressB) || !recipe.getBottomOptional().test(pressA))) continue;
            return true;
        }
        return false;
    }

    public static boolean isValidOptionalIngredient(Level level, ItemStack is) {
        for (RecipeHolder<InscriberRecipe> holder : InscriberRecipes.getRecipes(level)) {
            InscriberRecipe recipe = (InscriberRecipe)holder.value();
            if (!recipe.getTopOptional().test(is) && !recipe.getBottomOptional().test(is)) continue;
            return true;
        }
        return false;
    }
}

