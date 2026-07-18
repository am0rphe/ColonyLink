package com.colonylink.colonylink;

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
 * Ce cache masque le rendu de la BARRE : ColonyLinkWand.getBarWidth/getBarColor
 * lisent la valeur poussée par ColonyLinkPacket (rfStored/rfMax) au lieu de relire
 * le NBT à chaque frame — la barre ne clignote donc plus.
 *
 * Note (v1.6.2) — correction d'une doc trompeuse : le serveur CONTINUE bel et bien
 * de drainer le RF dans le NBT de la wand au repos (drain passif, GUI ouvert : voir
 * ColonyLinkServerTicker.onServerTick + WandEnergyStorage.setStoredRF). Le RF étant
 * stocké en NBT, le décrémenter implique nécessairement une écriture NBT. Ce cache
 * supprime uniquement le clignotement VISUEL de la barre ; l'écriture NBT et la
 * resync de slot associées subsistent (coût réseau minime, décision v1.6.2 =
 * conserver le drain passif tel quel).
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
            max    = ColonyLinkConfig.safeGet(ColonyLinkConfig.WAND_RF_CAPACITY, 160_000L);
        }
        // v1.6.1 — clamp [0..100] : si le cap est abaisse sous le stored, on plafonne
        // l'affichage au lieu d'un pourcentage aberrant (ex. 625%).
        return max > 0 ? (int) Math.max(0L, Math.min(100L, stored * 100L / max)) : 0;
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
            max    = ColonyLinkConfig.safeGet(ColonyLinkConfig.WAND_RF_CAPACITY, 160_000L);
        }
        if (max <= 0) return 0;
        // v1.6.1 — clamp [0..13] : evite que la barre deborde du slot (barre verte qui
        // traverse l'ecran) quand stored > max apres une baisse de capacite.
        return (int) Math.max(0L, Math.min(13L, stored * 13L / max));
    }

    /**
     * Couleur barre durabilité depuis le cache ou le NBT.
     */
    public static int getBarColor(ItemStack stack)
    {
        int pct = getPercent(stack);
        int threshold = ColonyLinkConfig.safeGet(ColonyLinkConfig.LOW_POWER_THRESHOLD_PERCENT, 10);
        if (pct <= threshold) return 0xFF2222; // rouge
        if (pct <= 30)        return 0xFFAA00; // jaune
        return 0x22CC22;                        // vert
    }
}