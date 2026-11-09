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

            // v1.6.2 — redirectorPos is client-supplied: only allow toggling a
            // redirector actually linked to THIS player's wand (mirrors the
            // WarehouseCraft validation). Otherwise a modified client could flip
            // another player's redirector priority (griefing).
            var wand = ColonyLinkServerTicker.findWandInInventory(serverPlayer);
            if (wand == null) return;
            BuilderEntry entry = ColonyLinkWandLinkableHandler.findEntryByRedirectorPos(wand, packet.redirectorPos());
            if (entry == null) return;

            // v1.7.1 — colony permission check (mirrors RestartBuilderPacket / LocateBuilderPacket).
            var colony = com.minecolonies.api.colony.IColonyManager.getInstance()
                    .getClosestColony(serverPlayer.serverLevel(), entry.builderPos());
            if (colony != null && !colony.getPermissions().hasPermission(
                    serverPlayer, com.minecolonies.api.colony.permissions.Action.ACCESS_HUTS))
            {
                serverPlayer.sendSystemMessage(
                        net.minecraft.network.chat.Component.translatable("colonylink.wand.msg.no_permission"));
                return;
            }

            // 1.7.0-beta.4 — by the entry's Redirector UUID first (follows a moved Redirector).
            var be = RedirectorRegistry.forEntry(serverPlayer.serverLevel(), entry);
            if (!(be instanceof ColonyLinkRedirectorBlockEntity redirector)) return;
            if (!redirector.hasWarehouseCard()) return;

            redirector.toggleWarehousePriority();
        });
    }
}
