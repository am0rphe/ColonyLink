package com.colonylink.colonylink;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * v1.8.0 — S→C packet: Stats tab payload, sent once in reply to a
 * {@link StatsRequestPacket}.
 *
 * - Activity counters (itemsSent, ae2Crafts, domumCrafts, tomesUsed) come from the
 *   persisted {@link ColonyLinkStats} (cumulative, global per colony).
 * - Colony Overview (citizens, guards, builders, happiness, avgDistance) is read
 *   live from MineColonies at send time, never persisted.
 *
 * happinessPercent is 0..100; avgBedToWork is in blocks (-1 when no citizen has
 * both a home and a work building).
 */
public record StatsPacket(
        long itemsSent,
        long ae2Crafts,
        long domumCrafts,
        long tomesUsed,
        int citizens,
        int maxCitizens,
        int guards,
        int builders,
        int happinessPercent,
        int avgBedToWork
) implements CustomPacketPayload
{
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(ColonyLink.MODID, "stats_packet");
    public static final CustomPacketPayload.Type<StatsPacket> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, StatsPacket> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> {
                buf.writeVarLong(p.itemsSent);
                buf.writeVarLong(p.ae2Crafts);
                buf.writeVarLong(p.domumCrafts);
                buf.writeVarLong(p.tomesUsed);
                buf.writeVarInt(p.citizens);
                buf.writeVarInt(p.maxCitizens);
                buf.writeVarInt(p.guards);
                buf.writeVarInt(p.builders);
                buf.writeVarInt(p.happinessPercent);
                buf.writeInt(p.avgBedToWork);
            },
            buf -> new StatsPacket(
                    buf.readVarLong(), buf.readVarLong(), buf.readVarLong(), buf.readVarLong(),
                    buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(),
                    buf.readVarInt(), buf.readInt()
            )
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }
}
