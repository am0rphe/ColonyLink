/*
 * Decompiled with CFR 0.152.
 */
package appeng.api.networking.ticking;

import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridService;

public interface ITickManager
extends IGridService {
    public boolean alertDevice(IGridNode var1);

    public boolean sleepDevice(IGridNode var1);

    public boolean wakeDevice(IGridNode var1);
}

