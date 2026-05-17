/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.inventory.MenuType
 */
package appeng.menu.implementations;

import appeng.api.config.Settings;
import appeng.api.util.IConfigManager;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.implementations.UpgradeableMenu;
import appeng.parts.automation.EnergyLevelEmitterPart;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

public class EnergyLevelEmitterMenu
extends UpgradeableMenu<EnergyLevelEmitterPart> {
    private static final String ACTION_SET_REPORTING_VALUE = "setReportingValue";
    public static final MenuType<EnergyLevelEmitterMenu> TYPE = MenuTypeBuilder.create(EnergyLevelEmitterMenu::new, EnergyLevelEmitterPart.class).withInitialData((host, buffer) -> buffer.writeVarLong(host.getReportingValue()), (host, menu, buffer) -> {
        menu.reportingValue = buffer.readVarLong();
    }).build("energy_level_emitter");
    private long reportingValue;

    public EnergyLevelEmitterMenu(int id, Inventory ip, EnergyLevelEmitterPart host) {
        super((MenuType<?>)TYPE, id, ip, host);
        this.registerClientAction(ACTION_SET_REPORTING_VALUE, Long.class, this::setReportingValue);
    }

    public long getReportingValue() {
        return this.reportingValue;
    }

    public void setReportingValue(long reportingValue) {
        if (this.isClientSide()) {
            if (reportingValue != this.reportingValue) {
                this.reportingValue = reportingValue;
                this.sendClientAction(ACTION_SET_REPORTING_VALUE, reportingValue);
            }
        } else {
            ((EnergyLevelEmitterPart)this.getHost()).setReportingValue(reportingValue);
        }
    }

    @Override
    protected void loadSettingsFromHost(IConfigManager cm) {
        this.setRedStoneMode(cm.getSetting(Settings.REDSTONE_EMITTER));
    }
}

