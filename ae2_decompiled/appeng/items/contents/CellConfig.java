/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Preconditions
 *  net.minecraft.world.item.ItemStack
 */
package appeng.items.contents;

import appeng.api.ids.AEComponents;
import appeng.api.stacks.AEKeyType;
import appeng.util.ConfigInventory;
import com.google.common.base.Preconditions;
import java.util.List;
import java.util.Set;
import net.minecraft.world.item.ItemStack;

public final class CellConfig {
    private CellConfig() {
    }

    public static ConfigInventory create(Set<AEKeyType> supportedTypes, ItemStack is, int size) {
        Preconditions.checkArgument((size >= 1 && size <= 63 ? 1 : 0) != 0, (Object)"Config inventory must have between 1 and 63 slots inclusive.");
        Holder holder = new Holder(is);
        holder.inv = ConfigInventory.configTypes(size).supportedTypes(supportedTypes).changeListener(holder::save).build();
        holder.load();
        return holder.inv;
    }

    public static ConfigInventory create(Set<AEKeyType> supportedTypes, ItemStack is) {
        Holder holder = new Holder(is);
        holder.inv = ConfigInventory.configTypes(63).supportedTypes(supportedTypes).changeListener(holder::save).build();
        holder.load();
        return holder.inv;
    }

    public static ConfigInventory create(ItemStack is) {
        Holder holder = new Holder(is);
        holder.inv = ConfigInventory.configTypes(63).changeListener(holder::save).build();
        holder.load();
        return holder.inv;
    }

    private static class Holder {
        private final ItemStack stack;
        private ConfigInventory inv;

        public Holder(ItemStack stack) {
            this.stack = stack;
        }

        public void load() {
            this.inv.readFromList((List)this.stack.getOrDefault(AEComponents.STORAGE_CELL_CONFIG_INV, List.of()));
        }

        public void save() {
            this.stack.set(AEComponents.STORAGE_CELL_CONFIG_INV, this.inv.toList());
        }
    }
}

