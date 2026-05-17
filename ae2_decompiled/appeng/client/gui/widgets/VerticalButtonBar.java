/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.AbstractWidget
 *  net.minecraft.client.gui.components.Button
 *  net.minecraft.client.renderer.Rect2i
 */
package appeng.client.gui.widgets;

import appeng.client.Point;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.ICompositeWidget;
import appeng.core.AppEng;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.Rect2i;

public class VerticalButtonBar
implements ICompositeWidget {
    private static final int VERTICAL_SPACING = 6;
    private static final int MARGIN = 2;
    private final List<Button> buttons = new ArrayList<Button>();
    private Point screenOrigin = Point.ZERO;
    private Rect2i bounds = new Rect2i(0, 0, 0, 0);
    private Point position;

    public void add(Button button) {
        this.buttons.add(button);
    }

    @Override
    public void setPosition(Point position) {
        this.position = position;
    }

    @Override
    public void setSize(int width, int height) {
    }

    @Override
    public Rect2i getBounds() {
        return this.bounds;
    }

    @Override
    public void updateBeforeRender() {
        int currentY = this.position.getY() + 2;
        int maxWidth = 0;
        for (Button button : this.buttons) {
            if (!button.visible) continue;
            button.setX(this.screenOrigin.getX() + this.position.getX() - 2 - button.getWidth());
            button.setY(this.screenOrigin.getY() + currentY);
            currentY += button.getHeight() + 6;
            maxWidth = Math.max(button.getWidth(), maxWidth);
        }
        if (maxWidth == 0) {
            this.bounds = new Rect2i(0, 0, 0, 0);
        } else {
            int boundX = this.position.getX() - maxWidth - 4;
            int boundY = this.position.getY();
            this.bounds = new Rect2i(boundX, boundY, maxWidth + 4, currentY - boundY);
        }
    }

    @Override
    public void populateScreen(Consumer<AbstractWidget> addWidget, Rect2i bounds, AEBaseScreen<?> screen) {
        this.screenOrigin = Point.fromTopLeft(bounds);
        for (Button button : this.buttons) {
            if (button.isFocused()) {
                button.setFocused(false);
            }
            addWidget.accept((AbstractWidget)button);
        }
    }

    @Override
    public void drawBackgroundLayer(GuiGraphics guiGraphics, Rect2i bounds, Point mouse) {
        guiGraphics.blitSprite(AppEng.makeId("vertical_buttons_bg"), bounds.getX() + this.bounds.getX() - 2, bounds.getY() + this.bounds.getY() - 1, 1, this.bounds.getWidth() + 1, this.bounds.getHeight() + 4);
    }
}

