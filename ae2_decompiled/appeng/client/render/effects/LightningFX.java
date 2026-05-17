/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.Camera
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.particle.Particle
 *  net.minecraft.client.particle.ParticleProvider
 *  net.minecraft.client.particle.ParticleRenderType
 *  net.minecraft.client.particle.SpriteSet
 *  net.minecraft.client.particle.TextureSheetParticle
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.core.particles.SimpleParticleType
 *  net.minecraft.util.Mth
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.phys.Vec3
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.OnlyIn
 */
package appeng.client.render.effects;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(value=Dist.CLIENT)
public class LightningFX
extends TextureSheetParticle {
    private static final RandomSource RANDOM_GENERATOR = RandomSource.create();
    private static final int STEPS = 5;
    private static final int BRIGHTNESS = 208;
    private final float[][] precomputedSteps;
    private final float[] vertices = new float[3];
    private final float[] verticesWithUV = new float[3];
    private boolean hasData = false;

    private LightningFX(ClientLevel level, double x, double y, double z, double r, double g, double b) {
        this(level, x, y, z, r, g, b, 6);
        this.regen();
    }

    protected LightningFX(ClientLevel level, double x, double y, double z, double r, double g, double b, int maxAge) {
        super(level, x, y, z, r, g, b);
        this.precomputedSteps = new float[5][3];
        this.xd = 0.0;
        this.yd = 0.0;
        this.zd = 0.0;
        this.lifetime = maxAge;
    }

    protected void regen() {
        float lastDirectionX = (RANDOM_GENERATOR.nextFloat() - 0.5f) * 0.9f;
        float lastDirectionY = (RANDOM_GENERATOR.nextFloat() - 0.5f) * 0.9f;
        float lastDirectionZ = (RANDOM_GENERATOR.nextFloat() - 0.5f) * 0.9f;
        for (int s = 0; s < 5; ++s) {
            this.precomputedSteps[s][0] = lastDirectionX = (lastDirectionX + (RANDOM_GENERATOR.nextFloat() - 0.5f) * 0.9f) / 2.0f;
            this.precomputedSteps[s][1] = lastDirectionY = (lastDirectionY + (RANDOM_GENERATOR.nextFloat() - 0.5f) * 0.9f) / 2.0f;
            this.precomputedSteps[s][2] = lastDirectionZ = (lastDirectionZ + (RANDOM_GENERATOR.nextFloat() - 0.5f) * 0.9f) / 2.0f;
        }
    }

    protected int getSteps() {
        return 5;
    }

    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
        }
        this.yd -= 0.04 * (double)this.gravity;
        this.move(this.xd, this.yd, this.zd);
        this.xd *= (double)0.98f;
        this.yd *= (double)0.98f;
        this.zd *= (double)0.98f;
    }

    public void render(VertexConsumer buffer, Camera renderInfo, float partialTicks) {
        Vec3 Vector3d = renderInfo.getPosition();
        float centerX = (float)(Mth.lerp((double)partialTicks, (double)this.xo, (double)this.x) - Vector3d.x());
        float centerY = (float)(Mth.lerp((double)partialTicks, (double)this.yo, (double)this.y) - Vector3d.y());
        float centerZ = (float)(Mth.lerp((double)partialTicks, (double)this.zo, (double)this.z) - Vector3d.z());
        float j = 1.0f;
        float red = this.rCol * 1.0f * 0.9f;
        float green = this.gCol * 1.0f * 0.95f;
        float blue = this.bCol * 1.0f;
        float alpha = this.alpha;
        if (this.age == 3) {
            this.regen();
        }
        float u = this.getU0() + (this.getU1() - this.getU0()) / 2.0f;
        float v = this.getV0() + (this.getV1() - this.getV0()) / 2.0f;
        float scale = 0.02f;
        float[] a = new float[3];
        float[] b = new float[3];
        float ox = 0.0f;
        float oy = 0.0f;
        float oz = 0.0f;
        LocalPlayer p = Minecraft.getInstance().player;
        for (int layer = 0; layer < 2; ++layer) {
            if (layer == 0) {
                scale = 0.04f;
                red = this.rCol * 1.0f * 0.4f;
                green = this.gCol * 1.0f * 0.25f;
                blue = this.bCol * 1.0f * 0.45f;
            } else {
                scale = 0.02f;
                red = this.rCol * 1.0f * 0.9f;
                green = this.gCol * 1.0f * 0.65f;
                blue = this.bCol * 1.0f * 0.85f;
            }
            for (int cycle = 0; cycle < 3; ++cycle) {
                this.clear();
                float x = centerX;
                float y = centerY;
                float z = centerZ;
                for (int s = 0; s < 5; ++s) {
                    float xN = x + this.precomputedSteps[s][0];
                    float yN = y + this.precomputedSteps[s][1];
                    float zN = z + this.precomputedSteps[s][2];
                    float xD = xN - x;
                    float yD = yN - y;
                    float zD = zN - z;
                    if (cycle == 0) {
                        ox = yD * 0.0f - 1.0f * zD;
                        oy = zD * 0.0f - 0.0f * xD;
                        oz = xD * 1.0f - 0.0f * yD;
                    }
                    if (cycle == 1) {
                        ox = yD * 1.0f - 0.0f * zD;
                        oy = zD * 0.0f - 1.0f * xD;
                        oz = xD * 0.0f - 0.0f * yD;
                    }
                    if (cycle == 2) {
                        ox = yD * 0.0f - 0.0f * zD;
                        oy = zD * 1.0f - 0.0f * xD;
                        oz = xD * 0.0f - 1.0f * yD;
                    }
                    float ss = Mth.sqrt((float)(ox * ox + oy * oy + oz * oz)) / ((5.0f - (float)s) / 5.0f * scale);
                    a[0] = x + (ox /= ss);
                    a[1] = y + (oy /= ss);
                    a[2] = z + (oz /= ss);
                    b[0] = x;
                    b[1] = y;
                    b[2] = z;
                    this.draw(red, green, blue, buffer, a, b, u, v);
                    x = xN;
                    y = yN;
                    z = zN;
                }
            }
        }
    }

    private void clear() {
        this.hasData = false;
    }

    private void draw(float red, float green, float blue, VertexConsumer tess, float[] a, float[] b, float u, float v) {
        if (this.hasData) {
            tess.addVertex(a[0], a[1], a[2]).setUv(u, v).setColor(red, green, blue, this.alpha).setUv2(208, 208);
            tess.addVertex(this.vertices[0], this.vertices[1], this.vertices[2]).setUv(u, v).setColor(red, green, blue, this.alpha).setUv2(208, 208);
            tess.addVertex(this.verticesWithUV[0], this.verticesWithUV[1], this.verticesWithUV[2]).setUv(u, v).setColor(red, green, blue, this.alpha).setUv2(208, 208);
            tess.addVertex(b[0], b[1], b[2]).setUv(u, v).setColor(red, green, blue, this.alpha).setUv2(208, 208);
        }
        this.hasData = true;
        for (int x = 0; x < 3; ++x) {
            this.vertices[x] = a[x];
            this.verticesWithUV[x] = b[x];
        }
    }

    protected float[][] getPrecomputedSteps() {
        return this.precomputedSteps;
    }

    @OnlyIn(value=Dist.CLIENT)
    public static class Factory
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;

        public Factory(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        public Particle createParticle(SimpleParticleType typeIn, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            LightningFX lightningFX = new LightningFX(level, x, y, z, xSpeed, ySpeed, zSpeed);
            lightningFX.pickSprite(this.spriteSet);
            return lightningFX;
        }
    }
}

