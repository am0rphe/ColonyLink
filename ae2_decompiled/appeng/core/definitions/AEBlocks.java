/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Preconditions
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.item.BlockItem
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.RotatedPillarBlock
 *  net.minecraft.world.level.block.SlabBlock
 *  net.minecraft.world.level.block.SoundType
 *  net.minecraft.world.level.block.StairBlock
 *  net.minecraft.world.level.block.WallBlock
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.block.state.BlockBehaviour$StateArgumentPredicate
 *  net.minecraft.world.level.material.MapColor
 *  net.neoforged.neoforge.registries.DeferredBlock
 *  net.neoforged.neoforge.registries.DeferredItem
 *  net.neoforged.neoforge.registries.DeferredRegister
 *  net.neoforged.neoforge.registries.DeferredRegister$Blocks
 *  org.jetbrains.annotations.Nullable
 */
package appeng.core.definitions;

import appeng.api.ids.AEBlockIds;
import appeng.block.AEBaseBlock;
import appeng.block.AEBaseBlockItem;
import appeng.block.crafting.CraftingBlockItem;
import appeng.block.crafting.CraftingMonitorBlock;
import appeng.block.crafting.CraftingUnitBlock;
import appeng.block.crafting.CraftingUnitType;
import appeng.block.crafting.MolecularAssemblerBlock;
import appeng.block.crafting.PatternProviderBlock;
import appeng.block.misc.CellWorkbenchBlock;
import appeng.block.misc.ChargerBlock;
import appeng.block.misc.CondenserBlock;
import appeng.block.misc.CrankBlock;
import appeng.block.misc.GrowthAcceleratorBlock;
import appeng.block.misc.InscriberBlock;
import appeng.block.misc.InterfaceBlock;
import appeng.block.misc.LightDetectorBlock;
import appeng.block.misc.MysteriousCubeBlock;
import appeng.block.misc.QuartzFixtureBlock;
import appeng.block.misc.TinyTNTBlock;
import appeng.block.misc.VibrationChamberBlock;
import appeng.block.networking.CableBusBlock;
import appeng.block.networking.ControllerBlock;
import appeng.block.networking.CreativeEnergyCellBlock;
import appeng.block.networking.CrystalResonanceGeneratorBlock;
import appeng.block.networking.EnergyAcceptorBlock;
import appeng.block.networking.EnergyCellBlock;
import appeng.block.networking.EnergyCellBlockItem;
import appeng.block.networking.WirelessAccessPointBlock;
import appeng.block.paint.PaintSplotchesBlock;
import appeng.block.qnb.QuantumLinkChamberBlock;
import appeng.block.qnb.QuantumRingBlock;
import appeng.block.spatial.MatrixFrameBlock;
import appeng.block.spatial.SpatialAnchorBlock;
import appeng.block.spatial.SpatialIOPortBlock;
import appeng.block.spatial.SpatialPylonBlock;
import appeng.block.storage.DriveBlock;
import appeng.block.storage.IOPortBlock;
import appeng.block.storage.MEChestBlock;
import appeng.block.storage.SkyChestBlock;
import appeng.block.storage.SkyStoneTankBlock;
import appeng.core.AppEng;
import appeng.core.MainCreativeTab;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.BlockDefinition;
import appeng.core.definitions.ItemDefinition;
import appeng.debug.CubeGeneratorBlock;
import appeng.debug.EnergyGeneratorBlock;
import appeng.debug.ItemGenBlock;
import appeng.debug.PhantomNodeBlock;
import appeng.decorative.AEDecorativeBlock;
import appeng.decorative.solid.BuddingCertusQuartzBlock;
import appeng.decorative.solid.CertusQuartzClusterBlock;
import appeng.decorative.solid.NotSoMysteriousCubeBlock;
import appeng.decorative.solid.QuartzGlassBlock;
import appeng.decorative.solid.QuartzLampBlock;
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.Nullable;

public final class AEBlocks {
    public static final DeferredRegister.Blocks DR = DeferredRegister.createBlocks((String)"ae2");
    private static final List<BlockDefinition<?>> BLOCKS = new ArrayList();
    private static final BlockBehaviour.Properties QUARTZ_CLUSTER_PROPERTIES = AEBaseBlock.defaultProps(MapColor.COLOR_CYAN, SoundType.AMETHYST_CLUSTER).forceSolidOn().strength(1.5f).requiresCorrectToolForDrops();
    private static final BlockBehaviour.Properties QUARTZ_PROPERTIES = AEBaseBlock.stoneProps().mapColor(MapColor.COLOR_CYAN).strength(3.0f, 5.0f).requiresCorrectToolForDrops();
    private static final BlockBehaviour.Properties SKYSTONE_PROPERTIES = AEBaseBlock.stoneProps().mapColor(MapColor.COLOR_BLACK).strength(5.0f, 150.0f).requiresCorrectToolForDrops();
    private static final BlockBehaviour.StateArgumentPredicate<EntityType<?>> NEVER_ALLOW_SPAWN = (p1, p2, p3, p4) -> false;
    private static final BlockBehaviour.Properties SKY_STONE_CHEST_PROPS = AEBaseBlock.stoneProps().mapColor(MapColor.COLOR_BLACK).strength(5.0f, 150.0f).noOcclusion();
    private static final BlockBehaviour.Properties FLUIX_PROPERTIES = AEBaseBlock.stoneProps().mapColor(MapColor.COLOR_PURPLE).strength(3.0f, 5.0f).requiresCorrectToolForDrops();
    public static final BlockDefinition<BuddingCertusQuartzBlock> FLAWLESS_BUDDING_QUARTZ = AEBlocks.block("Flawless Budding Certus Quartz", AEBlockIds.FLAWLESS_BUDDING_QUARTZ, () -> new BuddingCertusQuartzBlock(QUARTZ_PROPERTIES.randomTicks()));
    public static final BlockDefinition<BuddingCertusQuartzBlock> FLAWED_BUDDING_QUARTZ = AEBlocks.block("Flawed Budding Certus Quartz", AEBlockIds.FLAWED_BUDDING_QUARTZ, () -> new BuddingCertusQuartzBlock(QUARTZ_PROPERTIES.randomTicks()));
    public static final BlockDefinition<BuddingCertusQuartzBlock> CHIPPED_BUDDING_QUARTZ = AEBlocks.block("Chipped Budding Certus Quartz", AEBlockIds.CHIPPED_BUDDING_QUARTZ, () -> new BuddingCertusQuartzBlock(QUARTZ_PROPERTIES.randomTicks()));
    public static final BlockDefinition<BuddingCertusQuartzBlock> DAMAGED_BUDDING_QUARTZ = AEBlocks.block("Damaged Budding Certus Quartz", AEBlockIds.DAMAGED_BUDDING_QUARTZ, () -> new BuddingCertusQuartzBlock(QUARTZ_PROPERTIES.randomTicks()));
    public static final BlockDefinition<CertusQuartzClusterBlock> SMALL_QUARTZ_BUD = AEBlocks.block("Small Certus Quartz Bud", AEBlockIds.SMALL_QUARTZ_BUD, () -> new CertusQuartzClusterBlock(3, 4, QUARTZ_CLUSTER_PROPERTIES.sound(SoundType.SMALL_AMETHYST_BUD).lightLevel(s -> 1)));
    public static final BlockDefinition<CertusQuartzClusterBlock> MEDIUM_QUARTZ_BUD = AEBlocks.block("Medium Certus Quartz Bud", AEBlockIds.MEDIUM_QUARTZ_BUD, () -> new CertusQuartzClusterBlock(4, 3, QUARTZ_CLUSTER_PROPERTIES.sound(SoundType.MEDIUM_AMETHYST_BUD).lightLevel(s -> 2)));
    public static final BlockDefinition<CertusQuartzClusterBlock> LARGE_QUARTZ_BUD = AEBlocks.block("Large Certus Quartz Bud", AEBlockIds.LARGE_QUARTZ_BUD, () -> new CertusQuartzClusterBlock(5, 3, QUARTZ_CLUSTER_PROPERTIES.sound(SoundType.LARGE_AMETHYST_BUD).lightLevel(s -> 4)));
    public static final BlockDefinition<CertusQuartzClusterBlock> QUARTZ_CLUSTER = AEBlocks.block("Certus Quartz Cluster", AEBlockIds.QUARTZ_CLUSTER, () -> new CertusQuartzClusterBlock(7, 3, QUARTZ_CLUSTER_PROPERTIES.sound(SoundType.AMETHYST_CLUSTER).lightLevel(s -> 5)));
    public static final BlockDefinition<MatrixFrameBlock> MATRIX_FRAME = AEBlocks.block("Matrix Frame", AEBlockIds.MATRIX_FRAME, MatrixFrameBlock::new);
    public static final BlockDefinition<AEDecorativeBlock> QUARTZ_BLOCK = AEBlocks.block("Certus Quartz Block", AEBlockIds.QUARTZ_BLOCK, () -> new AEDecorativeBlock(QUARTZ_PROPERTIES));
    public static final BlockDefinition<AEDecorativeBlock> CUT_QUARTZ_BLOCK = AEBlocks.block("Cut Certus Quartz Block", AEBlockIds.CUT_QUARTZ_BLOCK, () -> new AEDecorativeBlock(QUARTZ_PROPERTIES));
    public static final BlockDefinition<AEDecorativeBlock> SMOOTH_QUARTZ_BLOCK = AEBlocks.block("Smooth Certus Quartz Block", AEBlockIds.SMOOTH_QUARTZ_BLOCK, () -> new AEDecorativeBlock(QUARTZ_PROPERTIES));
    public static final BlockDefinition<AEDecorativeBlock> QUARTZ_BRICKS = AEBlocks.block("Certus Quartz Bricks", AEBlockIds.QUARTZ_BRICKS, () -> new AEDecorativeBlock(QUARTZ_PROPERTIES));
    public static final BlockDefinition<RotatedPillarBlock> QUARTZ_PILLAR = AEBlocks.block("Certus Quartz Pillar", AEBlockIds.QUARTZ_PILLAR, () -> new RotatedPillarBlock(QUARTZ_PROPERTIES));
    public static final BlockDefinition<AEDecorativeBlock> CHISELED_QUARTZ_BLOCK = AEBlocks.block("Chiseled Certus Quartz Block", AEBlockIds.CHISELED_QUARTZ_BLOCK, () -> new AEDecorativeBlock(QUARTZ_PROPERTIES));
    public static final BlockDefinition<QuartzGlassBlock> QUARTZ_GLASS = AEBlocks.block("Quartz Glass", AEBlockIds.QUARTZ_GLASS, () -> new QuartzGlassBlock(AEBaseBlock.glassProps().noOcclusion().isValidSpawn(NEVER_ALLOW_SPAWN)));
    public static final BlockDefinition<QuartzLampBlock> QUARTZ_VIBRANT_GLASS = AEBlocks.block("Vibrant Quartz Glass", AEBlockIds.QUARTZ_VIBRANT_GLASS, () -> new QuartzLampBlock(AEBaseBlock.glassProps().lightLevel(b -> 15).noOcclusion().isValidSpawn(NEVER_ALLOW_SPAWN)));
    public static final BlockDefinition<QuartzFixtureBlock> QUARTZ_FIXTURE = AEBlocks.block("Charged Quartz Fixture", AEBlockIds.QUARTZ_FIXTURE, QuartzFixtureBlock::new);
    public static final BlockDefinition<AEDecorativeBlock> FLUIX_BLOCK = AEBlocks.block("Fluix Block", AEBlockIds.FLUIX_BLOCK, () -> new AEDecorativeBlock(FLUIX_PROPERTIES));
    public static final BlockDefinition<AEDecorativeBlock> SKY_STONE_BLOCK = AEBlocks.block("Sky Stone", AEBlockIds.SKY_STONE_BLOCK, () -> new AEDecorativeBlock(AEBaseBlock.stoneProps().strength(50.0f, 150.0f).requiresCorrectToolForDrops()));
    public static final BlockDefinition<AEDecorativeBlock> SMOOTH_SKY_STONE_BLOCK = AEBlocks.block("Sky Stone Block", AEBlockIds.SMOOTH_SKY_STONE_BLOCK, () -> new AEDecorativeBlock(SKYSTONE_PROPERTIES));
    public static final BlockDefinition<AEDecorativeBlock> SKY_STONE_BRICK = AEBlocks.block("Sky Stone Brick", AEBlockIds.SKY_STONE_BRICK, () -> new AEDecorativeBlock(SKYSTONE_PROPERTIES));
    public static final BlockDefinition<AEDecorativeBlock> SKY_STONE_SMALL_BRICK = AEBlocks.block("Sky Stone Small Brick", AEBlockIds.SKY_STONE_SMALL_BRICK, () -> new AEDecorativeBlock(SKYSTONE_PROPERTIES));
    public static final BlockDefinition<SkyChestBlock> SKY_STONE_CHEST = AEBlocks.block("Sky Stone Chest", AEBlockIds.SKY_STONE_CHEST, () -> new SkyChestBlock(SkyChestBlock.SkyChestType.STONE, SKY_STONE_CHEST_PROPS));
    public static final BlockDefinition<SkyChestBlock> SMOOTH_SKY_STONE_CHEST = AEBlocks.block("Sky Stone Block Chest", AEBlockIds.SMOOTH_SKY_STONE_CHEST, () -> new SkyChestBlock(SkyChestBlock.SkyChestType.BLOCK, SKY_STONE_CHEST_PROPS));
    public static final BlockDefinition<SkyStoneTankBlock> SKY_STONE_TANK = AEBlocks.block("Sky Stone Tank", AEBlockIds.SKY_STONE_TANK, () -> new SkyStoneTankBlock(SKY_STONE_CHEST_PROPS));
    public static final BlockDefinition<MysteriousCubeBlock> MYSTERIOUS_CUBE = AEBlocks.block("Mysterious Cube", AEBlockIds.MYSTERIOUS_CUBE, MysteriousCubeBlock::new);
    public static final BlockDefinition<NotSoMysteriousCubeBlock> NOT_SO_MYSTERIOUS_CUBE = AEBlocks.block("Not So Mysterious Cube", AEBlockIds.NOT_SO_MYSTERIOUS_CUBE, NotSoMysteriousCubeBlock::new);
    public static final BlockDefinition<InscriberBlock> INSCRIBER = AEBlocks.block("Inscriber", AEBlockIds.INSCRIBER, () -> new InscriberBlock(AEBaseBlock.metalProps().noOcclusion()));
    public static final BlockDefinition<WirelessAccessPointBlock> WIRELESS_ACCESS_POINT = AEBlocks.block("ME Wireless Access Point", AEBlockIds.WIRELESS_ACCESS_POINT, WirelessAccessPointBlock::new);
    public static final BlockDefinition<ChargerBlock> CHARGER = AEBlocks.block("Charger", AEBlockIds.CHARGER, ChargerBlock::new);
    public static final BlockDefinition<TinyTNTBlock> TINY_TNT = AEBlocks.block("Tiny TNT", AEBlockIds.TINY_TNT, () -> new TinyTNTBlock(AEBaseBlock.defaultProps(MapColor.FIRE, SoundType.GRAVEL).strength(0.0f).noOcclusion()));
    public static final BlockDefinition<QuantumRingBlock> QUANTUM_RING = AEBlocks.block("ME Quantum Ring", AEBlockIds.QUANTUM_RING, QuantumRingBlock::new);
    public static final BlockDefinition<QuantumLinkChamberBlock> QUANTUM_LINK = AEBlocks.block("ME Quantum Link Chamber", AEBlockIds.QUANTUM_LINK, QuantumLinkChamberBlock::new);
    public static final BlockDefinition<SpatialPylonBlock> SPATIAL_PYLON = AEBlocks.block("Spatial Pylon", AEBlockIds.SPATIAL_PYLON, SpatialPylonBlock::new);
    public static final BlockDefinition<SpatialIOPortBlock> SPATIAL_IO_PORT = AEBlocks.block("Spatial IO Port", AEBlockIds.SPATIAL_IO_PORT, SpatialIOPortBlock::new);
    public static final BlockDefinition<ControllerBlock> CONTROLLER = AEBlocks.block("ME Controller", AEBlockIds.CONTROLLER, ControllerBlock::new);
    public static final BlockDefinition<DriveBlock> DRIVE = AEBlocks.block("ME Drive", AEBlockIds.DRIVE, DriveBlock::new);
    public static final BlockDefinition<MEChestBlock> ME_CHEST = AEBlocks.block("ME Chest", AEBlockIds.ME_CHEST, MEChestBlock::new);
    public static final BlockDefinition<InterfaceBlock> INTERFACE = AEBlocks.block("ME Interface", AEBlockIds.INTERFACE, InterfaceBlock::new);
    public static final BlockDefinition<CellWorkbenchBlock> CELL_WORKBENCH = AEBlocks.block("Cell Workbench", AEBlockIds.CELL_WORKBENCH, CellWorkbenchBlock::new);
    public static final BlockDefinition<IOPortBlock> IO_PORT = AEBlocks.block("ME IO Port", AEBlockIds.IO_PORT, IOPortBlock::new);
    public static final BlockDefinition<CondenserBlock> CONDENSER = AEBlocks.block("Matter Condenser", AEBlockIds.CONDENSER, CondenserBlock::new);
    public static final BlockDefinition<EnergyAcceptorBlock> ENERGY_ACCEPTOR = AEBlocks.block("Energy Acceptor", AEBlockIds.ENERGY_ACCEPTOR, EnergyAcceptorBlock::new);
    public static final BlockDefinition<CrystalResonanceGeneratorBlock> CRYSTAL_RESONANCE_GENERATOR = AEBlocks.block("Crystal Resonance Generator", AEBlockIds.CRYSTAL_RESONANCE_GENERATOR, CrystalResonanceGeneratorBlock::new);
    public static final BlockDefinition<VibrationChamberBlock> VIBRATION_CHAMBER = AEBlocks.block("Vibration Chamber", AEBlockIds.VIBRATION_CHAMBER, VibrationChamberBlock::new);
    public static final BlockDefinition<GrowthAcceleratorBlock> GROWTH_ACCELERATOR = AEBlocks.block("Growth Accelerator", AEBlockIds.GROWTH_ACCELERATOR, GrowthAcceleratorBlock::new);
    public static final BlockDefinition<EnergyCellBlock> ENERGY_CELL = AEBlocks.block("Energy Cell", AEBlockIds.ENERGY_CELL, () -> new EnergyCellBlock(200000.0, 800.0, 200), EnergyCellBlockItem::new);
    public static final BlockDefinition<EnergyCellBlock> DENSE_ENERGY_CELL = AEBlocks.block("Dense Energy Cell", AEBlockIds.DENSE_ENERGY_CELL, () -> new EnergyCellBlock(1600000.0, 1600.0, 1600), EnergyCellBlockItem::new);
    public static final BlockDefinition<CreativeEnergyCellBlock> CREATIVE_ENERGY_CELL = AEBlocks.block("Creative Energy Cell", AEBlockIds.CREATIVE_ENERGY_CELL, CreativeEnergyCellBlock::new);
    public static final BlockDefinition<CraftingUnitBlock> CRAFTING_UNIT = AEBlocks.block("Crafting Unit", AEBlockIds.CRAFTING_UNIT, () -> new CraftingUnitBlock(CraftingUnitType.UNIT));
    public static final BlockDefinition<CraftingUnitBlock> CRAFTING_ACCELERATOR = AEBlocks.craftingBlock("Crafting Co-Processing Unit", AEBlockIds.CRAFTING_ACCELERATOR, () -> new CraftingUnitBlock(CraftingUnitType.ACCELERATOR));
    public static final BlockDefinition<CraftingUnitBlock> CRAFTING_STORAGE_1K = AEBlocks.craftingBlock("1k Crafting Storage", AEBlockIds.CRAFTING_STORAGE_1K, () -> new CraftingUnitBlock(CraftingUnitType.STORAGE_1K));
    public static final BlockDefinition<CraftingUnitBlock> CRAFTING_STORAGE_4K = AEBlocks.craftingBlock("4k Crafting Storage", AEBlockIds.CRAFTING_STORAGE_4K, () -> new CraftingUnitBlock(CraftingUnitType.STORAGE_4K));
    public static final BlockDefinition<CraftingUnitBlock> CRAFTING_STORAGE_16K = AEBlocks.craftingBlock("16k Crafting Storage", AEBlockIds.CRAFTING_STORAGE_16K, () -> new CraftingUnitBlock(CraftingUnitType.STORAGE_16K));
    public static final BlockDefinition<CraftingUnitBlock> CRAFTING_STORAGE_64K = AEBlocks.craftingBlock("64k Crafting Storage", AEBlockIds.CRAFTING_STORAGE_64K, () -> new CraftingUnitBlock(CraftingUnitType.STORAGE_64K));
    public static final BlockDefinition<CraftingUnitBlock> CRAFTING_STORAGE_256K = AEBlocks.craftingBlock("256k Crafting Storage", AEBlockIds.CRAFTING_STORAGE_256K, () -> new CraftingUnitBlock(CraftingUnitType.STORAGE_256K));
    public static final BlockDefinition<CraftingMonitorBlock> CRAFTING_MONITOR = AEBlocks.craftingBlock("Crafting Monitor", AEBlockIds.CRAFTING_MONITOR, () -> new CraftingMonitorBlock(CraftingUnitType.MONITOR));
    public static final BlockDefinition<PatternProviderBlock> PATTERN_PROVIDER = AEBlocks.block("ME Pattern Provider", AEBlockIds.PATTERN_PROVIDER, PatternProviderBlock::new);
    public static final BlockDefinition<MolecularAssemblerBlock> MOLECULAR_ASSEMBLER = AEBlocks.block("Molecular Assembler", AEBlockIds.MOLECULAR_ASSEMBLER, () -> new MolecularAssemblerBlock(AEBaseBlock.metalProps().noOcclusion()));
    public static final BlockDefinition<LightDetectorBlock> LIGHT_DETECTOR = AEBlocks.block("Light Detecting Fixture", AEBlockIds.LIGHT_DETECTOR, LightDetectorBlock::new);
    public static final BlockDefinition<PaintSplotchesBlock> PAINT = AEBlocks.block("Paint", AEBlockIds.PAINT, PaintSplotchesBlock::new);
    public static final BlockDefinition<StairBlock> SKY_STONE_STAIRS = AEBlocks.block("Sky Stone Stairs", AEBlockIds.SKY_STONE_STAIRS, () -> new StairBlock(SKY_STONE_BLOCK.block().defaultBlockState(), SKYSTONE_PROPERTIES));
    public static final BlockDefinition<StairBlock> SMOOTH_SKY_STONE_STAIRS = AEBlocks.block("Sky Stone Block Stairs", AEBlockIds.SMOOTH_SKY_STONE_STAIRS, () -> new StairBlock(SMOOTH_SKY_STONE_BLOCK.block().defaultBlockState(), SKYSTONE_PROPERTIES));
    public static final BlockDefinition<StairBlock> SKY_STONE_BRICK_STAIRS = AEBlocks.block("Sky Stone Brick Stairs", AEBlockIds.SKY_STONE_BRICK_STAIRS, () -> new StairBlock(SKY_STONE_BRICK.block().defaultBlockState(), SKYSTONE_PROPERTIES));
    public static final BlockDefinition<StairBlock> SKY_STONE_SMALL_BRICK_STAIRS = AEBlocks.block("Sky Stone Small Brick Stairs", AEBlockIds.SKY_STONE_SMALL_BRICK_STAIRS, () -> new StairBlock(SKY_STONE_SMALL_BRICK.block().defaultBlockState(), SKYSTONE_PROPERTIES));
    public static final BlockDefinition<StairBlock> FLUIX_STAIRS = AEBlocks.block("Fluix Stairs", AEBlockIds.FLUIX_STAIRS, () -> new StairBlock(FLUIX_BLOCK.block().defaultBlockState(), FLUIX_PROPERTIES));
    public static final BlockDefinition<StairBlock> QUARTZ_STAIRS = AEBlocks.block("Certus Quartz Stairs", AEBlockIds.QUARTZ_STAIRS, () -> new StairBlock(QUARTZ_BLOCK.block().defaultBlockState(), QUARTZ_PROPERTIES));
    public static final BlockDefinition<StairBlock> CUT_QUARTZ_STAIRS = AEBlocks.block("Cut Certus Quartz Stairs", AEBlockIds.CUT_QUARTZ_STAIRS, () -> new StairBlock(CUT_QUARTZ_BLOCK.block().defaultBlockState(), QUARTZ_PROPERTIES));
    public static final BlockDefinition<StairBlock> SMOOTH_QUARTZ_STAIRS = AEBlocks.block("Smooth Certus Quartz Stairs", AEBlockIds.SMOOTH_QUARTZ_STAIRS, () -> new StairBlock(SMOOTH_QUARTZ_BLOCK.block().defaultBlockState(), QUARTZ_PROPERTIES));
    public static final BlockDefinition<StairBlock> QUARTZ_BRICK_STAIRS = AEBlocks.block("Certus Quartz Brick Stairs", AEBlockIds.QUARTZ_BRICK_STAIRS, () -> new StairBlock(QUARTZ_BRICKS.block().defaultBlockState(), QUARTZ_PROPERTIES));
    public static final BlockDefinition<StairBlock> CHISELED_QUARTZ_STAIRS = AEBlocks.block("Chiseled Certus Quartz Stairs", AEBlockIds.CHISELED_QUARTZ_STAIRS, () -> new StairBlock(CHISELED_QUARTZ_BLOCK.block().defaultBlockState(), QUARTZ_PROPERTIES));
    public static final BlockDefinition<StairBlock> QUARTZ_PILLAR_STAIRS = AEBlocks.block("Certus Quartz Pillar Stairs", AEBlockIds.QUARTZ_PILLAR_STAIRS, () -> new StairBlock(QUARTZ_PILLAR.block().defaultBlockState(), QUARTZ_PROPERTIES));
    public static final BlockDefinition<WallBlock> SKY_STONE_WALL = AEBlocks.block("Sky Stone Wall", AEBlockIds.SKY_STONE_WALL, () -> new WallBlock(SKYSTONE_PROPERTIES));
    public static final BlockDefinition<WallBlock> SMOOTH_SKY_STONE_WALL = AEBlocks.block("Sky Stone Block Wall", AEBlockIds.SMOOTH_SKY_STONE_WALL, () -> new WallBlock(SKYSTONE_PROPERTIES));
    public static final BlockDefinition<WallBlock> SKY_STONE_BRICK_WALL = AEBlocks.block("Sky Stone Brick Wall", AEBlockIds.SKY_STONE_BRICK_WALL, () -> new WallBlock(SKYSTONE_PROPERTIES));
    public static final BlockDefinition<WallBlock> SKY_STONE_SMALL_BRICK_WALL = AEBlocks.block("Sky Stone Small Brick Wall", AEBlockIds.SKY_STONE_SMALL_BRICK_WALL, () -> new WallBlock(SKYSTONE_PROPERTIES));
    public static final BlockDefinition<WallBlock> FLUIX_WALL = AEBlocks.block("Fluix Wall", AEBlockIds.FLUIX_WALL, () -> new WallBlock(FLUIX_PROPERTIES));
    public static final BlockDefinition<WallBlock> QUARTZ_WALL = AEBlocks.block("Certus Quartz Wall", AEBlockIds.QUARTZ_WALL, () -> new WallBlock(QUARTZ_PROPERTIES));
    public static final BlockDefinition<WallBlock> CUT_QUARTZ_WALL = AEBlocks.block("Cut Certus Quartz Wall", AEBlockIds.CUT_QUARTZ_WALL, () -> new WallBlock(QUARTZ_PROPERTIES));
    public static final BlockDefinition<WallBlock> SMOOTH_QUARTZ_WALL = AEBlocks.block("Smooth Certus Quartz Wall", AEBlockIds.SMOOTH_QUARTZ_WALL, () -> new WallBlock(QUARTZ_PROPERTIES));
    public static final BlockDefinition<WallBlock> QUARTZ_BRICK_WALL = AEBlocks.block("Certus Quartz Brick Wall", AEBlockIds.QUARTZ_BRICK_WALL, () -> new WallBlock(QUARTZ_PROPERTIES));
    public static final BlockDefinition<WallBlock> CHISELED_QUARTZ_WALL = AEBlocks.block("Chiseled Certus Quartz Wall", AEBlockIds.CHISELED_QUARTZ_WALL, () -> new WallBlock(QUARTZ_PROPERTIES));
    public static final BlockDefinition<WallBlock> QUARTZ_PILLAR_WALL = AEBlocks.block("Certus Quartz Pillar Wall", AEBlockIds.QUARTZ_PILLAR_WALL, () -> new WallBlock(QUARTZ_PROPERTIES));
    public static final BlockDefinition<CableBusBlock> CABLE_BUS = AEBlocks.block("AE2 Cable and/or Bus", AEBlockIds.CABLE_BUS, CableBusBlock::new);
    public static final BlockDefinition<SlabBlock> SKY_STONE_SLAB = AEBlocks.block("Sky Stone Slab", AEBlockIds.SKY_STONE_SLAB, () -> new SlabBlock(SKYSTONE_PROPERTIES));
    public static final BlockDefinition<SlabBlock> SMOOTH_SKY_STONE_SLAB = AEBlocks.block("Sky Stone Block Slab", AEBlockIds.SMOOTH_SKY_STONE_SLAB, () -> new SlabBlock(SKYSTONE_PROPERTIES));
    public static final BlockDefinition<SlabBlock> SKY_STONE_BRICK_SLAB = AEBlocks.block("Sky Stone Brick Slab", AEBlockIds.SKY_STONE_BRICK_SLAB, () -> new SlabBlock(SKYSTONE_PROPERTIES));
    public static final BlockDefinition<SlabBlock> SKY_STONE_SMALL_BRICK_SLAB = AEBlocks.block("Sky Stone Small Brick Slab", AEBlockIds.SKY_STONE_SMALL_BRICK_SLAB, () -> new SlabBlock(SKYSTONE_PROPERTIES));
    public static final BlockDefinition<SlabBlock> FLUIX_SLAB = AEBlocks.block("Fluix Slab", AEBlockIds.FLUIX_SLAB, () -> new SlabBlock(FLUIX_PROPERTIES));
    public static final BlockDefinition<SlabBlock> QUARTZ_SLAB = AEBlocks.block("Certus Quartz Slab", AEBlockIds.QUARTZ_SLAB, () -> new SlabBlock(QUARTZ_PROPERTIES));
    public static final BlockDefinition<SlabBlock> CUT_QUARTZ_SLAB = AEBlocks.block("Cut Certus Quartz Slab", AEBlockIds.CUT_QUARTZ_SLAB, () -> new SlabBlock(QUARTZ_PROPERTIES));
    public static final BlockDefinition<SlabBlock> SMOOTH_QUARTZ_SLAB = AEBlocks.block("Smooth Certus Quartz Slab", AEBlockIds.SMOOTH_QUARTZ_SLAB, () -> new SlabBlock(QUARTZ_PROPERTIES));
    public static final BlockDefinition<SlabBlock> QUARTZ_BRICK_SLAB = AEBlocks.block("Certus Quartz Brick Slab", AEBlockIds.QUARTZ_BRICK_SLAB, () -> new SlabBlock(QUARTZ_PROPERTIES));
    public static final BlockDefinition<SlabBlock> CHISELED_QUARTZ_SLAB = AEBlocks.block("Chiseled Certus Quartz Slab", AEBlockIds.CHISELED_QUARTZ_SLAB, () -> new SlabBlock(QUARTZ_PROPERTIES));
    public static final BlockDefinition<SlabBlock> QUARTZ_PILLAR_SLAB = AEBlocks.block("Certus Quartz Pillar Slab", AEBlockIds.QUARTZ_PILLAR_SLAB, () -> new SlabBlock(QUARTZ_PROPERTIES));
    public static final BlockDefinition<SpatialAnchorBlock> SPATIAL_ANCHOR = AEBlocks.block("Spatial Anchor", AEBlockIds.SPATIAL_ANCHOR, SpatialAnchorBlock::new);
    public static final BlockDefinition<ItemGenBlock> DEBUG_ITEM_GEN = AEBlocks.block("Dev.ItemGen", AppEng.makeId("debug_item_gen"), ItemGenBlock::new);
    public static final BlockDefinition<PhantomNodeBlock> DEBUG_PHANTOM_NODE = AEBlocks.block("Dev.PhantomNode", AppEng.makeId("debug_phantom_node"), PhantomNodeBlock::new);
    public static final BlockDefinition<CubeGeneratorBlock> DEBUG_CUBE_GEN = AEBlocks.block("Dev.CubeGen", AppEng.makeId("debug_cube_gen"), CubeGeneratorBlock::new);
    public static final BlockDefinition<EnergyGeneratorBlock> DEBUG_ENERGY_GEN = AEBlocks.block("Dev.EnergyGen", AppEng.makeId("debug_energy_gen"), EnergyGeneratorBlock::new);
    public static final BlockDefinition<CrankBlock> CRANK = AEBlocks.block("Wooden Crank", AEBlockIds.CRANK, () -> new CrankBlock(AEBaseBlock.defaultProps(MapColor.WOOD, SoundType.WOOD).isViewBlocking(Blocks::never).noCollission()));

    private static <T extends Block> BlockDefinition<T> craftingBlock(String englishName, ResourceLocation id, Supplier<T> blockSupplier) {
        return AEBlocks.block(englishName, id, blockSupplier, CraftingBlockItem::new);
    }

    public static List<BlockDefinition<?>> getBlocks() {
        return Collections.unmodifiableList(BLOCKS);
    }

    private static <T extends Block> BlockDefinition<T> block(String englishName, ResourceLocation id, Supplier<T> blockSupplier) {
        return AEBlocks.block(englishName, id, blockSupplier, null);
    }

    private static <T extends Block> BlockDefinition<T> block(String englishName, ResourceLocation id, Supplier<T> blockSupplier, @Nullable BiFunction<Block, Item.Properties, BlockItem> itemFactory) {
        Preconditions.checkArgument((boolean)id.getNamespace().equals("ae2"));
        DeferredBlock deferredBlock = DR.register(id.getPath(), blockSupplier);
        DeferredItem deferredItem = AEItems.DR.register(id.getPath(), () -> {
            Block block = (Block)deferredBlock.get();
            Item.Properties itemProperties = new Item.Properties();
            if (itemFactory != null) {
                BlockItem item = (BlockItem)itemFactory.apply(block, itemProperties);
                if (item == null) {
                    throw new IllegalArgumentException("BlockItem factory for " + String.valueOf(id) + " returned null");
                }
                return item;
            }
            if (block instanceof AEBaseBlock) {
                return new AEBaseBlockItem(block, itemProperties);
            }
            return new BlockItem(block, itemProperties);
        });
        ItemDefinition<BlockItem> itemDef = new ItemDefinition<BlockItem>(englishName, deferredItem);
        MainCreativeTab.add(itemDef);
        BlockDefinition definition = new BlockDefinition(englishName, deferredBlock, itemDef);
        BLOCKS.add(definition);
        return definition;
    }
}

