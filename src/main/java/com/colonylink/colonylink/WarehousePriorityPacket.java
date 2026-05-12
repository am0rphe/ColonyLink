package com.colonylink.colonylink;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record WarehousePriorityPacket(BlockPos redirectorPos) implements CustomPacketPayload
{
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(
            ColonyLink.MODID, "warehouse_priority");
    public static final CustomPacketPayload.Type<WarehousePriorityPacket> TYPE =
            new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, WarehousePriorityPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> buf.writeBlockPos(packet.redirectorPos()),
            buf -> new WarehousePriorityPacket(buf.readBlockPos())
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }

    public static void handle(WarehousePriorityPacket packet, IPayloadContext context)
    {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) return;

            ServerPlayer player = serverPlayer;
            var be = player.serverLevel().getBlockEntity(packet.redirectorPos());

            // Supporte AE2 et RS2
            if (be instanceof ColonyLinkRedirectorBlockEntity redirector)
            {
                if (!redirector.hasWarehouseCard()) return;
                redirector.toggleWarehousePriority();
            }
            else if (be instanceof ColonyLinkRedirectorBlockEntityRS redirectorRS)
            {
                if (!redirectorRS.hasWarehouseCard()) return;
                redirectorRS.toggleWarehousePriority();
            }
        });
    }
}