/*
 * Decompiled with CFR 0.152.
 */
package appeng.api.networking.events;

import appeng.api.networking.IGridNode;
import appeng.api.networking.events.GridEvent;

public class GridPowerIdleChange
extends GridEvent {
    public final IGridNode node;

    public GridPowerIdleChange(IGridNode nodeThatChanged) {
        this.node = nodeThatChanged;
    }
}

