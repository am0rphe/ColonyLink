/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Preconditions
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.entity.BlockEntityTicker
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.entity.BlockEntityType$BlockEntitySupplier
 *  net.minecraft.world.level.block.entity.BlockEntityType$Builder
 *  net.minecraft.world.level.block.state.BlockState
 *  net.neoforged.neoforge.registries.DeferredHolder
 *  net.neoforged.neoforge.registries.DeferredRegister
 */
package appeng.core.definitions;

import appeng.block.AEBaseEntityBlock;
import appeng.blockentity.AEBaseBlockEntity;
import appeng.blockentity.ClientTickingBlockEntity;
import appeng.blockentity.ServerTickingBlockEntity;
import appeng.blockentity.crafting.CraftingBlockEntity;
import appeng.blockentity.crafting.CraftingMonitorBlockEntity;
import appeng.blockentity.crafting.MolecularAssemblerBlockEntity;
import appeng.blockentity.crafting.PatternProviderBlockEntity;
import appeng.blockentity.misc.CellWorkbenchBlockEntity;
import appeng.blockentity.misc.ChargerBlockEntity;
import appeng.blockentity.misc.CondenserBlockEntity;
import appeng.blockentity.misc.CrankBlockEntity;
import appeng.blockentity.misc.GrowthAcceleratorBlockEntity;
import appeng.blockentity.misc.InscriberBlockEntity;
import appeng.blockentity.misc.InterfaceBlockEntity;
import appeng.blockentity.misc.LightDetectorBlockEntity;
import appeng.blockentity.misc.MysteriousCubeBlockEntity;
import appeng.blockentity.misc.PaintSplotchesBlockEntity;
import appeng.blockentity.misc.VibrationChamberBlockEntity;
import appeng.blockentity.networking.CableBusBlockEntity;
import appeng.blockentity.networking.ControllerBlockEntity;
import appeng.blockentity.networking.CreativeEnergyCellBlockEntity;
import appeng.blockentity.networking.CrystalResonanceGeneratorBlockEntity;
import appeng.blockentity.networking.EnergyAcceptorBlockEntity;
import appeng.blockentity.networking.EnergyCellBlockEntity;
import appeng.blockentity.networking.WirelessAccessPointBlockEntity;
import appeng.blockentity.qnb.QuantumBridgeBlockEntity;
import appeng.blockentity.spatial.SpatialAnchorBlockEntity;
import appeng.blockentity.spatial.SpatialIOPortBlockEntity;
import appeng.blockentity.spatial.SpatialPylonBlockEntity;
import appeng.blockentity.storage.DriveBlockEntity;
import appeng.blockentity.storage.IOPortBlockEntity;
import appeng.blockentity.storage.MEChestBlockEntity;
import appeng.blockentity.storage.SkyChestBlockEntity;
import appeng.blockentity.storage.SkyStoneTankBlockEntity;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.BlockDefinition;
import appeng.core.definitions.DeferredBlockEntityType;
import appeng.debug.CubeGeneratorBlockEntity;
import appeng.debug.EnergyGeneratorBlockEntity;
import appeng.debug.ItemGenBlockEntity;
import appeng.debug.PhantomNodeBlockEntity;
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class AEBlockEntities {
    private static final List<DeferredBlockEntityType<?>> BLOCK_ENTITY_TYPES = new ArrayList();
    public static final DeferredRegister<BlockEntityType<?>> DR = DeferredRegister.create((ResourceKey)Registries.BLOCK_ENTITY_TYPE, (String)"ae2");
    public static final DeferredBlockEntityType<InscriberBlockEntity> INSCRIBER = AEBlockEntities.create("inscriber", InscriberBlockEntity.class, InscriberBlockEntity::new, AEBlocks.INSCRIBER);
    public static final DeferredBlockEntityType<WirelessAccessPointBlockEntity> WIRELESS_ACCESS_POINT = AEBlockEntities.create("wireless_access_point", WirelessAccessPointBlockEntity.class, WirelessAccessPointBlockEntity::new, AEBlocks.WIRELESS_ACCESS_POINT);
    public static final DeferredBlockEntityType<ChargerBlockEntity> CHARGER = AEBlockEntities.create("charger", ChargerBlockEntity.class, ChargerBlockEntity::new, AEBlocks.CHARGER);
    public static final DeferredBlockEntityType<QuantumBridgeBlockEntity> QUANTUM_BRIDGE = AEBlockEntities.create("quantum_ring", QuantumBridgeBlockEntity.class, QuantumBridgeBlockEntity::new, AEBlocks.QUANTUM_RING, AEBlocks.QUANTUM_LINK);
    public static final DeferredBlockEntityType<SpatialPylonBlockEntity> SPATIAL_PYLON = AEBlockEntities.create("spatial_pylon", SpatialPylonBlockEntity.class, SpatialPylonBlockEntity::new, AEBlocks.SPATIAL_PYLON);
    public static final DeferredBlockEntityType<SpatialIOPortBlockEntity> SPATIAL_IO_PORT = AEBlockEntities.create("spatial_io_port", SpatialIOPortBlockEntity.class, SpatialIOPortBlockEntity::new, AEBlocks.SPATIAL_IO_PORT);
    public static final DeferredBlockEntityType<SpatialAnchorBlockEntity> SPATIAL_ANCHOR = AEBlockEntities.create("spatial_anchor", SpatialAnchorBlockEntity.class, SpatialAnchorBlockEntity::new, AEBlocks.SPATIAL_ANCHOR);
    public static final DeferredBlockEntityType<CableBusBlockEntity> CABLE_BUS = AEBlockEntities.create("cable_bus", CableBusBlockEntity.class, CableBusBlockEntity::new, AEBlocks.CABLE_BUS);
    public static final DeferredBlockEntityType<ControllerBlockEntity> CONTROLLER = AEBlockEntities.create("controller", ControllerBlockEntity.class, ControllerBlockEntity::new, AEBlocks.CONTROLLER);
    public static final DeferredBlockEntityType<DriveBlockEntity> DRIVE = AEBlockEntities.create("drive", DriveBlockEntity.class, DriveBlockEntity::new, AEBlocks.DRIVE);
    public static final DeferredBlockEntityType<MEChestBlockEntity> ME_CHEST = AEBlockEntities.create("chest", MEChestBlockEntity.class, MEChestBlockEntity::new, AEBlocks.ME_CHEST);
    public static final DeferredBlockEntityType<InterfaceBlockEntity> INTERFACE = AEBlockEntities.create("interface", InterfaceBlockEntity.class, InterfaceBlockEntity::new, AEBlocks.INTERFACE);
    public static final DeferredBlockEntityType<CellWorkbenchBlockEntity> CELL_WORKBENCH = AEBlockEntities.create("cell_workbench", CellWorkbenchBlockEntity.class, CellWorkbenchBlockEntity::new, AEBlocks.CELL_WORKBENCH);
    public static final DeferredBlockEntityType<IOPortBlockEntity> IO_PORT = AEBlockEntities.create("io_port", IOPortBlockEntity.class, IOPortBlockEntity::new, AEBlocks.IO_PORT);
    public static final DeferredBlockEntityType<CondenserBlockEntity> CONDENSER = AEBlockEntities.create("condenser", CondenserBlockEntity.class, CondenserBlockEntity::new, AEBlocks.CONDENSER);
    public static final DeferredBlockEntityType<EnergyAcceptorBlockEntity> ENERGY_ACCEPTOR = AEBlockEntities.create("energy_acceptor", EnergyAcceptorBlockEntity.class, EnergyAcceptorBlockEntity::new, AEBlocks.ENERGY_ACCEPTOR);
    public static final DeferredBlockEntityType<CrystalResonanceGeneratorBlockEntity> CRYSTAL_RESONANCE_GENERATOR = AEBlockEntities.create("crystal_resonance_generator", CrystalResonanceGeneratorBlockEntity.class, CrystalResonanceGeneratorBlockEntity::new, AEBlocks.CRYSTAL_RESONANCE_GENERATOR);
    public static final DeferredBlockEntityType<VibrationChamberBlockEntity> VIBRATION_CHAMBER = AEBlockEntities.create("vibration_chamber", VibrationChamberBlockEntity.class, VibrationChamberBlockEntity::new, AEBlocks.VIBRATION_CHAMBER);
    public static final DeferredBlockEntityType<GrowthAcceleratorBlockEntity> GROWTH_ACCELERATOR = AEBlockEntities.create("growth_accelerator", GrowthAcceleratorBlockEntity.class, GrowthAcceleratorBlockEntity::new, AEBlocks.GROWTH_ACCELERATOR);
    public static final DeferredBlockEntityType<EnergyCellBlockEntity> ENERGY_CELL = AEBlockEntities.create("energy_cell", EnergyCellBlockEntity.class, EnergyCellBlockEntity::new, AEBlocks.ENERGY_CELL);
    public static final DeferredBlockEntityType<EnergyCellBlockEntity> DENSE_ENERGY_CELL = AEBlockEntities.create("dense_energy_cell", EnergyCellBlockEntity.class, EnergyCellBlockEntity::new, AEBlocks.DENSE_ENERGY_CELL);
    public static final DeferredBlockEntityType<CreativeEnergyCellBlockEntity> CREATIVE_ENERGY_CELL = AEBlockEntities.create("creative_energy_cell", CreativeEnergyCellBlockEntity.class, CreativeEnergyCellBlockEntity::new, AEBlocks.CREATIVE_ENERGY_CELL);
    public static final DeferredBlockEntityType<CraftingBlockEntity> CRAFTING_UNIT = AEBlockEntities.create("crafting_unit", CraftingBlockEntity.class, CraftingBlockEntity::new, AEBlocks.CRAFTING_UNIT, AEBlocks.CRAFTING_ACCELERATOR);
    public static final DeferredBlockEntityType<CraftingBlockEntity> CRAFTING_STORAGE = AEBlockEntities.create("crafting_storage", CraftingBlockEntity.class, CraftingBlockEntity::new, AEBlocks.CRAFTING_STORAGE_1K, AEBlocks.CRAFTING_STORAGE_4K, AEBlocks.CRAFTING_STORAGE_16K, AEBlocks.CRAFTING_STORAGE_64K, AEBlocks.CRAFTING_STORAGE_256K);
    public static final DeferredBlockEntityType<CraftingMonitorBlockEntity> CRAFTING_MONITOR = AEBlockEntities.create("crafting_monitor", CraftingMonitorBlockEntity.class, CraftingMonitorBlockEntity::new, AEBlocks.CRAFTING_MONITOR);
    public static final DeferredBlockEntityType<PatternProviderBlockEntity> PATTERN_PROVIDER = AEBlockEntities.create("pattern_provider", PatternProviderBlockEntity.class, PatternProviderBlockEntity::new, AEBlocks.PATTERN_PROVIDER);
    public static final DeferredBlockEntityType<MolecularAssemblerBlockEntity> MOLECULAR_ASSEMBLER = AEBlockEntities.create("molecular_assembler", MolecularAssemblerBlockEntity.class, MolecularAssemblerBlockEntity::new, AEBlocks.MOLECULAR_ASSEMBLER);
    public static final DeferredBlockEntityType<LightDetectorBlockEntity> LIGHT_DETECTOR = AEBlockEntities.create("light_detector", LightDetectorBlockEntity.class, LightDetectorBlockEntity::new, AEBlocks.LIGHT_DETECTOR);
    public static final DeferredBlockEntityType<PaintSplotchesBlockEntity> PAINT = AEBlockEntities.create("paint", PaintSplotchesBlockEntity.class, PaintSplotchesBlockEntity::new, AEBlocks.PAINT);
    public static final DeferredBlockEntityType<SkyChestBlockEntity> SKY_CHEST = AEBlockEntities.create("sky_chest", SkyChestBlockEntity.class, SkyChestBlockEntity::new, AEBlocks.SKY_STONE_CHEST, AEBlocks.SMOOTH_SKY_STONE_CHEST);
    public static final DeferredBlockEntityType<SkyStoneTankBlockEntity> SKY_STONE_TANK = AEBlockEntities.create("sky_tank", SkyStoneTankBlockEntity.class, SkyStoneTankBlockEntity::new, AEBlocks.SKY_STONE_TANK);
    public static final DeferredBlockEntityType<ItemGenBlockEntity> DEBUG_ITEM_GEN = AEBlockEntities.create("debug_item_gen", ItemGenBlockEntity.class, ItemGenBlockEntity::new, AEBlocks.DEBUG_ITEM_GEN);
    public static final DeferredBlockEntityType<PhantomNodeBlockEntity> DEBUG_PHANTOM_NODE = AEBlockEntities.create("debug_phantom_node", PhantomNodeBlockEntity.class, PhantomNodeBlockEntity::new, AEBlocks.DEBUG_PHANTOM_NODE);
    public static final DeferredBlockEntityType<CubeGeneratorBlockEntity> DEBUG_CUBE_GEN = AEBlockEntities.create("debug_cube_gen", CubeGeneratorBlockEntity.class, CubeGeneratorBlockEntity::new, AEBlocks.DEBUG_CUBE_GEN);
    public static final DeferredBlockEntityType<EnergyGeneratorBlockEntity> DEBUG_ENERGY_GEN = AEBlockEntities.create("debug_energy_gen", EnergyGeneratorBlockEntity.class, EnergyGeneratorBlockEntity::new, AEBlocks.DEBUG_ENERGY_GEN);
    public static final DeferredBlockEntityType<CrankBlockEntity> CRANK = AEBlockEntities.create("crank", CrankBlockEntity.class, CrankBlockEntity::new, AEBlocks.CRANK);
    public static final DeferredBlockEntityType<MysteriousCubeBlockEntity> MYSTERIOUS_CUBE = AEBlockEntities.create("mysterious_cube", MysteriousCubeBlockEntity.class, MysteriousCubeBlockEntity::new, AEBlocks.MYSTERIOUS_CUBE);

    private AEBlockEntities() {
    }

    public static <T extends BlockEntity> List<BlockEntityType<? extends T>> getSubclassesOf(Class<T> baseClass) {
        ArrayList<BlockEntityType<T>> result = new ArrayList<BlockEntityType<T>>();
        for (DeferredBlockEntityType<?> type : BLOCK_ENTITY_TYPES) {
            if (!baseClass.isAssignableFrom(type.getBlockEntityClass())) continue;
            result.add(type.get());
        }
        return result;
    }

    public static List<BlockEntityType<?>> getImplementorsOf(Class<?> iface) {
        ArrayList result = new ArrayList();
        for (DeferredBlockEntityType<?> type : BLOCK_ENTITY_TYPES) {
            if (!iface.isAssignableFrom(type.getBlockEntityClass())) continue;
            result.add(type.get());
        }
        return result;
    }

    @SafeVarargs
    private static <T extends AEBaseBlockEntity> DeferredBlockEntityType<T> create(String shortId, Class<T> entityClass, BlockEntityFactory<T> factory, BlockDefinition<? extends AEBaseEntityBlock<?>> ... blockDefinitions) {
        Preconditions.checkArgument((blockDefinitions.length > 0 ? 1 : 0) != 0);
        DeferredHolder deferred = DR.register(shortId, () -> {
            AtomicReference<BlockEntityType> typeHolder = new AtomicReference<BlockEntityType>();
            BlockEntityType.BlockEntitySupplier supplier = (blockPos, blockState) -> factory.create((BlockEntityType)typeHolder.get(), blockPos, blockState);
            Block[] blocks = (AEBaseEntityBlock[])Arrays.stream(blockDefinitions).map(BlockDefinition::block).toArray(AEBaseEntityBlock[]::new);
            BlockEntityType type = BlockEntityType.Builder.of((BlockEntityType.BlockEntitySupplier)supplier, (Block[])blocks).build(null);
            typeHolder.setPlain(type);
            AEBaseBlockEntity.registerBlockEntityItem(type, blockDefinitions[0].asItem());
            BlockEntityTicker serverTicker = null;
            if (ServerTickingBlockEntity.class.isAssignableFrom(entityClass)) {
                serverTicker = (level, pos, state, entity) -> ((ServerTickingBlockEntity)((Object)entity)).serverTick();
            }
            BlockEntityTicker clientTicker = null;
            if (ClientTickingBlockEntity.class.isAssignableFrom(entityClass)) {
                clientTicker = (level, pos, state, entity) -> ((ClientTickingBlockEntity)((Object)entity)).clientTick();
            }
            Block[] blockArray = blocks;
            int n = blockArray.length;
            for (int i = 0; i < n; ++i) {
                Block block;
                Block baseBlock = block = blockArray[i];
                baseBlock.setBlockEntity(entityClass, type, clientTicker, serverTicker);
            }
            return type;
        });
        DeferredBlockEntityType<T> result = new DeferredBlockEntityType<T>(entityClass, deferred);
        BLOCK_ENTITY_TYPES.add(result);
        return result;
    }

    @FunctionalInterface
    static interface BlockEntityFactory<T extends AEBaseBlockEntity> {
        public T create(BlockEntityType<T> var1, BlockPos var2, BlockState var3);
    }
}

