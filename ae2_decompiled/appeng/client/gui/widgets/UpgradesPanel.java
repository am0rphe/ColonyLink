/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.AbstractWidget
 *  net.minecraft.client.renderer.Rect2i
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.inventory.Slot
 *  org.jetbrains.annotations.Nullable
 */
package appeng.client.gui.widgets;

import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.upgrades.Upgrades;
import appeng.client.Point;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.ICompositeWidget;
import appeng.client.gui.Rects;
import appeng.client.gui.Tooltip;
import appeng.client.gui.style.Blitter;
import appeng.menu.slot.AppEngSlot;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.Nullable;

public final class UpgradesPanel
implements ICompositeWidget {
    private static final int SLOT_SIZE = 18;
    private static final int PADDING = 5;
    private static final int MAX_ROWS = 8;
    private static final Blitter BACKGROUND = Blitter.texture("guis/extra_panels.png", 128, 128);
    private static final Blitter INNER_CORNER = BACKGROUND.copy().src(12, 33, 18, 18);
    private final List<Slot> slots;
    private Point screenOrigin = Point.ZERO;
    private int x;
    private int y;
    private final Supplier<List<Component>> tooltipSupplier;

    public UpgradesPanel(List<Slot> slots) {
        this(slots, Collections::emptyList);
    }

    public UpgradesPanel(List<Slot> slots, IUpgradeableObject upgradeableObject) {
        this(slots, () -> Upgrades.getTooltipLinesForMachine(upgradeableObject.getUpgrades().getUpgradableItem()));
    }

    public UpgradesPanel(List<Slot> slots, Supplier<List<Component>> tooltipSupplier) {
        this.slots = slots;
        this.tooltipSupplier = tooltipSupplier;
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
        int slotCount = this.getUpgradeSlotCount();
        int height = 10 + Math.min(8, slotCount) * 18;
        int width = 10 + (slotCount + 8 - 1) / 8 * 18;
        return new Rect2i(this.x, this.y, width, height);
    }

    @Override
    public void populateScreen(Consumer<AbstractWidget> addWidget, Rect2i bounds, AEBaseScreen<?> screen) {
        this.screenOrigin = Point.fromTopLeft(bounds);
    }

    @Override
    public void updateBeforeRender() {
        int slotOriginX = this.x;
        int slotOriginY = this.y + 5;
        for (Slot slot : this.slots) {
            if (!slot.isActive()) continue;
            slot.x = slotOriginX + 1;
            slot.y = slotOriginY + 1;
            slotOriginY += 18;
        }
    }

    @Override
    public void drawBackgroundLayer(GuiGraphics guiGraphics, Rect2i bounds, Point mouse) {
        int slotCount = this.getUpgradeSlotCount();
        if (slotCount <= 0) {
            return;
        }
        int slotOriginX = this.screenOrigin.getX() + this.x + 5;
        int slotOriginY = this.screenOrigin.getY() + this.y + 5;
        for (int i = 0; i < slotCount; ++i) {
            int row = i % 8;
            int col = i / 8;
            int x = slotOriginX + col * 18;
            int y = slotOriginY + row * 18;
            boolean borderLeft = col == 0;
            boolean borderTop = row == 0;
            boolean lastSlot = i + 1 >= slotCount;
            boolean lastRow = row + 1 >= 8;
            boolean borderBottom = lastRow || lastSlot;
            boolean borderRight = i >= slotCount - 8;
            UpgradesPanel.drawSlot(guiGraphics, x, y, borderLeft, borderTop, borderRight, borderBottom);
            if (col <= 0 || !lastSlot || lastRow) continue;
            INNER_CORNER.dest(x, y + 18).blit(guiGraphics);
        }
        guiGraphics.hLine(slotOriginX - 4, slotOriginX + 11, slotOriginY, -855310);
        guiGraphics.hLine(slotOriginX - 4, slotOriginX + 11, slotOriginY + 18 * slotCount - 1, -855310);
        guiGraphics.vLine(slotOriginX - 5, slotOriginY - 1, slotOriginY + 18 * slotCount, -855310);
        guiGraphics.vLine(slotOriginX + 12, slotOriginY - 1, slotOriginY + 18 * slotCount, -855310);
    }

    @Override
    public void addExclusionZones(List<Rect2i> exclusionZones, Rect2i screenBounds) {
        int remaining;
        int offsetX = screenBounds.getX();
        int offsetY = screenBounds.getY();
        int slotCount = this.getUpgradeSlotCount();
        int margin = 2;
        int fullCols = slotCount / 8;
        int rightEdge = offsetX + this.x;
        if (fullCols > 0) {
            int fullColWidth = 10 + fullCols * 18;
            exclusionZones.add(Rects.expand(new Rect2i(rightEdge, offsetY + this.y, fullColWidth, 154), 2));
            rightEdge += fullColWidth;
        }
        if ((remaining = slotCount - fullCols * 8) > 0) {
            exclusionZones.add(Rects.expand(new Rect2i(rightEdge, offsetY + this.y, 18 + (fullCols > 0 ? 0 : 10), 10 + remaining * 18), 2));
        }
    }

    @Override
    @Nullable
    public Tooltip getTooltip(int mouseX, int mouseY) {
        if (this.getUpgradeSlotCount() == 0) {
            return null;
        }
        List<Component> tooltip = this.tooltipSupplier.get();
        if (tooltip.isEmpty()) {
            return null;
        }
        return new Tooltip(tooltip);
    }

    private static void drawSlot(GuiGraphics guiGraphics, int x, int y, boolean borderLeft, boolean borderTop, boolean borderRight, boolean borderBottom) {
        int srcX = 5;
        int srcY = 5;
        int srcWidth = 18;
        int srcHeight = 18;
        if (borderLeft) {
            x -= 5;
            srcX = 0;
            srcWidth += 5;
        }
        if (borderRight) {
            srcWidth += 5;
        }
        if (borderTop) {
            y -= 5;
            srcY = 0;
            srcHeight += 5;
        }
        if (borderBottom) {
            srcHeight += 7;
        }
        BACKGROUND.src(srcX, srcY, srcWidth, srcHeight).dest(x, y).blit(guiGraphics);
    }

    private int getUpgradeSlotCount() {
        int count = 0;
        for (Slot slot : this.slots) {
            if (!(slot instanceof AppEngSlot) || !((AppEngSlot)slot).isSlotEnabled()) continue;
            ++count;
        }
        return count;
    }
}

