/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.ApiStatus$Experimental
 *  org.jetbrains.annotations.ApiStatus$OverrideOnly
 *  org.jetbrains.annotations.Nullable
 */
package appeng.api.integrations.igtooltip.providers;

import appeng.api.integrations.igtooltip.TooltipContext;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
@ApiStatus.Experimental
@ApiStatus.OverrideOnly
public interface ModNameProvider<T> {
    @Nullable
    public String getModName(T var1, TooltipContext var2);
}

