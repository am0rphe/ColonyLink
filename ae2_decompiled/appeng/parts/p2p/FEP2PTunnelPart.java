/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.neoforged.neoforge.capabilities.Capabilities$EnergyStorage
 *  net.neoforged.neoforge.energy.IEnergyStorage
 */
package appeng.parts.p2p;

import appeng.api.config.PowerUnit;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.core.AppEng;
import appeng.items.parts.PartModels;
import appeng.parts.p2p.CapabilityP2PTunnelPart;
import appeng.parts.p2p.P2PModels;
import java.util.List;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

public class FEP2PTunnelPart
extends CapabilityP2PTunnelPart<FEP2PTunnelPart, IEnergyStorage> {
    private static final P2PModels MODELS = new P2PModels(AppEng.makeId("part/p2p/p2p_tunnel_fe"));
    private static final IEnergyStorage NULL_ENERGY_STORAGE = new NullEnergyStorage();

    @PartModels
    public static List<IPartModel> getModels() {
        return MODELS.getModels();
    }

    public FEP2PTunnelPart(IPartItem<?> partItem) {
        super(partItem, Capabilities.EnergyStorage.BLOCK);
        this.inputHandler = new InputEnergyStorage();
        this.outputHandler = new OutputEnergyStorage();
        this.emptyHandler = NULL_ENERGY_STORAGE;
    }

    @Override
    public IPartModel getStaticModels() {
        return MODELS.getModel(this.isPowered(), this.isActive());
    }

    private class InputEnergyStorage
    implements IEnergyStorage {
        private InputEnergyStorage() {
        }

        public int extractEnergy(int maxExtract, boolean simulate) {
            return 0;
        }

        public int receiveEnergy(int maxReceive, boolean simulate) {
            int total = 0;
            int outputTunnels = FEP2PTunnelPart.this.getOutputs().size();
            if (outputTunnels == 0 | maxReceive == 0) {
                return 0;
            }
            int amountPerOutput = maxReceive / outputTunnels;
            int overflow = amountPerOutput == 0 ? maxReceive : maxReceive % amountPerOutput;
            for (FEP2PTunnelPart target : FEP2PTunnelPart.this.getOutputs()) {
                CapabilityP2PTunnelPart.CapabilityGuard capabilityGuard = target.getAdjacentCapability();
                try {
                    IEnergyStorage output = (IEnergyStorage)capabilityGuard.get();
                    int toSend = amountPerOutput + overflow;
                    int received = output.receiveEnergy(toSend, simulate);
                    overflow = toSend - received;
                    total += received;
                }
                finally {
                    if (capabilityGuard == null) continue;
                    capabilityGuard.close();
                }
            }
            if (!simulate) {
                FEP2PTunnelPart.this.deductEnergyCost(total, PowerUnit.FE);
            }
            return total;
        }

        public boolean canExtract() {
            return false;
        }

        public boolean canReceive() {
            return true;
        }

        public int getMaxEnergyStored() {
            int total = 0;
            for (FEP2PTunnelPart t : FEP2PTunnelPart.this.getOutputs()) {
                CapabilityP2PTunnelPart.CapabilityGuard capabilityGuard = t.getAdjacentCapability();
                try {
                    total += ((IEnergyStorage)capabilityGuard.get()).getMaxEnergyStored();
                }
                finally {
                    if (capabilityGuard == null) continue;
                    capabilityGuard.close();
                }
            }
            return total;
        }

        public int getEnergyStored() {
            int total = 0;
            for (FEP2PTunnelPart t : FEP2PTunnelPart.this.getOutputs()) {
                CapabilityP2PTunnelPart.CapabilityGuard capabilityGuard = t.getAdjacentCapability();
                try {
                    total += ((IEnergyStorage)capabilityGuard.get()).getEnergyStored();
                }
                finally {
                    if (capabilityGuard == null) continue;
                    capabilityGuard.close();
                }
            }
            return total;
        }
    }

    private class OutputEnergyStorage
    implements IEnergyStorage {
        private OutputEnergyStorage() {
        }

        public int extractEnergy(int maxExtract, boolean simulate) {
            try (CapabilityP2PTunnelPart.CapabilityGuard input = FEP2PTunnelPart.this.getInputCapability();){
                int total = ((IEnergyStorage)input.get()).extractEnergy(maxExtract, simulate);
                if (!simulate) {
                    FEP2PTunnelPart.this.deductEnergyCost(total, PowerUnit.FE);
                }
                int n = total;
                return n;
            }
        }

        public int receiveEnergy(int maxReceive, boolean simulate) {
            return 0;
        }

        public boolean canExtract() {
            try (CapabilityP2PTunnelPart.CapabilityGuard input = FEP2PTunnelPart.this.getInputCapability();){
                boolean bl = ((IEnergyStorage)input.get()).canExtract();
                return bl;
            }
        }

        public boolean canReceive() {
            return false;
        }

        public int getMaxEnergyStored() {
            try (CapabilityP2PTunnelPart.CapabilityGuard input = FEP2PTunnelPart.this.getInputCapability();){
                int n = ((IEnergyStorage)input.get()).getMaxEnergyStored();
                return n;
            }
        }

        public int getEnergyStored() {
            try (CapabilityP2PTunnelPart.CapabilityGuard input = FEP2PTunnelPart.this.getInputCapability();){
                int n = ((IEnergyStorage)input.get()).getEnergyStored();
                return n;
            }
        }
    }

    private static class NullEnergyStorage
    implements IEnergyStorage {
        private NullEnergyStorage() {
        }

        public int receiveEnergy(int maxReceive, boolean simulate) {
            return 0;
        }

        public int extractEnergy(int maxExtract, boolean simulate) {
            return 0;
        }

        public int getEnergyStored() {
            return 0;
        }

        public int getMaxEnergyStored() {
            return 0;
        }

        public boolean canExtract() {
            return false;
        }

        public boolean canReceive() {
            return false;
        }
    }
}

