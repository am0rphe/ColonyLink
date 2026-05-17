/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.Container
 *  net.minecraft.world.item.ItemStack
 *  net.neoforged.neoforge.items.IItemHandler
 */
package appeng.util.inv;

import appeng.api.inventories.InternalInventory;
import java.util.Iterator;
import java.util.function.Supplier;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

public class SupplierInternalInventory<T extends InternalInventory>
implements InternalInventory {
    private final Supplier<T> delegate;

    public SupplierInternalInventory(Supplier<T> delegate) {
        this.delegate = delegate;
    }

    protected final T getDelegate() {
        return (T)((InternalInventory)this.delegate.get());
    }

    @Override
    public boolean isEmpty() {
        return this.getDelegate().isEmpty();
    }

    @Override
    public IItemHandler toItemHandler() {
        return this.getDelegate().toItemHandler();
    }

    @Override
    public Container toContainer() {
        return this.getDelegate().toContainer();
    }

    @Override
    public int size() {
        return this.getDelegate().size();
    }

    @Override
    public int getSlotLimit(int slot) {
        return this.getDelegate().getSlotLimit(slot);
    }

    @Override
    public ItemStack getStackInSlot(int slotIndex) {
        return this.getDelegate().getStackInSlot(slotIndex);
    }

    @Override
    public void setItemDirect(int slotIndex, ItemStack stack) {
        this.getDelegate().setItemDirect(slotIndex, stack);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return this.getDelegate().isItemValid(slot, stack);
    }

    @Override
    public InternalInventory getSubInventory(int fromSlotInclusive, int toSlotExclusive) {
        return this.getDelegate().getSubInventory(fromSlotInclusive, toSlotExclusive);
    }

    @Override
    public InternalInventory getSlotInv(int slotIndex) {
        return this.getDelegate().getSlotInv(slotIndex);
    }

    @Override
    public int getRedstoneSignal() {
        return this.getDelegate().getRedstoneSignal();
    }

    @Override
    public Iterator<ItemStack> iterator() {
        return this.getDelegate().iterator();
    }

    @Override
    public ItemStack addItems(ItemStack stack) {
        return this.getDelegate().addItems(stack);
    }

    @Override
    public ItemStack addItems(ItemStack stack, boolean simulate) {
        return this.getDelegate().addItems(stack, simulate);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return this.getDelegate().insertItem(slot, stack, simulate);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return this.getDelegate().extractItem(slot, amount, simulate);
    }

    @Override
    public void sendChangeNotification(int slot) {
        this.getDelegate().sendChangeNotification(slot);
    }
}

