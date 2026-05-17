/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  me.shedaniel.math.Rectangle
 *  me.shedaniel.rei.api.client.gui.widgets.Slot
 *  me.shedaniel.rei.api.client.gui.widgets.Tooltip
 *  me.shedaniel.rei.api.client.gui.widgets.Widget
 *  me.shedaniel.rei.api.client.registry.entry.EntryRegistry
 *  me.shedaniel.rei.api.client.registry.transfer.TransferHandler$Result
 *  me.shedaniel.rei.api.client.registry.transfer.TransferHandlerRenderer
 *  me.shedaniel.rei.api.common.display.Display
 *  me.shedaniel.rei.api.common.entry.EntryIngredient
 *  me.shedaniel.rei.api.common.entry.EntryStack
 *  me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes
 *  me.shedaniel.rei.api.common.util.EntryStacks
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.crafting.Recipe
 *  net.minecraft.world.item.crafting.RecipeHolder
 */
package appeng.integration.modules.rei.transfer;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.core.localization.ItemModText;
import appeng.integration.modules.itemlists.EncodingHelper;
import appeng.integration.modules.itemlists.TransferHelper;
import appeng.integration.modules.rei.GenericEntryStackHelper;
import appeng.integration.modules.rei.transfer.AbstractTransferHandler;
import appeng.menu.me.common.GridInventoryEntry;
import appeng.menu.me.common.IClientRepo;
import appeng.menu.me.common.MEStorageMenu;
import appeng.menu.me.items.PatternEncodingTermMenu;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.Slot;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandler;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandlerRenderer;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

public class EncodePatternTransferHandler<T extends PatternEncodingTermMenu>
extends AbstractTransferHandler<T> {
    private final IngredientVisibility ingredientVisibility = new IngredientVisibility();

    public EncodePatternTransferHandler(Class<T> containerClass) {
        super(containerClass);
    }

    @Override
    protected TransferHandler.Result transferRecipe(T menu, RecipeHolder<?> holder, Display display, boolean doTransfer) {
        ResourceLocation recipeId = holder != null ? holder.id() : null;
        Recipe recipe = holder != null ? holder.value() : null;
        boolean craftingRecipe = this.isCraftingRecipe(recipe, display);
        if (craftingRecipe && !this.fitsIn3x3Grid(recipe, display)) {
            return TransferHandler.Result.createFailed((Component)ItemModText.RECIPE_TOO_LARGE.text());
        }
        if (doTransfer) {
            if (craftingRecipe && recipeId != null) {
                EncodingHelper.encodeCraftingRecipe(menu, new RecipeHolder(recipeId, recipe), this.getGuiIngredientsForCrafting(display), this::isIngredientVisible);
            } else {
                EncodingHelper.encodeProcessingRecipe(menu, GenericEntryStackHelper.ofInputs(display), GenericEntryStackHelper.ofOutputs(display));
            }
        } else {
            IClientRepo repo = ((MEStorageMenu)menu).getClientRepo();
            Set<AEKey> craftableKeys = repo != null ? repo.getAllEntries().stream().filter(GridInventoryEntry::isCraftable).map(GridInventoryEntry::getWhat).collect(Collectors.toSet()) : Set.of();
            boolean anyCraftable = display.getInputEntries().stream().anyMatch(ing -> EncodePatternTransferHandler.isCraftable(craftableKeys, ing));
            List<Component> tooltip = TransferHelper.createEncodingTooltip(anyCraftable, true);
            return TransferHandler.Result.createSuccessful().blocksFurtherHandling().overrideTooltipRenderer((point, sink) -> sink.accept(Tooltip.create((Collection)tooltip))).renderer(EncodePatternTransferHandler.createErrorRenderer(craftableKeys));
        }
        return TransferHandler.Result.createSuccessful().blocksFurtherHandling();
    }

    private boolean isIngredientVisible(ItemStack itemStack) {
        return this.ingredientVisibility.isVisible(itemStack);
    }

    private List<List<GenericStack>> getGuiIngredientsForCrafting(Display recipeLayout) {
        ArrayList<List<GenericStack>> result = new ArrayList<List<GenericStack>>(9);
        for (int i = 0; i < 9; ++i) {
            ArrayList<GenericStack> stacks = new ArrayList<GenericStack>();
            if (i < recipeLayout.getInputEntries().size()) {
                for (EntryStack entryStack : (EntryIngredient)recipeLayout.getInputEntries().get(i)) {
                    if (entryStack.getType() != VanillaEntryTypes.ITEM) continue;
                    stacks.add(GenericStack.fromItemStack((ItemStack)entryStack.castValue()));
                }
            }
            result.add(stacks);
        }
        return result;
    }

    private static boolean isCraftable(Set<AEKey> craftableKeys, List<EntryStack<?>> ingredient) {
        return ingredient.stream().anyMatch(entryStack -> {
            GenericStack stack = GenericEntryStackHelper.ingredientToStack(entryStack);
            return stack != null && craftableKeys.contains(stack.what());
        });
    }

    private static TransferHandlerRenderer createErrorRenderer(Set<AEKey> craftableKeys) {
        return (guiGraphics, mouseX, mouseY, delta, widgets, bounds, display) -> {
            for (Widget widget : widgets) {
                Slot slot;
                if (!(widget instanceof Slot) || (slot = (Slot)widget).getNoticeMark() != 1 || !EncodePatternTransferHandler.isCraftable(craftableKeys, slot.getEntries())) continue;
                PoseStack poseStack = guiGraphics.pose();
                poseStack.pushPose();
                poseStack.translate(0.0f, 0.0f, 400.0f);
                Rectangle innerBounds = slot.getInnerBounds();
                guiGraphics.fill(innerBounds.x, innerBounds.y, innerBounds.getMaxX(), innerBounds.getMaxY(), 0x400000FF);
                poseStack.popPose();
            }
        };
    }

    private static class IngredientVisibility {
        private final EntryRegistry registry;
        private final Map<ItemStack, Boolean> cache = new HashMap<ItemStack, Boolean>();

        private IngredientVisibility() {
            this.registry = EntryRegistry.getInstance();
        }

        private boolean isVisible(ItemStack stack) {
            if (this.cache.containsKey(stack)) {
                return this.cache.get(stack);
            }
            EntryStack entryStack = EntryStacks.of((ItemStack)stack);
            if (!this.registry.alreadyContain(entryStack)) {
                this.cache.put(stack, false);
                return false;
            }
            Collection entryStacks = this.registry.refilterNew(false, Collections.singleton(entryStack));
            boolean visible = !entryStacks.isEmpty();
            this.cache.put(stack, visible);
            return visible;
        }
    }
}

