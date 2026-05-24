package com.colonylink.colonylink;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

/**
 * C→S : le client demande l'ouverture du GUI du Clipboard via keybind.
 *
 * Le serveur localise le wand dans l'inventaire du joueur et appelle
 * la même logique que ColonyLinkWand.use() — sans nécessiter que le
 * joueur tienne le wand en main.
 *
 * Packet sans payload (aucune donnée à transmettre).
 */
public record OpenWandGuiPacket() implements CustomPacketPayload
{
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(
            ColonyLink.MODID, "open_wand_gui");
    public static final CustomPacketPayload.Type<OpenWandGuiPacket> TYPE =
            new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenWandGuiPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, packet) -> { /* rien à écrire */ },
                    buf -> new OpenWandGuiPacket()
            );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }

    public static void handle(OpenWandGuiPacket packet, IPayloadContext context)
    {
        context.enqueueWork(() ->
        {
            if (!(context.player() instanceof ServerPlayer sp)) return;

            // Cherche le wand dans l'inventaire (même logique que ColonyLinkServerTicker)
            ItemStack wand = ColonyLinkServerTicker.findWandInInventory(sp);
            if (wand == null)
            {
                sp.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "§c[ColonyLink] No Clipboard found in inventory."));
                return;
            }

            // Vérifications identiques à ColonyLinkWand.use()
            if (WandEnergyStorage.getStoredRF(wand) <= 0)
            {
                sp.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "§c[ColonyLink] Out of Power! Charge Clipboard in AE2 Charger or FE charger."));
                return;
            }

            if (!ColonyLinkWandLinkableHandler.isLinked(wand))
            {
                sp.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "§cThis Clipboard is not linked to an AE2 network!"));
                return;
            }

            List<BuilderEntry> entries = ColonyLinkWandLinkableHandler.getBuilderEntries(wand);
            if (entries.isEmpty())
            {
                sp.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "§eNo Builder's Hut linked yet. Sneak + Right-click a Builder's Hut first."));
                return;
            }

            int activeTab = ColonyLinkWandLinkableHandler.getActiveTab(wand);
            BuilderEntry active = entries.get(Math.min(activeTab, entries.size() - 1));

            ServerLevel sl = sp.serverLevel();

            // Délègue à ColonyLinkWand.openGUIStatic() — méthode extraite pour
            // être appelable sans instance (voir ColonyLinkWand).
            ColonyLinkWand.openGUIStatic(wand, sp, sl, active.builderPos(), activeTab);
        });
    }
}