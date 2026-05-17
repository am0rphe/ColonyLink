/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.server.level.ServerLevel
 */
package appeng.me.cluster.implementations;

import appeng.blockentity.spatial.SpatialPylonBlockEntity;
import appeng.me.cluster.IAECluster;
import appeng.me.cluster.MBCalculator;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public class SpatialPylonCluster
implements IAECluster {
    private final ServerLevel level;
    private final BlockPos boundsMin;
    private final BlockPos boundsMax;
    private final List<SpatialPylonBlockEntity> line = new ArrayList<SpatialPylonBlockEntity>();
    private boolean isDestroyed = false;
    private Axis currentAxis = Axis.UNFORMED;
    private boolean isValid;

    public SpatialPylonCluster(ServerLevel level, BlockPos boundsMin, BlockPos boundsMax) {
        this.level = level;
        this.boundsMin = boundsMin.immutable();
        this.boundsMax = boundsMax.immutable();
        if (this.getBoundsMin().getX() != this.getBoundsMax().getX()) {
            this.setCurrentAxis(Axis.X);
        } else if (this.getBoundsMin().getY() != this.getBoundsMax().getY()) {
            this.setCurrentAxis(Axis.Y);
        } else if (this.getBoundsMin().getZ() != this.getBoundsMax().getZ()) {
            this.setCurrentAxis(Axis.Z);
        } else {
            this.setCurrentAxis(Axis.UNFORMED);
        }
    }

    @Override
    public void updateStatus(boolean updateGrid) {
        for (SpatialPylonBlockEntity r : this.getLine()) {
            r.recalculateDisplay();
        }
    }

    @Override
    public boolean isDestroyed() {
        return this.isDestroyed;
    }

    @Override
    public void destroy() {
        if (this.isDestroyed) {
            return;
        }
        this.isDestroyed = true;
        MBCalculator.setModificationInProgress(this);
        try {
            for (SpatialPylonBlockEntity r : this.getLine()) {
                r.updateStatus(null);
            }
        }
        finally {
            MBCalculator.setModificationInProgress(null);
        }
    }

    public Iterator<SpatialPylonBlockEntity> getBlockEntities() {
        return this.getLine().iterator();
    }

    public int size() {
        return this.getLine().size();
    }

    public Axis getCurrentAxis() {
        return this.currentAxis;
    }

    private void setCurrentAxis(Axis currentAxis) {
        this.currentAxis = currentAxis;
    }

    public boolean isValid() {
        return this.isValid;
    }

    public void setValid(boolean isValid) {
        this.isValid = isValid;
    }

    public ServerLevel setLevel() {
        return this.level;
    }

    @Override
    public BlockPos getBoundsMax() {
        return this.boundsMax;
    }

    @Override
    public BlockPos getBoundsMin() {
        return this.boundsMin;
    }

    List<SpatialPylonBlockEntity> getLine() {
        return this.line;
    }

    public static enum Axis {
        X,
        Y,
        Z,
        UNFORMED;

    }
}

