/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.ItemStack
 */
package appeng.api.inventories;

import appeng.api.inventories.InternalInventory;
import java.util.Iterator;
import java.util.NoSuchElementException;
import net.minecraft.world.item.ItemStack;

class InternalInventoryIterator
implements Iterator<ItemStack> {
    private final InternalInventory inventory;
    private int currentSlot;
    private ItemStack currentStack;

    InternalInventoryIterator(InternalInventory inventory) {
        this.inventory = inventory;
        this.currentSlot = -1;
        this.seekNext();
    }

    private void seekNext() {
        this.currentStack = ItemStack.EMPTY;
        ++this.currentSlot;
        while (this.currentSlot < this.inventory.size()) {
            this.currentStack = this.inventory.getStackInSlot(this.currentSlot);
            if (!this.currentStack.isEmpty()) break;
            ++this.currentSlot;
        }
    }

    @Override
    public boolean hasNext() {
        return !this.currentStack.isEmpty();
    }

    @Override
    public ItemStack next() {
        if (this.currentStack.isEmpty()) {
            throw new NoSuchElementException();
        }
        ItemStack result = this.currentStack;
        this.seekNext();
        return result;
    }
}

