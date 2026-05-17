/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.ItemStack
 *  org.jetbrains.annotations.ApiStatus$Experimental
 *  org.jetbrains.annotations.ApiStatus$OverrideOnly
 *  org.jetbrains.annotations.Nullable
 */
package appeng.api.integrations.igtooltip.providers;

import appeng.api.integrations.igtooltip.TooltipContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
@ApiStatus.Experimental
@ApiStatus.OverrideOnly
public interface IconProvider<T> {
    @Nullable
    public ItemStack getIcon(T var1, TooltipContext var2);
}

