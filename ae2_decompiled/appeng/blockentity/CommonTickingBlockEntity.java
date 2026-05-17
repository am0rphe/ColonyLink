/*
 * Decompiled with CFR 0.152.
 */
package appeng.blockentity;

import appeng.blockentity.ClientTickingBlockEntity;
import appeng.blockentity.ServerTickingBlockEntity;

public interface CommonTickingBlockEntity
extends ServerTickingBlockEntity,
ClientTickingBlockEntity {
    @Override
    default public void serverTick() {
        this.commonTick();
    }

    @Override
    default public void clientTick() {
        this.commonTick();
    }

    public void commonTick();
}

