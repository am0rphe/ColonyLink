/*
 * Decompiled with CFR 0.152.
 */
package appeng.me;

import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridVisitor;
import appeng.me.Grid;
import appeng.me.GridNode;

public class GridPropagator
implements IGridVisitor {
    private final Grid g;

    public GridPropagator(Grid g) {
        this.g = g;
    }

    @Override
    public boolean visitNode(IGridNode n) {
        GridNode gn = (GridNode)n;
        if (gn.getMyGrid() != this.g || this.g.getPivot() == n) {
            gn.setGrid(this.g);
            return true;
        }
        return false;
    }
}

