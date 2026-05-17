/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 */
package appeng.api.implementations.menuobjects;

import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableItem;
import appeng.api.upgrades.UpgradeInventories;
import appeng.items.contents.StackDependentSupplier;
import appeng.util.inv.SupplierInternalInventory;
import java.util.function.Supplier;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public final class DelegateItemUpgradeInventory
extends SupplierInternalInventory<IUpgradeInventory>
implements IUpgradeInventory {
    public DelegateItemUpgradeInventory(Supplier<ItemStack> stackSupplier) {
        super(new StackDependentSupplier<IUpgradeInventory>(stackSupplier, DelegateItemUpgradeInventory::inventoryFromStack));
    }

    @Override
    public ItemLike getUpgradableItem() {
        return ((IUpgradeInventory)this.getDelegate()).getUpgradableItem();
    }

    @Override
    public int getInstalledUpgrades(ItemLike u) {
        return ((IUpgradeInventory)this.getDelegate()).getInstalledUpgrades(u);
    }

    @Override
    public int getMaxInstalled(ItemLike u) {
        return ((IUpgradeInventory)this.getDelegate()).getMaxInstalled(u);
    }

    @Override
    public void readFromNBT(CompoundTag data, String subtag, HolderLookup.Provider registries) {
        ((IUpgradeInventory)this.getDelegate()).readFromNBT(data, subtag, registries);
    }

    @Override
    public void writeToNBT(CompoundTag data, String subtag, HolderLookup.Provider registries) {
        ((IUpgradeInventory)this.getDelegate()).writeToNBT(data, subtag, registries);
    }

    private static IUpgradeInventory inventoryFromStack(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof IUpgradeableItem) {
            IUpgradeableItem upgradeableItem = (IUpgradeableItem)item;
            return upgradeableItem.getUpgrades(stack);
        }
        return UpgradeInventories.empty();
    }
}

