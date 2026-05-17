/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  dev.emi.emi.api.recipe.EmiPlayerInventory
 *  dev.emi.emi.api.recipe.EmiRecipe
 *  dev.emi.emi.api.recipe.VanillaEmiRecipeCategories
 *  dev.emi.emi.api.recipe.handler.EmiCraftContext
 *  dev.emi.emi.api.recipe.handler.EmiCraftContext$Type
 *  dev.emi.emi.api.recipe.handler.StandardRecipeHandler
 *  dev.emi.emi.api.stack.EmiIngredient
 *  dev.emi.emi.api.stack.EmiStack
 *  dev.emi.emi.api.widget.Bounds
 *  dev.emi.emi.api.widget.SlotWidget
 *  dev.emi.emi.api.widget.Widget
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
 *  net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.inventory.Slot
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.crafting.Recipe
 *  net.minecraft.world.item.crafting.RecipeHolder
 *  net.minecraft.world.level.Level
 *  org.jetbrains.annotations.Nullable
 */
package appeng.integration.modules.emi;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.core.AEConfig;
import appeng.integration.modules.emi.EmiStackHelper;
import appeng.integration.modules.itemlists.EncodingHelper;
import appeng.integration.modules.itemlists.TransferHelper;
import appeng.menu.AEBaseMenu;
import appeng.menu.SlotSemantics;
import appeng.menu.me.common.GridInventoryEntry;
import appeng.menu.me.common.IClientRepo;
import appeng.menu.me.common.MEStorageMenu;
import appeng.menu.me.items.CraftingTermMenu;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.emi.emi.api.recipe.EmiPlayerInventory;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.Widget;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

abstract class AbstractRecipeHandler<T extends AEBaseMenu>
implements StandardRecipeHandler<T> {
    protected static final int CRAFTING_GRID_WIDTH = 3;
    protected static final int CRAFTING_GRID_HEIGHT = 3;
    private final Class<T> containerClass;

    AbstractRecipeHandler(Class<T> containerClass) {
        this.containerClass = containerClass;
    }

    public List<Slot> getInputSources(T menu) {
        ArrayList<Slot> slots = new ArrayList<Slot>();
        slots.addAll(((AEBaseMenu)((Object)menu)).getSlots(SlotSemantics.PLAYER_INVENTORY));
        slots.addAll(((AEBaseMenu)((Object)menu)).getSlots(SlotSemantics.PLAYER_HOTBAR));
        slots.addAll(((AEBaseMenu)((Object)menu)).getSlots(SlotSemantics.CRAFTING_GRID));
        return slots;
    }

    public List<Slot> getCraftingSlots(T menu) {
        return ((AEBaseMenu)((Object)menu)).getSlots(SlotSemantics.CRAFTING_GRID);
    }

    @Nullable
    public Slot getOutputSlot(T menu) {
        Iterator<Slot> iterator = ((AEBaseMenu)((Object)menu)).getSlots(SlotSemantics.CRAFTING_RESULT).iterator();
        if (iterator.hasNext()) {
            Slot slot = iterator.next();
            return slot;
        }
        return null;
    }

    public EmiPlayerInventory getInventory(AbstractContainerScreen<T> screen) {
        MEStorageMenu menu;
        IClientRepo iClientRepo;
        if (!AEConfig.instance().isExposeNetworkInventoryToEmi()) {
            return super.getInventory(screen);
        }
        ArrayList<EmiStack> list = new ArrayList<EmiStack>();
        for (Slot slot : this.getInputSources((T)((Object)((AEBaseMenu)screen.getMenu())))) {
            list.add(EmiStack.of((ItemStack)slot.getItem()));
        }
        AbstractContainerMenu abstractContainerMenu = screen.getMenu();
        if (abstractContainerMenu instanceof MEStorageMenu && (iClientRepo = (menu = (MEStorageMenu)abstractContainerMenu).getClientRepo()) != null) {
            for (GridInventoryEntry entry : iClientRepo.getAllEntries()) {
                EmiStack emiStack;
                if (entry.getStoredAmount() <= 0L || (emiStack = EmiStackHelper.toEmiStack(new GenericStack(entry.getWhat(), entry.getStoredAmount()))) == null) continue;
                list.add(emiStack);
            }
        }
        return new EmiPlayerInventory(list);
    }

    public boolean canCraft(EmiRecipe recipe, EmiCraftContext<T> context) {
        if (context.getType() == EmiCraftContext.Type.FILL_BUTTON) {
            return this.transferRecipe(recipe, context, false).canCraft();
        }
        return super.canCraft(recipe, context);
    }

    protected abstract Result transferRecipe(T var1, @Nullable RecipeHolder<?> var2, EmiRecipe var3, boolean var4);

    protected final Result transferRecipe(EmiRecipe emiRecipe, EmiCraftContext<T> context, boolean doTransfer) {
        RecipeHolder<?> holder;
        if (!this.containerClass.isInstance(context.getScreenHandler())) {
            return Result.createNotApplicable();
        }
        AEBaseMenu menu = (AEBaseMenu)((Object)this.containerClass.cast(context.getScreenHandler()));
        Result result = this.transferRecipe(menu, holder = this.getRecipeHolder(((AEBaseMenu)context.getScreenHandler()).getPlayer().level(), emiRecipe), emiRecipe, doTransfer);
        if (result instanceof Result.Success && doTransfer) {
            Minecraft.getInstance().setScreen((Screen)context.getScreen());
        }
        return result;
    }

    public boolean supportsRecipe(EmiRecipe recipe) {
        return true;
    }

    public boolean craft(EmiRecipe recipe, EmiCraftContext<T> context) {
        return this.transferRecipe(recipe, context, true).canCraft();
    }

    public List<ClientTooltipComponent> getTooltip(EmiRecipe recipe, EmiCraftContext<T> context) {
        List<Component> tooltip = this.transferRecipe(recipe, context, false).getTooltip(recipe, context);
        if (tooltip != null) {
            return tooltip.stream().map(Component::getVisualOrderText).map(ClientTooltipComponent::create).toList();
        }
        return super.getTooltip(recipe, context);
    }

    public void render(EmiRecipe recipe, EmiCraftContext<T> context, List<Widget> widgets, GuiGraphics draw) {
        this.transferRecipe(recipe, context, false).render(recipe, context, widgets, draw);
    }

    @Nullable
    private RecipeHolder<?> getRecipeHolder(Level level, EmiRecipe recipe) {
        if (recipe.getBackingRecipe() != null) {
            return recipe.getBackingRecipe();
        }
        if (recipe.getId() != null) {
            return level.getRecipeManager().byKey(recipe.getId()).orElse(null);
        }
        return null;
    }

    protected final boolean isCraftingRecipe(Recipe<?> recipe, EmiRecipe emiRecipe) {
        return EncodingHelper.isSupportedCraftingRecipe(recipe) || emiRecipe.getCategory().equals(VanillaEmiRecipeCategories.CRAFTING);
    }

    protected final boolean fitsIn3x3Grid(Recipe<?> recipe, EmiRecipe emiRecipe) {
        if (recipe != null) {
            return recipe.canCraftInDimensions(3, 3);
        }
        return true;
    }

    private static void renderMissingAndCraftableSlotOverlays(Map<Integer, SlotWidget> inputSlots, GuiGraphics guiGraphics, Set<Integer> missingSlots, Set<Integer> craftableSlots) {
        for (Map.Entry<Integer, SlotWidget> entry : inputSlots.entrySet()) {
            boolean missing = missingSlots.contains(entry.getKey());
            boolean craftable = craftableSlots.contains(entry.getKey());
            if (!missing && !craftable) continue;
            PoseStack poseStack = guiGraphics.pose();
            poseStack.pushPose();
            poseStack.translate(0.0f, 0.0f, 400.0f);
            Bounds innerBounds = AbstractRecipeHandler.getInnerBounds(entry.getValue());
            guiGraphics.fill(innerBounds.x(), innerBounds.y(), innerBounds.right(), innerBounds.bottom(), missing ? 0x66FF0000 : 0x400000FF);
            poseStack.popPose();
        }
    }

    private static boolean isInputSlot(SlotWidget slot) {
        return slot.getRecipe() == null;
    }

    private static Bounds getInnerBounds(SlotWidget slot) {
        Bounds bounds = slot.getBounds();
        return new Bounds(bounds.x() + 1, bounds.y() + 1, bounds.width() - 2, bounds.height() - 2);
    }

    private static Map<Integer, SlotWidget> getRecipeInputSlots(EmiRecipe recipe, List<Widget> widgets) {
        HashMap<Integer, SlotWidget> inputSlots = new HashMap<Integer, SlotWidget>(recipe.getInputs().size());
        for (int i = 0; i < recipe.getInputs().size(); ++i) {
            for (Widget widget : widgets) {
                SlotWidget slot;
                if (!(widget instanceof SlotWidget) || !AbstractRecipeHandler.isInputSlot(slot = (SlotWidget)widget) || slot.getStack() != recipe.getInputs().get(i)) continue;
                inputSlots.put(i, slot);
            }
        }
        return inputSlots;
    }

    protected static abstract sealed class Result {
        protected Result() {
        }

        @Nullable
        List<Component> getTooltip(EmiRecipe recipe, EmiCraftContext<?> context) {
            return null;
        }

        abstract boolean canCraft();

        void render(EmiRecipe recipe, EmiCraftContext<? extends AEBaseMenu> context, List<Widget> widgets, GuiGraphics draw) {
        }

        static NotApplicable createNotApplicable() {
            return new NotApplicable();
        }

        static Success createSuccessful() {
            return new Success();
        }

        static Error createFailed(Component text) {
            return new Error(text, Set.of());
        }

        static Error createFailed(Component text, Set<Integer> missingSlots) {
            return new Error(text, missingSlots);
        }

        static final class NotApplicable
        extends Result {
            NotApplicable() {
            }

            @Override
            boolean canCraft() {
                return false;
            }
        }

        static final class Success
        extends Result {
            Success() {
            }

            @Override
            boolean canCraft() {
                return true;
            }
        }

        static final class Error
        extends Result {
            private final Component message;
            private final Set<Integer> missingSlots;

            public Error(Component message, Set<Integer> missingSlots) {
                this.message = message;
                this.missingSlots = missingSlots;
            }

            public Component getMessage() {
                return this.message;
            }

            @Override
            boolean canCraft() {
                return false;
            }

            @Override
            void render(EmiRecipe recipe, EmiCraftContext<? extends AEBaseMenu> context, List<Widget> widgets, GuiGraphics guiGraphics) {
                AbstractRecipeHandler.renderMissingAndCraftableSlotOverlays(AbstractRecipeHandler.getRecipeInputSlots(recipe, widgets), guiGraphics, this.missingSlots, Set.of());
            }
        }

        static final class EncodeWithCraftables
        extends Result {
            private final Set<AEKey> craftableKeys;

            public EncodeWithCraftables(Set<AEKey> craftableKeys) {
                this.craftableKeys = craftableKeys;
            }

            @Override
            boolean canCraft() {
                return true;
            }

            @Override
            List<Component> getTooltip(EmiRecipe emiRecipe, EmiCraftContext<?> context) {
                boolean anyCraftable = emiRecipe.getInputs().stream().anyMatch(ing -> EncodeWithCraftables.isCraftable(this.craftableKeys, ing));
                if (anyCraftable) {
                    return TransferHelper.createEncodingTooltip(true, false);
                }
                return null;
            }

            @Override
            void render(EmiRecipe recipe, EmiCraftContext<? extends AEBaseMenu> context, List<Widget> widgets, GuiGraphics guiGraphics) {
                for (Widget widget : widgets) {
                    SlotWidget slot;
                    if (!(widget instanceof SlotWidget) || !AbstractRecipeHandler.isInputSlot(slot = (SlotWidget)widget) || !EncodeWithCraftables.isCraftable(this.craftableKeys, slot.getStack())) continue;
                    PoseStack poseStack = guiGraphics.pose();
                    poseStack.pushPose();
                    poseStack.translate(0.0f, 0.0f, 400.0f);
                    Bounds bounds = AbstractRecipeHandler.getInnerBounds(slot);
                    guiGraphics.fill(bounds.x(), bounds.y(), bounds.right(), bounds.bottom(), 0x400000FF);
                    poseStack.popPose();
                }
            }

            private static boolean isCraftable(Set<AEKey> craftableKeys, EmiIngredient ingredient) {
                return ingredient.getEmiStacks().stream().anyMatch(emiIngredient -> {
                    GenericStack stack = EmiStackHelper.toGenericStack(emiIngredient);
                    return stack != null && craftableKeys.contains(stack.what());
                });
            }
        }

        static final class PartiallyCraftable
        extends Result {
            private final CraftingTermMenu.MissingIngredientSlots missingSlots;

            public PartiallyCraftable(CraftingTermMenu.MissingIngredientSlots missingSlots) {
                this.missingSlots = missingSlots;
            }

            @Override
            boolean canCraft() {
                return true;
            }

            @Override
            List<Component> getTooltip(EmiRecipe recipe, EmiCraftContext<?> context) {
                return TransferHelper.createCraftingTooltip(this.missingSlots, false, false);
            }

            @Override
            void render(EmiRecipe recipe, EmiCraftContext<? extends AEBaseMenu> context, List<Widget> widgets, GuiGraphics guiGraphics) {
                AbstractRecipeHandler.renderMissingAndCraftableSlotOverlays(AbstractRecipeHandler.getRecipeInputSlots(recipe, widgets), guiGraphics, this.missingSlots.missingSlots(), this.missingSlots.craftableSlots());
            }
        }
    }
}

