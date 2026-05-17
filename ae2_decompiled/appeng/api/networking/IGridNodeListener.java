/*
 * Decompiled with CFR 0.152.
 */
package appeng.api.networking;

import appeng.api.networking.IGridNode;

public interface IGridNodeListener<T> {
    public void onSaveChanges(T var1, IGridNode var2);

    default public void onInWorldConnectionChanged(T nodeOwner, IGridNode node) {
    }

    default public void onOwnerChanged(T nodeOwner, IGridNode node) {
        this.onSaveChanges(nodeOwner, node);
    }

    default public void onGridChanged(T nodeOwner, IGridNode node) {
    }

    default public void onStateChanged(T nodeOwner, IGridNode node, State state) {
    }

    public static enum State {
        POWER,
        CHANNEL,
        GRID_BOOT;

    }
}

