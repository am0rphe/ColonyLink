/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.Rect2i
 *  org.jetbrains.annotations.Nullable
 */
package appeng.client.gui.style;

import appeng.client.Point;
import net.minecraft.client.renderer.Rect2i;
import org.jetbrains.annotations.Nullable;

public class Position {
    @Nullable
    private Integer left;
    @Nullable
    private Integer top;
    @Nullable
    private Integer right;
    @Nullable
    private Integer bottom;

    public Integer getLeft() {
        return this.left;
    }

    public void setLeft(Integer left) {
        this.left = left;
    }

    public Integer getTop() {
        return this.top;
    }

    public void setTop(Integer top) {
        this.top = top;
    }

    public Integer getRight() {
        return this.right;
    }

    public void setRight(Integer right) {
        this.right = right;
    }

    public Integer getBottom() {
        return this.bottom;
    }

    public void setBottom(Integer bottom) {
        this.bottom = bottom;
    }

    public Point resolve(Rect2i bounds) {
        int x = this.left != null ? this.left : (this.right != null ? bounds.getWidth() - this.right : 0);
        int y = this.top != null ? this.top : (this.bottom != null ? bounds.getHeight() - this.bottom : 0);
        return new Point(x, y).move(bounds.getX(), bounds.getY());
    }

    public String toString() {
        StringBuilder result = new StringBuilder();
        if (this.left != null) {
            result.append("left=").append(this.left).append(",");
        }
        if (this.top != null) {
            result.append("top=").append(this.top).append(",");
        }
        if (this.right != null) {
            result.append("right=").append(this.right).append(",");
        }
        if (this.bottom != null) {
            result.append("bottom=").append(this.bottom).append(",");
        }
        if (result.length() > 0) {
            result.deleteCharAt(result.length() - 1);
        }
        return result.toString();
    }
}

