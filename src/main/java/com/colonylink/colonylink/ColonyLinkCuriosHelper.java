package com.colonylink.colonylink;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

/**
 * Curios API integration for the ColonyLink Clipboard (wand).
 *
 * This class MUST NOT be referenced directly from any other class.
 * All calls go through {@link ColonyLinkCuriosCompat}, which guards
 * every call with ModList.isLoaded("curios").
 *
 * Registration: CuriosApi.registerCurio() in FMLCommonSetupEvent.
 *
 * ICurioItem notes:
 *   - canEquip/canUnequip are default methods whose signatures changed
 *     across Curios versions — we do NOT override them. The defaults
 *     accept the item in any slot, which is exactly what we want.
 *   - curioTick() is the only method we implement (no-op: the server
 *     ticker already handles the wand via findWandInInventory).
 */
public final class ColonyLinkCuriosHelper
{
    private ColonyLinkCuriosHelper() {}

    /**
     * Registers the Clipboard as a curio item.
     * Called from commonSetup (FMLCommonSetupEvent) via ColonyLinkCuriosCompat.
     */
    public static void registerCurio()
    {
        CuriosApi.registerCurio(ColonyLink.COLONY_LINK_WAND.get(), new ClipboardCurio());
    }

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

    // ── ICurioItem implementation ─────────────────────────────────────────────

    private static final class ClipboardCurio implements ICurioItem
    {
        /**
         * No tick logic needed — ColonyLinkServerTicker handles the wand
         * via findWandInInventory() which already checks curio slots.
         */
        @Override
        public void curioTick(SlotContext slotContext, ItemStack stack) {}
    }
}