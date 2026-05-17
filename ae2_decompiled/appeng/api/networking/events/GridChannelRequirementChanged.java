/*
 * Decompiled with CFR 0.152.
 */
package appeng.api.networking.events;

import appeng.api.networking.IGridNode;
import appeng.api.networking.events.GridEvent;

public class GridChannelRequirementChanged
extends GridEvent {
    public final IGridNode node;

    public GridChannelRequirementChanged(IGridNode n) {
        this.node = n;
    }
}

