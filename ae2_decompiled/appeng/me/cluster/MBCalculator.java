/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.BlockPos$MutableBlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.entity.BlockEntity
 */
package appeng.me.cluster;

import appeng.me.cluster.IAECluster;
import appeng.me.cluster.IAEMultiBlock;
import java.lang.ref.WeakReference;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public abstract class MBCalculator<TBlockEntity extends IAEMultiBlock<TCluster>, TCluster extends IAECluster> {
    private static WeakReference<IAECluster> modificationInProgress = new WeakReference<Object>(null);
    protected final TBlockEntity target;

    public MBCalculator(TBlockEntity t) {
        this.target = t;
    }

    public static void setModificationInProgress(IAECluster cluster) {
        IAECluster inProgress = (IAECluster)modificationInProgress.get();
        if (inProgress == cluster) {
            return;
        }
        if (inProgress != null && cluster != null) {
            throw new IllegalStateException("A modification is already in-progress for: " + String.valueOf(inProgress));
        }
        modificationInProgress = new WeakReference<IAECluster>(cluster);
    }

    public static boolean isModificationInProgress() {
        return modificationInProgress.get() != null;
    }

    public void updateMultiblockAfterNeighborUpdate(ServerLevel level, BlockPos loc, BlockPos changedPos) {
        Object cluster = this.target.getCluster();
        boolean recheck = cluster != null ? (MBCalculator.isWithinBounds(changedPos, cluster.getBoundsMin(), cluster.getBoundsMax()) ? true : this.isValidBlockEntityAt((Level)level, changedPos.getX(), changedPos.getY(), changedPos.getZ())) : true;
        if (recheck) {
            this.calculateMultiblock(level, loc);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void calculateMultiblock(ServerLevel level, BlockPos loc) {
        block18: {
            if (MBCalculator.isModificationInProgress()) {
                return;
            }
            Object currentCluster = this.target.getCluster();
            if (currentCluster != null && currentCluster.isDestroyed()) {
                return;
            }
            try {
                BlockPos.MutableBlockPos min = loc.mutable();
                BlockPos.MutableBlockPos max = loc.mutable();
                while (this.isValidBlockEntityAt((Level)level, min.getX() - 1, min.getY(), min.getZ())) {
                    min.setX(min.getX() - 1);
                }
                while (this.isValidBlockEntityAt((Level)level, min.getX(), min.getY() - 1, min.getZ())) {
                    min.setY(min.getY() - 1);
                }
                while (this.isValidBlockEntityAt((Level)level, min.getX(), min.getY(), min.getZ() - 1)) {
                    min.setZ(min.getZ() - 1);
                }
                while (this.isValidBlockEntityAt((Level)level, max.getX() + 1, max.getY(), max.getZ())) {
                    max.setX(max.getX() + 1);
                }
                while (this.isValidBlockEntityAt((Level)level, max.getX(), max.getY() + 1, max.getZ())) {
                    max.setY(max.getY() + 1);
                }
                while (this.isValidBlockEntityAt((Level)level, max.getX(), max.getY(), max.getZ() + 1)) {
                    max.setZ(max.getZ() + 1);
                }
                if (!this.checkMultiblockScale((BlockPos)min, (BlockPos)max) || !this.verifyUnownedRegion(level, (BlockPos)min, (BlockPos)max)) break block18;
                try {
                    if (!this.verifyInternalStructure(level, (BlockPos)min, (BlockPos)max)) {
                        this.disconnect();
                        return;
                    }
                }
                catch (Exception err) {
                    this.disconnect();
                    return;
                }
                boolean updateGrid = false;
                Object cluster = this.target.getCluster();
                if (cluster == null || !cluster.getBoundsMin().equals((Object)min) || !cluster.getBoundsMax().equals((Object)max)) {
                    cluster = this.createCluster(level, (BlockPos)min, (BlockPos)max);
                    MBCalculator.setModificationInProgress(cluster);
                    this.updateBlockEntities(cluster, level, (BlockPos)min, (BlockPos)max);
                    updateGrid = true;
                } else {
                    MBCalculator.setModificationInProgress(cluster);
                }
                cluster.updateStatus(updateGrid);
                return;
            }
            finally {
                MBCalculator.setModificationInProgress(null);
            }
        }
        this.disconnect();
    }

    private static boolean isWithinBounds(BlockPos pos, BlockPos boundsMin, BlockPos boundsMax) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        return x >= boundsMin.getX() && y >= boundsMin.getY() && z >= boundsMin.getZ() && x <= boundsMax.getX() && y <= boundsMax.getY() && z <= boundsMax.getZ();
    }

    private boolean isValidBlockEntityAt(Level level, int x, int y, int z) {
        return this.isValidBlockEntity(level.getBlockEntity(new BlockPos(x, y, z)));
    }

    public abstract boolean checkMultiblockScale(BlockPos var1, BlockPos var2);

    private boolean verifyUnownedRegion(ServerLevel level, BlockPos min, BlockPos max) {
        for (Direction side : Direction.values()) {
            if (!this.verifyUnownedRegionInner(level, min.getX(), min.getY(), min.getZ(), max.getX(), max.getY(), max.getZ(), side)) continue;
            return false;
        }
        return true;
    }

    public abstract TCluster createCluster(ServerLevel var1, BlockPos var2, BlockPos var3);

    public abstract boolean verifyInternalStructure(ServerLevel var1, BlockPos var2, BlockPos var3);

    public void disconnect() {
        this.target.disconnect(true);
    }

    public abstract void updateBlockEntities(TCluster var1, ServerLevel var2, BlockPos var3, BlockPos var4);

    public abstract boolean isValidBlockEntity(BlockEntity var1);

    private boolean verifyUnownedRegionInner(ServerLevel level, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, Direction side) {
        switch (side) {
            case WEST: {
                maxX = --minX;
                break;
            }
            case EAST: {
                minX = ++maxX;
                break;
            }
            case DOWN: {
                maxY = --minY;
                break;
            }
            case NORTH: {
                minZ = ++maxZ;
                break;
            }
            case SOUTH: {
                maxZ = --minZ;
                break;
            }
            case UP: {
                minY = ++maxY;
                break;
            }
            default: {
                return false;
            }
        }
        for (BlockPos p : BlockPos.betweenClosed((int)minX, (int)minY, (int)minZ, (int)maxX, (int)maxY, (int)maxZ)) {
            BlockEntity te = level.getBlockEntity(p);
            if (!this.isValidBlockEntity(te)) continue;
            return true;
        }
        return false;
    }
}

