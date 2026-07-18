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
                // v1.6.2 — the client-supplied index is never trusted: a negative value
                // would crash the server ticker (allEntries.get(negative)) and, once
                // persisted to the wand NBT, re-crash every GUI open. Clamp to a valid
                // tab, preserving the Integer.MAX_VALUE sentinel (Citizens tab).
                net.minecraft.world.item.ItemStack _wand1 = ColonyLinkServerTicker.findWandInInventory(serverPlayer);
                int entriesSize = _wand1 != null
                        ? ColonyLinkWandLinkableHandler.getBuilderEntries(_wand1).size() : 0;
                int safeTab = sanitizeTab(packet.activeTabIndex(), entriesSize);

                ColonyLinkServerTicker.addViewer(
                        serverPlayer.getUUID(), packet.builderPos(), safeTab);
                ColonyLinkServerTicker.sendImmediateUpdate(
                        serverPlayer, packet.builderPos(), safeTab);

                if (_wand1 != null)
                    ColonyLinkWandLinkableHandler.setActiveTab(_wand1, safeTab);
            }
            else
            {
                ColonyLinkServerTicker.removeViewer(serverPlayer.getUUID());

                // Fix 3 : sauvegarde la tab active en NBT wand à la fermeture du GUI.
                // activeTabIndex == -1 reste la sentinelle "mode pairing" (message ci-dessous).
                if (packet.activeTabIndex() >= 0)
                {
                    net.minecraft.world.item.ItemStack _wand2 = ColonyLinkServerTicker.findWandInInventory(serverPlayer);
                    if (_wand2 != null)
                    {
                        int entriesSize = ColonyLinkWandLinkableHandler.getBuilderEntries(_wand2).size();
                        ColonyLinkWandLinkableHandler.setActiveTab(_wand2,
                                sanitizeTab(packet.activeTabIndex(), entriesSize));
                    }
                }
                else
                {
                    serverPlayer.sendSystemMessage(net.minecraft.network.chat.Component.translatable(
                            "colonylink.gui.pair_hint"));
                }
            }
        });
    }

    /**
     * v1.6.2 — Clamps a client-supplied tab index to a safe value. The
     * Integer.MAX_VALUE sentinel (Citizens tab) is preserved as-is; any other
     * value is bounded to [0, entriesSize-1]. Negative indices (only reachable
     * from a modified client) would otherwise crash the server ticker and brick
     * the wand NBT.
     */
    private static int sanitizeTab(int raw, int entriesSize)
    {
        if (raw == Integer.MAX_VALUE) return Integer.MAX_VALUE;
        int max = Math.max(0, entriesSize - 1);
        return Math.max(0, Math.min(raw, max));
    }
}