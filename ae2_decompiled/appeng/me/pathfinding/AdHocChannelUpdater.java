/*
 * Decompiled with CFR 0.152.
 */
package appeng.me.pathfinding;

import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridConnectionVisitor;
import appeng.api.networking.IGridNode;
import appeng.me.GridConnection;
import appeng.me.GridNode;

public class AdHocChannelUpdater
implements IGridConnectionVisitor {
    private final int usedChannels;

    public AdHocChannelUpdater(int used) {
        this.usedChannels = used;
    }

    @Override
    public boolean visitNode(IGridNode n) {
        GridNode gn = (GridNode)n;
        gn.setAdHocChannels(this.usedChannels);
        return true;
    }

    @Override
    public void visitConnection(IGridConnection gcc) {
        GridConnection gc = (GridConnection)gcc;
        gc.setAdHocChannels(this.usedChannels);
    }
}

