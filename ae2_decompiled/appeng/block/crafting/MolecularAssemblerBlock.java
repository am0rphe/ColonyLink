/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.StateDefinition$Builder
 *  net.minecraft.world.level.block.state.properties.BooleanProperty
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.phys.BlockHitResult
 */
package appeng.block.crafting;

import appeng.block.AEBaseEntityBlock;
import appeng.blockentity.crafting.MolecularAssemblerBlockEntity;
import appeng.menu.MenuOpener;
import appeng.menu.implementations.MolecularAssemblerMenu;
import appeng.menu.locator.MenuLocators;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;

public class MolecularAssemblerBlock
extends AEBaseEntityBlock<MolecularAssemblerBlockEntity> {
    public static final BooleanProperty POWERED = BooleanProperty.create((String)"powered");

    public MolecularAssemblerBlock(BlockBehaviour.Properties props) {
        super(props);
        this.registerDefaultState((BlockState)this.defaultBlockState().setValue((Property)POWERED, (Comparable)Boolean.valueOf(false)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(new Property[]{POWERED});
    }

    @Override
    protected BlockState updateBlockStateFromBlockEntity(BlockState currentState, MolecularAssemblerBlockEntity be) {
        return (BlockState)currentState.setValue((Property)POWERED, (Comparable)Boolean.valueOf(be.isPowered()));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        MolecularAssemblerBlockEntity be = (MolecularAssemblerBlockEntity)this.getBlockEntity((BlockGetter)level, pos);
        if (be != null) {
            if (!level.isClientSide()) {
                MenuOpener.open(MolecularAssemblerMenu.TYPE, player, MenuLocators.forBlockEntity(be));
            }
            return InteractionResult.sidedSuccess((boolean)level.isClientSide());
        }
        return InteractionResult.PASS;
    }
}

