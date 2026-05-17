/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Vec3i
 *  net.minecraft.util.Mth
 *  org.jetbrains.annotations.NotNull
 *  org.joml.Vector3f
 */
package appeng.thirdparty.fabric;

import appeng.thirdparty.fabric.GeometryHelper;
import appeng.thirdparty.fabric.QuadView;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

public abstract class NormalHelper {
    private NormalHelper() {
    }

    public static int packNormal(float x, float y, float z, float w) {
        x = Mth.clamp((float)x, (float)-1.0f, (float)1.0f);
        y = Mth.clamp((float)y, (float)-1.0f, (float)1.0f);
        z = Mth.clamp((float)z, (float)-1.0f, (float)1.0f);
        w = Mth.clamp((float)w, (float)-1.0f, (float)1.0f);
        return (int)(x * 127.0f) & 0xFF | ((int)(y * 127.0f) & 0xFF) << 8 | ((int)(z * 127.0f) & 0xFF) << 16 | ((int)(w * 127.0f) & 0xFF) << 24;
    }

    public static int packNormal(Vector3f normal, float w) {
        return NormalHelper.packNormal(normal.x(), normal.y(), normal.z(), w);
    }

    public static float getPackedNormalComponent(int packedNormal, int component) {
        return (float)((byte)(packedNormal >> 8 * component)) / 127.0f;
    }

    public static void computeFaceNormal(@NotNull Vector3f saveTo, QuadView q) {
        float normZ;
        float dx0;
        float dx1;
        float normY;
        float dy1;
        float dz0;
        Direction nominalFace = q.nominalFace();
        if (GeometryHelper.isQuadParallelToFace(nominalFace, q)) {
            Vec3i vec = nominalFace.getNormal();
            saveTo.set((float)vec.getX(), (float)vec.getY(), (float)vec.getZ());
            return;
        }
        float x0 = q.x(0);
        float y0 = q.y(0);
        float z0 = q.z(0);
        float x1 = q.x(1);
        float y1 = q.y(1);
        float z1 = q.z(1);
        float x2 = q.x(2);
        float y2 = q.y(2);
        float z2 = q.z(2);
        float x3 = q.x(3);
        float y3 = q.y(3);
        float dy0 = y2 - y0;
        float z3 = q.z(3);
        float dz1 = z3 - z1;
        float normX = dy0 * dz1 - (dz0 = z2 - z0) * (dy1 = y3 - y1);
        float l = (float)Math.sqrt(normX * normX + (normY = dz0 * (dx1 = x3 - x1) - (dx0 = x2 - x0) * dz1) * normY + (normZ = dx0 * dy1 - dy0 * dx1) * normZ);
        if (l != 0.0f) {
            normX /= l;
            normY /= l;
            normZ /= l;
        }
        saveTo.set(normX, normY, normZ);
    }
}

