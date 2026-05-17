/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 *  org.jetbrains.annotations.ApiStatus$NonExtendable
 */
package appeng.api.upgrades;

import appeng.api.upgrades.EmptyUpgradeInventory;
import appeng.api.upgrades.IUpgradeInventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface IUpgradeableItem
extends ItemLike {
    default public IUpgradeInventory getUpgrades(ItemStack stack) {
        return EmptyUpgradeInventory.INSTANCE;
    }
}

