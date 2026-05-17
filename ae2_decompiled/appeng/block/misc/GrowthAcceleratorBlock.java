/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.StateDefinition$Builder
 *  net.minecraft.world.level.block.state.properties.BooleanProperty
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.OnlyIn
 */
package appeng.block.misc;

import appeng.api.orientation.IOrientationStrategy;
import appeng.api.orientation.OrientationStrategies;
import appeng.block.AEBaseEntityBlock;
import appeng.blockentity.misc.GrowthAcceleratorBlockEntity;
import appeng.client.render.effects.ParticleTypes;
import appeng.core.AEConfig;
import appeng.core.AppEngClient;
import appeng.util.Platform;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class GrowthAcceleratorBlock
extends AEBaseEntityBlock<GrowthAcceleratorBlockEntity> {
    public static final BooleanProperty POWERED = BooleanProperty.create((String)"powered");

    public GrowthAcceleratorBlock() {
        super(GrowthAcceleratorBlock.metalProps());
        this.registerDefaultState((BlockState)this.defaultBlockState().setValue((Property)POWERED, (Comparable)Boolean.valueOf(false)));
    }

    @Override
    protected BlockState updateBlockStateFromBlockEntity(BlockState currentState, GrowthAcceleratorBlockEntity be) {
        return (BlockState)currentState.setValue((Property)POWERED, (Comparable)Boolean.valueOf(be.isPowered()));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(new Property[]{POWERED});
    }

    @Override
    public IOrientationStrategy getOrientationStrategy() {
        return OrientationStrategies.facing();
    }

    @OnlyIn(value=Dist.CLIENT)
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource r) {
        if (!AEConfig.instance().isEnableEffects()) {
            return;
        }
        GrowthAcceleratorBlockEntity cga = (GrowthAcceleratorBlockEntity)this.getBlockEntity((BlockGetter)level, pos);
        if (cga != null && cga.isPowered() && AppEngClient.instance().shouldAddParticles(r)) {
            double d0 = r.nextFloat() - 0.5f;
            double d1 = r.nextFloat() - 0.5f;
            Direction up = cga.getTop();
            Direction forward = cga.getFront();
            Direction west = Platform.crossProduct(forward, up);
            double rx = 0.5 + (double)pos.getX();
            double ry = 0.5 + (double)pos.getY();
            double rz = 0.5 + (double)pos.getZ();
            rx += (double)up.getStepX() * d0;
            ry += (double)up.getStepY() * d0;
            rz += (double)up.getStepZ() * d0;
            int x = pos.getX();
            int y = pos.getY();
            int z = pos.getZ();
            double dz = 0.0;
            double dx = 0.0;
            BlockPos pt = null;
            switch (r.nextInt(4)) {
                case 0: {
                    dx = 0.6;
                    dz = d1;
                    pt = new BlockPos(x + west.getStepX(), y + west.getStepY(), z + west.getStepZ());
                    break;
                }
                case 1: {
                    dx = d1;
                    dz += 0.6;
                    pt = new BlockPos(x + forward.getStepX(), y + forward.getStepY(), z + forward.getStepZ());
                    break;
                }
                case 2: {
                    dx = d1;
                    dz = -0.6;
                    pt = new BlockPos(x - forward.getStepX(), y - forward.getStepY(), z - forward.getStepZ());
                    break;
                }
                case 3: {
                    dx = -0.6;
                    dz = d1;
                    pt = new BlockPos(x - west.getStepX(), y - west.getStepY(), z - west.getStepZ());
                }
            }
            if (!level.getBlockState(pt).isAir()) {
                return;
            }
            rx += dx * (double)west.getStepX();
            ry += dx * (double)west.getStepY();
            rz += dx * (double)west.getStepZ();
            Minecraft.getInstance().particleEngine.createParticle((ParticleOptions)ParticleTypes.LIGHTNING, rx += dz * (double)forward.getStepX(), ry += dz * (double)forward.getStepY(), rz += dz * (double)forward.getStepZ(), 0.0, 0.0, 0.0);
        }
    }
}

