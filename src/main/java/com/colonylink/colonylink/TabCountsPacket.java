package com.colonylink.colonylink;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Packet S→C léger : nombre de requêtes par tab (pour badge #5).
 *
 * Envoyé à chaque cycle ticker en même temps que sendFullUpdate.
 * Contient uniquement les counts des tabs inactives — la tab active
 * est gérée par ColonyLinkPacket directement.
 *
 * Le client met à jour ColonyLinkScreen.markTabUnread() si un count > 0
 * sur une tab qui n'est pas active.
 */
public record TabCountsPacket(
        List<Integer> counts,   // counts[i] = nb entrées tab i (-1 = non calculé)
        int activeTabIndex
) implements CustomPacketPayload
{
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(
            ColonyLink.MODID, "tab_counts");
    public static final CustomPacketPayload.Type<TabCountsPacket> TYPE =
            new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, TabCountsPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeInt(packet.counts().size());
                for (int c : packet.counts()) buf.writeInt(c);
                buf.writeInt(packet.activeTabIndex());
            },
            buf -> {
                int size = buf.readInt();
                List<Integer> counts = new ArrayList<>();
                for (int i = 0; i < size; i++) counts.add(buf.readInt());
                int active = buf.readInt();
                return new TabCountsPacket(counts, active);
            }
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }
}