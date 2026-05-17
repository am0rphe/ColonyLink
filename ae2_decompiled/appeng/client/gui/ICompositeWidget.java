/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.AbstractWidget
 *  net.minecraft.client.renderer.Rect2i
 *  org.jetbrains.annotations.Nullable
 */
package appeng.client.gui;

import appeng.client.Point;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.Tooltip;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.renderer.Rect2i;
import org.jetbrains.annotations.Nullable;

public interface ICompositeWidget {
    default public boolean isVisible() {
        return true;
    }

    public void setPosition(Point var1);

    public void setSize(int var1, int var2);

    public Rect2i getBounds();

    default public void addExclusionZones(List<Rect2i> exclusionZones, Rect2i screenBounds) {
        Rect2i bounds = this.getBounds();
        if (bounds.getWidth() <= 0 || bounds.getHeight() <= 0) {
            return;
        }
        if (bounds.getX() < 0 || bounds.getY() < 0 || bounds.getX() + bounds.getWidth() > screenBounds.getWidth() || bounds.getY() + bounds.getHeight() > screenBounds.getHeight()) {
            exclusionZones.add(new Rect2i(screenBounds.getX() + bounds.getX(), screenBounds.getY() + bounds.getY(), bounds.getWidth(), bounds.getHeight()));
        }
    }

    default public void populateScreen(Consumer<AbstractWidget> addWidget, Rect2i bounds, AEBaseScreen<?> screen) {
    }

    default public void tick() {
    }

    default public void updateBeforeRender() {
    }

    default public void drawBackgroundLayer(GuiGraphics guiGraphics, Rect2i bounds, Point mouse) {
    }

    default public void drawForegroundLayer(GuiGraphics guiGraphics, Rect2i bounds, Point mouse) {
    }

    default public boolean onMouseDown(Point mousePos, int button) {
        return false;
    }

    default public boolean wantsAllMouseDownEvents() {
        return false;
    }

    default public boolean onMouseUp(Point mousePos, int button) {
        return false;
    }

    default public boolean wantsAllMouseUpEvents() {
        return false;
    }

    default public boolean onMouseDrag(Point mousePos, int button) {
        return false;
    }

    default public boolean onMouseWheel(Point mousePos, double delta) {
        return false;
    }

    default public boolean wantsAllMouseWheelEvents() {
        return false;
    }

    @Nullable
    default public Tooltip getTooltip(int mouseX, int mouseY) {
        return null;
    }
}

