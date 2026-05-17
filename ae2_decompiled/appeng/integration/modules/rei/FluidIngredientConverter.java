/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  dev.architectury.fluid.FluidStack
 *  dev.architectury.hooks.fluid.forge.FluidStackHooksForge
 *  me.shedaniel.rei.api.common.entry.EntryStack
 *  me.shedaniel.rei.api.common.entry.type.EntryType
 *  me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes
 *  net.neoforged.neoforge.fluids.FluidStack
 *  org.jetbrains.annotations.Nullable
 */
package appeng.integration.modules.rei;

import appeng.api.integrations.rei.IngredientConverter;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import dev.architectury.fluid.FluidStack;
import dev.architectury.hooks.fluid.forge.FluidStackHooksForge;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.EntryType;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import org.jetbrains.annotations.Nullable;

public class FluidIngredientConverter
implements IngredientConverter<FluidStack> {
    @Override
    public EntryType<FluidStack> getIngredientType() {
        return VanillaEntryTypes.FLUID;
    }

    @Override
    @Nullable
    public EntryStack<FluidStack> getIngredientFromStack(GenericStack stack) {
        AEKey aEKey = stack.what();
        if (aEKey instanceof AEFluidKey) {
            AEFluidKey fluidKey = (AEFluidKey)aEKey;
            return EntryStack.of(this.getIngredientType(), (Object)FluidStackHooksForge.fromForge((net.neoforged.neoforge.fluids.FluidStack)fluidKey.toStack(1)).copyWithAmount(Math.max(1L, stack.amount())));
        }
        return null;
    }

    @Override
    @Nullable
    public GenericStack getStackFromIngredient(EntryStack<FluidStack> ingredient) {
        if (ingredient.getType() == this.getIngredientType()) {
            FluidStack fluidStack = (FluidStack)ingredient.castValue();
            return new GenericStack(AEFluidKey.of(FluidStackHooksForge.toForge((FluidStack)fluidStack)), fluidStack.getAmount());
        }
        return null;
    }
}

