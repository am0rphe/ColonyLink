/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Preconditions
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.CreativeModeTab
 *  net.minecraft.world.item.CreativeModeTabs
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.Rarity
 *  net.neoforged.neoforge.registries.DeferredRegister
 *  net.neoforged.neoforge.registries.DeferredRegister$Items
 *  org.jetbrains.annotations.Nullable
 */
package appeng.core.definitions;

import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.ids.AECreativeTabIds;
import appeng.api.ids.AEItemIds;
import appeng.api.stacks.AEKeyType;
import appeng.api.upgrades.Upgrades;
import appeng.api.util.AEColor;
import appeng.core.AEConfig;
import appeng.core.AppEng;
import appeng.core.MainCreativeTab;
import appeng.core.definitions.ColoredItemDefinition;
import appeng.core.definitions.ItemDefinition;
import appeng.crafting.pattern.AECraftingPattern;
import appeng.crafting.pattern.AEProcessingPattern;
import appeng.crafting.pattern.AESmithingTablePattern;
import appeng.crafting.pattern.AEStonecuttingPattern;
import appeng.debug.DebugCardItem;
import appeng.debug.EraserItem;
import appeng.debug.MeteoritePlacerItem;
import appeng.debug.ReplicatorCardItem;
import appeng.items.materials.EnergyCardItem;
import appeng.items.materials.MaterialItem;
import appeng.items.materials.NamePressItem;
import appeng.items.materials.StorageComponentItem;
import appeng.items.misc.MeteoriteCompassItem;
import appeng.items.misc.MissingContentItem;
import appeng.items.misc.PaintBallItem;
import appeng.items.misc.WrappedGenericStack;
import appeng.items.parts.FacadeItem;
import appeng.items.storage.BasicStorageCell;
import appeng.items.storage.CreativeCellItem;
import appeng.items.storage.SpatialStorageCellItem;
import appeng.items.storage.StorageTier;
import appeng.items.storage.ViewCellItem;
import appeng.items.tools.GuideItem;
import appeng.items.tools.MemoryCardItem;
import appeng.items.tools.NetworkToolItem;
import appeng.items.tools.fluix.FluixAxeItem;
import appeng.items.tools.fluix.FluixHoeItem;
import appeng.items.tools.fluix.FluixPickaxeItem;
import appeng.items.tools.fluix.FluixSmithingTemplateItem;
import appeng.items.tools.fluix.FluixSpadeItem;
import appeng.items.tools.fluix.FluixSwordItem;
import appeng.items.tools.powered.ChargedStaffItem;
import appeng.items.tools.powered.ColorApplicatorItem;
import appeng.items.tools.powered.EntropyManipulatorItem;
import appeng.items.tools.powered.MatterCannonItem;
import appeng.items.tools.powered.PortableCellItem;
import appeng.items.tools.powered.WirelessCraftingTerminalItem;
import appeng.items.tools.powered.WirelessTerminalItem;
import appeng.items.tools.quartz.QuartzAxeItem;
import appeng.items.tools.quartz.QuartzCuttingKnifeItem;
import appeng.items.tools.quartz.QuartzHoeItem;
import appeng.items.tools.quartz.QuartzPickaxeItem;
import appeng.items.tools.quartz.QuartzSpadeItem;
import appeng.items.tools.quartz.QuartzSwordItem;
import appeng.items.tools.quartz.QuartzToolType;
import appeng.items.tools.quartz.QuartzWrenchItem;
import appeng.menu.me.common.MEStorageMenu;
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.Nullable;

public final class AEItems {
    public static final DeferredRegister.Items DR = DeferredRegister.createItems((String)"ae2");
    private static final List<ItemDefinition<?>> ITEMS = new ArrayList();
    public static final ItemDefinition<QuartzAxeItem> CERTUS_QUARTZ_AXE = AEItems.item("Certus Quartz Axe", AEItemIds.CERTUS_QUARTZ_AXE, p -> new QuartzAxeItem((Item.Properties)p, QuartzToolType.CERTUS), (ResourceKey<CreativeModeTab>)CreativeModeTabs.TOOLS_AND_UTILITIES);
    public static final ItemDefinition<QuartzHoeItem> CERTUS_QUARTZ_HOE = AEItems.item("Certus Quartz Hoe", AEItemIds.CERTUS_QUARTZ_HOE, p -> new QuartzHoeItem((Item.Properties)p, QuartzToolType.CERTUS), (ResourceKey<CreativeModeTab>)CreativeModeTabs.TOOLS_AND_UTILITIES);
    public static final ItemDefinition<QuartzSpadeItem> CERTUS_QUARTZ_SHOVEL = AEItems.item("Certus Quartz Shovel", AEItemIds.CERTUS_QUARTZ_SHOVEL, p -> new QuartzSpadeItem((Item.Properties)p, QuartzToolType.CERTUS), (ResourceKey<CreativeModeTab>)CreativeModeTabs.TOOLS_AND_UTILITIES);
    public static final ItemDefinition<QuartzPickaxeItem> CERTUS_QUARTZ_PICK = AEItems.item("Certus Quartz Pickaxe", AEItemIds.CERTUS_QUARTZ_PICK, p -> new QuartzPickaxeItem((Item.Properties)p, QuartzToolType.CERTUS), (ResourceKey<CreativeModeTab>)CreativeModeTabs.TOOLS_AND_UTILITIES);
    public static final ItemDefinition<QuartzSwordItem> CERTUS_QUARTZ_SWORD = AEItems.item("Certus Quartz Sword", AEItemIds.CERTUS_QUARTZ_SWORD, p -> new QuartzSwordItem((Item.Properties)p, QuartzToolType.CERTUS), (ResourceKey<CreativeModeTab>)CreativeModeTabs.COMBAT);
    public static final ItemDefinition<QuartzWrenchItem> CERTUS_QUARTZ_WRENCH = AEItems.item("Certus Quartz Wrench", AEItemIds.CERTUS_QUARTZ_WRENCH, p -> new QuartzWrenchItem(p.stacksTo(1)), (ResourceKey<CreativeModeTab>)CreativeModeTabs.TOOLS_AND_UTILITIES);
    public static final ItemDefinition<QuartzCuttingKnifeItem> CERTUS_QUARTZ_KNIFE = AEItems.item("Certus Quartz Cutting Knife", AEItemIds.CERTUS_QUARTZ_KNIFE, p -> new QuartzCuttingKnifeItem(p.durability(50), QuartzToolType.CERTUS), (ResourceKey<CreativeModeTab>)CreativeModeTabs.TOOLS_AND_UTILITIES);
    public static final ItemDefinition<QuartzAxeItem> NETHER_QUARTZ_AXE = AEItems.item("Nether Quartz Axe", AEItemIds.NETHER_QUARTZ_AXE, p -> new QuartzAxeItem((Item.Properties)p, QuartzToolType.NETHER), (ResourceKey<CreativeModeTab>)CreativeModeTabs.TOOLS_AND_UTILITIES);
    public static final ItemDefinition<QuartzHoeItem> NETHER_QUARTZ_HOE = AEItems.item("Nether Quartz Hoe", AEItemIds.NETHER_QUARTZ_HOE, p -> new QuartzHoeItem((Item.Properties)p, QuartzToolType.NETHER), (ResourceKey<CreativeModeTab>)CreativeModeTabs.TOOLS_AND_UTILITIES);
    public static final ItemDefinition<QuartzSpadeItem> NETHER_QUARTZ_SHOVEL = AEItems.item("Nether Quartz Shovel", AEItemIds.NETHER_QUARTZ_SHOVEL, p -> new QuartzSpadeItem((Item.Properties)p, QuartzToolType.NETHER), (ResourceKey<CreativeModeTab>)CreativeModeTabs.TOOLS_AND_UTILITIES);
    public static final ItemDefinition<QuartzPickaxeItem> NETHER_QUARTZ_PICK = AEItems.item("Nether Quartz Pickaxe", AEItemIds.NETHER_QUARTZ_PICK, p -> new QuartzPickaxeItem((Item.Properties)p, QuartzToolType.NETHER), (ResourceKey<CreativeModeTab>)CreativeModeTabs.TOOLS_AND_UTILITIES);
    public static final ItemDefinition<QuartzSwordItem> NETHER_QUARTZ_SWORD = AEItems.item("Nether Quartz Sword", AEItemIds.NETHER_QUARTZ_SWORD, p -> new QuartzSwordItem((Item.Properties)p, QuartzToolType.NETHER), (ResourceKey<CreativeModeTab>)CreativeModeTabs.COMBAT);
    public static final ItemDefinition<QuartzWrenchItem> NETHER_QUARTZ_WRENCH = AEItems.item("Nether Quartz Wrench", AEItemIds.NETHER_QUARTZ_WRENCH, p -> new QuartzWrenchItem(p.stacksTo(1)), (ResourceKey<CreativeModeTab>)CreativeModeTabs.TOOLS_AND_UTILITIES);
    public static final ItemDefinition<QuartzCuttingKnifeItem> NETHER_QUARTZ_KNIFE = AEItems.item("Nether Quartz Cutting Knife", AEItemIds.NETHER_QUARTZ_KNIFE, p -> new QuartzCuttingKnifeItem(p.stacksTo(1).durability(50), QuartzToolType.NETHER), (ResourceKey<CreativeModeTab>)CreativeModeTabs.TOOLS_AND_UTILITIES);
    public static final ItemDefinition<FluixSmithingTemplateItem> FLUIX_UPGRADE_SMITHING_TEMPLATE = AEItems.item("Fluix Upgrade", AEItemIds.FLUIX_UPGRADE_SMITHING_TEMPLATE, p -> new FluixSmithingTemplateItem(), (ResourceKey<CreativeModeTab>)CreativeModeTabs.INGREDIENTS);
    public static final ItemDefinition<FluixAxeItem> FLUIX_AXE = AEItems.item("Fluix Axe", AEItemIds.FLUIX_AXE, FluixAxeItem::new, (ResourceKey<CreativeModeTab>)CreativeModeTabs.TOOLS_AND_UTILITIES);
    public static final ItemDefinition<FluixHoeItem> FLUIX_HOE = AEItems.item("Fluix Hoe", AEItemIds.FLUIX_HOE, FluixHoeItem::new, (ResourceKey<CreativeModeTab>)CreativeModeTabs.TOOLS_AND_UTILITIES);
    public static final ItemDefinition<FluixSpadeItem> FLUIX_SHOVEL = AEItems.item("Fluix Shovel", AEItemIds.FLUIX_SHOVEL, FluixSpadeItem::new, (ResourceKey<CreativeModeTab>)CreativeModeTabs.TOOLS_AND_UTILITIES);
    public static final ItemDefinition<FluixPickaxeItem> FLUIX_PICK = AEItems.item("Fluix Pickaxe", AEItemIds.FLUIX_PICK, FluixPickaxeItem::new, (ResourceKey<CreativeModeTab>)CreativeModeTabs.TOOLS_AND_UTILITIES);
    public static final ItemDefinition<FluixSwordItem> FLUIX_SWORD = AEItems.item("Fluix Sword", AEItemIds.FLUIX_SWORD, FluixSwordItem::new, (ResourceKey<CreativeModeTab>)CreativeModeTabs.COMBAT);
    public static final ItemDefinition<EntropyManipulatorItem> ENTROPY_MANIPULATOR = AEItems.item("Entropy Manipulator", AEItemIds.ENTROPY_MANIPULATOR, p -> new EntropyManipulatorItem(p.stacksTo(1)));
    public static final ItemDefinition<WirelessTerminalItem> WIRELESS_TERMINAL = AEItems.item("Wireless Terminal", AEItemIds.WIRELESS_TERMINAL, p -> new WirelessTerminalItem(AEConfig.instance().getWirelessTerminalBattery(), p.stacksTo(1)));
    public static final ItemDefinition<WirelessTerminalItem> WIRELESS_CRAFTING_TERMINAL = AEItems.item("Wireless Crafting Terminal", AEItemIds.WIRELESS_CRAFTING_TERMINAL, p -> new WirelessCraftingTerminalItem(AEConfig.instance().getWirelessTerminalBattery(), p.stacksTo(1)));
    public static final ItemDefinition<ChargedStaffItem> CHARGED_STAFF = AEItems.item("Charged Staff", AEItemIds.CHARGED_STAFF, p -> new ChargedStaffItem(p.stacksTo(1)));
    public static final ItemDefinition<ColorApplicatorItem> COLOR_APPLICATOR = AEItems.item("Color Applicator", AEItemIds.COLOR_APPLICATOR, p -> new ColorApplicatorItem(p.stacksTo(1)));
    public static final ItemDefinition<MatterCannonItem> MATTER_CANNON = AEItems.item("Matter Cannon", AEItemIds.MATTER_CANNON, p -> new MatterCannonItem(p.stacksTo(1)));
    public static final ItemDefinition<PortableCellItem> PORTABLE_ITEM_CELL1K = AEItems.makePortableItemCell(AEItemIds.PORTABLE_ITEM_CELL1K, StorageTier.SIZE_1K);
    public static final ItemDefinition<PortableCellItem> PORTABLE_ITEM_CELL4K = AEItems.makePortableItemCell(AEItemIds.PORTABLE_ITEM_CELL4K, StorageTier.SIZE_4K);
    public static final ItemDefinition<PortableCellItem> PORTABLE_ITEM_CELL16K = AEItems.makePortableItemCell(AEItemIds.PORTABLE_ITEM_CELL16K, StorageTier.SIZE_16K);
    public static final ItemDefinition<PortableCellItem> PORTABLE_ITEM_CELL64K = AEItems.makePortableItemCell(AEItemIds.PORTABLE_ITEM_CELL64K, StorageTier.SIZE_64K);
    public static final ItemDefinition<PortableCellItem> PORTABLE_ITEM_CELL256K = AEItems.makePortableItemCell(AEItemIds.PORTABLE_ITEM_CELL256K, StorageTier.SIZE_256K);
    public static final ItemDefinition<PortableCellItem> PORTABLE_FLUID_CELL1K = AEItems.makePortableFluidCell(AEItemIds.PORTABLE_FLUID_CELL1K, StorageTier.SIZE_1K);
    public static final ItemDefinition<PortableCellItem> PORTABLE_FLUID_CELL4K = AEItems.makePortableFluidCell(AEItemIds.PORTABLE_FLUID_CELL4K, StorageTier.SIZE_4K);
    public static final ItemDefinition<PortableCellItem> PORTABLE_FLUID_CELL16K = AEItems.makePortableFluidCell(AEItemIds.PORTABLE_FLUID_CELL16K, StorageTier.SIZE_16K);
    public static final ItemDefinition<PortableCellItem> PORTABLE_FLUID_CELL64K = AEItems.makePortableFluidCell(AEItemIds.PORTABLE_FLUID_CELL64K, StorageTier.SIZE_64K);
    public static final ItemDefinition<PortableCellItem> PORTABLE_FLUID_CELL256K = AEItems.makePortableFluidCell(AEItemIds.PORTABLE_FLUID_CELL256K, StorageTier.SIZE_256K);
    public static final ItemDefinition<NetworkToolItem> NETWORK_TOOL = AEItems.item("Network Tool", AEItemIds.NETWORK_TOOL, p -> new NetworkToolItem(p.stacksTo(1)));
    public static final ItemDefinition<MemoryCardItem> MEMORY_CARD = AEItems.item("Memory Card", AEItemIds.MEMORY_CARD, p -> new MemoryCardItem(p.stacksTo(1)));
    public static final ItemDefinition<FacadeItem> FACADE = AEItems.item("Cable Facade", AEItemIds.FACADE, FacadeItem::new);
    public static final ItemDefinition<MaterialItem> BLANK_PATTERN = AEItems.item("Blank Pattern", AEItemIds.BLANK_PATTERN, MaterialItem::new);
    public static final ItemDefinition<Item> CRAFTING_PATTERN = AEItems.item("Crafting Pattern", AEItemIds.CRAFTING_PATTERN, p -> PatternDetailsHelper.encodedPatternItemBuilder(AECraftingPattern::new).invalidPatternTooltip(AECraftingPattern::getInvalidPatternTooltip).build());
    public static final ItemDefinition<Item> PROCESSING_PATTERN = AEItems.item("Processing Pattern", AEItemIds.PROCESSING_PATTERN, p -> PatternDetailsHelper.encodedPatternItemBuilder(AEProcessingPattern::new).invalidPatternTooltip(AEProcessingPattern::getInvalidPatternTooltip).build());
    public static final ItemDefinition<Item> SMITHING_TABLE_PATTERN = AEItems.item("Smithing Table Pattern", AEItemIds.SMITHING_TABLE_PATTERN, p -> PatternDetailsHelper.encodedPatternItemBuilder(AESmithingTablePattern::new).invalidPatternTooltip(AESmithingTablePattern::getInvalidTooltip).build());
    public static final ItemDefinition<Item> STONECUTTING_PATTERN = AEItems.item("Stonecutting Pattern", AEItemIds.STONECUTTING_PATTERN, p -> PatternDetailsHelper.encodedPatternItemBuilder(AEStonecuttingPattern::new).invalidPatternTooltip(AEStonecuttingPattern::getInvalidTooltip).build());
    public static final ItemDefinition<Item> MISSING_CONTENT = AEItems.item("Missing Content", AEItemIds.MISSING_CONTENT, MissingContentItem::new, null);
    public static final ColoredItemDefinition<PaintBallItem> COLORED_PAINT_BALL = AEItems.createColoredItems("Paint Ball", AEItemIds.COLORED_PAINT_BALL, (p, color) -> new PaintBallItem((Item.Properties)p, (AEColor)((Object)color), false));
    public static final ColoredItemDefinition<PaintBallItem> COLORED_LUMEN_PAINT_BALL = AEItems.createColoredItems("Lumen Paint Ball", AEItemIds.COLORED_LUMEN_PAINT_BALL, (p, color) -> new PaintBallItem((Item.Properties)p, (AEColor)((Object)color), true));
    public static final ItemDefinition<MeteoriteCompassItem> METEORITE_COMPASS = AEItems.item("Meteorite Compass", AEItemIds.METEORITE_COMPASS, MeteoriteCompassItem::new);
    public static final ItemDefinition<MaterialItem> CERTUS_QUARTZ_CRYSTAL = AEItems.item("Certus Quartz Crystal", AEItemIds.CERTUS_QUARTZ_CRYSTAL, MaterialItem::new);
    public static final ItemDefinition<MaterialItem> CERTUS_QUARTZ_CRYSTAL_CHARGED = AEItems.item("Charged Certus Quartz Crystal", AEItemIds.CERTUS_QUARTZ_CRYSTAL_CHARGED, MaterialItem::new);
    public static final ItemDefinition<MaterialItem> CERTUS_QUARTZ_DUST = AEItems.item("Certus Quartz Dust", AEItemIds.CERTUS_QUARTZ_DUST, MaterialItem::new);
    public static final ItemDefinition<MaterialItem> SILICON = AEItems.item("Silicon", AEItemIds.SILICON, MaterialItem::new);
    public static final ItemDefinition<MaterialItem> MATTER_BALL = AEItems.item("Matter Ball", AEItemIds.MATTER_BALL, MaterialItem::new);
    public static final ItemDefinition<MaterialItem> FLUIX_CRYSTAL = AEItems.item("Fluix Crystal", AEItemIds.FLUIX_CRYSTAL, MaterialItem::new);
    public static final ItemDefinition<MaterialItem> FLUIX_DUST = AEItems.item("Fluix Dust", AEItemIds.FLUIX_DUST, MaterialItem::new);
    public static final ItemDefinition<MaterialItem> FLUIX_PEARL = AEItems.item("Fluix Pearl", AEItemIds.FLUIX_PEARL, MaterialItem::new);
    public static final ItemDefinition<MaterialItem> CALCULATION_PROCESSOR_PRESS = AEItems.item("Inscriber Calculation Press", AEItemIds.CALCULATION_PROCESSOR_PRESS, MaterialItem::new);
    public static final ItemDefinition<MaterialItem> ENGINEERING_PROCESSOR_PRESS = AEItems.item("Inscriber Engineering Press", AEItemIds.ENGINEERING_PROCESSOR_PRESS, MaterialItem::new);
    public static final ItemDefinition<MaterialItem> LOGIC_PROCESSOR_PRESS = AEItems.item("Inscriber Logic Press", AEItemIds.LOGIC_PROCESSOR_PRESS, MaterialItem::new);
    public static final ItemDefinition<MaterialItem> CALCULATION_PROCESSOR_PRINT = AEItems.item("Printed Calculation Circuit", AEItemIds.CALCULATION_PROCESSOR_PRINT, MaterialItem::new);
    public static final ItemDefinition<MaterialItem> ENGINEERING_PROCESSOR_PRINT = AEItems.item("Printed Engineering Circuit", AEItemIds.ENGINEERING_PROCESSOR_PRINT, MaterialItem::new);
    public static final ItemDefinition<MaterialItem> LOGIC_PROCESSOR_PRINT = AEItems.item("Printed Logic Circuit", AEItemIds.LOGIC_PROCESSOR_PRINT, MaterialItem::new);
    public static final ItemDefinition<MaterialItem> SILICON_PRESS = AEItems.item("Inscriber Silicon Press", AEItemIds.SILICON_PRESS, MaterialItem::new);
    public static final ItemDefinition<MaterialItem> SILICON_PRINT = AEItems.item("Printed Silicon", AEItemIds.SILICON_PRINT, MaterialItem::new);
    public static final ItemDefinition<NamePressItem> NAME_PRESS = AEItems.item("Inscriber Name Press", AEItemIds.NAME_PRESS, NamePressItem::new);
    public static final ItemDefinition<MaterialItem> LOGIC_PROCESSOR = AEItems.item("Logic Processor", AEItemIds.LOGIC_PROCESSOR, MaterialItem::new);
    public static final ItemDefinition<MaterialItem> CALCULATION_PROCESSOR = AEItems.item("Calculation Processor", AEItemIds.CALCULATION_PROCESSOR, MaterialItem::new);
    public static final ItemDefinition<MaterialItem> ENGINEERING_PROCESSOR = AEItems.item("Engineering Processor", AEItemIds.ENGINEERING_PROCESSOR, MaterialItem::new);
    public static final ItemDefinition<MaterialItem> BASIC_CARD = AEItems.item("Basic Card", AEItemIds.BASIC_CARD, MaterialItem::new);
    public static final ItemDefinition<Item> REDSTONE_CARD = AEItems.item("Redstone Card", AEItemIds.REDSTONE_CARD, Upgrades::createUpgradeCardItem);
    public static final ItemDefinition<Item> CAPACITY_CARD = AEItems.item("Capacity Card", AEItemIds.CAPACITY_CARD, Upgrades::createUpgradeCardItem);
    public static final ItemDefinition<Item> VOID_CARD = AEItems.item("Overflow Destruction Card", AEItemIds.VOID_CARD, Upgrades::createUpgradeCardItem);
    public static final ItemDefinition<MaterialItem> ADVANCED_CARD = AEItems.item("Advanced Card", AEItemIds.ADVANCED_CARD, MaterialItem::new);
    public static final ItemDefinition<Item> FUZZY_CARD = AEItems.item("Fuzzy Card", AEItemIds.FUZZY_CARD, Upgrades::createUpgradeCardItem);
    public static final ItemDefinition<Item> SPEED_CARD = AEItems.item("Acceleration Card", AEItemIds.SPEED_CARD, Upgrades::createUpgradeCardItem);
    public static final ItemDefinition<Item> INVERTER_CARD = AEItems.item("Inverter Card", AEItemIds.INVERTER_CARD, Upgrades::createUpgradeCardItem);
    public static final ItemDefinition<Item> CRAFTING_CARD = AEItems.item("Crafting Card", AEItemIds.CRAFTING_CARD, Upgrades::createUpgradeCardItem);
    public static final ItemDefinition<Item> EQUAL_DISTRIBUTION_CARD = AEItems.item("Equal Distribution Card", AEItemIds.EQUAL_DISTRIBUTION_CARD, Upgrades::createUpgradeCardItem);
    public static final ItemDefinition<EnergyCardItem> ENERGY_CARD = AEItems.item("Energy Card", AEItemIds.ENERGY_CARD, p -> new EnergyCardItem((Item.Properties)p, 1));
    public static final ItemDefinition<MaterialItem> SPATIAL_2_CELL_COMPONENT = AEItems.item("2\u00b3 Spatial Component", AEItemIds.SPATIAL_2_CELL_COMPONENT, MaterialItem::new);
    public static final ItemDefinition<MaterialItem> SPATIAL_16_CELL_COMPONENT = AEItems.item("16\u00b3 Spatial Component", AEItemIds.SPATIAL_16_CELL_COMPONENT, MaterialItem::new);
    public static final ItemDefinition<MaterialItem> SPATIAL_128_CELL_COMPONENT = AEItems.item("128\u00b3 Spatial Component", AEItemIds.SPATIAL_128_CELL_COMPONENT, MaterialItem::new);
    public static final ItemDefinition<StorageComponentItem> CELL_COMPONENT_1K = AEItems.item("1k ME Storage Component", AEItemIds.CELL_COMPONENT_1K, p -> new StorageComponentItem((Item.Properties)p, 1));
    public static final ItemDefinition<StorageComponentItem> CELL_COMPONENT_4K = AEItems.item("4k ME Storage Component", AEItemIds.CELL_COMPONENT_4K, p -> new StorageComponentItem((Item.Properties)p, 4));
    public static final ItemDefinition<StorageComponentItem> CELL_COMPONENT_16K = AEItems.item("16k ME Storage Component", AEItemIds.CELL_COMPONENT_16K, p -> new StorageComponentItem((Item.Properties)p, 16));
    public static final ItemDefinition<StorageComponentItem> CELL_COMPONENT_64K = AEItems.item("64k ME Storage Component", AEItemIds.CELL_COMPONENT_64K, p -> new StorageComponentItem((Item.Properties)p, 64));
    public static final ItemDefinition<StorageComponentItem> CELL_COMPONENT_256K = AEItems.item("256k ME Storage Component", AEItemIds.CELL_COMPONENT_256K, p -> new StorageComponentItem((Item.Properties)p, 256));
    public static final ItemDefinition<MaterialItem> ITEM_CELL_HOUSING = AEItems.item("ME Item Cell Housing", AEItemIds.ITEM_CELL_HOUSING, MaterialItem::new);
    public static final ItemDefinition<MaterialItem> FLUID_CELL_HOUSING = AEItems.item("ME Fluid Cell Housing", AEItemIds.FLUID_CELL_HOUSING, MaterialItem::new);
    public static final ItemDefinition<MaterialItem> WIRELESS_RECEIVER = AEItems.item("Wireless Receiver", AEItemIds.WIRELESS_RECEIVER, MaterialItem::new);
    public static final ItemDefinition<MaterialItem> WIRELESS_BOOSTER = AEItems.item("Wireless Booster", AEItemIds.WIRELESS_BOOSTER, MaterialItem::new);
    public static final ItemDefinition<MaterialItem> FORMATION_CORE = AEItems.item("Formation Core", AEItemIds.FORMATION_CORE, MaterialItem::new);
    public static final ItemDefinition<MaterialItem> ANNIHILATION_CORE = AEItems.item("Annihilation Core", AEItemIds.ANNIHILATION_CORE, MaterialItem::new);
    public static final ItemDefinition<MaterialItem> SKY_DUST = AEItems.item("Sky Stone Dust", AEItemIds.SKY_DUST, MaterialItem::new);
    public static final ItemDefinition<MaterialItem> ENDER_DUST = AEItems.item("Ender Dust", AEItemIds.ENDER_DUST, MaterialItem::new);
    public static final ItemDefinition<MaterialItem> SINGULARITY = AEItems.item("Singularity", AEItemIds.SINGULARITY, MaterialItem::new);
    public static final ItemDefinition<MaterialItem> QUANTUM_ENTANGLED_SINGULARITY = AEItems.item("Quantum Entangled Singularity", AEItemIds.QUANTUM_ENTANGLED_SINGULARITY, MaterialItem::new);
    public static final ItemDefinition<CreativeCellItem> CREATIVE_CELL = AEItems.item("Creative ME Storage Cell", AEItemIds.CREATIVE_CELL, p -> new CreativeCellItem(p.stacksTo(1).rarity(Rarity.EPIC)));
    public static final ItemDefinition<ViewCellItem> VIEW_CELL = AEItems.item("View Cell", AEItemIds.VIEW_CELL, p -> new ViewCellItem(p.stacksTo(1)));
    public static final ItemDefinition<BasicStorageCell> ITEM_CELL_1K = AEItems.item("1k ME Item Storage Cell", AEItemIds.ITEM_CELL_1K, p -> new BasicStorageCell(p.stacksTo(1), 0.5, 1, 8, 63, AEKeyType.items()));
    public static final ItemDefinition<BasicStorageCell> ITEM_CELL_4K = AEItems.item("4k ME Item Storage Cell", AEItemIds.ITEM_CELL_4K, p -> new BasicStorageCell(p.stacksTo(1), 1.0, 4, 32, 63, AEKeyType.items()));
    public static final ItemDefinition<BasicStorageCell> ITEM_CELL_16K = AEItems.item("16k ME Item Storage Cell", AEItemIds.ITEM_CELL_16K, p -> new BasicStorageCell(p.stacksTo(1), 1.5, 16, 128, 63, AEKeyType.items()));
    public static final ItemDefinition<BasicStorageCell> ITEM_CELL_64K = AEItems.item("64k ME Item Storage Cell", AEItemIds.ITEM_CELL_64K, p -> new BasicStorageCell(p.stacksTo(1), 2.0, 64, 512, 63, AEKeyType.items()));
    public static final ItemDefinition<BasicStorageCell> ITEM_CELL_256K = AEItems.item("256k ME Item Storage Cell", AEItemIds.ITEM_CELL_256K, p -> new BasicStorageCell(p.stacksTo(1), 2.5, 256, 2048, 63, AEKeyType.items()));
    public static final ItemDefinition<BasicStorageCell> FLUID_CELL_1K = AEItems.item("1k ME Fluid Storage Cell", AEItemIds.FLUID_CELL_1K, p -> new BasicStorageCell(p.stacksTo(1), 0.5, 1, 8, 18, AEKeyType.fluids()));
    public static final ItemDefinition<BasicStorageCell> FLUID_CELL_4K = AEItems.item("4k ME Fluid Storage Cell", AEItemIds.FLUID_CELL_4K, p -> new BasicStorageCell(p.stacksTo(1), 1.0, 4, 32, 18, AEKeyType.fluids()));
    public static final ItemDefinition<BasicStorageCell> FLUID_CELL_16K = AEItems.item("16k ME Fluid Storage Cell", AEItemIds.FLUID_CELL_16K, p -> new BasicStorageCell(p.stacksTo(1), 1.5, 16, 128, 18, AEKeyType.fluids()));
    public static final ItemDefinition<BasicStorageCell> FLUID_CELL_64K = AEItems.item("64k ME Fluid Storage Cell", AEItemIds.FLUID_CELL_64K, p -> new BasicStorageCell(p.stacksTo(1), 2.0, 64, 512, 18, AEKeyType.fluids()));
    public static final ItemDefinition<BasicStorageCell> FLUID_CELL_256K = AEItems.item("256k ME Fluid Storage Cell", AEItemIds.FLUID_CELL_256K, p -> new BasicStorageCell(p.stacksTo(1), 2.5, 256, 2048, 18, AEKeyType.fluids()));
    public static final ItemDefinition<SpatialStorageCellItem> SPATIAL_CELL2 = AEItems.item("2\u00b3 Spatial Storage Cell", AEItemIds.SPATIAL_CELL_2, p -> new SpatialStorageCellItem(p.stacksTo(1), 2));
    public static final ItemDefinition<SpatialStorageCellItem> SPATIAL_CELL16 = AEItems.item("16\u00b3 Spatial Storage Cell", AEItemIds.SPATIAL_CELL_16, p -> new SpatialStorageCellItem(p.stacksTo(1), 16));
    public static final ItemDefinition<SpatialStorageCellItem> SPATIAL_CELL128 = AEItems.item("128\u00b3 Spatial Storage Cell", AEItemIds.SPATIAL_CELL_128, p -> new SpatialStorageCellItem(p.stacksTo(1), 128));
    public static final ItemDefinition<Item> TABLET = AEItems.item("Guide", AEItemIds.GUIDE, p -> new GuideItem(p.stacksTo(1)));
    public static final ItemDefinition<EraserItem> DEBUG_ERASER = AEItems.item("Dev.Eraser", AppEng.makeId("debug_eraser"), EraserItem::new);
    public static final ItemDefinition<MeteoritePlacerItem> DEBUG_METEORITE_PLACER = AEItems.item("Dev.MeteoritePlacer", AppEng.makeId("debug_meteorite_placer"), MeteoritePlacerItem::new);
    public static final ItemDefinition<DebugCardItem> DEBUG_CARD = AEItems.item("Dev.DebugCard", AppEng.makeId("debug_card"), DebugCardItem::new);
    public static final ItemDefinition<ReplicatorCardItem> DEBUG_REPLICATOR_CARD = AEItems.item("Dev.ReplicatorCard", AppEng.makeId("debug_replicator_card"), ReplicatorCardItem::new);
    public static final ItemDefinition<WrappedGenericStack> WRAPPED_GENERIC_STACK = AEItems.item("Wrapped Generic Stack", AEItemIds.WRAPPED_GENERIC_STACK, WrappedGenericStack::new);

    private static ItemDefinition<PortableCellItem> makePortableItemCell(ResourceLocation id, StorageTier tier) {
        String name = tier.namePrefix() + " Portable Item Cell";
        return AEItems.item(name, id, p -> new PortableCellItem(AEKeyType.items(), 63 - tier.index() * 9, MEStorageMenu.PORTABLE_ITEM_CELL_TYPE, tier, p.stacksTo(1), 8440575));
    }

    private static ItemDefinition<PortableCellItem> makePortableFluidCell(ResourceLocation id, StorageTier tier) {
        String name = tier.namePrefix() + " Portable Fluid Cell";
        return AEItems.item(name, id, p -> new PortableCellItem(AEKeyType.fluids(), 18, MEStorageMenu.PORTABLE_FLUID_CELL_TYPE, tier, p.stacksTo(1), 8440575));
    }

    public static List<ItemDefinition<?>> getItems() {
        return Collections.unmodifiableList(ITEMS);
    }

    private static <T extends Item> ColoredItemDefinition<T> createColoredItems(String name, Map<AEColor, ResourceLocation> ids, BiFunction<Item.Properties, AEColor, T> factory) {
        ColoredItemDefinition<Item> colors = new ColoredItemDefinition<Item>();
        for (Map.Entry<AEColor, ResourceLocation> entry : ids.entrySet()) {
            Object fullName = entry.getKey() == AEColor.TRANSPARENT ? name : entry.getKey().getEnglishName() + " " + name;
            colors.add(entry.getKey(), entry.getValue(), AEItems.item((String)fullName, entry.getValue(), p -> (Item)factory.apply((Item.Properties)p, (AEColor)((Object)((Object)entry.getKey())))));
        }
        return colors;
    }

    static <T extends Item> ItemDefinition<T> item(String name, ResourceLocation id, Function<Item.Properties, T> factory) {
        return AEItems.item(name, id, factory, AECreativeTabIds.MAIN);
    }

    static <T extends Item> ItemDefinition<T> item(String name, ResourceLocation id, Function<Item.Properties, T> factory, @Nullable ResourceKey<CreativeModeTab> group) {
        Item.Properties p = new Item.Properties();
        Preconditions.checkArgument((boolean)id.getNamespace().equals("ae2"), (Object)"Can only register for AE2");
        ItemDefinition definition = new ItemDefinition(name, DR.registerItem(id.getPath(), factory));
        if (Objects.equals(group, AECreativeTabIds.MAIN)) {
            MainCreativeTab.add(definition);
        } else if (group != null) {
            MainCreativeTab.add(definition);
            MainCreativeTab.addExternal(group, definition);
        }
        ITEMS.add(definition);
        return definition;
    }
}

