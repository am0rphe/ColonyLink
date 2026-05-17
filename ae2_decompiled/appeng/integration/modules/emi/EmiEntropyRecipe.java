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
 *  dev.emi.emi.api.widget.TextWidget
 *  dev.emi.emi.api.widget.TextWidget$Alignment
 *  dev.emi.emi.api.widget.Widget
 *  dev.emi.emi.api.widget.WidgetHolder
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.crafting.RecipeHolder
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.material.FlowingFluid
 *  net.minecraft.world.level.material.Fluid
 *  net.minecraft.world.level.material.Fluids
 */
package appeng.integration.modules.emi;

import appeng.core.AELog;
import appeng.core.AppEng;
import appeng.core.localization.ItemModText;
import appeng.integration.modules.emi.AppEngEmiPlugin;
import appeng.integration.modules.emi.AppEngRecipeCategory;
import appeng.integration.modules.emi.EmiEntropySlot;
import appeng.integration.modules.emi.EmiText;
import appeng.recipes.entropy.EntropyMode;
import appeng.recipes.entropy.EntropyRecipe;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiRenderable;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.TextWidget;
import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.api.widget.WidgetHolder;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public class EmiEntropyRecipe
extends BasicEmiRecipe {
    public static final EmiRecipeCategory CATEGORY = new AppEngRecipeCategory("entropy", EmiEntropyRecipe.createIcon(), EmiText.CATEGORY_ENTROPY_MANIPULATOR);
    private static final int BODY_TEXT_COLOR = 0x7E7E7E;
    private final EntropyRecipe recipe;
    private final EmiStack inputBlockIngredient;
    private final boolean inputFluidFlowing;
    private final EmiStack outputBlockIngredient;
    private final boolean outputFluidFlowing;
    private final List<EmiStack> additionalDrops;
    private final boolean inputConsumed;

    public EmiEntropyRecipe(RecipeHolder<EntropyRecipe> holder) {
        super(CATEGORY, holder.id(), 130, 50);
        this.recipe = (EntropyRecipe)holder.value();
        EntropyRecipe.Input input = this.recipe.getInput();
        this.inputBlockIngredient = EmiEntropyRecipe.createIngredient(input.block().map(EntropyRecipe.BlockInput::block).orElse(null), input.fluid().map(EntropyRecipe.FluidInput::fluid).orElse(null));
        this.inputFluidFlowing = input.fluid().map(EntropyRecipe.FluidInput::fluid).map(this::isFlowing).orElse(false);
        this.inputs.add(this.inputBlockIngredient);
        EntropyRecipe.Output output = this.recipe.getOutput();
        this.outputBlockIngredient = EmiEntropyRecipe.createIngredient(output.block().map(EntropyRecipe.BlockOutput::block).orElse(null), output.fluid().map(EntropyRecipe.FluidOutput::fluid).orElse(null));
        this.outputFluidFlowing = output.fluid().map(EntropyRecipe.FluidOutput::fluid).map(this::isFlowing).orElse(false);
        if (!this.outputBlockIngredient.isEmpty()) {
            this.outputs.add(this.outputBlockIngredient);
        }
        boolean bl = this.inputConsumed = output.block().isPresent() && output.block().get().block().defaultBlockState().isAir() && (output.fluid().isEmpty() || output.fluid().get().fluid() == Fluids.EMPTY);
        if (!this.inputConsumed) {
            this.inputBlockIngredient.setRemainder(this.inputBlockIngredient);
        }
        this.additionalDrops = this.recipe.getDrops().stream().map(EmiStack::of).toList();
        this.outputs.addAll(this.additionalDrops);
    }

    private boolean isFlowing(Fluid fluid) {
        return fluid != Fluids.EMPTY && !fluid.isSource(fluid.defaultFluidState());
    }

    public void addWidgets(WidgetHolder widgets) {
        int centerX = this.width / 2;
        MutableComponent labelText = switch (this.recipe.getMode()) {
            default -> throw new MatchException(null, null);
            case EntropyMode.HEAT -> ItemModText.ENTROPY_MANIPULATOR_HEAT.text(1600);
            case EntropyMode.COOL -> ItemModText.ENTROPY_MANIPULATOR_COOL.text(1600);
        };
        MutableComponent interaction = switch (this.recipe.getMode()) {
            default -> throw new MatchException(null, null);
            case EntropyMode.HEAT -> ItemModText.RIGHT_CLICK.text();
            case EntropyMode.COOL -> ItemModText.SHIFT_RIGHT_CLICK.text();
        };
        TextWidget modeLabel = widgets.addText((Component)labelText, centerX + 4, 2, 0x7E7E7E, false).horizontalAlign(TextWidget.Alignment.CENTER);
        int modeLabelX = modeLabel.getBounds().x();
        switch (this.recipe.getMode()) {
            case HEAT: {
                widgets.addTexture(AppEngEmiPlugin.TEXTURE, modeLabelX - 9, 3, 6, 6, 0, 68);
                break;
            }
            case COOL: {
                widgets.addTexture(AppEngEmiPlugin.TEXTURE, modeLabelX - 9, 3, 6, 6, 6, 68);
            }
        }
        widgets.addTexture(EmiTexture.EMPTY_ARROW, centerX - 12, 14);
        widgets.addText((Component)interaction, centerX, 38, 0x7E7E7E, false).horizontalAlign(TextWidget.Alignment.CENTER);
        widgets.add((Widget)new EmiEntropySlot(this.inputBlockIngredient, false, this.inputFluidFlowing, this.width / 2 - 35, 14));
        int x = centerX + 20;
        if (this.inputConsumed) {
            widgets.add((Widget)new EmiEntropySlot(this.inputBlockIngredient, true, this.outputFluidFlowing, x - 1, 14));
            x += 18;
        } else if (!this.outputBlockIngredient.isEmpty()) {
            widgets.add((Widget)new EmiEntropySlot(this.outputBlockIngredient, false, this.outputFluidFlowing, x - 1, 14).recipeContext((EmiRecipe)this));
            x += 18;
        }
        for (EmiStack drop : this.additionalDrops) {
            widgets.addSlot((EmiIngredient)drop, x - 1, 14).recipeContext((EmiRecipe)this);
            x += 18;
        }
    }

    private static EmiRenderable createIcon() {
        return new EmiTexture(AppEng.makeId("textures/item/entropy_manipulator.png"), 0, 0, 16, 16, 16, 16, 16, 16);
    }

    private static EmiStack createIngredient(Block block, Fluid fluid) {
        if (fluid != null) {
            if (!fluid.isSource(fluid.defaultFluidState())) {
                if (fluid instanceof FlowingFluid) {
                    FlowingFluid flowingFluid = (FlowingFluid)fluid;
                    return EmiStack.of((Fluid)flowingFluid.getSource());
                }
                AELog.warn("Don't know how to get the source fluid for %s", fluid);
                return EmiStack.of((Fluid)fluid);
            }
            return EmiStack.of((Fluid)fluid);
        }
        if (block != null) {
            return EmiStack.of((ItemStack)block.asItem().getDefaultInstance());
        }
        return EmiStack.EMPTY;
    }
}

