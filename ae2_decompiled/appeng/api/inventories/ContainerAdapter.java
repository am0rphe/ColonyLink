/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.Container
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 */
package appeng.api.inventories;

import appeng.api.inventories.InternalInventory;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

class ContainerAdapter
implements Container {
    private final InternalInventory inventory;

    public ContainerAdapter(InternalInventory inventory) {
        this.inventory = inventory;
    }

    public int getContainerSize() {
        return this.inventory.size();
    }

    public boolean isEmpty() {
        return !this.inventory.iterator().hasNext();
    }

    public ItemStack getItem(int slotIndex) {
        return this.inventory.getStackInSlot(slotIndex);
    }

    public void setItem(int slotIndex, ItemStack stack) {
        this.inventory.setItemDirect(slotIndex, stack);
    }

    public ItemStack removeItem(int slotIndex, int count) {
        return this.inventory.extractItem(slotIndex, count, false);
    }

    public ItemStack removeItemNoUpdate(int slotIndex) {
        return this.inventory.extractItem(slotIndex, this.inventory.getSlotLimit(slotIndex), false);
    }

    public int getMaxStackSize() {
        int max = 99;
        for (int i = 0; i < this.inventory.size(); ++i) {
            max = Math.min(max, this.inventory.getSlotLimit(i));
        }
        return max;
    }

    public boolean canPlaceItem(int slotIndex, ItemStack stack) {
        return this.inventory.isItemValid(slotIndex, stack);
    }

    public void clearContent() {
        for (int i = 0; i < this.inventory.size(); ++i) {
            this.inventory.setItemDirect(i, ItemStack.EMPTY);
        }
    }

    public void setChanged() {
    }

    public boolean stillValid(Player player) {
        return false;
    }
}

