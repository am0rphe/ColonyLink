package com.colonylink.colonylink;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Envoyé par le client au serveur pour :
 * - open=true  : le joueur ouvre le GUI (activeTabIndex = tab active)
 * - open=false : le joueur ferme le GUI
 *
 * activeTabIndex ignoré si open=false, sauf si == -1 (bouton "+", mode pairing).
 */
public record GuiStatePacket(boolean open, BlockPos builderPos, int activeTabIndex)
        implements CustomPacketPayload
{
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(
            ColonyLink.MODID, "gui_state");
    public static final CustomPacketPayload.Type<GuiStatePacket> TYPE =
            new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, GuiStatePacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeBoolean(packet.open());
                buf.writeBlockPos(packet.builderPos());
                buf.writeInt(packet.activeTabIndex());
            },
            buf -> new GuiStatePacket(buf.readBoolean(), buf.readBlockPos(), buf.readInt())
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(GuiStatePacket packet, IPayloadContext context)
    {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) return;

            if (packet.open())
            {
                ColonyLinkServerTicker.addViewer(
                        serverPlayer.getUUID(), packet.builderPos(), packet.activeTabIndex());
                ColonyLinkServerTicker.sendImmediateUpdate(
                        serverPlayer, packet.builderPos(), packet.activeTabIndex());

                net.minecraft.world.item.ItemStack _wand1 = ColonyLinkServerTicker.findWandInInventory(serverPlayer);
                if (_wand1 != null)
                    ColonyLinkWandLinkableHandler.setActiveTab(_wand1, packet.activeTabIndex());
            }
            else
            {
                ColonyLinkServerTicker.removeViewer(serverPlayer.getUUID());

                // Fix 3 : sauvegarde la tab active en NBT wand à la fermeture du GUI
                if (packet.activeTabIndex() >= 0)
                {
                    net.minecraft.world.item.ItemStack _wand2 = ColonyLinkServerTicker.findWandInInventory(serverPlayer);
                    if (_wand2 != null)
                        ColonyLinkWandLinkableHandler.setActiveTab(_wand2, packet.activeTabIndex());
                }
                else
                {
                    serverPlayer.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                            "§a[ColonyLink] §fSneak + right-click a Builder's Hut to pair a new builder."));
                }
            }
        });
    }
}