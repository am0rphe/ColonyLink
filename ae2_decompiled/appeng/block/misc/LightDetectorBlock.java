/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.world.item.context.BlockPlaceContext
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.LevelAccessor
 *  net.minecraft.world.level.LevelReader
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.StateDefinition$Builder
 *  net.minecraft.world.level.block.state.properties.BlockStateProperties
 *  net.minecraft.world.level.block.state.properties.BooleanProperty
 *  net.minecraft.world.level.block.state.properties.DirectionProperty
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.level.material.Fluid
 *  net.minecraft.world.level.material.FluidState
 *  net.minecraft.world.level.material.Fluids
 *  net.minecraft.world.phys.shapes.CollisionContext
 *  net.minecraft.world.phys.shapes.Shapes
 *  net.minecraft.world.phys.shapes.VoxelShape
 */
package appeng.block.misc;

import appeng.block.AEBaseEntityBlock;
import appeng.blockentity.misc.LightDetectorBlockEntity;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class LightDetectorBlock
extends AEBaseEntityBlock<LightDetectorBlockEntity> {
    private static final Map<Direction, VoxelShape> SHAPES = new EnumMap<Direction, VoxelShape>(Direction.class);
    public static final DirectionProperty FACING;
    public static final BooleanProperty ODD;
    public static final BooleanProperty WATERLOGGED;

    public LightDetectorBlock() {
        super(LightDetectorBlock.fixtureProps());
        this.registerDefaultState((BlockState)((BlockState)((BlockState)this.defaultBlockState().setValue((Property)BlockStateProperties.FACING, (Comparable)Direction.UP)).setValue((Property)ODD, (Comparable)Boolean.valueOf(false))).setValue((Property)WATERLOGGED, (Comparable)Boolean.valueOf(false)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(new Property[]{BlockStateProperties.FACING, ODD, WATERLOGGED});
    }

    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction side) {
        if (level instanceof Level && ((LightDetectorBlockEntity)this.getBlockEntity(level, pos)).isExposedToLight()) {
            return ((Level)level).getMaxLocalRawBrightness(pos) - 6;
        }
        return 0;
    }

    public void onNeighborChange(BlockState state, LevelReader level, BlockPos pos, BlockPos neighbor) {
        LightDetectorBlockEntity tld = (LightDetectorBlockEntity)this.getBlockEntity((BlockGetter)level, pos);
        if (tld != null) {
            tld.updateLight();
        }
    }

    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue((Property)FACING));
    }

    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    private void dropTorch(Level level, BlockPos pos) {
        BlockState prev = level.getBlockState(pos);
        level.destroyBlock(pos, true);
        level.sendBlockUpdated(pos, prev, level.getBlockState(pos), 3);
    }

    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = (Direction)state.getValue((Property)FACING);
        BlockPos blockPos = pos.relative(facing.getOpposite());
        return LightDetectorBlock.canSupportCenter((LevelReader)level, (BlockPos)blockPos, (Direction)facing);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction[] adirection;
        BlockState state = super.getStateForPlacement(context);
        BlockPos pos = context.getClickedPos();
        FluidState fluidState = context.getLevel().getFluidState(pos);
        boolean oddPlacement = (pos.getX() + pos.getY() + pos.getZ()) % 2 != 0;
        state = (BlockState)((BlockState)state.setValue((Property)ODD, (Comparable)Boolean.valueOf(oddPlacement))).setValue((Property)WATERLOGGED, (Comparable)Boolean.valueOf(fluidState.getType() == Fluids.WATER));
        Level levelReader = context.getLevel();
        for (Direction direction : adirection = context.getNearestLookingDirections()) {
            BlockState placedState = (BlockState)state.setValue((Property)FACING, (Comparable)direction.getOpposite());
            if (!this.canSurvive(placedState, (LevelReader)levelReader, pos)) continue;
            return placedState;
        }
        return null;
    }

    public FluidState getFluidState(BlockState blockState) {
        return (Boolean)blockState.getValue((Property)WATERLOGGED) != false ? Fluids.WATER.getSource(false) : super.getFluidState(blockState);
    }

    public BlockState updateShape(BlockState state, Direction direction, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        if (((Boolean)state.getValue((Property)WATERLOGGED)).booleanValue()) {
            level.scheduleTick(currentPos, (Fluid)Fluids.WATER, Fluids.WATER.getTickDelay((LevelReader)level));
        }
        if (direction.getOpposite() == state.getValue((Property)FACING) && !state.canSurvive((LevelReader)level, currentPos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return state;
    }

    static {
        for (Direction facing : Direction.values()) {
            double xOff = -0.3 * (double)facing.getStepX();
            double yOff = -0.3 * (double)facing.getStepY();
            double zOff = -0.3 * (double)facing.getStepZ();
            VoxelShape shape = Shapes.create((double)(xOff + 0.3), (double)(yOff + 0.3), (double)(zOff + 0.3), (double)(xOff + 0.7), (double)(yOff + 0.7), (double)(zOff + 0.7));
            SHAPES.put(facing, shape);
        }
        FACING = BlockStateProperties.FACING;
        ODD = BooleanProperty.create((String)"odd");
        WATERLOGGED = BlockStateProperties.WATERLOGGED;
    }
}

