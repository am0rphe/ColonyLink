/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.Font$DisplayMode
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.MultiBufferSource$BufferSource
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.world.item.ItemStack
 *  org.joml.Matrix4f
 */
package appeng.client.render;

import appeng.api.client.AEKeyRendering;
import appeng.api.stacks.AmountFormat;
import appeng.api.stacks.GenericStack;
import appeng.client.gui.me.common.StackSizeRenderer;
import appeng.core.localization.GuiText;
import appeng.items.storage.StorageCellTooltipComponent;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;

public class StorageCellClientTooltipComponent
implements ClientTooltipComponent {
    private final StorageCellTooltipComponent tooltipComponent;
    private final Component upgradesLabel;

    public StorageCellClientTooltipComponent(StorageCellTooltipComponent tooltipComponent) {
        this.tooltipComponent = tooltipComponent;
        this.upgradesLabel = GuiText.StorageCellTooltipUpgrades.text();
    }

    public int getHeight() {
        List<GenericStack> content;
        int height = 0;
        List<ItemStack> upgrades = this.tooltipComponent.upgrades();
        if (!upgrades.isEmpty()) {
            height += 17;
        }
        if (!(content = this.tooltipComponent.content()).isEmpty()) {
            height += 17;
        }
        return height;
    }

    public int getWidth(Font font) {
        List<ItemStack> upgrades;
        int width = 0;
        List<GenericStack> content = this.tooltipComponent.content();
        if (!content.isEmpty()) {
            int filterWidth = content.size() * 17;
            if (this.tooltipComponent.hasMoreContent()) {
                filterWidth += 10;
            }
            width = Math.max(width, filterWidth);
        }
        if (!(upgrades = this.tooltipComponent.upgrades()).isEmpty()) {
            int upgradesWidth = font.width((FormattedText)this.upgradesLabel) + 2 + 17 * upgrades.size();
            width = Math.max(width, upgradesWidth);
        }
        return width;
    }

    public void renderText(Font font, int x, int y, Matrix4f matrix4f, MultiBufferSource.BufferSource bufferSource) {
        List<ItemStack> upgrades;
        Objects.requireNonNull(font);
        int yoff = (16 - 9) / 2;
        List<GenericStack> content = this.tooltipComponent.content();
        if (!content.isEmpty()) {
            int xoff = content.size() * 17;
            if (this.tooltipComponent.hasMoreContent()) {
                font.drawInBatch("\u2026", (float)(x + xoff + 2), (float)(y + 2), -1, false, matrix4f, (MultiBufferSource)bufferSource, Font.DisplayMode.NORMAL, 0, 0xF000F0);
            }
            y += 17;
        }
        if (!(upgrades = this.tooltipComponent.upgrades()).isEmpty()) {
            font.drawInBatch(this.upgradesLabel, (float)x, (float)(y + yoff), 0x7E7E7E, false, matrix4f, (MultiBufferSource)bufferSource, Font.DisplayMode.NORMAL, 0, 0xF000F0);
        }
    }

    public void renderImage(Font font, int x, int y, GuiGraphics guiGraphics) {
        List<ItemStack> upgrades;
        List<GenericStack> content = this.tooltipComponent.content();
        if (!content.isEmpty()) {
            int xoff = 0;
            for (GenericStack stack : content) {
                AEKeyRendering.drawInGui(Minecraft.getInstance(), guiGraphics, x + xoff, y, stack.what());
                xoff += 17;
            }
            if (this.tooltipComponent.showAmounts()) {
                xoff = 0;
                for (GenericStack stack : content) {
                    String amtText = stack.what().formatAmount(stack.amount(), AmountFormat.SLOT);
                    StackSizeRenderer.renderSizeLabel(guiGraphics, font, (float)(x + xoff), (float)y, amtText, false);
                    xoff += 17;
                }
            }
            y += 17;
        }
        if (!(upgrades = this.tooltipComponent.upgrades()).isEmpty()) {
            int xoff = font.width((FormattedText)this.upgradesLabel) + 2;
            for (ItemStack upgrade : upgrades) {
                guiGraphics.renderItem(upgrade, x + xoff, y);
                xoff += 17;
            }
        }
    }
}

