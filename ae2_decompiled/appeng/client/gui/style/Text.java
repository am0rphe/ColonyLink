/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 */
package appeng.client.gui.style;

import appeng.client.gui.style.PaletteColor;
import appeng.client.gui.style.Position;
import appeng.client.gui.style.TextAlignment;
import net.minecraft.network.chat.Component;

public class Text {
    private Component text = Component.empty();
    private PaletteColor color = PaletteColor.DEFAULT_TEXT_COLOR;
    private Position position;
    private TextAlignment align = TextAlignment.LEFT;
    private float scale = 1.0f;
    private int maxWidth = 0;

    public Component getText() {
        return this.text;
    }

    public void setText(Component text) {
        this.text = text;
    }

    public PaletteColor getColor() {
        return this.color;
    }

    public void setColor(PaletteColor color) {
        this.color = color;
    }

    public Position getPosition() {
        return this.position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public TextAlignment getAlign() {
        return this.align;
    }

    public void setAlign(TextAlignment align) {
        this.align = align;
    }

    public float getScale() {
        return this.scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public int getMaxWidth() {
        return this.maxWidth;
    }

    public void setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
    }
}

