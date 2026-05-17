/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package appeng.menu.me.common;

import appeng.api.stacks.AEKey;
import org.jetbrains.annotations.Nullable;

public class GridInventoryEntry {
    private final long serial;
    @Nullable
    private final AEKey what;
    private final long storedAmount;
    private final long requestableAmount;
    private final boolean craftable;

    public GridInventoryEntry(long serial, @Nullable AEKey what, long storedAmount, long requestableAmount, boolean craftable) {
        this.serial = serial;
        this.what = what;
        this.storedAmount = storedAmount;
        this.requestableAmount = requestableAmount;
        this.craftable = craftable;
    }

    public long getSerial() {
        return this.serial;
    }

    @Nullable
    public AEKey getWhat() {
        return this.what;
    }

    public long getStoredAmount() {
        return this.storedAmount;
    }

    public long getRequestableAmount() {
        return this.requestableAmount;
    }

    public boolean isCraftable() {
        return this.craftable;
    }

    public boolean isMeaningful() {
        return this.storedAmount > 0L || this.requestableAmount > 0L || this.craftable;
    }
}

