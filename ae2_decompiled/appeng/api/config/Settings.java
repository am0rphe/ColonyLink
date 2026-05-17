/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Preconditions
 */
package appeng.api.config;

import appeng.api.config.AccessRestriction;
import appeng.api.config.CondenserOutput;
import appeng.api.config.CopyMode;
import appeng.api.config.CpuSelectionMode;
import appeng.api.config.FullnessMode;
import appeng.api.config.FuzzyMode;
import appeng.api.config.InscriberInputCapacity;
import appeng.api.config.LevelEmitterMode;
import appeng.api.config.LockCraftingMode;
import appeng.api.config.OperationMode;
import appeng.api.config.PowerUnit;
import appeng.api.config.RedstoneMode;
import appeng.api.config.RelativeDirection;
import appeng.api.config.SchedulingMode;
import appeng.api.config.Setting;
import appeng.api.config.ShowPatternProviders;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.StorageFilter;
import appeng.api.config.TerminalStyle;
import appeng.api.config.ViewItems;
import appeng.api.config.YesNo;
import com.google.common.base.Preconditions;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public final class Settings {
    private static final Map<String, Setting<?>> SETTINGS = new HashMap();
    public static final Setting<LevelEmitterMode> LEVEL_EMITTER_MODE = Settings.register("level_emitter_mode", LevelEmitterMode.class);
    public static final Setting<RedstoneMode> REDSTONE_EMITTER = Settings.register((String)"redstone_emitter", (Enum)RedstoneMode.HIGH_SIGNAL, (Enum[])new RedstoneMode[]{RedstoneMode.LOW_SIGNAL});
    public static final Setting<RedstoneMode> REDSTONE_CONTROLLED = Settings.register("redstone_controlled", RedstoneMode.class);
    public static final Setting<CondenserOutput> CONDENSER_OUTPUT = Settings.register("condenser_output", CondenserOutput.class);
    public static final Setting<PowerUnit> POWER_UNITS = Settings.register("power_units", PowerUnit.class);
    public static final Setting<AccessRestriction> ACCESS = Settings.register((String)"access", (Enum)AccessRestriction.READ_WRITE, (Enum[])new AccessRestriction[]{AccessRestriction.READ, AccessRestriction.WRITE});
    public static final Setting<SortDir> SORT_DIRECTION = Settings.register("sort_direction", SortDir.class);
    public static final Setting<SortOrder> SORT_BY = Settings.register("sort_by", SortOrder.class);
    public static final Setting<YesNo> SEARCH_TOOLTIPS = Settings.register((String)"search_tooltips", (Enum)YesNo.YES, (Enum[])new YesNo[]{YesNo.NO});
    public static final Setting<ViewItems> VIEW_MODE = Settings.register("view_mode", ViewItems.class);
    public static final Setting<RelativeDirection> IO_DIRECTION = Settings.register((String)"io_direction", (Enum)RelativeDirection.LEFT, (Enum[])new RelativeDirection[]{RelativeDirection.RIGHT});
    public static final Setting<YesNo> BLOCKING_MODE = Settings.register((String)"blocking_mode", (Enum)YesNo.YES, (Enum[])new YesNo[]{YesNo.NO});
    public static final Setting<LockCraftingMode> LOCK_CRAFTING_MODE = Settings.register("lock_crafting_mode", LockCraftingMode.class);
    public static final Setting<OperationMode> OPERATION_MODE = Settings.register("operation_mode", OperationMode.class);
    public static final Setting<FullnessMode> FULLNESS_MODE = Settings.register("fullness_mode", FullnessMode.class);
    public static final Setting<YesNo> CRAFT_ONLY = Settings.register((String)"craft_only", (Enum)YesNo.YES, (Enum[])new YesNo[]{YesNo.NO});
    public static final Setting<FuzzyMode> FUZZY_MODE = Settings.register("fuzzy_mode", FuzzyMode.class);
    public static final Setting<TerminalStyle> TERMINAL_STYLE = Settings.register((String)"terminal_style", (Enum)TerminalStyle.SMALL, (Enum[])new TerminalStyle[]{TerminalStyle.MEDIUM, TerminalStyle.TALL, TerminalStyle.FULL});
    public static final Setting<ShowPatternProviders> TERMINAL_SHOW_PATTERN_PROVIDERS = Settings.register("show_pattern_providers", ShowPatternProviders.class);
    public static final Setting<CopyMode> COPY_MODE = Settings.register("copy_mode", CopyMode.class);
    public static final Setting<YesNo> PATTERN_ACCESS_TERMINAL = Settings.register((String)"pattern_access_terminal", (Enum)YesNo.YES, (Enum[])new YesNo[]{YesNo.NO});
    public static final Setting<YesNo> CRAFT_VIA_REDSTONE = Settings.register((String)"craft_via_redstone", (Enum)YesNo.YES, (Enum[])new YesNo[]{YesNo.NO});
    public static final Setting<StorageFilter> STORAGE_FILTER = Settings.register("storage_filter", StorageFilter.class);
    public static final Setting<YesNo> PLACE_BLOCK = Settings.register((String)"place_block", (Enum)YesNo.YES, (Enum[])new YesNo[]{YesNo.NO});
    public static final Setting<SchedulingMode> SCHEDULING_MODE = Settings.register("scheduling_mode", SchedulingMode.class);
    public static final Setting<YesNo> OVERLAY_MODE = Settings.register((String)"overlay_mode", (Enum)YesNo.YES, (Enum[])new YesNo[]{YesNo.NO});
    public static final Setting<YesNo> FILTER_ON_EXTRACT = Settings.register((String)"filter_on_extract", (Enum)YesNo.YES, (Enum[])new YesNo[]{YesNo.NO});
    public static final Setting<CpuSelectionMode> CPU_SELECTION_MODE = Settings.register("crafting_scheduling_mode", CpuSelectionMode.class);
    public static final Setting<YesNo> INSCRIBER_SEPARATE_SIDES = Settings.register((String)"inscriber_separate_sides", (Enum)YesNo.NO, (Enum[])new YesNo[]{YesNo.YES});
    public static final Setting<YesNo> AUTO_EXPORT = Settings.register((String)"auto_export", (Enum)YesNo.NO, (Enum[])new YesNo[]{YesNo.YES});
    @Deprecated(forRemoval=true)
    public static final Setting<YesNo> INSCRIBER_BUFFER_SIZE = Settings.register((String)"inscriber_buffer_size", (Enum)YesNo.NO, (Enum[])new YesNo[]{YesNo.YES});
    public static final Setting<InscriberInputCapacity> INSCRIBER_INPUT_CAPACITY = Settings.register("inscriber_input_capacity", InscriberInputCapacity.class);

    private Settings() {
    }

    private static synchronized <T extends Enum<T>> Setting<T> register(String name, Class<T> enumClass) {
        Preconditions.checkState((!SETTINGS.containsKey(name) ? 1 : 0) != 0);
        Setting<T> setting = new Setting<T>(name, enumClass);
        SETTINGS.put(name, setting);
        return setting;
    }

    @SafeVarargs
    private static synchronized <T extends Enum<T>> Setting<T> register(String name, T firstOption, T ... moreOptions) {
        Preconditions.checkState((!SETTINGS.containsKey(name) ? 1 : 0) != 0);
        Setting<T[]> setting = new Setting<T[]>(name, firstOption.getDeclaringClass(), EnumSet.of(firstOption, moreOptions));
        SETTINGS.put(name, setting);
        return setting;
    }

    public static Setting<?> getOrThrow(String name) {
        Setting<?> setting = SETTINGS.get(name);
        if (setting == null) {
            throw new IllegalArgumentException("Unknown setting '" + name + "'");
        }
        return setting;
    }
}

