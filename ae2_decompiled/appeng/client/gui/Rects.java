/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.Rect2i
 */
package appeng.client.gui;

import net.minecraft.client.renderer.Rect2i;

public final class Rects {
    public static final Rect2i ZERO = new Rect2i(0, 0, 0, 0);

    private Rects() {
    }

    public static Rect2i expand(Rect2i rect, int amount) {
        return new Rect2i(rect.getX() - amount, rect.getY() - amount, rect.getWidth() + 2 * amount, rect.getHeight() + 2 * amount);
    }

    public static Rect2i move(Rect2i rect, int x, int y) {
        return new Rect2i(rect.getX() + x, rect.getY() + y, rect.getWidth(), rect.getHeight());
    }
}

