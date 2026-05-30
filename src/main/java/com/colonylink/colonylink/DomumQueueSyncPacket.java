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
 * DomumQueueSyncPacket — v1.4.8
 *
 * Envoyé S→C à tous les viewers du WarehouseLinkTerminal quand la queue Domum change.
 * Transporte la liste complète des ItemStack en attente d'encodage.
 *
 * Stockage côté client : TerminalClientPacketHandler.domumQueue (List<ItemStack>).
 */
public record DomumQueueSyncPacket(List<ItemStack> queue) implements CustomPacketPayload
{
    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(ColonyLink.MODID, "domum_queue_sync");
    public static final CustomPacketPayload.Type<DomumQueueSyncPacket> TYPE =
            new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, DomumQueueSyncPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, packet) -> {
                        buf.writeVarInt(packet.queue().size());
                        for (ItemStack stack : packet.queue())
                            ItemStack.STREAM_CODEC.encode(buf, stack);
                    },
                    buf -> {
                        int size = buf.readVarInt();
                        List<ItemStack> list = new ArrayList<>(size);
                        for (int i = 0; i < size; i++)
                            list.add(ItemStack.STREAM_CODEC.decode(buf));
                        return new DomumQueueSyncPacket(list);
                    }
            );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(DomumQueueSyncPacket packet, IPayloadContext ctx)
    {
        // Handler délégué via ColonyLink.registerPayloads() → TerminalClientPacketHandler
        // Ne pas appeler TerminalClientPacketHandler ici — classe @OnlyIn(CLIENT),
        // le classloader la chargerait côté serveur dédié au chargement de ce packet.
    }
}