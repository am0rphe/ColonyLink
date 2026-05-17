/*
 * Decompiled with CFR 0.152.
 */
package appeng.api.networking.storage;

import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridService;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.IStorageProvider;
import appeng.api.storage.MEStorage;

public interface IStorageService
extends IGridService {
    public MEStorage getInventory();

    public KeyCounter getCachedInventory();

    public void addGlobalStorageProvider(IStorageProvider var1);

    public void removeGlobalStorageProvider(IStorageProvider var1);

    public void refreshNodeStorageProvider(IGridNode var1);

    public void refreshGlobalStorageProvider(IStorageProvider var1);

    public void invalidateCache();
}

