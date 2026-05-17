/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  org.jetbrains.annotations.Nullable
 */
package appeng.menu.me.networktool;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.energy.IPassiveEnergyGenerator;
import appeng.api.stacks.AEItemKey;
import appeng.blockentity.misc.VibrationChamberBlockEntity;
import appeng.menu.me.networktool.MachineGroup;
import appeng.menu.me.networktool.MachineGroupKey;
import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.jetbrains.annotations.Nullable;

public class NetworkStatus {
    private double averagePowerInjection;
    private double averagePowerUsage;
    private double storedPower;
    private double maxStoredPower;
    private double channelPower;
    private int channelsUsed;
    private List<MachineGroup> groupedMachines = Collections.emptyList();

    public static NetworkStatus fromGrid(IGrid grid) {
        IEnergyService eg = grid.getEnergyService();
        NetworkStatus status = new NetworkStatus();
        status.averagePowerInjection = eg.getAvgPowerInjection();
        status.averagePowerUsage = eg.getAvgPowerUsage();
        status.storedPower = eg.getStoredPower();
        status.maxStoredPower = eg.getMaxStoredPower();
        status.channelPower = eg.getChannelPowerUsage();
        status.channelsUsed = grid.getPathingService().getUsedChannels();
        HashMap<MachineGroupKey, MachineGroup> groupedMachines = new HashMap<MachineGroupKey, MachineGroup>();
        for (Class<?> machineClass : grid.getMachineClasses()) {
            for (IGridNode machine : grid.getMachineNodes(machineClass)) {
                MachineGroupKey key = NetworkStatus.getKey(machine);
                if (key == null) continue;
                MachineGroup group = groupedMachines.computeIfAbsent(key, MachineGroup::new);
                group.setCount(group.getCount() + 1);
                group.setIdlePowerUsage(group.getIdlePowerUsage() + machine.getIdlePowerUsage());
                Object owner = machine.getOwner();
                IPassiveEnergyGenerator passiveEnergyGenerator = machine.getService(IPassiveEnergyGenerator.class);
                if (passiveEnergyGenerator != null && !passiveEnergyGenerator.isSuppressed()) {
                    group.setPowerGenerationCapacity(group.getPowerGenerationCapacity() + passiveEnergyGenerator.getRate());
                }
                if (!(owner instanceof VibrationChamberBlockEntity)) continue;
                VibrationChamberBlockEntity vibrationChamberBlockEntity = (VibrationChamberBlockEntity)owner;
                group.setPowerGenerationCapacity(group.getPowerGenerationCapacity() + vibrationChamberBlockEntity.getMaxEnergyRate());
            }
        }
        status.groupedMachines = ImmutableList.copyOf(groupedMachines.values());
        return status;
    }

    @Nullable
    private static MachineGroupKey getKey(IGridNode machine) {
        AEItemKey visualRepresentation = machine.getVisualRepresentation();
        if (visualRepresentation == null) {
            return null;
        }
        return new MachineGroupKey(visualRepresentation, !machine.meetsChannelRequirements());
    }

    public double getAveragePowerInjection() {
        return this.averagePowerInjection;
    }

    public double getAveragePowerUsage() {
        return this.averagePowerUsage;
    }

    public double getStoredPower() {
        return this.storedPower;
    }

    public double getMaxStoredPower() {
        return this.maxStoredPower;
    }

    public double getChannelPower() {
        return this.channelPower;
    }

    public int getChannelsUsed() {
        return this.channelsUsed;
    }

    public List<MachineGroup> getGroupedMachines() {
        return this.groupedMachines;
    }

    public static NetworkStatus read(RegistryFriendlyByteBuf data) {
        NetworkStatus status = new NetworkStatus();
        status.averagePowerInjection = data.readDouble();
        status.averagePowerUsage = data.readDouble();
        status.storedPower = data.readDouble();
        status.maxStoredPower = data.readDouble();
        status.channelPower = data.readDouble();
        status.channelsUsed = data.readVarInt();
        int count = data.readVarInt();
        ImmutableList.Builder machines = ImmutableList.builder();
        for (int i = 0; i < count; ++i) {
            machines.add((Object)MachineGroup.read(data));
        }
        status.groupedMachines = machines.build();
        return status;
    }

    public void write(RegistryFriendlyByteBuf data) {
        data.writeDouble(this.averagePowerInjection);
        data.writeDouble(this.averagePowerUsage);
        data.writeDouble(this.storedPower);
        data.writeDouble(this.maxStoredPower);
        data.writeDouble(this.channelPower);
        data.writeVarInt(this.channelsUsed);
        data.writeVarInt(this.groupedMachines.size());
        for (MachineGroup machine : this.groupedMachines) {
            machine.write(data);
        }
    }
}

