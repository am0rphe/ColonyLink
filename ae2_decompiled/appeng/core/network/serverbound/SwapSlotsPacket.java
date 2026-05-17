/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.codec.StreamCodec
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload$Type
 *  net.minecraft.server.level.ServerPlayer
 */
package appeng.core.network.serverbound;

import appeng.core.network.CustomAppEngPayload;
import appeng.core.network.ServerboundPacket;
import appeng.menu.AEBaseMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public record SwapSlotsPacket(int slotA, int slotB) implements ServerboundPacket
{
    public static final StreamCodec<RegistryFriendlyByteBuf, SwapSlotsPacket> STREAM_CODEC = StreamCodec.ofMember(SwapSlotsPacket::write, SwapSlotsPacket::decode);
    public static final CustomPacketPayload.Type<SwapSlotsPacket> TYPE = CustomAppEngPayload.createType("swap_slots");

    public CustomPacketPayload.Type<SwapSlotsPacket> type() {
        return TYPE;
    }

    public static SwapSlotsPacket decode(RegistryFriendlyByteBuf stream) {
        int slotA = stream.readInt();
        int slotB = stream.readInt();
        return new SwapSlotsPacket(slotA, slotB);
    }

    public void write(RegistryFriendlyByteBuf data) {
        data.writeInt(this.slotA);
        data.writeInt(this.slotB);
    }

    @Override
    public void handleOnServer(ServerPlayer player) {
        if (player != null && player.containerMenu instanceof AEBaseMenu) {
            ((AEBaseMenu)player.containerMenu).swapSlotContents(this.slotA, this.slotB);
        }
    }
}

