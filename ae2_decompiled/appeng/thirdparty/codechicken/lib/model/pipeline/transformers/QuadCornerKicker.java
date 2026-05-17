/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Direction$AxisDirection
 *  net.minecraft.core.Vec3i
 *  net.minecraft.world.phys.AABB
 */
package appeng.thirdparty.codechicken.lib.model.pipeline.transformers;

import appeng.thirdparty.fabric.MutableQuadView;
import appeng.thirdparty.fabric.RenderContext;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.AABB;

public class QuadCornerKicker
implements RenderContext.QuadTransform {
    public static final QuadCornerKicker INSTANCE = new QuadCornerKicker();
    public static int[][] horizonals = new int[][]{{2, 3, 4, 5}, {2, 3, 4, 5}, {0, 1, 4, 5}, {0, 1, 4, 5}, {0, 1, 2, 3}, {0, 1, 2, 3}};
    private int mySide;
    private int facadeMask;
    private AABB box;
    private double thickness;
    private static final double EPSILON = 1.0E-5;

    public void setSide(int side) {
        this.mySide = side;
    }

    public void setFacadeMask(int mask) {
        this.facadeMask = mask;
    }

    public void setBox(AABB box) {
        this.box = box;
    }

    public void setThickness(double thickness) {
        this.thickness = thickness;
    }

    @Override
    public boolean transform(MutableQuadView quad) {
        int side = quad.nominalFace().ordinal();
        if (side != this.mySide && side != (this.mySide ^ 1)) {
            for (int hoz : horizonals[this.mySide]) {
                if (side == hoz || side == (hoz ^ 1) || (this.facadeMask & 1 << hoz) == 0) continue;
                Corner corner = Corner.fromSides(this.mySide ^ 1, side, hoz);
                for (int i = 0; i < 4; ++i) {
                    float x = quad.posByIndex(i, 0);
                    float y = quad.posByIndex(i, 1);
                    float z = quad.posByIndex(i, 2);
                    if (!QuadCornerKicker.epsComp(x, corner.pX(this.box)) || !QuadCornerKicker.epsComp(y, corner.pY(this.box)) || !QuadCornerKicker.epsComp(z, corner.pZ(this.box))) continue;
                    Vec3i vec = Direction.values()[hoz].getNormal();
                    x = (float)((double)x - (double)vec.getX() * this.thickness);
                    y = (float)((double)y - (double)vec.getY() * this.thickness);
                    z = (float)((double)z - (double)vec.getZ() * this.thickness);
                    quad.pos(i, x, y, z);
                }
            }
        }
        return true;
    }

    private static boolean epsComp(float a, float b) {
        if (a == b) {
            return true;
        }
        return (double)Math.abs(a - b) < 1.0E-5;
    }

    public static enum Corner {
        MIN_X_MIN_Y_MIN_Z(Direction.AxisDirection.NEGATIVE, Direction.AxisDirection.NEGATIVE, Direction.AxisDirection.NEGATIVE),
        MIN_X_MIN_Y_MAX_Z(Direction.AxisDirection.NEGATIVE, Direction.AxisDirection.NEGATIVE, Direction.AxisDirection.POSITIVE),
        MIN_X_MAX_Y_MIN_Z(Direction.AxisDirection.NEGATIVE, Direction.AxisDirection.POSITIVE, Direction.AxisDirection.NEGATIVE),
        MIN_X_MAX_Y_MAX_Z(Direction.AxisDirection.NEGATIVE, Direction.AxisDirection.POSITIVE, Direction.AxisDirection.POSITIVE),
        MAX_X_MIN_Y_MIN_Z(Direction.AxisDirection.POSITIVE, Direction.AxisDirection.NEGATIVE, Direction.AxisDirection.NEGATIVE),
        MAX_X_MIN_Y_MAX_Z(Direction.AxisDirection.POSITIVE, Direction.AxisDirection.NEGATIVE, Direction.AxisDirection.POSITIVE),
        MAX_X_MAX_Y_MIN_Z(Direction.AxisDirection.POSITIVE, Direction.AxisDirection.POSITIVE, Direction.AxisDirection.NEGATIVE),
        MAX_X_MAX_Y_MAX_Z(Direction.AxisDirection.POSITIVE, Direction.AxisDirection.POSITIVE, Direction.AxisDirection.POSITIVE);

        private Direction.AxisDirection xAxis;
        private Direction.AxisDirection yAxis;
        private Direction.AxisDirection zAxis;
        private static final int[] sideMask;

        private Corner(Direction.AxisDirection xAxis, Direction.AxisDirection yAxis, Direction.AxisDirection zAxis) {
            this.xAxis = xAxis;
            this.yAxis = yAxis;
            this.zAxis = zAxis;
        }

        public static Corner fromSides(int sideA, int sideB, int sideC) {
            return Corner.values()[sideMask[sideA] | sideMask[sideB] | sideMask[sideC]];
        }

        public float pX(AABB box) {
            return (float)(this.xAxis == Direction.AxisDirection.NEGATIVE ? box.minX : box.maxX);
        }

        public float pY(AABB box) {
            return (float)(this.yAxis == Direction.AxisDirection.NEGATIVE ? box.minY : box.maxY);
        }

        public float pZ(AABB box) {
            return (float)(this.zAxis == Direction.AxisDirection.NEGATIVE ? box.minZ : box.maxZ);
        }

        static {
            sideMask = new int[]{0, 2, 0, 1, 0, 4};
        }
    }
}

