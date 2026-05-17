/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.AbstractButton
 *  net.minecraft.client.gui.narration.NarratedElementType
 *  net.minecraft.client.gui.narration.NarrationElementOutput
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.util.FormattedCharSequence
 */
package appeng.client.gui.widgets;

import appeng.client.gui.style.Blitter;
import appeng.client.gui.style.PaletteColor;
import appeng.client.gui.style.ScreenStyle;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;

public class AECheckbox
extends AbstractButton {
    public static final int SIZE = 14;
    private static final Blitter BLITTER = Blitter.texture("guis/checkbox.png", 64, 64);
    private static final Blitter UNCHECKED = BLITTER.copy().src(0, 28, 22, 12);
    private static final Blitter UNCHECKED_FOCUS = BLITTER.copy().src(22, 28, 22, 12);
    private static final Blitter CHECKED = BLITTER.copy().src(0, 40, 22, 12);
    private static final Blitter CHECKED_FOCUS = BLITTER.copy().src(22, 40, 22, 12);
    private static final Blitter RADIO_UNCHECKED = BLITTER.copy().src(28, 0, 14, 14);
    private static final Blitter RADIO_UNCHECKED_FOCUS = BLITTER.copy().src(42, 0, 14, 14);
    private static final Blitter RADIO_CHECKED = BLITTER.copy().src(28, 14, 14, 14);
    private static final Blitter RADIO_CHECKED_FOCUS = BLITTER.copy().src(42, 14, 14, 14);
    private final ScreenStyle style;
    private boolean selected;
    private Runnable changeListener;
    private boolean radio;

    public AECheckbox(int x, int y, int width, int height, ScreenStyle style, Component component) {
        super(x, y, width, height, component);
        this.style = style;
    }

    public void onPress() {
        boolean bl = this.selected = !this.selected;
        if (this.changeListener != null) {
            this.changeListener.run();
        }
    }

    public boolean isRadio() {
        return this.radio;
    }

    public void setRadio(boolean radio) {
        this.radio = radio;
    }

    public boolean isSelected() {
        return this.selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public void setChangeListener(Runnable listener) {
        this.changeListener = listener;
    }

    public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, (Component)this.createNarrationMessage());
        if (this.active) {
            if (this.isFocused()) {
                narrationElementOutput.add(NarratedElementType.USAGE, (Component)Component.translatable((String)"narration.checkbox.usage.focused"));
            } else {
                narrationElementOutput.add(NarratedElementType.USAGE, (Component)Component.translatable((String)"narration.checkbox.usage.hovered"));
            }
        }
    }

    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        Blitter icon;
        if (this.isRadio()) {
            icon = this.isMouseOver(mouseX, mouseY) ? (this.isSelected() ? RADIO_CHECKED_FOCUS : RADIO_UNCHECKED_FOCUS) : (this.isSelected() ? RADIO_CHECKED : RADIO_UNCHECKED);
        } else if (this.isMouseOver(mouseX, mouseY) && !this.isFocused()) {
            icon = this.isSelected() ? CHECKED_FOCUS : UNCHECKED_FOCUS;
        } else {
            Blitter blitter = icon = this.isSelected() ? CHECKED : UNCHECKED;
        }
        if (!this.isMouseOver(mouseX, mouseY)) {
            this.setFocused(false);
        }
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        PaletteColor textColor = this.isActive() ? PaletteColor.DEFAULT_TEXT_COLOR : PaletteColor.MUTED_TEXT_COLOR;
        float opacity = this.isActive() ? 1.0f : 0.5f;
        icon.dest(this.getX(), this.getY()).opacity(opacity).blit(guiGraphics);
        List lines = font.split((FormattedText)this.getMessage(), this.width - 22);
        int lineY = this.getY() + (lines.size() <= 1 ? 4 : 1);
        for (FormattedCharSequence line : lines) {
            guiGraphics.drawString(font, line, this.getX() + (this.isRadio() ? 16 : 26), lineY, this.style.getColor(textColor).toARGB(), false);
            Objects.requireNonNull(font);
            lineY += 9;
        }
    }
}

