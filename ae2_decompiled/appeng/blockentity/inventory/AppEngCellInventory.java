/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.ItemStack
 */
package appeng.blockentity.inventory;

import appeng.api.inventories.BaseInternalInventory;
import appeng.api.storage.cells.StorageCell;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.InternalInventoryHost;
import appeng.util.inv.filter.IAEItemFilter;
import net.minecraft.world.item.ItemStack;

public class AppEngCellInventory
extends BaseInternalInventory {
    private final AppEngInternalInventory inv;
    private final StorageCell[] handlerForSlot;

    public AppEngCellInventory(InternalInventoryHost host, int slots) {
        this.inv = new AppEngInternalInventory(host, slots, 1);
        this.handlerForSlot = new StorageCell[slots];
    }

    public void setHandler(int slot, StorageCell handler) {
        this.handlerForSlot[slot] = handler;
    }

    public void setFilter(IAEItemFilter filter) {
        this.inv.setFilter(filter);
    }

    @Override
    public void setItemDirect(int slot, ItemStack stack) {
        this.persist(slot);
        this.inv.setItemDirect(slot, stack);
    }

    @Override
    public int size() {
        return this.inv.size();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        this.persist(slot);
        return this.inv.getStackInSlot(slot);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        this.persist(slot);
        return this.inv.insertItem(slot, stack, simulate);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        this.persist(slot);
        return this.inv.extractItem(slot, amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {
        return this.inv.getSlotLimit(slot);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return this.inv.isItemValid(slot, stack);
    }

    public void persist() {
        for (int i = 0; i < this.size(); ++i) {
            this.persist(i);
        }
    }

    private void persist(int slot) {
        if (this.handlerForSlot[slot] != null) {
            this.handlerForSlot[slot].persist();
        }
    }

    @Override
    public void sendChangeNotification(int slot) {
        this.inv.sendChangeNotification(slot);
    }
}

