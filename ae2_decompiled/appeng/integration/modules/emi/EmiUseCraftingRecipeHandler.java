/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  dev.emi.emi.api.recipe.EmiRecipe
 *  dev.emi.emi.api.recipe.VanillaEmiRecipeCategories
 *  dev.emi.emi.api.stack.EmiIngredient
 *  dev.emi.emi.api.stack.EmiStack
 *  net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
 *  net.minecraft.core.NonNullList
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.crafting.CraftingBookCategory
 *  net.minecraft.world.item.crafting.Ingredient
 *  net.minecraft.world.item.crafting.Recipe
 *  net.minecraft.world.item.crafting.RecipeHolder
 *  net.minecraft.world.item.crafting.ShapedRecipe
 *  net.minecraft.world.item.crafting.ShapedRecipePattern
 */
package appeng.integration.modules.emi;

import appeng.core.localization.ItemModText;
import appeng.integration.modules.emi.AbstractRecipeHandler;
import appeng.integration.modules.itemlists.CraftingHelper;
import appeng.menu.me.items.CraftingTermMenu;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;

public class EmiUseCraftingRecipeHandler<T extends CraftingTermMenu>
extends AbstractRecipeHandler<T> {
    public EmiUseCraftingRecipeHandler(Class<T> containerClass) {
        super(containerClass);
    }

    @Override
    public boolean supportsRecipe(EmiRecipe recipe) {
        return recipe.getCategory().equals(VanillaEmiRecipeCategories.CRAFTING);
    }

    @Override
    protected AbstractRecipeHandler.Result transferRecipe(T menu, RecipeHolder<?> holder, EmiRecipe emiRecipe, boolean doTransfer) {
        Map<Integer, Ingredient> slotToIngredientMap;
        CraftingTermMenu.MissingIngredientSlots missingSlots;
        ResourceLocation recipeId = holder != null ? holder.id() : null;
        Recipe<?> recipe = holder != null ? holder.value() : null;
        boolean craftingRecipe = this.isCraftingRecipe(recipe, emiRecipe);
        if (!craftingRecipe) {
            return AbstractRecipeHandler.Result.createNotApplicable();
        }
        if (!this.fitsIn3x3Grid(recipe, emiRecipe)) {
            return AbstractRecipeHandler.Result.createFailed((Component)ItemModText.RECIPE_TOO_LARGE.text());
        }
        if (recipe == null) {
            recipe = this.createFakeRecipe(emiRecipe);
        }
        if ((missingSlots = ((CraftingTermMenu)menu).findMissingIngredients(slotToIngredientMap = EmiUseCraftingRecipeHandler.getGuiSlotToIngredientMap(recipe))).missingSlots().size() == slotToIngredientMap.size()) {
            return AbstractRecipeHandler.Result.createFailed((Component)ItemModText.NO_ITEMS.text(), missingSlots.missingSlots());
        }
        if (!doTransfer) {
            if (missingSlots.anyMissingOrCraftable()) {
                return new AbstractRecipeHandler.Result.PartiallyCraftable(missingSlots);
            }
        } else {
            boolean craftMissing = AbstractContainerScreen.hasControlDown();
            CraftingHelper.performTransfer(menu, recipeId, recipe, craftMissing);
        }
        return AbstractRecipeHandler.Result.createSuccessful();
    }

    private Recipe<?> createFakeRecipe(EmiRecipe display) {
        NonNullList ingredients = NonNullList.withSize((int)9, (Object)Ingredient.EMPTY);
        for (int i = 0; i < Math.min(display.getInputs().size(), ingredients.size()); ++i) {
            Ingredient ingredient = Ingredient.of(((EmiIngredient)display.getInputs().get(i)).getEmiStacks().stream().map(EmiStack::getItemStack).filter(is -> !is.isEmpty()));
            ingredients.set(i, (Object)ingredient);
        }
        ShapedRecipePattern pattern = new ShapedRecipePattern(3, 3, ingredients, Optional.empty());
        return new ShapedRecipe("", CraftingBookCategory.MISC, pattern, ItemStack.EMPTY);
    }

    public static Map<Integer, Ingredient> getGuiSlotToIngredientMap(Recipe<?> recipe) {
        int width;
        NonNullList ingredients = recipe.getIngredients();
        if (recipe instanceof ShapedRecipe) {
            ShapedRecipe shapedRecipe = (ShapedRecipe)recipe;
            width = shapedRecipe.getWidth();
        } else {
            width = 3;
        }
        HashMap<Integer, Ingredient> result = new HashMap<Integer, Ingredient>(ingredients.size());
        for (int i = 0; i < ingredients.size(); ++i) {
            int guiSlot = i / width * 3 + i % width;
            Ingredient ingredient = (Ingredient)ingredients.get(i);
            if (ingredient.isEmpty()) continue;
            result.put(guiSlot, ingredient);
        }
        return result;
    }
}

