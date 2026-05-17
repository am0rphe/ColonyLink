/*
 * Decompiled with CFR 0.152.
 */
package appeng.api.storage;

import appeng.api.stacks.AEKey;
import appeng.api.storage.NoOpKeyFilter;

@FunctionalInterface
public interface AEKeyFilter {
    public static AEKeyFilter none() {
        return NoOpKeyFilter.INSTANCE;
    }

    public boolean matches(AEKey var1);
}

