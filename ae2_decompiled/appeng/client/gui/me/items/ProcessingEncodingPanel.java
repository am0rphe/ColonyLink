/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.AbstractWidget
 *  net.minecraft.client.renderer.Rect2i
 *  net.minecraft.network.chat.Component
 */
package appeng.client.gui.me.items;

import appeng.api.config.ActionItems;
import appeng.client.Point;
import appeng.client.gui.Icon;
import appeng.client.gui.WidgetContainer;
import appeng.client.gui.me.items.EncodingModePanel;
import appeng.client.gui.me.items.PatternEncodingTermScreen;
import appeng.client.gui.style.Blitter;
import appeng.client.gui.widgets.ActionButton;
import appeng.client.gui.widgets.Scrollbar;
import appeng.core.localization.GuiText;
import appeng.menu.SlotSemantics;
import appeng.menu.slot.FakeSlot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;

public class ProcessingEncodingPanel
extends EncodingModePanel {
    private static final Blitter BG = Blitter.texture("guis/pattern_modes.png").src(0, 70, 124, 66);
    private final ActionButton clearBtn = new ActionButton(ActionItems.S_CLOSE, act -> this.menu.clear());
    private final ActionButton cycleOutputBtn;
    private final Scrollbar scrollbar;

    public ProcessingEncodingPanel(PatternEncodingTermScreen<?> screen, WidgetContainer widgets) {
        super(screen, widgets);
        this.clearBtn.setHalfSize(true);
        this.clearBtn.setDisableBackground(true);
        widgets.add("processingClearPattern", (AbstractWidget)this.clearBtn);
        this.cycleOutputBtn = new ActionButton(ActionItems.S_CYCLE_PROCESSING_OUTPUT, act -> this.menu.cycleProcessingOutput());
        this.cycleOutputBtn.setHalfSize(true);
        this.cycleOutputBtn.setDisableBackground(true);
        widgets.add("processingCycleOutput", (AbstractWidget)this.cycleOutputBtn);
        this.scrollbar = widgets.addScrollBar("processingPatternModeScrollbar", Scrollbar.SMALL);
        this.scrollbar.setRange(0, this.menu.getProcessingInputSlots().length / 3 - 3, 3);
        this.scrollbar.setCaptureMouseWheel(false);
    }

    @Override
    public void updateBeforeRender() {
        int effectiveRow;
        FakeSlot slot;
        int i;
        this.screen.repositionSlots(SlotSemantics.PROCESSING_INPUTS);
        this.screen.repositionSlots(SlotSemantics.PROCESSING_OUTPUTS);
        for (i = 0; i < this.menu.getProcessingInputSlots().length; ++i) {
            slot = this.menu.getProcessingInputSlots()[i];
            effectiveRow = i / 3 - this.scrollbar.getCurrentScroll();
            slot.setActive(effectiveRow >= 0 && effectiveRow < 3);
            slot.y -= this.scrollbar.getCurrentScroll() * 18;
        }
        for (i = 0; i < this.menu.getProcessingOutputSlots().length; ++i) {
            slot = this.menu.getProcessingOutputSlots()[i];
            effectiveRow = i - this.scrollbar.getCurrentScroll();
            slot.setActive(effectiveRow >= 0 && effectiveRow < 3);
            slot.y -= this.scrollbar.getCurrentScroll() * 18;
        }
        this.updateTooltipVisibility();
    }

    @Override
    public void drawBackgroundLayer(GuiGraphics guiGraphics, Rect2i bounds, Point mouse) {
        BG.dest(bounds.getX() + 8, bounds.getY() + bounds.getHeight() - 165).blit(guiGraphics);
    }

    @Override
    public boolean onMouseWheel(Point mousePos, double delta) {
        return this.scrollbar.onMouseWheel(mousePos, delta);
    }

    private void updateTooltipVisibility() {
        this.widgets.setTooltipAreaEnabled("processing-primary-output", this.visible && this.scrollbar.getCurrentScroll() == 0);
        this.widgets.setTooltipAreaEnabled("processing-optional-output1", this.visible && this.scrollbar.getCurrentScroll() > 0);
        this.widgets.setTooltipAreaEnabled("processing-optional-output2", this.visible);
        this.widgets.setTooltipAreaEnabled("processing-optional-output3", this.visible);
    }

    @Override
    Icon getIcon() {
        return Icon.TAB_PROCESSING;
    }

    @Override
    public Component getTabTooltip() {
        return GuiText.ProcessingPattern.text();
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        this.scrollbar.setVisible(visible);
        this.clearBtn.setVisibility(visible);
        this.cycleOutputBtn.setVisibility(this.menu.canCycleProcessingOutputs());
        this.screen.setSlotsHidden(SlotSemantics.PROCESSING_INPUTS, !visible);
        this.screen.setSlotsHidden(SlotSemantics.PROCESSING_OUTPUTS, !visible);
        this.updateTooltipVisibility();
    }
}

