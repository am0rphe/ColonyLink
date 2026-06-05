package com.colonylink.colonylink;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Commande /colonylink — op-only (level 2).
 *
 * Sous-commandes :
 *   /colonylink info                         — version, mods détectés, état général
 *   /colonylink debug <subsystem>            — toggle logs verbeux par sous-système
 *   /colonylink debug list                   — liste les sous-systèmes disponibles
 *   /colonylink config <key> <value>         — modifier des paramètres runtime
 *   /colonylink config list                  — lister les clés modifiables
 *   /colonylink reload                       — recharger la config depuis le fichier TOML
 *   /colonylink stats                        — statistiques d'utilisation en temps réel
 *   /colonylink stats reset                  — remettre les compteurs à zéro
 *
 * Enregistrement : NeoForge.EVENT_BUS.register(ColonyLinkCommand.class)
 * dans ColonyLink constructor (côté commun — les commandes sont server-only
 * par nature et RegisterCommandsEvent ne se déclenche pas côté client pur).
 */
public class ColonyLinkCommand
{
    // ── Sous-systèmes de debug ────────────────────────────────────────────────

    public enum DebugSubsystem
    {
        WAREHOUSE("warehouse",  "Warehouse scan and snapshot operations"),
        TERMINAL ("terminal",   "Warehouse Link Terminal sync packets"),
        PACKETS  ("packets",    "All network packet send/receive"),
        CRAFTING ("crafting",   "AE2 craft job submission and tracking"),
        TICKER   ("ticker",     "Server ticker per-tick detail"),
        TOOLS    ("tools",      "Tool substitution decisions");

        public final String key;
        public final String description;

        DebugSubsystem(String key, String description)
        {
            this.key = key;
            this.description = description;
        }

        public static DebugSubsystem fromKey(String key)
        {
            for (DebugSubsystem s : values())
                if (s.key.equalsIgnoreCase(key)) return s;
            return null;
        }
    }

    /** Ensemble des sous-systèmes dont le debug verbose est actuellement actif. */
    private static final Set<DebugSubsystem> enabledDebug =
            ConcurrentHashMap.newKeySet();

    /** Vérifie si le debug est actif pour un sous-système donné. */
    public static boolean isDebugEnabled(DebugSubsystem subsystem)
    {
        return enabledDebug.contains(subsystem);
    }

    // ── Statistiques d'utilisation ────────────────────────────────────────────

    private static final AtomicLong statsCraftJobsSubmitted  = new AtomicLong(0);
    private static final AtomicLong statsSendToBuilderCalls  = new AtomicLong(0);
    private static final AtomicLong statsWarehouseScans      = new AtomicLong(0);
    private static final AtomicLong statsWhToMeTransfers     = new AtomicLong(0);
    private static final AtomicLong statsMeToWhTransfers     = new AtomicLong(0);
    private static final AtomicLong statsToolSubstitutions   = new AtomicLong(0);
    private static long             statsResetEpoch           = System.currentTimeMillis();

    public static void recordCraftJobSubmitted()  { statsCraftJobsSubmitted.incrementAndGet(); }
    public static void recordSendToBuilder()      { statsSendToBuilderCalls.incrementAndGet(); }
    public static void recordWarehouseScan()      { statsWarehouseScans.incrementAndGet(); }
    public static void recordWhToMeTransfer()     { statsWhToMeTransfers.incrementAndGet(); }
    public static void recordMeToWhTransfer()     { statsMeToWhTransfers.incrementAndGet(); }
    public static void recordToolSubstitution()   { statsToolSubstitutions.incrementAndGet(); }

    // ── Clés de config modifiables à chaud ───────────────────────────────────
    //
    // Seules les valeurs qui n'affectent pas la structure du monde (pas de
    // buffer size de redirector, pas de capacité RF du wand) sont exposées ici.
    // Les valeurs structurelles nécessitent un redémarrage et sont exclues.

    private static final Map<String, String> RUNTIME_CONFIG_KEYS = Map.ofEntries(
            Map.entry("ticker_interval_ticks",            "General — ticker interval in ticks (5–100)"),
            Map.entry("passive_drain_rf",                 "Energy — RF drained per ticker interval (0+)"),
            Map.entry("send_cost_rf",                     "Energy — RF cost per Send action (0+)"),
            Map.entry("craft_cost_rf",                    "Energy — RF cost per Craft job (0+)"),
            Map.entry("block_actions_if_no_power",        "Energy — block actions when RF empty (true/false)"),
            Map.entry("low_power_threshold_percent",      "Energy — low-power warning threshold 0–100"),
            Map.entry("max_builders_per_wand",            "General — max linked builders per wand (1–10)"),
            Map.entry("wand_range_check",                 "General — enforce WAP range check (true/false)"),
            Map.entry("enable_tool_upgrade",              "Tools — enable tool tier substitution (true/false)"),
            Map.entry("tool_upgrade_send_auto",           "Tools — auto-send substituted tool (true/false)"),
            Map.entry("respect_enchant_level_cap",        "Tools — enforce enchant level cap (true/false)"),
            Map.entry("show_crafting_status",             "Interface — show crafting items in GUI (true/false)"),
            Map.entry("show_no_pattern_items",            "Interface — show no-pattern items in GUI (true/false)"),
            Map.entry("max_resources_displayed",          "Interface — max resource entries in GUI (10–500)"),
            Map.entry("warehouse_snapshot_validity_ticks","Interface — warehouse snapshot TTL in ticks (20–24000)")
    );

    // ── Métadonnées typées par clé (pour la tab-complétion de la valeur) ──────
    //
    // Parallèle à RUNTIME_CONFIG_KEYS : mêmes clés, mais avec le type, la plage
    // et la valeur par défaut, plus un accès à la valeur courante. Sert uniquement
    // à proposer des candidats pertinents en autocomplétion ; la validation réelle
    // reste faite par applyConfig().

    private record ConfigSpec(boolean bool, long min, long max, String def,
                              java.util.function.Supplier<String> current) {}

    private static final Map<String, ConfigSpec> CONFIG_SPECS = Map.ofEntries(
            Map.entry("ticker_interval_ticks",
                    new ConfigSpec(false, 5, 100, "10",
                            () -> String.valueOf(ColonyLinkConfig.TICKER_INTERVAL_TICKS.get()))),
            Map.entry("passive_drain_rf",
                    new ConfigSpec(false, 0, Long.MAX_VALUE, "1200",
                            () -> String.valueOf(ColonyLinkConfig.PASSIVE_DRAIN_RF.get()))),
            Map.entry("send_cost_rf",
                    new ConfigSpec(false, 0, Long.MAX_VALUE, "1500",
                            () -> String.valueOf(ColonyLinkConfig.SEND_COST_RF.get()))),
            Map.entry("craft_cost_rf",
                    new ConfigSpec(false, 0, Long.MAX_VALUE, "2500",
                            () -> String.valueOf(ColonyLinkConfig.CRAFT_COST_RF.get()))),
            Map.entry("block_actions_if_no_power",
                    new ConfigSpec(true, 0, 0, "true",
                            () -> String.valueOf(ColonyLinkConfig.BLOCK_ACTIONS_IF_NO_POWER.get()))),
            Map.entry("low_power_threshold_percent",
                    new ConfigSpec(false, 0, 100, "10",
                            () -> String.valueOf(ColonyLinkConfig.LOW_POWER_THRESHOLD_PERCENT.get()))),
            Map.entry("max_builders_per_wand",
                    new ConfigSpec(false, 1, 10, "5",
                            () -> String.valueOf(ColonyLinkConfig.MAX_BUILDERS_PER_WAND.get()))),
            Map.entry("wand_range_check",
                    new ConfigSpec(true, 0, 0, "false",
                            () -> String.valueOf(ColonyLinkConfig.WAND_RANGE_CHECK.get()))),
            Map.entry("enable_tool_upgrade",
                    new ConfigSpec(true, 0, 0, "true",
                            () -> String.valueOf(ColonyLinkConfig.ENABLE_TOOL_UPGRADE.get()))),
            Map.entry("tool_upgrade_send_auto",
                    new ConfigSpec(true, 0, 0, "true",
                            () -> String.valueOf(ColonyLinkConfig.TOOL_UPGRADE_SEND_AUTO.get()))),
            Map.entry("respect_enchant_level_cap",
                    new ConfigSpec(true, 0, 0, "true",
                            () -> String.valueOf(ColonyLinkConfig.RESPECT_ENCHANT_LEVEL_CAP.get()))),
            Map.entry("show_crafting_status",
                    new ConfigSpec(true, 0, 0, "true",
                            () -> String.valueOf(ColonyLinkConfig.SHOW_CRAFTING_STATUS.get()))),
            Map.entry("show_no_pattern_items",
                    new ConfigSpec(true, 0, 0, "true",
                            () -> String.valueOf(ColonyLinkConfig.SHOW_NO_PATTERN_ITEMS.get()))),
            Map.entry("max_resources_displayed",
                    new ConfigSpec(false, 10, 500, "100",
                            () -> String.valueOf(ColonyLinkConfig.MAX_RESOURCES_DISPLAYED.get()))),
            Map.entry("warehouse_snapshot_validity_ticks",
                    new ConfigSpec(false, 20, 24000, "400",
                            () -> String.valueOf(ColonyLinkConfig.WAREHOUSE_SNAPSHOT_VALIDITY_TICKS.get()))),
            Map.entry("enable_advanced_ae_compat",
                    new ConfigSpec(true, 0, 0, "true",
                            () -> String.valueOf(ColonyLinkConfig.ENABLE_ADVANCED_AE_COMPAT.get()))),
            Map.entry("advanced_ae_craft_submission_limit",
                    new ConfigSpec(false, 0, 256, "32",
                            () -> String.valueOf(ColonyLinkConfig.ADVANCED_AE_CRAFT_SUBMISSION_LIMIT.get())))
    );

    // ── Fournisseurs de suggestions Brigadier (tab-complétion) ────────────────

    /** /colonylink debug <subsystem> — sous-systèmes + 'all', description en tooltip. */
    private static final SuggestionProvider<CommandSourceStack> SUGGEST_DEBUG = (ctx, b) ->
    {
        String rem = b.getRemaining().toLowerCase(Locale.ROOT);
        if ("all".startsWith(rem))
            b.suggest("all", Component.literal("Toggle every subsystem at once"));
        for (DebugSubsystem s : DebugSubsystem.values())
            if (s.key.startsWith(rem))
                b.suggest(s.key, Component.literal(s.description));
        return b.buildFuture();
    };

    /** /colonylink config <key> — clés modifiables, description en tooltip. */
    private static final SuggestionProvider<CommandSourceStack> SUGGEST_CONFIG_KEY = (ctx, b) ->
    {
        String rem = b.getRemaining().toLowerCase(Locale.ROOT);
        RUNTIME_CONFIG_KEYS.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .filter(e -> e.getKey().startsWith(rem))
                .forEach(e -> b.suggest(e.getKey(), Component.literal(e.getValue())));
        return b.buildFuture();
    };

    /** /colonylink config <key> <value> — valeurs valides selon la clé déjà saisie. */
    private static final SuggestionProvider<CommandSourceStack> SUGGEST_CONFIG_VALUE = (ctx, b) ->
    {
        String key;
        try { key = StringArgumentType.getString(ctx, "key"); }
        catch (IllegalArgumentException e) { return b.buildFuture(); }

        ConfigSpec spec = CONFIG_SPECS.get(key);
        if (spec == null) return b.buildFuture();

        String rem = b.getRemaining().toLowerCase(Locale.ROOT);
        String cur = spec.current().get();

        // (valeur → tooltip), ordre conservé, sans doublon
        java.util.LinkedHashMap<String, String> cands = new java.util.LinkedHashMap<>();
        if (spec.bool())
        {
            cands.put("true",  cur.equalsIgnoreCase("true")  ? "Enable (current)"  : "Enable");
            cands.put("false", cur.equalsIgnoreCase("false") ? "Disable (current)" : "Disable");
        }
        else
        {
            cands.putIfAbsent(cur,                        "Current value");
            cands.putIfAbsent(spec.def(),                 "Default");
            cands.putIfAbsent(String.valueOf(spec.min()), "Minimum");
            if (spec.max() != Long.MAX_VALUE)
                cands.putIfAbsent(String.valueOf(spec.max()), "Maximum");
        }

        cands.forEach((val, tip) ->
        {
            if (val.toLowerCase(Locale.ROOT).startsWith(rem))
                b.suggest(val, Component.literal(tip));
        });
        return b.buildFuture();
    };


    // ── Enregistrement de la commande ─────────────────────────────────────────

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event)
    {
        register(event.getDispatcher());
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        dispatcher.register(
                Commands.literal("colonylink")
                        .requires(src -> src.hasPermission(2))

                        // /colonylink info
                        .then(Commands.literal("info")
                                .executes(ColonyLinkCommand::cmdInfo))

                        // /colonylink debug list
                        // /colonylink debug <subsystem>
                        .then(Commands.literal("debug")
                                .then(Commands.literal("list")
                                        .executes(ColonyLinkCommand::cmdDebugList))
                                .then(Commands.argument("subsystem", StringArgumentType.word())
                                        .suggests(SUGGEST_DEBUG)
                                        .executes(ColonyLinkCommand::cmdDebugToggle)))

                        // /colonylink config list
                        // /colonylink config <key> <value>
                        .then(Commands.literal("config")
                                .then(Commands.literal("list")
                                        .executes(ColonyLinkCommand::cmdConfigList))
                                .then(Commands.argument("key", StringArgumentType.word())
                                        .suggests(SUGGEST_CONFIG_KEY)
                                        .then(Commands.argument("value", StringArgumentType.greedyString())
                                                .suggests(SUGGEST_CONFIG_VALUE)
                                                .executes(ColonyLinkCommand::cmdConfigSet))))

                        // /colonylink reload
                        .then(Commands.literal("reload")
                                .executes(ColonyLinkCommand::cmdReload))

                        // /colonylink stats
                        // /colonylink stats reset
                        .then(Commands.literal("stats")
                                .executes(ColonyLinkCommand::cmdStats)
                                .then(Commands.literal("reset")
                                        .executes(ColonyLinkCommand::cmdStatsReset)))
        );
    }

    // ── /colonylink info ──────────────────────────────────────────────────────

    private static int cmdInfo(CommandContext<CommandSourceStack> ctx)
    {
        CommandSourceStack src = ctx.getSource();

        // Détection mods optionnels
        boolean hasCurios  = net.neoforged.fml.ModList.get().isLoaded("curios");
        boolean hasRS2     = net.neoforged.fml.ModList.get().isLoaded("refinedstorage")
                || net.neoforged.fml.ModList.get().isLoaded("refinedstorageaddons");
        boolean hasJade    = net.neoforged.fml.ModList.get().isLoaded("jade");
        boolean hasApothe  = net.neoforged.fml.ModList.get().isLoaded("apotheosis");
        boolean hasAdvancedAe = net.neoforged.fml.ModList.get().isLoaded("advanced_ae");

        // Récupère la version depuis le manifest NeoForge
        String version = net.neoforged.fml.ModList.get()
                .getModContainerById(ColonyLink.MODID)
                .map(c -> c.getModInfo().getVersion().toString())
                .orElse("unknown");

        send(src, "§6════════ ColonyLink Info ════════");
        send(src, "§7  Version : §f" + version);
        send(src, "§7  Mod ID  : §f" + ColonyLink.MODID);
        send(src, "");
        send(src, "§6  Optional mods:");
        send(src, "§7    Curios API  : " + flag(hasCurios));
        send(src, "§7    Refined Stor: " + flag(hasRS2));
        send(src, "§7    Jade        : " + flag(hasJade));
        send(src, "§7    Apotheosis  : " + flag(hasApothe));
        send(src, "§7    AdvancedAE  : " + flag(hasAdvancedAe));
        send(src, "");
        send(src, "§6  Active debug subsystems:");
        if (enabledDebug.isEmpty())
            send(src, "§7    (none — use /colonylink debug <subsystem> to enable)");
        else
            for (DebugSubsystem s : enabledDebug)
                send(src, "§a    ✔ " + s.key);
        send(src, "");
        send(src, "§6  Config snapshot:");
        send(src, "§7    ticker_interval_ticks : §f" + ColonyLinkConfig.TICKER_INTERVAL_TICKS.get());
        send(src, "§7    max_builders_per_wand  : §f" + ColonyLinkConfig.MAX_BUILDERS_PER_WAND.get());
        send(src, "§7    enable_tool_upgrade    : §f" + ColonyLinkConfig.ENABLE_TOOL_UPGRADE.get());
        send(src, "§7    block_actions_if_no_power : §f" + ColonyLinkConfig.BLOCK_ACTIONS_IF_NO_POWER.get());
        send(src, "§7    enable_advanced_ae_compat : §f" + ColonyLinkConfig.ENABLE_ADVANCED_AE_COMPAT.get());
        send(src, "§7    advanced_ae_craft_submission_limit : §f" + ColonyLinkConfig.ADVANCED_AE_CRAFT_SUBMISSION_LIMIT.get());
        send(src, "§6══════════════════════════════════");

        return 1;
    }

    // ── /colonylink debug ─────────────────────────────────────────────────────

    private static int cmdDebugList(CommandContext<CommandSourceStack> ctx)
    {
        CommandSourceStack src = ctx.getSource();
        send(src, "§6════ ColonyLink Debug Subsystems ════");
        for (DebugSubsystem s : DebugSubsystem.values())
        {
            boolean on = enabledDebug.contains(s);
            String status = on ? "§a[ON] " : "§7[off]";
            send(src, status + " §f" + s.key + " §8— " + s.description);
        }
        send(src, "§7Use §f/colonylink debug <name> §7to toggle.");
        return 1;
    }

    private static int cmdDebugToggle(CommandContext<CommandSourceStack> ctx)
    {
        CommandSourceStack src = ctx.getSource();
        String key = StringArgumentType.getString(ctx, "subsystem");

        if (key.equalsIgnoreCase("all"))
        {
            boolean anyOn = !enabledDebug.isEmpty();
            if (anyOn)
            {
                enabledDebug.clear();
                send(src, "§c[ColonyLink] All debug subsystems §4disabled§c.");
            }
            else
            {
                for (DebugSubsystem s : DebugSubsystem.values()) enabledDebug.add(s);
                send(src, "§a[ColonyLink] All debug subsystems §2enabled§a.");
            }
            ColonyLink.LOGGER.info("[ColonyLink] debug all toggled to {}", !anyOn);
            return 1;
        }

        DebugSubsystem sub = DebugSubsystem.fromKey(key);
        if (sub == null)
        {
            send(src, "§c[ColonyLink] Unknown subsystem '§4" + key + "§c'. "
                    + "Use §f/colonylink debug list §cto see available subsystems.");
            return 0;
        }

        boolean wasEnabled = enabledDebug.remove(sub);
        if (!wasEnabled) enabledDebug.add(sub);
        boolean nowEnabled = !wasEnabled;

        String state = nowEnabled ? "§a§lENABLED" : "§c§lDISABLED";
        send(src, "§7[ColonyLink] Debug §f" + sub.key + " §7→ " + state);
        ColonyLink.LOGGER.info("[ColonyLink] debug subsystem '{}' toggled to {}", sub.key, nowEnabled);
        return 1;
    }

    // ── /colonylink config ────────────────────────────────────────────────────

    private static int cmdConfigList(CommandContext<CommandSourceStack> ctx)
    {
        CommandSourceStack src = ctx.getSource();
        send(src, "§6════ ColonyLink Runtime Config Keys ════");
        RUNTIME_CONFIG_KEYS.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> send(src, "§f  " + e.getKey() + " §8— " + e.getValue()));
        send(src, "§7Use §f/colonylink config <key> <value> §7to change a value.");
        send(src, "§7Use §f/colonylink reload §7to re-read values from the TOML file.");
        return 1;
    }

    private static int cmdConfigSet(CommandContext<CommandSourceStack> ctx)
    {
        CommandSourceStack src = ctx.getSource();
        String key   = StringArgumentType.getString(ctx, "key");
        String value = StringArgumentType.getString(ctx, "value").trim();

        if (!RUNTIME_CONFIG_KEYS.containsKey(key))
        {
            send(src, "§c[ColonyLink] Unknown config key '§4" + key + "§c'. "
                    + "Use §f/colonylink config list §cto see available keys.");
            return 0;
        }

        try
        {
            boolean changed = applyConfig(key, value);
            if (changed)
            {
                send(src, "§a[ColonyLink] §f" + key + " §a→ §f" + value);
                ColonyLink.LOGGER.info("[ColonyLink] config set {} = {}", key, value);
            }
            else
            {
                send(src, "§e[ColonyLink] §f" + key + " §ewas already §f" + value + "§e, no change.");
            }
        }
        catch (IllegalArgumentException e)
        {
            send(src, "§c[ColonyLink] Invalid value '§4" + value + "§c' for key §f" + key
                    + "§c: " + e.getMessage());
            return 0;
        }
        return 1;
    }

    /**
     * Applique une valeur de config à chaud via l'API ModConfigSpec.
     * Retourne true si la valeur a effectivement changé.
     */
    private static boolean applyConfig(String key, String value) throws IllegalArgumentException
    {
        switch (key)
        {
            // ── [energy] ──────────────────────────────────────────────────────
            case "passive_drain_rf" ->
            {
                long v = parseLong(value, 0, Long.MAX_VALUE, key);
                long old = ColonyLinkConfig.PASSIVE_DRAIN_RF.get();
                if (v == old) return false;
                ColonyLinkConfig.PASSIVE_DRAIN_RF.set(v);
                return true;
            }
            case "send_cost_rf" ->
            {
                long v = parseLong(value, 0, Long.MAX_VALUE, key);
                long old = ColonyLinkConfig.SEND_COST_RF.get();
                if (v == old) return false;
                ColonyLinkConfig.SEND_COST_RF.set(v);
                return true;
            }
            case "craft_cost_rf" ->
            {
                long v = parseLong(value, 0, Long.MAX_VALUE, key);
                long old = ColonyLinkConfig.CRAFT_COST_RF.get();
                if (v == old) return false;
                ColonyLinkConfig.CRAFT_COST_RF.set(v);
                return true;
            }
            case "block_actions_if_no_power" ->
            {
                boolean v = parseBool(value, key);
                boolean old = ColonyLinkConfig.BLOCK_ACTIONS_IF_NO_POWER.get();
                if (v == old) return false;
                ColonyLinkConfig.BLOCK_ACTIONS_IF_NO_POWER.set(v);
                return true;
            }
            case "low_power_threshold_percent" ->
            {
                int v = parseInt(value, 0, 100, key);
                int old = ColonyLinkConfig.LOW_POWER_THRESHOLD_PERCENT.get();
                if (v == old) return false;
                ColonyLinkConfig.LOW_POWER_THRESHOLD_PERCENT.set(v);
                return true;
            }
            // ── [general] ─────────────────────────────────────────────────────
            case "ticker_interval_ticks" ->
            {
                int v = parseInt(value, 5, 100, key);
                int old = ColonyLinkConfig.TICKER_INTERVAL_TICKS.get();
                if (v == old) return false;
                ColonyLinkConfig.TICKER_INTERVAL_TICKS.set(v);
                return true;
            }
            case "max_builders_per_wand" ->
            {
                int v = parseInt(value, 1, 10, key);
                int old = ColonyLinkConfig.MAX_BUILDERS_PER_WAND.get();
                if (v == old) return false;
                ColonyLinkConfig.MAX_BUILDERS_PER_WAND.set(v);
                return true;
            }
            case "wand_range_check" ->
            {
                boolean v = parseBool(value, key);
                boolean old = ColonyLinkConfig.WAND_RANGE_CHECK.get();
                if (v == old) return false;
                ColonyLinkConfig.WAND_RANGE_CHECK.set(v);
                return true;
            }
            // ── [tools] ───────────────────────────────────────────────────────
            case "enable_tool_upgrade" ->
            {
                boolean v = parseBool(value, key);
                boolean old = ColonyLinkConfig.ENABLE_TOOL_UPGRADE.get();
                if (v == old) return false;
                ColonyLinkConfig.ENABLE_TOOL_UPGRADE.set(v);
                return true;
            }
            case "tool_upgrade_send_auto" ->
            {
                boolean v = parseBool(value, key);
                boolean old = ColonyLinkConfig.TOOL_UPGRADE_SEND_AUTO.get();
                if (v == old) return false;
                ColonyLinkConfig.TOOL_UPGRADE_SEND_AUTO.set(v);
                return true;
            }
            case "respect_enchant_level_cap" ->
            {
                boolean v = parseBool(value, key);
                boolean old = ColonyLinkConfig.RESPECT_ENCHANT_LEVEL_CAP.get();
                if (v == old) return false;
                ColonyLinkConfig.RESPECT_ENCHANT_LEVEL_CAP.set(v);
                return true;
            }
            // ── [interface] ───────────────────────────────────────────────────
            case "show_crafting_status" ->
            {
                boolean v = parseBool(value, key);
                boolean old = ColonyLinkConfig.SHOW_CRAFTING_STATUS.get();
                if (v == old) return false;
                ColonyLinkConfig.SHOW_CRAFTING_STATUS.set(v);
                return true;
            }
            case "show_no_pattern_items" ->
            {
                boolean v = parseBool(value, key);
                boolean old = ColonyLinkConfig.SHOW_NO_PATTERN_ITEMS.get();
                if (v == old) return false;
                ColonyLinkConfig.SHOW_NO_PATTERN_ITEMS.set(v);
                return true;
            }
            case "max_resources_displayed" ->
            {
                int v = parseInt(value, 10, 500, key);
                int old = ColonyLinkConfig.MAX_RESOURCES_DISPLAYED.get();
                if (v == old) return false;
                ColonyLinkConfig.MAX_RESOURCES_DISPLAYED.set(v);
                return true;
            }
            case "warehouse_snapshot_validity_ticks" ->
            {
                int v = parseInt(value, 20, 24000, key);
                int old = ColonyLinkConfig.WAREHOUSE_SNAPSHOT_VALIDITY_TICKS.get();
                if (v == old) return false;
                ColonyLinkConfig.WAREHOUSE_SNAPSHOT_VALIDITY_TICKS.set(v);
                return true;
            }
            // -- [advanced_ae] -------------------------------------------------
            case "enable_advanced_ae_compat" ->
            {
                boolean v = parseBool(value, key);
                boolean old = ColonyLinkConfig.ENABLE_ADVANCED_AE_COMPAT.get();
                if (v == old) return false;
                ColonyLinkConfig.ENABLE_ADVANCED_AE_COMPAT.set(v);
                return true;
            }
            case "advanced_ae_craft_submission_limit" ->
            {
                int v = parseInt(value, 0, 256, key);
                int old = ColonyLinkConfig.ADVANCED_AE_CRAFT_SUBMISSION_LIMIT.get();
                if (v == old) return false;
                ColonyLinkConfig.ADVANCED_AE_CRAFT_SUBMISSION_LIMIT.set(v);
                return true;
            }
            default -> throw new IllegalArgumentException("Key not handled: " + key);
        }
    }

    // ── /colonylink reload ────────────────────────────────────────────────────

    private static int cmdReload(CommandContext<CommandSourceStack> ctx)
    {
        CommandSourceStack src = ctx.getSource();
        // NeoForge 1.21.1: ModConfigSpec.save() writes the current in-memory values back
        // to colonylink-common.toml. This is the safe and supported operation.
        //
        // Disk → memory direction: NeoForge handles that automatically via its FileWatcher
        // whenever the TOML file changes on disk. There is no public API to force-trigger
        // that reload from code without reflection hacks — we intentionally avoid those.
        //
        // Practical usage of /reload:
        //   1. You edited colonylink-common.toml manually on disk
        //      → Wait ~2s for NeoForge's FileWatcher to pick it up automatically, OR restart.
        //   2. You used /colonylink config set to change values in memory
        //      → /reload persists those changes to the TOML file.
        try
        {
            ColonyLinkConfig.SPEC.save();
            send(src, "§a[ColonyLink] In-memory config saved to §fcolonylink-common.toml§a.");
            send(src, "§7If you edited the TOML manually, NeoForge reloads it automatically");
            send(src, "§7within ~2s via its file watcher (no command needed).");
            ColonyLink.LOGGER.info("[ColonyLink] Config saved to disk via /colonylink reload");
        }
        catch (Exception e)
        {
            send(src, "§c[ColonyLink] Save failed: " + e.getMessage());
            ColonyLink.LOGGER.warn("[ColonyLink] config save failed", e);
        }
        return 1;
    }

    // ── /colonylink stats ─────────────────────────────────────────────────────

    private static int cmdStats(CommandContext<CommandSourceStack> ctx)
    {
        CommandSourceStack src = ctx.getSource();
        long uptimeMs = System.currentTimeMillis() - statsResetEpoch;
        long uptimeSec = uptimeMs / 1000;
        long uptimeMin = uptimeSec / 60;

        send(src, "§6════ ColonyLink Statistics ════");
        send(src, "§7  Since last reset : §f" + uptimeMin + "m " + (uptimeSec % 60) + "s ago");
        send(src, "");
        send(src, "§7  Craft jobs submitted  : §f" + statsCraftJobsSubmitted.get());
        send(src, "§7  Send to builder calls : §f" + statsSendToBuilderCalls.get());
        send(src, "§7  Warehouse scans       : §f" + statsWarehouseScans.get());
        send(src, "§7  WH → ME transfers     : §f" + statsWhToMeTransfers.get());
        send(src, "§7  ME → WH transfers     : §f" + statsMeToWhTransfers.get());
        send(src, "§7  Tool substitutions    : §f" + statsToolSubstitutions.get());
        send(src, "");
        send(src, "§8  Use §f/colonylink stats reset §8to clear counters.");
        return 1;
    }

    private static int cmdStatsReset(CommandContext<CommandSourceStack> ctx)
    {
        statsCraftJobsSubmitted.set(0);
        statsSendToBuilderCalls.set(0);
        statsWarehouseScans.set(0);
        statsWhToMeTransfers.set(0);
        statsMeToWhTransfers.set(0);
        statsToolSubstitutions.set(0);
        statsResetEpoch = System.currentTimeMillis();
        send(ctx.getSource(), "§a[ColonyLink] Statistics reset.");
        return 1;
    }

    // ── Parse helpers ─────────────────────────────────────────────────────────

    private static long parseLong(String value, long min, long max, String key)
    {
        long v;
        try { v = Long.parseLong(value); }
        catch (NumberFormatException e)
        { throw new IllegalArgumentException("Expected a number, got '" + value + "'"); }
        if (v < min || v > max)
            throw new IllegalArgumentException("Value must be between " + min + " and " + max);
        return v;
    }

    private static int parseInt(String value, int min, int max, String key)
    {
        int v;
        try { v = Integer.parseInt(value); }
        catch (NumberFormatException e)
        { throw new IllegalArgumentException("Expected an integer, got '" + value + "'"); }
        if (v < min || v > max)
            throw new IllegalArgumentException("Value must be between " + min + " and " + max);
        return v;
    }

    private static boolean parseBool(String value, String key)
    {
        if (value.equalsIgnoreCase("true")  || value.equals("1")) return true;
        if (value.equalsIgnoreCase("false") || value.equals("0")) return false;
        throw new IllegalArgumentException("Expected true/false, got '" + value + "'");
    }

    // ── Send helper ───────────────────────────────────────────────────────────

    private static void send(CommandSourceStack src, String msg)
    {
        src.sendSuccess(() -> Component.literal(msg), false);
    }

    private static String flag(boolean present)
    {
        return present ? "§a✔ present" : "§7— not installed";
    }
}
