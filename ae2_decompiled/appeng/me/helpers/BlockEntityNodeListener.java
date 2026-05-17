/*
 * Decompiled with CFR 0.152.
 */
package appeng.me.helpers;

import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.me.helpers.IGridConnectedBlockEntity;

public class BlockEntityNodeListener<T extends IGridConnectedBlockEntity>
implements IGridNodeListener<T> {
    public static final BlockEntityNodeListener<IGridConnectedBlockEntity> INSTANCE = new BlockEntityNodeListener();

    @Override
    public void onSaveChanges(T nodeOwner, IGridNode node) {
        nodeOwner.saveChanges();
    }

    @Override
    public void onStateChanged(T nodeOwner, IGridNode node, IGridNodeListener.State state) {
        nodeOwner.onMainNodeStateChanged(state);
    }
}

