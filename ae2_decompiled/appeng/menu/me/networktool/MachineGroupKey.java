/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.RegistryFriendlyByteBuf
 */
package appeng.menu.me.networktool;

import appeng.api.stacks.AEItemKey;
import net.minecraft.network.RegistryFriendlyByteBuf;

record MachineGroupKey(AEItemKey display, boolean missingChannel) {
    public static MachineGroupKey fromPacket(RegistryFriendlyByteBuf data) {
        AEItemKey display = AEItemKey.fromPacket(data);
        boolean missingChannel = data.readBoolean();
        return new MachineGroupKey(display, missingChannel);
    }

    public void write(RegistryFriendlyByteBuf data) {
        this.display.writeToPacket(data);
        data.writeBoolean(this.missingChannel);
    }
}

