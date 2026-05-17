/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ItemLike
 *  net.neoforged.neoforge.items.IItemHandler
 *  net.neoforged.neoforge.items.wrapper.EmptyItemHandler
 */
package appeng.api.upgrades;

import appeng.api.upgrades.IUpgradeInventory;
import java.util.Collections;
import java.util.Iterator;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.EmptyItemHandler;

final class EmptyUpgradeInventory
implements IUpgradeInventory {
    public static final EmptyUpgradeInventory INSTANCE = new EmptyUpgradeInventory();

    EmptyUpgradeInventory() {
    }

    @Override
    public ItemLike getUpgradableItem() {
        return Items.AIR;
    }

    @Override
    public boolean isInstalled(ItemLike upgradeCard) {
        return false;
    }

    @Override
    public int getInstalledUpgrades(ItemLike u) {
        return 0;
    }

    @Override
    public int getMaxInstalled(ItemLike u) {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public IItemHandler toItemHandler() {
        return EmptyItemHandler.INSTANCE;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public ItemStack getStackInSlot(int slotIndex) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItemDirect(int slotIndex, ItemStack stack) {
    }

    @Override
    public Iterator<ItemStack> iterator() {
        return Collections.emptyIterator();
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return stack;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return ItemStack.EMPTY;
    }

    @Override
    public void readFromNBT(CompoundTag data, String subtag, HolderLookup.Provider registries) {
    }

    @Override
    public void writeToNBT(CompoundTag data, String subtag, HolderLookup.Provider registries) {
    }
}

