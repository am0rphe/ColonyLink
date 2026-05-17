/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.neoforged.neoforge.fluids.capability.IFluidHandler$FluidAction
 */
package appeng.api.config;

import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public enum Actionable {
    MODULATE(IFluidHandler.FluidAction.EXECUTE),
    SIMULATE(IFluidHandler.FluidAction.SIMULATE);

    private final IFluidHandler.FluidAction fluidAction;

    private Actionable(IFluidHandler.FluidAction fluidAction) {
        this.fluidAction = fluidAction;
    }

    public static Actionable of(IFluidHandler.FluidAction action) {
        return switch (action) {
            default -> throw new MatchException(null, null);
            case IFluidHandler.FluidAction.EXECUTE -> MODULATE;
            case IFluidHandler.FluidAction.SIMULATE -> SIMULATE;
        };
    }

    public static Actionable ofSimulate(boolean simulate) {
        return simulate ? SIMULATE : MODULATE;
    }

    public IFluidHandler.FluidAction getFluidAction() {
        return this.fluidAction;
    }

    public boolean isSimulate() {
        return this == SIMULATE;
    }
}

