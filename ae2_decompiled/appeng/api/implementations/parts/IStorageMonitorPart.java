/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package appeng.api.implementations.parts;

import appeng.api.implementations.parts.IMonitorPart;
import appeng.api.parts.IPart;
import appeng.api.stacks.AEKey;
import appeng.api.util.INetworkToolAware;
import org.jetbrains.annotations.Nullable;

public interface IStorageMonitorPart
extends IMonitorPart,
IPart,
INetworkToolAware {
    @Nullable
    public AEKey getDisplayed();

    public long getAmount();

    public boolean isLocked();
}

