package com.colonylink.colonylink;

import appeng.api.util.AEColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

/**
 * Client-side registration for ColonyLink visuals.
 *
 * Currently handles:
 *   - ItemColor handler for the Warehouse Link Terminal item.
 *   - Explicit model registration for the Warehouse Link Terminal part models
 *     via ModelEvent.RegisterAdditional. ModelEvent.RegisterAdditional requires
 *     ModelResourceLocation (ResourceLocation + variant string), not plain
 *     ResourceLocation. Side-loaded models (non-blockstate, non-item) MUST use
 *     the reserved variant string "standalone" since NeoForge 1.21 — passing
 *     "" triggers IllegalArgumentException("Side-loaded models must use the
 *     'standalone' variant") at ModelEvent$RegisterAdditional.register()
 *     (ModelEvent.java:153, Preconditions.checkArgument).
 *
 *     Worse: that crash propagates as a ResourceReload failure, which in turn
 *     triggers a SECOND full reload of every mod's FMLCommonSetupEvent —
 *     causing a cascade of ~25 unrelated mods to crash with misleading
 *     "already registered" errors (placebo, accessories, gateways, minecolonies,
 *     etc.). The real culprit is always the FIRST mod to crash in the log;
 *     the rest are collateral.
 *
 * Server safety:
 *   This class is @OnlyIn(Dist.CLIENT) — it never touches a server thread.
 *   The dispatcher in ColonyLink.java MUST wrap the listener registration
 *   in dist.isClient() to avoid ClassNotFoundException on dedicated servers.
 *
 * History:
 *   - v1.3.3: PartModels.registerModels() (non-existent) removed from commonSetup.
 *   - v1.3.4: models registered client-side via ModelEvent.RegisterAdditional,
 *     wrapping the ResourceLocation in ModelResourceLocation with variant "".
 *     BROKEN: variant "" is rejected by NeoForge — crashes ResourceReload,
 *     which cascades into "already registered" errors across ~25 other mods.
 *   - v1.3.5: variant changed from "" to "standalone" (the reserved sentinel
 *     for side-loaded models in NeoForge 1.21.1). Fixes the crash cascade.
 */
@OnlyIn(Dist.CLIENT)
public final class ColonyLinkClient
{
    /**
     * Reserved variant string for side-loaded (non-blockstate, non-item) models.
     *
     * NeoForge enforces this at ModelEvent$RegisterAdditional.register():
     *
     *   Preconditions.checkArgument(
     *       loc.getVariant().equals("standalone"),
     *       "Side-loaded models must use the 'standalone' variant");
     *
     * Any other string (including "") triggers IllegalArgumentException.
     */
    private static final String STANDALONE_VARIANT = "standalone";

    private ColonyLinkClient() {}

    /**
     * Registers the part model ResourceLocations with the NeoForge model pipeline.
     *
     * CableBusBakedModel (AE2) looks up part models by ResourceLocation at render
     * time. Registering here ensures the models are baked during the normal
     * resource-load cycle, client-side only.
     *
     * ModelEvent.RegisterAdditional requires ModelResourceLocation, and side-loaded
     * models (which is what part models are — not blockstate variants, not items)
     * MUST use variant "standalone". Any other variant string causes
     * IllegalArgumentException and a cascading mod-loading failure.
     */
    public static void onRegisterAdditionalModels(ModelEvent.RegisterAdditional event)
    {
        event.register(new ModelResourceLocation(WarehouseLinkTerminalPart.MODEL_OFF, STANDALONE_VARIANT));
        event.register(new ModelResourceLocation(WarehouseLinkTerminalPart.MODEL_ON,  STANDALONE_VARIANT));
    }

    /**
     * Registers ItemColor handlers for ColonyLink items.
     */
    public static void onRegisterItemColors(RegisterColorHandlersEvent.Item event)
    {
        event.register(
                makeOpaque(new StaticItemColor(AEColor.TRANSPARENT)),
                ColonyLinkRegistry.WAREHOUSE_LINK_TERMINAL_ITEM.get());
    }

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

    private static ItemColor makeOpaque(ItemColor inner)
    {
        return (stack, tintIndex) ->
                FastColor.ARGB32.opaque(inner.getColor(stack, tintIndex));
    }
}