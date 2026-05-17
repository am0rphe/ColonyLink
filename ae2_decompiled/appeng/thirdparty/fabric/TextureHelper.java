/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.texture.TextureAtlasSprite
 *  net.minecraft.core.Direction
 */
package appeng.thirdparty.fabric;

import appeng.thirdparty.fabric.MutableQuadView;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;

public class TextureHelper {
    private static final float NORMALIZER = 0.0625f;
    private static final VertexModifier[] ROTATIONS = new VertexModifier[]{null, (q, i) -> q.uv(i, q.v(i), 1.0f - q.u(i)), (q, i) -> q.uv(i, 1.0f - q.u(i), 1.0f - q.v(i)), (q, i) -> q.uv(i, 1.0f - q.v(i), q.u(i))};
    private static final VertexModifier[] UVLOCKERS = new VertexModifier[6];

    private TextureHelper() {
    }

    public static void bakeSprite(MutableQuadView quad, TextureAtlasSprite sprite, int bakeFlags) {
        if (quad.nominalFace() != null && (4 & bakeFlags) != 0) {
            TextureHelper.applyModifier(quad, UVLOCKERS[quad.nominalFace().get3DDataValue()]);
        } else if ((0x20 & bakeFlags) == 0) {
            TextureHelper.applyModifier(quad, (q, i) -> q.uv(i, q.u(i) * 0.0625f, q.v(i) * 0.0625f));
        }
        int rotation = bakeFlags & 3;
        if (rotation != 0) {
            TextureHelper.applyModifier(quad, ROTATIONS[rotation]);
        }
        if ((8 & bakeFlags) != 0) {
            TextureHelper.applyModifier(quad, (q, i) -> q.uv(i, 1.0f - q.u(i), q.v(i)));
        }
        if ((0x10 & bakeFlags) != 0) {
            TextureHelper.applyModifier(quad, (q, i) -> q.uv(i, q.u(i), 1.0f - q.v(i)));
        }
        TextureHelper.interpolate(quad, sprite);
    }

    private static void interpolate(MutableQuadView q, TextureAtlasSprite sprite) {
        float uMin = sprite.getU0();
        float uSpan = sprite.getU1() - uMin;
        float vMin = sprite.getV0();
        float vSpan = sprite.getV1() - vMin;
        for (int i = 0; i < 4; ++i) {
            q.uv(i, uMin + q.u(i) * uSpan, vMin + q.v(i) * vSpan);
        }
    }

    private static void applyModifier(MutableQuadView quad, VertexModifier modifier) {
        for (int i = 0; i < 4; ++i) {
            modifier.apply(quad, i);
        }
    }

    static {
        TextureHelper.UVLOCKERS[Direction.EAST.get3DDataValue()] = (q, i) -> q.uv(i, 1.0f - q.z(i), 1.0f - q.y(i));
        TextureHelper.UVLOCKERS[Direction.WEST.get3DDataValue()] = (q, i) -> q.uv(i, q.z(i), 1.0f - q.y(i));
        TextureHelper.UVLOCKERS[Direction.NORTH.get3DDataValue()] = (q, i) -> q.uv(i, 1.0f - q.x(i), 1.0f - q.y(i));
        TextureHelper.UVLOCKERS[Direction.SOUTH.get3DDataValue()] = (q, i) -> q.uv(i, q.x(i), 1.0f - q.y(i));
        TextureHelper.UVLOCKERS[Direction.DOWN.get3DDataValue()] = (q, i) -> q.uv(i, q.x(i), 1.0f - q.z(i));
        TextureHelper.UVLOCKERS[Direction.UP.get3DDataValue()] = (q, i) -> q.uv(i, q.x(i), q.z(i));
    }

    @FunctionalInterface
    private static interface VertexModifier {
        public void apply(MutableQuadView var1, int var2);
    }
}

