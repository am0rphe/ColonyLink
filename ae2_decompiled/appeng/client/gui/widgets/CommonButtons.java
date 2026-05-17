/*
 * Decompiled with CFR 0.152.
 */
package appeng.client.gui.widgets;

import appeng.api.config.PowerUnit;
import appeng.api.config.Settings;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.core.AEConfig;

public final class CommonButtons {
    private CommonButtons() {
    }

    public static SettingToggleButton<PowerUnit> togglePowerUnit() {
        return new SettingToggleButton<PowerUnit>(Settings.POWER_UNITS, AEConfig.instance().getSelectedEnergyUnit(), CommonButtons::togglePowerUnit);
    }

    private static void togglePowerUnit(SettingToggleButton<PowerUnit> button, boolean backwards) {
        AEConfig.instance().nextEnergyUnit(backwards);
        button.set(AEConfig.instance().getSelectedEnergyUnit());
    }
}

