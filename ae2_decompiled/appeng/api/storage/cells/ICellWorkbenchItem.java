/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.ItemStack
 */
package appeng.api.storage.cells;

import appeng.api.config.FuzzyMode;
import appeng.api.upgrades.IUpgradeableItem;
import appeng.util.ConfigInventory;
import net.minecraft.world.item.ItemStack;

public interface ICellWorkbenchItem
extends IUpgradeableItem {
    default public boolean isEditable(ItemStack is) {
        return this.getConfigInventory(is).size() > 0 || this.getUpgrades(is).size() > 0;
    }

    default public ConfigInventory getConfigInventory(ItemStack is) {
        return ConfigInventory.emptyTypes();
    }

    public FuzzyMode getFuzzyMode(ItemStack var1);

    public void setFuzzyMode(ItemStack var1, FuzzyMode var2);
}

