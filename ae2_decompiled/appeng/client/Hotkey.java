/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.KeyMapping
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.neoforged.neoforge.network.PacketDistributor
 */
package appeng.client;

import appeng.core.network.serverbound.HotkeyPacket;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.PacketDistributor;

public record Hotkey(String name, KeyMapping mapping) {
    public void check() {
        while (this.mapping().consumeClick()) {
            HotkeyPacket message = new HotkeyPacket(this);
            PacketDistributor.sendToServer((CustomPacketPayload)message, (CustomPacketPayload[])new CustomPacketPayload[0]);
        }
    }
}

