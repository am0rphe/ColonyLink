/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Preconditions
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.AbstractWidget
 *  net.minecraft.client.gui.components.Button$OnPress
 *  net.minecraft.client.renderer.Rect2i
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.neoforged.neoforge.network.PacketDistributor
 *  org.jetbrains.annotations.Nullable
 */
package appeng.client.gui;

import appeng.client.Point;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.ICompositeWidget;
import appeng.client.gui.Icon;
import appeng.client.gui.NumberEntryType;
import appeng.client.gui.Tooltip;
import appeng.client.gui.style.Blitter;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.style.TooltipArea;
import appeng.client.gui.style.WidgetStyle;
import appeng.client.gui.widgets.AE2Button;
import appeng.client.gui.widgets.AECheckbox;
import appeng.client.gui.widgets.AETextField;
import appeng.client.gui.widgets.BackgroundPanel;
import appeng.client.gui.widgets.IResizableWidget;
import appeng.client.gui.widgets.NumberEntryWidget;
import appeng.client.gui.widgets.Scrollbar;
import appeng.client.gui.widgets.TabButton;
import appeng.core.localization.GuiText;
import appeng.core.network.serverbound.SwitchGuisPacket;
import appeng.menu.implementations.PriorityMenu;
import com.google.common.base.Preconditions;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

public class WidgetContainer {
    private final ScreenStyle style;
    private final Map<String, AbstractWidget> widgets = new LinkedHashMap<String, AbstractWidget>();
    private final Map<String, ICompositeWidget> compositeWidgets = new LinkedHashMap<String, ICompositeWidget>();
    private final Map<String, ResolvedTooltipArea> tooltips = new LinkedHashMap<String, ResolvedTooltipArea>();

    public WidgetContainer(ScreenStyle style) {
        this.style = style;
    }

    public void add(String id, AbstractWidget widget) {
        int height;
        Preconditions.checkState((!this.compositeWidgets.containsKey(id) ? 1 : 0) != 0, (String)"%s already used for composite widget", (Object)id);
        WidgetStyle widgetStyle = this.style.getWidget(id);
        int width = widgetStyle.getWidth() != 0 ? widgetStyle.getWidth() : widget.getWidth();
        int n = height = widgetStyle.getHeight() != 0 ? widgetStyle.getHeight() : widget.getHeight();
        if (widget instanceof IResizableWidget) {
            IResizableWidget resizableWidget = (IResizableWidget)widget;
            resizableWidget.resize(width, height);
        } else {
            widget.setWidth(width);
            widget.setHeight(height);
        }
        if (widget instanceof TabButton) {
            TabButton tabButton = (TabButton)widget;
            if (widgetStyle.isHideEdge()) {
                tabButton.setStyle(TabButton.Style.CORNER);
            }
        }
        if (this.widgets.put(id, widget) != null) {
            throw new IllegalStateException("Duplicate id: " + id);
        }
    }

    public void add(String id, ICompositeWidget widget) {
        Preconditions.checkState((!this.widgets.containsKey(id) ? 1 : 0) != 0, (String)"%s already used for widget", (Object)id);
        WidgetStyle widgetStyle = this.style.getWidget(id);
        widget.setSize(widgetStyle.getWidth(), widgetStyle.getHeight());
        if (this.compositeWidgets.put(id, widget) != null) {
            throw new IllegalStateException("Duplicate id: " + id);
        }
    }

    public AE2Button addButton(String id, Component text, Button.OnPress action) {
        AE2Button button = new AE2Button(text, action);
        this.add(id, (AbstractWidget)button);
        return button;
    }

    public AE2Button addButton(String id, Component text, Runnable action) {
        return this.addButton(id, text, btn -> action.run());
    }

    public AECheckbox addCheckbox(String id, Component text, Runnable changeListener) {
        AECheckbox checkbox = new AECheckbox(0, 0, 0, 14, this.style, text);
        this.add(id, (AbstractWidget)checkbox);
        checkbox.setChangeListener(changeListener);
        return checkbox;
    }

    public NumberEntryWidget addNumberEntryWidget(String id, NumberEntryType type) {
        NumberEntryWidget numberEntry = new NumberEntryWidget(this.style, type);
        this.add(id, numberEntry);
        return numberEntry;
    }

    public Scrollbar addScrollBar(String id) {
        return this.addScrollBar(id, Scrollbar.DEFAULT);
    }

    public Scrollbar addScrollBar(String id, Scrollbar.Style style) {
        Scrollbar scrollbar = new Scrollbar(style);
        this.add(id, scrollbar);
        return scrollbar;
    }

    public void addBackgroundPanel(String id) {
        Blitter background = this.style.getImage(id).copy();
        this.add(id, new BackgroundPanel(background));
    }

    void populateScreen(Consumer<AbstractWidget> addWidget, Rect2i bounds, AEBaseScreen<?> screen) {
        for (Map.Entry<String, AbstractWidget> entry : this.widgets.entrySet()) {
            AbstractWidget abstractWidget = entry.getValue();
            if (abstractWidget.isFocused()) {
                abstractWidget.setFocused(false);
            }
            WidgetStyle widgetStyle = this.style.getWidget(entry.getKey());
            Point pos = widgetStyle.resolve(bounds);
            if (abstractWidget instanceof IResizableWidget) {
                IResizableWidget resizableWidget = (IResizableWidget)abstractWidget;
                resizableWidget.move(pos);
            } else {
                abstractWidget.setX(pos.getX());
                abstractWidget.setY(pos.getY());
            }
            addWidget.accept(abstractWidget);
        }
        Rect2i relativeBounds = new Rect2i(0, 0, bounds.getWidth(), bounds.getHeight());
        for (Map.Entry<String, ICompositeWidget> entry : this.compositeWidgets.entrySet()) {
            ICompositeWidget widget = entry.getValue();
            WidgetStyle widgetStyle = this.style.getWidget(entry.getKey());
            widget.setPosition(widgetStyle.resolve(relativeBounds));
            widget.populateScreen(addWidget, bounds, screen);
        }
        this.tooltips.clear();
        for (Map.Entry<String, Object> entry : this.style.getTooltips().entrySet()) {
            Point pos = ((TooltipArea)entry.getValue()).resolve(relativeBounds);
            Rect2i area = new Rect2i(pos.getX(), pos.getY(), ((TooltipArea)entry.getValue()).getWidth(), ((TooltipArea)entry.getValue()).getHeight());
            this.tooltips.put(entry.getKey(), new ResolvedTooltipArea(area, new Tooltip(((TooltipArea)entry.getValue()).getTooltip())));
        }
    }

    public void tick() {
        for (ICompositeWidget widget : this.compositeWidgets.values()) {
            if (!widget.isVisible()) continue;
            widget.tick();
        }
    }

    public void updateBeforeRender() {
        for (ICompositeWidget widget : this.compositeWidgets.values()) {
            if (!widget.isVisible()) continue;
            widget.updateBeforeRender();
        }
    }

    public void drawBackgroundLayer(GuiGraphics guiGraphics, Rect2i bounds, Point mouse) {
        for (ICompositeWidget widget : this.compositeWidgets.values()) {
            if (!widget.isVisible()) continue;
            widget.drawBackgroundLayer(guiGraphics, bounds, mouse);
        }
    }

    public void drawForegroundLayer(GuiGraphics poseStack, Rect2i bounds, Point mouse) {
        for (ICompositeWidget widget : this.compositeWidgets.values()) {
            if (!widget.isVisible()) continue;
            widget.drawForegroundLayer(poseStack, bounds, mouse);
        }
    }

    public boolean onMouseDown(Point mousePos, int btn) {
        for (ICompositeWidget widget : this.compositeWidgets.values()) {
            if (!widget.isVisible() || !widget.wantsAllMouseDownEvents() && !mousePos.isIn(widget.getBounds()) || !widget.onMouseDown(mousePos, btn)) continue;
            return true;
        }
        return false;
    }

    public boolean onMouseUp(Point mousePos, int btn) {
        for (ICompositeWidget widget : this.compositeWidgets.values()) {
            if (!widget.isVisible() || !widget.wantsAllMouseUpEvents() && !mousePos.isIn(widget.getBounds()) || !widget.onMouseUp(mousePos, btn)) continue;
            return true;
        }
        return false;
    }

    public boolean onMouseDrag(Point mousePos, int btn) {
        for (ICompositeWidget widget : this.compositeWidgets.values()) {
            if (!widget.isVisible() || !widget.onMouseDrag(mousePos, btn)) continue;
            return true;
        }
        return false;
    }

    boolean onMouseWheel(Point mousePos, double wheelDelta) {
        for (ICompositeWidget widget : this.compositeWidgets.values()) {
            if (!widget.isVisible() || !mousePos.isIn(widget.getBounds()) || !widget.onMouseWheel(mousePos, wheelDelta)) continue;
            return true;
        }
        for (ICompositeWidget widget : this.compositeWidgets.values()) {
            if (!widget.isVisible() || !widget.wantsAllMouseWheelEvents() || !widget.onMouseWheel(mousePos, wheelDelta)) continue;
            return true;
        }
        return false;
    }

    public void addExclusionZones(List<Rect2i> exclusionZones, Rect2i bounds) {
        for (ICompositeWidget widget : this.compositeWidgets.values()) {
            if (!widget.isVisible()) continue;
            widget.addExclusionZones(exclusionZones, bounds);
        }
    }

    public void addOpenPriorityButton() {
        this.add("openPriority", (AbstractWidget)new TabButton(Icon.PRIORITY, (Component)GuiText.Priority.text(), btn -> this.openPriorityGui()));
    }

    private void openPriorityGui() {
        SwitchGuisPacket message = SwitchGuisPacket.openSubMenu(PriorityMenu.TYPE);
        PacketDistributor.sendToServer((CustomPacketPayload)message, (CustomPacketPayload[])new CustomPacketPayload[0]);
    }

    public void setTooltipAreaEnabled(String id, boolean enabled) {
        ResolvedTooltipArea tooltip = this.tooltips.get(id);
        Preconditions.checkArgument((tooltip != null ? 1 : 0) != 0, (String)"No tooltip with id '%s' is defined", (Object)id);
        tooltip.enabled = enabled;
    }

    @Nullable
    public Tooltip getTooltip(int mouseX, int mouseY) {
        for (ICompositeWidget c : this.compositeWidgets.values()) {
            Tooltip tooltip;
            Rect2i bounds;
            if (!c.isVisible() || mouseX < (bounds = c.getBounds()).getX() || mouseX >= bounds.getX() + bounds.getWidth() || mouseY < bounds.getY() || mouseY >= bounds.getY() + bounds.getHeight() || (tooltip = c.getTooltip(mouseX, mouseY)) == null) continue;
            return tooltip;
        }
        for (ResolvedTooltipArea tooltipArea : this.tooltips.values()) {
            if (!tooltipArea.enabled || !WidgetContainer.contains(tooltipArea.area, mouseX, mouseY)) continue;
            return tooltipArea.tooltip;
        }
        return null;
    }

    public boolean hitTest(Point mousePos) {
        for (ICompositeWidget widget : this.compositeWidgets.values()) {
            if (!mousePos.isIn(widget.getBounds())) continue;
            return true;
        }
        return false;
    }

    private static boolean contains(Rect2i area, int mouseX, int mouseY) {
        return mouseX >= area.getX() && mouseX < area.getX() + area.getWidth() && mouseY >= area.getY() && mouseY < area.getY() + area.getHeight();
    }

    public AETextField addTextField(String id) {
        AETextField searchField = new AETextField(this.style, Minecraft.getInstance().font, 0, 0, 0, 0);
        searchField.setBordered(false);
        searchField.setMaxLength(25);
        searchField.setTextColor(0xFFFFFF);
        searchField.setSelectionColor(-16777088);
        searchField.setVisible(true);
        this.add(id, (AbstractWidget)searchField);
        return searchField;
    }

    private static class ResolvedTooltipArea {
        private final Rect2i area;
        private final Tooltip tooltip;
        private boolean enabled = true;

        public ResolvedTooltipArea(Rect2i area, Tooltip tooltip) {
            this.area = area;
            this.tooltip = tooltip;
        }
    }
}

