/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  me.shedaniel.math.Rectangle
 *  me.shedaniel.rei.api.client.gui.widgets.Slot
 *  me.shedaniel.rei.api.client.gui.widgets.Tooltip
 *  me.shedaniel.rei.api.client.gui.widgets.Widget
 *  me.shedaniel.rei.api.client.registry.transfer.TransferHandler$Result
 *  me.shedaniel.rei.api.client.registry.transfer.TransferHandlerRenderer
 *  me.shedaniel.rei.api.common.display.Display
 *  me.shedaniel.rei.api.common.entry.EntryIngredient
 *  me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes
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
package appeng.integration.modules.rei.transfer;

import appeng.core.localization.ItemModText;
import appeng.integration.modules.itemlists.CraftingHelper;
import appeng.integration.modules.itemlists.TransferHelper;
import appeng.integration.modules.rei.transfer.AbstractTransferHandler;
import appeng.menu.me.items.CraftingTermMenu;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.Slot;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandler;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandlerRenderer;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
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

public class UseCraftingRecipeTransfer<T extends CraftingTermMenu>
extends AbstractTransferHandler<T> {
    public UseCraftingRecipeTransfer(Class<T> containerClass) {
        super(containerClass);
    }

    @Override
    protected TransferHandler.Result transferRecipe(T menu, RecipeHolder<?> holder, Display display, boolean doTransfer) {
        ResourceLocation recipeId = holder != null ? holder.id() : null;
        Recipe<?> recipe = holder != null ? holder.value() : null;
        boolean craftingRecipe = this.isCraftingRecipe(recipe, display);
        if (!craftingRecipe) {
            return TransferHandler.Result.createNotApplicable();
        }
        if (!this.fitsIn3x3Grid(recipe, display)) {
            return TransferHandler.Result.createFailed((Component)ItemModText.RECIPE_TOO_LARGE.text());
        }
        if (recipe == null) {
            recipe = this.createFakeRecipe(display);
        }
        boolean craftMissing = AbstractContainerScreen.hasControlDown();
        Map<Integer, Ingredient> slotToIngredientMap = UseCraftingRecipeTransfer.getGuiSlotToIngredientMap(recipe);
        CraftingTermMenu.MissingIngredientSlots missingSlots = ((CraftingTermMenu)menu).findMissingIngredients(UseCraftingRecipeTransfer.getGuiSlotToIngredientMap(recipe));
        if (missingSlots.missingSlots().size() == slotToIngredientMap.size()) {
            return TransferHandler.Result.createFailed((Component)ItemModText.NO_ITEMS.text()).renderer(UseCraftingRecipeTransfer.createErrorRenderer(missingSlots));
        }
        if (!doTransfer) {
            if (missingSlots.anyMissingOrCraftable()) {
                int color = missingSlots.anyMissing() ? -2130729728 : -2142943745;
                TransferHandler.Result result = TransferHandler.Result.createSuccessful().color(color).renderer(UseCraftingRecipeTransfer.createErrorRenderer(missingSlots));
                List<Component> tooltip = TransferHelper.createCraftingTooltip(missingSlots, craftMissing, true);
                result.overrideTooltipRenderer((point, sink) -> sink.accept(Tooltip.create((Collection)tooltip)));
                return result;
            }
        } else {
            CraftingHelper.performTransfer(menu, recipeId, recipe, craftMissing);
        }
        return TransferHandler.Result.createSuccessful().blocksFurtherHandling();
    }

    private Recipe<?> createFakeRecipe(Display display) {
        NonNullList ingredients = NonNullList.withSize((int)9, (Object)Ingredient.EMPTY);
        for (int i = 0; i < Math.min(display.getInputEntries().size(), ingredients.size()); ++i) {
            Ingredient ingredient = Ingredient.of(((EntryIngredient)display.getInputEntries().get(i)).stream().filter(es -> es.getType() == VanillaEntryTypes.ITEM).map(es -> (ItemStack)es.castValue()));
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

    private static TransferHandlerRenderer createErrorRenderer(CraftingTermMenu.MissingIngredientSlots indices) {
        return (guiGraphics, mouseX, mouseY, delta, widgets, bounds, display) -> {
            int i = 0;
            for (Widget widget : widgets) {
                Slot slot;
                if (!(widget instanceof Slot) || (slot = (Slot)widget).getNoticeMark() != 1) continue;
                boolean missing = indices.missingSlots().contains(i);
                boolean craftable = indices.craftableSlots().contains(i);
                ++i;
                if (!missing && !craftable) continue;
                PoseStack poseStack = guiGraphics.pose();
                poseStack.pushPose();
                poseStack.translate(0.0f, 0.0f, 400.0f);
                Rectangle innerBounds = slot.getInnerBounds();
                guiGraphics.fill(innerBounds.x, innerBounds.y, innerBounds.getMaxX(), innerBounds.getMaxY(), missing ? 0x66FF0000 : 0x400000FF);
                poseStack.popPose();
            }
        };
    }
}

