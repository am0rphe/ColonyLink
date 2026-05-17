/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.ItemStack
 *  net.neoforged.neoforge.items.IItemHandler
 *  net.neoforged.neoforge.items.IItemHandlerModifiable
 */
package appeng.api.inventories;

import appeng.api.inventories.InternalInventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

public class PlatformInventoryWrapper
implements InternalInventory {
    private final IItemHandler handler;

    public PlatformInventoryWrapper(IItemHandler handler) {
        this.handler = handler;
    }

    @Override
    public IItemHandler toItemHandler() {
        return this.handler;
    }

    @Override
    public int size() {
        return this.handler.getSlots();
    }

    @Override
    public int getSlotLimit(int slot) {
        return this.handler.getSlotLimit(slot);
    }

    @Override
    public ItemStack getStackInSlot(int slotIndex) {
        return this.handler.getStackInSlot(slotIndex);
    }

    @Override
    public void setItemDirect(int slotIndex, ItemStack stack) {
        IItemHandler iItemHandler = this.handler;
        if (iItemHandler instanceof IItemHandlerModifiable) {
            IItemHandlerModifiable modifiableHandler = (IItemHandlerModifiable)iItemHandler;
            modifiableHandler.setStackInSlot(slotIndex, stack);
        } else {
            this.handler.extractItem(slotIndex, Integer.MAX_VALUE, false);
            this.handler.insertItem(slotIndex, stack, false);
        }
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return this.handler.isItemValid(slot, stack);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return this.handler.insertItem(slot, stack, simulate);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return this.handler.extractItem(slot, amount, simulate);
    }
}

