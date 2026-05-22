package com.colonylink.colonylink;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Packet S→C : synchronise le nombre de ColonyLink Packages stockés dans la wand.
 * Envoyé après chaque action PackageTokenPacket pour mettre à jour l'affichage du slot.
 * Aussi envoyé par le ticker à l'ouverture de la tab Citizens.
 */
public record PackageTokenSyncPacket(int count) implements CustomPacketPayload
{
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(
            ColonyLink.MODID, "package_token_sync");
    public static final CustomPacketPayload.Type<PackageTokenSyncPacket> TYPE =
            new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, PackageTokenSyncPacket> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> buf.writeInt(p.count()),
            buf -> new PackageTokenSyncPacket(buf.readInt())
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }
}