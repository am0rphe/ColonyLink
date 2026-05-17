/*
 * Decompiled with CFR 0.152.
 */
package appeng.menu.slot;

import appeng.api.inventories.InternalInventory;
import appeng.menu.slot.AppEngSlot;

public class ResizableSlot
extends AppEngSlot {
    private final String styleId;
    private int width = 16;
    private int height = 16;

    public ResizableSlot(InternalInventory inv, int invSlot, String styleId) {
        super(inv, invSlot);
        this.styleId = styleId;
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

    public String getStyleId() {
        return this.styleId;
    }
}

