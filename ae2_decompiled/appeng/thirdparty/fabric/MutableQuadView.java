/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.block.model.BakedQuad
 *  net.minecraft.client.renderer.texture.TextureAtlasSprite
 *  net.minecraft.core.Direction
 *  org.jetbrains.annotations.Nullable
 *  org.joml.Vector2f
 *  org.joml.Vector3f
 */
package appeng.thirdparty.fabric;

import appeng.thirdparty.fabric.MutableQuadViewImpl;
import appeng.thirdparty.fabric.QuadView;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector3f;

public interface MutableQuadView
extends QuadView {
    public static final int BAKE_ROTATE_NONE = 0;
    public static final int BAKE_ROTATE_90 = 1;
    public static final int BAKE_ROTATE_180 = 2;
    public static final int BAKE_ROTATE_270 = 3;
    public static final int BAKE_LOCK_UV = 4;
    public static final int BAKE_FLIP_U = 8;
    public static final int BAKE_FLIP_V = 16;
    public static final int BAKE_NORMALIZED = 32;

    public static MutableQuadView getInstance() {
        return MutableQuadViewImpl.THREAD_LOCAL.get();
    }

    public MutableQuadView pos(int var1, float var2, float var3, float var4);

    default public MutableQuadView pos(int vertexIndex, Vector3f pos) {
        return this.pos(vertexIndex, pos.x(), pos.y(), pos.z());
    }

    public MutableQuadView color(int var1, int var2);

    default public MutableQuadView color(int c0, int c1, int c2, int c3) {
        this.color(0, c0);
        this.color(1, c1);
        this.color(2, c2);
        this.color(3, c3);
        return this;
    }

    public MutableQuadView uv(int var1, float var2, float var3);

    default public MutableQuadView uv(int vertexIndex, Vector2f uv) {
        return this.uv(vertexIndex, uv.x, uv.y);
    }

    public MutableQuadView shade(boolean var1);

    public MutableQuadView ambientOcclusion(boolean var1);

    public MutableQuadView spriteBake(TextureAtlasSprite var1, int var2);

    public MutableQuadView lightmap(int var1, int var2);

    default public MutableQuadView lightmap(int b0, int b1, int b2, int b3) {
        this.lightmap(0, b0);
        this.lightmap(1, b1);
        this.lightmap(2, b2);
        this.lightmap(3, b3);
        return this;
    }

    public MutableQuadView normal(int var1, float var2, float var3, float var4);

    default public MutableQuadView normal(int vertexIndex, Vector3f normal) {
        return this.normal(vertexIndex, normal.x(), normal.y(), normal.z());
    }

    public MutableQuadView cullFace(@Nullable Direction var1);

    public MutableQuadView nominalFace(@Nullable Direction var1);

    public MutableQuadView colorIndex(int var1);

    public MutableQuadView tag(int var1);

    public MutableQuadView fromVanilla(int[] var1, int var2);

    public MutableQuadView fromVanilla(BakedQuad var1, @Nullable Direction var2);
}

