/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  dev.emi.emi.api.stack.EmiIngredient
 *  dev.emi.emi.api.stack.EmiStack
 *  dev.emi.emi.api.widget.Bounds
 *  dev.emi.emi.api.widget.SlotWidget
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.util.FormattedCharSequence
 *  net.minecraft.world.level.material.Fluid
 */
package appeng.integration.modules.emi;

import appeng.api.client.AEKeyRendering;
import appeng.api.stacks.AEFluidKey;
import appeng.core.localization.ItemModText;
import appeng.integration.modules.emi.AppEngEmiPlugin;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.SlotWidget;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.material.Fluid;

class EmiEntropySlot
extends SlotWidget {
    private final boolean consumed;
    private final boolean flowing;

    public EmiEntropySlot(EmiStack stack, boolean consumed, boolean flowing, int x, int y) {
        super((EmiIngredient)stack, x, y);
        this.consumed = consumed;
        this.flowing = flowing;
    }

    public List<ClientTooltipComponent> getTooltip(int mouseX, int mouseY) {
        List tooltip = super.getTooltip(mouseX, mouseY);
        Fluid fluid = (Fluid)((EmiStack)this.stack).getKeyOfType(Fluid.class);
        if (fluid != null) {
            tooltip.clear();
            List<Component> fluidTooltip = AEKeyRendering.getTooltip(AEFluidKey.of(fluid));
            if (!fluidTooltip.isEmpty() && this.flowing) {
                fluidTooltip.set(0, (Component)ItemModText.FLOWING_FLUID_NAME.text(fluidTooltip.get(0)));
            }
            fluidTooltip.stream().map(Component::getVisualOrderText).map(ClientTooltipComponent::create).forEach(tooltip::add);
            this.addSlotTooltip(tooltip);
        }
        if (this.consumed) {
            MutableComponent text = ItemModText.CONSUMED.text().withStyle(new ChatFormatting[]{ChatFormatting.RED, ChatFormatting.BOLD});
            tooltip.add(ClientTooltipComponent.create((FormattedCharSequence)text.getVisualOrderText()));
        }
        return tooltip;
    }

    public void drawOverlay(GuiGraphics draw, int mouseX, int mouseY, float delta) {
        if (this.consumed) {
            Bounds bounds = this.getBounds();
            draw.blit(AppEngEmiPlugin.TEXTURE, bounds.x() + 1, bounds.y() + 1, 0, 52, 16, 16);
        }
        super.drawOverlay(draw, mouseX, mouseY, delta);
    }
}

