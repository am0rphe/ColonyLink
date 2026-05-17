/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  dev.emi.emi.api.stack.EmiIngredient
 *  dev.emi.emi.api.widget.Bounds
 *  dev.emi.emi.api.widget.SlotWidget
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.world.level.material.Fluid
 *  net.minecraft.world.level.material.Fluids
 */
package appeng.integration.modules.emi;

import appeng.integration.modules.itemlists.FluidBlockRendering;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.SlotWidget;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

class EmiFluidBlockSlot
extends SlotWidget {
    private final List<Fluid> allFluids;

    public EmiFluidBlockSlot(EmiIngredient stack, int x, int y) {
        super(stack, x, y);
        this.allFluids = stack.getEmiStacks().stream().map(s -> (Fluid)s.getKeyOfType(Fluid.class)).distinct().toList();
    }

    public void drawStack(GuiGraphics draw, int mouseX, int mouseY, float delta) {
        Bounds bounds = this.getBounds();
        FluidBlockRendering.render(draw, this.getCurrentFluid(), bounds.x(), bounds.y(), bounds.width(), bounds.height());
    }

    private Fluid getCurrentFluid() {
        if (this.allFluids.isEmpty()) {
            return Fluids.EMPTY;
        }
        int item = (int)(System.currentTimeMillis() / 1000L % (long)this.allFluids.size());
        return this.allFluids.get(item);
    }
}

