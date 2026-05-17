/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Preconditions
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.core.NonNullList
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.component.ItemContainerContents
 *  org.jetbrains.annotations.ApiStatus$Internal
 *  org.jetbrains.annotations.Nullable
 */
package appeng.util.inv;

import appeng.api.inventories.BaseInternalInventory;
import appeng.util.inv.InternalInventoryHost;
import appeng.util.inv.filter.IAEItemFilter;
import com.google.common.base.Preconditions;
import java.util.Arrays;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public class AppEngInternalInventory
extends BaseInternalInventory {
    private boolean enableClientEvents = false;
    private InternalInventoryHost host;
    private final NonNullList<ItemStack> stacks;
    private final int[] maxStack;
    private IAEItemFilter filter;
    private boolean notifyingChanges = false;

    public AppEngInternalInventory(InternalInventoryHost host, int size, int maxStack, IAEItemFilter filter) {
        this.setHost(host);
        this.setFilter(filter);
        this.maxStack = new int[size];
        this.stacks = NonNullList.withSize((int)size, (Object)ItemStack.EMPTY);
        Arrays.fill(this.maxStack, maxStack);
    }

    public AppEngInternalInventory(@Nullable InternalInventoryHost inventory, int size, int maxStack) {
        this(inventory, size, maxStack, null);
    }

    public AppEngInternalInventory(int size) {
        this(null, size, 64);
    }

    public AppEngInternalInventory(@Nullable InternalInventoryHost inventory, int size) {
        this(inventory, size, 64);
    }

    public void setFilter(IAEItemFilter filter) {
        this.filter = filter;
    }

    @Override
    public int getSlotLimit(int slot) {
        return this.maxStack[slot];
    }

    @Override
    public ItemStack getStackInSlot(int slotIndex) {
        return (ItemStack)this.stacks.get(slotIndex);
    }

    @Override
    public void setItemDirect(int slot, ItemStack stack) {
        this.stacks.set(slot, (Object)stack);
        this.notifyContentsChanged(slot);
    }

    private void notifyContentsChanged(int slot) {
        this.onContentsChanged(slot);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        Preconditions.checkArgument((slot >= 0 && slot < this.size() ? 1 : 0) != 0, (Object)"slot out of range");
        if (this.filter != null && !this.filter.allowExtract(this, slot, amount)) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = (ItemStack)this.stacks.get(slot);
        int toExtract = Math.min(stack.getCount(), Math.min(amount, stack.getMaxStackSize()));
        if (toExtract <= 0) {
            return ItemStack.EMPTY;
        }
        if (stack.getCount() <= toExtract) {
            if (!simulate) {
                this.setItemDirect(slot, ItemStack.EMPTY);
                this.notifyContentsChanged(slot);
                return stack;
            }
            return stack.copy();
        }
        ItemStack result = stack.copy();
        if (!simulate) {
            stack.shrink(toExtract);
            this.notifyContentsChanged(slot);
        }
        result.setCount(toExtract);
        return result;
    }

    protected void onContentsChanged(int slot) {
        if (this.host != null && this.eventsEnabled() && !this.notifyingChanges) {
            this.notifyingChanges = true;
            this.host.onChangeInventory(this, slot);
            this.host.saveChangedInventory(this);
            this.notifyingChanges = false;
        }
    }

    protected boolean eventsEnabled() {
        return this.host != null && !this.host.isClientSide() || this.isEnableClientEvents();
    }

    public void setMaxStackSize(int slot, int size) {
        this.maxStack[slot] = size;
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        if (this.maxStack[slot] == 0) {
            return false;
        }
        if (this.filter != null) {
            return this.filter.allowInsert(this, slot, stack);
        }
        return true;
    }

    public ItemContainerContents toItemContainerContents() {
        return ItemContainerContents.fromItems(this.stacks);
    }

    public void fromItemContainerContents(ItemContainerContents contents) {
        contents.copyInto(this.stacks);
    }

    public void writeToNBT(CompoundTag data, String name, HolderLookup.Provider registries) {
        if (this.isEmpty()) {
            data.remove(name);
            return;
        }
        ListTag items = new ListTag();
        for (int i = 0; i < this.stacks.size(); ++i) {
            ItemStack stack = (ItemStack)this.stacks.get(i);
            if (stack.isEmpty()) continue;
            CompoundTag itemTag = new CompoundTag();
            itemTag.putInt("Slot", i);
            items.add((Object)stack.save(registries, (Tag)itemTag));
        }
        data.put(name, (Tag)items);
    }

    public void readFromNBT(CompoundTag data, String name, HolderLookup.Provider registries) {
        if (data.contains(name, 9)) {
            ListTag tagList = data.getList(name, 10);
            for (Tag itemTag : tagList) {
                CompoundTag itemCompound = (CompoundTag)itemTag;
                int slot = itemCompound.getInt("Slot");
                if (slot < 0 || slot >= this.stacks.size()) continue;
                this.stacks.set(slot, (Object)ItemStack.parseOptional((HolderLookup.Provider)registries, (CompoundTag)itemCompound));
            }
        }
    }

    private boolean isEnableClientEvents() {
        return this.enableClientEvents;
    }

    public void setEnableClientEvents(boolean enableClientEvents) {
        this.enableClientEvents = enableClientEvents;
    }

    @ApiStatus.Internal
    public InternalInventoryHost getHost() {
        return this.host;
    }

    protected final void setHost(InternalInventoryHost host) {
        this.host = host;
    }

    @Override
    public int size() {
        return this.stacks.size();
    }
}

