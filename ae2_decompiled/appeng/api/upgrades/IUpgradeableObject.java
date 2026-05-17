/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.level.ItemLike
 */
package appeng.api.upgrades;

import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.UpgradeInventories;
import net.minecraft.world.level.ItemLike;

public interface IUpgradeableObject {
    default public IUpgradeInventory getUpgrades() {
        return UpgradeInventories.empty();
    }

    default public int getInstalledUpgrades(ItemLike upgradeCard) {
        return this.getUpgrades().getInstalledUpgrades(upgradeCard);
    }

    default public boolean isUpgradedWith(ItemLike upgradeCard) {
        return this.getUpgrades().isInstalled(upgradeCard);
    }
}

