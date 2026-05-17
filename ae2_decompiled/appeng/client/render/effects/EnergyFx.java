/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.Camera
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.particle.Particle
 *  net.minecraft.client.particle.ParticleProvider
 *  net.minecraft.client.particle.ParticleRenderType
 *  net.minecraft.client.particle.SpriteSet
 *  net.minecraft.client.particle.TextureSheetParticle
 *  net.minecraft.util.Mth
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.OnlyIn
 */
package appeng.client.render.effects;

import appeng.client.render.effects.EnergyParticleData;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(value=Dist.CLIENT)
public class EnergyFx
extends TextureSheetParticle {
    private final int startBlkX;
    private final int startBlkY;
    private final int startBlkZ;

    public EnergyFx(ClientLevel level, double par2, double par4, double par6, SpriteSet sprite) {
        super(level, par2, par4, par6);
        this.gravity = 0.0f;
        this.bCol = 1.0f;
        this.gCol = 1.0f;
        this.rCol = 1.0f;
        this.alpha = 1.4f;
        this.quadSize = 3.5f;
        this.pickSprite(sprite);
        this.startBlkX = Mth.floor((double)this.x);
        this.startBlkY = Mth.floor((double)this.y);
        this.startBlkZ = Mth.floor((double)this.z);
    }

    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public float getQuadSize(float scaleFactor) {
        return 0.1f * this.quadSize;
    }

    public void render(VertexConsumer buffer, Camera renderInfo, float partialTicks) {
        float x = (float)(this.xo + (this.x - this.xo) * (double)partialTicks);
        float y = (float)(this.yo + (this.y - this.yo) * (double)partialTicks);
        float z = (float)(this.zo + (this.z - this.zo) * (double)partialTicks);
        int blkX = Mth.floor((float)x);
        int blkY = Mth.floor((float)y);
        int blkZ = Mth.floor((float)z);
        if (blkX == this.startBlkX && blkY == this.startBlkY && blkZ == this.startBlkZ) {
            super.render(buffer, renderInfo, partialTicks);
        }
    }

    public void tick() {
        super.tick();
        this.onGround = false;
        this.quadSize *= 0.89f;
        this.alpha *= 0.89f;
    }

    public void setMotionX(float motionX) {
        this.xd = motionX;
    }

    public void setMotionY(float motionY) {
        this.yd = motionY;
    }

    public void setMotionZ(float motionZ) {
        this.zd = motionZ;
    }

    @OnlyIn(value=Dist.CLIENT)
    public static class Factory
    implements ParticleProvider<EnergyParticleData> {
        private final SpriteSet spriteSet;

        public Factory(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        public Particle createParticle(EnergyParticleData data, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            EnergyFx result = new EnergyFx(level, x, y, z, this.spriteSet);
            result.setMotionX((float)xSpeed);
            result.setMotionY((float)ySpeed);
            result.setMotionZ((float)zSpeed);
            if (data.forItem()) {
                result.x += -0.2 * (double)data.direction().getStepX();
                result.y += -0.2 * (double)data.direction().getStepY();
                result.z += -0.2 * (double)data.direction().getStepZ();
                result.quadSize *= 0.8f;
            }
            return result;
        }
    }
}

