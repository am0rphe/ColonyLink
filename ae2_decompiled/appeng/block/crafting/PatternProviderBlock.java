/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.ItemInteractionResult
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.StateDefinition$Builder
 *  net.minecraft.world.level.block.state.properties.EnumProperty
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.phys.BlockHitResult
 */
package appeng.block.crafting;

import appeng.block.AEBaseEntityBlock;
import appeng.block.crafting.PushDirection;
import appeng.blockentity.crafting.PatternProviderBlockEntity;
import appeng.menu.locator.MenuLocators;
import appeng.util.InteractionUtil;
import appeng.util.Platform;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;

public class PatternProviderBlock
extends AEBaseEntityBlock<PatternProviderBlockEntity> {
    public static final EnumProperty<PushDirection> PUSH_DIRECTION = EnumProperty.create((String)"push_direction", PushDirection.class);

    public PatternProviderBlock() {
        super(PatternProviderBlock.metalProps());
        this.registerDefaultState((BlockState)this.defaultBlockState().setValue(PUSH_DIRECTION, (Comparable)((Object)PushDirection.ALL)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(new Property[]{PUSH_DIRECTION});
    }

    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        PatternProviderBlockEntity be = (PatternProviderBlockEntity)this.getBlockEntity((BlockGetter)level, pos);
        if (be != null) {
            be.getLogic().updateRedstoneState();
        }
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack heldItem, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (InteractionUtil.canWrenchRotate(heldItem)) {
            this.setSide(level, pos, hit.getDirection());
            return ItemInteractionResult.sidedSuccess((boolean)level.isClientSide());
        }
        return super.useItemOn(heldItem, state, level, pos, player, hand, hit);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        PatternProviderBlockEntity be = (PatternProviderBlockEntity)this.getBlockEntity((BlockGetter)level, pos);
        if (be != null) {
            if (!level.isClientSide()) {
                be.openMenu(player, MenuLocators.forBlockEntity(be));
            }
            return InteractionResult.sidedSuccess((boolean)level.isClientSide());
        }
        return InteractionResult.PASS;
    }

    public void setSide(Level level, BlockPos pos, Direction facing) {
        BlockState currentState = level.getBlockState(pos);
        Direction pushSide = ((PushDirection)((Object)currentState.getValue(PUSH_DIRECTION))).getDirection();
        PushDirection newPushDirection = pushSide == facing.getOpposite() ? PushDirection.fromDirection(facing) : (pushSide == facing ? PushDirection.ALL : (pushSide == null ? PushDirection.fromDirection(facing.getOpposite()) : PushDirection.fromDirection(Platform.rotateAround(pushSide, facing))));
        level.setBlockAndUpdate(pos, (BlockState)currentState.setValue(PUSH_DIRECTION, (Comparable)((Object)newPushDirection)));
    }
}

