/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.neoforged.fml.ModContainer
 *  net.neoforged.fml.config.IConfigSpec
 *  net.neoforged.fml.config.ModConfig$Type
 *  net.neoforged.neoforge.common.ModConfigSpec
 *  net.neoforged.neoforge.common.ModConfigSpec$BooleanValue
 *  net.neoforged.neoforge.common.ModConfigSpec$Builder
 *  net.neoforged.neoforge.common.ModConfigSpec$DoubleValue
 *  net.neoforged.neoforge.common.ModConfigSpec$EnumValue
 *  net.neoforged.neoforge.common.ModConfigSpec$IntValue
 */
package appeng.core;

import appeng.api.config.CondenserOutput;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.PowerUnit;
import appeng.api.config.Settings;
import appeng.api.config.TerminalStyle;
import appeng.api.networking.pathing.ChannelMode;
import appeng.core.AELog;
import appeng.core.settings.TickRates;
import appeng.util.EnumCycler;
import appeng.util.Platform;
import java.util.HashMap;
import java.util.Map;
import java.util.function.DoubleSupplier;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.IConfigSpec;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class AEConfig {
    private final ClientConfig client = new ClientConfig();
    private final CommonConfig common = new CommonConfig();
    private static final double DEFAULT_FE_EXCHANGE = 0.5;
    private static AEConfig instance;

    private AEConfig(ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, (IConfigSpec)this.client.spec);
        container.registerConfig(ModConfig.Type.COMMON, (IConfigSpec)this.common.spec);
        container.getEventBus().addListener(evt -> {
            if (evt.getConfig().getSpec() == this.common.spec) {
                this.common.sync();
            }
        });
        container.getEventBus().addListener(evt -> {
            if (evt.getConfig().getSpec() == this.common.spec) {
                this.common.sync();
            }
        });
    }

    public static void register(ModContainer container) {
        if (!container.getModId().equals("ae2")) {
            throw new IllegalArgumentException();
        }
        instance = new AEConfig(container);
    }

    public static AEConfig instance() {
        return instance;
    }

    public double getP2PTunnelEnergyTax() {
        return (Double)this.common.p2pTunnelEnergyTax.get();
    }

    public double getP2PTunnelTransportTax() {
        return (Double)this.common.p2pTunnelTransportTax.get();
    }

    public double wireless_getDrainRate(double range) {
        return (Double)this.common.wirelessTerminalDrainMultiplier.get() * range;
    }

    public double wireless_getMaxRange(int boosters) {
        return (Double)this.common.wirelessBaseRange.get() + (Double)this.common.wirelessBoosterRangeMultiplier.get() * Math.pow(boosters, (Double)this.common.wirelessBoosterExp.get());
    }

    public double wireless_getPowerDrain(int boosters) {
        return (Double)this.common.wirelessBaseCost.get() + (Double)this.common.wirelessCostMultiplier.get() * Math.pow(boosters, 1.0 + (double)boosters / (Double)this.common.wirelessHighWirelessCount.get());
    }

    public boolean isSearchModNameInTooltips() {
        return (Boolean)this.client.searchModNameInTooltips.get();
    }

    public void setSearchModNameInTooltips(boolean enable) {
        if (enable != this.client.searchModNameInTooltips.getAsBoolean()) {
            this.client.searchModNameInTooltips.set((Object)enable);
            this.client.spec.save();
        }
    }

    public boolean isUseExternalSearch() {
        return (Boolean)this.client.useExternalSearch.get();
    }

    public void setUseExternalSearch(boolean enable) {
        if (enable != this.client.useExternalSearch.getAsBoolean()) {
            this.client.useExternalSearch.set((Object)enable);
            this.client.spec.save();
        }
    }

    public boolean isClearExternalSearchOnOpen() {
        return (Boolean)this.client.clearExternalSearchOnOpen.get();
    }

    public void setClearExternalSearchOnOpen(boolean enable) {
        if (enable != this.client.clearExternalSearchOnOpen.getAsBoolean()) {
            this.client.clearExternalSearchOnOpen.set((Object)enable);
            this.client.spec.save();
        }
    }

    public boolean isRememberLastSearch() {
        return (Boolean)this.client.rememberLastSearch.get();
    }

    public void setRememberLastSearch(boolean enable) {
        if (enable != this.client.rememberLastSearch.getAsBoolean()) {
            this.client.rememberLastSearch.set((Object)enable);
            this.client.spec.save();
        }
    }

    public boolean isAutoFocusSearch() {
        return (Boolean)this.client.autoFocusSearch.get();
    }

    public void setAutoFocusSearch(boolean enable) {
        if (enable != this.client.autoFocusSearch.getAsBoolean()) {
            this.client.autoFocusSearch.set((Object)enable);
            this.client.spec.save();
        }
    }

    public boolean isSyncWithExternalSearch() {
        return (Boolean)this.client.syncWithExternalSearch.get();
    }

    public void setSyncWithExternalSearch(boolean enable) {
        if (enable != this.client.syncWithExternalSearch.getAsBoolean()) {
            this.client.syncWithExternalSearch.set((Object)enable);
            this.client.spec.save();
        }
    }

    public TerminalStyle getTerminalStyle() {
        return (TerminalStyle)((Object)this.client.terminalStyle.get());
    }

    public void setTerminalStyle(TerminalStyle setting) {
        if (setting != this.client.terminalStyle.get()) {
            this.client.terminalStyle.set((Object)setting);
            this.client.spec.save();
        }
    }

    public double getGridEnergyStoragePerNode() {
        return (Double)this.common.gridEnergyStoragePerNode.get();
    }

    public double getCrystalResonanceGeneratorRate() {
        return (Double)this.common.crystalResonanceGeneratorRate.get();
    }

    public PowerUnit getSelectedEnergyUnit() {
        return (PowerUnit)((Object)this.client.selectedPowerUnit.get());
    }

    public void nextEnergyUnit(boolean backwards) {
        PowerUnit selected = EnumCycler.rotateEnum(this.getSelectedEnergyUnit(), backwards, Settings.POWER_UNITS.getValues());
        this.client.selectedPowerUnit.set((Object)selected);
        this.client.spec.save();
    }

    public boolean isDebugToolsEnabled() {
        return (Boolean)this.common.debugTools.get();
    }

    public int getFormationPlaneEntityLimit() {
        return (Integer)this.common.formationPlaneEntityLimit.get();
    }

    public boolean isEnableEffects() {
        return this.client.enableEffects.getAsBoolean();
    }

    public boolean isUseLargeFonts() {
        return this.client.useLargeFonts.getAsBoolean();
    }

    public boolean isUseColoredCraftingStatus() {
        return this.client.useColoredCraftingStatus.getAsBoolean();
    }

    public boolean isDisableColoredCableRecipesInRecipeViewer() {
        return this.client.disableColoredCableRecipesInRecipeViewer.getAsBoolean();
    }

    public boolean isEnableFacadesInRecipeViewer() {
        return this.client.enableFacadesInRecipeViewer.getAsBoolean();
    }

    public boolean isEnableFacadeRecipesInRecipeViewer() {
        return this.client.enableFacadeRecipesInRecipeViewer.getAsBoolean();
    }

    public boolean isExposeNetworkInventoryToEmi() {
        return this.client.exposeNetworkInventoryToEmi.getAsBoolean();
    }

    public int getCraftingCalculationTimePerTick() {
        return (Integer)this.common.craftingCalculationTimePerTick.get();
    }

    public boolean isSpatialAnchorEnablesRandomTicks() {
        return (Boolean)this.common.spatialAnchorEnableRandomTicks.get();
    }

    public double getSpatialPowerExponent() {
        return (Double)this.common.spatialPowerExponent.get();
    }

    public double getSpatialPowerMultiplier() {
        return (Double)this.common.spatialPowerMultiplier.get();
    }

    public double getChargerChargeRate() {
        return (Double)this.common.chargerChargeRate.get();
    }

    public DoubleSupplier getWirelessTerminalBattery() {
        return () -> ((ModConfigSpec.IntValue)this.common.wirelessTerminalBattery).get();
    }

    public DoubleSupplier getEntropyManipulatorBattery() {
        return () -> ((ModConfigSpec.IntValue)this.common.entropyManipulatorBattery).get();
    }

    public DoubleSupplier getMatterCannonBattery() {
        return () -> ((ModConfigSpec.IntValue)this.common.matterCannonBattery).get();
    }

    public DoubleSupplier getPortableCellBattery() {
        return () -> ((ModConfigSpec.IntValue)this.common.portableCellBattery).get();
    }

    public DoubleSupplier getColorApplicatorBattery() {
        return () -> ((ModConfigSpec.IntValue)this.common.colorApplicatorBattery).get();
    }

    public DoubleSupplier getChargedStaffBattery() {
        return () -> ((ModConfigSpec.IntValue)this.common.chargedStaffBattery).get();
    }

    public boolean isShowDebugGuiOverlays() {
        return (Boolean)this.client.debugGuiOverlays.get();
    }

    public void setShowDebugGuiOverlays(boolean enable) {
        if (enable != this.client.debugGuiOverlays.getAsBoolean()) {
            this.client.debugGuiOverlays.set((Object)enable);
            this.client.spec.save();
        }
    }

    public boolean isSpawnPressesInMeteoritesEnabled() {
        return (Boolean)this.common.spawnPressesInMeteorites.get();
    }

    public boolean isSpawnFlawlessOnlyEnabled() {
        return (Boolean)this.common.spawnFlawlessOnly.get();
    }

    public boolean isMatterCanonBlockDamageEnabled() {
        return (Boolean)this.common.matterCannonBlockDamage.get();
    }

    public boolean isTinyTntBlockDamageEnabled() {
        return (Boolean)this.common.tinyTntBlockDamage.get();
    }

    public int getGrowthAcceleratorSpeed() {
        return (Integer)this.common.growthAcceleratorSpeed.get();
    }

    public boolean isAnnihilationPlaneSkyDustGenerationEnabled() {
        return (Boolean)this.common.annihilationPlaneSkyDustGeneration.get();
    }

    public boolean isBlockUpdateLogEnabled() {
        return (Boolean)this.common.blockUpdateLog.get();
    }

    public boolean isChunkLoggerTraceEnabled() {
        return (Boolean)this.common.chunkLoggerTrace.get();
    }

    public ChannelMode getChannelMode() {
        return (ChannelMode)((Object)this.common.channels.get());
    }

    public void setChannelModel(ChannelMode mode) {
        if (mode != this.common.channels.get()) {
            this.common.channels.set((Object)mode);
            this.client.spec.save();
        }
    }

    public boolean isPlacementPreviewEnabled() {
        return (Boolean)this.client.showPlacementPreview.get();
    }

    public boolean isTooltipShowCellUpgrades() {
        return (Boolean)this.client.tooltipShowCellUpgrades.get();
    }

    public boolean isTooltipShowCellContent() {
        return (Boolean)this.client.tooltipShowCellContent.get();
    }

    public int getTooltipMaxCellContentShown() {
        return (Integer)this.client.tooltipMaxCellContentShown.get();
    }

    public boolean isPinAutoCraftedItems() {
        return (Boolean)this.client.pinAutoCraftedItems.get();
    }

    public void setPinAutoCraftedItems(boolean enabled) {
        if (enabled != this.client.pinAutoCraftedItems.getAsBoolean()) {
            this.client.pinAutoCraftedItems.set((Object)enabled);
            this.client.spec.save();
        }
    }

    public boolean isNotifyForFinishedCraftingJobs() {
        return (Boolean)this.client.notifyForFinishedCraftingJobs.get();
    }

    public void setNotifyForFinishedCraftingJobs(boolean enabled) {
        if (enabled != this.client.notifyForFinishedCraftingJobs.getAsBoolean()) {
            this.client.notifyForFinishedCraftingJobs.set((Object)enabled);
            this.client.spec.save();
        }
    }

    public boolean isClearGridOnClose() {
        return (Boolean)this.client.clearGridOnClose.get();
    }

    public void setClearGridOnClose(boolean enabled) {
        if (enabled != this.client.clearGridOnClose.getAsBoolean()) {
            this.client.clearGridOnClose.set((Object)enabled);
            this.client.spec.save();
        }
    }

    public double getVibrationChamberBaseEnergyPerFuelTick() {
        return (Double)this.common.vibrationChamberBaseEnergyPerFuelTick.get();
    }

    public int getVibrationChamberMinEnergyPerGameTick() {
        return (Integer)this.common.vibrationChamberMinEnergyPerTick.get();
    }

    public int getVibrationChamberMaxEnergyPerGameTick() {
        return (Integer)this.common.vibrationChamberMaxEnergyPerTick.get();
    }

    public int getTerminalMargin() {
        return (Integer)this.client.terminalMargin.get();
    }

    public void save() {
        this.common.spec.save();
        this.client.spec.save();
    }

    private static ModConfigSpec.BooleanValue define(ModConfigSpec.Builder builder, String name, boolean defaultValue, String comment) {
        builder.comment(comment);
        return AEConfig.define(builder, name, defaultValue);
    }

    private static ModConfigSpec.BooleanValue define(ModConfigSpec.Builder builder, String name, boolean defaultValue) {
        return builder.define(name, defaultValue);
    }

    private static ModConfigSpec.IntValue define(ModConfigSpec.Builder builder, String name, int defaultValue, String comment) {
        builder.comment(comment);
        return AEConfig.define(builder, name, defaultValue);
    }

    private static ModConfigSpec.DoubleValue define(ModConfigSpec.Builder builder, String name, double defaultValue) {
        return AEConfig.define(builder, name, defaultValue, Double.MIN_VALUE, Double.MAX_VALUE);
    }

    private static ModConfigSpec.DoubleValue define(ModConfigSpec.Builder builder, String name, double defaultValue, String comment) {
        builder.comment(comment);
        return AEConfig.define(builder, name, defaultValue);
    }

    private static ModConfigSpec.DoubleValue define(ModConfigSpec.Builder builder, String name, double defaultValue, double min, double max, String comment) {
        builder.comment(comment);
        return AEConfig.define(builder, name, defaultValue, min, max);
    }

    private static ModConfigSpec.DoubleValue define(ModConfigSpec.Builder builder, String name, double defaultValue, double min, double max) {
        return builder.defineInRange(name, defaultValue, min, max);
    }

    private static ModConfigSpec.IntValue define(ModConfigSpec.Builder builder, String name, int defaultValue, int min, int max, String comment) {
        builder.comment(comment);
        return AEConfig.define(builder, name, defaultValue, min, max);
    }

    private static ModConfigSpec.IntValue define(ModConfigSpec.Builder builder, String name, int defaultValue, int min, int max) {
        return builder.defineInRange(name, defaultValue, min, max);
    }

    private static ModConfigSpec.IntValue define(ModConfigSpec.Builder builder, String name, int defaultValue) {
        return AEConfig.define(builder, name, defaultValue, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    private static <T extends Enum<T>> ModConfigSpec.EnumValue<T> defineEnum(ModConfigSpec.Builder builder, String name, T defaultValue) {
        return builder.defineEnum(name, defaultValue);
    }

    private static <T extends Enum<T>> ModConfigSpec.EnumValue<T> defineEnum(ModConfigSpec.Builder builder, String name, T defaultValue, String comment) {
        builder.comment(comment);
        return AEConfig.defineEnum(builder, name, defaultValue);
    }

    private static class ClientConfig {
        private final ModConfigSpec spec;
        public final ModConfigSpec.BooleanValue enableEffects;
        public final ModConfigSpec.BooleanValue useLargeFonts;
        public final ModConfigSpec.BooleanValue useColoredCraftingStatus;
        public final ModConfigSpec.BooleanValue disableColoredCableRecipesInRecipeViewer;
        public final ModConfigSpec.BooleanValue enableFacadesInRecipeViewer;
        public final ModConfigSpec.BooleanValue enableFacadeRecipesInRecipeViewer;
        public final ModConfigSpec.BooleanValue exposeNetworkInventoryToEmi;
        public final ModConfigSpec.EnumValue<PowerUnit> selectedPowerUnit;
        public final ModConfigSpec.BooleanValue debugGuiOverlays;
        public final ModConfigSpec.BooleanValue showPlacementPreview;
        public final ModConfigSpec.BooleanValue notifyForFinishedCraftingJobs;
        public final ModConfigSpec.EnumValue<TerminalStyle> terminalStyle;
        public final ModConfigSpec.BooleanValue pinAutoCraftedItems;
        public final ModConfigSpec.BooleanValue clearGridOnClose;
        public final ModConfigSpec.IntValue terminalMargin;
        public final ModConfigSpec.BooleanValue searchModNameInTooltips;
        public final ModConfigSpec.BooleanValue useExternalSearch;
        public final ModConfigSpec.BooleanValue clearExternalSearchOnOpen;
        public final ModConfigSpec.BooleanValue syncWithExternalSearch;
        public final ModConfigSpec.BooleanValue rememberLastSearch;
        public final ModConfigSpec.BooleanValue autoFocusSearch;
        public final ModConfigSpec.BooleanValue tooltipShowCellUpgrades;
        public final ModConfigSpec.BooleanValue tooltipShowCellContent;
        public final ModConfigSpec.IntValue tooltipMaxCellContentShown;

        public ClientConfig() {
            ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
            builder.push("recipeViewers");
            this.disableColoredCableRecipesInRecipeViewer = AEConfig.define(builder, "disableColoredCableRecipesInRecipeViewer", true);
            this.enableFacadesInRecipeViewer = AEConfig.define(builder, "enableFacadesInRecipeViewer", false, "Show facades in REI/JEI/EMI item list");
            this.enableFacadeRecipesInRecipeViewer = AEConfig.define(builder, "enableFacadeRecipesInRecipeViewer", true, "Show facade recipes in REI/JEI/EMI for supported blocks");
            this.exposeNetworkInventoryToEmi = AEConfig.define(builder, "provideNetworkInventoryToEmi", false, "Expose the full network inventory to EMI, which might cause performance problems.");
            builder.pop();
            builder.push("client");
            this.enableEffects = AEConfig.define(builder, "enableEffects", true);
            this.useLargeFonts = AEConfig.define(builder, "useTerminalUseLargeFont", false);
            this.useColoredCraftingStatus = AEConfig.define(builder, "useColoredCraftingStatus", true);
            this.selectedPowerUnit = AEConfig.defineEnum(builder, "powerUnit", PowerUnit.AE, "Unit of power shown in AE UIs");
            this.debugGuiOverlays = AEConfig.define(builder, "showDebugGuiOverlays", false, "Show debugging GUI overlays");
            this.showPlacementPreview = AEConfig.define(builder, "showPlacementPreview", true, "Show a preview of part and facade placement");
            this.notifyForFinishedCraftingJobs = AEConfig.define(builder, "notifyForFinishedCraftingJobs", true, "Show toast when long-running crafting jobs finish.");
            builder.pop();
            ModConfigSpec.Builder terminals = builder.push("terminals");
            this.terminalStyle = AEConfig.defineEnum(terminals, "terminalStyle", TerminalStyle.SMALL);
            this.pinAutoCraftedItems = AEConfig.define(builder, "pinAutoCraftedItems", true, "Pin items that the player auto-crafts to the top of the terminal");
            this.clearGridOnClose = AEConfig.define(builder, "clearGridOnClose", false, "Automatically clear the crafting/encoding grid when closing the terminal");
            this.terminalMargin = AEConfig.define(builder, "terminalMargin", 25, "The vertical margin to apply when sizing terminals. Used to make room for centered item mod search bars");
            builder.pop();
            builder.push("search");
            this.searchModNameInTooltips = AEConfig.define(builder, "searchModNameInTooltips", false, "Should the mod name be included when searching in tooltips.");
            this.useExternalSearch = AEConfig.define(builder, "useExternalSearch", false, "Replaces AEs own search with the search of REI or JEI");
            this.clearExternalSearchOnOpen = AEConfig.define(builder, "clearExternalSearchOnOpen", true, "When using useExternalSearch, clears the search when the terminal opens");
            this.syncWithExternalSearch = AEConfig.define(builder, "syncWithExternalSearch", true, "When REI/JEI is installed, automatically set the AE or REI/JEI search text when either is changed while the terminal is open");
            this.rememberLastSearch = AEConfig.define(builder, "rememberLastSearch", true, "Remembers the last search term and restores it when the terminal opens");
            this.autoFocusSearch = AEConfig.define(builder, "autoFocusSearch", false, "Automatically focuses the search field when the terminal opens");
            builder.pop();
            builder.push("tooltips");
            this.tooltipShowCellUpgrades = AEConfig.define(builder, "showCellUpgrades", true, "Show installed upgrades in the tooltips of storage cells, color applicators and matter cannons");
            this.tooltipShowCellContent = AEConfig.define(builder, "showCellContent", true, "Show a preview of the content in the tooltips of storage cells, color applicators and matter cannons");
            this.tooltipMaxCellContentShown = AEConfig.define(builder, "maxCellContentShown", 5, 1, 32, "The maximum number of content entries to show in the tooltip of storage cells, color applicators and matter cannons");
            builder.pop();
            this.spec = builder.build();
        }
    }

    private static class CommonConfig {
        private final ModConfigSpec spec;
        public final ModConfigSpec.IntValue formationPlaneEntityLimit;
        public final ModConfigSpec.IntValue craftingCalculationTimePerTick;
        public final ModConfigSpec.BooleanValue debugTools;
        public final ModConfigSpec.BooleanValue matterCannonBlockDamage;
        public final ModConfigSpec.BooleanValue tinyTntBlockDamage;
        public final ModConfigSpec.EnumValue<ChannelMode> channels;
        public final ModConfigSpec.BooleanValue spatialAnchorEnableRandomTicks;
        public final ModConfigSpec.IntValue growthAcceleratorSpeed;
        public final ModConfigSpec.BooleanValue annihilationPlaneSkyDustGeneration;
        public final ModConfigSpec.DoubleValue spatialPowerExponent;
        public final ModConfigSpec.DoubleValue spatialPowerMultiplier;
        public final ModConfigSpec.BooleanValue blockUpdateLog;
        public final ModConfigSpec.BooleanValue craftingLog;
        public final ModConfigSpec.BooleanValue debugLog;
        public final ModConfigSpec.BooleanValue gridLog;
        public final ModConfigSpec.BooleanValue chunkLoggerTrace;
        public final ModConfigSpec.DoubleValue chargerChargeRate;
        public final ModConfigSpec.IntValue wirelessTerminalBattery;
        public final ModConfigSpec.IntValue entropyManipulatorBattery;
        public final ModConfigSpec.IntValue matterCannonBattery;
        public final ModConfigSpec.IntValue portableCellBattery;
        public final ModConfigSpec.IntValue colorApplicatorBattery;
        public final ModConfigSpec.IntValue chargedStaffBattery;
        public final ModConfigSpec.BooleanValue spawnPressesInMeteorites;
        public final ModConfigSpec.BooleanValue spawnFlawlessOnly;
        public final ModConfigSpec.DoubleValue wirelessBaseCost;
        public final ModConfigSpec.DoubleValue wirelessCostMultiplier;
        public final ModConfigSpec.DoubleValue wirelessTerminalDrainMultiplier;
        public final ModConfigSpec.DoubleValue wirelessBaseRange;
        public final ModConfigSpec.DoubleValue wirelessBoosterRangeMultiplier;
        public final ModConfigSpec.DoubleValue wirelessBoosterExp;
        public final ModConfigSpec.DoubleValue wirelessHighWirelessCount;
        public final ModConfigSpec.DoubleValue powerRatioForgeEnergy;
        public final ModConfigSpec.DoubleValue powerUsageMultiplier;
        public final ModConfigSpec.DoubleValue gridEnergyStoragePerNode;
        public final ModConfigSpec.DoubleValue crystalResonanceGeneratorRate;
        public final ModConfigSpec.DoubleValue p2pTunnelEnergyTax;
        public final ModConfigSpec.DoubleValue p2pTunnelTransportTax;
        public final ModConfigSpec.DoubleValue vibrationChamberBaseEnergyPerFuelTick;
        public final ModConfigSpec.IntValue vibrationChamberMinEnergyPerTick;
        public final ModConfigSpec.IntValue vibrationChamberMaxEnergyPerTick;
        public final ModConfigSpec.IntValue condenserMatterBallsPower;
        public final ModConfigSpec.IntValue condenserSingularityPower;
        public final Map<TickRates, ModConfigSpec.IntValue> tickRateMin = new HashMap<TickRates, ModConfigSpec.IntValue>();
        public final Map<TickRates, ModConfigSpec.IntValue> tickRateMax = new HashMap<TickRates, ModConfigSpec.IntValue>();

        public CommonConfig() {
            ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
            builder.push("general");
            this.debugTools = AEConfig.define(builder, "unsupportedDeveloperTools", Platform.isDevelopmentEnvironment());
            this.matterCannonBlockDamage = AEConfig.define(builder, "matterCannonBlockDamage", true, "Enables the ability of the Matter Cannon to break blocks.");
            this.tinyTntBlockDamage = AEConfig.define(builder, "tinyTntBlockDamage", true, "Enables the ability of Tiny TNT to break blocks.");
            this.channels = AEConfig.defineEnum(builder, "channels", ChannelMode.DEFAULT, "Changes the channel capacity that cables provide in AE2.");
            this.spatialAnchorEnableRandomTicks = AEConfig.define(builder, "spatialAnchorEnableRandomTicks", true, "Whether Spatial Anchors should force random chunk ticks and entity spawning.");
            builder.pop();
            builder.push("automation");
            this.formationPlaneEntityLimit = AEConfig.define(builder, "formationPlaneEntityLimit", 128);
            builder.pop();
            builder.push("craftingCPU");
            this.craftingCalculationTimePerTick = AEConfig.define(builder, "craftingCalculationTimePerTick", 5);
            builder.pop();
            builder.push("crafting");
            this.growthAcceleratorSpeed = AEConfig.define(builder, "growthAccelerator", 10, 1, 100, "Number of ticks between two crystal growth accelerator ticks");
            this.annihilationPlaneSkyDustGeneration = AEConfig.define(builder, "annihilationPlaneSkyDustGeneration", true, "If enabled, an annihilation placed face up at the maximum world height will generate sky stone passively.");
            builder.pop();
            builder.push("spatialio");
            this.spatialPowerMultiplier = AEConfig.define(builder, "spatialPowerMultiplier", 1250.0);
            this.spatialPowerExponent = AEConfig.define(builder, "spatialPowerExponent", 1.35);
            builder.pop();
            builder.push("logging");
            this.blockUpdateLog = AEConfig.define(builder, "blockUpdateLog", false);
            this.craftingLog = AEConfig.define(builder, "craftingLog", false);
            this.debugLog = AEConfig.define(builder, "debugLog", false);
            this.gridLog = AEConfig.define(builder, "gridLog", false);
            this.chunkLoggerTrace = AEConfig.define(builder, "chunkLoggerTrace", false, "Enable stack trace logging for the chunk loading debug command");
            builder.pop();
            builder.push("battery");
            this.chargerChargeRate = AEConfig.define(builder, "chargerChargeRate", 1.0, 0.1, 10.0, "The chargers charging rate factor, which is applied to the charged items charge rate. 2 means it charges everything twice as fast. 0.5 half as fast.");
            this.wirelessTerminalBattery = AEConfig.define(builder, "wirelessTerminal", 1600000);
            this.chargedStaffBattery = AEConfig.define(builder, "chargedStaff", 8000);
            this.entropyManipulatorBattery = AEConfig.define(builder, "entropyManipulator", 200000);
            this.portableCellBattery = AEConfig.define(builder, "portableCell", 20000);
            this.colorApplicatorBattery = AEConfig.define(builder, "colorApplicator", 20000);
            this.matterCannonBattery = AEConfig.define(builder, "matterCannon", 200000);
            builder.pop();
            builder.push("worldGen");
            this.spawnPressesInMeteorites = AEConfig.define(builder, "spawnPressesInMeteorites", true);
            this.spawnFlawlessOnly = AEConfig.define(builder, "spawnFlawlessOnly", false);
            builder.pop();
            builder.push("wireless");
            this.wirelessBaseCost = AEConfig.define(builder, "wirelessBaseCost", 8.0);
            this.wirelessCostMultiplier = AEConfig.define(builder, "wirelessCostMultiplier", 1.0);
            this.wirelessBaseRange = AEConfig.define(builder, "wirelessBaseRange", 16.0);
            this.wirelessBoosterRangeMultiplier = AEConfig.define(builder, "wirelessBoosterRangeMultiplier", 1.0);
            this.wirelessBoosterExp = AEConfig.define(builder, "wirelessBoosterExp", 1.5);
            this.wirelessHighWirelessCount = AEConfig.define(builder, "wirelessHighWirelessCount", 64.0);
            this.wirelessTerminalDrainMultiplier = AEConfig.define(builder, "wirelessTerminalDrainMultiplier", 1.0);
            builder.pop();
            builder.push("powerRatios");
            this.powerRatioForgeEnergy = AEConfig.define(builder, "forgeEnergy", 0.5);
            this.powerUsageMultiplier = AEConfig.define(builder, "usageMultiplier", 1.0, 0.01, Double.MAX_VALUE);
            this.gridEnergyStoragePerNode = AEConfig.define(builder, "gridEnergyStoragePerNode", 25.0, 1.0, 1000000.0, "How much energy can the internal grid buffer storage per node attached to the grid.");
            this.crystalResonanceGeneratorRate = AEConfig.define(builder, "crystalResonanceGeneratorRate", 20.0, 0.0, 1000000.0, "How much energy a crystal resonance generator generates per tick.");
            this.p2pTunnelEnergyTax = AEConfig.define(builder, "p2pTunnelEnergyTax", 0.025, 0.0, 1.0, "The cost to transport energy through an energy P2P tunnel expressed as a factor of the transported energy.");
            this.p2pTunnelTransportTax = AEConfig.define(builder, "p2pTunnelTransportTax", 0.025, 0.0, 1.0, "The cost to transport items/fluids/etc. through P2P tunnels, expressed in AE energy per equivalent I/O bus operation for the transported object type (i.e. items=per 1 item, fluids=per 125mb).");
            builder.pop();
            builder.push("condenser");
            this.condenserMatterBallsPower = AEConfig.define(builder, "matterBalls", 256);
            this.condenserSingularityPower = AEConfig.define(builder, "singularity", 256000);
            builder.pop();
            builder.comment(" Min / Max Tickrates for dynamic ticking, most of these components also use sleeping, to prevent constant ticking, adjust with care, non standard rates are not supported or tested.");
            builder.push("tickRates");
            for (TickRates tickRate : TickRates.values()) {
                this.tickRateMin.put(tickRate, AEConfig.define(builder, tickRate.name() + "Min", tickRate.getDefaultMin()));
                this.tickRateMax.put(tickRate, AEConfig.define(builder, tickRate.name() + "Max", tickRate.getDefaultMax()));
            }
            builder.pop();
            builder.comment("Settings for the Vibration Chamber");
            builder.push("vibrationChamber");
            this.vibrationChamberBaseEnergyPerFuelTick = AEConfig.define(builder, "baseEnergyPerFuelTick", 5.0, 0.1, 1000.0, "AE energy produced per fuel burn tick (reminder: coal = 1600, block of coal = 16000, lava bucket = 20000 burn ticks)");
            this.vibrationChamberMinEnergyPerTick = AEConfig.define(builder, "minEnergyPerGameTick", 4, 0, 1000, "Minimum amount of AE/t the vibration chamber can slow down to when energy is being wasted.");
            this.vibrationChamberMaxEnergyPerTick = AEConfig.define(builder, "baseMaxEnergyPerGameTick", 40, 1, 1000, "Maximum amount of AE/t the vibration chamber can speed up to when generated energy is being fully consumed.");
            builder.pop();
            this.spec = builder.build();
        }

        public void sync() {
            PowerUnit.FE.conversionRatio = (Double)this.powerRatioForgeEnergy.get();
            PowerMultiplier.CONFIG.multiplier = (Double)this.powerUsageMultiplier.get();
            CondenserOutput.MATTER_BALLS.requiredPower = (Integer)this.condenserMatterBallsPower.get();
            CondenserOutput.SINGULARITY.requiredPower = (Integer)this.condenserSingularityPower.get();
            for (TickRates tr : TickRates.values()) {
                tr.setMin((Integer)this.tickRateMin.get((Object)tr).get());
                tr.setMax((Integer)this.tickRateMax.get((Object)tr).get());
            }
            AELog.setCraftingLogEnabled((Boolean)this.craftingLog.get());
            AELog.setDebugLogEnabled((Boolean)this.debugLog.get());
            AELog.setGridLogEnabled((Boolean)this.gridLog.get());
        }
    }
}

