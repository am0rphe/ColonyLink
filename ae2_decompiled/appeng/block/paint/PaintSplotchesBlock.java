/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.item.CreativeModeTab$ItemDisplayParameters
 *  net.minecraft.world.item.CreativeModeTab$Output
 *  net.minecraft.world.item.context.BlockPlaceContext
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.biome.Biome$Precipitation
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.SoundType
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.StateDefinition$Builder
 *  net.minecraft.world.level.block.state.properties.IntegerProperty
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.level.material.Fluid
 *  net.minecraft.world.level.material.MapColor
 *  net.minecraft.world.phys.shapes.CollisionContext
 *  net.minecraft.world.phys.shapes.Shapes
 *  net.minecraft.world.phys.shapes.VoxelShape
 */
package appeng.block.paint;

import appeng.api.orientation.IOrientationStrategy;
import appeng.api.orientation.OrientationStrategies;
import appeng.block.AEBaseEntityBlock;
import appeng.blockentity.misc.PaintSplotchesBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PaintSplotchesBlock
extends AEBaseEntityBlock<PaintSplotchesBlockEntity> {
    public static final IntegerProperty LIGHT_LEVEL = IntegerProperty.create((String)"light_level", (int)0, (int)2);

    public PaintSplotchesBlock() {
        super(PaintSplotchesBlock.defaultProps(MapColor.NONE, SoundType.WET_GRASS).noOcclusion().air().lightLevel(state -> {
            Integer lightLevel = (Integer)state.getValue((Property)LIGHT_LEVEL);
            return switch (lightLevel) {
                default -> 0;
                case 1 -> 12;
                case 2 -> 15;
            };
        }));
        this.registerDefaultState((BlockState)this.defaultBlockState().setValue((Property)LIGHT_LEVEL, (Comparable)Integer.valueOf(0)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(new Property[]{LIGHT_LEVEL});
    }

    @Override
    public IOrientationStrategy getOrientationStrategy() {
        return OrientationStrategies.facingNoPlayerRotation();
    }

    @Override
    public void addToMainCreativeTab(CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output) {
    }

    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        PaintSplotchesBlockEntity tp = (PaintSplotchesBlockEntity)this.getBlockEntity((BlockGetter)level, pos);
        if (tp != null) {
            tp.neighborChanged();
        }
    }

    public void handlePrecipitation(BlockState state, Level level, BlockPos pos, Biome.Precipitation precipitation) {
        if (!level.isClientSide() && precipitation == Biome.Precipitation.RAIN) {
            level.removeBlock(pos, false);
        }
    }

    public boolean canBeReplaced(BlockState state, BlockPlaceContext useContext) {
        return true;
    }

    public boolean canBeReplaced(BlockState state, Fluid fluid) {
        return true;
    }
}

