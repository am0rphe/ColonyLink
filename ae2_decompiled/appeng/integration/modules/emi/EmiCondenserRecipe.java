/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Splitter
 *  dev.emi.emi.api.recipe.BasicEmiRecipe
 *  dev.emi.emi.api.recipe.EmiRecipeCategory
 *  dev.emi.emi.api.render.EmiRenderable
 *  dev.emi.emi.api.stack.EmiIngredient
 *  dev.emi.emi.api.stack.EmiStack
 *  dev.emi.emi.api.widget.WidgetHolder
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 */
package appeng.integration.modules.emi;

import appeng.api.config.CondenserOutput;
import appeng.api.implementations.items.IStorageComponent;
import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.localization.ButtonToolTips;
import appeng.integration.modules.emi.AppEngRecipeCategory;
import appeng.integration.modules.emi.EmiText;
import com.google.common.base.Splitter;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiRenderable;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

class EmiCondenserRecipe
extends BasicEmiRecipe {
    public static final EmiRecipeCategory CATEGORY = new AppEngRecipeCategory("condenser", (EmiRenderable)EmiStack.of(AEBlocks.CONDENSER), EmiText.CATEGORY_CONDENSER);
    private final CondenserOutput type;
    private final EmiIngredient viableStorageComponents;
    private final EmiStack output;

    public EmiCondenserRecipe(CondenserOutput type) {
        super(CATEGORY, EmiCondenserRecipe.getRecipeId(type), 96, 48);
        this.type = type;
        this.output = EmiStack.of((ItemStack)EmiCondenserRecipe.getOutput(type));
        this.outputs.add(this.output);
        this.viableStorageComponents = this.getViableStorageComponents(type);
        this.catalysts.add(this.viableStorageComponents);
    }

    public void addWidgets(WidgetHolder widgets) {
        ResourceLocation background = AppEng.makeId("textures/guis/condenser.png");
        widgets.addTexture(background, 0, 0, 96, 48, 48, 25);
        ResourceLocation statesLocation = AppEng.makeId("textures/guis/states.png");
        widgets.addTexture(statesLocation, 4, 28, 14, 14, 241, 81);
        widgets.addTexture(statesLocation, 80, 28, 16, 16, 240, 240);
        widgets.addAnimatedTexture(background, 72, 0, 6, 18, 176, 0, 2000, false, true, false);
        if (this.type == CondenserOutput.MATTER_BALLS) {
            widgets.addTexture(statesLocation, 80, 28, 14, 14, 16, 112);
        } else if (this.type == CondenserOutput.SINGULARITY) {
            widgets.addTexture(statesLocation, 80, 28, 14, 14, 32, 112);
        }
        widgets.addTooltipText(this.getTooltip(this.type), 80, 28, 16, 16);
        widgets.addSlot((EmiIngredient)this.output, 56, 26).drawBack(false);
        widgets.addSlot(this.viableStorageComponents, 52, 0).drawBack(false);
    }

    private static ItemStack getOutput(CondenserOutput recipe) {
        return switch (recipe) {
            case CondenserOutput.MATTER_BALLS -> AEItems.MATTER_BALL.stack();
            case CondenserOutput.SINGULARITY -> AEItems.SINGULARITY.stack();
            default -> ItemStack.EMPTY;
        };
    }

    private EmiIngredient getViableStorageComponents(CondenserOutput condenserOutput) {
        ArrayList<EmiStack> viableComponents = new ArrayList<EmiStack>();
        this.addViableComponent(condenserOutput, viableComponents, AEItems.CELL_COMPONENT_1K);
        this.addViableComponent(condenserOutput, viableComponents, AEItems.CELL_COMPONENT_4K);
        this.addViableComponent(condenserOutput, viableComponents, AEItems.CELL_COMPONENT_16K);
        this.addViableComponent(condenserOutput, viableComponents, AEItems.CELL_COMPONENT_64K);
        this.addViableComponent(condenserOutput, viableComponents, AEItems.CELL_COMPONENT_256K);
        return EmiIngredient.of(viableComponents);
    }

    private void addViableComponent(CondenserOutput condenserOutput, List<EmiStack> viableComponents, ItemLike item) {
        IStorageComponent comp = (IStorageComponent)item.asItem();
        int storage = comp.getBytes(item.asItem().getDefaultInstance()) * 8;
        if (storage >= condenserOutput.requiredPower) {
            viableComponents.add(EmiStack.of((ItemLike)item));
        }
    }

    private static ResourceLocation getRecipeId(CondenserOutput type) {
        return AppEng.makeId("/" + type.name().toLowerCase(Locale.ROOT));
    }

    private List<Component> getTooltip(CondenserOutput type) {
        String key;
        switch (type) {
            case MATTER_BALLS: {
                key = ButtonToolTips.MatterBalls.getTranslationKey();
                break;
            }
            case SINGULARITY: {
                key = ButtonToolTips.Singularity.getTranslationKey();
                break;
            }
            default: {
                return Collections.emptyList();
            }
        }
        return Splitter.on((String)"\n").splitToList((CharSequence)Component.translatable((String)key, (Object[])new Object[]{type.requiredPower}).getString()).stream().map(Component::literal).toList();
    }
}

