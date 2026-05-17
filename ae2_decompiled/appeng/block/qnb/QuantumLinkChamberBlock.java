/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.shapes.CollisionContext
 *  net.minecraft.world.phys.shapes.Shapes
 *  net.minecraft.world.phys.shapes.VoxelShape
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.OnlyIn
 */
package appeng.block.qnb;

import appeng.block.qnb.QuantumBaseBlock;
import appeng.blockentity.qnb.QuantumBridgeBlockEntity;
import appeng.client.EffectType;
import appeng.core.AppEng;
import appeng.core.AppEngClient;
import appeng.menu.MenuOpener;
import appeng.menu.implementations.QNBMenu;
import appeng.menu.locator.MenuLocators;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class QuantumLinkChamberBlock
extends QuantumBaseBlock {
    private static final VoxelShape SHAPE;

    public QuantumLinkChamberBlock() {
        super(QuantumLinkChamberBlock.glassProps());
    }

    @OnlyIn(value=Dist.CLIENT)
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource rand) {
        QuantumBridgeBlockEntity bridge = (QuantumBridgeBlockEntity)this.getBlockEntity((BlockGetter)level, pos);
        if (bridge != null && bridge.hasQES() && AppEngClient.instance().shouldAddParticles(rand)) {
            AppEng.instance().spawnEffect(EffectType.Energy, level, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, null);
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof QuantumBridgeBlockEntity) {
            QuantumBridgeBlockEntity be = (QuantumBridgeBlockEntity)blockEntity;
            if (!level.isClientSide()) {
                MenuOpener.open(QNBMenu.TYPE, player, MenuLocators.forBlockEntity(be));
            }
            return InteractionResult.sidedSuccess((boolean)level.isClientSide());
        }
        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    static {
        double onePixel = 0.125;
        SHAPE = Shapes.create((AABB)new AABB(0.125, 0.125, 0.125, 0.875, 0.875, 0.875));
    }
}

