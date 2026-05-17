/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.level.Level
 */
package appeng.api.networking.spatial;

import appeng.api.networking.IGridService;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface ISpatialService
extends IGridService {
    public boolean hasRegion();

    public boolean isValidRegion();

    public Level getLevel();

    public BlockPos getMin();

    public BlockPos getMax();

    public long requiredPower();

    public float currentEfficiency();
}

