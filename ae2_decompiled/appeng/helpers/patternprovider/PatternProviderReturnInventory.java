/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 */
package appeng.helpers.patternprovider;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.MEStorage;
import appeng.helpers.externalstorage.GenericStackInv;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class PatternProviderReturnInventory
extends GenericStackInv {
    public static int NUMBER_OF_SLOTS = 9;
    private boolean injectingIntoNetwork = false;

    public PatternProviderReturnInventory(Runnable listener) {
        super(listener, NUMBER_OF_SLOTS);
        this.useRegisteredCapacities();
    }

    @Override
    public boolean canExtract() {
        return false;
    }

    @Override
    public boolean canInsert() {
        return !this.injectingIntoNetwork;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean injectIntoNetwork(MEStorage storage, IActionSource src, Consumer<GenericStack> insertionCallback) {
        boolean didSomething = false;
        this.injectingIntoNetwork = true;
        try {
            for (int i = 0; i < this.stacks.length; ++i) {
                GenericStack stack = this.stacks[i];
                if (stack == null) continue;
                long sizeBefore = stack.amount();
                long inserted = storage.insert(stack.what(), stack.amount(), Actionable.MODULATE, src);
                this.stacks[i] = inserted >= stack.amount() ? null : new GenericStack(stack.what(), stack.amount() - inserted);
                inserted = Math.max(0L, sizeBefore - GenericStack.getStackSizeOrZero(this.stacks[i]));
                if (inserted <= 0L) continue;
                didSomething = true;
                insertionCallback.accept(new GenericStack(stack.what(), inserted));
            }
        }
        finally {
            this.injectingIntoNetwork = false;
        }
        return didSomething;
    }

    public void addDrops(List<ItemStack> drops, Level level, BlockPos pos) {
        for (GenericStack stack : this.stacks) {
            if (stack == null) continue;
            stack.what().addDrops(stack.amount(), drops, level, pos);
        }
    }
}

