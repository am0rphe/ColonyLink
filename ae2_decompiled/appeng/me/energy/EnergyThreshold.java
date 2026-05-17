/*
 * Decompiled with CFR 0.152.
 */
package appeng.me.energy;

import appeng.api.networking.energy.IEnergyWatcher;

public class EnergyThreshold
implements Comparable<EnergyThreshold> {
    private final double threshold;
    private final IEnergyWatcher watcher;
    private final int watcherHash;

    public EnergyThreshold(double lim, IEnergyWatcher watcher) {
        this.threshold = lim;
        this.watcher = watcher;
        this.watcherHash = watcher.hashCode();
    }

    public EnergyThreshold(double lim, int bound) {
        this.threshold = lim;
        this.watcher = null;
        this.watcherHash = bound;
    }

    public IEnergyWatcher getEnergyWatcher() {
        return this.watcher;
    }

    @Override
    public int compareTo(EnergyThreshold o) {
        int a = Double.compare(this.threshold, o.threshold);
        if (a == 0) {
            return Integer.compare(this.watcherHash, o.watcherHash);
        }
        return a;
    }

    public int hashCode() {
        int prime = 31;
        int result = 1;
        long temp = Double.doubleToLongBits(this.threshold);
        result = 31 * result + (int)(temp ^ temp >>> 32);
        result = 31 * result + (this.watcher == null ? 0 : this.watcher.hashCode());
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        EnergyThreshold other = (EnergyThreshold)obj;
        if (Double.doubleToLongBits(this.threshold) != Double.doubleToLongBits(other.threshold)) {
            return false;
        }
        return !(this.watcher == null ? other.watcher != null : !this.watcher.equals(other.watcher));
    }
}

