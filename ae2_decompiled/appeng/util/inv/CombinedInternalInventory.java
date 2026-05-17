/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.ItemStack
 */
package appeng.util.inv;

import appeng.api.inventories.BaseInternalInventory;
import appeng.api.inventories.InternalInventory;
import net.minecraft.world.item.ItemStack;

public class CombinedInternalInventory
extends BaseInternalInventory {
    private final InternalInventory[] inventories;
    private final int[] baseIndex;
    private final int slotCount;

    public CombinedInternalInventory(InternalInventory ... inventories) {
        this.inventories = inventories;
        this.baseIndex = new int[this.inventories.length];
        int index = 0;
        for (int i = 0; i < this.inventories.length; ++i) {
            this.baseIndex[i] = index += this.inventories[i].size();
        }
        this.slotCount = index;
    }

    private int getIndexForSlot(int slot) {
        if (slot < 0) {
            return -1;
        }
        for (int i = 0; i < this.baseIndex.length; ++i) {
            if (slot - this.baseIndex[i] >= 0) continue;
            return i;
        }
        return -1;
    }

    private InternalInventory getHandlerFromIndex(int index) {
        if (index < 0 || index >= this.inventories.length) {
            return InternalInventory.empty();
        }
        return this.inventories[index];
    }

    private int getSlotFromIndex(int slot, int index) {
        if (index <= 0 || index >= this.baseIndex.length) {
            return slot;
        }
        return slot - this.baseIndex[index - 1];
    }

    @Override
    public int size() {
        return this.slotCount;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        int index = this.getIndexForSlot(slot);
        InternalInventory handler = this.getHandlerFromIndex(index);
        int targetSlot = this.getSlotFromIndex(slot, index);
        return handler.getStackInSlot(targetSlot);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        int index = this.getIndexForSlot(slot);
        InternalInventory handler = this.getHandlerFromIndex(index);
        int targetSlot = this.getSlotFromIndex(slot, index);
        return handler.insertItem(targetSlot, stack, simulate);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        int index = this.getIndexForSlot(slot);
        InternalInventory handler = this.getHandlerFromIndex(index);
        int targetSlot = this.getSlotFromIndex(slot, index);
        return handler.extractItem(targetSlot, amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {
        int index = this.getIndexForSlot(slot);
        InternalInventory handler = this.getHandlerFromIndex(index);
        int localSlot = this.getSlotFromIndex(slot, index);
        return handler.getSlotLimit(localSlot);
    }

    @Override
    public void setItemDirect(int slot, ItemStack stack) {
        int index = this.getIndexForSlot(slot);
        InternalInventory handler = this.getHandlerFromIndex(index);
        int targetSlot = this.getSlotFromIndex(slot, index);
        handler.setItemDirect(targetSlot, stack);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        int index = this.getIndexForSlot(slot);
        InternalInventory handler = this.getHandlerFromIndex(index);
        int targetSlot = this.getSlotFromIndex(slot, index);
        return handler.isItemValid(targetSlot, stack);
    }

    @Override
    public void sendChangeNotification(int slot) {
        int index = this.getIndexForSlot(slot);
        InternalInventory handler = this.getHandlerFromIndex(index);
        int targetSlot = this.getSlotFromIndex(slot, index);
        handler.sendChangeNotification(targetSlot);
    }
}

