/*
 * Decompiled with CFR 0.152.
 */
package appeng.me.storage;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.storage.cells.CellState;
import appeng.api.storage.cells.StorageCell;
import appeng.me.storage.MEInventoryHandler;

public class DriveWatcher
extends MEInventoryHandler {
    private CellState oldStatus = CellState.EMPTY;
    private final Runnable activityCallback;

    public DriveWatcher(StorageCell i, Runnable activityCallback) {
        super(i);
        this.activityCallback = activityCallback;
        this.oldStatus = this.getStatus();
    }

    public CellState getStatus() {
        return this.getCell().getStatus();
    }

    public StorageCell getCell() {
        return (StorageCell)this.getDelegate();
    }

    @Override
    public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
        CellState newStatus;
        long inserted = super.insert(what, amount, mode, source);
        if (mode == Actionable.MODULATE && inserted > 0L && (newStatus = this.getStatus()) != this.oldStatus) {
            this.activityCallback.run();
            this.oldStatus = newStatus;
        }
        return inserted;
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        CellState newStatus;
        long extracted = super.extract(what, amount, mode, source);
        if (mode == Actionable.MODULATE && extracted > 0L && (newStatus = this.getStatus()) != this.oldStatus) {
            this.activityCallback.run();
            this.oldStatus = newStatus;
        }
        return extracted;
    }
}

