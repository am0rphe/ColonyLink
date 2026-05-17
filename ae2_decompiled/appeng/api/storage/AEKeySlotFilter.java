/*
 * Decompiled with CFR 0.152.
 */
package appeng.api.storage;

import appeng.api.stacks.AEKey;

@FunctionalInterface
public interface AEKeySlotFilter {
    public boolean isAllowed(int var1, AEKey var2);
}

