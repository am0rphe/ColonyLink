/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.LevelReader
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.BlockHitResult
 *  net.neoforged.neoforge.event.entity.player.PlayerInteractEvent$RightClickBlock
 */
package appeng.hooks;

import appeng.api.orientation.BlockOrientation;
import appeng.api.orientation.IOrientationStrategy;
import appeng.api.orientation.RelativeSide;
import appeng.api.util.DimensionalBlockPos;
import appeng.blockentity.AEBaseBlockEntity;
import appeng.util.InteractionUtil;
import appeng.util.Platform;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public final class WrenchHook {
    private static final ThreadLocal<Boolean> IS_DISASSEMBLING = new ThreadLocal();

    private WrenchHook() {
    }

    public static boolean isDisassembling() {
        return Boolean.TRUE.equals(IS_DISASSEMBLING.get());
    }

    public static void onPlayerUseBlockEvent(PlayerInteractEvent.RightClickBlock event) {
        if (event.isCanceled()) {
            return;
        }
        InteractionResult result = WrenchHook.onPlayerUseBlock(event.getEntity(), event.getLevel(), event.getHand(), event.getHitVec());
        if (result != InteractionResult.PASS) {
            event.setCanceled(true);
            event.setCancellationResult(result);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static InteractionResult onPlayerUseBlock(Player player, Level level, InteractionHand hand, BlockHitResult hitResult) {
        BlockPos pos;
        BlockState state;
        IOrientationStrategy strategy;
        if (player.isSpectator() || hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        ItemStack itemStack = player.getItemInHand(hand);
        if (InteractionUtil.isInAlternateUseMode(player) && InteractionUtil.canWrenchDisassemble(itemStack)) {
            BlockEntity be = level.getBlockEntity(hitResult.getBlockPos());
            if (be instanceof AEBaseBlockEntity) {
                AEBaseBlockEntity baseBlockEntity = (AEBaseBlockEntity)be;
                IS_DISASSEMBLING.set(true);
                try {
                    SoundEvent soundType;
                    if (!Platform.hasPermissions(new DimensionalBlockPos(level, hitResult.getBlockPos()), player)) {
                        InteractionResult interactionResult = InteractionResult.FAIL;
                        return interactionResult;
                    }
                    InteractionResult result = baseBlockEntity.disassembleWithWrench(player, level, hitResult, itemStack);
                    if (result.consumesAction()) {
                        soundType = SoundEvents.ITEM_FRAME_REMOVE_ITEM;
                        level.playSound(player, hitResult.getBlockPos(), soundType, SoundSource.BLOCKS, 0.7f, 1.0f);
                    }
                    soundType = result;
                    return soundType;
                }
                finally {
                    IS_DISASSEMBLING.remove();
                }
            }
        } else if (!InteractionUtil.isInAlternateUseMode(player) && InteractionUtil.canWrenchRotate(itemStack) && (strategy = IOrientationStrategy.get(state = level.getBlockState(pos = hitResult.getBlockPos()))).allowsPlayerRotation()) {
            Direction clickedFace = hitResult.getDirection();
            BlockOrientation orientation = BlockOrientation.get(strategy, state);
            BlockState newState = strategy.setOrientation(state, (orientation = orientation.rotateClockwiseAround(clickedFace)).getSide(RelativeSide.FRONT), orientation.getSpin());
            if (newState != state && newState.canSurvive((LevelReader)level, pos)) {
                if (!Platform.hasPermissions(new DimensionalBlockPos(level, hitResult.getBlockPos()), player)) {
                    return InteractionResult.FAIL;
                }
                level.setBlockAndUpdate(pos, newState);
                return InteractionResult.sidedSuccess((boolean)level.isClientSide);
            }
        }
        return InteractionResult.PASS;
    }
}

