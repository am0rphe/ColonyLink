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
 *  net.minecraft.core.particles.SimpleParticleType
 *  net.minecraft.util.Mth
 *  net.minecraft.world.phys.Vec3
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.OnlyIn
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 */
package appeng.client.render.effects;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Quaternionfc;
import org.joml.Vector3f;

@OnlyIn(value=Dist.CLIENT)
public class CraftingFx
extends TextureSheetParticle {
    private final float offsetX;
    private final float offsetY;
    private final float offsetZ;

    public CraftingFx(ClientLevel level, double x, double y, double z, SpriteSet sprite) {
        super(level, x, y, z);
        Vector3f off = new Vector3f(this.random.nextFloat() - 0.5f, this.random.nextFloat() - 0.5f, this.random.nextFloat() - 0.5f);
        off.normalize();
        off.mul(0.35f);
        this.offsetX = off.x();
        this.offsetY = off.y();
        this.offsetZ = off.z();
        this.gravity = 0.0f;
        this.bCol = 1.0f;
        this.gCol = 0.9f;
        this.rCol = 1.0f;
        this.pickSprite(sprite);
        this.lifetime = (int)((double)this.lifetime / 1.2);
        this.hasPhysics = false;
    }

    public void render(VertexConsumer buffer, Camera renderInfo, float partialTicks) {
        float f = ((float)this.age + partialTicks) / (float)this.lifetime;
        float offX = (float)this.x + Mth.lerp((float)f, (float)this.offsetX, (float)0.0f);
        float offY = (float)this.y + Mth.lerp((float)f, (float)this.offsetY, (float)0.0f);
        float offZ = (float)this.z + Mth.lerp((float)f, (float)this.offsetZ, (float)0.0f);
        float alpha = Mth.lerp((float)CraftingFx.easeOutCirc(f), (float)1.3f, (float)0.1f);
        float scale = Mth.lerp((float)CraftingFx.easeOutCirc(f), (float)0.13f, (float)0.0f);
        Vec3 Vector3d = renderInfo.getPosition();
        offX = (float)((double)offX - Vector3d.x);
        offY = (float)((double)offY - Vector3d.y);
        offZ = (float)((double)offZ - Vector3d.z);
        Vector3f[] avector3f = new Vector3f[]{new Vector3f(-1.0f, -1.0f, 0.0f), new Vector3f(-1.0f, 1.0f, 0.0f), new Vector3f(1.0f, 1.0f, 0.0f), new Vector3f(1.0f, -1.0f, 0.0f)};
        for (int i = 0; i < 4; ++i) {
            Vector3f vector3f = avector3f[i];
            vector3f.rotate((Quaternionfc)renderInfo.rotation());
            vector3f.mul(scale);
            vector3f.add(offX, offY, offZ);
        }
        float minU = this.getU0();
        float maxU = this.getU1();
        float minV = this.getV0();
        float maxV = this.getV1();
        int j = 0xF000F0;
        buffer.addVertex(avector3f[3].x(), avector3f[3].y(), avector3f[0].z()).setUv(maxU, maxV).setColor(this.rCol, this.gCol, this.bCol, alpha).setLight(j);
        buffer.addVertex(avector3f[2].x(), avector3f[2].y(), avector3f[1].z()).setUv(maxU, minV).setColor(this.rCol, this.gCol, this.bCol, alpha).setLight(j);
        buffer.addVertex(avector3f[1].x(), avector3f[1].y(), avector3f[2].z()).setUv(minU, minV).setColor(this.rCol, this.gCol, this.bCol, alpha).setLight(j);
        buffer.addVertex(avector3f[0].x(), avector3f[0].y(), avector3f[3].z()).setUv(minU, maxV).setColor(this.rCol, this.gCol, this.bCol, alpha).setLight(j);
    }

    private static float easeOutCirc(float x) {
        return (float)Math.sqrt(1.0 - Math.pow(x - 1.0f, 2.0));
    }

    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public void tick() {
        if (this.age++ >= this.lifetime) {
            this.remove();
        }
    }

    @OnlyIn(value=Dist.CLIENT)
    public static class Factory
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;

        public Factory(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        public Particle createParticle(SimpleParticleType data, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new CraftingFx(level, x, y, z, this.spriteSet);
        }
    }
}

