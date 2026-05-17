/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Direction
 */
package appeng.api.parts;

import net.minecraft.core.Direction;

public interface IPartCollisionHelper {
    public void addBox(double var1, double var3, double var5, double var7, double var9, double var11);

    public Direction getWorldX();

    public Direction getWorldY();

    public Direction getWorldZ();

    public boolean isBBCollision();
}

