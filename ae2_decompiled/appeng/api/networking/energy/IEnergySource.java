/*
 * Decompiled with CFR 0.152.
 */
package appeng.api.networking.energy;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.energy.EmptyEnergySource;

public interface IEnergySource {
    public static IEnergySource empty() {
        return EmptyEnergySource.INSTANCE;
    }

    public double extractAEPower(double var1, Actionable var3, PowerMultiplier var4);
}

