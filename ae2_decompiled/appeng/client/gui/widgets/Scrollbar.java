/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.renderer.Rect2i
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.util.Mth
 */
package appeng.client.gui.widgets;

import appeng.client.Point;
import appeng.client.gui.ICompositeWidget;
import appeng.client.gui.style.Blitter;
import appeng.client.gui.widgets.EventRepeater;
import appeng.client.gui.widgets.IScrollSource;
import appeng.core.AppEng;
import java.time.Duration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class Scrollbar
implements IScrollSource,
ICompositeWidget {
    private boolean visible = true;
    private int displayX = 0;
    private int displayY = 0;
    private final Style style;
    private int height = 16;
    private int pageSize = 1;
    private int maxScroll = 0;
    private int minScroll = 0;
    private int currentScroll = 0;
    private boolean dragging;
    private int dragYOffset;
    private boolean captureMouseWheel = true;
    private final EventRepeater eventRepeater = new EventRepeater(Duration.ofMillis(250L), Duration.ofMillis(150L));
    public static final Style DEFAULT = Style.create(ResourceLocation.fromNamespaceAndPath((String)"minecraft", (String)"container/creative_inventory/scroller"), ResourceLocation.fromNamespaceAndPath((String)"minecraft", (String)"container/creative_inventory/scroller_disabled"));
    public static final Style BIG = Style.create(AppEng.makeId("big_scroller"), AppEng.makeId("big_scroller_disabled"));
    public static final Style SMALL = Style.create(AppEng.makeId("small_scroller"), AppEng.makeId("small_scroller_disabled"));

    public Scrollbar(Style style) {
        this.style = style;
    }

    public Scrollbar() {
        this(DEFAULT);
    }

    @Override
    public Rect2i getBounds() {
        return new Rect2i(this.displayX, this.displayY, this.style.handleWidth(), this.height);
    }

    @Override
    public void drawForegroundLayer(GuiGraphics guiGraphics, Rect2i bounds, Point mouse) {
        Blitter image;
        int yOffset;
        if (this.getRange() == 0) {
            yOffset = 0;
            image = Blitter.guiSprite(this.style.disabledSprite());
        } else {
            yOffset = this.getHandleYOffset();
            image = Blitter.guiSprite(this.style.enabledSprite());
        }
        image.dest(this.displayX, this.displayY + yOffset).blit(guiGraphics);
    }

    private int getHandleYOffset() {
        if (this.getRange() == 0) {
            return 0;
        }
        int availableHeight = this.height - this.style.handleHeight();
        return (this.currentScroll - this.minScroll) * availableHeight / this.getRange();
    }

    private int getRange() {
        return this.maxScroll - this.minScroll;
    }

    public Scrollbar setHeight(int v) {
        this.height = v;
        return this;
    }

    @Override
    public void setPosition(Point position) {
        this.displayX = position.getX();
        this.displayY = position.getY();
    }

    @Override
    public void setSize(int width, int height) {
        if (height != 0) {
            this.height = height;
        }
    }

    public void setRange(int min, int max, int pageSize) {
        this.minScroll = min;
        this.maxScroll = max;
        this.pageSize = pageSize;
        if (this.minScroll > this.maxScroll) {
            this.maxScroll = this.minScroll;
        }
        this.applyRange();
    }

    private void applyRange() {
        this.currentScroll = Math.max(Math.min(this.currentScroll, this.maxScroll), this.minScroll);
    }

    @Override
    public int getCurrentScroll() {
        return this.currentScroll;
    }

    public void setCurrentScroll(int currentScroll) {
        this.currentScroll = currentScroll;
        this.applyRange();
    }

    @Override
    public boolean onMouseDown(Point mousePos, int button) {
        int handleYOffset;
        if (button != 0) {
            return false;
        }
        this.dragging = false;
        if (this.getRange() == 0) {
            return true;
        }
        int relY = mousePos.getY() - this.displayY;
        if (relY < (handleYOffset = this.getHandleYOffset())) {
            this.pageUp();
            this.eventRepeater.repeat(this::pageUp);
        } else if (relY < handleYOffset + this.style.handleHeight()) {
            this.dragging = true;
            this.dragYOffset = relY - handleYOffset;
        } else {
            this.pageDown();
            this.eventRepeater.repeat(this::pageDown);
        }
        return true;
    }

    @Override
    public boolean onMouseUp(Point mousePos, int button) {
        if (button == 0) {
            this.dragging = false;
            this.eventRepeater.stop();
        }
        return false;
    }

    @Override
    public boolean wantsAllMouseUpEvents() {
        return true;
    }

    @Override
    public boolean onMouseDrag(Point mousePos, int button) {
        if (this.getRange() == 0 || !this.dragging || this.eventRepeater.isRepeating()) {
            return false;
        }
        double handleUpperEdgeY = mousePos.getY() - this.displayY - this.dragYOffset;
        double availableHeight = this.height - this.style.handleHeight();
        double position = Mth.clamp((double)(handleUpperEdgeY / availableHeight), (double)0.0, (double)1.0);
        this.currentScroll = this.minScroll + (int)Math.round(position * (double)this.getRange());
        this.applyRange();
        return true;
    }

    @Override
    public boolean onMouseWheel(Point mousePos, double delta) {
        if (this.getRange() == 0) {
            return false;
        }
        delta = Math.max(Math.min(-delta, 1.0), -1.0);
        this.currentScroll = (int)((double)this.currentScroll + delta * (double)this.pageSize);
        this.applyRange();
        return true;
    }

    @Override
    public boolean wantsAllMouseWheelEvents() {
        return this.captureMouseWheel;
    }

    public boolean isCaptureMouseWheel() {
        return this.captureMouseWheel;
    }

    public void setCaptureMouseWheel(boolean captureMouseWheel) {
        this.captureMouseWheel = captureMouseWheel;
    }

    @Override
    public void tick() {
        this.eventRepeater.tick();
    }

    private void pageUp() {
        this.currentScroll -= this.pageSize;
        this.applyRange();
    }

    private void pageDown() {
        this.currentScroll += this.pageSize;
        this.applyRange();
    }

    @Override
    public boolean isVisible() {
        return this.visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public record Style(ResourceLocation enabledSprite, ResourceLocation disabledSprite) {
        public static Style create(ResourceLocation enabledSprite, ResourceLocation disabledSprite) {
            return new Style(enabledSprite, disabledSprite);
        }

        public int handleWidth() {
            Minecraft minecraft = Minecraft.getInstance();
            return minecraft.getGuiSprites().getSprite(this.enabledSprite).contents().width();
        }

        public int handleHeight() {
            Minecraft minecraft = Minecraft.getInstance();
            return minecraft.getGuiSprites().getSprite(this.enabledSprite).contents().height();
        }
    }
}

