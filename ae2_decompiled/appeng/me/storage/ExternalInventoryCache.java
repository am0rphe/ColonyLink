/*
 * Decompiled with CFR 0.152.
 */
package appeng.me.storage;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.me.storage.ExternalStorageFacade;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

class ExternalInventoryCache {
    private GenericStack[] cached = new GenericStack[0];
    private final ExternalStorageFacade facade;

    private ExternalInventoryCache(ExternalStorageFacade facade) {
        this.facade = facade;
    }

    public static ExternalInventoryCache of(ExternalStorageFacade facade) {
        return new ExternalInventoryCache(facade);
    }

    public void getAvailableItems(KeyCounter out) {
        for (GenericStack stack : this.cached) {
            out.add(stack.what(), stack.amount());
        }
    }

    public Set<AEKey> update() {
        int slot;
        HashSet<AEKey> changes = new HashSet<AEKey>();
        int slots = this.facade.getSlots();
        if (slots > this.cached.length) {
            this.cached = Arrays.copyOf(this.cached, slots);
        }
        for (slot = 0; slot < slots; ++slot) {
            GenericStack oldGenericStack = this.cached[slot];
            GenericStack newGenericStack = this.facade.getStackInSlot(slot);
            this.handlePossibleSlotChanges(slot, oldGenericStack, newGenericStack, changes);
        }
        if (slots < this.cached.length) {
            for (slot = slots; slot < this.cached.length; ++slot) {
                GenericStack aeStack = this.cached[slot];
                if (aeStack == null) continue;
                changes.add(aeStack.what());
            }
            this.cached = Arrays.copyOf(this.cached, slots);
        }
        return changes;
    }

    private void handlePossibleSlotChanges(int slot, GenericStack oldStack, GenericStack newStack, Set<AEKey> changes) {
        if (oldStack != null && newStack != null && oldStack.what().equals(newStack.what())) {
            this.handleAmountChanged(slot, oldStack, newStack, changes);
        } else {
            this.handleItemChanged(slot, oldStack, newStack, changes);
        }
    }

    private void handleAmountChanged(int slot, GenericStack oldStack, GenericStack newStack, Set<AEKey> changes) {
        if (newStack.amount() != oldStack.amount()) {
            this.cached[slot] = newStack;
            changes.add(newStack.what());
        }
    }

    private void handleItemChanged(int slot, GenericStack oldStack, GenericStack newStack, Set<AEKey> changes) {
        this.cached[slot] = newStack;
        if (oldStack != null) {
            changes.add(oldStack.what());
        }
        if (newStack != null) {
            changes.add(newStack.what());
        }
    }
}

