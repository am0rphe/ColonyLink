/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.ApiStatus$Experimental
 *  org.jetbrains.annotations.ApiStatus$NonExtendable
 */
package appeng.api.behaviors;

import appeng.api.config.Actionable;
import appeng.api.stacks.AEKey;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
@ApiStatus.NonExtendable
public interface PickupSink {
    public long insert(AEKey var1, long var2, Actionable var4);
}

