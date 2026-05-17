/*
 * Decompiled with CFR 0.152.
 */
package appeng.api.networking.energy;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.energy.IEnergySource;

final class EmptyEnergySource
implements IEnergySource {
    static final IEnergySource INSTANCE = new EmptyEnergySource();

    EmptyEnergySource() {
    }

    @Override
    public double extractAEPower(double amt, Actionable mode, PowerMultiplier usePowerMultiplier) {
        return 0.0;
    }
}

