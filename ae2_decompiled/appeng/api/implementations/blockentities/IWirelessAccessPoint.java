/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package appeng.api.implementations.blockentities;

import appeng.api.networking.IGrid;
import appeng.api.networking.security.IActionHost;
import appeng.api.util.DimensionalBlockPos;
import org.jetbrains.annotations.Nullable;

public interface IWirelessAccessPoint
extends IActionHost {
    public DimensionalBlockPos getLocation();

    public double getRange();

    public boolean isActive();

    @Nullable
    public IGrid getGrid();
}

