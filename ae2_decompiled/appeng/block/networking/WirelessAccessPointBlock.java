/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.util.StringRepresentable
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.context.BlockPlaceContext
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.LevelAccessor
 *  net.minecraft.world.level.LevelReader
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.SimpleWaterloggedBlock
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.StateDefinition$Builder
 *  net.minecraft.world.level.block.state.properties.BlockStateProperties
 *  net.minecraft.world.level.block.state.properties.BooleanProperty
 *  net.minecraft.world.level.block.state.properties.EnumProperty
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.level.material.Fluid
 *  net.minecraft.world.level.material.FluidState
 *  net.minecraft.world.level.material.Fluids
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.shapes.CollisionContext
 *  net.minecraft.world.phys.shapes.Shapes
 *  net.minecraft.world.phys.shapes.VoxelShape
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package appeng.block.networking;

import appeng.api.orientation.BlockOrientation;
import appeng.api.orientation.IOrientationStrategy;
import appeng.api.orientation.OrientationStrategies;
import appeng.api.orientation.RelativeSide;
import appeng.block.AEBaseEntityBlock;
import appeng.blockentity.networking.WirelessAccessPointBlockEntity;
import appeng.menu.MenuOpener;
import appeng.menu.implementations.WirelessAccessPointMenu;
import appeng.menu.locator.MenuLocators;
import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WirelessAccessPointBlock
extends AEBaseEntityBlock<WirelessAccessPointBlockEntity>
implements SimpleWaterloggedBlock {
    public static final EnumProperty<State> STATE = EnumProperty.create((String)"state", State.class);
    private static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public WirelessAccessPointBlock() {
        super(WirelessAccessPointBlock.glassProps().noOcclusion().forceSolidOn());
        this.registerDefaultState((BlockState)((BlockState)this.defaultBlockState().setValue(STATE, (Comparable)((Object)State.OFF))).setValue((Property)WATERLOGGED, (Comparable)Boolean.valueOf(false)));
    }

    @Override
    protected BlockState updateBlockStateFromBlockEntity(BlockState currentState, WirelessAccessPointBlockEntity be) {
        State teState = State.OFF;
        if (be.isActive()) {
            teState = State.HAS_CHANNEL;
        } else if (be.isPowered()) {
            teState = State.ON;
        }
        return (BlockState)currentState.setValue(STATE, (Comparable)((Object)teState));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(new Property[]{STATE});
        builder.add(new Property[]{WATERLOGGED});
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        WirelessAccessPointBlockEntity be = (WirelessAccessPointBlockEntity)this.getBlockEntity((BlockGetter)level, pos);
        if (be != null) {
            if (!level.isClientSide()) {
                MenuOpener.open(WirelessAccessPointMenu.TYPE, player, MenuLocators.forBlockEntity(be));
            }
            return InteractionResult.sidedSuccess((boolean)level.isClientSide());
        }
        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return this.getVoxelShape(state);
    }

    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return this.getVoxelShape(state);
    }

    @NotNull
    private VoxelShape getVoxelShape(BlockState state) {
        BlockOrientation orientation = this.getOrientation(state);
        Direction forward = orientation.getSide(RelativeSide.FRONT);
        double minX = 0.0;
        double minY = 0.0;
        double minZ = 0.0;
        double maxX = 1.0;
        double maxY = 1.0;
        double maxZ = 1.0;
        switch (forward) {
            case DOWN: {
                minX = 0.1875;
                minZ = 0.1875;
                maxX = 0.8125;
                maxZ = 0.8125;
                maxY = 1.0;
                minY = 0.3125;
                break;
            }
            case EAST: {
                minY = 0.1875;
                minZ = 0.1875;
                maxY = 0.8125;
                maxZ = 0.8125;
                maxX = 0.6875;
                minX = 0.0;
                break;
            }
            case NORTH: {
                minX = 0.1875;
                minY = 0.1875;
                maxX = 0.8125;
                maxY = 0.8125;
                maxZ = 1.0;
                minZ = 0.3125;
                break;
            }
            case SOUTH: {
                minX = 0.1875;
                minY = 0.1875;
                maxX = 0.8125;
                maxY = 0.8125;
                maxZ = 0.6875;
                minZ = 0.0;
                break;
            }
            case UP: {
                minX = 0.1875;
                minZ = 0.1875;
                maxX = 0.8125;
                maxZ = 0.8125;
                maxY = 0.6875;
                minY = 0.0;
                break;
            }
            case WEST: {
                minY = 0.1875;
                minZ = 0.1875;
                maxY = 0.8125;
                maxZ = 0.8125;
                maxX = 1.0;
                minX = 0.3125;
                break;
            }
        }
        return Shapes.create((AABB)new AABB(minX, minY, minZ, maxX, maxY, maxZ));
    }

    public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
        return true;
    }

    @Override
    public IOrientationStrategy getOrientationStrategy() {
        return OrientationStrategies.facing();
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

    public static enum State implements StringRepresentable
    {
        OFF,
        ON,
        HAS_CHANNEL;


        public String getSerializedName() {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }
}

