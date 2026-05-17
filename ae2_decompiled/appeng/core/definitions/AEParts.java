/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.Item$Properties
 */
package appeng.core.definitions;

import appeng.api.ids.AEPartIds;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartItem;
import appeng.api.parts.PartModels;
import appeng.api.util.AEColor;
import appeng.core.AppEng;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.ColoredItemDefinition;
import appeng.core.definitions.ItemDefinition;
import appeng.items.parts.ColoredPartItem;
import appeng.items.parts.PartItem;
import appeng.items.parts.PartModelsHelper;
import appeng.parts.automation.AnnihilationPlanePart;
import appeng.parts.automation.AnnihilationPlanePartItem;
import appeng.parts.automation.EnergyLevelEmitterPart;
import appeng.parts.automation.ExportBusPart;
import appeng.parts.automation.FormationPlanePart;
import appeng.parts.automation.ImportBusPart;
import appeng.parts.automation.StorageLevelEmitterPart;
import appeng.parts.crafting.PatternProviderPart;
import appeng.parts.encoding.PatternEncodingTerminalPart;
import appeng.parts.misc.CableAnchorPart;
import appeng.parts.misc.InterfacePart;
import appeng.parts.misc.InvertedToggleBusPart;
import appeng.parts.misc.ToggleBusPart;
import appeng.parts.networking.CoveredCablePart;
import appeng.parts.networking.CoveredDenseCablePart;
import appeng.parts.networking.EnergyAcceptorPart;
import appeng.parts.networking.GlassCablePart;
import appeng.parts.networking.QuartzFiberPart;
import appeng.parts.networking.SmartCablePart;
import appeng.parts.networking.SmartDenseCablePart;
import appeng.parts.p2p.FEP2PTunnelPart;
import appeng.parts.p2p.FluidP2PTunnelPart;
import appeng.parts.p2p.ItemP2PTunnelPart;
import appeng.parts.p2p.LightP2PTunnelPart;
import appeng.parts.p2p.MEP2PTunnelPart;
import appeng.parts.p2p.RedstoneP2PTunnelPart;
import appeng.parts.reporting.ConversionMonitorPart;
import appeng.parts.reporting.CraftingTerminalPart;
import appeng.parts.reporting.DarkPanelPart;
import appeng.parts.reporting.ItemTerminalPart;
import appeng.parts.reporting.PanelPart;
import appeng.parts.reporting.PatternAccessTerminalPart;
import appeng.parts.reporting.SemiDarkPanelPart;
import appeng.parts.reporting.StorageMonitorPart;
import appeng.parts.storagebus.StorageBusPart;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public final class AEParts {
    public static final List<ColoredItemDefinition<?>> COLORED_PARTS = new ArrayList();
    public static final ColoredItemDefinition<ColoredPartItem<SmartCablePart>> SMART_CABLE = AEParts.constructColoredDefinition("ME Smart Cable", "smart_cable", SmartCablePart.class, SmartCablePart::new);
    public static final ColoredItemDefinition<ColoredPartItem<CoveredCablePart>> COVERED_CABLE = AEParts.constructColoredDefinition("ME Covered Cable", "covered_cable", CoveredCablePart.class, CoveredCablePart::new);
    public static final ColoredItemDefinition<ColoredPartItem<GlassCablePart>> GLASS_CABLE = AEParts.constructColoredDefinition("ME Glass Cable", "glass_cable", GlassCablePart.class, GlassCablePart::new);
    public static final ColoredItemDefinition<ColoredPartItem<CoveredDenseCablePart>> COVERED_DENSE_CABLE = AEParts.constructColoredDefinition("ME Dense Covered Cable", "covered_dense_cable", CoveredDenseCablePart.class, CoveredDenseCablePart::new);
    public static final ColoredItemDefinition<ColoredPartItem<SmartDenseCablePart>> SMART_DENSE_CABLE = AEParts.constructColoredDefinition("ME Dense Smart Cable", "smart_dense_cable", SmartDenseCablePart.class, SmartDenseCablePart::new);
    public static final ItemDefinition<PartItem<QuartzFiberPart>> QUARTZ_FIBER = AEParts.createPart("Quartz Fiber", AEPartIds.QUARTZ_FIBER, QuartzFiberPart.class, QuartzFiberPart::new);
    public static final ItemDefinition<PartItem<ToggleBusPart>> TOGGLE_BUS = AEParts.createPart("ME Toggle Bus", AEPartIds.TOGGLE_BUS, ToggleBusPart.class, ToggleBusPart::new);
    public static final ItemDefinition<PartItem<InvertedToggleBusPart>> INVERTED_TOGGLE_BUS = AEParts.createPart("ME Inverted Toggle Bus", AEPartIds.INVERTED_TOGGLE_BUS, InvertedToggleBusPart.class, InvertedToggleBusPart::new);
    public static final ItemDefinition<PartItem<CableAnchorPart>> CABLE_ANCHOR = AEParts.createPart("Cable Anchor", AEPartIds.CABLE_ANCHOR, CableAnchorPart.class, CableAnchorPart::new);
    public static final ItemDefinition<PartItem<PanelPart>> MONITOR = AEParts.createPart("Bright Illuminated Panel", AEPartIds.MONITOR, PanelPart.class, PanelPart::new);
    public static final ItemDefinition<PartItem<SemiDarkPanelPart>> SEMI_DARK_MONITOR = AEParts.createPart("Illuminated Panel", AEPartIds.SEMI_DARK_MONITOR, SemiDarkPanelPart.class, SemiDarkPanelPart::new);
    public static final ItemDefinition<PartItem<DarkPanelPart>> DARK_MONITOR = AEParts.createPart("Dark Illuminated Panel", AEPartIds.DARK_MONITOR, DarkPanelPart.class, DarkPanelPart::new);
    public static final ItemDefinition<PartItem<StorageBusPart>> STORAGE_BUS = AEParts.createPart("ME Storage Bus", AEPartIds.STORAGE_BUS, StorageBusPart.class, StorageBusPart::new);
    public static final ItemDefinition<PartItem<ImportBusPart>> IMPORT_BUS = AEParts.createPart("ME Import Bus", AEPartIds.IMPORT_BUS, ImportBusPart.class, ImportBusPart::new);
    public static final ItemDefinition<PartItem<ExportBusPart>> EXPORT_BUS = AEParts.createPart("ME Export Bus", AEPartIds.EXPORT_BUS, ExportBusPart.class, ExportBusPart::new);
    public static final ItemDefinition<PartItem<StorageLevelEmitterPart>> LEVEL_EMITTER = AEParts.createPart("ME Level Emitter", AEPartIds.LEVEL_EMITTER, StorageLevelEmitterPart.class, StorageLevelEmitterPart::new);
    public static final ItemDefinition<PartItem<EnergyLevelEmitterPart>> ENERGY_LEVEL_EMITTER = AEParts.createPart("ME Energy Level Emitter", AEPartIds.ENERGY_LEVEL_EMITTER, EnergyLevelEmitterPart.class, EnergyLevelEmitterPart::new);
    public static final ItemDefinition<PartItem<AnnihilationPlanePart>> ANNIHILATION_PLANE = AEParts.createCustomPartItem("ME Annihilation Plane", AEPartIds.ANNIHILATION_PLANE, AnnihilationPlanePart.class, AnnihilationPlanePartItem::new);
    public static final ItemDefinition<PartItem<FormationPlanePart>> FORMATION_PLANE = AEParts.createPart("ME Formation Plane", AEPartIds.FORMATION_PLANE, FormationPlanePart.class, FormationPlanePart::new);
    public static final ItemDefinition<PartItem<PatternEncodingTerminalPart>> PATTERN_ENCODING_TERMINAL = AEParts.createPart("ME Pattern Encoding Terminal", AEPartIds.PATTERN_ENCODING_TERMINAL, PatternEncodingTerminalPart.class, PatternEncodingTerminalPart::new);
    public static final ItemDefinition<PartItem<CraftingTerminalPart>> CRAFTING_TERMINAL = AEParts.createPart("ME Crafting Terminal", AEPartIds.CRAFTING_TERMINAL, CraftingTerminalPart.class, CraftingTerminalPart::new);
    public static final ItemDefinition<PartItem<ItemTerminalPart>> TERMINAL = AEParts.createPart("ME Terminal", AEPartIds.TERMINAL, ItemTerminalPart.class, ItemTerminalPart::new);
    public static final ItemDefinition<PartItem<StorageMonitorPart>> STORAGE_MONITOR = AEParts.createPart("ME Storage Monitor", AEPartIds.STORAGE_MONITOR, StorageMonitorPart.class, StorageMonitorPart::new);
    public static final ItemDefinition<PartItem<ConversionMonitorPart>> CONVERSION_MONITOR = AEParts.createPart("ME Conversion Monitor", AEPartIds.CONVERSION_MONITOR, ConversionMonitorPart.class, ConversionMonitorPart::new);
    public static final ItemDefinition<PartItem<PatternProviderPart>> PATTERN_PROVIDER = AEParts.createPart("ME Pattern Provider", AEPartIds.PATTERN_PROVIDER, PatternProviderPart.class, PatternProviderPart::new);
    public static final ItemDefinition<PartItem<InterfacePart>> INTERFACE = AEParts.createPart("ME Interface", AEPartIds.INTERFACE, InterfacePart.class, InterfacePart::new);
    public static final ItemDefinition<PartItem<PatternAccessTerminalPart>> PATTERN_ACCESS_TERMINAL = AEParts.createPart("ME Pattern Access Terminal", AEPartIds.PATTERN_ACCESS_TERMINAL, PatternAccessTerminalPart.class, PatternAccessTerminalPart::new);
    public static final ItemDefinition<PartItem<EnergyAcceptorPart>> ENERGY_ACCEPTOR = AEParts.createPart("Energy Acceptor", AEPartIds.ENERGY_ACCEPTOR, EnergyAcceptorPart.class, EnergyAcceptorPart::new);
    public static final ItemDefinition<PartItem<MEP2PTunnelPart>> ME_P2P_TUNNEL = AEParts.createPart("ME P2P Tunnel", AEPartIds.ME_P2P_TUNNEL, MEP2PTunnelPart.class, MEP2PTunnelPart::new);
    public static final ItemDefinition<PartItem<RedstoneP2PTunnelPart>> REDSTONE_P2P_TUNNEL = AEParts.createPart("Redstone P2P Tunnel", AEPartIds.REDSTONE_P2P_TUNNEL, RedstoneP2PTunnelPart.class, RedstoneP2PTunnelPart::new);
    public static final ItemDefinition<PartItem<ItemP2PTunnelPart>> ITEM_P2P_TUNNEL = AEParts.createPart("Item P2P Tunnel", AEPartIds.ITEM_P2P_TUNNEL, ItemP2PTunnelPart.class, ItemP2PTunnelPart::new);
    public static final ItemDefinition<PartItem<FluidP2PTunnelPart>> FLUID_P2P_TUNNEL = AEParts.createPart("Fluid P2P Tunnel", AEPartIds.FLUID_P2P_TUNNEL, FluidP2PTunnelPart.class, FluidP2PTunnelPart::new);
    public static final ItemDefinition<PartItem<FEP2PTunnelPart>> FE_P2P_TUNNEL = AEParts.createPart("Energy P2P Tunnel", AEPartIds.FE_P2P_TUNNEL, FEP2PTunnelPart.class, FEP2PTunnelPart::new);
    public static final ItemDefinition<PartItem<LightP2PTunnelPart>> LIGHT_P2P_TUNNEL = AEParts.createPart("Light P2P Tunnel", AEPartIds.LIGHT_P2P_TUNNEL, LightP2PTunnelPart.class, LightP2PTunnelPart::new);

    private static <T extends IPart> ItemDefinition<PartItem<T>> createPart(String englishName, ResourceLocation id, Class<T> partClass, Function<IPartItem<T>, T> factory) {
        PartModels.registerModels(PartModelsHelper.createModels(partClass));
        return AEItems.item(englishName, id, props -> new PartItem((Item.Properties)props, partClass, factory));
    }

    private static <T extends IPart> ItemDefinition<PartItem<T>> createCustomPartItem(String englishName, ResourceLocation id, Class<T> partClass, Function<Item.Properties, PartItem<T>> factory) {
        PartModels.registerModels(PartModelsHelper.createModels(partClass));
        return AEItems.item(englishName, id, factory);
    }

    private static <T extends IPart> ColoredItemDefinition<ColoredPartItem<T>> constructColoredDefinition(String nameSuffix, String idSuffix, Class<T> partClass, Function<ColoredPartItem<T>, T> factory) {
        PartModels.registerModels(PartModelsHelper.createModels(partClass));
        ColoredItemDefinition<ColoredPartItem<ColoredPartItem>> definition = new ColoredItemDefinition<ColoredPartItem<ColoredPartItem>>();
        for (AEColor color : AEColor.values()) {
            String id = color.registryPrefix + "_" + idSuffix;
            String name = color.englishName + " " + nameSuffix;
            ItemDefinition<ColoredPartItem> itemDef = AEItems.item(name, AppEng.makeId(id), props -> new ColoredPartItem((Item.Properties)props, partClass, factory, color));
            definition.add(color, AppEng.makeId(id), itemDef);
        }
        COLORED_PARTS.add(definition);
        return definition;
    }

    public static void init() {
    }
}

