package com.colonylink.colonylink;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;

/**
 * DomumQueuePacket — v1.4.8
 *
 * Envoyé C→S quand le joueur clique "No Pattern" sur un item Domum dans le Clipboard.
 * Le serveur ajoute l'item à la queue du WarehouseLinkTerminalPart lié au Redirector.
 *
 * redirectorPos : position du Redirector dont provient la requête Domum.
 * domumStack    : l'item Domum exact (bloc + DataComponents complets).
 */
public record DomumQueuePacket(
        BlockPos  redirectorPos,
        ItemStack domumStack
) implements CustomPacketPayload
{
    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(ColonyLink.MODID, "domum_queue");
    public static final CustomPacketPayload.Type<DomumQueuePacket> TYPE =
            new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, DomumQueuePacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, packet) -> {
                        buf.writeBlockPos(packet.redirectorPos());
                        ItemStack.STREAM_CODEC.encode(buf, packet.domumStack());
                    },
                    buf -> new DomumQueuePacket(
                            buf.readBlockPos(),
                            ItemStack.STREAM_CODEC.decode(buf)
                    )
            );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(DomumQueuePacket packet, IPayloadContext ctx)
    {
        ctx.enqueueWork(() -> {
            if (ctx.player() instanceof ServerPlayer sp) handleServer(sp, packet);
        });
    }

    private static void handleServer(ServerPlayer player, DomumQueuePacket packet)
    {
        ColonyLink.LOGGER.debug("[DomumQueue] Received packet from {} — item: {}",
                player.getName().getString(),
                net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(packet.domumStack().getItem()));

        if (!DomumCraftHandler.isDomumItem(packet.domumStack()))
        {
            player.sendSystemMessage(Component.translatable("colonylink.domum_queue.invalid",
                    net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(packet.domumStack().getItem()).toString()));
            return;
        }

        ServerLevel level = player.serverLevel();

        // v1.6.1 — Resout la GRILLE ME du Redirector (via redirectorPos) et ajoute l'item a
        // la queue Domum PARTAGEE de cette grille (DomumQueueManager). L'item apparait alors
        // sur TOUS les terminaux du reseau, pas seulement un. La queue partagee est la source
        // de verite ; chaque terminal garde une copie NBT comme filet de persistance.
        IGrid redirectorGrid = null;
        if (level.getBlockEntity(packet.redirectorPos()) instanceof ColonyLinkRedirectorBlockEntity redirector)
        {
            IGridNode rnode = redirector.getActionableNode();
            if (rnode != null) redirectorGrid = rnode.getGrid();
        }

        if (redirectorGrid == null
                || ColonyLinkServerTicker.findLiveTerminalsForGrid(level, redirectorGrid).isEmpty())
        {
            player.sendSystemMessage(Component.translatable("colonylink.domum_queue.no_terminal",
                    packet.domumStack().getDisplayName()));
            return;
        }

        boolean added = WarehouseLinkTerminalPart.queueDomumOnGrid(
                level, redirectorGrid, packet.domumStack());

        player.sendSystemMessage(Component.translatable(
                added ? "colonylink.domum_queue.sent" : "colonylink.domum_queue.already_queued"));
    }
}