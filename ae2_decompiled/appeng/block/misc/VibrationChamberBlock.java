/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.StateDefinition$Builder
 *  net.minecraft.world.level.block.state.properties.BooleanProperty
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.phys.BlockHitResult
 */
package appeng.block.misc;

import appeng.api.orientation.IOrientationStrategy;
import appeng.api.orientation.OrientationStrategies;
import appeng.block.AEBaseEntityBlock;
import appeng.blockentity.misc.VibrationChamberBlockEntity;
import appeng.core.AEConfig;
import appeng.menu.MenuOpener;
import appeng.menu.implementations.VibrationChamberMenu;
import appeng.menu.locator.MenuLocators;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;

public final class VibrationChamberBlock
extends AEBaseEntityBlock<VibrationChamberBlockEntity> {
    public static final BooleanProperty ACTIVE = BooleanProperty.create((String)"active");

    public VibrationChamberBlock() {
        super(VibrationChamberBlock.metalProps().strength(4.2f));
        this.registerDefaultState((BlockState)this.defaultBlockState().setValue((Property)ACTIVE, (Comparable)Boolean.valueOf(false)));
    }

    @Override
    protected BlockState updateBlockStateFromBlockEntity(BlockState currentState, VibrationChamberBlockEntity be) {
        return (BlockState)currentState.setValue((Property)ACTIVE, (Comparable)Boolean.valueOf(be.isOn));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(new Property[]{ACTIVE});
    }

    @Override
    public IOrientationStrategy getOrientationStrategy() {
        return OrientationStrategies.full();
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof VibrationChamberBlockEntity) {
            VibrationChamberBlockEntity be = (VibrationChamberBlockEntity)blockEntity;
            if (!level.isClientSide) {
                MenuOpener.open(VibrationChamberMenu.TYPE, player, MenuLocators.forBlockEntity(be));
            }
            return InteractionResult.sidedSuccess((boolean)level.isClientSide());
        }
        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource r) {
        if (!AEConfig.instance().isEnableEffects()) {
            return;
        }
        VibrationChamberBlockEntity tc = (VibrationChamberBlockEntity)this.getBlockEntity((BlockGetter)level, pos);
        if (tc != null && tc.isOn) {
            double f1 = (float)pos.getX() + 0.5f;
            double f2 = (float)pos.getY() + 0.5f;
            double f3 = (float)pos.getZ() + 0.5f;
            Direction front = tc.getFront();
            Direction top = tc.getTop();
            int west_x = front.getStepY() * top.getStepZ() - front.getStepZ() * top.getStepY();
            int west_y = front.getStepZ() * top.getStepX() - front.getStepX() * top.getStepZ();
            int west_z = front.getStepX() * top.getStepY() - front.getStepY() * top.getStepX();
            f1 += (double)front.getStepX() * 0.6;
            f2 += (double)front.getStepY() * 0.6;
            f3 += (double)front.getStepZ() * 0.6;
            double ox = r.nextDouble();
            double oy = r.nextDouble() * (double)0.2f;
            f1 += (double)top.getStepX() * (-0.3 + oy);
            f2 += (double)top.getStepY() * (-0.3 + oy);
            f3 += (double)top.getStepZ() * (-0.3 + oy);
            level.addParticle((ParticleOptions)ParticleTypes.SMOKE, f1 += (double)west_x * (0.3 * ox - 0.15), f2 += (double)west_y * (0.3 * ox - 0.15), f3 += (double)west_z * (0.3 * ox - 0.15), 0.0, 0.0, 0.0);
            level.addParticle((ParticleOptions)ParticleTypes.FLAME, f1, f2, f3, 0.0, 0.0, 0.0);
        }
    }
}

