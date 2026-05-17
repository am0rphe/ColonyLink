/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Direction$Axis
 *  net.minecraft.core.Direction$AxisDirection
 *  net.minecraft.util.Mth
 *  org.jetbrains.annotations.NotNull
 *  org.joml.Vector3f
 */
package appeng.thirdparty.fabric;

import appeng.thirdparty.fabric.QuadView;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

public abstract class GeometryHelper {
    public static final int CUBIC_FLAG = 1;
    public static final int AXIS_ALIGNED_FLAG = 2;
    public static final int LIGHT_FACE_FLAG = 4;
    public static final int FLAG_BIT_COUNT = 3;
    private static final float EPS_MIN = 1.0E-4f;
    private static final float EPS_MAX = 0.9999f;

    private GeometryHelper() {
    }

    public static int computeShapeFlags(QuadView quad) {
        Direction lightFace = quad.lightFace();
        int bits = 0;
        if (GeometryHelper.isQuadParallelToFace(lightFace, quad)) {
            bits |= 2;
            if (GeometryHelper.isParallelQuadOnFace(lightFace, quad)) {
                bits |= 4;
            }
        }
        if (GeometryHelper.isQuadCubic(lightFace, quad)) {
            bits |= 1;
        }
        return bits;
    }

    public static boolean isQuadParallelToFace(Direction face, QuadView quad) {
        if (face == null) {
            return false;
        }
        int i = face.getAxis().ordinal();
        float val = quad.posByIndex(0, i);
        return Mth.equal((float)val, (float)quad.posByIndex(1, i)) && Mth.equal((float)val, (float)quad.posByIndex(2, i)) && Mth.equal((float)val, (float)quad.posByIndex(3, i));
    }

    public static boolean isParallelQuadOnFace(Direction lightFace, QuadView quad) {
        if (lightFace == null) {
            return false;
        }
        float x = quad.posByIndex(0, lightFace.getAxis().ordinal());
        return lightFace.getAxisDirection() == Direction.AxisDirection.POSITIVE ? x >= 0.9999f : x <= 1.0E-4f;
    }

    public static boolean isQuadCubic(@NotNull Direction lightFace, QuadView quad) {
        int b;
        int a;
        if (lightFace == null) {
            return false;
        }
        switch (lightFace) {
            case EAST: 
            case WEST: {
                a = 1;
                b = 2;
                break;
            }
            case UP: 
            case DOWN: {
                a = 0;
                b = 2;
                break;
            }
            case SOUTH: 
            case NORTH: {
                a = 1;
                b = 0;
                break;
            }
            default: {
                return false;
            }
        }
        return GeometryHelper.confirmSquareCorners(a, b, quad);
    }

    private static boolean confirmSquareCorners(int aCoordinate, int bCoordinate, QuadView quad) {
        int flags = 0;
        for (int i = 0; i < 4; ++i) {
            float a = quad.posByIndex(i, aCoordinate);
            float b = quad.posByIndex(i, bCoordinate);
            if (a <= 1.0E-4f) {
                if (b <= 1.0E-4f) {
                    flags |= 1;
                    continue;
                }
                if (b >= 0.9999f) {
                    flags |= 2;
                    continue;
                }
                return false;
            }
            if (a >= 0.9999f) {
                if (b <= 1.0E-4f) {
                    flags |= 4;
                    continue;
                }
                if (b >= 0.9999f) {
                    flags |= 8;
                    continue;
                }
                return false;
            }
            return false;
        }
        return flags == 15;
    }

    public static Direction lightFace(QuadView quad) {
        Vector3f normal = quad.faceNormal();
        switch (GeometryHelper.longestAxis(normal)) {
            case X: {
                return normal.x() > 0.0f ? Direction.EAST : Direction.WEST;
            }
            case Y: {
                return normal.y() > 0.0f ? Direction.UP : Direction.DOWN;
            }
            case Z: {
                return normal.z() > 0.0f ? Direction.SOUTH : Direction.NORTH;
            }
        }
        return Direction.UP;
    }

    public static float min(float a, float b, float c, float d) {
        float x = a < b ? a : b;
        float y = c < d ? c : d;
        return x < y ? x : y;
    }

    public static float max(float a, float b, float c, float d) {
        float x = a > b ? a : b;
        float y = c > d ? c : d;
        return x > y ? x : y;
    }

    public static Direction.Axis longestAxis(Vector3f vec) {
        return GeometryHelper.longestAxis(vec.x(), vec.y(), vec.z());
    }

    public static Direction.Axis longestAxis(float normalX, float normalY, float normalZ) {
        Direction.Axis result = Direction.Axis.Y;
        float longest = Math.abs(normalY);
        float a = Math.abs(normalX);
        if (a > longest) {
            result = Direction.Axis.X;
            longest = a;
        }
        return Math.abs(normalZ) > longest ? Direction.Axis.Z : result;
    }
}

