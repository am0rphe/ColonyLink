/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonPrimitive
 *  net.minecraft.core.Direction
 *  net.minecraft.data.PackOutput
 *  net.minecraft.data.models.blockstates.Condition
 *  net.minecraft.data.models.blockstates.Condition$TerminalCondition
 *  net.minecraft.data.models.blockstates.MultiPartGenerator
 *  net.minecraft.data.models.blockstates.MultiVariantGenerator
 *  net.minecraft.data.models.blockstates.PropertyDispatch
 *  net.minecraft.data.models.blockstates.Variant
 *  net.minecraft.data.models.blockstates.VariantProperties
 *  net.minecraft.data.models.blockstates.VariantProperties$Rotation
 *  net.minecraft.data.models.blockstates.VariantProperty
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.SlabBlock
 *  net.minecraft.world.level.block.StairBlock
 *  net.minecraft.world.level.block.WallBlock
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.BlockStateProperties
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.neoforged.neoforge.client.model.generators.BlockModelBuilder
 *  net.neoforged.neoforge.client.model.generators.BlockStateProvider
 *  net.neoforged.neoforge.client.model.generators.ModelBuilder
 *  net.neoforged.neoforge.client.model.generators.ModelFile
 *  net.neoforged.neoforge.common.data.ExistingFileHelper
 */
package appeng.datagen.providers.models;

import appeng.api.orientation.BlockOrientation;
import appeng.api.orientation.IOrientationStrategy;
import appeng.core.AppEng;
import appeng.core.definitions.BlockDefinition;
import appeng.datagen.providers.IAE2DataProvider;
import appeng.datagen.providers.models.VariantsBuilder;
import com.google.gson.JsonPrimitive;
import java.util.function.Supplier;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.data.models.blockstates.Condition;
import net.minecraft.data.models.blockstates.MultiPartGenerator;
import net.minecraft.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.data.models.blockstates.PropertyDispatch;
import net.minecraft.data.models.blockstates.Variant;
import net.minecraft.data.models.blockstates.VariantProperties;
import net.minecraft.data.models.blockstates.VariantProperty;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelBuilder;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public abstract class AE2BlockStateProvider
extends BlockStateProvider
implements IAE2DataProvider {
    private static final VariantProperty<VariantProperties.Rotation> Z_ROT = new VariantProperty("ae2:z", r -> new JsonPrimitive((Number)(r.ordinal() * 90)));

    public AE2BlockStateProvider(PackOutput packOutput, String modid, ExistingFileHelper exFileHelper) {
        super(packOutput, modid, exFileHelper);
    }

    protected void simpleBlockAndItem(BlockDefinition<?> block) {
        ModelFile model = this.cubeAll((Block)block.block());
        this.simpleBlock((Block)block.block(), model);
        this.simpleBlockItem((Block)block.block(), model);
    }

    protected void simpleBlockAndItem(BlockDefinition<?> block, ModelFile model) {
        this.simpleBlock((Block)block.block(), model);
        this.simpleBlockItem((Block)block.block(), model);
    }

    protected void simpleBlockAndItem(BlockDefinition<?> block, String textureName) {
        BlockModelBuilder model = (BlockModelBuilder)this.models().cubeAll(block.id().getPath(), AppEng.makeId(textureName));
        this.simpleBlock((Block)block.block(), (ModelFile)model);
        this.simpleBlockItem((Block)block.block(), (ModelFile)model);
    }

    protected void wall(BlockDefinition<WallBlock> block, String texture) {
        this.wallBlock(block.block(), AppEng.makeId(texture));
        this.itemModels().wallInventory(block.id().getPath(), AppEng.makeId(texture));
    }

    protected void slabBlock(BlockDefinition<SlabBlock> slab, BlockDefinition<?> base) {
        String texture = this.blockTexture((Block)base.block()).getPath();
        this.slabBlock(slab, base, texture, texture, texture);
    }

    protected void slabBlock(BlockDefinition<SlabBlock> slab, BlockDefinition<?> base, String bottomTexture, String sideTexture, String topTexture) {
        ResourceLocation side = AppEng.makeId(sideTexture);
        ResourceLocation bottom = AppEng.makeId(bottomTexture);
        ResourceLocation top = AppEng.makeId(topTexture);
        BlockModelBuilder bottomModel = (BlockModelBuilder)this.models().slab(slab.id().getPath(), side, bottom, top);
        this.simpleBlockItem((Block)slab.block(), (ModelFile)bottomModel);
        this.slabBlock(slab.block(), (ModelFile)bottomModel, (ModelFile)this.models().slabTop(slab.id().getPath() + "_top", side, bottom, top), (ModelFile)this.models().getExistingFile(base.id()));
    }

    protected void stairsBlock(BlockDefinition<StairBlock> stairs, BlockDefinition<?> base) {
        String texture = "block/" + base.id().getPath();
        this.stairsBlock(stairs, texture, texture, texture);
    }

    protected void stairsBlock(BlockDefinition<StairBlock> stairs, String bottomTexture, String sideTexture, String topTexture) {
        String baseName = stairs.id().getPath();
        ResourceLocation side = AppEng.makeId(sideTexture);
        ResourceLocation bottom = AppEng.makeId(bottomTexture);
        ResourceLocation top = AppEng.makeId(topTexture);
        ModelBuilder stairsModel = this.models().stairs(baseName, side, bottom, top);
        ModelBuilder stairsInner = this.models().stairsInner(baseName + "_inner", side, bottom, top);
        ModelBuilder stairsOuter = this.models().stairsOuter(baseName + "_outer", side, bottom, top);
        this.stairsBlock(stairs.block(), (ModelFile)stairsModel, (ModelFile)stairsInner, (ModelFile)stairsOuter);
        this.simpleBlockItem((Block)stairs.block(), (ModelFile)stairsModel);
    }

    protected VariantsBuilder rotatedVariants(BlockDefinition<?> blockDef) {
        Object block = blockDef.block();
        VariantsBuilder builder = new VariantsBuilder((Block)block);
        this.registeredBlocks.put(block, builder);
        return builder;
    }

    protected final MultiVariantGenerator multiVariantGenerator(BlockDefinition<?> blockDef, Variant ... variants) {
        if (variants.length == 0) {
            variants = new Variant[]{Variant.variant()};
        }
        MultiVariantGenerator builder = MultiVariantGenerator.multiVariant(blockDef.block(), (Variant[])variants);
        this.registeredBlocks.put(blockDef.block(), () -> builder.get().getAsJsonObject());
        return builder;
    }

    protected static PropertyDispatch createFacingDispatch(int baseRotX, int baseRotY) {
        return PropertyDispatch.property((Property)BlockStateProperties.FACING).select((Comparable)Direction.DOWN, AE2BlockStateProvider.applyRotation(Variant.variant(), baseRotX + 90, baseRotY, 0)).select((Comparable)Direction.UP, AE2BlockStateProvider.applyRotation(Variant.variant(), baseRotX + 270, baseRotY, 0)).select((Comparable)Direction.NORTH, AE2BlockStateProvider.applyRotation(Variant.variant(), baseRotX, baseRotY, 0)).select((Comparable)Direction.SOUTH, AE2BlockStateProvider.applyRotation(Variant.variant(), baseRotX, baseRotY + 180, 0)).select((Comparable)Direction.WEST, AE2BlockStateProvider.applyRotation(Variant.variant(), baseRotX, baseRotY + 270, 0)).select((Comparable)Direction.EAST, AE2BlockStateProvider.applyRotation(Variant.variant(), baseRotX, baseRotY + 90, 0));
    }

    protected static PropertyDispatch createFacingSpinDispatch(int baseRotX, int baseRotY) {
        return PropertyDispatch.properties((Property)BlockStateProperties.FACING, (Property)IOrientationStrategy.SPIN).generate((facing, spin) -> {
            BlockOrientation orientation = BlockOrientation.get(facing, spin);
            return AE2BlockStateProvider.applyRotation(Variant.variant(), orientation.getAngleX() + baseRotX, orientation.getAngleY() + baseRotY, orientation.getAngleZ());
        });
    }

    protected static PropertyDispatch createFacingSpinDispatch() {
        return AE2BlockStateProvider.createFacingSpinDispatch(0, 0);
    }

    protected static void withOrientations(MultiPartGenerator multipart, Variant baseVariant) {
        AE2BlockStateProvider.withOrientations(multipart, Condition::condition, baseVariant);
    }

    protected static void withOrientations(MultiPartGenerator multipart, Supplier<Condition.TerminalCondition> baseCondition, Variant baseVariant) {
        BlockState defaultState = multipart.getBlock().defaultBlockState();
        IOrientationStrategy strategy = IOrientationStrategy.get(defaultState);
        strategy.getAllStates(defaultState).forEach(blockState -> {
            Condition.TerminalCondition condition = (Condition.TerminalCondition)baseCondition.get();
            for (Property<?> property : strategy.getProperties()) {
                AE2BlockStateProvider.addConditionTerm(condition, blockState, property);
            }
            BlockOrientation orientation = BlockOrientation.get(strategy, blockState);
            Variant variant = Variant.merge((Variant)baseVariant, (Variant)baseVariant);
            multipart.with((Condition)condition, AE2BlockStateProvider.applyOrientation(variant, orientation));
        });
    }

    protected static Variant applyOrientation(Variant variant, BlockOrientation orientation) {
        return AE2BlockStateProvider.applyRotation(variant, orientation.getAngleX(), orientation.getAngleY(), orientation.getAngleZ());
    }

    protected static Variant applyRotation(Variant variant, int angleX, int angleY, int angleZ) {
        angleX = AE2BlockStateProvider.normalizeAngle(angleX);
        angleY = AE2BlockStateProvider.normalizeAngle(angleY);
        angleZ = AE2BlockStateProvider.normalizeAngle(angleZ);
        if (angleX != 0) {
            variant = variant.with(VariantProperties.X_ROT, (Object)AE2BlockStateProvider.rotationByAngle(angleX));
        }
        if (angleY != 0) {
            variant = variant.with(VariantProperties.Y_ROT, (Object)AE2BlockStateProvider.rotationByAngle(angleY));
        }
        if (angleZ != 0) {
            variant = variant.with(Z_ROT, (Object)AE2BlockStateProvider.rotationByAngle(angleZ));
        }
        return variant;
    }

    private static int normalizeAngle(int angle) {
        return angle - angle / 360 * 360;
    }

    private static VariantProperties.Rotation rotationByAngle(int angle) {
        return switch (angle) {
            case 0 -> VariantProperties.Rotation.R0;
            case 90 -> VariantProperties.Rotation.R90;
            case 180 -> VariantProperties.Rotation.R180;
            case 270 -> VariantProperties.Rotation.R270;
            default -> throw new IllegalArgumentException("Invalid angle: " + angle);
        };
    }

    protected final MultiPartGenerator multiPartGenerator(BlockDefinition<?> blockDef) {
        MultiPartGenerator multipart = MultiPartGenerator.multiPart(blockDef.block());
        this.registeredBlocks.put(blockDef.block(), () -> multipart.get().getAsJsonObject());
        return multipart;
    }

    private static <T extends Comparable<T>> Condition addConditionTerm(Condition.TerminalCondition condition, BlockState blockState, Property<T> property) {
        return condition.term(property, blockState.getValue(property));
    }

    public String getName() {
        return super.getName() + " " + this.getClass().getName();
    }
}

