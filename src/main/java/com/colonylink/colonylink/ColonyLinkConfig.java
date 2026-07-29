package com.colonylink.colonylink;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * ColonyLink configuration (v1.6.0).
 * File: config/colonylink-server.toml (ModConfig.Type.SERVER)
 *
 * All values are server-authoritative gameplay settings. NeoForge synchronizes
 * this config from the server to every client during the connection phase, so
 * client-side reads (tooltips, GUI) always reflect the server's values while
 * in game. Values are NOT available at the main menu — client-reachable call
 * sites must go through the safeGet() helpers below.
 *
 * Runtime changes via /colonylink config set apply to the server immediately;
 * already-connected clients see updated display values after reconnecting
 * (NeoForge only syncs server configs at login).
 *
 * Modpack notes: this file can be shipped/locked globally; a template can be
 * provided via defaultconfigs/colonylink-server.toml. A per-world override in
 * {@code <world>/serverconfig/} takes precedence if that file exists.
 *
 * Sections:
 *   [energy]      — RF, costs, drain
 *   [general]     — max builders, ticker, range
 *   [delivery]    — where the Send button delivers resources
 *   [tools]       — tool substitution
 *   [interface]   — GUI list filtering (applied server-side before sending packets)
 *   [advanced_ae] — optional AdvancedAE compatibility
 *   [network]     — redirector buffer
 */
public class ColonyLinkConfig
{
    public static final ModConfigSpec SPEC;

    /**
     * Where the "Send" button delivers resources.
     * Enum (not boolean) so a future PLAYER_CHOICE value can be added without
     * breaking existing TOML files.
     */
    public enum SendTarget
    {
        BUILDER,
        WAREHOUSE
    }

    // ── [energy] ──────────────────────────────────────────────────────────────
    public static final ModConfigSpec.LongValue    WAND_RF_CAPACITY;
    public static final ModConfigSpec.LongValue    WAND_RF_TRANSFER_RATE;
    public static final ModConfigSpec.LongValue    PASSIVE_DRAIN_RF;
    public static final ModConfigSpec.LongValue    SEND_COST_RF;
    public static final ModConfigSpec.LongValue    CRAFT_COST_RF;
    public static final ModConfigSpec.BooleanValue BLOCK_ACTIONS_IF_NO_POWER;
    public static final ModConfigSpec.IntValue     LOW_POWER_THRESHOLD_PERCENT;

    // ── [general] ─────────────────────────────────────────────────────────────
    public static final ModConfigSpec.IntValue     MAX_BUILDERS_PER_WAND;
    public static final ModConfigSpec.IntValue     TICKER_INTERVAL_TICKS;
    public static final ModConfigSpec.BooleanValue WAND_RANGE_CHECK;

    // ── [delivery] ────────────────────────────────────────────────────────────
    public static final ModConfigSpec.EnumValue<SendTarget> SEND_TARGET;

    // ── [tools] ───────────────────────────────────────────────────────────────
    public static final ModConfigSpec.BooleanValue ENABLE_TOOL_UPGRADE;
    public static final ModConfigSpec.BooleanValue TOOL_UPGRADE_SEND_AUTO;
    public static final ModConfigSpec.BooleanValue RESPECT_ENCHANT_LEVEL_CAP;
    public static final ModConfigSpec.BooleanValue TOOL_SUBSTITUTION_IN_LIST;

    // ── [interface] ───────────────────────────────────────────────────────────
    public static final ModConfigSpec.BooleanValue SHOW_CRAFTING_STATUS;
    public static final ModConfigSpec.BooleanValue SHOW_NO_PATTERN_ITEMS;
    public static final ModConfigSpec.IntValue     MAX_RESOURCES_DISPLAYED;
    public static final ModConfigSpec.IntValue     WAREHOUSE_SNAPSHOT_VALIDITY_TICKS;

    // -- [advanced_ae] ---------------------------------------------------------
    public static final ModConfigSpec.BooleanValue ENABLE_ADVANCED_AE_COMPAT;
    public static final ModConfigSpec.IntValue     ADVANCED_AE_CRAFT_SUBMISSION_LIMIT;

    // ── [general — locate] ────────────────────────────────────────────────────
    public static final ModConfigSpec.IntValue LOCATE_GLOW_DURATION_SECONDS;
    public static final ModConfigSpec.LongValue LOCATE_COST_RF;

    // ── [network] ─────────────────────────────────────────────────────────────
    public static final ModConfigSpec.IntValue REDIRECTOR_CRAFT_QUEUE_MAX;

    // ── [loot] ────────────────────────────────────────────────────────────────
    public static final ModConfigSpec.BooleanValue PACKAGE_IN_CHESTS;
    public static final ModConfigSpec.DoubleValue  PACKAGE_CHEST_CHANCE;
    public static final ModConfigSpec.BooleanValue PACKAGE_FROM_RAIDERS;
    public static final ModConfigSpec.DoubleValue  PACKAGE_RAIDER_CHANCE;

    // ─────────────────────────────────────────────────────────────────────────

    static
    {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        // ── [energy] ──────────────────────────────────────────────────────────
        builder.comment(
                "RF/FE Energy settings for the Colony Link Wand.",
                "1 AE (Applied Energistics internal unit) = 2 RF by default."
        ).push("energy");

        WAND_RF_CAPACITY = builder
                .comment("Maximum RF stored in the Colony Link Wand.",
                        "Default: 160,000 RF")
                .defineInRange("wand_rf_capacity", 160_000L, 1L, Long.MAX_VALUE);

        WAND_RF_TRANSFER_RATE = builder
                .comment("Maximum RF/tick that can be transferred into the wand by chargers.",
                        "Should be >= the highest action cost to avoid blocking.",
                        "Default: 2,500 RF/tick")
                .defineInRange("wand_rf_transfer_rate", 2_500L, 1L, Long.MAX_VALUE);

        PASSIVE_DRAIN_RF = builder
                .comment("RF drained from the wand every ticker_interval_ticks while the GUI is open.",
                        "At default interval (10t = 0.5s), this equals ~120 RF/tick.",
                        "Set to 0 to disable passive drain entirely.")
                .defineInRange("passive_drain_rf", 1_200L, 0L, Long.MAX_VALUE);

        SEND_COST_RF = builder
                .comment("RF cost per Send action (per click, regardless of item count).",
                        "Applies to both delivery targets (see [delivery] send_target).",
                        "Default: 1,500 RF")
                .defineInRange("send_cost_rf", 1_500L, 0L, Long.MAX_VALUE);

        CRAFT_COST_RF = builder
                .comment("RF cost per craft job submitted to AE2 (per job, Craft and Craft All).",
                        "Default: 2,500 RF")
                .defineInRange("craft_cost_rf", 2_500L, 0L, Long.MAX_VALUE);

        BLOCK_ACTIONS_IF_NO_POWER = builder
                .comment("If true, Send and Craft actions are blocked when the wand has insufficient RF.",
                        "If false, actions always proceed regardless of RF level.",
                        "Default: true")
                .define("block_actions_if_no_power", true);

        LOW_POWER_THRESHOLD_PERCENT = builder
                .comment("The durability bar turns red when RF charge is below this percentage.",
                        "Range: 0 (never red) to 100 (always red). Default: 10")
                .defineInRange("low_power_threshold_percent", 10, 0, 100);

        builder.pop();

        // ── [general] ─────────────────────────────────────────────────────────
        builder.comment(
                "General behaviour settings for the Colony Link Wand and server ticker."
        ).push("general");

        MAX_BUILDERS_PER_WAND = builder
                .comment("Maximum number of Builder's Huts that can be linked to a single wand.",
                        "MineColonies builder limit is 5. Range: 1-10. Default: 5")
                .defineInRange("max_builders_per_wand", 5, 1, 10);

        TICKER_INTERVAL_TICKS = builder
                .comment("Interval in ticks between each server ticker update.",
                        "Lower = more responsive GUI but more server load.",
                        "Higher = less server load but slower GUI updates.",
                        "10t = 0.5s, 20t = 1s, 40t = 2s. Default: 10")
                .defineInRange("ticker_interval_ticks", 10, 5, 100);

        WAND_RANGE_CHECK = builder
                .comment("If true, the wand checks that the player is within range of the linked",
                        "Wireless Access Point before opening the GUI (like a Wireless Terminal).",
                        "If false, the wand works from anywhere. Default: false")
                .define("wand_range_check", false);

        LOCATE_GLOW_DURATION_SECONDS = builder
                .comment("Duration in seconds of the Glowing effect applied to a builder NPC",
                        "when the 'Locate' button is pressed in the Clipboard GUI.",
                        "Op-only: requires level 2+ operator to modify in-game.",
                        "Range: 1-60. Default: 8")
                .defineInRange("locate_glow_duration_seconds", 8, 1, 60);

        LOCATE_COST_RF = builder
                .comment("RF cost of pressing the 'Locate' button in the Clipboard GUI.",
                        "Set to 0 to disable the RF cost entirely.",
                        "Default: 500 RF")
                .defineInRange("locate_cost_rf", 500L, 0L, Long.MAX_VALUE);

        builder.pop();

        // ── [delivery] ────────────────────────────────────────────────────────
        builder.comment(
                "Delivery target settings.",
                "This is a server/modpack setting: it applies to every player and is",
                "synced from the server to clients on login."
        ).push("delivery");

        SEND_TARGET = builder
                .comment("Controls where the \"Send\" button delivers resources.",
                        "  BUILDER   - Resources are inserted directly into the Builder's Hut inventory.",
                        "              Bypasses the courier entirely. Fastest. This is the default and the",
                        "              intended ColonyLink experience.",
                        "  WAREHOUSE - Resources are inserted into the colony Warehouse racks instead.",
                        "              A courier must then deliver them to the builder. Slower, but keeps",
                        "              couriers relevant. Recommended for balanced modpacks.",
                        "              While a delivery is pending, the resource line turns grey",
                        "              (\"Sent\") until the courier completes the delivery.",
                        "Default: BUILDER")
                .defineEnum("send_target", SendTarget.BUILDER);

        builder.pop();

        // ── [tools] ───────────────────────────────────────────────────────────
        builder.comment(
                "Tool upgrade and substitution settings.",
                "When enabled, ColonyLink automatically sends the best tool tier available",
                "in the ME network based on the builder's Work Hut level."
        ).push("tools");

        ENABLE_TOOL_UPGRADE = builder
                .comment("Enable automatic tool tier substitution.",
                        "When true, ColonyLink replaces tool requests with the best tier",
                        "available in ME according to the Work Hut level table.",
                        "Default: true")
                .define("enable_tool_upgrade", true);

        TOOL_UPGRADE_SEND_AUTO = builder
                .comment("If true, the best available tool is sent automatically when clicking Send,",
                        "without requiring a separate confirmation.",
                        "If false, the substituted tool is shown in the GUI but must be sent manually.",
                        "Default: true")
                .define("tool_upgrade_send_auto", true);

        RESPECT_ENCHANT_LEVEL_CAP = builder
                .comment("If true, tools with enchantments above the Work Hut level cap are excluded.",
                        "If false, any enchantment level is accepted (ignores the MineColonies table).",
                        "Default: true")
                .define("respect_enchant_level_cap", true);

        TOOL_SUBSTITUTION_IN_LIST = builder
                .comment("When false (default), tool substitution applies only to the Priority Request",
                        "line, not to the resource list. Set true to also substitute in the list",
                        "(pre-1.6.4 behavior).",
                        "Default: false")
                .define("tool_substitution_in_list", false);

        builder.pop();

        // ── [interface] ───────────────────────────────────────────────────────
        builder.comment(
                "GUI display settings for the Colony Link Wand interface.",
                "Despite the section name these are applied SERVER-side: the server filters",
                "the resource list before sending it to clients."
        ).push("interface");

        SHOW_CRAFTING_STATUS = builder
                .comment("If true, items currently being crafted (CRAFTING status) are shown in the list.",
                        "If false, they are hidden while craft is in progress.",
                        "Default: true")
                .define("show_crafting_status", true);

        SHOW_NO_PATTERN_ITEMS = builder
                .comment("If true, items with no AE2 pattern and not available in ME are shown in red.",
                        "If false, they are hidden from the list (cleaner view for large builds).",
                        "Default: true")
                .define("show_no_pattern_items", true);

        MAX_RESOURCES_DISPLAYED = builder
                .comment("Maximum number of resource entries shown in the wand GUI list.",
                        "Large builds can have 100+ missing items — set lower for performance.",
                        "Range: 10-500. Default: 100")
                .defineInRange("max_resources_displayed", 100, 10, 500);

        WAREHOUSE_SNAPSHOT_VALIDITY_TICKS = builder
                .comment("How long a warehouse scan result remains valid before expiring (in ticks).",
                        "Also the server-side cooldown between two 'Check Warehouse' scans.",
                        "20t = 1s, 400t = 20s. Default: 400")
                .defineInRange("warehouse_snapshot_validity_ticks", 400, 20, 24000);

        builder.pop();

        // -- [advanced_ae] -----------------------------------------------------
        builder.comment(
                "Optional AdvancedAE compatibility settings.",
                "These settings only apply when the advanced_ae mod is installed."
        ).push("advanced_ae");

        ENABLE_ADVANCED_AE_COMPAT = builder
                .comment("Enables higher craft submission limit when AdvancedAE is installed.",
                        "Default: true")
                .define("enable_advanced_ae_compat", true);

        ADVANCED_AE_CRAFT_SUBMISSION_LIMIT = builder
                .comment("0 = normal AE2 behaviour based on free CPU count. Higher values allow more ColonyLink craft submissions when AdvancedAE is installed, useful for AdvancedAE Quantum Computers. Suggested values: 16, 32, 64.",
                        "Range: 0-256. Default: 32")
                .defineInRange("advanced_ae_craft_submission_limit", 32, 0, 256);

        builder.pop();

        // ── [network] ─────────────────────────────────────────────────────────
        builder.comment(
                "Colony Link Redirector settings.",
                "v1.6.2 — the item buffer is fixed at 3x9 (27 slots) to stay in sync with the",
                "GUI layout and texture; the old redirector_buffer_rows/cols options were removed."
        ).push("network");

        REDIRECTOR_CRAFT_QUEUE_MAX = builder
                .comment("Maximum number of Domum autocraft jobs the Redirector accepts from AE2",
                        "before reporting itself busy (back-pressure). When the limit is reached,",
                        "or when crafted items cannot be returned to a full/offline ME network,",
                        "AE2 keeps the materials and retries later — nothing is ever voided.",
                        "Higher = more Domum crafts buffered, but more state saved per Redirector.",
                        "Default: 64")
                .defineInRange("redirector_craft_queue_max", 64, 1, 1024);

        builder.pop();

        // ── [loot] ─────────────────────────────────────────────────────────────
        builder.comment(
                "World loot for the Colony Link Package.",
                "The Package stays craftable — this loot is a COMPLEMENT to crafting, never a",
                "replacement. Nothing here removes or overrides any existing recipe or loot table;",
                "the Package is only ADDED (never subtracted) to the targeted tables.",
                "Applied server-side (loot resolves on the server). Defaults are deliberately low",
                "so the Package stays a rare bonus and the craft remains the main source."
        ).push("loot");

        PACKAGE_IN_CHESTS = builder
                .comment("If true, the Package can be added to village and pillager outpost chests.",
                        "Default: true")
                .define("package_in_chests", true);

        PACKAGE_CHEST_CHANCE = builder
                .comment("Chance to add one Package to each eligible chest (per chest, rolled once).",
                        "Range: 0.0-1.0 (0.05 = 5%). Set 0.0 to disable without flipping the toggle.",
                        "Default: 0.05")
                .defineInRange("package_chest_chance", 0.10, 0.0, 1.0);

        PACKAGE_FROM_RAIDERS = builder
                .comment("If true, MineColonies raid raiders can drop the Package on death.",
                        "Only raid raiders have loot tables; camp mobs (campbarbarian, ...) never drop it.",
                        "Default: true")
                .define("package_from_raiders", true);

        PACKAGE_RAIDER_CHANCE = builder
                .comment("Chance to drop one Package per raid raider killed (rolled once per raider).",
                        "Range: 0.0-1.0 (0.03 = 3%). Set 0.0 to disable without flipping the toggle.",
                        "Default: 0.03")
                .defineInRange("package_raider_chance", 0.15, 0.0, 1.0);

        builder.pop();

        SPEC = builder.build();
    }

    // ── Safe accessors ────────────────────────────────────────────────────────
    // A SERVER-type config is only loaded while a server/world is active (and,
    // on remote clients, only after the login sync). These helpers protect the
    // few client- and capability-side call sites that could in theory execute
    // outside that window (item bar rendering, tooltips, third-party mods
    // exercising the energy capability): they fall back to the value's default
    // instead of throwing "Cannot get config value before config is loaded."

    public static boolean isLoaded()
    {
        return SPEC.isLoaded();
    }

    public static int safeGet(ModConfigSpec.IntValue value, int fallback)
    {
        return SPEC.isLoaded() ? value.get() : fallback;
    }

    public static long safeGet(ModConfigSpec.LongValue value, long fallback)
    {
        return SPEC.isLoaded() ? value.get() : fallback;
    }

    public static boolean safeGet(ModConfigSpec.BooleanValue value, boolean fallback)
    {
        return SPEC.isLoaded() ? value.get() : fallback;
    }

    /**
     * Current delivery target, defaulting to BUILDER when the config is not
     * loaded (e.g. main menu). Server-side gameplay code may read SEND_TARGET
     * directly; client/UI code should use this accessor.
     */
    public static SendTarget getSendTarget()
    {
        return SPEC.isLoaded() ? SEND_TARGET.get() : SendTarget.BUILDER;
    }
}