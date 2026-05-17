/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Preconditions
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.world.Container
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.neoforged.neoforge.capabilities.Capabilities$ItemHandler
 *  net.neoforged.neoforge.items.IItemHandler
 *  org.jetbrains.annotations.ApiStatus$Internal
 *  org.jetbrains.annotations.Nullable
 */
package appeng.api.inventories;

import appeng.api.config.FuzzyMode;
import appeng.api.inventories.ContainerAdapter;
import appeng.api.inventories.EmptyInternalInventory;
import appeng.api.inventories.InternalInventoryItemHandler;
import appeng.api.inventories.InternalInventoryIterator;
import appeng.api.inventories.ItemTransfer;
import appeng.api.inventories.PlatformInventoryWrapper;
import appeng.api.inventories.SubInventoryProxy;
import appeng.util.helpers.ItemComparisonHelper;
import com.google.common.base.Preconditions;
import java.util.Iterator;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public interface InternalInventory
extends Iterable<ItemStack>,
ItemTransfer {
    @Nullable
    public static ItemTransfer wrapExternal(Level level, BlockPos pos, Direction side) {
        IItemHandler handler = (IItemHandler)level.getCapability(Capabilities.ItemHandler.BLOCK, pos, (Object)side);
        if (handler != null) {
            return new PlatformInventoryWrapper(handler);
        }
        return null;
    }

    public static InternalInventory empty() {
        return EmptyInternalInventory.INSTANCE;
    }

    default public void clear() {
        for (int i = 0; i < this.size(); ++i) {
            this.setItemDirect(i, ItemStack.EMPTY);
        }
    }

    default public boolean isEmpty() {
        return !this.iterator().hasNext();
    }

    default public IItemHandler toItemHandler() {
        return new InternalInventoryItemHandler(this);
    }

    default public Container toContainer() {
        return new ContainerAdapter(this);
    }

    public int size();

    default public int getSlotLimit(int slot) {
        return 99;
    }

    public ItemStack getStackInSlot(int var1);

    public void setItemDirect(int var1, ItemStack var2);

    default public boolean isItemValid(int slot, ItemStack stack) {
        return true;
    }

    default public InternalInventory getSubInventory(int fromSlotInclusive, int toSlotExclusive) {
        return new SubInventoryProxy(this, fromSlotInclusive, toSlotExclusive);
    }

    default public InternalInventory getSlotInv(int slotIndex) {
        Preconditions.checkArgument((slotIndex >= 0 && slotIndex < this.size() ? 1 : 0) != 0, (Object)"slot out of range");
        return new SubInventoryProxy(this, slotIndex, slotIndex + 1);
    }

    default public int getRedstoneSignal() {
        ContainerAdapter adapter = new ContainerAdapter(this);
        return AbstractContainerMenu.getRedstoneSignalFromContainer((Container)adapter);
    }

    @Override
    default public Iterator<ItemStack> iterator() {
        return new InternalInventoryIterator(this);
    }

    @Override
    default public ItemStack addItems(ItemStack stack) {
        return this.addItems(stack, false);
    }

    @Override
    default public ItemStack simulateAdd(ItemStack stack) {
        return this.addItems(stack, true);
    }

    @Override
    default public ItemStack addItems(ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        if (this.size() <= 54) {
            return this.addItemSlow(stack, simulate);
        }
        return this.addItemFast(stack, simulate);
    }

    private ItemStack addItemSlow(ItemStack stack, boolean simulate) {
        ItemStack remainder = stack.copy();
        for (int pass = 0; pass < 2; ++pass) {
            boolean fillEmptySlots = pass == 1;
            for (int slot = 0; slot < this.size(); ++slot) {
                if (this.getStackInSlot(slot).isEmpty() == fillEmptySlots) {
                    remainder = this.insertItem(slot, remainder, simulate);
                }
                if (!remainder.isEmpty()) continue;
                return ItemStack.EMPTY;
            }
        }
        return remainder;
    }

    private ItemStack addItemFast(ItemStack stack, boolean simulate) {
        ItemStack remainder = stack.copy();
        for (int slot = 0; slot < this.size(); ++slot) {
            if (!(remainder = this.insertItem(slot, remainder, simulate)).isEmpty()) continue;
            return ItemStack.EMPTY;
        }
        return remainder;
    }

    @Override
    default public ItemStack removeItems(int amount, ItemStack filter, @Nullable Predicate<ItemStack> destination) {
        int slots = this.size();
        ItemStack rv = ItemStack.EMPTY;
        for (int slot = 0; slot < slots && amount > 0; ++slot) {
            ItemStack extracted;
            ItemStack is = this.getStackInSlot(slot);
            if (is.isEmpty() || !filter.isEmpty() && !ItemStack.isSameItemSameComponents((ItemStack)is, (ItemStack)filter) || destination != null && ((extracted = this.extractItem(slot, amount, true)).isEmpty() || !destination.test(extracted)) || (extracted = this.extractItem(slot, amount, false)).isEmpty()) continue;
            if (rv.isEmpty()) {
                rv = extracted;
                filter = extracted;
            } else {
                rv.grow(extracted.getCount());
            }
            amount -= extracted.getCount();
        }
        return rv;
    }

    @Override
    default public ItemStack simulateRemove(int amount, ItemStack filter, Predicate<ItemStack> destination) {
        int slots = this.size();
        ItemStack rv = ItemStack.EMPTY;
        for (int slot = 0; slot < slots && amount > 0; ++slot) {
            ItemStack extracted;
            ItemStack is = this.getStackInSlot(slot);
            if (is.isEmpty() || !filter.isEmpty() && !ItemStack.isSameItemSameComponents((ItemStack)is, (ItemStack)filter) || (extracted = this.extractItem(slot, amount, true)).isEmpty() || destination != null && !destination.test(extracted)) continue;
            if (rv.isEmpty()) {
                rv = extracted.copy();
                filter = extracted;
            } else {
                rv.grow(extracted.getCount());
            }
            amount -= extracted.getCount();
        }
        return rv;
    }

    @Override
    default public ItemStack removeSimilarItems(int amount, ItemStack filter, FuzzyMode fuzzyMode, Predicate<ItemStack> destination) {
        int slots = this.size();
        ItemStack extracted = ItemStack.EMPTY;
        for (int slot = 0; slot < slots && extracted.isEmpty(); ++slot) {
            ItemStack simulated;
            ItemStack is = this.getStackInSlot(slot);
            if (is.isEmpty() || !filter.isEmpty() && !ItemComparisonHelper.isFuzzyEqualItem(is, filter, fuzzyMode) || destination != null && ((simulated = this.extractItem(slot, amount, true)).isEmpty() || !destination.test(simulated))) continue;
            extracted = this.extractItem(slot, amount, false);
        }
        return extracted;
    }

    @Override
    default public ItemStack simulateSimilarRemove(int amount, ItemStack filter, FuzzyMode fuzzyMode, Predicate<ItemStack> destination) {
        int slots = this.size();
        ItemStack extracted = ItemStack.EMPTY;
        for (int slot = 0; slot < slots && extracted.isEmpty(); ++slot) {
            ItemStack is = this.getStackInSlot(slot);
            if (is.isEmpty() || !filter.isEmpty() && !ItemComparisonHelper.isFuzzyEqualItem(is, filter, fuzzyMode) || (extracted = this.extractItem(slot, amount, true)).isEmpty() || destination == null || destination.test(extracted)) continue;
            extracted = ItemStack.EMPTY;
        }
        return extracted;
    }

    default public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        Preconditions.checkArgument((slot >= 0 && slot < this.size() ? 1 : 0) != 0, (Object)"slot out of range");
        if (stack.isEmpty() || !this.isItemValid(slot, stack)) {
            return stack;
        }
        ItemStack inSlot = this.getStackInSlot(slot);
        int maxSpace = Math.min(this.getSlotLimit(slot), stack.getMaxStackSize());
        int freeSpace = maxSpace - inSlot.getCount();
        if (freeSpace <= 0) {
            return stack;
        }
        if (!inSlot.isEmpty() && !ItemStack.isSameItemSameComponents((ItemStack)inSlot, (ItemStack)stack)) {
            return stack;
        }
        int insertAmount = Math.min(stack.getCount(), freeSpace);
        if (!simulate) {
            ItemStack newItem = inSlot.isEmpty() ? stack.copy() : inSlot.copy();
            newItem.setCount(inSlot.getCount() + insertAmount);
            this.setItemDirect(slot, newItem);
        }
        if (freeSpace >= stack.getCount()) {
            return ItemStack.EMPTY;
        }
        ItemStack r = stack.copy();
        r.shrink(insertAmount);
        return r;
    }

    default public ItemStack extractItem(int slot, int amount, boolean simulate) {
        ItemStack item = this.getStackInSlot(slot);
        if (item.isEmpty()) {
            return ItemStack.EMPTY;
        }
        if (amount >= item.getCount()) {
            if (!simulate) {
                this.setItemDirect(slot, ItemStack.EMPTY);
                return item;
            }
            return item.copy();
        }
        ItemStack result = item.copy();
        result.setCount(amount);
        if (!simulate) {
            ItemStack reduced = item.copy();
            reduced.shrink(amount);
            this.setItemDirect(slot, reduced);
        }
        return result;
    }

    @ApiStatus.Internal
    default public void sendChangeNotification(int slot) {
    }
}

