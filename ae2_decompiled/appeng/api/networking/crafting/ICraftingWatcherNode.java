/*
 * Decompiled with CFR 0.152.
 */
package appeng.api.networking.crafting;

import appeng.api.networking.IGridNodeService;
import appeng.api.networking.IStackWatcher;
import appeng.api.stacks.AEKey;

public interface ICraftingWatcherNode
extends IGridNodeService {
    public void updateWatcher(IStackWatcher var1);

    public void onRequestChange(AEKey var1);

    public void onCraftableChange(AEKey var1);
}

