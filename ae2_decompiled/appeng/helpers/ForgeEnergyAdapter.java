/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.neoforged.neoforge.energy.IEnergyStorage
 */
package appeng.helpers;

import appeng.api.config.Actionable;
import appeng.api.config.PowerUnit;
import appeng.blockentity.powersink.IExternalPowerSink;
import net.neoforged.neoforge.energy.IEnergyStorage;

public class ForgeEnergyAdapter
implements IEnergyStorage {
    private final IExternalPowerSink sink;

    public ForgeEnergyAdapter(IExternalPowerSink sink) {
        this.sink = sink;
    }

    public final int receiveEnergy(int maxReceive, boolean simulate) {
        double offered = maxReceive;
        double overflow = this.sink.injectExternalPower(PowerUnit.FE, offered, simulate ? Actionable.SIMULATE : Actionable.MODULATE);
        return (int)((double)maxReceive - overflow);
    }

    public final int getEnergyStored() {
        return (int)Math.floor(PowerUnit.AE.convertTo(PowerUnit.FE, this.sink.getAECurrentPower()));
    }

    public final int getMaxEnergyStored() {
        return (int)Math.floor(PowerUnit.AE.convertTo(PowerUnit.FE, this.sink.getAEMaxPower()));
    }

    public int extractEnergy(int maxExtract, boolean simulate) {
        return 0;
    }

    public boolean canExtract() {
        return false;
    }

    public boolean canReceive() {
        return true;
    }
}

