package com.colonylink.colonylink;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record GuiStatePacket(boolean open, BlockPos builderPos) implements CustomPacketPayload
{
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(ColonyLink.MODID, "gui_state");
    public static final CustomPacketPayload.Type<GuiStatePacket> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, GuiStatePacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeBoolean(packet.open());
                buf.writeBlockPos(packet.builderPos());
            },
            buf -> new GuiStatePacket(buf.readBoolean(), buf.readBlockPos())
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }

    public static void handle(GuiStatePacket packet, IPayloadContext context)
    {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer)
            {
                if (packet.open())
                {
                    ColonyLinkServerTicker.addViewer(serverPlayer.getUUID(), packet.builderPos());
                    // Envoi immédiat du premier packet sans attendre le ticker (40 ticks)
                    ColonyLinkServerTicker.sendImmediateUpdate(serverPlayer, packet.builderPos());
                }
                else
                    ColonyLinkServerTicker.removeViewer(serverPlayer.getUUID());
            }
        });
    }
}