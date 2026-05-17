/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.level.Level
 *  org.jetbrains.annotations.Nullable
 */
package appeng.api.crafting;

import appeng.api.crafting.PatternDetailsTooltip;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface InvalidPatternTooltipStrategy {
    public PatternDetailsTooltip getTooltip(ItemStack var1, Level var2, @Nullable Exception var3, TooltipFlag var4);
}

