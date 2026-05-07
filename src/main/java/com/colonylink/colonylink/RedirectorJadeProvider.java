package com.colonylink.colonylink;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;

/**
 * Provider Jade pour le Colony Link Redirector.
 *
 * Affiche dans le HUD :
 * - Nom du bloc avec icône colorée selon l'état
 * - État AE2 (connecté / déconnecté) avec couleur
 * - Builder lié (nom + coordonnées)
 * - Présence de la Warehouse Link Card
 * - Priorité WH/AE2 si card présente
 * - Nombre d'items dans le buffer
 * - Guide de setup si le redirector n'est pas configuré
 */
public class RedirectorJadeProvider implements
        IBlockComponentProvider,
        IServerDataProvider<BlockAccessor>
{
    public static final RedirectorJadeProvider INSTANCE = new RedirectorJadeProvider();

    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(
            ColonyLink.MODID, "redirector");

    // Clés NBT pour le transfert serveur → client
    private static final String KEY_STATE        = "cl_state";
    private static final String KEY_AE2_ACTIVE   = "cl_ae2";
    private static final String KEY_HAS_CARD     = "cl_card";
    private static final String KEY_WH_PRIORITY  = "cl_whprio";
    private static final String KEY_BUILDER_X    = "cl_bx";
    private static final String KEY_BUILDER_Y    = "cl_by";
    private static final String KEY_BUILDER_Z    = "cl_bz";
    private static final String KEY_BUFFER_USED  = "cl_buf_used";
    private static final String KEY_BUFFER_MAX   = "cl_buf_max";

    // ── Données serveur → client ──────────────────────────────────────────────

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor)
    {
        if (!(accessor.getBlockEntity() instanceof ColonyLinkRedirectorBlockEntity be)) return;

        // Recalcule le state à la volée pour garantir la cohérence avec isAe2Active().
        // be.getState() peut être désynchronisé (valeur NBT ancienne) si updateState()
        // n'a pas encore été rappelé depuis le dernier chargement du chunk.
        be.updateState();
        data.putString(KEY_STATE, be.getState().name());
        data.putBoolean(KEY_AE2_ACTIVE, be.isAe2Active());
        data.putBoolean(KEY_HAS_CARD, be.hasWarehouseCard());
        data.putBoolean(KEY_WH_PRIORITY, be.isWarehousePriority());

        if (be.getLinkedBuilderPos() != null)
        {
            data.putInt(KEY_BUILDER_X, be.getLinkedBuilderPos().getX());
            data.putInt(KEY_BUILDER_Y, be.getLinkedBuilderPos().getY());
            data.putInt(KEY_BUILDER_Z, be.getLinkedBuilderPos().getZ());
        }

        // Compte les slots utilisés dans le buffer
        int used = 0;
        for (int slot = 0; slot < be.buffer.getSlots(); slot++)
            if (!be.buffer.getStackInSlot(slot).isEmpty()) used++;

        data.putInt(KEY_BUFFER_USED, used);
        data.putInt(KEY_BUFFER_MAX, be.buffer.getSlots());
    }

    // ── Affichage HUD client ──────────────────────────────────────────────────

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config)
    {
        CompoundTag data = accessor.getServerData();
        if (data.isEmpty()) return;

        IElementHelper helper = IElementHelper.get();

        // ── État AE2 ─────────────────────────────────────────────────────────
        boolean ae2Active = data.getBoolean(KEY_AE2_ACTIVE);
        String ae2Text = ae2Active ? "§aAE2: Connected" : "§cAE2: Disconnected";
        tooltip.add(Component.literal(ae2Text));

        if (!ae2Active)
        {
            tooltip.add(Component.literal("§7Place adjacent to an §fAE2 cable§7."));
            return;
        }

        // ── État du redirector ────────────────────────────────────────────────
        String stateName = data.getString(KEY_STATE);
        ColonyLinkRedirectorBlockEntity.RedirectorState state;
        try { state = ColonyLinkRedirectorBlockEntity.RedirectorState.valueOf(stateName); }
        catch (Exception e) { state = ColonyLinkRedirectorBlockEntity.RedirectorState.NO_CONTROLLER; }

        switch (state)
        {
            case NOT_LINKED ->
            {
                tooltip.add(Component.literal("§eStatus: §6Not linked"));
                tooltip.add(Component.literal("§8──────────────────"));
                tooltip.add(Component.literal("§eSetup required:"));
                tooltip.add(Component.literal("§7 1. Link Wand to a §fWireless Access Point"));
                tooltip.add(Component.literal("§7 2. Sneak+click a §fBuilder's Hut §7with the Wand"));
                tooltip.add(Component.literal("§7 3. Sneak+click §fthis Redirector §7with the Wand"));
            }
            case STANDBY ->
            {
                tooltip.add(Component.literal("§eStatus: §6Standby"));
                tooltip.add(Component.literal("§7Builder's Hut inventory is §cfull§7."));
                tooltip.add(Component.literal("§7Items will resume once space is available."));
                appendLinkedInfo(tooltip, data);
            }
            case LINKED ->
            {
                tooltip.add(Component.literal("§eStatus: §aLinked & Operational"));
                appendLinkedInfo(tooltip, data);
            }
            case NO_CONTROLLER ->
                    tooltip.add(Component.literal("§eStatus: §cNot connected to AE2"));
        }

        // ── Warehouse Link Card ───────────────────────────────────────────────
        tooltip.add(Component.literal("§8──────────────────"));
        boolean hasCard = data.getBoolean(KEY_HAS_CARD);
        if (hasCard)
        {
            tooltip.add(Component.literal("§aWarehouse Card: §fInserted"));
            boolean whPriority = data.getBoolean(KEY_WH_PRIORITY);
            tooltip.add(Component.literal("§7Priority: "
                    + (whPriority ? "§aWarehouse first" : "§9AE2 first")));
        }
        else
        {
            tooltip.add(Component.literal("§8Warehouse Card: §7Empty"));
            tooltip.add(Component.literal("§8Insert a §fWarehouse Link Card §8to"));
            tooltip.add(Component.literal("§8enable warehouse scanning."));
        }

        // ── Buffer ────────────────────────────────────────────────────────────
        int bufUsed = data.getInt(KEY_BUFFER_USED);
        int bufMax  = data.getInt(KEY_BUFFER_MAX);
        if (bufUsed > 0)
        {
            tooltip.add(Component.literal("§8──────────────────"));
            tooltip.add(Component.literal("§7Buffer: §f" + bufUsed + "§7/§f" + bufMax + " §7slots used"));
        }
    }

    private void appendLinkedInfo(ITooltip tooltip, CompoundTag data)
    {
        if (data.contains(KEY_BUILDER_X))
        {
            int bx = data.getInt(KEY_BUILDER_X);
            int by = data.getInt(KEY_BUILDER_Y);
            int bz = data.getInt(KEY_BUILDER_Z);
            tooltip.add(Component.literal("§7Builder: §f" + bx + ", " + by + ", " + bz));
        }
    }

    @Override
    public ResourceLocation getUid() { return UID; }
}