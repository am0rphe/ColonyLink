/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.GlStateManager$LogicOp
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.EditBox
 *  net.minecraft.client.renderer.Rect2i
 *  net.minecraft.network.chat.Component
 *  net.minecraft.util.Mth
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package appeng.client.gui.widgets;

import appeng.client.Point;
import appeng.client.gui.style.Blitter;
import appeng.client.gui.style.PaletteColor;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.IResizableWidget;
import appeng.client.gui.widgets.ITooltip;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AETextField
extends EditBox
implements IResizableWidget,
ITooltip {
    private static final Blitter BLITTER = Blitter.texture("guis/text_field.png", 128, 128);
    private static final int PADDING = 2;
    private final int fontPad;
    private final ScreenStyle style;
    private int selectionColor;
    private List<Component> tooltipMessage = Collections.emptyList();
    @Nullable
    private Component placeholder;

    public AETextField(ScreenStyle style, Font fontRenderer, int xPos, int yPos, int width, int height) {
        super(fontRenderer, xPos + 2, yPos + 2, width - 4 - fontRenderer.width("_"), height - 4, (Component)Component.empty());
        this.style = style;
        this.fontPad = fontRenderer.width("_");
        this.setSelectionColor(style.getColor(PaletteColor.TEXTFIELD_SELECTION).toARGB());
        this.setTextColor(style.getColor(PaletteColor.TEXTFIELD_TEXT).toARGB());
    }

    public boolean isMouseOver(double mouseX, double mouseY) {
        VisualBounds bounds = this.getVisualBounds();
        return mouseX >= (double)bounds.left && mouseX < (double)bounds.right && mouseY >= (double)bounds.top && mouseY < (double)bounds.bottom;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isMouseOver(mouseX, mouseY)) {
            mouseX = Mth.clamp((double)mouseX, (double)this.getX(), (double)(this.getX() + this.width - 1));
            mouseY = Mth.clamp((double)mouseY, (double)this.getY(), (double)(this.getY() + this.height - 1));
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        return this.isFocused() && this.canConsumeInput() && keyCode != 258 && keyCode != 256;
    }

    @Override
    public void move(Point pos) {
        super.setX(pos.getX() + 2);
        this.setY(pos.getY() + 2);
    }

    @Override
    public void resize(int width, int height) {
        super.setWidth(width - 4 - this.fontPad);
        this.height = height - 4;
    }

    public void selectAll() {
        this.moveCursorTo(0, false);
        this.setHighlightPos(this.getMaxLength());
    }

    public void setSelectionColor(int color) {
        this.selectionColor = color;
    }

    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partial) {
        if (this.isVisible()) {
            int yOffset = 0;
            if (!this.isEditable()) {
                yOffset = 12;
            } else if (this.isFocused()) {
                yOffset = 24;
            }
            VisualBounds bounds = this.getVisualBounds();
            BLITTER.src(0, yOffset, 1, 12).dest(bounds.left, bounds.top).blit(guiGraphics);
            int backgroundWidth = Math.min(126, bounds.right - bounds.left - 2);
            BLITTER.src(1, yOffset, backgroundWidth, 12).dest(bounds.left + 1, bounds.top).blit(guiGraphics);
            BLITTER.src(127, yOffset, 1, 12).dest(bounds.right - 1, bounds.top).blit(guiGraphics);
            super.renderWidget(guiGraphics, mouseX, mouseY, partial);
            if (this.placeholder != null && !this.isFocused() && this.getValue().isEmpty()) {
                Font font = Minecraft.getInstance().font;
                guiGraphics.drawString(font, this.placeholder, this.getX(), this.getY(), this.style.getColor(PaletteColor.TEXTFIELD_PLACEHOLDER).toARGB(), false);
            }
        }
    }

    public void renderHighlight(GuiGraphics guiGraphics, int startX, int startY, int endX, int endY) {
        if (!this.isFocused()) {
            return;
        }
        if (startX < endX) {
            int i = startX;
            startX = endX;
            endX = i;
        }
        ++startX;
        --endX;
        if (startY < endY) {
            int j = startY;
            startY = endY;
            endY = j;
        }
        endX = Mth.clamp((int)endX, (int)this.getX(), (int)(this.getX() + this.width));
        startX = Mth.clamp((int)startX, (int)this.getX(), (int)(this.getX() + this.width));
        RenderSystem.enableColorLogicOp();
        RenderSystem.logicOp((GlStateManager.LogicOp)GlStateManager.LogicOp.OR_REVERSE);
        guiGraphics.fill(startX, startY -= 2, endX, endY, this.selectionColor);
        RenderSystem.disableColorLogicOp();
    }

    @Override
    public Rect2i getTooltipArea() {
        return new Rect2i(this.getX() - 2, this.getY() - 2, this.width + 4 + this.fontPad, this.height + 4);
    }

    @Override
    public boolean isTooltipAreaVisible() {
        return this.visible;
    }

    @Override
    @NotNull
    public List<Component> getTooltipMessage() {
        return this.tooltipMessage;
    }

    public void setTooltipMessage(List<Component> tooltipMessage) {
        this.tooltipMessage = Objects.requireNonNull(tooltipMessage);
    }

    private VisualBounds getVisualBounds() {
        int left = this.getX() - 2;
        int top = this.getY() - 2;
        int right = left + this.width + 4 + this.fontPad;
        return new VisualBounds(left, top, right, top + this.height + 4);
    }

    public void setPlaceholder(Component placeholder) {
        this.placeholder = placeholder;
    }

    public Component getPlaceholder() {
        return this.placeholder;
    }

    private record VisualBounds(int left, int top, int right, int bottom) {
    }
}

