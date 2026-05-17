/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.codec.StreamCodec
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload$Type
 *  net.minecraft.server.level.ServerPlayer
 *  org.jetbrains.annotations.NotNull
 */
package appeng.core.network.serverbound;

import appeng.core.definitions.AEAttachmentTypes;
import appeng.core.network.CustomAppEngPayload;
import appeng.core.network.ServerboundPacket;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public record UpdateHoldingCtrlPacket(boolean keyDown) implements ServerboundPacket
{
    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateHoldingCtrlPacket> STREAM_CODEC = StreamCodec.ofMember(UpdateHoldingCtrlPacket::write, UpdateHoldingCtrlPacket::decode);
    public static final CustomPacketPayload.Type<UpdateHoldingCtrlPacket> TYPE = CustomAppEngPayload.createType("toggle_ctrl_down");

    @NotNull
    public CustomPacketPayload.Type<UpdateHoldingCtrlPacket> type() {
        return TYPE;
    }

    public static UpdateHoldingCtrlPacket decode(RegistryFriendlyByteBuf buf) {
        boolean keyDown = buf.readBoolean();
        return new UpdateHoldingCtrlPacket(keyDown);
    }

    public void write(RegistryFriendlyByteBuf data) {
        data.writeBoolean(this.keyDown);
    }

    @Override
    public void handleOnServer(ServerPlayer player) {
        player.setData(AEAttachmentTypes.HOLDING_CTRL, (Object)this.keyDown);
    }
}

