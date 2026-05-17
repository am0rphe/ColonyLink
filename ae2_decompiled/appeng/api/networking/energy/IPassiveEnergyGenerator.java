/*
 * Decompiled with CFR 0.152.
 */
package appeng.api.networking.energy;

import appeng.api.networking.IGridNodeService;

public interface IPassiveEnergyGenerator
extends IGridNodeService {
    public double getRate();

    public void setSuppressed(boolean var1);

    public boolean isSuppressed();
}

