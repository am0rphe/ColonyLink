/*
 * Decompiled with CFR 0.152.
 */
package appeng.api.networking.events;

import appeng.api.networking.energy.IAEPowerStorage;
import appeng.api.networking.events.GridEvent;

public class GridPowerStorageStateChanged
extends GridEvent {
    public final IAEPowerStorage storage;
    public final PowerEventType type;

    public GridPowerStorageStateChanged(IAEPowerStorage storage, PowerEventType type) {
        this.storage = storage;
        this.type = type;
    }

    public static enum PowerEventType {
        RECEIVE_POWER,
        PROVIDE_POWER;

    }
}

