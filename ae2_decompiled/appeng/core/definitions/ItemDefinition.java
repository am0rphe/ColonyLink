/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Holder
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 *  net.neoforged.neoforge.registries.DeferredItem
 */
package appeng.core.definitions;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.util.helpers.ItemComparisonHelper;
import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.registries.DeferredItem;

public class ItemDefinition<T extends Item>
implements ItemLike,
Supplier<T> {
    private final String englishName;
    private final DeferredItem<T> item;

    public ItemDefinition(String englishName, DeferredItem<T> item) {
        this.englishName = englishName;
        this.item = item;
    }

    public String getEnglishName() {
        return this.englishName;
    }

    public ResourceLocation id() {
        return this.item.getId();
    }

    public ItemStack stack() {
        return this.stack(1);
    }

    public ItemStack stack(int stackSize) {
        return new ItemStack(this.item, stackSize);
    }

    public GenericStack genericStack(long stackSize) {
        return new GenericStack(AEItemKey.of(this.item), stackSize);
    }

    public Holder<Item> holder() {
        return this.item;
    }

    @Deprecated(forRemoval=true, since="1.21")
    public final boolean isSameAs(ItemStack comparableStack) {
        return this.is(comparableStack);
    }

    public final boolean is(ItemStack comparableStack) {
        return ItemComparisonHelper.isEqualItemType(comparableStack, this.stack());
    }

    public final boolean is(AEKey key) {
        if (key instanceof AEItemKey) {
            AEItemKey itemKey = (AEItemKey)key;
            return this.asItem() == itemKey.getItem();
        }
        return false;
    }

    @Deprecated(forRemoval=true, since="1.21")
    public final boolean isSameAs(AEKey key) {
        return this.is(key);
    }

    @Override
    public T get() {
        return (T)((Item)this.item.get());
    }

    public T asItem() {
        return (T)((Item)this.item.get());
    }
}

