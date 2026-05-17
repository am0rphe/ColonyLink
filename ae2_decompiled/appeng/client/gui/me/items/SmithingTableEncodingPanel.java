/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.AbstractWidget
 *  net.minecraft.client.renderer.Rect2i
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.Container
 *  net.minecraft.world.SimpleContainer
 *  net.minecraft.world.inventory.Slot
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.crafting.RecipeHolder
 *  net.minecraft.world.item.crafting.RecipeInput
 *  net.minecraft.world.item.crafting.RecipeType
 *  net.minecraft.world.item.crafting.SmithingRecipe
 *  net.minecraft.world.item.crafting.SmithingRecipeInput
 *  net.minecraft.world.level.Level
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
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.level.Level;

public class SmithingTableEncodingPanel
extends EncodingModePanel {
    private static final Blitter BG = Blitter.texture("guis/pattern_modes.png").src(128, 70, 124, 66);
    private final ActionButton clearBtn = new ActionButton(ActionItems.S_CLOSE, act -> this.menu.clear());
    private final ToggleButton substitutionsBtn;
    private final Slot resultSlot;

    public SmithingTableEncodingPanel(PatternEncodingTermScreen<?> screen, WidgetContainer widgets) {
        super(screen, widgets);
        this.clearBtn.setHalfSize(true);
        this.clearBtn.setDisableBackground(true);
        widgets.add("smithingTableClearPattern", (AbstractWidget)this.clearBtn);
        this.substitutionsBtn = this.createSubstitutionButton(widgets);
        this.resultSlot = new Slot((Container)new SimpleContainer(1), 0, 0, 0);
        this.menu.addClientSideSlot(this.resultSlot, SlotSemantics.SMITHING_TABLE_RESULT);
    }

    @Override
    Icon getIcon() {
        return Icon.TAB_SMITHING;
    }

    @Override
    public Component getTabTooltip() {
        return GuiText.SmithingTablePattern.text();
    }

    private ToggleButton createSubstitutionButton(WidgetContainer widgets) {
        ToggleButton button = new ToggleButton(Icon.S_SUBSTITUTION_ENABLED, Icon.S_SUBSTITUTION_DISABLED, this.menu::setSubstitute);
        button.setHalfSize(true);
        button.setDisableBackground(true);
        button.setTooltipOn(List.of(ButtonToolTips.SubstitutionsOn.text(), ButtonToolTips.SubstitutionsDescEnabled.text()));
        button.setTooltipOff(List.of(ButtonToolTips.SubstitutionsOff.text(), ButtonToolTips.SubstitutionsDescDisabled.text()));
        widgets.add("smithingTableSubstitutions", (AbstractWidget)button);
        return button;
    }

    @Override
    public void drawBackgroundLayer(GuiGraphics guiGraphics, Rect2i bounds, Point mouse) {
        BG.dest(bounds.getX() + 8, bounds.getY() + bounds.getHeight() - 165).blit(guiGraphics);
    }

    @Override
    public void updateBeforeRender() {
        this.substitutionsBtn.setState(this.menu.substitute);
        SmithingRecipeInput recipeInput = new SmithingRecipeInput(this.menu.getSmithingTableTemplateSlot().getItem(), this.menu.getSmithingTableBaseSlot().getItem(), this.menu.getSmithingTableAdditionSlot().getItem());
        Level level = this.menu.getPlayer().level();
        RecipeHolder recipe = level.getRecipeManager().getRecipeFor(RecipeType.SMITHING, (RecipeInput)recipeInput, level).orElse(null);
        if (recipe == null) {
            this.resultSlot.set(ItemStack.EMPTY);
        } else {
            this.resultSlot.set(((SmithingRecipe)recipe.value()).assemble((RecipeInput)recipeInput, (HolderLookup.Provider)level.registryAccess()));
        }
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        this.clearBtn.setVisibility(visible);
        this.substitutionsBtn.setVisibility(visible);
        this.screen.setSlotsHidden(SlotSemantics.SMITHING_TABLE_TEMPLATE, !visible);
        this.screen.setSlotsHidden(SlotSemantics.SMITHING_TABLE_BASE, !visible);
        this.screen.setSlotsHidden(SlotSemantics.SMITHING_TABLE_ADDITION, !visible);
        this.screen.setSlotsHidden(SlotSemantics.SMITHING_TABLE_RESULT, !visible);
    }
}

