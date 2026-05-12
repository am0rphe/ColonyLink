package com.colonylink.colonylink;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import net.neoforged.neoforge.energy.IEnergyStorage;

/**
 * Implémentation d'IEnergyStorage pour la ColonyLink Wand.
 *
 * Stocke le RF en NBT dans la CustomData de l'item (clé "wand_rf").
 * Expose la capability forge:energy pour être compatible avec :
 *   - Mods de charge tiers (Powah, Mekanism, IE, Mining Gadgets…)
 *   - Le slot output du WAP (ColonyLinkWandCharger)
 *
 * La clé "wand_rf" est intentionnellement SÉPARÉE des autres données wand
 * pour ne PAS être effacée par ClearNbtRecipe (qui reconstruit l'item vierge
 * sans copier la CustomData — voir ClearNbtRecipe.assemble()).
 * → ClearNbtRecipe crée un new ItemStack propre : le RF ne survit donc pas.
 *   Pour préserver le RF, ClearNbtRecipe.assemble() copie explicitement
 *   la clé "wand_rf" depuis l'input vers l'output.
 *
 * Capacité et transfer rate lus depuis ColonyLinkConfig à chaque appel
 * (hot-reloadable sans redémarrage).
 */
public class WandEnergyStorage implements IEnergyStorage
{
    /** Clé NBT dans CustomData de l'item. */
    public static final String NBT_KEY = "wand_rf";

    private final ItemStack stack;

    public WandEnergyStorage(ItemStack stack)
    {
        this.stack = stack;
    }

    // ── Lecture / écriture NBT ────────────────────────────────────────────────

    public static long getStoredRF(ItemStack stack)
    {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return 0L;
        var tag = data.copyTag();
        return tag.contains(NBT_KEY) ? tag.getLong(NBT_KEY) : 0L;
    }

    public static void setStoredRF(ItemStack stack, long rf)
    {
        long clamped = Math.max(0, Math.min(rf, ColonyLinkConfig.WAND_RF_CAPACITY.get()));
        stack.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, data -> {
            var tag = data.copyTag();
            tag.putLong(NBT_KEY, clamped);
            return CustomData.of(tag);
        });
    }

    /**
     * Tente de consommer {@code amount} RF du buffer de la wand.
     * @return true si la consommation a réussi (ou si le blocage est désactivé en config)
     */
    public static boolean tryConsume(ItemStack stack, long amount)
    {
        if (amount <= 0) return true;
        long stored = getStoredRF(stack);
        boolean blockIfNoPower = ColonyLinkConfig.BLOCK_ACTIONS_IF_NO_POWER.get();

        if (stored < amount)
        {
            if (blockIfNoPower) return false;
            // Consomme ce qui reste
            setStoredRF(stack, 0);
            return true;
        }

        setStoredRF(stack, stored - amount);
        return true;
    }

    // ── IEnergyStorage ────────────────────────────────────────────────────────

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate)
    {
        long stored   = getStoredRF(stack);
        long capacity = ColonyLinkConfig.WAND_RF_CAPACITY.get();
        long rate     = ColonyLinkConfig.WAND_RF_TRANSFER_RATE.get();
        long toInsert = Math.min(maxReceive, Math.min(rate, capacity - stored));
        if (toInsert <= 0) return 0;
        if (!simulate)
            setStoredRF(stack, stored + toInsert);
        return (int) Math.min(toInsert, Integer.MAX_VALUE);
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate)
    {
        // La wand ne distribue pas d'énergie vers l'extérieur
        return 0;
    }

    @Override
    public int getEnergyStored()
    {
        return (int) Math.min(getStoredRF(stack), Integer.MAX_VALUE);
    }

    @Override
    public int getMaxEnergyStored()
    {
        return (int) Math.min(ColonyLinkConfig.WAND_RF_CAPACITY.get(), Integer.MAX_VALUE);
    }

    @Override
    public boolean canExtract() { return false; }

    @Override
    public boolean canReceive() { return true; }
}