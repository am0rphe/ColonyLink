/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.BlockHitResult
 */
package appeng.block.misc;

import appeng.block.AEBaseEntityBlock;
import appeng.blockentity.misc.CondenserBlockEntity;
import appeng.menu.MenuOpener;
import appeng.menu.implementations.CondenserMenu;
import appeng.menu.locator.MenuLocators;
import appeng.util.InteractionUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class CondenserBlock
extends AEBaseEntityBlock<CondenserBlockEntity> {
    public CondenserBlock() {
        super(CondenserBlock.metalProps());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        CondenserBlockEntity tc;
        if (!level.isClientSide() && (tc = (CondenserBlockEntity)this.getBlockEntity((BlockGetter)level, pos)) != null && !InteractionUtil.isInAlternateUseMode(player)) {
            MenuOpener.open(CondenserMenu.TYPE, player, MenuLocators.forBlockEntity(tc));
        }
        return InteractionResult.sidedSuccess((boolean)level.isClientSide());
    }
}

