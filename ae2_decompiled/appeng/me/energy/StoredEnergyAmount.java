/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.Mth
 */
package appeng.me.energy;

import appeng.api.networking.events.GridPowerStorageStateChanged;
import net.minecraft.util.Mth;

public final class StoredEnergyAmount {
    public static final double MIN_AMOUNT = 1.0E-6;
    public static final double MAX_MAXIMUM = 9.22337203685477E14;
    private final double provideThreshold;
    private final double receiveThreshold;
    private double maximum;
    private final EventEmitter eventEmitter;
    private double stored;
    private boolean isReceiving = true;
    private boolean isProviding = true;

    public StoredEnergyAmount(double stored, double maximum, EventEmitter eventEmitter) {
        this(stored, 0.1, 0.1, maximum, eventEmitter);
    }

    public StoredEnergyAmount(double stored, double provideThreshold, double receiveThreshold, double maximum, EventEmitter eventEmitter) {
        this.provideThreshold = provideThreshold;
        this.receiveThreshold = receiveThreshold;
        this.maximum = maximum;
        this.eventEmitter = eventEmitter;
        this.stored = stored;
    }

    public double getAmount() {
        return this.stored;
    }

    public double getMaximum() {
        return this.maximum;
    }

    public double insert(double amount, boolean commit) {
        if (amount < 1.0E-6) {
            return 0.0;
        }
        double inserted = Math.min(amount, this.maximum - this.stored);
        if (commit) {
            this.setStored(this.stored + inserted);
        }
        return inserted;
    }

    public double extract(double amount, boolean commit) {
        if (amount < 1.0E-6) {
            return 0.0;
        }
        double extracted = Math.min(amount, this.stored);
        if (commit) {
            this.setStored(this.stored - extracted);
        }
        return extracted;
    }

    public void setStored(double amount) {
        if (amount < 1.0E-6) {
            amount = 0.0;
        }
        this.stored = Mth.clamp((double)amount, (double)0.0, (double)this.maximum);
        this.sendEvents();
    }

    public void setMaximum(double maximum) {
        this.maximum = Math.min(9.22337203685477E14, maximum);
        this.stored = Mth.clamp((double)this.stored, (double)0.0, (double)maximum);
        this.sendEvents();
    }

    public double remainingCapacity() {
        return this.maximum - this.stored;
    }

    private boolean canProvide() {
        return this.stored >= this.provideThreshold;
    }

    private boolean canReceive() {
        return this.remainingCapacity() >= this.receiveThreshold;
    }

    private void sendEvents() {
        boolean wasProviding = this.isProviding;
        boolean wasReceiving = this.isReceiving;
        this.isProviding = this.canProvide();
        this.isReceiving = this.canReceive();
        if (!wasProviding && this.isProviding) {
            this.eventEmitter.emitEvent(GridPowerStorageStateChanged.PowerEventType.PROVIDE_POWER);
        }
        if (!wasReceiving && this.isReceiving) {
            this.eventEmitter.emitEvent(GridPowerStorageStateChanged.PowerEventType.RECEIVE_POWER);
        }
    }

    @FunctionalInterface
    public static interface EventEmitter {
        public void emitEvent(GridPowerStorageStateChanged.PowerEventType var1);
    }
}

