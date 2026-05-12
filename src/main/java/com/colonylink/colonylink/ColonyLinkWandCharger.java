package com.colonylink.colonylink;

import appeng.api.implementations.blockentities.IWirelessAccessPoint;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

/**
 * Gère la charge RF de la ColonyLink Wand via le slot output du WAP AE2.
 *
 * Mécanisme :
 * ──────────
 * Le Wireless Access Point AE2 expose son inventaire via la capability
 * Capabilities.ItemHandler.BLOCK. Le slot 1 (index 1) est le slot "output"
 * visible en bas à droite dans son GUI — c'est là que la wand liée sort
 * après le processus de link AE2 (GridLinkables).
 *
 * À chaque cycle de 40 ticks (ServerTicker), pour chaque joueur actif :
 *   1. On localise le WAP lié à la wand du joueur
 *   2. On vérifie si la wand est présente dans le slot 1 du WAP
 *      OU dans l'inventaire du joueur (charge en poche)
 *   3. Si la wand est dans le slot WAP, on lui injecte des RF depuis
 *      l'énergie du réseau AE2 (IEnergyService)
 *
 * Charge en poche (inventaire joueur) :
 * ──────────────────────────────────────
 * Les mods tiers (Powah, Mekanism, IE, Mining Gadgets…) chargent via la
 * capability IEnergyStorage enregistrée sur l'item wand. Pas de logique
 * supplémentaire nécessaire ici — ils appellent directement receiveEnergy().
 *
 * Note sur le slot WAP :
 * ──────────────────────
 * AE2 expose 2 slots via IItemHandler sur le WAP :
 *   slot 0 = slot INPUT (où on insère la wand pour la lier)
 *   slot 1 = slot OUTPUT (la wand liée, visible en bas à droite)
 * On lit le slot 1 pour détecter la wand et lui injecter du RF.
 */
public class ColonyLinkWandCharger
{
    /** Index du slot output du WAP AE2 (bas à droite dans son GUI). */
    private static final int WAP_OUTPUT_SLOT = 1;

    /**
     * Tente de charger la wand dans le slot output du WAP lié.
     * Appelé par ColonyLinkServerTicker toutes les 40 ticks par joueur actif.
     *
     * @param wandStack la wand dans l'inventaire du joueur (pour lire le WAP lié)
     * @param level     niveau serveur courant
     */
    public static void tryChargeFromWap(ItemStack wandStack, ServerLevel level)
    {
        // Récupère la position du WAP lié à cette wand
        net.minecraft.core.GlobalPos linkedPos = ColonyLinkWandLinkableHandler.getLinkedPos(wandStack);
        if (linkedPos == null) return;

        ServerLevel wapLevel = level.getServer().getLevel(linkedPos.dimension());
        if (wapLevel == null) return;

        BlockPos wapPos = linkedPos.pos();

        // Vérifie que c'est bien un WAP
        var be = wapLevel.getBlockEntity(wapPos);
        if (!(be instanceof IWirelessAccessPoint wap)) return;

        // Récupère le réseau AE2 pour l'énergie
        var grid = wap.getGrid();
        if (grid == null) return;
        var energyService = grid.getEnergyService();

        // Récupère le slot output du WAP via IItemHandler
        IItemHandler wapHandler = wapLevel.getCapability(
                Capabilities.ItemHandler.BLOCK, wapPos, null);
        if (wapHandler == null || wapHandler.getSlots() <= WAP_OUTPUT_SLOT) return;

        ItemStack inOutputSlot = wapHandler.getStackInSlot(WAP_OUTPUT_SLOT);
        if (inOutputSlot.isEmpty() || !(inOutputSlot.getItem() instanceof ColonyLinkWand)) return;

        // Calcule combien de RF on peut injecter
        long stored   = WandEnergyStorage.getStoredRF(inOutputSlot);
        long capacity = ColonyLinkConfig.WAND_RF_CAPACITY.get();
        long rate     = ColonyLinkConfig.WAND_RF_TRANSFER_RATE.get();
        long space    = capacity - stored;
        if (space <= 0) return;

        long toCharge = Math.min(space, rate);

        // AE2 stocke en AE interne (1 AE ≈ 2 RF par défaut AE2)
        // On extrait en AE et on convertit → RF
        // PowerMultiplier.CONFIG tient compte du ratio configuré dans AE2
        double aeNeeded = toCharge / 2.0; // ratio par défaut
        double aeExtracted = energyService.extractAEPower(
                aeNeeded, appeng.api.config.Actionable.MODULATE,
                appeng.api.config.PowerMultiplier.CONFIG);

        if (aeExtracted <= 0) return;

        long rfCharged = (long) (aeExtracted * 2.0);
        WandEnergyStorage.setStoredRF(inOutputSlot, stored + rfCharged);
    }

    /**
     * Retourne true si la wand dans l'inventaire du joueur manque de RF
     * (en dessous du seuil configuré).
     */
    public static boolean isLowPower(ItemStack wandStack)
    {
        long stored   = WandEnergyStorage.getStoredRF(wandStack);
        long capacity = ColonyLinkConfig.WAND_RF_CAPACITY.get();
        int threshold = ColonyLinkConfig.LOW_POWER_THRESHOLD_PERCENT.get();
        return (stored * 100L / capacity) < threshold;
    }

    /**
     * Retourne true si la wand est complètement vide.
     */
    public static boolean isEmpty(ItemStack wandStack)
    {
        return WandEnergyStorage.getStoredRF(wandStack) <= 0;
    }
}