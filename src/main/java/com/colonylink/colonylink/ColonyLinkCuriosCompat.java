package com.colonylink.colonylink;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

/**
 * Facade for Curios API integration.
 *
 * Every public method guards its body with ModList.isLoaded("curios")
 * before delegating to ColonyLinkCuriosHelper.
 *
 * ColonyLinkCuriosHelper (which imports Curios types) is NEVER
 * class-loaded when Curios is absent — no NoClassDefFoundError.
 *
 * All other classes call only this class, never ColonyLinkCuriosHelper directly.
 */
public final class ColonyLinkCuriosCompat
{
    private ColonyLinkCuriosCompat() {}

    /** True if Curios API is present in the current mod list. */
    public static boolean isLoaded()
    {
        return ModList.get().isLoaded("curios");
    }

    /**
     * Registers the Clipboard item as a curio via CuriosApi.registerCurio().
     * Called from FMLCommonSetupEvent (enqueueWork) in ColonyLink.commonSetup().
     * No-op if Curios is not installed.
     */
    public static void registerCurio()
    {
        if (!isLoaded()) return;
        ColonyLinkCuriosHelper.registerCurio();
    }

    /**
     * Searches the player's curio slots for a ColonyLink Wand.
     * Returns ItemStack.EMPTY if Curios is not installed or no wand found.
     */
    public static ItemStack findWandInCurioSlots(ServerPlayer player)
    {
        if (!isLoaded()) return ItemStack.EMPTY;
        return ColonyLinkCuriosHelper.findWandInCurioSlots(player);
    }
}