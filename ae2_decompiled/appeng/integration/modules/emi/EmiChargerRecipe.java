/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  dev.emi.emi.api.recipe.BasicEmiRecipe
 *  dev.emi.emi.api.recipe.EmiRecipeCategory
 *  dev.emi.emi.api.render.EmiRenderable
 *  dev.emi.emi.api.render.EmiTexture
 *  dev.emi.emi.api.stack.EmiIngredient
 *  dev.emi.emi.api.stack.EmiStack
 *  dev.emi.emi.api.widget.WidgetHolder
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.crafting.Ingredient
 *  net.minecraft.world.item.crafting.RecipeHolder
 */
package appeng.integration.modules.emi;

import appeng.core.definitions.AEBlocks;
import appeng.core.localization.ItemModText;
import appeng.integration.modules.emi.AppEngRecipeCategory;
import appeng.integration.modules.emi.EmiText;
import appeng.recipes.handlers.ChargerRecipe;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiRenderable;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;

class EmiChargerRecipe
extends BasicEmiRecipe {
    public static final EmiRecipeCategory CATEGORY = new AppEngRecipeCategory("charger", (EmiRenderable)EmiStack.of(AEBlocks.CHARGER), EmiText.CATEGORY_CHARGER);
    private final ChargerRecipe recipe;
    private final EmiIngredient ingredient;
    private final EmiStack result;

    public EmiChargerRecipe(RecipeHolder<ChargerRecipe> holder) {
        super(CATEGORY, holder.id(), 130, 50);
        this.recipe = (ChargerRecipe)holder.value();
        this.ingredient = EmiIngredient.of((Ingredient)this.recipe.getIngredient());
        this.inputs.add(this.ingredient);
        this.result = EmiStack.of((ItemStack)this.recipe.getResultItem());
        this.outputs.add(this.result);
        this.catalysts.add(EmiStack.of(AEBlocks.CRANK));
    }

    public void addWidgets(WidgetHolder widgets) {
        widgets.addSlot(this.ingredient, 30, 7);
        widgets.addSlot((EmiIngredient)this.result, 80, 7);
        widgets.addSlot((EmiIngredient)EmiStack.of(AEBlocks.CRANK), 2, 29).drawBack(false);
        widgets.addTexture(EmiTexture.EMPTY_ARROW, 52, 8);
        int turns = 10;
        widgets.addText((Component)ItemModText.CHARGER_REQUIRED_POWER.text(turns, 1600), 20, 35, 0x7E7E7E, false);
    }
}

