package com.colonylink.colonylink;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Packet S→C : résultat du scan warehouse.
 *
 * Contient une liste d'entrées enrichies : pour chaque ressource builder,
 * la quantité disponible en warehouse (brut + via craft récursif).
 *
 * Le client stocke ce snapshot dans ColonyLinkScreen.
 * Un timestamp (ticks serveur) permet d'invalider le snapshot après 400 ticks.
 */
public record WarehouseResultPacket(
        List<WarehouseEntry> entries,
        long scanTimestamp,
        boolean scanSuccess
) implements CustomPacketPayload
{
    /**
     * Entrée warehouse pour un item builder.
     *
     * @param stack         l'item demandé par le builder
     * @param inWarehouse   quantité directement disponible en warehouse (sans craft)
     * @param viaCraft      quantité supplémentaire accessible via craft récursif depuis warehouse
     * @param tooltipLines  lignes de détail pour le tooltip (composants Domum, arbre de craft, etc.)
     */
    public record WarehouseEntry(
            ItemStack stack,
            long inWarehouse,
            long viaCraft,
            List<Component> tooltipLines
    ) {}

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(
            ColonyLink.MODID, "warehouse_result");
    public static final CustomPacketPayload.Type<WarehouseResultPacket> TYPE =
            new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, WarehouseResultPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeInt(packet.entries().size());
                for (WarehouseEntry e : packet.entries())
                {
                    ItemStack.STREAM_CODEC.encode(buf, e.stack());
                    buf.writeLong(e.inWarehouse());
                    buf.writeLong(e.viaCraft());
                    buf.writeInt(e.tooltipLines().size());
                    for (Component line : e.tooltipLines())
                        ComponentSerialization.STREAM_CODEC.encode(buf, line);
                }
                buf.writeLong(packet.scanTimestamp());
                buf.writeBoolean(packet.scanSuccess());
            },
            buf -> {
                int size = buf.readInt();
                List<WarehouseEntry> entries = new ArrayList<>();
                for (int i = 0; i < size; i++)
                {
                    ItemStack stack = ItemStack.STREAM_CODEC.decode(buf);
                    long inWarehouse = buf.readLong();
                    long viaCraft = buf.readLong();
                    int tooltipCount = buf.readInt();
                    List<Component> tooltipLines = new ArrayList<>();
                    for (int t = 0; t < tooltipCount; t++)
                        tooltipLines.add(ComponentSerialization.STREAM_CODEC.decode(buf));
                    entries.add(new WarehouseEntry(stack, inWarehouse, viaCraft, tooltipLines));
                }
                long timestamp = buf.readLong();
                boolean success = buf.readBoolean();
                return new WarehouseResultPacket(entries, timestamp, success);
            }
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }
}