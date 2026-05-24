package com.colonylink.colonylink;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
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
    private static final String KEY_BUFFER_USED    = "cl_buf_used";
    private static final String KEY_BUFFER_MAX     = "cl_buf_max";
    private static final String KEY_DOMUM_PATTERNS = "cl_domum";
    private static final String KEY_DOMUM_INFO     = "cl_domum_info"; // "name|variant;name|variant;..."

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

        // Compte les DomumPatternItems + collecte leurs infos
        int domumCount = 0;
        StringBuilder domumInfo = new StringBuilder();
        for (int slot = 0; slot < be.buffer.getSlots(); slot++)
        {
            net.minecraft.world.item.ItemStack s = be.buffer.getStackInSlot(slot);
            if (!(s.getItem() instanceof DomumPatternItem)) continue;
            domumCount++;

            // Nom de l'item cible
            net.minecraft.world.item.ItemStack target =
                    DomumPatternItem.decodeTarget(s, accessor.getLevel().registryAccess());
            String name = target != null ? target.getDisplayName().getString() : "?";

            // Variant
            net.minecraft.world.item.component.BlockItemStateProperties bs =
                    s.get(net.minecraft.core.component.DataComponents.BLOCK_STATE);
            String variant = "";
            if (bs != null && !bs.properties().isEmpty())
                variant = bs.properties().entrySet().stream()
                        .map(e -> e.getKey() + "=" + e.getValue())
                        .collect(java.util.stream.Collectors.joining(","));

            if (domumInfo.length() > 0) domumInfo.append(";");
            domumInfo.append(name).append("|").append(variant);
        }

        data.putInt(KEY_BUFFER_USED, used);
        data.putInt(KEY_BUFFER_MAX, be.buffer.getSlots());
        data.putInt(KEY_DOMUM_PATTERNS, domumCount);
        data.putString(KEY_DOMUM_INFO, domumInfo.toString());


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
                tooltip.add(Component.literal("§7 1. Link Clipboard to a §fWireless Access Point"));
                tooltip.add(Component.literal("§7 2. Sneak+click a §fBuilder's Hut §7with the Clipboard"));
                tooltip.add(Component.literal("§7 3. Sneak+click §fthis Redirector §7with the Clipboard"));
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
        int bufUsed     = data.getInt(KEY_BUFFER_USED);
        int bufMax      = data.getInt(KEY_BUFFER_MAX);
        int domumCount  = data.getInt(KEY_DOMUM_PATTERNS);
        String domumInfo = data.getString(KEY_DOMUM_INFO);
        tooltip.add(Component.literal("§8──────────────────"));
        if (bufUsed > 0)
        {
            tooltip.add(Component.literal("§7Buffer: §f" + bufUsed + "§7/§f" + bufMax
                    + " §7slots" + (domumCount > 0 ? " §8(§f" + domumCount + " §8Domum)" : "")));

            // Détail des DomumPatterns (nom + variant) — shift only
            if (domumCount > 0 && !domumInfo.isEmpty()
                    && net.minecraft.client.gui.screens.Screen.hasShiftDown())
            {
                for (String entry : domumInfo.split(";"))
                {
                    String[] parts = entry.split("\\|", 2);
                    String name    = parts[0];
                    String variant = parts.length > 1 && !parts[1].isEmpty() ? " §8[" + parts[1] + "]" : "";
                    tooltip.add(Component.literal("§7  • §f" + name + variant));
                }
            }
            else if (domumCount > 0)
            {
                tooltip.add(Component.literal("§8Hold §eShift §8to see Domum patterns."));
            }
        }
        else
        {
            tooltip.add(Component.literal("§8Buffer: §7empty — add §fDomum Patterns §7to enable crafting."));
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