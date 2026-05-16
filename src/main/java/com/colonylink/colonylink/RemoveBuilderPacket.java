package com.colonylink.colonylink;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Envoyé par le client quand le joueur clique "Unlink" dans le GUI.
 * Le serveur retire l'entry à l'index donné de la wand et met à jour activeViewers.
 */
public record RemoveBuilderPacket(int tabIndex) implements CustomPacketPayload
{
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(ColonyLink.MODID, "remove_builder");
    public static final CustomPacketPayload.Type<RemoveBuilderPacket> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, RemoveBuilderPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> buf.writeInt(packet.tabIndex()),
            buf -> new RemoveBuilderPacket(buf.readInt())
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(RemoveBuilderPacket packet, IPayloadContext context)
    {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            ItemStack wandStack = null;
            for (ItemStack s : player.getInventory().items)
                if (s.getItem() instanceof ColonyLinkWand) { wandStack = s; break; }
            if (wandStack == null) return;

            int index = packet.tabIndex();
            java.util.List<BuilderEntry> entries = ColonyLinkWandLinkableHandler.getBuilderEntries(wandStack);
            if (index < 0 || index >= entries.size()) return;

            String removedName = entries.get(index).builderName();
            ColonyLinkWandLinkableHandler.removeEntryAt(wandStack, index);

            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§e[ColonyLink] Builder §f" + removedName + " §eunlinked from Clipboard."));

            java.util.List<BuilderEntry> updated = ColonyLinkWandLinkableHandler.getBuilderEntries(wandStack);
            if (updated.isEmpty())
            {
                ColonyLinkServerTicker.removeViewer(player.getUUID());
            }
            else
            {
                int newActive = ColonyLinkWandLinkableHandler.getActiveTab(wandStack);
                BuilderEntry newEntry = updated.get(newActive);
                ColonyLinkServerTicker.addViewer(player.getUUID(), newEntry.builderPos(), newActive);
                ColonyLinkServerTicker.sendImmediateUpdate(player, newEntry.builderPos(), newActive);
            }
        });
    }
}
