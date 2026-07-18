package com.colonylink.colonylink;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;

/**
 * Curios API integration for the ColonyLink Clipboard (wand).
 *
 * This class MUST NOT be referenced directly from any other class.
 * All calls go through {@link ColonyLinkCuriosCompat}, which guards
 * every call with ModList.isLoaded("curios").
 *
 * v1.6.2 — no explicit CuriosApi.registerCurio() is needed: in Curios 9.x the
 * wand is made equippable through data-driven slot tags (verified in game). Only
 * the read path (findWandInCurioSlots) remains, used by the server ticker.
 */
public final class ColonyLinkCuriosHelper
{
    private ColonyLinkCuriosHelper() {}

    /**
     * Searches Curios slots on the given server player for a ColonyLink Wand.
     * Returns ItemStack.EMPTY if none found.
     */
    public static ItemStack findWandInCurioSlots(ServerPlayer player)
    {
        return CuriosApi.getCuriosInventory(player)
                .map(handler ->
                {
                    var result = handler.findFirstCurio(ColonyLink.COLONY_LINK_WAND.get());
                    return result.map(r -> r.stack()).orElse(ItemStack.EMPTY);
                })
                .orElse(ItemStack.EMPTY);
    }
}
