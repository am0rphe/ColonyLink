/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  dev.architectury.fluid.FluidStack
 *  dev.architectury.hooks.fluid.forge.FluidStackHooksForge
 *  me.shedaniel.math.Point
 *  me.shedaniel.math.Rectangle
 *  me.shedaniel.rei.api.client.entry.renderer.EntryRenderer
 *  me.shedaniel.rei.api.client.gui.widgets.Tooltip
 *  me.shedaniel.rei.api.client.gui.widgets.TooltipContext
 *  me.shedaniel.rei.api.common.entry.EntryStack
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.world.level.material.Fluid
 *  org.jetbrains.annotations.Nullable
 */
package appeng.integration.modules.rei;

import appeng.api.client.AEKeyRendering;
import appeng.api.stacks.AEFluidKey;
import appeng.integration.modules.itemlists.FluidBlockRendering;
import dev.architectury.fluid.FluidStack;
import dev.architectury.hooks.fluid.forge.FluidStackHooksForge;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.entry.renderer.EntryRenderer;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.TooltipContext;
import me.shedaniel.rei.api.common.entry.EntryStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

public class FluidBlockRenderer
implements EntryRenderer<FluidStack> {
    public void render(EntryStack<FluidStack> entry, GuiGraphics guiGraphics, Rectangle bounds, int mouseX, int mouseY, float delta) {
        Fluid fluid = ((FluidStack)entry.getValue()).getFluid();
        FluidBlockRendering.render(guiGraphics, fluid, bounds.x, bounds.y, bounds.width, bounds.height);
    }

    @Nullable
    public Tooltip getTooltip(EntryStack<FluidStack> entry, TooltipContext context) {
        AEFluidKey key = AEFluidKey.of(FluidStackHooksForge.toForge((FluidStack)((FluidStack)entry.getValue())));
        return Tooltip.create((Point)context.getPoint(), AEKeyRendering.getTooltip(key));
    }
}

