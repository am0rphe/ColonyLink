/*
 * Decompiled with CFR 0.152.
 */
package appeng.api.networking.energy;

import appeng.api.config.Actionable;
import appeng.api.networking.IGridService;
import appeng.api.networking.energy.IEnergySource;

public interface IEnergyService
extends IGridService,
IEnergySource {
    public double getIdlePowerUsage();

    public double getChannelPowerUsage();

    public double getAvgPowerUsage();

    public double getAvgPowerInjection();

    public boolean isNetworkPowered();

    public double injectPower(double var1, Actionable var3);

    public double getStoredPower();

    public double getMaxStoredPower();

    public double getEnergyDemand(double var1);
}

