/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.particle.ParticleRenderType
 *  net.minecraft.client.particle.TextureSheetParticle
 *  net.minecraft.client.renderer.texture.TextureAtlasSprite
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.OnlyIn
 */
package appeng.client.render.cablebus;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(value=Dist.CLIENT)
public class CableBusBreakingParticle
extends TextureSheetParticle {
    private final float uCoord;
    private final float vCoord;

    public CableBusBreakingParticle(ClientLevel level, double x, double y, double z, double speedX, double speedY, double speedZ, TextureAtlasSprite sprite) {
        super(level, x, y, z, speedX, speedY, speedZ);
        this.setSprite(sprite);
        this.gravity = 1.0f;
        this.quadSize /= 2.0f;
        this.uCoord = this.random.nextFloat() * 3.0f;
        this.vCoord = this.random.nextFloat() * 3.0f;
    }

    public CableBusBreakingParticle(ClientLevel level, double x, double y, double z, TextureAtlasSprite sprite) {
        this(level, x, y, z, 0.0, 0.0, 0.0, sprite);
    }

    public ParticleRenderType getRenderType() {
        return ParticleRenderType.TERRAIN_SHEET;
    }

    protected float getU0() {
        return this.sprite.getU((this.uCoord + 1.0f) / 4.0f);
    }

    protected float getU1() {
        return this.sprite.getU(this.uCoord / 4.0f);
    }

    protected float getV0() {
        return this.sprite.getV(this.vCoord / 4.0f);
    }

    protected float getV1() {
        return this.sprite.getV((this.vCoord + 1.0f) / 4.0f);
    }
}

