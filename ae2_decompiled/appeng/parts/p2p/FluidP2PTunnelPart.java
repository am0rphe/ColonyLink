/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.neoforged.neoforge.capabilities.Capabilities$FluidHandler
 *  net.neoforged.neoforge.fluids.FluidStack
 *  net.neoforged.neoforge.fluids.capability.IFluidHandler
 *  net.neoforged.neoforge.fluids.capability.IFluidHandler$FluidAction
 */
package appeng.parts.p2p;

import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.api.stacks.AEKeyType;
import appeng.core.AppEng;
import appeng.items.parts.PartModels;
import appeng.parts.p2p.CapabilityP2PTunnelPart;
import appeng.parts.p2p.P2PModels;
import java.util.List;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public class FluidP2PTunnelPart
extends CapabilityP2PTunnelPart<FluidP2PTunnelPart, IFluidHandler> {
    private static final P2PModels MODELS = new P2PModels(AppEng.makeId("part/p2p/p2p_tunnel_fluids"));
    private static final IFluidHandler NULL_FLUID_HANDLER = new NullFluidHandler();

    @PartModels
    public static List<IPartModel> getModels() {
        return MODELS.getModels();
    }

    public FluidP2PTunnelPart(IPartItem<?> partItem) {
        super(partItem, Capabilities.FluidHandler.BLOCK);
        this.inputHandler = new InputFluidHandler();
        this.outputHandler = new OutputFluidHandler();
        this.emptyHandler = NULL_FLUID_HANDLER;
    }

    @Override
    public IPartModel getStaticModels() {
        return MODELS.getModel(this.isPowered(), this.isActive());
    }

    private class InputFluidHandler
    implements IFluidHandler {
        private InputFluidHandler() {
        }

        public int getTanks() {
            return 1;
        }

        public FluidStack getFluidInTank(int tank) {
            return FluidStack.EMPTY;
        }

        public int getTankCapacity(int tank) {
            return Integer.MAX_VALUE;
        }

        public boolean isFluidValid(int tank, FluidStack stack) {
            return true;
        }

        public int fill(FluidStack resource, IFluidHandler.FluidAction action) {
            int total = 0;
            int outputTunnels = FluidP2PTunnelPart.this.getOutputs().size();
            int amount = resource.getAmount();
            if (outputTunnels == 0 || amount == 0) {
                return 0;
            }
            int amountPerOutput = amount / outputTunnels;
            int overflow = amountPerOutput == 0 ? amount : amount % amountPerOutput;
            for (FluidP2PTunnelPart target : FluidP2PTunnelPart.this.getOutputs()) {
                CapabilityP2PTunnelPart.CapabilityGuard capabilityGuard = target.getAdjacentCapability();
                try {
                    IFluidHandler output = (IFluidHandler)capabilityGuard.get();
                    int toSend = amountPerOutput + overflow;
                    FluidStack fillWithFluidStack = resource.copy();
                    fillWithFluidStack.setAmount(toSend);
                    int received = output.fill(fillWithFluidStack, action);
                    overflow = toSend - received;
                    total += received;
                }
                finally {
                    if (capabilityGuard == null) continue;
                    capabilityGuard.close();
                }
            }
            if (action == IFluidHandler.FluidAction.EXECUTE) {
                FluidP2PTunnelPart.this.deductTransportCost(total, AEKeyType.fluids());
            }
            return total;
        }

        public FluidStack drain(FluidStack resource, IFluidHandler.FluidAction action) {
            return FluidStack.EMPTY;
        }

        public FluidStack drain(int maxDrain, IFluidHandler.FluidAction action) {
            return FluidStack.EMPTY;
        }
    }

    private class OutputFluidHandler
    implements IFluidHandler {
        private OutputFluidHandler() {
        }

        public int getTanks() {
            try (CapabilityP2PTunnelPart.CapabilityGuard input = FluidP2PTunnelPart.this.getInputCapability();){
                int n = ((IFluidHandler)input.get()).getTanks();
                return n;
            }
        }

        public FluidStack getFluidInTank(int tank) {
            try (CapabilityP2PTunnelPart.CapabilityGuard input = FluidP2PTunnelPart.this.getInputCapability();){
                FluidStack fluidStack = ((IFluidHandler)input.get()).getFluidInTank(tank);
                return fluidStack;
            }
        }

        public int getTankCapacity(int tank) {
            try (CapabilityP2PTunnelPart.CapabilityGuard input = FluidP2PTunnelPart.this.getInputCapability();){
                int n = ((IFluidHandler)input.get()).getTankCapacity(tank);
                return n;
            }
        }

        public boolean isFluidValid(int tank, FluidStack stack) {
            try (CapabilityP2PTunnelPart.CapabilityGuard input = FluidP2PTunnelPart.this.getInputCapability();){
                boolean bl = ((IFluidHandler)input.get()).isFluidValid(tank, stack);
                return bl;
            }
        }

        public int fill(FluidStack resource, IFluidHandler.FluidAction action) {
            return 0;
        }

        public FluidStack drain(FluidStack resource, IFluidHandler.FluidAction action) {
            try (CapabilityP2PTunnelPart.CapabilityGuard input = FluidP2PTunnelPart.this.getInputCapability();){
                FluidStack result = ((IFluidHandler)input.get()).drain(resource, action);
                if (action.execute()) {
                    FluidP2PTunnelPart.this.deductTransportCost(result.getAmount(), AEKeyType.fluids());
                }
                FluidStack fluidStack = result;
                return fluidStack;
            }
        }

        public FluidStack drain(int maxDrain, IFluidHandler.FluidAction action) {
            try (CapabilityP2PTunnelPart.CapabilityGuard input = FluidP2PTunnelPart.this.getInputCapability();){
                FluidStack result = ((IFluidHandler)input.get()).drain(maxDrain, action);
                if (action.execute()) {
                    FluidP2PTunnelPart.this.deductTransportCost(result.getAmount(), AEKeyType.fluids());
                }
                FluidStack fluidStack = result;
                return fluidStack;
            }
        }
    }

    private static class NullFluidHandler
    implements IFluidHandler {
        private NullFluidHandler() {
        }

        public int getTanks() {
            return 0;
        }

        public FluidStack getFluidInTank(int tank) {
            return FluidStack.EMPTY;
        }

        public int getTankCapacity(int tank) {
            return 0;
        }

        public boolean isFluidValid(int tank, FluidStack stack) {
            return false;
        }

        public int fill(FluidStack resource, IFluidHandler.FluidAction action) {
            return 0;
        }

        public FluidStack drain(FluidStack resource, IFluidHandler.FluidAction action) {
            return FluidStack.EMPTY;
        }

        public FluidStack drain(int maxDrain, IFluidHandler.FluidAction action) {
            return FluidStack.EMPTY;
        }
    }
}

