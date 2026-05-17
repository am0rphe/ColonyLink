/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.RegistryFriendlyByteBuf
 */
package appeng.menu.me.networktool;

import appeng.api.stacks.AEItemKey;
import appeng.menu.me.networktool.MachineGroupKey;
import java.util.Comparator;
import net.minecraft.network.RegistryFriendlyByteBuf;

public class MachineGroup {
    public static final Comparator<MachineGroup> COMPARATOR = Comparator.comparing(MachineGroup::isMissingChannel).thenComparingInt(MachineGroup::getCount).reversed();
    private final MachineGroupKey key;
    private double idlePowerUsage;
    private double powerGenerationCapacity;
    private int count;

    MachineGroup(MachineGroupKey key) {
        this.key = key;
    }

    static MachineGroup read(RegistryFriendlyByteBuf data) {
        MachineGroup entry = new MachineGroup(MachineGroupKey.fromPacket(data));
        entry.idlePowerUsage = data.readDouble();
        entry.powerGenerationCapacity = data.readDouble();
        entry.count = data.readVarInt();
        return entry;
    }

    void write(RegistryFriendlyByteBuf data) {
        this.key.write(data);
        data.writeDouble(this.idlePowerUsage);
        data.writeDouble(this.powerGenerationCapacity);
        data.writeVarInt(this.count);
    }

    public AEItemKey getDisplay() {
        return this.key.display();
    }

    public boolean isMissingChannel() {
        return this.key.missingChannel();
    }

    public double getIdlePowerUsage() {
        return this.idlePowerUsage;
    }

    void setIdlePowerUsage(double idlePowerUsage) {
        this.idlePowerUsage = idlePowerUsage;
    }

    public double getPowerGenerationCapacity() {
        return this.powerGenerationCapacity;
    }

    public void setPowerGenerationCapacity(double powerGenerationCapacity) {
        this.powerGenerationCapacity = powerGenerationCapacity;
    }

    public int getCount() {
        return this.count;
    }

    void setCount(int count) {
        this.count = count;
    }
}

