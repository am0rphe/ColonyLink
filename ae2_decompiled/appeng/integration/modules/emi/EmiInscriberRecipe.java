/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  dev.emi.emi.api.recipe.BasicEmiRecipe
 *  dev.emi.emi.api.recipe.EmiRecipeCategory
 *  dev.emi.emi.api.render.EmiRenderable
 *  dev.emi.emi.api.stack.EmiIngredient
 *  dev.emi.emi.api.stack.EmiStack
 *  dev.emi.emi.api.widget.WidgetHolder
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.crafting.Ingredient
 *  net.minecraft.world.item.crafting.RecipeHolder
 */
package appeng.integration.modules.emi;

import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.integration.modules.emi.AppEngRecipeCategory;
import appeng.integration.modules.emi.EmiText;
import appeng.recipes.handlers.InscriberProcessType;
import appeng.recipes.handlers.InscriberRecipe;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiRenderable;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;

class EmiInscriberRecipe
extends BasicEmiRecipe {
    public static final EmiRecipeCategory CATEGORY = new AppEngRecipeCategory("inscriber", (EmiRenderable)EmiStack.of(AEBlocks.INSCRIBER), EmiText.CATEGORY_INSCRIBER);
    private final InscriberRecipe recipe;

    public EmiInscriberRecipe(RecipeHolder<InscriberRecipe> holder) {
        super(CATEGORY, holder.id(), 105, 54);
        this.recipe = (InscriberRecipe)holder.value();
        if (!this.recipe.getTopOptional().isEmpty()) {
            EmiIngredient top = EmiIngredient.of((Ingredient)this.recipe.getTopOptional());
            if (this.recipe.getProcessType() == InscriberProcessType.INSCRIBE) {
                top.getEmiStacks().forEach(s -> s.setRemainder(s));
            }
            this.inputs.add(top);
        }
        if (!this.recipe.getBottomOptional().isEmpty()) {
            EmiIngredient bottom = EmiIngredient.of((Ingredient)this.recipe.getBottomOptional());
            if (this.recipe.getProcessType() == InscriberProcessType.INSCRIBE) {
                bottom.getEmiStacks().forEach(s -> s.setRemainder(s));
            }
            this.inputs.add(bottom);
        }
        this.inputs.add(EmiIngredient.of((Ingredient)this.recipe.getMiddleInput()));
        this.outputs.add(EmiStack.of((ItemStack)this.recipe.getResultItem()));
    }

    public void addWidgets(WidgetHolder widgets) {
        ResourceLocation background = AppEng.makeId("textures/guis/inscriber.png");
        widgets.addTexture(background, 0, 0, 105, 54, 36, 20);
        widgets.addAnimatedTexture(background, 100, 19, 6, 18, 177, 0, 2000, false, true, false);
        widgets.addSlot(EmiIngredient.of((Ingredient)this.recipe.getTopOptional()), 2, 2).drawBack(false);
        widgets.addSlot(EmiIngredient.of((Ingredient)this.recipe.getMiddleInput()), 26, 18).drawBack(false);
        widgets.addSlot(EmiIngredient.of((Ingredient)this.recipe.getBottomOptional()), 2, 34).drawBack(false);
        widgets.addSlot((EmiIngredient)EmiStack.of((ItemStack)this.recipe.getResultItem()), 76, 19).drawBack(false);
    }
}

