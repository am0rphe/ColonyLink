/*
 * Decompiled with CFR 0.152.
 */
package appeng.client.gui.style;

import appeng.client.gui.style.Position;

public class WidgetStyle
extends Position {
    private int width;
    private int height;
    private boolean hideEdge;

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

    public boolean isHideEdge() {
        return this.hideEdge;
    }

    public void setHideEdge(boolean hideEdge) {
        this.hideEdge = hideEdge;
    }
}

