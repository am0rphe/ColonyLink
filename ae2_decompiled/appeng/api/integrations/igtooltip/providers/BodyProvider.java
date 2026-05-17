/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.ApiStatus$Experimental
 *  org.jetbrains.annotations.ApiStatus$OverrideOnly
 */
package appeng.api.integrations.igtooltip.providers;

import appeng.api.integrations.igtooltip.TooltipBuilder;
import appeng.api.integrations.igtooltip.TooltipContext;
import org.jetbrains.annotations.ApiStatus;

@FunctionalInterface
@ApiStatus.Experimental
@ApiStatus.OverrideOnly
public interface BodyProvider<T> {
    public void buildTooltip(T var1, TooltipContext var2, TooltipBuilder var3);
}

