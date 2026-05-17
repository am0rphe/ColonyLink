/*
 * Decompiled with CFR 0.152.
 */
package appeng.parts.networking;

import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridNode;
import appeng.api.networking.pathing.ChannelMode;
import appeng.api.parts.IPart;
import appeng.me.GridNode;

public interface IUsedChannelProvider
extends IPart {
    default public int getUsedChannelsInfo() {
        int howMany = 0;
        IGridNode node = this.getGridNode();
        if (node != null && node.isActive()) {
            for (IGridConnection gc : node.getConnections()) {
                howMany = Math.max(gc.getUsedChannels(), howMany);
            }
        }
        return howMany;
    }

    default public int getMaxChannelsInfo() {
        IGridNode node = this.getGridNode();
        if (node instanceof GridNode) {
            GridNode gridNode = (GridNode)node;
            if (gridNode.getGrid().getPathingService().getChannelMode() == ChannelMode.INFINITE) {
                return -1;
            }
            return gridNode.getMaxChannels();
        }
        return 0;
    }
}

