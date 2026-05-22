package com.colonylink.colonylink;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Packet S→C : liste des requêtes ouvertes de tous les citoyens NON-builders.
 * Envoyé par le ticker quand la tab Citizens est active, ou par réponse à CitizensRequestPacket.
 */
public record CitizensPacket(List<CitizenRequestEntry> entries) implements CustomPacketPayload
{
    /**
     * Une requête d'un citoyen non-builder.
     *
     * @param stack       item demandé (avec count)
     * @param citizenName nom du citoyen
     * @param jobName     nom du job (ex. "Farmer", "Miner"...)
     * @param count       quantité demandée
     */
    public record CitizenRequestEntry(
            ItemStack stack,
            String citizenName,
            String jobName,
            int count,
            boolean availableInME,  // true si l'item est en stock dans le ME (bouton Send actif)
            boolean craftableInME   // true si un pattern existe dans le ME (bouton Craft actif)
    ) {}

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(ColonyLink.MODID, "citizens_packet");
    public static final CustomPacketPayload.Type<CitizensPacket> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, CitizensPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeInt(packet.entries().size());
                for (CitizenRequestEntry e : packet.entries())
                {
                    ItemStack.STREAM_CODEC.encode(buf, e.stack());
                    buf.writeUtf(e.citizenName());
                    buf.writeUtf(e.jobName());
                    buf.writeInt(e.count());
                    buf.writeBoolean(e.availableInME());
                    buf.writeBoolean(e.craftableInME());
                }
            },
            buf -> {
                int size = buf.readInt();
                List<CitizenRequestEntry> list = new ArrayList<>();
                for (int i = 0; i < size; i++)
                {
                    ItemStack stack   = ItemStack.STREAM_CODEC.decode(buf);
                    String citizen    = buf.readUtf();
                    String job        = buf.readUtf();
                    int count         = buf.readInt();
                    boolean avail   = buf.readBoolean();
                    boolean craft   = buf.readBoolean();
                    list.add(new CitizenRequestEntry(stack, citizen, job, count, avail, craft));
                }
                return new CitizensPacket(list);
            }
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }
}