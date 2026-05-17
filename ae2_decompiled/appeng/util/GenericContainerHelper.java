/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.ItemStack
 *  net.neoforged.neoforge.fluids.FluidStack
 *  net.neoforged.neoforge.fluids.FluidUtil
 *  org.jetbrains.annotations.Nullable
 */
package appeng.util;

import appeng.api.stacks.GenericStack;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import org.jetbrains.annotations.Nullable;

public final class GenericContainerHelper {
    private GenericContainerHelper() {
    }

    @Nullable
    public static GenericStack getContainedFluidStack(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        FluidStack content = FluidUtil.getFluidContained((ItemStack)stack).orElse(null);
        if (content != null) {
            return GenericStack.fromFluidStack(content);
        }
        return null;
    }
}

