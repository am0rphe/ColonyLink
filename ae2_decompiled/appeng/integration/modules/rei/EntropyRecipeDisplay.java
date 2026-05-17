/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  me.shedaniel.math.Rectangle
 *  me.shedaniel.rei.api.client.entry.renderer.EntryRenderer
 *  me.shedaniel.rei.api.client.gui.widgets.Tooltip
 *  me.shedaniel.rei.api.client.gui.widgets.Tooltip$Entry
 *  me.shedaniel.rei.api.client.gui.widgets.TooltipContext
 *  me.shedaniel.rei.api.common.category.CategoryIdentifier
 *  me.shedaniel.rei.api.common.display.Display
 *  me.shedaniel.rei.api.common.entry.EntryIngredient
 *  me.shedaniel.rei.api.common.entry.EntryStack
 *  me.shedaniel.rei.api.common.util.EntryIngredients
 *  me.shedaniel.rei.api.common.util.EntryStacks
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.crafting.RecipeHolder
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.material.FlowingFluid
 *  net.minecraft.world.level.material.Fluid
 *  net.minecraft.world.level.material.Fluids
 *  org.jetbrains.annotations.Nullable
 */
package appeng.integration.modules.rei;

import appeng.core.AELog;
import appeng.core.localization.ItemModText;
import appeng.integration.modules.rei.EntropyRecipeCategory;
import appeng.integration.modules.rei.ReiPlugin;
import appeng.recipes.entropy.EntropyRecipe;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.entry.renderer.EntryRenderer;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.TooltipContext;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

public class EntropyRecipeDisplay
implements Display {
    private final RecipeHolder<EntropyRecipe> holder;
    private final EntryIngredient input;
    private final List<EntryIngredient> inputs;
    private final List<EntryIngredient> consumed;
    private final List<EntryIngredient> outputs;

    public EntropyRecipeDisplay(RecipeHolder<EntropyRecipe> holder) {
        boolean inputConsumed;
        this.holder = holder;
        EntropyRecipe recipe = (EntropyRecipe)holder.value();
        EntropyRecipe.Input input = recipe.getInput();
        this.input = EntryIngredient.of(EntropyRecipeDisplay.createIngredient(input.block().map(EntropyRecipe.BlockInput::block).orElse(null), input.fluid().map(EntropyRecipe.FluidInput::fluid).orElse(null)));
        this.inputs = List.of(this.input);
        EntropyRecipe.Output output = recipe.getOutput();
        ArrayList<EntryIngredient> outputs = new ArrayList<EntryIngredient>();
        boolean bl = inputConsumed = output.block().isPresent() && output.block().get().block().defaultBlockState().isAir() && (output.fluid().isEmpty() || output.fluid().get().fluid() == Fluids.EMPTY);
        if (inputConsumed) {
            this.consumed = List.of(this.input.map(EntropyRecipeDisplay::makeConsumed));
        } else {
            this.consumed = List.of();
            if (output.block().isPresent() || output.fluid().isPresent()) {
                outputs.add(EntryIngredient.of(EntropyRecipeDisplay.createIngredient(output.block().map(EntropyRecipe.BlockOutput::block).orElse(null), output.fluid().map(EntropyRecipe.FluidOutput::fluid).orElse(null))));
            }
            recipe.getDrops().stream().map(EntryIngredients::of).forEach(outputs::add);
        }
        this.outputs = List.copyOf(outputs);
    }

    private static <T> EntryStack<T> makeConsumed(EntryStack<T> entryStack) {
        entryStack = entryStack.copy();
        entryStack.tooltip(new Component[]{ItemModText.CONSUMED.text().withStyle(new ChatFormatting[]{ChatFormatting.RED, ChatFormatting.BOLD})});
        entryStack.withRenderer(new EntryRenderer<T>(){

            public void render(EntryStack<T> entry, GuiGraphics graphics, Rectangle bounds, int mouseX, int mouseY, float delta) {
                EntryRenderer baseRenderer = entry.getDefinition().getRenderer();
                baseRenderer.render(entry, graphics, bounds, mouseX, mouseY, delta);
                graphics.blit(ReiPlugin.TEXTURE, bounds.x, bounds.y, 0, 52, 16, 16);
            }

            @Nullable
            public Tooltip getTooltip(EntryStack<T> entry, TooltipContext context) {
                EntryRenderer baseRenderer = entry.getDefinition().getRenderer();
                return baseRenderer.getTooltip(entry, context);
            }
        });
        return entryStack;
    }

    public RecipeHolder<EntropyRecipe> getHolder() {
        return this.holder;
    }

    public EntropyRecipe getRecipe() {
        return (EntropyRecipe)this.holder.value();
    }

    public EntryIngredient getInput() {
        return this.input;
    }

    public List<EntryIngredient> getConsumed() {
        return this.consumed;
    }

    public List<EntryIngredient> getInputEntries() {
        return this.inputs;
    }

    public List<EntryIngredient> getOutputEntries() {
        return this.outputs;
    }

    public CategoryIdentifier<?> getCategoryIdentifier() {
        return EntropyRecipeCategory.ID;
    }

    public Optional<ResourceLocation> getDisplayLocation() {
        return Optional.of(this.holder.id());
    }

    private static EntryStack<?> createIngredient(Block block, Fluid fluid) {
        if (fluid != null) {
            if (!fluid.isSource(fluid.defaultFluidState())) {
                EntryStack entryStack;
                if (fluid instanceof FlowingFluid) {
                    FlowingFluid flowingFluid = (FlowingFluid)fluid;
                    entryStack = EntryStacks.of((Fluid)flowingFluid.getSource());
                } else {
                    AELog.warn("Don't know how to get the source fluid for %s", fluid);
                    entryStack = EntryStacks.of((Fluid)fluid);
                }
                entryStack.tooltipProcessor(EntropyRecipeDisplay::addFlowingToTooltip);
                return entryStack;
            }
            return EntryStacks.of((Fluid)fluid);
        }
        if (block != null) {
            return EntryStacks.of((ItemStack)block.asItem().getDefaultInstance());
        }
        return EntryStack.empty();
    }

    private static Tooltip addFlowingToTooltip(EntryStack<?> entryStack, Tooltip tooltip) {
        Tooltip newTooltip = Tooltip.from((Tooltip.Entry[])new Tooltip.Entry[0]).withContextStack(tooltip.getContextStack());
        boolean appended = false;
        for (Tooltip.Entry entry : tooltip.entries()) {
            if (!appended && entry.isText()) {
                appended = true;
                newTooltip.add((Component)ItemModText.FLOWING_FLUID_NAME.text(entry.getAsText()));
                continue;
            }
            if (entry.isText()) {
                newTooltip.add(entry.getAsText());
                continue;
            }
            newTooltip.add(entry.getAsTooltipComponent());
        }
        return newTooltip;
    }
}

