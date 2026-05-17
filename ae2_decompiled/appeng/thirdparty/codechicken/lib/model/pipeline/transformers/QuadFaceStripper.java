/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Direction$AxisDirection
 *  net.minecraft.world.phys.AABB
 */
package appeng.thirdparty.codechicken.lib.model.pipeline.transformers;

import appeng.thirdparty.fabric.MutableQuadView;
import appeng.thirdparty.fabric.RenderContext;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;

public class QuadFaceStripper
implements RenderContext.QuadTransform {
    private AABB bounds;
    private int mask;

    QuadFaceStripper() {
    }

    public QuadFaceStripper(AABB bounds, int mask) {
        this.bounds = bounds;
        this.mask = mask;
    }

    public void setBounds(AABB bounds) {
        this.bounds = bounds;
    }

    public void setMask(int mask) {
        this.mask = mask;
    }

    @Override
    public boolean transform(MutableQuadView quad) {
        if (this.mask == 0) {
            return true;
        }
        Direction face = quad.nominalFace();
        if ((this.mask & 1 << face.ordinal()) != 0) {
            Direction.AxisDirection dir = face.getAxisDirection();
            switch (face.getAxis()) {
                case X: {
                    float bound = (float)(dir == Direction.AxisDirection.POSITIVE ? this.bounds.maxX : this.bounds.minX);
                    float x1 = quad.posByIndex(0, 0);
                    float x2 = quad.posByIndex(1, 0);
                    float x3 = quad.posByIndex(2, 0);
                    float x4 = quad.posByIndex(3, 0);
                    return x1 != x2 || x2 != x3 || x3 != x4 || x4 != bound;
                }
                case Y: {
                    float bound = (float)(dir == Direction.AxisDirection.POSITIVE ? this.bounds.maxY : this.bounds.minY);
                    float y1 = quad.posByIndex(0, 1);
                    float y2 = quad.posByIndex(1, 1);
                    float y3 = quad.posByIndex(2, 1);
                    float y4 = quad.posByIndex(3, 1);
                    return y1 != y2 || y2 != y3 || y3 != y4 || y4 != bound;
                }
                case Z: {
                    float bound = (float)(dir == Direction.AxisDirection.POSITIVE ? this.bounds.maxZ : this.bounds.minZ);
                    float z1 = quad.posByIndex(0, 2);
                    float z2 = quad.posByIndex(1, 2);
                    float z3 = quad.posByIndex(2, 2);
                    float z4 = quad.posByIndex(3, 2);
                    return z1 != z2 || z2 != z3 || z3 != z4 || z4 != bound;
                }
            }
        }
        return true;
    }
}

