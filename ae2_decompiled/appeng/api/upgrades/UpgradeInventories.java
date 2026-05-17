/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 */
package appeng.api.upgrades;

import appeng.api.upgrades.EmptyUpgradeInventory;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.ItemUpgradeInventory;
import appeng.api.upgrades.ItemUpgradesChanged;
import appeng.api.upgrades.MachineUpgradeInventory;
import appeng.api.upgrades.MachineUpgradesChanged;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public final class UpgradeInventories {
    private UpgradeInventories() {
    }

    public static IUpgradeInventory empty() {
        return EmptyUpgradeInventory.INSTANCE;
    }

    public static IUpgradeInventory forMachine(ItemLike machineType, int maxUpgrades, MachineUpgradesChanged changeCallback) {
        return new MachineUpgradeInventory(machineType, maxUpgrades, changeCallback);
    }

    public static IUpgradeInventory forItem(ItemStack stack, int maxUpgrades) {
        return new ItemUpgradeInventory(stack, maxUpgrades, null);
    }

    public static IUpgradeInventory forItem(ItemStack stack, int maxUpgrades, ItemUpgradesChanged changeCallback) {
        return new ItemUpgradeInventory(stack, maxUpgrades, changeCallback);
    }
}

