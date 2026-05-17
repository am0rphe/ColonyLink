/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.block.model.BakedQuad
 *  net.minecraft.client.renderer.texture.TextureAtlasSprite
 *  net.minecraft.core.Direction
 *  org.jetbrains.annotations.Nullable
 */
package appeng.thirdparty.fabric;

import appeng.thirdparty.fabric.ColorHelper;
import appeng.thirdparty.fabric.EncodingFormat;
import appeng.thirdparty.fabric.MutableQuadView;
import appeng.thirdparty.fabric.NormalHelper;
import appeng.thirdparty.fabric.QuadEmitter;
import appeng.thirdparty.fabric.QuadViewImpl;
import appeng.thirdparty.fabric.TextureHelper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

public abstract class MutableQuadViewImpl
extends QuadViewImpl
implements QuadEmitter {
    public static final ThreadLocal<MutableQuadView> THREAD_LOCAL = ThreadLocal.withInitial(() -> new MutableQuadViewImpl(){
        {
            this.begin(new int[EncodingFormat.TOTAL_STRIDE], 0);
        }

        @Override
        public QuadEmitter emit() {
            throw new UnsupportedOperationException();
        }
    });

    public final void begin(int[] data, int baseIndex) {
        this.data = data;
        this.baseIndex = baseIndex;
        this.clear();
    }

    public void clear() {
        System.arraycopy(EncodingFormat.EMPTY, 0, this.data, this.baseIndex, EncodingFormat.TOTAL_STRIDE);
        this.isGeometryInvalid = true;
        this.nominalFace = null;
        this.normalFlags(0);
        this.tag(0);
        this.colorIndex(-1);
        this.cullFace(null);
        this.shade(true);
        this.ambientOcclusion(true);
    }

    @Override
    public MutableQuadViewImpl pos(int vertexIndex, float x, float y, float z) {
        int index = this.baseIndex + vertexIndex * EncodingFormat.VERTEX_STRIDE + EncodingFormat.VERTEX_X;
        this.data[index] = Float.floatToRawIntBits(x);
        this.data[index + 1] = Float.floatToRawIntBits(y);
        this.data[index + 2] = Float.floatToRawIntBits(z);
        this.isGeometryInvalid = true;
        return this;
    }

    @Override
    public MutableQuadViewImpl color(int vertexIndex, int color) {
        this.data[this.baseIndex + vertexIndex * EncodingFormat.VERTEX_STRIDE + EncodingFormat.VERTEX_COLOR] = color;
        return this;
    }

    @Override
    public MutableQuadViewImpl uv(int vertexIndex, float u, float v) {
        int i = this.baseIndex + vertexIndex * EncodingFormat.VERTEX_STRIDE + EncodingFormat.VERTEX_U;
        this.data[i] = Float.floatToRawIntBits(u);
        this.data[i + 1] = Float.floatToRawIntBits(v);
        return this;
    }

    @Override
    public MutableQuadViewImpl shade(boolean shade) {
        this.data[this.baseIndex + 0] = EncodingFormat.shade(this.data[this.baseIndex + 0], shade);
        return this;
    }

    @Override
    public MutableQuadViewImpl ambientOcclusion(boolean ao) {
        this.data[this.baseIndex + 0] = EncodingFormat.ambientOcclusion(this.data[this.baseIndex + 0], ao);
        return this;
    }

    @Override
    public MutableQuadViewImpl spriteBake(TextureAtlasSprite sprite, int bakeFlags) {
        TextureHelper.bakeSprite(this, sprite, bakeFlags);
        return this;
    }

    @Override
    public MutableQuadViewImpl lightmap(int vertexIndex, int lightmap) {
        this.data[this.baseIndex + vertexIndex * EncodingFormat.VERTEX_STRIDE + EncodingFormat.VERTEX_LIGHTMAP] = lightmap;
        return this;
    }

    protected void normalFlags(int flags) {
        this.data[this.baseIndex + 0] = EncodingFormat.normalFlags(this.data[this.baseIndex + 0], flags);
    }

    @Override
    public MutableQuadViewImpl normal(int vertexIndex, float x, float y, float z) {
        this.normalFlags(this.normalFlags() | 1 << vertexIndex);
        this.data[this.baseIndex + vertexIndex * EncodingFormat.VERTEX_STRIDE + EncodingFormat.VERTEX_NORMAL] = NormalHelper.packNormal(x, y, z, 0.0f);
        return this;
    }

    public final void populateMissingNormals() {
        int normalFlags = this.normalFlags();
        if (normalFlags == 15) {
            return;
        }
        int packedFaceNormal = NormalHelper.packNormal(this.faceNormal(), 0.0f);
        for (int v = 0; v < 4; ++v) {
            if ((normalFlags & 1 << v) != 0) continue;
            this.data[this.baseIndex + v * EncodingFormat.VERTEX_STRIDE + EncodingFormat.VERTEX_NORMAL] = packedFaceNormal;
        }
        this.normalFlags(15);
    }

    @Override
    public final MutableQuadViewImpl cullFace(@Nullable Direction face) {
        this.data[this.baseIndex + 0] = EncodingFormat.cullFace(this.data[this.baseIndex + 0], face);
        this.nominalFace(face);
        return this;
    }

    @Override
    public final MutableQuadViewImpl nominalFace(@Nullable Direction face) {
        this.nominalFace = face;
        return this;
    }

    @Override
    public final MutableQuadViewImpl colorIndex(int colorIndex) {
        this.data[this.baseIndex + 1] = colorIndex;
        return this;
    }

    @Override
    public final MutableQuadViewImpl tag(int tag) {
        this.data[this.baseIndex + 2] = tag;
        return this;
    }

    @Override
    public final MutableQuadViewImpl fromVanilla(int[] quadData, int startIndex) {
        System.arraycopy(quadData, startIndex, this.data, this.baseIndex + 3, 32);
        this.isGeometryInvalid = true;
        int colorIndex = this.baseIndex + EncodingFormat.VERTEX_COLOR;
        for (int i = 0; i < 4; ++i) {
            this.data[colorIndex] = ColorHelper.fromVanillaColor(this.data[colorIndex]);
            colorIndex += EncodingFormat.VERTEX_STRIDE;
        }
        return this;
    }

    @Override
    public final MutableQuadViewImpl fromVanilla(BakedQuad quad, @Nullable Direction cullFace) {
        this.fromVanilla(quad.getVertices(), 0);
        this.data[this.baseIndex + 0] = EncodingFormat.cullFace(0, cullFace);
        this.nominalFace(quad.getDirection());
        this.colorIndex(quad.getTintIndex());
        this.shade(quad.isShade());
        this.ambientOcclusion(quad.hasAmbientOcclusion());
        this.tag(0);
        return this;
    }
}

