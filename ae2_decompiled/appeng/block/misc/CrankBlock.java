/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.LevelReader
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.RenderShape
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.shapes.CollisionContext
 *  net.minecraft.world.phys.shapes.Shapes
 *  net.minecraft.world.phys.shapes.VoxelShape
 *  net.neoforged.neoforge.common.util.FakePlayer
 */
package appeng.block.misc;

import appeng.api.implementations.blockentities.ICrankable;
import appeng.api.orientation.IOrientationStrategy;
import appeng.api.orientation.OrientationStrategies;
import appeng.api.orientation.RelativeSide;
import appeng.block.AEBaseEntityBlock;
import appeng.blockentity.misc.CrankBlockEntity;
import java.util.Arrays;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.util.FakePlayer;

public class CrankBlock
extends AEBaseEntityBlock<CrankBlockEntity> {
    private static final VoxelShape[] SHAPES = (VoxelShape[])Arrays.stream(Direction.values()).map(CrankBlock::createShape).toArray(VoxelShape[]::new);

    public CrankBlock(BlockBehaviour.Properties props) {
        super(props);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (player instanceof FakePlayer) {
            this.dropCrank(level, pos);
            return InteractionResult.sidedSuccess((boolean)level.isClientSide());
        }
        CrankBlockEntity crank = (CrankBlockEntity)this.getBlockEntity((BlockGetter)level, pos);
        if (crank != null) {
            crank.power();
            return InteractionResult.sidedSuccess((boolean)level.isClientSide());
        }
        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    private void dropCrank(Level level, BlockPos pos) {
        level.destroyBlock(pos, true);
        level.sendBlockUpdated(pos, this.defaultBlockState(), level.getBlockState(pos), 3);
    }

    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        if (this.getAttachedToPos(state, pos).equals((Object)fromPos) && this.getCrankable(state, level, pos) == null) {
            this.dropCrank(level, pos);
        }
    }

    public boolean canSurvive(BlockState state, LevelReader levelReader, BlockPos pos) {
        if (levelReader instanceof Level) {
            Level level = (Level)levelReader;
            return this.getCrankable(state, level, pos) != null;
        }
        return true;
    }

    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction top = this.getOrientationStrategy().getSide(state, RelativeSide.FRONT);
        return SHAPES[top.ordinal()];
    }

    @Override
    public IOrientationStrategy getOrientationStrategy() {
        return OrientationStrategies.facing();
    }

    public ICrankable getCrankable(BlockState state, Level level, BlockPos pos) {
        Direction facing = this.getOrientationStrategy().getFacing(state);
        BlockPos attachedToPos = this.getAttachedToPos(state, pos);
        return ICrankable.get(level, attachedToPos, facing);
    }

    private BlockPos getAttachedToPos(BlockState state, BlockPos pos) {
        Direction attachedToSide = this.getOrientationStrategy().getFacing(state).getOpposite();
        return pos.relative(attachedToSide);
    }

    private static VoxelShape createShape(Direction forward) {
        double xOff = -0.15 * (double)forward.getStepX();
        double yOff = -0.15 * (double)forward.getStepY();
        double zOff = -0.15 * (double)forward.getStepZ();
        return Shapes.create((double)(xOff + 0.15), (double)(yOff + 0.15), (double)(zOff + 0.15), (double)(xOff + 0.85), (double)(yOff + 0.85), (double)(zOff + 0.85));
    }
}

