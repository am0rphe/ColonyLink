/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.ItemStack
 *  net.neoforged.neoforge.items.IItemHandlerModifiable
 */
package appeng.api.inventories;

import appeng.api.inventories.InternalInventory;
import appeng.api.stacks.GenericStack;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

class InternalInventoryItemHandler
implements IItemHandlerModifiable {
    private final InternalInventory inventory;

    public InternalInventoryItemHandler(InternalInventory inventory) {
        this.inventory = inventory;
    }

    public int getSlots() {
        return this.inventory.size();
    }

    public ItemStack getStackInSlot(int slot) {
        return this.inventory.getStackInSlot(slot);
    }

    public void setStackInSlot(int slot, ItemStack stack) {
        this.inventory.setItemDirect(slot, stack);
    }

    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return this.inventory.insertItem(slot, stack, simulate);
    }

    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (GenericStack.isWrapped(this.inventory.getStackInSlot(slot))) {
            return ItemStack.EMPTY;
        }
        return this.inventory.extractItem(slot, amount, simulate);
    }

    public int getSlotLimit(int slot) {
        return this.inventory.getSlotLimit(slot);
    }

    public boolean isItemValid(int slot, ItemStack stack) {
        return this.inventory.isItemValid(slot, stack);
    }
}

