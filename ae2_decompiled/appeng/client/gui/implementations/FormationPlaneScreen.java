/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.entity.player.Inventory
 */
package appeng.client.gui.implementations;

import appeng.api.config.FuzzyMode;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.ServerSettingToggleButton;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.menu.implementations.FormationPlaneMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class FormationPlaneScreen
extends UpgradeableScreen<FormationPlaneMenu> {
    private final SettingToggleButton<FuzzyMode> fuzzyMode;
    private final SettingToggleButton<YesNo> placeMode = new ServerSettingToggleButton<YesNo>(Settings.PLACE_BLOCK, YesNo.YES);

    public FormationPlaneScreen(FormationPlaneMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
        this.addToLeftToolbar(this.placeMode);
        this.fuzzyMode = new ServerSettingToggleButton<FuzzyMode>(Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL);
        this.addToLeftToolbar(this.fuzzyMode);
        this.widgets.addOpenPriorityButton();
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();
        this.fuzzyMode.set(((FormationPlaneMenu)this.menu).getFuzzyMode());
        this.fuzzyMode.setVisibility(((FormationPlaneMenu)this.menu).supportsFuzzyMode());
        this.placeMode.set(((FormationPlaneMenu)this.menu).getPlaceMode());
    }
}

