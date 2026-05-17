/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.ItemStack
 */
package appeng.util.inv.filter;

import appeng.api.inventories.InternalInventory;
import net.minecraft.world.item.ItemStack;

public interface IAEItemFilter {
    default public boolean allowExtract(InternalInventory inv, int slot, int amount) {
        return true;
    }

    default public boolean allowInsert(InternalInventory inv, int slot, ItemStack stack) {
        return true;
    }
}

