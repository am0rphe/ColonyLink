/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.item.context.BlockPlaceContext
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.LevelAccessor
 *  net.minecraft.world.level.LevelReader
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.SimpleWaterloggedBlock
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.StateDefinition$Builder
 *  net.minecraft.world.level.block.state.properties.BlockStateProperties
 *  net.minecraft.world.level.block.state.properties.BooleanProperty
 *  net.minecraft.world.level.block.state.properties.DirectionProperty
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.level.material.Fluid
 *  net.minecraft.world.level.material.FluidState
 *  net.minecraft.world.level.material.Fluids
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.shapes.CollisionContext
 *  net.minecraft.world.phys.shapes.Shapes
 *  net.minecraft.world.phys.shapes.VoxelShape
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.OnlyIn
 *  org.jetbrains.annotations.Nullable
 */
package appeng.block.misc;

import appeng.api.orientation.BlockOrientation;
import appeng.api.orientation.RelativeSide;
import appeng.block.AEBaseBlock;
import appeng.client.render.effects.ParticleTypes;
import appeng.core.AEConfig;
import appeng.core.AppEngClient;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

public class QuartzFixtureBlock
extends AEBaseBlock
implements SimpleWaterloggedBlock {
    private static final Map<Direction, VoxelShape> SHAPES = new EnumMap<Direction, VoxelShape>(Direction.class);
    public static final DirectionProperty FACING;
    public static final BooleanProperty ODD;
    public static final BooleanProperty WATERLOGGED;

    public QuartzFixtureBlock() {
        super(QuartzFixtureBlock.fixtureProps().lightLevel(b -> 15));
        this.registerDefaultState((BlockState)((BlockState)((BlockState)this.defaultBlockState().setValue((Property)FACING, (Comparable)Direction.UP)).setValue((Property)ODD, (Comparable)Boolean.valueOf(false))).setValue((Property)WATERLOGGED, (Comparable)Boolean.valueOf(false)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(new Property[]{FACING, ODD, WATERLOGGED});
    }

    @Override
    @Nullable
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

    public BlockState updateShape(BlockState state, Direction direction, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        if (((Boolean)state.getValue((Property)WATERLOGGED)).booleanValue()) {
            level.scheduleTick(currentPos, (Fluid)Fluids.WATER, Fluids.WATER.getTickDelay((LevelReader)level));
        }
        if (direction.getOpposite() == state.getValue((Property)FACING) && !state.canSurvive((LevelReader)level, currentPos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return state;
    }

    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction facing = (Direction)state.getValue((Property)FACING);
        return SHAPES.get(facing);
    }

    @OnlyIn(value=Dist.CLIENT)
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource r) {
        if (!AEConfig.instance().isEnableEffects()) {
            return;
        }
        if ((double)r.nextFloat() < 0.98) {
            return;
        }
        BlockOrientation orientation = this.getOrientation(state);
        Direction top = orientation.getSide(RelativeSide.TOP);
        double xOff = -0.3 * (double)top.getStepX();
        double yOff = -0.3 * (double)top.getStepY();
        double zOff = -0.3 * (double)top.getStepZ();
        for (int bolts = 0; bolts < 3; ++bolts) {
            if (!AppEngClient.instance().shouldAddParticles(r)) continue;
            level.addParticle((ParticleOptions)ParticleTypes.LIGHTNING, xOff + 0.5 + (double)pos.getX(), yOff + 0.5 + (double)pos.getY(), zOff + 0.5 + (double)pos.getZ(), 0.0, 0.0, 0.0);
        }
    }

    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = (Direction)state.getValue((Property)FACING);
        BlockPos blockPos = pos.relative(facing.getOpposite());
        return QuartzFixtureBlock.canSupportCenter((LevelReader)level, (BlockPos)blockPos, (Direction)facing);
    }

    public FluidState getFluidState(BlockState blockState) {
        return (Boolean)blockState.getValue((Property)WATERLOGGED) != false ? Fluids.WATER.getSource(false) : super.getFluidState(blockState);
    }

    static {
        for (Direction facing : Direction.values()) {
            double xOff = -0.3 * (double)facing.getStepX();
            double yOff = -0.3 * (double)facing.getStepY();
            double zOff = -0.3 * (double)facing.getStepZ();
            VoxelShape shape = Shapes.create((AABB)new AABB(xOff + 0.3, yOff + 0.3, zOff + 0.3, xOff + 0.7, yOff + 0.7, zOff + 0.7));
            SHAPES.put(facing, shape);
        }
        FACING = BlockStateProperties.FACING;
        ODD = BooleanProperty.create((String)"odd");
        WATERLOGGED = BlockStateProperties.WATERLOGGED;
    }
}

