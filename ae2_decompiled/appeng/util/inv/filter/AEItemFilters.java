/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.ItemStack
 */
package appeng.util.inv.filter;

import appeng.api.inventories.InternalInventory;
import appeng.util.inv.filter.IAEItemFilter;
import net.minecraft.world.item.ItemStack;

public class AEItemFilters {
    public static final IAEItemFilter INSERT_ONLY = new InsertOnlyFilter();
    public static final IAEItemFilter EXTRACT_ONLY = new ExtractOnlyFilter();

    private AEItemFilters() {
    }

    private static class InsertOnlyFilter
    implements IAEItemFilter {
        private InsertOnlyFilter() {
        }

        @Override
        public boolean allowExtract(InternalInventory inv, int slot, int amount) {
            return false;
        }

        @Override
        public boolean allowInsert(InternalInventory inv, int slot, ItemStack stack) {
            return true;
        }
    }

    private static class ExtractOnlyFilter
    implements IAEItemFilter {
        private ExtractOnlyFilter() {
        }

        @Override
        public boolean allowExtract(InternalInventory inv, int slot, int amount) {
            return true;
        }

        @Override
        public boolean allowInsert(InternalInventory inv, int slot, ItemStack stack) {
            return false;
        }
    }
}

