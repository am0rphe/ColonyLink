/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.codec.StreamCodec
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload$Type
 *  net.minecraft.world.entity.player.Player
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.OnlyIn
 */
package appeng.core.network.clientbound;

import appeng.client.gui.me.networktool.NetworkStatusScreen;
import appeng.core.network.ClientboundPacket;
import appeng.core.network.CustomAppEngPayload;
import appeng.menu.me.networktool.NetworkStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public record NetworkStatusPacket(NetworkStatus status) implements ClientboundPacket
{
    public static final StreamCodec<RegistryFriendlyByteBuf, NetworkStatusPacket> STREAM_CODEC = StreamCodec.ofMember(NetworkStatusPacket::write, NetworkStatusPacket::decode);
    public static final CustomPacketPayload.Type<NetworkStatusPacket> TYPE = CustomAppEngPayload.createType("network_status");

    public CustomPacketPayload.Type<NetworkStatusPacket> type() {
        return TYPE;
    }

    public static NetworkStatusPacket decode(RegistryFriendlyByteBuf data) {
        NetworkStatus status = NetworkStatus.read(data);
        return new NetworkStatusPacket(status);
    }

    public void write(RegistryFriendlyByteBuf data) {
        this.status.write(data);
    }

    @Override
    @OnlyIn(value=Dist.CLIENT)
    public void handleOnClient(Player player) {
        Screen gs = Minecraft.getInstance().screen;
        if (gs instanceof NetworkStatusScreen) {
            ((NetworkStatusScreen)gs).processServerUpdate(this.status);
        }
    }
}

