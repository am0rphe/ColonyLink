/*
 * Decompiled with CFR 0.152.
 */
package appeng.api.networking.events;

import appeng.api.networking.IGridNode;
import appeng.api.networking.events.GridEvent;

public class GridCraftingCpuChange
extends GridEvent {
    public final IGridNode node;

    public GridCraftingCpuChange(IGridNode n) {
        this.node = n;
    }
}

