/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  org.jetbrains.annotations.ApiStatus$Experimental
 *  org.jetbrains.annotations.ApiStatus$OverrideOnly
 *  org.jetbrains.annotations.Nullable
 */
package appeng.api.integrations.igtooltip.providers;

import appeng.api.integrations.igtooltip.TooltipContext;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
@ApiStatus.Experimental
@ApiStatus.OverrideOnly
public interface NameProvider<T> {
    @Nullable
    public Component getName(T var1, TooltipContext var2);
}

