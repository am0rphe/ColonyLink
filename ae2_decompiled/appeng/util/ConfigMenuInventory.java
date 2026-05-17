/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.primitives.Ints
 *  net.minecraft.world.item.ItemStack
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package appeng.util;

import appeng.api.inventories.InternalInventory;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.GenericStack;
import appeng.helpers.externalstorage.GenericStackInv;
import com.google.common.primitives.Ints;
import java.util.Objects;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ConfigMenuInventory
implements InternalInventory {
    private final GenericStackInv inv;

    public ConfigMenuInventory(GenericStackInv inv) {
        this.inv = Objects.requireNonNull(inv);
    }

    public GenericStackInv getDelegate() {
        return this.inv;
    }

    @Override
    public int size() {
        return this.inv.size();
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        if (stack.isEmpty()) {
            return true;
        }
        GenericStack genericStack = this.convertToSuitableStack(stack);
        return genericStack != null && this.inv.isAllowedIn(slot, genericStack.what());
    }

    @Override
    public int getSlotLimit(int slot) {
        return (int)Math.min(Integer.MAX_VALUE, this.inv.getCapacity(AEKeyType.items()));
    }

    @Override
    public ItemStack getStackInSlot(int slotIndex) {
        AEKey aEKey;
        GenericStack stack = this.inv.getStack(slotIndex);
        if (stack != null && (aEKey = stack.what()) instanceof AEItemKey) {
            AEItemKey itemKey = (AEItemKey)aEKey;
            if (this.inv.getMode() == GenericStackInv.Mode.CONFIG_TYPES) {
                return itemKey.toStack();
            }
            if (stack.amount() > 0L && stack.amount() <= (long)itemKey.getMaxStackSize()) {
                return itemKey.toStack((int)stack.amount());
            }
        }
        return GenericStack.wrapInItemStack(stack);
    }

    @Override
    public void setItemDirect(int slotIndex, @NotNull ItemStack stack) {
        if (stack.isEmpty()) {
            this.inv.setStack(slotIndex, null);
        } else {
            GenericStack converted = this.convertToSuitableStack(stack);
            if (converted != null) {
                this.inv.setStack(slotIndex, converted);
            }
        }
    }

    @Nullable
    public GenericStack convertToSuitableStack(ItemStack stack) {
        AEItemKey what;
        if (stack.isEmpty()) {
            return null;
        }
        GenericStack unwrapped = GenericStack.unwrapItemStack(stack);
        if (unwrapped != null) {
            AEKey aEKey = unwrapped.what();
            if (aEKey instanceof AEItemKey) {
                AEItemKey itemKey = (AEItemKey)aEKey;
                stack = itemKey.toStack(Math.max(1, Ints.saturatedCast((long)unwrapped.amount())));
            } else {
                if (this.inv.isSupportedType(unwrapped.what())) {
                    return unwrapped;
                }
                return null;
            }
        }
        if (this.inv.isSupportedType(AEKeyType.items()) && (what = AEItemKey.of(stack)) != null) {
            return new GenericStack(what, stack.getCount());
        }
        return null;
    }
}

