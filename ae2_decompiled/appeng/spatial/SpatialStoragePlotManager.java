/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Vec3i
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.saveddata.SavedData$Factory
 *  org.jetbrains.annotations.Nullable
 */
package appeng.spatial;

import appeng.core.AELog;
import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.spatial.SpatialStorageDimensionIds;
import appeng.spatial.SpatialStoragePlot;
import appeng.spatial.SpatialStorageWorldData;
import appeng.spatial.TransitionInfo;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.Nullable;

public final class SpatialStoragePlotManager {
    private static final SavedData.Factory<SpatialStorageWorldData> FACTORY = new SavedData.Factory(SpatialStorageWorldData::new, SpatialStorageWorldData::load, null);
    public static final SpatialStoragePlotManager INSTANCE = new SpatialStoragePlotManager();

    private SpatialStoragePlotManager() {
    }

    public ServerLevel getLevel() {
        MinecraftServer server = AppEng.instance().getCurrentServer();
        if (server == null) {
            throw new IllegalStateException("No server is currently running.");
        }
        ServerLevel level = server.getLevel(SpatialStorageDimensionIds.WORLD_ID);
        if (level == null) {
            throw new IllegalStateException("The storage cell level is missing.");
        }
        return level;
    }

    private SpatialStorageWorldData getWorldData() {
        return (SpatialStorageWorldData)this.getLevel().getChunkSource().getDataStorage().computeIfAbsent(FACTORY, "ae2_spatial_storage");
    }

    @Nullable
    public SpatialStoragePlot getPlot(int plotId) {
        if (plotId == -1) {
            return null;
        }
        return this.getWorldData().getPlotById(plotId);
    }

    public SpatialStoragePlot allocatePlot(BlockPos size, int ownerId) {
        SpatialStoragePlot plot = this.getWorldData().allocatePlot(size, ownerId);
        AELog.info("Allocating storage cell plot %d with size %s for %d", plot.getId(), size, ownerId);
        return plot;
    }

    public void setLastTransition(int plotId, TransitionInfo info) {
        this.getWorldData().setLastTransition(plotId, info);
    }

    public List<SpatialStoragePlot> getPlots() {
        return this.getWorldData().getPlots();
    }

    public void freePlot(int plotId, boolean resetBlocks) {
        SpatialStoragePlot plot = this.getPlot(plotId);
        if (plot == null) {
            return;
        }
        if (resetBlocks) {
            BlockPos from = plot.getOrigin();
            BlockPos to = from.offset((Vec3i)plot.getSize()).offset(-1, -1, -1);
            AELog.info("Clearing spatial storage plot %s (%s -> %s)", plotId, from, to);
            ServerLevel level = this.getLevel();
            BlockState matrixFrame = AEBlocks.MATRIX_FRAME.block().defaultBlockState();
            for (BlockPos blockPos : BlockPos.betweenClosed((BlockPos)from, (BlockPos)to)) {
                level.setBlockAndUpdate(blockPos, matrixFrame);
            }
        }
        this.getWorldData().removePlot(plotId);
    }
}

