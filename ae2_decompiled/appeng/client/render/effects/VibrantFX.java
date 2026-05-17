/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.particle.Particle
 *  net.minecraft.client.particle.ParticleProvider
 *  net.minecraft.client.particle.ParticleRenderType
 *  net.minecraft.client.particle.SpriteSet
 *  net.minecraft.client.particle.TextureSheetParticle
 *  net.minecraft.core.particles.SimpleParticleType
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.OnlyIn
 */
package appeng.client.render.effects;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(value=Dist.CLIENT)
public class VibrantFX
extends TextureSheetParticle {
    public VibrantFX(ClientLevel level, double x, double y, double z, double par8, double par10, double par12, SpriteSet sprite) {
        super(level, x, y, z, par8, par10, par12);
        float f = this.random.nextFloat() * 0.1f + 0.8f;
        this.rCol = f * 0.7f;
        this.gCol = f * 0.89f;
        this.bCol = f * 0.9f;
        this.pickSprite(sprite);
        this.setSize(0.04f, 0.04f);
        this.quadSize *= this.random.nextFloat() * 0.6f + 1.9f;
        this.xd = 0.0;
        this.yd = 0.0;
        this.zd = 0.0;
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.lifetime = (int)(20.0 / (Math.random() * 0.8 + 0.1));
    }

    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    public int getLightColor(float par1) {
        return 0xF000F0;
    }

    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.quadSize = (float)((double)this.quadSize * 0.95);
        if (this.lifetime <= 0 || (double)this.quadSize < 0.1) {
            this.remove();
        }
        --this.lifetime;
    }

    @OnlyIn(value=Dist.CLIENT)
    public static class Factory
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;

        public Factory(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        public Particle createParticle(SimpleParticleType typeIn, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new VibrantFX(level, x, y, z, xSpeed, ySpeed, zSpeed, this.spriteSet);
        }
    }
}

