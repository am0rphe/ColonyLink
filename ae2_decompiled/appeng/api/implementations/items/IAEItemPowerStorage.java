/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.ItemStack
 */
package appeng.api.implementations.items;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import net.minecraft.world.item.ItemStack;

public interface IAEItemPowerStorage {
    public double injectAEPower(ItemStack var1, double var2, Actionable var4);

    public double extractAEPower(ItemStack var1, double var2, Actionable var4);

    public double getAEMaxPower(ItemStack var1);

    public double getAECurrentPower(ItemStack var1);

    public AccessRestriction getPowerFlow(ItemStack var1);

    public double getChargeRate(ItemStack var1);
}

