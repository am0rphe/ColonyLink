/*
 * Decompiled with CFR 0.152.
 */
package appeng.api.networking.storage;

import appeng.api.networking.IGridNodeService;
import appeng.api.networking.IStackWatcher;
import appeng.api.stacks.AEKey;

public interface IStorageWatcherNode
extends IGridNodeService {
    public void updateWatcher(IStackWatcher var1);

    public void onStackChange(AEKey var1, long var2);
}

