/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 */
package appeng.client.gui.style;

import appeng.client.gui.style.Position;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;

public final class TooltipArea
extends Position {
    private int width;
    private int height;
    private List<Component> tooltip = new ArrayList<Component>();

    public List<Component> getTooltip() {
        return this.tooltip;
    }

    public void setTooltip(List<Component> tooltip) {
        this.tooltip = tooltip;
    }

    public int getWidth() {
        return this.width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return this.height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}

