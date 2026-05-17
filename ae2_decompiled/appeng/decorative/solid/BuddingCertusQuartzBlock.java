/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Util
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.level.block.AmethystClusterBlock
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.level.material.Fluids
 *  net.minecraft.world.level.material.PushReaction
 */
package appeng.decorative.solid;

import appeng.block.AEBaseBlock;
import appeng.core.definitions.AEBlocks;
import appeng.decorative.solid.CertusQuartzClusterBlock;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.PushReaction;

public class BuddingCertusQuartzBlock
extends AEBaseBlock {
    public static final int GROWTH_CHANCE = 5;
    public static final int DECAY_CHANCE = 12;
    private static final Direction[] DIRECTIONS = Direction.values();

    public BuddingCertusQuartzBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.DESTROY;
    }

    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource randomSource) {
        AEBaseBlock newBlock;
        if (randomSource.nextInt(5) != 0) {
            return;
        }
        Direction direction = (Direction)Util.getRandom((Object[])DIRECTIONS, (RandomSource)randomSource);
        BlockPos targetPos = pos.relative(direction);
        BlockState targetState = level.getBlockState(targetPos);
        CertusQuartzClusterBlock newCluster = null;
        if (BuddingCertusQuartzBlock.canClusterGrowAtState(targetState)) {
            newCluster = AEBlocks.SMALL_QUARTZ_BUD.block();
        } else if (targetState.is((Block)AEBlocks.SMALL_QUARTZ_BUD.block()) && targetState.getValue((Property)AmethystClusterBlock.FACING) == direction) {
            newCluster = AEBlocks.MEDIUM_QUARTZ_BUD.block();
        } else if (targetState.is((Block)AEBlocks.MEDIUM_QUARTZ_BUD.block()) && targetState.getValue((Property)AmethystClusterBlock.FACING) == direction) {
            newCluster = AEBlocks.LARGE_QUARTZ_BUD.block();
        } else if (targetState.is((Block)AEBlocks.LARGE_QUARTZ_BUD.block()) && targetState.getValue((Property)AmethystClusterBlock.FACING) == direction) {
            newCluster = AEBlocks.QUARTZ_CLUSTER.block();
        }
        if (newCluster == null) {
            return;
        }
        BlockState newClusterState = (BlockState)((BlockState)newCluster.defaultBlockState().setValue((Property)AmethystClusterBlock.FACING, (Comparable)direction)).setValue((Property)AmethystClusterBlock.WATERLOGGED, (Comparable)Boolean.valueOf(targetState.getFluidState().getType() == Fluids.WATER));
        level.setBlockAndUpdate(targetPos, newClusterState);
        if (this == AEBlocks.FLAWLESS_BUDDING_QUARTZ.block() || randomSource.nextInt(12) != 0) {
            return;
        }
        if (this == AEBlocks.FLAWED_BUDDING_QUARTZ.block()) {
            newBlock = AEBlocks.CHIPPED_BUDDING_QUARTZ.block();
        } else if (this == AEBlocks.CHIPPED_BUDDING_QUARTZ.block()) {
            newBlock = AEBlocks.DAMAGED_BUDDING_QUARTZ.block();
        } else if (this == AEBlocks.DAMAGED_BUDDING_QUARTZ.block()) {
            newBlock = AEBlocks.QUARTZ_BLOCK.block();
        } else {
            throw new IllegalStateException("Unexpected block: " + String.valueOf(this));
        }
        level.setBlockAndUpdate(pos, newBlock.defaultBlockState());
    }

    public static boolean canClusterGrowAtState(BlockState state) {
        return state.isAir() || state.is(Blocks.WATER) && state.getFluidState().getAmount() == 8;
    }
}

