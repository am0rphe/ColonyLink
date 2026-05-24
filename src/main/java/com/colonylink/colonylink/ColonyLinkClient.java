package com.colonylink.colonylink;

import appeng.api.util.AEColor;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Client-side registration for ColonyLink visuals and input.
 *
 * Currently handles:
 *   - ItemColor handler for the Warehouse Link Terminal item.
 *   - Explicit model registration for the Warehouse Link Terminal part models
 *     via ModelEvent.RegisterAdditional (variant must be "standalone").
 *   - Keybinding to open the Clipboard GUI from anywhere in the inventory
 *     (unbound by default — player assigns in Controls menu).
 *
 * Server safety:
 *   This class is @OnlyIn(Dist.CLIENT). The dispatcher in ColonyLink.java
 *   MUST wrap all listener registrations for this class in dist.isClient().
 *
 * Keybinding notes (v1.4.0):
 *   - Registered via RegisterKeyMappingsEvent on the mod event bus.
 *   - Consumed via InputEvent.Key on the NeoForge event bus (client-side).
 *   - When pressed, sends OpenWandGuiPacket C→S. The server locates the
 *     Clipboard anywhere in the player's inventory (not just held item).
 *   - Default: UNKNOWN (unbound). Player assigns in Options → Controls.
 *   - Category: "key.categories.colonylink" (separate group in Controls menu).
 *   - Guard: key is consumed only when no screen is open (getScreen() == null)
 *     to avoid conflicts with chest/crafting GUIs.
 */
@OnlyIn(Dist.CLIENT)
public final class ColonyLinkClient
{
    /**
     * Reserved variant string for side-loaded (non-blockstate, non-item) models.
     * NeoForge enforces this at ModelEvent$RegisterAdditional.register().
     */
    private static final String STANDALONE_VARIANT = "standalone";

    // ── Keybinding ────────────────────────────────────────────────────────────

    /** Category shown in the Controls menu. */
    public static final String KEY_CATEGORY = "key.categories.colonylink";

    /**
     * The keybinding for opening the Clipboard GUI.
     * Default: UNKNOWN (unbound) — the player assigns it manually.
     */
    public static final KeyMapping KEY_OPEN_CLIPBOARD = new KeyMapping(
            "key.colonylink.open_clipboard",     // translation key
            InputConstants.UNKNOWN.getValue(),   // default: unbound
            KEY_CATEGORY                         // Controls menu category
    );

    private ColonyLinkClient() {}

    // ── Model registration ────────────────────────────────────────────────────

    /**
     * Registers part model ResourceLocations with the NeoForge model pipeline.
     * Side-loaded models MUST use variant "standalone" in NeoForge 1.21.1.
     */
    public static void onRegisterAdditionalModels(ModelEvent.RegisterAdditional event)
    {
        event.register(new ModelResourceLocation(WarehouseLinkTerminalPart.MODEL_OFF, STANDALONE_VARIANT));
        event.register(new ModelResourceLocation(WarehouseLinkTerminalPart.MODEL_ON,  STANDALONE_VARIANT));
    }

    // ── Item color registration ───────────────────────────────────────────────

    /**
     * Registers ItemColor handlers for ColonyLink items.
     */
    public static void onRegisterItemDecorations(
            net.neoforged.neoforge.client.event.RegisterItemDecorationsEvent event)
    {
        event.register(ColonyLinkRegistry.DOMUM_PATTERN_ITEM.get(),
                DomumPatternDecorator.INSTANCE);
    }

    public static void onRegisterItemColors(RegisterColorHandlersEvent.Item event)
    {
        event.register(
                makeOpaque(new StaticItemColor(AEColor.TRANSPARENT)),
                ColonyLinkRegistry.WAREHOUSE_LINK_TERMINAL_ITEM.get());
    }

    // ── Keybinding registration ───────────────────────────────────────────────

    /**
     * Registers the ColonyLink keybinding with NeoForge.
     * Called via modEventBus.addListener(ColonyLinkClient::onRegisterKeyMappings)
     * from ColonyLink constructor (client-only, guarded by dist.isClient()).
     */
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event)
    {
        event.register(KEY_OPEN_CLIPBOARD);
    }

    // ── Keybinding input handler ──────────────────────────────────────────────

    /**
     * Fires on every key press/release. Checked on the NeoForge event bus
     * (client-side only — registered via NeoForge.EVENT_BUS.addListener
     * inside a dist.isClient() guard in ColonyLink constructor).
     *
     * Sends OpenWandGuiPacket C→S when:
     *   - The key is pressed (action == GLFW_PRESS)
     *   - No screen is currently open (avoids conflicts with chest/crafting GUIs)
     *   - The keybinding is actually bound by the player
     */
    public static void onKeyInput(InputEvent.Key event)
    {
        // Only on press, not hold or release
        if (event.getAction() != org.lwjgl.glfw.GLFW.GLFW_PRESS) return;

        // Don't intercept when a screen is open
        if (Minecraft.getInstance().screen != null) return;

        // Check if our keybind was the one pressed
        if (!KEY_OPEN_CLIPBOARD.isDown()) return;

        // Consume the key and send the packet to the server
        KEY_OPEN_CLIPBOARD.consumeClick();
        PacketDistributor.sendToServer(new OpenWandGuiPacket());
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

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