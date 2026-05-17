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
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.LevelAccessor
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.StateDefinition$Builder
 *  net.minecraft.world.level.block.state.properties.EnumProperty
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.phys.BlockHitResult
 *  org.jetbrains.annotations.Nullable
 */
package appeng.block.networking;

import appeng.block.AEBaseEntityBlock;
import appeng.blockentity.networking.ControllerBlockEntity;
import appeng.core.definitions.AEBlocks;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocators;
import appeng.menu.me.networktool.NetworkStatusMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class ControllerBlock
extends AEBaseEntityBlock<ControllerBlockEntity> {
    public static final EnumProperty<ControllerBlockState> CONTROLLER_STATE = EnumProperty.create((String)"state", ControllerBlockState.class);
    public static final EnumProperty<ControllerRenderType> CONTROLLER_TYPE = EnumProperty.create((String)"type", ControllerRenderType.class);

    public ControllerBlock() {
        super(ControllerBlock.metalProps().strength(6.0f));
        this.registerDefaultState((BlockState)((BlockState)this.defaultBlockState().setValue(CONTROLLER_STATE, (Comparable)((Object)ControllerBlockState.offline))).setValue(CONTROLLER_TYPE, (Comparable)((Object)ControllerRenderType.block)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(new Property[]{CONTROLLER_STATE});
        builder.add(new Property[]{CONTROLLER_TYPE});
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.getControllerType(this.defaultBlockState(), (LevelAccessor)context.getLevel(), context.getClickedPos());
    }

    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos pos, BlockPos facingPos) {
        return this.getControllerType(state, level, pos);
    }

    private BlockState getControllerType(BlockState baseState, LevelAccessor level, BlockPos pos) {
        boolean zz;
        int z;
        int y;
        ControllerRenderType type = ControllerRenderType.block;
        int x = pos.getX();
        boolean xx = ControllerBlock.isController(level, x - 1, y = pos.getY(), z = pos.getZ()) && ControllerBlock.isController(level, x + 1, y, z);
        boolean yy = ControllerBlock.isController(level, x, y - 1, z) && ControllerBlock.isController(level, x, y + 1, z);
        boolean bl = zz = ControllerBlock.isController(level, x, y, z - 1) && ControllerBlock.isController(level, x, y, z + 1);
        if (xx && !yy && !zz) {
            type = ControllerRenderType.column_x;
        } else if (!xx && yy && !zz) {
            type = ControllerRenderType.column_y;
        } else if (!xx && !yy && zz) {
            type = ControllerRenderType.column_z;
        } else if ((xx ? 1 : 0) + (yy ? 1 : 0) + (zz ? 1 : 0) >= 2) {
            int v = (Math.abs(x) + Math.abs(y) + Math.abs(z)) % 2;
            type = v == 0 ? ControllerRenderType.inside_a : ControllerRenderType.inside_b;
        }
        return (BlockState)baseState.setValue(CONTROLLER_TYPE, (Comparable)((Object)type));
    }

    private static boolean isController(LevelAccessor level, int x, int y, int z) {
        return level.getBlockState(new BlockPos(x, y, z)).is((Block)AEBlocks.CONTROLLER.block());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof ControllerBlockEntity) {
            ControllerBlockEntity be = (ControllerBlockEntity)blockEntity;
            if (!level.isClientSide) {
                MenuOpener.open(NetworkStatusMenu.CONTROLLER_TYPE, player, MenuLocators.forBlockEntity(be));
            }
            return InteractionResult.sidedSuccess((boolean)level.isClientSide);
        }
        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    public static enum ControllerBlockState implements StringRepresentable
    {
        offline,
        online,
        conflicted;


        public String getSerializedName() {
            return this.name();
        }
    }

    public static enum ControllerRenderType implements StringRepresentable
    {
        block,
        column_x,
        column_y,
        column_z,
        inside_a,
        inside_b;


        public String getSerializedName() {
            return this.name();
        }
    }
}

