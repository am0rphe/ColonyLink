package com.colonylink.colonylink;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Configuration complète de ColonyLink v1.1.3.
 * Fichier : colonylink-common.toml
 *
 * Sections :
 *   [energy]    — RF, coûts, drain
 *   [general]   — builders max, ticker, range
 *   [tools]     — substitution d'outils
 *   [interface] — affichage GUI
 *   [network]   — redirector buffer
 */
public class ColonyLinkConfig
{
    public static final ModConfigSpec SPEC;

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

    // ── [tools] ───────────────────────────────────────────────────────────────
    public static final ModConfigSpec.BooleanValue ENABLE_TOOL_UPGRADE;
    public static final ModConfigSpec.BooleanValue TOOL_UPGRADE_SEND_AUTO;
    public static final ModConfigSpec.BooleanValue RESPECT_ENCHANT_LEVEL_CAP;

    // ── [interface] ───────────────────────────────────────────────────────────
    public static final ModConfigSpec.BooleanValue SHOW_CRAFTING_STATUS;
    public static final ModConfigSpec.BooleanValue SHOW_NO_PATTERN_ITEMS;
    public static final ModConfigSpec.IntValue     MAX_RESOURCES_DISPLAYED;
    public static final ModConfigSpec.IntValue     WAREHOUSE_SNAPSHOT_VALIDITY_TICKS;

    // ── [general — locate] ────────────────────────────────────────────────────
    public static final ModConfigSpec.IntValue LOCATE_GLOW_DURATION_SECONDS;
    public static final ModConfigSpec.LongValue LOCATE_COST_RF;

    // ── [network] ─────────────────────────────────────────────────────────────
    public static final ModConfigSpec.IntValue REDIRECTOR_BUFFER_ROWS;
    public static final ModConfigSpec.IntValue REDIRECTOR_BUFFER_COLS;

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
                .comment("RF cost per 'Send to Builder' action (per click, regardless of item count).",
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

        builder.pop();

        // ── [interface] ───────────────────────────────────────────────────────
        builder.comment(
                "GUI display settings for the Colony Link Wand interface."
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
                        "After this time the 'Check Warehouse' button resets to allow a new scan.",
                        "20t = 1s, 400t = 20s. Default: 400")
                .defineInRange("warehouse_snapshot_validity_ticks", 400, 20, 24000);

        builder.pop();

        // ── [network] ─────────────────────────────────────────────────────────
        builder.comment(
                "Colony Link Redirector buffer configuration.",
                "Changes require existing Redirectors to be broken and replaced to take effect."
        ).push("network");

        REDIRECTOR_BUFFER_ROWS = builder
                .comment("Number of rows in the Redirector item buffer.",
                        "Total slots = rows × cols. Default: 10 (= 120 slots with default cols)")
                .defineInRange("redirector_buffer_rows", 3, 1, 20);

        REDIRECTOR_BUFFER_COLS = builder
                .comment("Number of columns in the Redirector item buffer.",
                        "Total slots = rows × cols. Default: 12 (= 120 slots with default rows)")
                .defineInRange("redirector_buffer_cols", 9, 6, 18);

        builder.pop();

        SPEC = builder.build();
    }
}