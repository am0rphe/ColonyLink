/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.components.AbstractWidget
 *  net.minecraft.client.gui.components.Button
 *  net.minecraft.client.gui.components.Button$OnPress
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.inventory.Slot
 *  net.minecraft.world.item.ItemStack
 */
package appeng.client.gui.implementations;

import appeng.api.config.FuzzyMode;
import appeng.api.config.Settings;
import appeng.client.gui.Icon;
import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.IconButton;
import appeng.client.gui.widgets.ServerSettingToggleButton;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.core.definitions.AEItems;
import appeng.core.localization.ButtonToolTips;
import appeng.menu.AEBaseMenu;
import appeng.menu.SlotSemantics;
import appeng.menu.implementations.InterfaceMenu;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class InterfaceScreen<C extends InterfaceMenu>
extends UpgradeableScreen<C> {
    private final SettingToggleButton<FuzzyMode> fuzzyMode;
    private final List<Button> amountButtons = new ArrayList<Button>();

    public InterfaceScreen(C menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
        this.fuzzyMode = new ServerSettingToggleButton<FuzzyMode>(Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL);
        this.addToLeftToolbar(this.fuzzyMode);
        this.widgets.addOpenPriorityButton();
        List<Slot> configSlots = ((AEBaseMenu)((Object)menu)).getSlots(SlotSemantics.CONFIG);
        for (int i = 0; i < configSlots.size(); ++i) {
            SetAmountButton button = new SetAmountButton(btn -> {
                int idx = this.amountButtons.indexOf(btn);
                Slot configSlot = (Slot)configSlots.get(idx);
                menu.openSetAmountMenu(configSlot.slot);
            });
            button.setDisableBackground(true);
            button.setMessage((Component)ButtonToolTips.InterfaceSetStockAmount.text());
            this.widgets.add("amtButton" + (1 + i), (AbstractWidget)button);
            this.amountButtons.add(button);
        }
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();
        this.fuzzyMode.set(((InterfaceMenu)this.menu).getFuzzyMode());
        this.fuzzyMode.setVisibility(((InterfaceMenu)this.menu).hasUpgrade(AEItems.FUZZY_CARD));
        List<Slot> configSlots = ((InterfaceMenu)this.menu).getSlots(SlotSemantics.CONFIG);
        for (int i = 0; i < this.amountButtons.size(); ++i) {
            Button button = this.amountButtons.get(i);
            ItemStack item = configSlots.get(i).getItem();
            button.visible = !item.isEmpty();
        }
    }

    static class SetAmountButton
    extends IconButton {
        public SetAmountButton(Button.OnPress onPress) {
            super(onPress);
        }

        @Override
        protected Icon getIcon() {
            return this.isHoveredOrFocused() ? Icon.COG : Icon.COG_DISABLED;
        }
    }
}

