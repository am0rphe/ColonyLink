/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.world.level.ItemLike
 */
package appeng.api.upgrades;

import appeng.api.inventories.InternalInventory;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ItemLike;

public interface IUpgradeInventory
extends InternalInventory {
    public ItemLike getUpgradableItem();

    default public boolean isInstalled(ItemLike upgradeCard) {
        return this.getInstalledUpgrades(upgradeCard) > 0;
    }

    public int getInstalledUpgrades(ItemLike var1);

    public int getMaxInstalled(ItemLike var1);

    public void readFromNBT(CompoundTag var1, String var2, HolderLookup.Provider var3);

    public void writeToNBT(CompoundTag var1, String var2, HolderLookup.Provider var3);
}

