/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.Mth
 *  net.minecraft.world.phys.AABB
 *  org.joml.Vector3f
 */
package appeng.thirdparty.codechicken.lib.model.pipeline.transformers;

import appeng.thirdparty.fabric.MutableQuadView;
import appeng.thirdparty.fabric.RenderContext;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3f;

public class QuadClamper
implements RenderContext.QuadTransform {
    private final AABB clampBounds;
    private final Vector3f pos = new Vector3f();

    public QuadClamper(AABB clampBounds) {
        this.clampBounds = clampBounds;
    }

    @Override
    public boolean transform(MutableQuadView quad) {
        int s = quad.nominalFace().ordinal() >> 1;
        this.clamp(quad, this.clampBounds);
        float x1 = quad.posByIndex(0, QuadClamper.dx(s));
        float x2 = quad.posByIndex(1, QuadClamper.dx(s));
        float x3 = quad.posByIndex(2, QuadClamper.dx(s));
        float x4 = quad.posByIndex(3, QuadClamper.dx(s));
        float y1 = quad.posByIndex(0, QuadClamper.dy(s));
        float y2 = quad.posByIndex(1, QuadClamper.dy(s));
        float y3 = quad.posByIndex(2, QuadClamper.dy(s));
        float y4 = quad.posByIndex(3, QuadClamper.dy(s));
        boolean flag1 = x1 == x2 && x2 == x3 && x3 == x4;
        boolean flag2 = y1 == y2 && y2 == y3 && y3 == y4;
        return !flag1 && !flag2;
    }

    private void clamp(MutableQuadView quad, AABB bb) {
        for (int i = 0; i < 4; ++i) {
            quad.copyPos(i, this.pos);
            this.pos.set((float)Mth.clamp((double)this.pos.x(), (double)bb.minX, (double)bb.maxX), (float)Mth.clamp((double)this.pos.y(), (double)bb.minY, (double)bb.maxY), (float)Mth.clamp((double)this.pos.z(), (double)bb.minZ, (double)bb.maxZ));
            quad.pos(i, this.pos);
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

