/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.entity.player.Inventory
 */
package appeng.client.gui.implementations;

import appeng.api.upgrades.Upgrades;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.ToolboxPanel;
import appeng.client.gui.widgets.UpgradesPanel;
import appeng.core.localization.GuiText;
import appeng.menu.AEBaseMenu;
import appeng.menu.SlotSemantics;
import appeng.menu.implementations.UpgradeableMenu;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class UpgradeableScreen<T extends UpgradeableMenu<?>>
extends AEBaseScreen<T> {
    public UpgradeableScreen(T menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
        this.widgets.add("upgrades", new UpgradesPanel(((AEBaseMenu)((Object)menu)).getSlots(SlotSemantics.UPGRADE), this::getCompatibleUpgrades));
        if (((UpgradeableMenu)menu).getToolbox().isPresent()) {
            this.widgets.add("toolbox", new ToolboxPanel(style, ((UpgradeableMenu)menu).getToolbox().getName()));
        }
    }

    private List<Component> getCompatibleUpgrades() {
        ArrayList<Component> list = new ArrayList<Component>();
        list.add((Component)GuiText.CompatibleUpgrades.text());
        list.addAll(Upgrades.getTooltipLinesForMachine(((UpgradeableMenu)this.menu).getUpgrades().getUpgradableItem()));
        return list;
    }
}

