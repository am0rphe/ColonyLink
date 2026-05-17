package com.colonylink.colonylink;

import appeng.api.util.AEColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

/**
 * Client-side registration for ColonyLink visuals.
 *
 * Currently handles:
 *   - ItemColor handler for the Warehouse Link Terminal item — applies the
 *     AE2 `AEColor.TRANSPARENT` variants to tintindex 1/2/3/4, matching the
 *     pattern used by AE2's own terminal items (see appeng.init.client.InitItemColors).
 *
 * Why this is necessary:
 *   The terminal item model inherits from `ae2:item/display_base`, which uses
 *   tintindex 1, 2, 3, and 4 on the illumination layers (lightsBright/Medium/Dark
 *   and the optional medium_bright). Without an ItemColor handler, these tintindex
 *   return 0xFFFFFFFF (white) — the item appears blank in the hand and in GUIs.
 *
 * Why we don't need a BlockColor handler for the part:
 *   AE2 registers `CableBusColor` on its `CABLE_BUS` block. Since our Part is
 *   placed on a cable bus, the cable bus's BlockColor handler is what colors
 *   our tintindex in-world — no action needed on our side.
 *
 * This class is only loaded on the client (via OnlyIn(Dist.CLIENT)) and is
 * wired up in ColonyLink::registerClientStuff.
 */
@OnlyIn(Dist.CLIENT)
public final class ColonyLinkClient
{
    private ColonyLinkClient() {}

    /**
     * Registers ItemColor handlers for ColonyLink items.
     * Called from the mod event bus via RegisterColorHandlersEvent.Item.
     */
    public static void onRegisterItemColors(RegisterColorHandlersEvent.Item event)
    {
        // Warehouse Link Terminal — same pattern as AE2's CraftingTerminal item:
        // a StaticItemColor with AEColor.TRANSPARENT (the "unpainted" colour).
        // Alpha is forced to 255 via makeOpaque, mirroring AE2's InitItemColors.
        event.register(
                makeOpaque(new StaticItemColor(AEColor.TRANSPARENT)),
                ColonyLinkRegistry.WAREHOUSE_LINK_TERMINAL_ITEM.get());
    }

    // ── Internal: replicates appeng.client.render.StaticItemColor ─────────────
    //
    // We can't directly use AE2's StaticItemColor because it lives in
    // appeng.client.render (not part of the public API). The logic is trivial:
    // for any tintIndex, return AEColor.getVariantByTintIndex(tintIndex), which
    // maps:
    //   tintIndex 1 → blackVariant
    //   tintIndex 2 → mediumVariant
    //   tintIndex 3 → whiteVariant
    //   tintIndex 4 → medium_bright variant (legacy)

    private static final class StaticItemColor implements ItemColor
    {
        private final AEColor color;

        StaticItemColor(AEColor color) { this.color = color; }

        @Override
        public int getColor(ItemStack stack, int tintIndex)
        {
            return color.getVariantByTintIndex(tintIndex);
        }
    }

    /**
     * Wraps an ItemColor so the returned ARGB always has full alpha.
     * Replicates appeng.init.client.InitItemColors::makeOpaque verbatim.
     */
    private static ItemColor makeOpaque(ItemColor inner)
    {
        return (stack, tintIndex) ->
                FastColor.ARGB32.opaque(inner.getColor(stack, tintIndex));
    }
}