/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  dev.emi.emi.api.neoforge.NeoForgeEmiStack
 *  dev.emi.emi.api.stack.EmiStack
 *  net.minecraft.core.Holder
 *  net.minecraft.world.level.material.Fluid
 *  net.minecraft.world.level.material.Fluids
 *  net.neoforged.neoforge.fluids.FluidStack
 *  org.jetbrains.annotations.Nullable
 */
package appeng.integration.modules.emi;

import appeng.api.integrations.emi.EmiStackConverter;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import dev.emi.emi.api.neoforge.NeoForgeEmiStack;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.core.Holder;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

class EmiFluidStackConverter
implements EmiStackConverter {
    EmiFluidStackConverter() {
    }

    @Override
    public Class<?> getKeyType() {
        return Fluid.class;
    }

    @Override
    @Nullable
    public EmiStack toEmiStack(GenericStack stack) {
        AEKey aEKey = stack.what();
        if (aEKey instanceof AEFluidKey) {
            AEFluidKey fluidKey = (AEFluidKey)aEKey;
            return NeoForgeEmiStack.of((FluidStack)fluidKey.toStack(1)).setAmount(stack.amount());
        }
        return null;
    }

    @Override
    @Nullable
    public GenericStack toGenericStack(EmiStack stack) {
        Fluid fluid = (Fluid)stack.getKeyOfType(Fluid.class);
        if (fluid != null && fluid != Fluids.EMPTY) {
            FluidStack fluidStack = new FluidStack((Holder)fluid.builtInRegistryHolder(), 1, stack.getComponentChanges());
            AEFluidKey fluidKey = AEFluidKey.of(fluidStack);
            return new GenericStack(fluidKey, stack.getAmount());
        }
        return null;
    }
}

