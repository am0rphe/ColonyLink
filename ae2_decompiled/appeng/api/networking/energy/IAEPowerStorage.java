/*
 * Decompiled with CFR 0.152.
 */
package appeng.api.networking.energy;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.networking.IGridNodeService;
import appeng.api.networking.energy.IEnergySource;

public interface IAEPowerStorage
extends IEnergySource,
IGridNodeService {
    public double injectAEPower(double var1, Actionable var3);

    public double getAEMaxPower();

    public double getAECurrentPower();

    public boolean isAEPublicPowerStorage();

    public AccessRestriction getPowerFlow();

    default public int getPriority() {
        return 0;
    }
}

