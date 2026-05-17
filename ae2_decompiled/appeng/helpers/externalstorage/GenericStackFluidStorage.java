/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.primitives.Ints
 *  net.neoforged.neoforge.fluids.FluidStack
 *  net.neoforged.neoforge.fluids.capability.IFluidHandler
 *  net.neoforged.neoforge.fluids.capability.IFluidHandler$FluidAction
 *  org.jetbrains.annotations.NotNull
 */
package appeng.helpers.externalstorage;

import appeng.api.behaviors.GenericInternalInventory;
import appeng.api.config.Actionable;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import com.google.common.primitives.Ints;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

public class GenericStackFluidStorage
implements IFluidHandler {
    private final GenericInternalInventory inv;

    public GenericStackFluidStorage(GenericInternalInventory inv) {
        this.inv = inv;
    }

    public int getTanks() {
        return this.inv.size();
    }

    @NotNull
    public FluidStack getFluidInTank(int tank) {
        AEKey aEKey = this.inv.getKey(tank);
        if (aEKey instanceof AEFluidKey) {
            AEFluidKey what = (AEFluidKey)aEKey;
            int amount = Ints.saturatedCast((long)this.inv.getAmount(tank));
            return what.toStack(amount);
        }
        return FluidStack.EMPTY;
    }

    public int getTankCapacity(int tank) {
        return Ints.saturatedCast((long)this.inv.getCapacity(AEKeyType.fluids()));
    }

    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        if (stack.isEmpty()) {
            return true;
        }
        AEFluidKey what = AEFluidKey.of(stack);
        return what != null && this.inv.isAllowedIn(tank, what);
    }

    public int fill(FluidStack resource, IFluidHandler.FluidAction action) {
        AEFluidKey what = AEFluidKey.of(resource);
        if (what == null) {
            return 0;
        }
        int inserted = 0;
        for (int i = 0; i < this.inv.size() && inserted < resource.getAmount(); inserted += (int)this.inv.insert(i, what, resource.getAmount() - inserted, Actionable.of(action)), ++i) {
        }
        return inserted;
    }

    @NotNull
    public FluidStack drain(FluidStack resource, IFluidHandler.FluidAction action) {
        AEFluidKey what = AEFluidKey.of(resource);
        if (what == null) {
            return FluidStack.EMPTY;
        }
        return this.extract(what, resource.getAmount(), action);
    }

    @NotNull
    public FluidStack drain(int maxDrain, IFluidHandler.FluidAction action) {
        for (int i = 0; i < this.inv.size(); ++i) {
            AEKey what = this.inv.getKey(i);
            AEKey aEKey = this.inv.getKey(i);
            if (!(aEKey instanceof AEFluidKey)) continue;
            AEFluidKey fluidKey = (AEFluidKey)aEKey;
            return this.extract(fluidKey, maxDrain, action);
        }
        return FluidStack.EMPTY;
    }

    private FluidStack extract(AEFluidKey what, int amount, IFluidHandler.FluidAction action) {
        int extracted = 0;
        for (int i = 0; i < this.inv.size() && extracted < amount; extracted += (int)this.inv.extract(i, what, amount - extracted, Actionable.of(action)), ++i) {
        }
        if (extracted > 0) {
            return what.toStack(extracted);
        }
        return FluidStack.EMPTY;
    }
}

