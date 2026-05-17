/*
 * Decompiled with CFR 0.152.
 */
package appeng.api.storage.cells;

import appeng.api.storage.MEStorage;
import appeng.api.storage.cells.CellState;

public interface StorageCell
extends MEStorage {
    public CellState getStatus();

    public double getIdleDrain();

    default public boolean canFitInsideCell() {
        return true;
    }

    public void persist();
}

