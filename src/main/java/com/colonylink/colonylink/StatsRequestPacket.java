package com.colonylink.colonylink;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * v1.8.0 — C→S packet: the client requests the Stats tab data.
 * Sent once when the player clicks the Stats tab (no polling).
 */
public record StatsRequestPacket() implements CustomPacketPayload
{
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(ColonyLink.MODID, "stats_request");
    public static final CustomPacketPayload.Type<StatsRequestPacket> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, StatsRequestPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {},
            buf -> new StatsRequestPacket()
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(StatsRequestPacket packet, IPayloadContext context)
    {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer sp)
                StatsScanHandler.sendStatsPacket(sp);
        });
    }
}
