/*
 * Decompiled with CFR 0.152.
 */
package appeng.api.storage;

import appeng.api.stacks.AEKey;
import appeng.api.storage.AEKeyFilter;

class NoOpKeyFilter
implements AEKeyFilter {
    static NoOpKeyFilter INSTANCE = new NoOpKeyFilter();

    NoOpKeyFilter() {
    }

    @Override
    public boolean matches(AEKey what) {
        return true;
    }
}

