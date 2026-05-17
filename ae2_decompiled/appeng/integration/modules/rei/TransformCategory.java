/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  dev.architectury.fluid.FluidStack
 *  me.shedaniel.math.Point
 *  me.shedaniel.math.Rectangle
 *  me.shedaniel.rei.api.client.gui.Renderer
 *  me.shedaniel.rei.api.client.gui.widgets.Arrow
 *  me.shedaniel.rei.api.client.gui.widgets.Slot
 *  me.shedaniel.rei.api.client.gui.widgets.Widget
 *  me.shedaniel.rei.api.client.gui.widgets.Widgets
 *  me.shedaniel.rei.api.client.registry.display.DisplayCategory
 *  me.shedaniel.rei.api.common.category.CategoryIdentifier
 *  me.shedaniel.rei.api.common.entry.EntryIngredient
 *  me.shedaniel.rei.api.common.entry.EntryStack
 *  me.shedaniel.rei.api.common.util.EntryStacks
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.material.Fluid
 */
package appeng.integration.modules.rei;

import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.localization.ItemModText;
import appeng.integration.modules.rei.BackgroundRenderer;
import appeng.integration.modules.rei.FluidBlockRenderer;
import appeng.integration.modules.rei.TransformRecipeWrapper;
import appeng.recipes.transform.TransformCircumstance;
import dev.architectury.fluid.FluidStack;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Arrow;
import me.shedaniel.rei.api.client.gui.widgets.Slot;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;

public class TransformCategory
implements DisplayCategory<TransformRecipeWrapper> {
    public static final CategoryIdentifier<TransformRecipeWrapper> ID = CategoryIdentifier.of((ResourceLocation)AppEng.makeId("item_transformation"));
    private final Renderer icon = EntryStacks.of(AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED);

    public Renderer getIcon() {
        return this.icon;
    }

    public Component getTitle() {
        return ItemModText.TRANSFORM_CATEGORY.text();
    }

    public CategoryIdentifier<TransformRecipeWrapper> getCategoryIdentifier() {
        return ID;
    }

    public List<Widget> setupDisplay(TransformRecipeWrapper display, Rectangle bounds) {
        int col1;
        ArrayList<Widget> widgets = new ArrayList<Widget>();
        widgets.add((Widget)Widgets.wrapRenderer((Rectangle)bounds, (Renderer)new BackgroundRenderer(this.getDisplayWidth(display), this.getDisplayHeight())));
        int x = col1 = bounds.x + 10;
        int y = bounds.y + 10;
        int nInputs = display.getInputEntries().size();
        if (nInputs < 3) {
            y += 9 * (3 - nInputs);
        }
        for (EntryIngredient input : display.getInputEntries()) {
            Slot slot = Widgets.createSlot((Point)new Point(x, y)).entries((Collection)input).markInput();
            if ((y += slot.getBounds().height) >= bounds.y + 64) {
                y -= 54;
                x += 18;
            }
            widgets.add((Widget)slot);
        }
        int yOffset = bounds.y + 28;
        int col2 = col1 + 25;
        Arrow arrow1 = Widgets.createArrow((Point)new Point(col2, yOffset));
        widgets.add((Widget)arrow1);
        int col3 = col2 + arrow1.getBounds().getWidth() + 6;
        Slot catalystSlot = Widgets.createSlot((Point)new Point(col3, yOffset)).entries(this.getCatalystForRendering(display)).markInput().backgroundEnabled(false);
        widgets.add((Widget)catalystSlot);
        int col4 = col3 + 16 + 5;
        Arrow arrow2 = Widgets.createArrow((Point)new Point(col4, yOffset));
        widgets.add((Widget)arrow2);
        int col5 = arrow2.getBounds().getMaxX() + 10;
        Slot slot = Widgets.createSlot((Point)new Point(col5, yOffset)).entries((Collection)display.getOutputEntries().get(0)).markOutput();
        widgets.add((Widget)slot);
        MutableComponent circumstance = display.getTransformCircumstance().isExplosion() ? ItemModText.EXPLOSION.text() : ItemModText.SUBMERGE_IN.text();
        widgets.add((Widget)Widgets.createLabel((Point)new Point(bounds.getCenterX(), bounds.y + 15), (Component)circumstance).color(0x7E7E7E).noShadow());
        return widgets;
    }

    private Collection<? extends EntryStack<?>> getCatalystForRendering(TransformRecipeWrapper display) {
        TransformCircumstance circumstance = display.getTransformCircumstance();
        if (circumstance.isFluid()) {
            return circumstance.getFluidsForRendering().stream().map(TransformCategory::makeCustomRenderingFluidEntry).toList();
        }
        if (circumstance.isExplosion()) {
            return List.of(EntryStacks.of(AEBlocks.TINY_TNT), EntryStacks.of((ItemLike)Blocks.TNT));
        }
        return List.of();
    }

    private static EntryStack<FluidStack> makeCustomRenderingFluidEntry(Fluid fluid) {
        EntryStack fluidStack = EntryStacks.of((Fluid)fluid);
        fluidStack.withRenderer(entryStack -> new FluidBlockRenderer());
        return fluidStack;
    }

    public int getDisplayHeight() {
        return 72;
    }
}

