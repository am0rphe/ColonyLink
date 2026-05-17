/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.StateDefinition$Builder
 *  net.minecraft.world.level.block.state.properties.BooleanProperty
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.phys.BlockHitResult
 */
package appeng.block.storage;

import appeng.api.orientation.IOrientationStrategy;
import appeng.api.orientation.OrientationStrategies;
import appeng.api.storage.cells.CellState;
import appeng.block.AEBaseEntityBlock;
import appeng.blockentity.storage.MEChestBlockEntity;
import appeng.core.localization.PlayerMessages;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;

public class MEChestBlock
extends AEBaseEntityBlock<MEChestBlockEntity> {
    public static final BooleanProperty LIGHTS_ON = BooleanProperty.create((String)"lights_on");

    public MEChestBlock() {
        super(MEChestBlock.metalProps());
        this.registerDefaultState((BlockState)this.defaultBlockState().setValue((Property)LIGHTS_ON, (Comparable)Boolean.valueOf(false)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(new Property[]{LIGHTS_ON});
    }

    @Override
    public IOrientationStrategy getOrientationStrategy() {
        return OrientationStrategies.full();
    }

    @Override
    protected BlockState updateBlockStateFromBlockEntity(BlockState currentState, MEChestBlockEntity be) {
        CellState cellState = CellState.ABSENT;
        if (be.getCellCount() >= 1) {
            cellState = be.getCellStatus(0);
        }
        return (BlockState)currentState.setValue((Property)LIGHTS_ON, (Comparable)Boolean.valueOf(be.isPowered() && cellState != CellState.ABSENT));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof MEChestBlockEntity) {
            MEChestBlockEntity be = (MEChestBlockEntity)blockEntity;
            if (!level.isClientSide()) {
                if (hitResult.getDirection() == be.getTop()) {
                    if (!be.openGui(player)) {
                        player.displayClientMessage((Component)PlayerMessages.ChestCannotReadStorageCell.text(), true);
                    }
                } else {
                    be.openCellInventoryMenu(player);
                }
            }
            return InteractionResult.sidedSuccess((boolean)level.isClientSide());
        }
        return super.useWithoutItem(state, level, pos, player, hitResult);
    }
}

