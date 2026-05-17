/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.renderer.Rect2i
 */
package appeng.client.gui.widgets;

import appeng.client.Point;
import appeng.client.gui.ICompositeWidget;
import appeng.client.gui.style.Blitter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;

public class BackgroundPanel
implements ICompositeWidget {
    private final Blitter background;
    private int x;
    private int y;

    public BackgroundPanel(Blitter background) {
        this.background = background;
    }

    @Override
    public void setPosition(Point position) {
        this.x = position.getX();
        this.y = position.getY();
    }

    @Override
    public void setSize(int width, int height) {
    }

    @Override
    public Rect2i getBounds() {
        return new Rect2i(this.x, this.y, this.background.getSrcWidth(), this.background.getSrcHeight());
    }

    @Override
    public void drawBackgroundLayer(GuiGraphics guiGraphics, Rect2i bounds, Point mouse) {
        this.background.dest(bounds.getX() + this.x, bounds.getY() + this.y).blit(guiGraphics);
    }
}

