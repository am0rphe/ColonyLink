/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.Rect2i
 */
package appeng.client;

import net.minecraft.client.renderer.Rect2i;

public final class Point {
    public static final Point ZERO = new Point(0, 0);
    private final int x;
    private final int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public static Point fromTopLeft(Rect2i bounds) {
        return new Point(bounds.getX(), bounds.getY());
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public Point move(int x, int y) {
        return new Point(this.x + x, this.y + y);
    }

    public boolean isIn(Rect2i rect) {
        return this.x >= rect.getX() && this.y >= rect.getY() && this.x < rect.getX() + rect.getWidth() && this.y < rect.getY() + rect.getHeight();
    }
}

