/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.ApiStatus$NonExtendable
 */
package appeng.api.networking;

import appeng.api.stacks.AEKey;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface IStackWatcher {
    public void setWatchAll(boolean var1);

    public void add(AEKey var1);

    public void remove(AEKey var1);

    public void reset();
}

