/*
 * Decompiled with CFR 0.152.
 */
package appeng.api.storage;

import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeService;
import appeng.api.networking.IManagedGridNode;
import appeng.api.storage.IStorageMounts;

public interface IStorageProvider
extends IGridNodeService {
    public void mountInventories(IStorageMounts var1);

    public static void requestUpdate(IManagedGridNode managedNode) {
        IGridNode node = managedNode.getNode();
        if (node != null) {
            node.getGrid().getStorageService().refreshNodeStorageProvider(node);
        }
    }
}

