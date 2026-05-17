/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.entity.player.Inventory
 */
package appeng.client.gui.implementations;

import appeng.api.config.FuzzyMode;
import appeng.api.config.RedstoneMode;
import appeng.api.config.SchedulingMode;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.storage.ISubMenuHost;
import appeng.api.util.KeyTypeSelectionHost;
import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.KeyTypeSelectionButton;
import appeng.client.gui.widgets.ServerSettingToggleButton;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.core.definitions.AEItems;
import appeng.core.localization.GuiText;
import appeng.menu.implementations.IOBusMenu;
import appeng.parts.automation.IOBusPart;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class IOBusScreen
extends UpgradeableScreen<IOBusMenu> {
    private final SettingToggleButton<RedstoneMode> redstoneMode;
    private final SettingToggleButton<FuzzyMode> fuzzyMode;
    private final SettingToggleButton<YesNo> craftMode;
    private final SettingToggleButton<SchedulingMode> schedulingMode;

    public IOBusScreen(IOBusMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
        if (menu.getHost() instanceof KeyTypeSelectionHost) {
            this.addToLeftToolbar(KeyTypeSelectionButton.create(this, (ISubMenuHost)menu.getHost(), (Component)GuiText.ConfigureImportedTypes.text()));
        }
        this.redstoneMode = new ServerSettingToggleButton<RedstoneMode>(Settings.REDSTONE_CONTROLLED, RedstoneMode.IGNORE);
        this.addToLeftToolbar(this.redstoneMode);
        this.fuzzyMode = new ServerSettingToggleButton<FuzzyMode>(Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL);
        this.addToLeftToolbar(this.fuzzyMode);
        if (((IOBusPart)menu.getHost()).getConfigManager().hasSetting(Settings.CRAFT_ONLY)) {
            this.craftMode = new ServerSettingToggleButton<YesNo>(Settings.CRAFT_ONLY, YesNo.NO);
            this.addToLeftToolbar(this.craftMode);
        } else {
            this.craftMode = null;
        }
        if (((IOBusPart)menu.getHost()).getConfigManager().hasSetting(Settings.SCHEDULING_MODE)) {
            this.schedulingMode = new ServerSettingToggleButton<SchedulingMode>(Settings.SCHEDULING_MODE, SchedulingMode.DEFAULT);
            this.addToLeftToolbar(this.schedulingMode);
        } else {
            this.schedulingMode = null;
        }
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();
        this.redstoneMode.set(((IOBusMenu)this.menu).getRedStoneMode());
        this.redstoneMode.setVisibility(((IOBusMenu)this.menu).hasUpgrade(AEItems.REDSTONE_CARD));
        this.fuzzyMode.set(((IOBusMenu)this.menu).getFuzzyMode());
        this.fuzzyMode.setVisibility(((IOBusMenu)this.menu).hasUpgrade(AEItems.FUZZY_CARD));
        if (this.craftMode != null) {
            this.craftMode.set(((IOBusMenu)this.menu).getCraftingMode());
            this.craftMode.setVisibility(((IOBusMenu)this.menu).hasUpgrade(AEItems.CRAFTING_CARD));
        }
        if (this.schedulingMode != null) {
            this.schedulingMode.set(((IOBusMenu)this.menu).getSchedulingMode());
        }
    }
}

