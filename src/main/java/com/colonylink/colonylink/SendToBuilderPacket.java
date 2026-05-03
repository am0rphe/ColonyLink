package com.colonylink.colonylink;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SendToBuilderPacket(ItemStack stack, BlockPos builderPos, int realCount) implements CustomPacketPayload
{
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(ColonyLink.MODID, "send_to_builder");
    public static final CustomPacketPayload.Type<SendToBuilderPacket> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, SendToBuilderPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
                ItemStack.STREAM_CODEC.encode(buf, packet.stack());
                buf.writeBlockPos(packet.builderPos());
                buf.writeInt(packet.realCount());
            },
            buf -> new SendToBuilderPacket(
                    ItemStack.STREAM_CODEC.decode(buf),
                    buf.readBlockPos(),
                    buf.readInt()
            )
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }

    public static void handle(SendToBuilderPacket packet, IPayloadContext context)
    {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer)
                SendToBuilderHandler.handleSendToBuilder(
                        serverPlayer, packet.stack(), packet.builderPos(), packet.realCount());
        });
    }
}