/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.particle.Particle
 *  net.minecraft.client.particle.ParticleProvider
 *  net.minecraft.client.particle.SpriteSet
 *  net.minecraft.util.Mth
 *  net.minecraft.util.RandomSource
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.OnlyIn
 */
package appeng.client.render.effects;

import appeng.client.render.effects.LightningArcParticleData;
import appeng.client.render.effects.LightningFX;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(value=Dist.CLIENT)
public class LightningArcFX
extends LightningFX {
    private static final RandomSource RANDOM_GENERATOR = RandomSource.create();
    private final double rx;
    private final double ry;
    private final double rz;

    public LightningArcFX(ClientLevel level, double x, double y, double z, double ex, double ey, double ez, double r, double g, double b) {
        super(level, x, y, z, r, g, b, 6);
        this.rx = ex - x;
        this.ry = ey - y;
        this.rz = ez - z;
        this.regen();
    }

    @Override
    protected void regen() {
        float i = 1.0f / (float)(this.getSteps() - 1);
        float lastDirectionX = (float)this.rx * i;
        float lastDirectionY = (float)this.ry * i;
        float lastDirectionZ = (float)this.rz * i;
        float len = Mth.sqrt((float)(lastDirectionX * lastDirectionX + lastDirectionY * lastDirectionY + lastDirectionZ * lastDirectionZ));
        for (int s = 0; s < this.getSteps(); ++s) {
            float[][] localSteps = this.getPrecomputedSteps();
            localSteps[s][0] = (lastDirectionX + (RANDOM_GENERATOR.nextFloat() - 0.5f) * len * 1.2f) / 2.0f;
            localSteps[s][1] = (lastDirectionY + (RANDOM_GENERATOR.nextFloat() - 0.5f) * len * 1.2f) / 2.0f;
            localSteps[s][2] = (lastDirectionZ + (RANDOM_GENERATOR.nextFloat() - 0.5f) * len * 1.2f) / 2.0f;
        }
    }

    @OnlyIn(value=Dist.CLIENT)
    public static class Factory
    implements ParticleProvider<LightningArcParticleData> {
        private final SpriteSet spriteSet;

        public Factory(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        public Particle createParticle(LightningArcParticleData data, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            LightningArcFX lightningFX = new LightningArcFX(level, x, y, z, data.target().x, data.target().y, data.target().z, 0.0, 0.0, 0.0);
            lightningFX.pickSprite(this.spriteSet);
            return lightningFX;
        }
    }
}

