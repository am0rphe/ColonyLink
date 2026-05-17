/*
 * Decompiled with CFR 0.152.
 */
package appeng.api.networking.energy;

import appeng.api.networking.IGridNodeService;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.energy.IEnergyWatcher;

public interface IEnergyWatcherNode
extends IGridNodeService {
    public void updateWatcher(IEnergyWatcher var1);

    public void onThresholdPass(IEnergyService var1);
}

