/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.phys.AABB
 */
package appeng.api.util;

import net.minecraft.world.phys.AABB;

public class AEAxisAlignedBB {
    public double minX;
    public double minY;
    public double minZ;
    public double maxX;
    public double maxY;
    public double maxZ;

    public AABB getBoundingBox() {
        return new AABB(this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ);
    }

    public AEAxisAlignedBB(double a, double b, double c, double d, double e, double f) {
        this.minX = a;
        this.minY = b;
        this.minZ = c;
        this.maxX = d;
        this.maxY = e;
        this.maxZ = f;
    }

    public static AEAxisAlignedBB fromBounds(double a, double b, double c, double d, double e, double f) {
        return new AEAxisAlignedBB(a, b, c, d, e, f);
    }

    public static AEAxisAlignedBB fromBounds(AABB bb) {
        return new AEAxisAlignedBB(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ);
    }
}

