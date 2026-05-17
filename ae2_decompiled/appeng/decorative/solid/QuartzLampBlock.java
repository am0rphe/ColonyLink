/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.block.state.BlockState
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.OnlyIn
 */
package appeng.decorative.solid;

import appeng.client.render.effects.ParticleTypes;
import appeng.core.AEConfig;
import appeng.core.AppEngClient;
import appeng.decorative.solid.QuartzGlassBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class QuartzLampBlock
extends QuartzGlassBlock {
    public QuartzLampBlock(BlockBehaviour.Properties props) {
        super(props);
    }

    @OnlyIn(value=Dist.CLIENT)
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource r) {
        if (!AEConfig.instance().isEnableEffects()) {
            return;
        }
        if (AppEngClient.instance().shouldAddParticles(r)) {
            double d0 = (double)(r.nextFloat() - 0.5f) * 0.96;
            double d1 = (double)(r.nextFloat() - 0.5f) * 0.96;
            double d2 = (double)(r.nextFloat() - 0.5f) * 0.96;
            level.addParticle((ParticleOptions)ParticleTypes.VIBRANT, 0.5 + (double)pos.getX() + d0, 0.5 + (double)pos.getY() + d1, 0.5 + (double)pos.getZ() + d2, 0.0, 0.0, 0.0);
        }
    }
}

