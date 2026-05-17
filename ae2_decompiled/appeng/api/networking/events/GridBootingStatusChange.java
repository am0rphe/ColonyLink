/*
 * Decompiled with CFR 0.152.
 */
package appeng.api.networking.events;

import appeng.api.networking.events.GridEvent;

public class GridBootingStatusChange
extends GridEvent {
    private final boolean booting;

    public GridBootingStatusChange(boolean booting) {
        this.booting = booting;
    }

    public boolean isBooting() {
        return this.booting;
    }
}

