/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Direction
 *  net.minecraft.data.PackOutput
 *  net.minecraft.data.models.blockstates.Condition
 *  net.minecraft.data.models.blockstates.MultiPartGenerator
 *  net.minecraft.data.models.blockstates.PropertyDispatch
 *  net.minecraft.data.models.blockstates.Variant
 *  net.minecraft.data.models.blockstates.VariantProperties
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.state.properties.BlockStateProperties
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.neoforged.neoforge.client.model.generators.BlockModelBuilder
 *  net.neoforged.neoforge.client.model.generators.ConfiguredModel
 *  net.neoforged.neoforge.client.model.generators.ItemModelBuilder
 *  net.neoforged.neoforge.client.model.generators.ModelFile
 *  net.neoforged.neoforge.client.model.generators.ModelFile$ExistingModelFile
 *  net.neoforged.neoforge.client.model.generators.MultiPartBlockStateBuilder
 *  net.neoforged.neoforge.client.model.generators.MultiPartBlockStateBuilder$PartBuilder
 *  net.neoforged.neoforge.client.model.generators.VariantBlockStateBuilder
 *  net.neoforged.neoforge.common.data.ExistingFileHelper
 */
package appeng.datagen.providers.models;

import appeng.api.orientation.BlockOrientation;
import appeng.block.crafting.AbstractCraftingUnitBlock;
import appeng.block.crafting.PatternProviderBlock;
import appeng.block.misc.GrowthAcceleratorBlock;
import appeng.block.misc.VibrationChamberBlock;
import appeng.block.networking.EnergyCellBlock;
import appeng.block.networking.WirelessAccessPointBlock;
import appeng.block.spatial.SpatialAnchorBlock;
import appeng.block.spatial.SpatialIOPortBlock;
import appeng.block.storage.IOPortBlock;
import appeng.block.storage.MEChestBlock;
import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.BlockDefinition;
import appeng.datagen.providers.models.AE2BlockStateProvider;
import appeng.init.client.InitItemModelsProperties;
import java.util.ArrayList;
import java.util.function.Function;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.data.models.blockstates.Condition;
import net.minecraft.data.models.blockstates.MultiPartGenerator;
import net.minecraft.data.models.blockstates.PropertyDispatch;
import net.minecraft.data.models.blockstates.Variant;
import net.minecraft.data.models.blockstates.VariantProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.client.model.generators.MultiPartBlockStateBuilder;
import net.neoforged.neoforge.client.model.generators.VariantBlockStateBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class BlockModelProvider
extends AE2BlockStateProvider {
    public BlockModelProvider(PackOutput packOutput, ExistingFileHelper exFileHelper) {
        super(packOutput, "ae2", exFileHelper);
    }

    protected void registerStatesAndModels() {
        this.emptyModel(AEBlocks.MATRIX_FRAME);
        this.builtInModel(AEBlocks.QUARTZ_GLASS, true);
        this.builtInModel(AEBlocks.CABLE_BUS);
        this.builtInModel(AEBlocks.PAINT);
        BlockModelBuilder driveModel = this.builtInBlockModel("drive");
        this.multiVariantGenerator(AEBlocks.DRIVE, Variant.variant().with(VariantProperties.MODEL, (Object)driveModel.getLocation())).with(BlockModelProvider.createFacingSpinDispatch());
        ModelFile.ExistingModelFile charger = this.models().getExistingFile(AppEng.makeId("charger"));
        this.multiVariantGenerator(AEBlocks.CHARGER, Variant.variant().with(VariantProperties.MODEL, (Object)charger.getLocation())).with(BlockModelProvider.createFacingSpinDispatch());
        ModelFile.ExistingModelFile inscriber = this.models().getExistingFile(AppEng.makeId("inscriber"));
        this.multiVariantGenerator(AEBlocks.INSCRIBER, Variant.variant().with(VariantProperties.MODEL, (Object)inscriber.getLocation())).with(BlockModelProvider.createFacingSpinDispatch());
        this.crystalResonanceGenerator();
        this.wirelessAccessPoint();
        this.craftingMonitor();
        this.quartzGrowthAccelerator();
        this.meChest();
        this.patternProvider();
        this.vibrationChamber();
        this.spatialAnchor();
        this.patternProvider();
        this.ioPort();
        this.spatialIoPort();
        this.builtInBlockModel("spatial_pylon");
        this.builtInBlockModel("qnb/qnb_formed");
        this.builtInBlockModel("crafting/unit_formed");
        this.builtInBlockModel("crafting/accelerator_formed");
        this.builtInBlockModel("crafting/1k_storage_formed");
        this.builtInBlockModel("crafting/4k_storage_formed");
        this.builtInBlockModel("crafting/16k_storage_formed");
        this.builtInBlockModel("crafting/64k_storage_formed");
        this.builtInBlockModel("crafting/256k_storage_formed");
        this.simpleBlock(AEBlocks.SPATIAL_PYLON.block(), (ModelFile)this.models().getBuilder(this.modelPath(AEBlocks.SPATIAL_PYLON)));
        this.itemModels().cubeAll(this.modelPath(AEBlocks.SPATIAL_PYLON), AppEng.makeId("block/spatial_pylon/spatial_pylon_item"));
        this.simpleBlockAndItem(AEBlocks.FLAWLESS_BUDDING_QUARTZ);
        this.simpleBlockAndItem(AEBlocks.FLAWED_BUDDING_QUARTZ);
        this.simpleBlockAndItem(AEBlocks.CHIPPED_BUDDING_QUARTZ);
        this.simpleBlockAndItem(AEBlocks.DAMAGED_BUDDING_QUARTZ);
        this.generateQuartzCluster(AEBlocks.SMALL_QUARTZ_BUD);
        this.generateQuartzCluster(AEBlocks.MEDIUM_QUARTZ_BUD);
        this.generateQuartzCluster(AEBlocks.LARGE_QUARTZ_BUD);
        this.generateQuartzCluster(AEBlocks.QUARTZ_CLUSTER);
        this.simpleBlockAndItem(AEBlocks.CONDENSER);
        this.simpleBlockAndItem(AEBlocks.ENERGY_ACCEPTOR);
        this.simpleBlockAndItem(AEBlocks.INTERFACE);
        this.simpleBlockAndItem(AEBlocks.DEBUG_ITEM_GEN, "block/debug/item_gen");
        this.simpleBlockAndItem(AEBlocks.DEBUG_PHANTOM_NODE, "block/debug/phantom_node");
        this.simpleBlockAndItem(AEBlocks.DEBUG_CUBE_GEN, "block/debug/cube_gen");
        this.simpleBlockAndItem(AEBlocks.DEBUG_ENERGY_GEN, "block/debug/energy_gen");
        this.craftingModel(AEBlocks.CRAFTING_ACCELERATOR, "accelerator");
        this.craftingModel(AEBlocks.CRAFTING_UNIT, "unit");
        this.craftingModel(AEBlocks.CRAFTING_STORAGE_1K, "1k_storage");
        this.craftingModel(AEBlocks.CRAFTING_STORAGE_4K, "4k_storage");
        this.craftingModel(AEBlocks.CRAFTING_STORAGE_16K, "16k_storage");
        this.craftingModel(AEBlocks.CRAFTING_STORAGE_64K, "64k_storage");
        this.craftingModel(AEBlocks.CRAFTING_STORAGE_256K, "256k_storage");
        this.simpleBlockAndItem(AEBlocks.CELL_WORKBENCH, (ModelFile)this.models().cubeBottomTop(this.modelPath(AEBlocks.CELL_WORKBENCH), AppEng.makeId("block/cell_workbench_side"), AppEng.makeId("block/generics/bottom"), AppEng.makeId("block/cell_workbench_top")));
        this.energyCell(AEBlocks.ENERGY_CELL, "block/energy_cell");
        this.energyCell(AEBlocks.DENSE_ENERGY_CELL, "block/dense_energy_cell");
        this.simpleBlockAndItem(AEBlocks.CREATIVE_ENERGY_CELL, "block/creative_energy_cell");
        this.simpleBlockAndItem(AEBlocks.MYSTERIOUS_CUBE, (ModelFile)this.models().getExistingFile(AppEng.makeId("block/mysterious_cube")));
        this.simpleBlockAndItem(AEBlocks.NOT_SO_MYSTERIOUS_CUBE, (ModelFile)this.models().getExistingFile(AppEng.makeId("block/mysterious_cube")));
    }

    private void meChest() {
        MultiPartGenerator multipart = this.multiPartGenerator(AEBlocks.ME_CHEST);
        BlockModelProvider.withOrientations(multipart, Variant.variant().with(VariantProperties.MODEL, (Object)AppEng.makeId("block/chest/base")));
        BlockModelProvider.withOrientations(multipart, () -> Condition.condition().term((Property)MEChestBlock.LIGHTS_ON, (Comparable)Boolean.valueOf(false)), Variant.variant().with(VariantProperties.MODEL, (Object)AppEng.makeId("block/chest/lights_off")));
        BlockModelProvider.withOrientations(multipart, () -> Condition.condition().term((Property)MEChestBlock.LIGHTS_ON, (Comparable)Boolean.valueOf(true)), Variant.variant().with(VariantProperties.MODEL, (Object)AppEng.makeId("block/chest/lights_on")));
    }

    private void quartzGrowthAccelerator() {
        BlockModelBuilder unpoweredModel = (BlockModelBuilder)this.models().cubeBottomTop(this.modelPath(AEBlocks.GROWTH_ACCELERATOR), AppEng.makeId("block/growth_accelerator_side"), AppEng.makeId("block/growth_accelerator_bottom"), AppEng.makeId("block/growth_accelerator_top"));
        BlockModelBuilder poweredModel = (BlockModelBuilder)this.models().cubeBottomTop(this.modelPath(AEBlocks.GROWTH_ACCELERATOR) + "_on", AppEng.makeId("block/growth_accelerator_side_on"), AppEng.makeId("block/growth_accelerator_bottom"), AppEng.makeId("block/growth_accelerator_top_on"));
        this.multiVariantGenerator(AEBlocks.GROWTH_ACCELERATOR, new Variant[0]).with(BlockModelProvider.createFacingDispatch(90, 0)).with((PropertyDispatch)PropertyDispatch.property((Property)GrowthAcceleratorBlock.POWERED).select((Comparable)Boolean.valueOf(false), Variant.variant().with(VariantProperties.MODEL, (Object)unpoweredModel.getLocation())).select((Comparable)Boolean.valueOf(true), Variant.variant().with(VariantProperties.MODEL, (Object)poweredModel.getLocation())));
        this.itemModels().withExistingParent(this.modelPath(AEBlocks.GROWTH_ACCELERATOR), unpoweredModel.getLocation());
    }

    private void craftingMonitor() {
        ResourceLocation formedModel = AppEng.makeId("block/crafting/monitor_formed");
        ResourceLocation unformedModel = AppEng.makeId("block/crafting/monitor");
        this.multiVariantGenerator(AEBlocks.CRAFTING_MONITOR, new Variant[0]).with(PropertyDispatch.properties((Property)AbstractCraftingUnitBlock.FORMED, (Property)BlockStateProperties.FACING).generate((formed, facing) -> {
            if (formed.booleanValue()) {
                return Variant.variant().with(VariantProperties.MODEL, (Object)formedModel);
            }
            return BlockModelProvider.applyOrientation(Variant.variant().with(VariantProperties.MODEL, (Object)unformedModel), BlockOrientation.get(facing));
        }));
    }

    private void crystalResonanceGenerator() {
        VariantBlockStateBuilder builder = this.getVariantBuilder(AEBlocks.CRYSTAL_RESONANCE_GENERATOR.block());
        ModelFile.ExistingModelFile modelFile = this.models().getExistingFile(AppEng.makeId("block/crystal_resonance_generator"));
        for (Direction facing : Direction.values()) {
            BlockOrientation rotation = BlockOrientation.get(facing, 0);
            builder.partialState().with((Property)BlockStateProperties.FACING, (Comparable)facing).setModels(ConfiguredModel.builder().modelFile((ModelFile)modelFile).rotationX(rotation.getAngleX() + 90).rotationY(rotation.getAngleY()).build());
        }
        this.simpleBlockItem(AEBlocks.CRYSTAL_RESONANCE_GENERATOR.block(), (ModelFile)modelFile);
    }

    private void wirelessAccessPoint() {
        MultiPartBlockStateBuilder builder = this.getMultipartBuilder(AEBlocks.WIRELESS_ACCESS_POINT.block());
        ModelFile.ExistingModelFile chassis = this.models().getExistingFile(AppEng.makeId("block/wireless_access_point_chassis"));
        ModelFile.ExistingModelFile antennaOff = this.models().getExistingFile(AppEng.makeId("block/wireless_access_point_off"));
        ModelFile.ExistingModelFile antennaOn = this.models().getExistingFile(AppEng.makeId("block/wireless_access_point_on"));
        ModelFile.ExistingModelFile statusOff = this.models().getExistingFile(AppEng.makeId("block/wireless_access_point_status_off"));
        ModelFile.ExistingModelFile statusOn = this.models().getExistingFile(AppEng.makeId("block/wireless_access_point_status_has_channel"));
        for (Direction facing : Direction.values()) {
            BlockOrientation rotation = BlockOrientation.get(facing, 0);
            Function<ModelFile, MultiPartBlockStateBuilder.PartBuilder> addModel = modelFile -> ((MultiPartBlockStateBuilder.PartBuilder)builder.part().modelFile(modelFile).rotationX(rotation.getAngleX()).rotationY(rotation.getAngleY()).addModel()).condition((Property)BlockStateProperties.FACING, (Comparable[])new Direction[]{facing});
            addModel.apply((ModelFile)chassis).end();
            addModel.apply((ModelFile)antennaOff).condition(WirelessAccessPointBlock.STATE, (Comparable[])new WirelessAccessPointBlock.State[]{WirelessAccessPointBlock.State.OFF}).end();
            addModel.apply((ModelFile)statusOff).condition(WirelessAccessPointBlock.STATE, (Comparable[])new WirelessAccessPointBlock.State[]{WirelessAccessPointBlock.State.OFF}).end();
            addModel.apply((ModelFile)antennaOff).condition(WirelessAccessPointBlock.STATE, (Comparable[])new WirelessAccessPointBlock.State[]{WirelessAccessPointBlock.State.ON}).end();
            addModel.apply((ModelFile)statusOn).condition(WirelessAccessPointBlock.STATE, (Comparable[])new WirelessAccessPointBlock.State[]{WirelessAccessPointBlock.State.ON}).end();
            addModel.apply((ModelFile)antennaOn).condition(WirelessAccessPointBlock.STATE, (Comparable[])new WirelessAccessPointBlock.State[]{WirelessAccessPointBlock.State.HAS_CHANNEL}).end();
            addModel.apply((ModelFile)statusOn).condition(WirelessAccessPointBlock.STATE, (Comparable[])new WirelessAccessPointBlock.State[]{WirelessAccessPointBlock.State.HAS_CHANNEL}).end();
        }
    }

    private void vibrationChamber() {
        BlockModelBuilder offModel = (BlockModelBuilder)((BlockModelBuilder)this.models().cube(this.modelPath(AEBlocks.VIBRATION_CHAMBER), AppEng.makeId("block/generics/bottom"), AppEng.makeId("block/vibration_chamber_top"), AppEng.makeId("block/vibration_chamber_front"), AppEng.makeId("block/vibration_chamber_back"), AppEng.makeId("block/vibration_chamber_side"), AppEng.makeId("block/vibration_chamber_side"))).texture("particle", AppEng.makeId("block/vibration_chamber_front"));
        BlockModelBuilder onModel = (BlockModelBuilder)((BlockModelBuilder)this.models().cube(this.modelPath(AEBlocks.VIBRATION_CHAMBER) + "_on", AppEng.makeId("block/generics/bottom"), AppEng.makeId("block/vibration_chamber_top_on"), AppEng.makeId("block/vibration_chamber_front_on"), AppEng.makeId("block/vibration_chamber_back_on"), AppEng.makeId("block/vibration_chamber_side"), AppEng.makeId("block/vibration_chamber_side"))).texture("particle", AppEng.makeId("block/vibration_chamber_front_on"));
        this.multiVariantGenerator(AEBlocks.VIBRATION_CHAMBER, new Variant[0]).with(BlockModelProvider.createFacingSpinDispatch()).with((PropertyDispatch)PropertyDispatch.property((Property)VibrationChamberBlock.ACTIVE).select((Comparable)Boolean.valueOf(false), Variant.variant().with(VariantProperties.MODEL, (Object)offModel.getLocation())).select((Comparable)Boolean.valueOf(true), Variant.variant().with(VariantProperties.MODEL, (Object)onModel.getLocation())));
        this.itemModels().withExistingParent(this.modelPath(AEBlocks.VIBRATION_CHAMBER), offModel.getLocation());
    }

    private void spatialAnchor() {
        ResourceLocation offModel = this.getExistingModel("block/spatial_anchor");
        ResourceLocation onModel = this.getExistingModel("block/spatial_anchor_on");
        this.multiVariantGenerator(AEBlocks.SPATIAL_ANCHOR, new Variant[0]).with(BlockModelProvider.createFacingDispatch(90, 0)).with((PropertyDispatch)PropertyDispatch.property((Property)SpatialAnchorBlock.POWERED).select((Comparable)Boolean.valueOf(false), Variant.variant().with(VariantProperties.MODEL, (Object)offModel)).select((Comparable)Boolean.valueOf(true), Variant.variant().with(VariantProperties.MODEL, (Object)onModel)));
        this.itemModels().withExistingParent(this.modelPath(AEBlocks.SPATIAL_ANCHOR), offModel);
    }

    private void patternProvider() {
        BlockDefinition<PatternProviderBlock> def = AEBlocks.PATTERN_PROVIDER;
        ModelFile normalModel = this.cubeAll(def.block());
        this.simpleBlockItem(def.block(), normalModel);
        ModelFile.ExistingModelFile orientedModel = this.models().getExistingFile(AppEng.makeId("block/pattern_provider_oriented"));
        this.multiVariantGenerator(AEBlocks.PATTERN_PROVIDER, Variant.variant()).with(PropertyDispatch.property(PatternProviderBlock.PUSH_DIRECTION).generate(pushDirection -> {
            Direction forward = pushDirection.getDirection();
            if (forward == null) {
                return Variant.variant().with(VariantProperties.MODEL, (Object)normalModel.getLocation());
            }
            BlockOrientation orientation = BlockOrientation.get(forward);
            return BlockModelProvider.applyRotation(Variant.variant().with(VariantProperties.MODEL, (Object)orientedModel.getLocation()), orientation.getAngleX() + 90, orientation.getAngleY(), 0);
        }));
    }

    private void ioPort() {
        ModelFile.ExistingModelFile offModel = this.models().getExistingFile(AppEng.makeId("block/io_port"));
        ModelFile.ExistingModelFile onModel = this.models().getExistingFile(AppEng.makeId("block/io_port_on"));
        this.multiVariantGenerator(AEBlocks.IO_PORT, new Variant[0]).with(BlockModelProvider.createFacingSpinDispatch()).with((PropertyDispatch)PropertyDispatch.property((Property)IOPortBlock.POWERED).select((Comparable)Boolean.valueOf(false), Variant.variant().with(VariantProperties.MODEL, (Object)offModel.getLocation())).select((Comparable)Boolean.valueOf(true), Variant.variant().with(VariantProperties.MODEL, (Object)onModel.getLocation())));
        this.itemModels().withExistingParent(this.modelPath(AEBlocks.IO_PORT), offModel.getLocation());
    }

    private void spatialIoPort() {
        ModelFile.ExistingModelFile offModel = this.models().getExistingFile(AppEng.makeId("block/spatial_io_port"));
        ModelFile.ExistingModelFile onModel = this.models().getExistingFile(AppEng.makeId("block/spatial_io_port_on"));
        this.multiVariantGenerator(AEBlocks.SPATIAL_IO_PORT, new Variant[0]).with(BlockModelProvider.createFacingSpinDispatch()).with((PropertyDispatch)PropertyDispatch.property((Property)SpatialIOPortBlock.POWERED).select((Comparable)Boolean.valueOf(false), Variant.variant().with(VariantProperties.MODEL, (Object)offModel.getLocation())).select((Comparable)Boolean.valueOf(true), Variant.variant().with(VariantProperties.MODEL, (Object)onModel.getLocation())));
        this.itemModels().withExistingParent(this.modelPath(AEBlocks.SPATIAL_IO_PORT), offModel.getLocation());
    }

    private String modelPath(BlockDefinition<?> block) {
        return block.id().getPath();
    }

    private void emptyModel(BlockDefinition<?> block) {
        BlockModelBuilder model = (BlockModelBuilder)this.models().getBuilder(block.id().getPath());
        this.simpleBlockAndItem(block, (ModelFile)model);
    }

    private void builtInModel(BlockDefinition<?> block) {
        this.builtInModel(block, false);
    }

    private void builtInModel(BlockDefinition<?> block, boolean skipItem) {
        BlockModelBuilder model = this.builtInBlockModel(block.id().getPath());
        this.getVariantBuilder((Block)block.block()).partialState().setModels(new ConfiguredModel[]{new ConfiguredModel((ModelFile)model)});
        if (!skipItem) {
            this.itemModels().getBuilder(block.id().getPath());
        }
    }

    private BlockModelBuilder builtInBlockModel(String name) {
        return (BlockModelBuilder)this.models().getBuilder("block/" + name);
    }

    private void energyCell(BlockDefinition<?> block, String baseTexture) {
        VariantBlockStateBuilder blockBuilder = this.getVariantBuilder((Block)block.block());
        ArrayList<BlockModelBuilder> models = new ArrayList<BlockModelBuilder>();
        for (int i = 0; i < 5; ++i) {
            BlockModelBuilder model = (BlockModelBuilder)this.models().cubeAll(this.modelPath(block) + "_" + i, AppEng.makeId(baseTexture + "_" + i));
            blockBuilder.partialState().with((Property)EnergyCellBlock.ENERGY_STORAGE, (Comparable)Integer.valueOf(i)).setModels(new ConfiguredModel[]{new ConfiguredModel((ModelFile)model)});
            models.add(model);
        }
        ItemModelBuilder item = (ItemModelBuilder)this.itemModels().withExistingParent(this.modelPath(block), ((ModelFile)models.get(0)).getLocation());
        for (int i = 1; i < models.size(); ++i) {
            float fillFactor = (float)i / (float)models.size();
            item.override().predicate(InitItemModelsProperties.ENERGY_FILL_LEVEL_ID, fillFactor).model((ModelFile)models.get(i));
        }
    }

    private void craftingModel(BlockDefinition<?> block, String name) {
        BlockModelBuilder blockModel = (BlockModelBuilder)this.models().cubeAll("block/crafting/" + name, AppEng.makeId("block/crafting/" + name));
        this.getVariantBuilder((Block)block.block()).partialState().with((Property)AbstractCraftingUnitBlock.FORMED, (Comparable)Boolean.valueOf(false)).setModels(new ConfiguredModel[]{new ConfiguredModel((ModelFile)blockModel)}).partialState().with((Property)AbstractCraftingUnitBlock.FORMED, (Comparable)Boolean.valueOf(true)).setModels(new ConfiguredModel[]{new ConfiguredModel((ModelFile)this.models().getBuilder("block/crafting/" + name + "_formed"))});
        this.simpleBlockItem((Block)block.block(), (ModelFile)blockModel);
    }

    private void generateQuartzCluster(BlockDefinition<?> quartz) {
        String name = quartz.id().getPath();
        ResourceLocation texture = AppEng.makeId("block/" + name);
        BlockModelBuilder model = (BlockModelBuilder)((BlockModelBuilder)this.models().cross(name, texture)).renderType("cutout");
        this.directionalBlock((Block)quartz.block(), (ModelFile)model);
        ((ItemModelBuilder)this.itemModels().withExistingParent(name, this.mcLoc("item/generated"))).texture("layer0", texture);
    }

    private ResourceLocation getExistingModel(String name) {
        return this.models().getExistingFile(AppEng.makeId(name)).getLocation();
    }
}

