/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.level.Level
 */
package appeng.api.networking.events;

import appeng.api.networking.events.GridEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class GridSpatialEvent
extends GridEvent {
    public final Level spatialIoLevel;
    public final BlockPos spatialIoPos;
    public final double spatialEnergyUsage;
    private boolean preventTransition;

    public GridSpatialEvent(Level spatialIoLevel, BlockPos spatialIoPos, double EnergyUsage) {
        this.spatialIoLevel = spatialIoLevel;
        this.spatialIoPos = spatialIoPos;
        this.spatialEnergyUsage = EnergyUsage;
    }

    public void preventTransition() {
        this.preventTransition = true;
    }

    public boolean isTransitionPrevented() {
        return this.preventTransition;
    }
}

