/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.ItemStack
 */
package appeng.api.upgrades;

import appeng.api.upgrades.IUpgradeInventory;
import net.minecraft.world.item.ItemStack;

@FunctionalInterface
public interface ItemUpgradesChanged {
    public void onUpgradesChanged(ItemStack var1, IUpgradeInventory var2);
}

