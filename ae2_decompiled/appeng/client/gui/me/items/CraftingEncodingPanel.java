/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.AbstractWidget
 *  net.minecraft.client.renderer.Rect2i
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.inventory.Slot
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
import appeng.client.gui.widgets.ToggleButton;
import appeng.core.localization.ButtonToolTips;
import appeng.core.localization.GuiText;
import appeng.menu.SlotSemantics;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;

public class CraftingEncodingPanel
extends EncodingModePanel {
    private static final Blitter BG = Blitter.texture("guis/pattern_modes.png").src(0, 0, 124, 66);
    private final ActionButton clearBtn = new ActionButton(ActionItems.S_CLOSE, act -> this.menu.clear());
    private final ToggleButton substitutionsBtn;
    private final ToggleButton fluidSubstitutionsBtn;

    public CraftingEncodingPanel(PatternEncodingTermScreen<?> screen, WidgetContainer widgets) {
        super(screen, widgets);
        this.clearBtn.setHalfSize(true);
        this.clearBtn.setDisableBackground(true);
        widgets.add("craftingClearPattern", (AbstractWidget)this.clearBtn);
        this.substitutionsBtn = this.createCraftingSubstitutionButton(widgets);
        this.fluidSubstitutionsBtn = this.createCraftingFluidSubstitutionButton(widgets);
    }

    @Override
    Icon getIcon() {
        return Icon.TAB_CRAFTING;
    }

    @Override
    public Component getTabTooltip() {
        return GuiText.CraftingPattern.text();
    }

    private ToggleButton createCraftingSubstitutionButton(WidgetContainer widgets) {
        ToggleButton button = new ToggleButton(Icon.S_SUBSTITUTION_ENABLED, Icon.S_SUBSTITUTION_DISABLED, this.menu::setSubstitute);
        button.setHalfSize(true);
        button.setDisableBackground(true);
        button.setTooltipOn(List.of(ButtonToolTips.SubstitutionsOn.text(), ButtonToolTips.SubstitutionsDescEnabled.text()));
        button.setTooltipOff(List.of(ButtonToolTips.SubstitutionsOff.text(), ButtonToolTips.SubstitutionsDescDisabled.text()));
        widgets.add("craftingSubstitutions", (AbstractWidget)button);
        return button;
    }

    private ToggleButton createCraftingFluidSubstitutionButton(WidgetContainer widgets) {
        ToggleButton button = new ToggleButton(Icon.S_FLUID_SUBSTITUTION_ENABLED, Icon.S_FLUID_SUBSTITUTION_DISABLED, this.menu::setSubstituteFluids);
        button.setHalfSize(true);
        button.setDisableBackground(true);
        button.setTooltipOn(List.of(ButtonToolTips.FluidSubstitutions.text(), ButtonToolTips.FluidSubstitutionsDescEnabled.text()));
        button.setTooltipOff(List.of(ButtonToolTips.FluidSubstitutions.text(), ButtonToolTips.FluidSubstitutionsDescDisabled.text()));
        widgets.add("craftingFluidSubstitutions", (AbstractWidget)button);
        return button;
    }

    @Override
    public void drawBackgroundLayer(GuiGraphics guiGraphics, Rect2i bounds, Point mouse) {
        BG.dest(bounds.getX() + 8, bounds.getY() + bounds.getHeight() - 165).blit(guiGraphics);
        int absMouseX = bounds.getX() + mouse.getX();
        int absMouseY = bounds.getY() + mouse.getY();
        if (this.menu.substituteFluids && this.fluidSubstitutionsBtn.isMouseOver(absMouseX, absMouseY)) {
            for (Integer slotIndex : this.menu.slotsSupportingFluidSubstitution) {
                this.drawSlotGreenBG(bounds, guiGraphics, this.menu.getCraftingGridSlots()[slotIndex]);
            }
        }
    }

    private void drawSlotGreenBG(Rect2i bounds, GuiGraphics guiGraphics, Slot slot) {
        int x = bounds.getX() + slot.x;
        int y = bounds.getY() + slot.y;
        guiGraphics.fill(x, y, x + 16, y + 16, -8732065);
    }

    @Override
    public void updateBeforeRender() {
        this.substitutionsBtn.setState(this.menu.substitute);
        this.fluidSubstitutionsBtn.setState(this.menu.substituteFluids);
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        this.clearBtn.setVisibility(visible);
        this.substitutionsBtn.setVisibility(visible);
        this.fluidSubstitutionsBtn.setVisibility(visible);
        this.screen.setSlotsHidden(SlotSemantics.CRAFTING_GRID, !visible);
        this.screen.setSlotsHidden(SlotSemantics.CRAFTING_RESULT, !visible);
    }
}

