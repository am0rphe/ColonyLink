package com.colonylink.colonylink;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Packet C→S : le client demande les données de l'onglet Citizens.
 * Envoyé quand le joueur clique sur la tab Citizens.
 */
public record CitizensRequestPacket() implements CustomPacketPayload
{
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(ColonyLink.MODID, "citizens_request");
    public static final CustomPacketPayload.Type<CitizensRequestPacket> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, CitizensRequestPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {},
            buf -> new CitizensRequestPacket()
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(CitizensRequestPacket packet, IPayloadContext context)
    {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer sp)
                CitizensScanHandler.sendCitizensPacket(sp);
        });
    }
}