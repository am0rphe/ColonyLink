/*
 * Decompiled with CFR 0.152.
 */
package appeng.blockentity.powersink;

import appeng.api.config.Actionable;
import appeng.api.config.PowerUnit;
import appeng.api.networking.energy.IAEPowerStorage;

public interface IExternalPowerSink
extends IAEPowerStorage {
    public double injectExternalPower(PowerUnit var1, double var2, Actionable var4);

    public double getExternalPowerDemand(PowerUnit var1, double var2);
}

