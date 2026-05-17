/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.context.BlockPlaceContext
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.LevelAccessor
 *  net.minecraft.world.level.LevelReader
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.RenderShape
 *  net.minecraft.world.level.block.SimpleWaterloggedBlock
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.StateDefinition$Builder
 *  net.minecraft.world.level.block.state.properties.BlockStateProperties
 *  net.minecraft.world.level.block.state.properties.BooleanProperty
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.level.material.Fluid
 *  net.minecraft.world.level.material.FluidState
 *  net.minecraft.world.level.material.Fluids
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.shapes.CollisionContext
 *  net.minecraft.world.phys.shapes.Shapes
 *  net.minecraft.world.phys.shapes.VoxelShape
 *  org.jetbrains.annotations.Nullable
 */
package appeng.block.storage;

import appeng.api.orientation.IOrientationStrategy;
import appeng.api.orientation.OrientationStrategies;
import appeng.block.AEBaseEntityBlock;
import appeng.blockentity.storage.SkyChestBlockEntity;
import appeng.core.definitions.AEBlockEntities;
import appeng.menu.MenuOpener;
import appeng.menu.implementations.SkyChestMenu;
import appeng.menu.locator.MenuLocators;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class SkyChestBlock
extends AEBaseEntityBlock<SkyChestBlockEntity>
implements SimpleWaterloggedBlock {
    private static final double AABB_OFFSET_BOTTOM = 0.0;
    private static final double AABB_OFFSET_SIDES = 0.06;
    private static final double AABB_OFFSET_TOP = 0.0625;
    private static final Map<Direction, VoxelShape> SHAPES = new EnumMap<Direction, VoxelShape>(Direction.class);
    private static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public final SkyChestType type;

    public SkyChestBlock(SkyChestType type, BlockBehaviour.Properties props) {
        super(props);
        this.type = type;
        this.registerDefaultState((BlockState)this.defaultBlockState().setValue((Property)WATERLOGGED, (Comparable)Boolean.valueOf(false)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(new Property[]{WATERLOGGED});
    }

    @Override
    public IOrientationStrategy getOrientationStrategy() {
        return OrientationStrategies.horizontalFacing();
    }

    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
        return true;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof SkyChestBlockEntity) {
            SkyChestBlockEntity be = (SkyChestBlockEntity)blockEntity;
            if (!level.isClientSide()) {
                MenuOpener.open(SkyChestMenu.TYPE, player, MenuLocators.forBlockEntity(be));
            }
            return InteractionResult.sidedSuccess((boolean)level.isClientSide());
        }
        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        SkyChestBlockEntity chest = AEBlockEntities.SKY_CHEST.getBlockEntity((BlockGetter)level, pos);
        if (chest != null) {
            chest.recheckOpen();
        }
    }

    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        SkyChestBlockEntity sk = (SkyChestBlockEntity)this.getBlockEntity(level, pos);
        Direction up = sk != null ? sk.getTop() : Direction.UP;
        return SHAPES.get(up);
    }

    private static AABB computeAABB(Direction up) {
        double offsetX = up.getStepX() == 0 ? 0.06 : 0.0;
        double offsetY = up.getStepY() == 0 ? 0.06 : 0.0;
        double offsetZ = up.getStepZ() == 0 ? 0.06 : 0.0;
        double minX = Math.max(0.0, offsetX + (up.getStepX() < 0 ? 0.0 : (double)up.getStepX() * 0.0625));
        double minY = Math.max(0.0, offsetY + (up.getStepY() < 0 ? 0.0625 : (double)up.getStepY() * 0.0));
        double minZ = Math.max(0.0, offsetZ + (up.getStepZ() < 0 ? 0.0 : (double)up.getStepZ() * 0.0625));
        double maxX = Math.min(1.0, 1.0 - offsetX - (up.getStepX() < 0 ? 0.0625 : (double)up.getStepX() * 0.0));
        double maxY = Math.min(1.0, 1.0 - offsetY - (up.getStepY() < 0 ? 0.0 : (double)up.getStepY() * 0.0625));
        double maxZ = Math.min(1.0, 1.0 - offsetZ - (up.getStepZ() < 0 ? 0.0625 : (double)up.getStepZ() * 0.0));
        return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState fluidState = context.getLevel().getFluidState(context.getClickedPos());
        return (BlockState)super.getStateForPlacement(context).setValue((Property)WATERLOGGED, (Comparable)Boolean.valueOf(fluidState.getType() == Fluids.WATER));
    }

    public FluidState getFluidState(BlockState blockState) {
        return (Boolean)blockState.getValue((Property)WATERLOGGED) != false ? Fluids.WATER.getSource(false) : super.getFluidState(blockState);
    }

    public BlockState updateShape(BlockState blockState, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        if (((Boolean)blockState.getValue((Property)WATERLOGGED)).booleanValue()) {
            level.scheduleTick(currentPos, (Fluid)Fluids.WATER, Fluids.WATER.getTickDelay((LevelReader)level));
        }
        return super.updateShape(blockState, facing, facingState, level, currentPos, facingPos);
    }

    static {
        for (Direction up : Direction.values()) {
            AABB aabb = SkyChestBlock.computeAABB(up);
            SHAPES.put(up, Shapes.create((AABB)aabb));
        }
    }

    public static enum SkyChestType {
        STONE,
        BLOCK;

    }
}

