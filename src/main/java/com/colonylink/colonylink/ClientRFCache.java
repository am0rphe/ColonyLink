package com.colonylink.colonylink;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

/**
 * Cache RF côté client uniquement.
 *
 * Problème résolu :
 * ─────────────────
 * Quand le serveur modifie le NBT d'un ItemStack dans l'inventaire du joueur
 * (via stack.update(DataComponents.CUSTOM_DATA, ...)), Minecraft envoie un
 * ClientboundContainerSetSlotPacket qui force le client à remplacer visuellement
 * l'item entier — ce qui provoque le "pop/flash" visible en hotbar toutes les 2s.
 *
 * Solution :
 * ──────────
 * Le serveur ne touche PLUS au NBT de la wand pour le drain passif.
 * Il envoie la valeur RF dans ColonyLinkPacket (rfStored/rfMax) toutes les 40t.
 * Le client stocke cette valeur ici et ColonyLinkWand.getBarWidth/getBarColor
 * lisent ce cache au lieu du NBT — pas de re-render item, pas de pop.
 *
 * Le NBT réel n'est modifié que lors des actions (craft/send) où le coût est
 * prélevé côté serveur, ce qui est acceptable (action ponctuelle, pas de loop).
 *
 * Usage :
 * ───────
 * ColonyLinkPacket.handle() → ClientRFCache.update(rfStored, rfMax)
 * ColonyLinkWand.getBarWidth() → ClientRFCache.getStoredRF()
 * ColonyLinkWand.getBarColor() → ClientRFCache.getStoredRF()
 */
public class ClientRFCache
{
    // Valeur RF reçue du dernier packet serveur
    private static long cachedRF  = 0L;
    private static long cachedMax = 1_600_000L;

    // Timestamp de la dernière mise à jour (ms) — pour invalidation si GUI fermé longtemps
    private static long lastUpdateMs = 0L;
    private static final long CACHE_TTL_MS = 10_000L; // 10s sans packet → retour au NBT

    /**
     * Appelé par ColonyLinkPacket.handle() à chaque packet reçu du serveur.
     */
    public static void update(long rfStored, long rfMax)
    {
        cachedRF      = rfStored;
        cachedMax     = rfMax > 0 ? rfMax : 1_600_000L;
        lastUpdateMs  = System.currentTimeMillis();
    }

    /**
     * Invalide le cache (GUI fermé, joueur déconnecté, etc.)
     */
    public static void invalidate()
    {
        lastUpdateMs = 0L;
    }

    /**
     * Retourne true si le cache est valide (GUI ouvert récemment).
     */
    public static boolean isValid()
    {
        return lastUpdateMs > 0L
                && (System.currentTimeMillis() - lastUpdateMs) < CACHE_TTL_MS;
    }

    /**
     * RF actuellement stockés selon le serveur.
     * Si le cache est invalide, retourne -1 (signal pour lire le NBT).
     */
    public static long getStoredRF()
    {
        return isValid() ? cachedRF : -1L;
    }

    public static long getMaxRF()
    {
        return cachedMax;
    }

    /**
     * Retourne le % de charge (0-100).
     * Si le cache est invalide, calcule depuis le NBT du stack passé.
     */
    public static int getPercent(ItemStack stack)
    {
        long stored, max;
        if (isValid())
        {
            stored = cachedRF;
            max    = cachedMax;
        }
        else
        {
            stored = WandEnergyStorage.getStoredRF(stack);
            max    = ColonyLinkConfig.WAND_RF_CAPACITY.get();
        }
        return max > 0 ? (int)(stored * 100L / max) : 0;
    }

    /**
     * Largeur barre durabilité (0..13) depuis le cache ou le NBT.
     */
    public static int getBarWidth(ItemStack stack)
    {
        long stored, max;
        if (isValid())
        {
            stored = cachedRF;
            max    = cachedMax;
        }
        else
        {
            stored = WandEnergyStorage.getStoredRF(stack);
            max    = ColonyLinkConfig.WAND_RF_CAPACITY.get();
        }
        if (max <= 0) return 0;
        return (int)(stored * 13L / max);
    }

    /**
     * Couleur barre durabilité depuis le cache ou le NBT.
     */
    public static int getBarColor(ItemStack stack)
    {
        int pct = getPercent(stack);
        int threshold = ColonyLinkConfig.LOW_POWER_THRESHOLD_PERCENT.get();
        if (pct <= threshold) return 0xFF2222; // rouge
        if (pct <= 30)        return 0xFFAA00; // jaune
        return 0x22CC22;                        // vert
    }
}