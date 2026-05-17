/*
 * Decompiled with CFR 0.152.
 */
package appeng.thirdparty.codechicken.lib.model.pipeline.transformers;

import appeng.thirdparty.fabric.MutableQuadView;
import appeng.thirdparty.fabric.RenderContext;

public class QuadTinter
implements RenderContext.QuadTransform {
    private final int argb;

    public QuadTinter(int rgb) {
        this.argb = 0xFF000000 | rgb;
    }

    @Override
    public boolean transform(MutableQuadView quad) {
        quad.colorIndex(-1);
        for (int i = 0; i < 4; ++i) {
            int color = quad.color(i);
            color = QuadTinter.multiplyColor(color, this.argb);
            quad.color(i, color);
        }
        return true;
    }

    private static int multiplyColor(int color1, int color2) {
        if (color1 == -1) {
            return color2;
        }
        if (color2 == -1) {
            return color1;
        }
        int alpha = (color1 >> 24 & 0xFF) * (color2 >> 24 & 0xFF) / 255;
        int red = (color1 >> 16 & 0xFF) * (color2 >> 16 & 0xFF) / 255;
        int green = (color1 >> 8 & 0xFF) * (color2 >> 8 & 0xFF) / 255;
        int blue = (color1 & 0xFF) * (color2 & 0xFF) / 255;
        return alpha << 24 | red << 16 | green << 8 | blue;
    }
}

