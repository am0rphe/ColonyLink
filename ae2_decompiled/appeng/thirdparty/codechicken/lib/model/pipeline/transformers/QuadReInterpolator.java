/*
 * Decompiled with CFR 0.152.
 */
package appeng.thirdparty.codechicken.lib.model.pipeline.transformers;

import appeng.thirdparty.codechicken.lib.math.InterpHelper;
import appeng.thirdparty.fabric.MutableQuadView;
import appeng.thirdparty.fabric.QuadView;
import appeng.thirdparty.fabric.RenderContext;

public class QuadReInterpolator
implements RenderContext.QuadTransform {
    private final InterpHelper interpHelper = new InterpHelper();
    private final int[] originalSpriteColor = new int[4];
    private final float[] originalSpriteU = new float[4];
    private final float[] originalSpriteV = new float[4];

    public void setInputQuad(QuadView quad) {
        int s = quad.nominalFace().ordinal() >> 1;
        int xIdx = QuadReInterpolator.dx(s);
        int yIdx = QuadReInterpolator.dy(s);
        this.interpHelper.reset(quad.posByIndex(0, xIdx), quad.posByIndex(0, yIdx), quad.posByIndex(1, xIdx), quad.posByIndex(1, yIdx), quad.posByIndex(2, xIdx), quad.posByIndex(2, yIdx), quad.posByIndex(3, xIdx), quad.posByIndex(3, yIdx));
        for (int i = 0; i < 4; ++i) {
            this.originalSpriteColor[i] = quad.color(i);
            this.originalSpriteU[i] = quad.u(i);
            this.originalSpriteV[i] = quad.v(i);
        }
    }

    @Override
    public boolean transform(MutableQuadView quad) {
        int s = quad.nominalFace().ordinal() >> 1;
        int xIdx = QuadReInterpolator.dx(s);
        int yIdx = QuadReInterpolator.dy(s);
        this.interpHelper.setup();
        for (int i = 0; i < 4; ++i) {
            float x = quad.posByIndex(i, xIdx);
            float y = quad.posByIndex(i, yIdx);
            this.interpHelper.locate(x, y);
            this.interpColorFrom(quad, i);
            this.interpUVFrom(quad, i);
            this.interpLightMapFrom(quad, i);
        }
        return true;
    }

    public void interpColorFrom(MutableQuadView quad, int vertexIndex) {
        int p1 = this.originalSpriteColor[0];
        int p2 = this.originalSpriteColor[1];
        int p3 = this.originalSpriteColor[2];
        int p4 = this.originalSpriteColor[3];
        if (p1 == p2 && p2 == p3 && p3 == p4) {
            return;
        }
        int color = 0;
        int mask = 255;
        for (int i = 0; i < 4; ++i) {
            float p1c = p1 & mask;
            float p2c = p2 & mask;
            float p3c = p3 & mask;
            float p4c = p4 & mask;
            int interp = (int)this.interpHelper.interpolate(p1c, p2c, p3c, p4c);
            color |= interp & mask;
            mask <<= 8;
        }
        quad.color(vertexIndex, color);
    }

    public void interpUVFrom(MutableQuadView quad, int vertexIndex) {
        float p1 = this.originalSpriteU[0];
        float p2 = this.originalSpriteU[1];
        float p3 = this.originalSpriteU[2];
        float p4 = this.originalSpriteU[3];
        float u = this.interpHelper.interpolate(p1, p2, p3, p4);
        p1 = this.originalSpriteV[0];
        p2 = this.originalSpriteV[1];
        p3 = this.originalSpriteV[2];
        p4 = this.originalSpriteV[3];
        float v = this.interpHelper.interpolate(p1, p2, p3, p4);
        quad.uv(vertexIndex, u, v);
    }

    public void interpLightMapFrom(MutableQuadView quad, int vertexIndex) {
        for (int e = 0; e < 2; ++e) {
        }
    }

    private static int dx(int s) {
        if (s <= 1) {
            return 0;
        }
        return 2;
    }

    private static int dy(int s) {
        if (s > 0) {
            return 1;
        }
        return 2;
    }
}

