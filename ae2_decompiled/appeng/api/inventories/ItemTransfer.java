/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.ItemStack
 *  org.jetbrains.annotations.Nullable
 */
package appeng.api.inventories;

import appeng.api.config.FuzzyMode;
import java.util.function.Predicate;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface ItemTransfer {
    public ItemStack removeItems(int var1, ItemStack var2, @Nullable Predicate<ItemStack> var3);

    public ItemStack simulateRemove(int var1, ItemStack var2, Predicate<ItemStack> var3);

    public ItemStack removeSimilarItems(int var1, ItemStack var2, FuzzyMode var3, Predicate<ItemStack> var4);

    public ItemStack simulateSimilarRemove(int var1, ItemStack var2, FuzzyMode var3, Predicate<ItemStack> var4);

    default public ItemStack addItems(ItemStack stack) {
        return this.addItems(stack, false);
    }

    default public ItemStack simulateAdd(ItemStack stack) {
        return this.addItems(stack, true);
    }

    public ItemStack addItems(ItemStack var1, boolean var2);
}

