/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.primitives.Ints
 *  net.minecraft.world.item.ItemStack
 *  net.neoforged.neoforge.items.IItemHandler
 *  org.jetbrains.annotations.NotNull
 */
package appeng.helpers.externalstorage;

import appeng.api.behaviors.GenericInternalInventory;
import appeng.api.config.Actionable;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.util.Platform;
import com.google.common.primitives.Ints;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

public class GenericStackItemStorage
implements IItemHandler {
    private final GenericInternalInventory inv;

    public GenericStackItemStorage(GenericInternalInventory inv) {
        this.inv = inv;
    }

    public int getSlots() {
        return this.inv.size();
    }

    @NotNull
    public ItemStack getStackInSlot(int slot) {
        AEKey aEKey = this.inv.getKey(slot);
        if (aEKey instanceof AEItemKey) {
            AEItemKey what = (AEItemKey)aEKey;
            int amount = Ints.saturatedCast((long)this.inv.getAmount(slot));
            return what.toStack(amount);
        }
        return ItemStack.EMPTY;
    }

    @NotNull
    public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        AEItemKey what = AEItemKey.of(stack);
        if (what == null) {
            return stack;
        }
        int inserted = (int)this.inv.insert(slot, what, stack.getCount(), Actionable.ofSimulate(simulate));
        return Platform.copyStackWithSize(stack, stack.getCount() - inserted);
    }

    @NotNull
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        AEKey aEKey = this.inv.getKey(slot);
        if (!(aEKey instanceof AEItemKey)) {
            return ItemStack.EMPTY;
        }
        AEItemKey what = (AEItemKey)aEKey;
        int extracted = (int)this.inv.extract(slot, what, amount, Actionable.ofSimulate(simulate));
        return what.toStack(extracted);
    }

    public int getSlotLimit(int slot) {
        return Ints.saturatedCast((long)this.inv.getCapacity(AEKeyType.items()));
    }

    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        if (stack.isEmpty()) {
            return true;
        }
        AEItemKey what = AEItemKey.of(stack);
        return what != null && this.inv.isAllowedIn(slot, what);
    }
}

