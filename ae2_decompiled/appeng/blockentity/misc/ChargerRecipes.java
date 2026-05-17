/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.crafting.RecipeHolder
 *  net.minecraft.world.level.Level
 *  org.jetbrains.annotations.Nullable
 */
package appeng.blockentity.misc;

import appeng.recipes.AERecipeTypes;
import appeng.recipes.handlers.ChargerRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class ChargerRecipes {
    public static Iterable<RecipeHolder<ChargerRecipe>> getRecipes(Level level) {
        return level.getRecipeManager().byType(AERecipeTypes.CHARGER);
    }

    @Nullable
    public static ChargerRecipe findRecipe(Level level, ItemStack input) {
        for (RecipeHolder<ChargerRecipe> recipe : ChargerRecipes.getRecipes(level)) {
            if (!((ChargerRecipe)recipe.value()).ingredient.test(input)) continue;
            return (ChargerRecipe)recipe.value();
        }
        return null;
    }

    public static boolean allowInsert(Level level, ItemStack stack) {
        return ChargerRecipes.findRecipe(level, stack) != null;
    }

    public static boolean allowExtract(Level level, ItemStack stack) {
        return ChargerRecipes.findRecipe(level, stack) == null;
    }
}

