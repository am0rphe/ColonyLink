/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.ItemStack
 */
package appeng.items.contents;

import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.world.item.ItemStack;

public final class StackDependentSupplier<T>
implements Supplier<T> {
    private final Supplier<ItemStack> stackSupplier;
    private final Function<ItemStack, T> transform;
    private ItemStack currentStack;
    private T currentValue;

    public StackDependentSupplier(Supplier<ItemStack> stackSupplier, Function<ItemStack, T> transform) {
        this.stackSupplier = stackSupplier;
        this.transform = transform;
    }

    @Override
    public T get() {
        ItemStack stack = this.stackSupplier.get();
        if (this.currentStack != stack) {
            this.currentValue = this.transform.apply(stack);
            this.currentStack = stack;
        }
        return this.currentValue;
    }
}

