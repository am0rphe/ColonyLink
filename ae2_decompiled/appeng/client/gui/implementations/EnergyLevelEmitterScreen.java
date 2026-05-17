/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.entity.player.Inventory
 */
package appeng.client.gui.implementations;

import appeng.api.config.RedstoneMode;
import appeng.api.config.Settings;
import appeng.client.gui.NumberEntryType;
import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.NumberEntryWidget;
import appeng.client.gui.widgets.ServerSettingToggleButton;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.menu.implementations.EnergyLevelEmitterMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class EnergyLevelEmitterScreen
extends UpgradeableScreen<EnergyLevelEmitterMenu> {
    private final SettingToggleButton<RedstoneMode> redstoneMode = new ServerSettingToggleButton<RedstoneMode>(Settings.REDSTONE_EMITTER, RedstoneMode.LOW_SIGNAL);
    private final NumberEntryWidget level;

    public EnergyLevelEmitterScreen(EnergyLevelEmitterMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
        this.addToLeftToolbar(this.redstoneMode);
        this.level = this.widgets.addNumberEntryWidget("level", NumberEntryType.ENERGY);
        this.level.setTextFieldStyle(style.getWidget("levelInput"));
        this.level.setLongValue(menu.getReportingValue());
        this.level.setOnChange(this::saveReportingValue);
        this.level.setOnConfirm(() -> ((EnergyLevelEmitterScreen)this).onClose());
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();
        this.redstoneMode.active = true;
        this.redstoneMode.set(((EnergyLevelEmitterMenu)this.menu).getRedStoneMode());
    }

    private void saveReportingValue() {
        this.level.getLongValue().ifPresent(((EnergyLevelEmitterMenu)this.menu)::setReportingValue);
    }
}

