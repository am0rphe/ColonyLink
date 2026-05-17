/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.texture.TextureAtlasSprite
 *  net.minecraft.core.Direction
 *  org.joml.Vector2f
 *  org.joml.Vector3f
 */
package appeng.thirdparty.fabric;

import appeng.thirdparty.fabric.MutableQuadView;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import org.joml.Vector2f;
import org.joml.Vector3f;

public interface QuadEmitter
extends MutableQuadView {
    public static final float CULL_FACE_EPSILON = 1.0E-5f;

    @Override
    public QuadEmitter pos(int var1, float var2, float var3, float var4);

    @Override
    default public QuadEmitter pos(int vertexIndex, Vector3f pos) {
        MutableQuadView.super.pos(vertexIndex, pos);
        return this;
    }

    @Override
    public QuadEmitter color(int var1, int var2);

    @Override
    default public QuadEmitter color(int c0, int c1, int c2, int c3) {
        MutableQuadView.super.color(c0, c1, c2, c3);
        return this;
    }

    @Override
    public QuadEmitter uv(int var1, float var2, float var3);

    @Override
    default public QuadEmitter uv(int vertexIndex, Vector2f uv) {
        MutableQuadView.super.uv(vertexIndex, uv);
        return this;
    }

    @Override
    public QuadEmitter shade(boolean var1);

    @Override
    public QuadEmitter ambientOcclusion(boolean var1);

    @Override
    public QuadEmitter spriteBake(TextureAtlasSprite var1, int var2);

    default public QuadEmitter uvUnitSquare() {
        this.uv(0, 0.0f, 0.0f);
        this.uv(1, 0.0f, 1.0f);
        this.uv(2, 1.0f, 1.0f);
        this.uv(3, 1.0f, 0.0f);
        return this;
    }

    @Override
    public QuadEmitter lightmap(int var1, int var2);

    @Override
    default public QuadEmitter lightmap(int b0, int b1, int b2, int b3) {
        MutableQuadView.super.lightmap(b0, b1, b2, b3);
        return this;
    }

    @Override
    public QuadEmitter normal(int var1, float var2, float var3, float var4);

    @Override
    default public QuadEmitter normal(int vertexIndex, Vector3f normal) {
        MutableQuadView.super.normal(vertexIndex, normal);
        return this;
    }

    @Override
    public QuadEmitter cullFace(Direction var1);

    @Override
    public QuadEmitter nominalFace(Direction var1);

    @Override
    public QuadEmitter colorIndex(int var1);

    @Override
    public QuadEmitter tag(int var1);

    @Override
    public QuadEmitter fromVanilla(int[] var1, int var2);

    default public QuadEmitter square(Direction nominalFace, float left, float bottom, float right, float top, float depth) {
        if (Math.abs(depth) < 1.0E-5f) {
            this.cullFace(nominalFace);
            depth = 0.0f;
        } else {
            this.cullFace(null);
        }
        this.nominalFace(nominalFace);
        switch (nominalFace) {
            case UP: {
                depth = 1.0f - depth;
                top = 1.0f - top;
                bottom = 1.0f - bottom;
            }
            case DOWN: {
                this.pos(0, left, depth, top);
                this.pos(1, left, depth, bottom);
                this.pos(2, right, depth, bottom);
                this.pos(3, right, depth, top);
                break;
            }
            case EAST: {
                depth = 1.0f - depth;
                left = 1.0f - left;
                right = 1.0f - right;
            }
            case WEST: {
                this.pos(0, depth, top, left);
                this.pos(1, depth, bottom, left);
                this.pos(2, depth, bottom, right);
                this.pos(3, depth, top, right);
                break;
            }
            case SOUTH: {
                depth = 1.0f - depth;
                left = 1.0f - left;
                right = 1.0f - right;
            }
            case NORTH: {
                this.pos(0, 1.0f - left, top, depth);
                this.pos(1, 1.0f - left, bottom, depth);
                this.pos(2, 1.0f - right, bottom, depth);
                this.pos(3, 1.0f - right, top, depth);
            }
        }
        return this;
    }

    public QuadEmitter emit();
}

