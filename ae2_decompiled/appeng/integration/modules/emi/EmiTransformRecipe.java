/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  dev.emi.emi.api.recipe.BasicEmiRecipe
 *  dev.emi.emi.api.recipe.EmiRecipe
 *  dev.emi.emi.api.recipe.EmiRecipeCategory
 *  dev.emi.emi.api.render.EmiRenderable
 *  dev.emi.emi.api.render.EmiTexture
 *  dev.emi.emi.api.stack.EmiIngredient
 *  dev.emi.emi.api.stack.EmiStack
 *  dev.emi.emi.api.widget.SlotWidget
 *  dev.emi.emi.api.widget.TextWidget$Alignment
 *  dev.emi.emi.api.widget.TextureWidget
 *  dev.emi.emi.api.widget.Widget
 *  dev.emi.emi.api.widget.WidgetHolder
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.crafting.Ingredient
 *  net.minecraft.world.item.crafting.RecipeHolder
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.block.Blocks
 */
package appeng.integration.modules.emi;

import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.localization.ItemModText;
import appeng.integration.modules.emi.AppEngRecipeCategory;
import appeng.integration.modules.emi.EmiFluidBlockSlot;
import appeng.recipes.transform.TransformRecipe;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiRenderable;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.TextWidget;
import dev.emi.emi.api.widget.TextureWidget;
import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.api.widget.WidgetHolder;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;

class EmiTransformRecipe
extends BasicEmiRecipe {
    public static final EmiRecipeCategory CATEGORY = new AppEngRecipeCategory("item_transformation", (EmiRenderable)EmiStack.of(AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED), ItemModText.TRANSFORM_CATEGORY);
    private final TransformRecipe recipe;

    public EmiTransformRecipe(RecipeHolder<TransformRecipe> holder) {
        super(CATEGORY, holder.id(), 150, 72);
        this.recipe = (TransformRecipe)holder.value();
        for (Ingredient ingredient : this.recipe.getIngredients()) {
            this.inputs.add(EmiIngredient.of((Ingredient)ingredient));
        }
        this.outputs.add(EmiStack.of((ItemStack)this.recipe.getResultItem()));
    }

    public void addWidgets(WidgetHolder widgets) {
        int col1;
        int x = col1 = 10;
        int y = 10;
        int nInputs = this.recipe.getIngredients().size();
        if (nInputs < 3) {
            y += 9 * (3 - nInputs);
        }
        for (EmiIngredient input : this.inputs) {
            SlotWidget slot;
            if ((y += (slot = widgets.addSlot(input, x - 1, y - 1)).getBounds().height()) >= 64) {
                y -= 54;
                x += 18;
            }
            widgets.add((Widget)slot);
        }
        int yOffset = 28;
        int col2 = col1 + 25;
        TextureWidget arrow1 = widgets.addTexture(EmiTexture.EMPTY_ARROW, col2, yOffset);
        int col3 = col2 + arrow1.getBounds().width() + 6;
        if (this.recipe.circumstance.isFluid()) {
            ingredient = EmiIngredient.of(this.recipe.circumstance.getFluidsForRendering().stream().map(EmiStack::of).toList());
            widgets.add((Widget)new EmiFluidBlockSlot(ingredient, col3 - 1, yOffset - 1).drawBack(false));
        } else if (this.recipe.circumstance.isExplosion()) {
            ingredient = EmiIngredient.of(List.of(EmiStack.of(AEBlocks.TINY_TNT), EmiStack.of((ItemLike)Blocks.TNT)));
            widgets.addSlot(ingredient, col3 - 1, yOffset - 1).drawBack(false);
        }
        int col4 = col3 + 16 + 5;
        TextureWidget arrow2 = widgets.addTexture(EmiTexture.EMPTY_ARROW, col4, yOffset);
        int col5 = arrow2.getBounds().right() + 10;
        widgets.addSlot((EmiIngredient)EmiStack.of((ItemStack)this.recipe.getResultItem()), col5 - 1, yOffset - 1).recipeContext((EmiRecipe)this);
        MutableComponent circumstanceText = this.recipe.circumstance.isExplosion() ? ItemModText.EXPLOSION.text() : ItemModText.SUBMERGE_IN.text();
        widgets.addText((Component)circumstanceText, this.width / 2, 15, 0x7E7E7E, false).horizontalAlign(TextWidget.Alignment.CENTER);
    }
}

