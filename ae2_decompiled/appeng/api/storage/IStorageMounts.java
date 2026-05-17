/*
 * Decompiled with CFR 0.152.
 */
package appeng.api.storage;

import appeng.api.storage.MEStorage;

public interface IStorageMounts {
    public static final int DEFAULT_PRIORITY = 0;

    default public void mount(MEStorage inventory) {
        this.mount(inventory, 0);
    }

    public void mount(MEStorage var1, int var2);
}

