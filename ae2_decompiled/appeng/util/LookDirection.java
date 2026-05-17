/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.phys.Vec3
 */
package appeng.util;

import net.minecraft.world.phys.Vec3;

public class LookDirection {
    private final Vec3 a;
    private final Vec3 b;

    public LookDirection(Vec3 a, Vec3 b) {
        this.a = a;
        this.b = b;
    }

    public Vec3 getA() {
        return this.a;
    }

    public Vec3 getB() {
        return this.b;
    }
}

