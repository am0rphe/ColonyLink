/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.ItemStack
 */
package appeng.util.inv;

import appeng.api.inventories.BaseInternalInventory;
import appeng.api.inventories.InternalInventory;
import appeng.util.inv.filter.IAEItemFilter;
import java.util.Objects;
import net.minecraft.world.item.ItemStack;

public class FilteredInternalInventory
extends BaseInternalInventory {
    private final InternalInventory delegate;
    private final IAEItemFilter filter;

    public FilteredInternalInventory(InternalInventory delegate, IAEItemFilter filter) {
        this.delegate = Objects.requireNonNull(delegate);
        this.filter = Objects.requireNonNull(filter);
    }

    @Override
    public void setItemDirect(int slot, ItemStack stack) {
        this.delegate.setItemDirect(slot, stack);
    }

    @Override
    public int size() {
        return this.delegate.size();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return this.delegate.getStackInSlot(slot);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (!this.filter.allowInsert(this.delegate, slot, stack)) {
            return stack;
        }
        return this.delegate.insertItem(slot, stack, simulate);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (!this.filter.allowExtract(this.delegate, slot, amount)) {
            return ItemStack.EMPTY;
        }
        return this.delegate.extractItem(slot, amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {
        return this.delegate.getSlotLimit(slot);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        if (!this.filter.allowInsert(this.delegate, slot, stack)) {
            return false;
        }
        return this.delegate.isItemValid(slot, stack);
    }

    @Override
    public void sendChangeNotification(int slot) {
        this.delegate.sendChangeNotification(slot);
    }
}

