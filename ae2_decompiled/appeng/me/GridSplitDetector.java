/*
 * Decompiled with CFR 0.152.
 */
package appeng.me;

import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridVisitor;

class GridSplitDetector
implements IGridVisitor {
    private final IGridNode pivot;
    private boolean pivotFound;

    public GridSplitDetector(IGridNode pivot) {
        this.pivot = pivot;
    }

    @Override
    public boolean visitNode(IGridNode n) {
        if (n == this.pivot) {
            this.setPivotFound(true);
        }
        return !this.isPivotFound();
    }

    public boolean isPivotFound() {
        return this.pivotFound;
    }

    private void setPivotFound(boolean pivotFound) {
        this.pivotFound = pivotFound;
    }
}

