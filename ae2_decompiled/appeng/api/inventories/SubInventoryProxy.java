/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Preconditions
 *  net.minecraft.world.item.ItemStack
 */
package appeng.api.inventories;

import appeng.api.inventories.BaseInternalInventory;
import appeng.api.inventories.InternalInventory;
import com.google.common.base.Preconditions;
import net.minecraft.world.item.ItemStack;

final class SubInventoryProxy
extends BaseInternalInventory {
    private final InternalInventory delegate;
    private final int fromSlot;
    private final int toSlot;

    public SubInventoryProxy(InternalInventory delegate, int fromSlotInclusive, int toSlotExclusive) {
        Preconditions.checkArgument((fromSlotInclusive <= toSlotExclusive ? 1 : 0) != 0, (Object)"fromSlotInclusive <= toSlotExclusive");
        Preconditions.checkArgument((fromSlotInclusive >= 0 ? 1 : 0) != 0, (Object)"fromSlotInclusive >= 0");
        Preconditions.checkArgument((toSlotExclusive <= delegate.size() ? 1 : 0) != 0, (Object)"toSlotExclusive <= size()");
        this.delegate = delegate;
        this.fromSlot = fromSlotInclusive;
        this.toSlot = toSlotExclusive;
    }

    @Override
    public int size() {
        return this.toSlot - this.fromSlot;
    }

    private int translateSlot(int slotIndex) {
        Preconditions.checkArgument((slotIndex >= 0 ? 1 : 0) != 0, (Object)"slotIndex >= 0");
        Preconditions.checkArgument((slotIndex < this.size() ? 1 : 0) != 0, (Object)"slotIndex < size()");
        return slotIndex + this.fromSlot;
    }

    @Override
    public ItemStack getStackInSlot(int slotIndex) {
        return this.delegate.getStackInSlot(this.translateSlot(slotIndex));
    }

    @Override
    public void setItemDirect(int slotIndex, ItemStack stack) {
        this.delegate.setItemDirect(this.translateSlot(slotIndex), stack);
    }

    @Override
    public InternalInventory getSubInventory(int fromSlotInclusive, int toSlotExclusive) {
        Preconditions.checkArgument((toSlotExclusive >= 0 ? 1 : 0) != 0, (Object)"toSlotExclusive >= 0");
        Preconditions.checkArgument((toSlotExclusive <= this.size() ? 1 : 0) != 0, (Object)"toSlotExclusive <= size()");
        return this.delegate.getSubInventory(this.translateSlot(fromSlotInclusive), toSlotExclusive + this.fromSlot);
    }

    @Override
    public InternalInventory getSlotInv(int slotIndex) {
        return this.delegate.getSlotInv(this.translateSlot(slotIndex));
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return this.delegate.insertItem(this.translateSlot(slot), stack, simulate);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return this.delegate.extractItem(this.translateSlot(slot), amount, simulate);
    }

    @Override
    public void sendChangeNotification(int slot) {
        this.delegate.sendChangeNotification(this.translateSlot(slot));
    }
}

