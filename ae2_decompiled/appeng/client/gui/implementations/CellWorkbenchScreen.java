/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.item.ItemStack
 */
package appeng.client.gui.implementations;

import appeng.api.config.ActionItems;
import appeng.api.config.CopyMode;
import appeng.api.config.FuzzyMode;
import appeng.api.config.Settings;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.AEKeySlotFilter;
import appeng.blockentity.misc.CellWorkbenchBlockEntity;
import appeng.client.gui.Icon;
import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.ActionButton;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.client.gui.widgets.ToggleButton;
import appeng.core.definitions.AEItems;
import appeng.core.localization.GuiText;
import appeng.menu.implementations.CellWorkbenchMenu;
import appeng.util.ConfigInventory;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class CellWorkbenchScreen
extends UpgradeableScreen<CellWorkbenchMenu> {
    private final ToggleButton copyMode;
    private final SettingToggleButton<FuzzyMode> fuzzyMode = this.addToLeftToolbar(new SettingToggleButton<FuzzyMode>(Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL, this::toggleFuzzyMode));

    public CellWorkbenchScreen(CellWorkbenchMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
        this.addToLeftToolbar(new ActionButton(ActionItems.COG, act -> menu.partition()));
        this.addToLeftToolbar(new ActionButton(ActionItems.CLOSE, act -> menu.clear()));
        this.copyMode = this.addToLeftToolbar(new ToggleButton(Icon.COPY_MODE_ON, Icon.COPY_MODE_OFF, (Component)GuiText.CopyMode.text(), (Component)GuiText.CopyModeDesc.text(), act -> menu.nextWorkBenchCopyMode()));
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();
        this.copyMode.setState(((CellWorkbenchMenu)this.menu).getCopyMode() == CopyMode.CLEAR_ON_REMOVE);
        boolean hasFuzzy = ((CellWorkbenchMenu)this.menu).getUpgrades().isInstalled(AEItems.FUZZY_CARD);
        this.fuzzyMode.set(((CellWorkbenchMenu)this.menu).getFuzzyMode());
        this.fuzzyMode.setVisibility(hasFuzzy);
    }

    private void toggleFuzzyMode(SettingToggleButton<FuzzyMode> button, boolean backwards) {
        FuzzyMode fz = button.getNextValue(backwards);
        ((CellWorkbenchMenu)this.menu).setCellFuzzyMode(fz);
    }

    protected List<Component> getTooltipFromContainerItem(ItemStack stack) {
        ItemStack cell = ((CellWorkbenchMenu)this.getMenu()).getWorkbenchItem();
        if (cell.isEmpty()) {
            return super.getTooltipFromContainerItem(stack);
        }
        if (cell == stack) {
            return super.getTooltipFromContainerItem(stack);
        }
        GenericStack genericStack = GenericStack.unwrapItemStack(stack);
        AEKey what = genericStack != null ? genericStack.what() : AEItemKey.of(stack);
        if (what == null) {
            return super.getTooltipFromContainerItem(stack);
        }
        ConfigInventory configInventory = ((CellWorkbenchBlockEntity)((CellWorkbenchMenu)this.getMenu()).getHost()).getCell().getConfigInventory(cell);
        if (!configInventory.isSupportedType(what.getType())) {
            ArrayList<Component> lines = new ArrayList<Component>(super.getTooltipFromContainerItem(stack));
            lines.add((Component)GuiText.IncompatibleWithCell.text().withStyle(ChatFormatting.RED));
            return lines;
        }
        AEKeySlotFilter filter = configInventory.getFilter();
        if (filter != null) {
            boolean anySlotMatches = false;
            for (int i = 0; i < configInventory.size(); ++i) {
                if (!configInventory.isAllowedIn(i, what)) continue;
                anySlotMatches = true;
                break;
            }
            if (!anySlotMatches) {
                ArrayList<Component> lines = new ArrayList<Component>(super.getTooltipFromContainerItem(stack));
                lines.add((Component)GuiText.IncompatibleWithCell.text().withStyle(ChatFormatting.RED));
                return lines;
            }
        }
        return super.getTooltipFromContainerItem(stack);
    }
}

