/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.ItemStack
 */
package appeng.util.inv.filter;

import appeng.api.inventories.InternalInventory;
import appeng.core.definitions.ItemDefinition;
import appeng.util.inv.filter.IAEItemFilter;
import net.minecraft.world.item.ItemStack;

public class AEItemDefinitionFilter
implements IAEItemFilter {
    private final ItemDefinition<?> definition;

    public AEItemDefinitionFilter(ItemDefinition<?> definition) {
        this.definition = definition;
    }

    @Override
    public boolean allowExtract(InternalInventory inv, int slot, int amount) {
        return true;
    }

    @Override
    public boolean allowInsert(InternalInventory inv, int slot, ItemStack stack) {
        return this.definition.is(stack);
    }
}

