/*
 * Decompiled with CFR 0.152.
 */
package appeng.api.networking.ticking;

import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeService;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;

public interface IGridTickable
extends IGridNodeService {
    public TickingRequest getTickingRequest(IGridNode var1);

    public TickRateModulation tickingRequest(IGridNode var1, int var2);
}

