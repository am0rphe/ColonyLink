/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.network.chat.Component
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.ItemInteractionResult
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.LevelAccessor
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.StateDefinition$Builder
 *  net.minecraft.world.level.block.state.properties.BlockStateProperties
 *  net.minecraft.world.level.block.state.properties.BooleanProperty
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.phys.BlockHitResult
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package appeng.block.crafting;

import appeng.block.AEBaseEntityBlock;
import appeng.block.crafting.CraftingUnitType;
import appeng.block.crafting.ICraftingUnitType;
import appeng.blockentity.crafting.CraftingBlockEntity;
import appeng.core.definitions.AEBlocks;
import appeng.core.localization.PlayerMessages;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocators;
import appeng.menu.me.crafting.CraftingCPUMenu;
import appeng.recipes.game.CraftingUnitTransformRecipe;
import appeng.util.InteractionUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractCraftingUnitBlock<T extends CraftingBlockEntity>
extends AEBaseEntityBlock<T> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractCraftingUnitBlock.class);
    public static final BooleanProperty FORMED = BooleanProperty.create((String)"formed");
    public static final BooleanProperty POWERED = BooleanProperty.create((String)"powered");
    public final ICraftingUnitType type;

    public AbstractCraftingUnitBlock(BlockBehaviour.Properties props, ICraftingUnitType type) {
        super(props);
        this.type = type;
        this.registerDefaultState((BlockState)((BlockState)this.defaultBlockState().setValue((Property)FORMED, (Comparable)Boolean.valueOf(false))).setValue((Property)POWERED, (Comparable)Boolean.valueOf(false)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(new Property[]{POWERED});
        builder.add(new Property[]{FORMED});
    }

    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        BlockEntity te = level.getBlockEntity(currentPos);
        if (te != null) {
            te.requestModelDataUpdate();
        }
        return super.updateShape(stateIn, facing, facingState, level, currentPos, facingPos);
    }

    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        CraftingBlockEntity cp = (CraftingBlockEntity)this.getBlockEntity((BlockGetter)level, pos);
        if (cp != null) {
            cp.updateMultiBlock(fromPos);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (newState.getBlock() == state.getBlock()) {
            return;
        }
        CraftingBlockEntity cp = (CraftingBlockEntity)this.getBlockEntity((BlockGetter)level, pos);
        if (cp != null) {
            cp.breakCluster();
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        CraftingBlockEntity be;
        InteractionResult result;
        if (InteractionUtil.isInAlternateUseMode(player) && (result = this.removeUpgrade(level, player, pos, AEBlocks.CRAFTING_UNIT.block().defaultBlockState())) != InteractionResult.FAIL) {
            return result;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof CraftingBlockEntity && (be = (CraftingBlockEntity)blockEntity).isFormed() && be.isActive()) {
            if (!level.isClientSide()) {
                MenuOpener.open(CraftingCPUMenu.TYPE, player, MenuLocators.forBlockEntity(be));
            }
            return InteractionResult.sidedSuccess((boolean)level.isClientSide());
        }
        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack heldItem, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (this.upgrade(heldItem, state, level, pos, player, hit)) {
            return ItemInteractionResult.sidedSuccess((boolean)level.isClientSide());
        }
        return super.useItemOn(heldItem, state, level, pos, player, hand, hit);
    }

    public boolean upgrade(ItemStack heldItem, BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        InteractionResult result;
        if (heldItem.isEmpty()) {
            return false;
        }
        Block upgradedBlock = CraftingUnitTransformRecipe.getUpgradedBlock(level, heldItem);
        if (upgradedBlock == null) {
            return false;
        }
        if (!(upgradedBlock instanceof AbstractCraftingUnitBlock)) {
            LOG.warn("Upgraded block for crafting unit upgrade with {} is not a crafting block: {}", (Object)heldItem, (Object)upgradedBlock);
            return false;
        }
        if (upgradedBlock == state.getBlock()) {
            return false;
        }
        if (level.isClientSide()) {
            return true;
        }
        BlockState newState = upgradedBlock.defaultBlockState();
        newState = (BlockState)newState.trySetValue((Property)BlockStateProperties.FACING, (Comparable)hit.getDirection());
        InteractionResult interactionResult = state.getBlock() == AEBlocks.CRAFTING_UNIT.block() ? (this.transform(level, pos, newState) ? InteractionResult.SUCCESS : InteractionResult.FAIL) : (result = this.removeUpgrade(level, player, pos, newState));
        if (result == InteractionResult.FAIL) {
            return false;
        }
        if (result == InteractionResult.PASS) {
            return true;
        }
        heldItem.consume(1, (LivingEntity)player);
        return true;
    }

    public InteractionResult removeUpgrade(Level level, Player player, BlockPos pos, BlockState newState) {
        if (this.type == CraftingUnitType.UNIT || level.isClientSide()) {
            return InteractionResult.FAIL;
        }
        ItemStack removedUpgrade = CraftingUnitTransformRecipe.getRemovedUpgrade(level, this);
        if (removedUpgrade.isEmpty()) {
            return InteractionResult.FAIL;
        }
        CraftingBlockEntity cb = (CraftingBlockEntity)this.getBlockEntity((BlockGetter)level, pos);
        if (cb != null && cb.getCluster() != null && cb.getCluster().isBusy()) {
            player.displayClientMessage((Component)PlayerMessages.CraftingCpuBusy.text().withColor(0xFF1F1F), true);
            return InteractionResult.PASS;
        }
        if (!this.transform(level, pos, newState)) {
            return InteractionResult.FAIL;
        }
        player.getInventory().placeItemBackInInventory(removedUpgrade);
        return InteractionResult.SUCCESS;
    }

    private boolean transform(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide() || !level.removeBlock(pos, false) || !level.setBlock(pos, state, 3)) {
            return false;
        }
        level.playSound(null, (double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 0.5f, 1.0f);
        return true;
    }
}

