/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 */
package appeng.core.definitions;

import appeng.api.util.AEColor;
import appeng.core.definitions.ItemDefinition;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class ColoredItemDefinition<T extends Item> {
    private final Map<AEColor, ItemDefinition<T>> items = new EnumMap<AEColor, ItemDefinition<T>>(AEColor.class);
    private final Map<AEColor, ResourceLocation> ids = new EnumMap<AEColor, ResourceLocation>(AEColor.class);

    void add(AEColor v, ResourceLocation id, ItemDefinition<T> is) {
        this.ids.put(v, id);
        this.items.put(v, is);
    }

    public ResourceLocation id(AEColor color) {
        return this.ids.get((Object)color);
    }

    public T item(AEColor color) {
        return this.items.get((Object)color).asItem();
    }

    public ItemStack stack(AEColor color) {
        return this.stack(color, 1);
    }

    public ItemStack stack(AEColor color, int stackSize) {
        T item = this.item(color);
        if (item == null) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(item, stackSize);
    }
}

