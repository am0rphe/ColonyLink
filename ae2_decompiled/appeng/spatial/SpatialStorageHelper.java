/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.longs.LongOpenHashSet
 *  it.unimi.dsi.fastutil.longs.LongSet
 *  net.minecraft.core.BlockPos
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.level.ChunkPos
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.chunk.status.ChunkStatus
 *  net.minecraft.world.level.entity.PersistentEntitySectionManager
 *  net.minecraft.world.level.entity.Visibility
 *  net.minecraft.world.level.portal.DimensionTransition
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 */
package appeng.spatial;

import appeng.block.spatial.MatrixFrameBlock;
import appeng.core.definitions.AEBlocks;
import appeng.core.stats.AdvancementTriggers;
import appeng.spatial.CachedPlane;
import appeng.spatial.ISpatialVisitor;
import appeng.spatial.SpatialStorageDimensionIds;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import net.minecraft.world.level.entity.Visibility;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class SpatialStorageHelper {
    private static SpatialStorageHelper instance;

    public static SpatialStorageHelper getInstance() {
        if (instance == null) {
            instance = new SpatialStorageHelper();
        }
        return instance;
    }

    private Entity teleportEntity(Entity entity, TelDestination link) {
        ServerLevel newLevel;
        ServerLevel oldLevel;
        try {
            oldLevel = (ServerLevel)entity.level();
            newLevel = link.dim;
        }
        catch (Throwable e) {
            return entity;
        }
        if (oldLevel == null) {
            return entity;
        }
        if (newLevel == null) {
            return entity;
        }
        if (newLevel == oldLevel) {
            newLevel.getChunkSource().getChunk(Mth.floor((double)link.x) >> 4, Mth.floor((double)link.z) >> 4, ChunkStatus.FULL, true);
            entity.teleportTo(link.x, link.y, link.z);
            return entity;
        }
        if (entity.isPassenger()) {
            return this.teleportEntity(entity.getVehicle(), link);
        }
        List passengers = entity.getPassengers();
        ArrayList<Entity> passengersOnOtherSide = new ArrayList<Entity>(passengers.size());
        for (Entity passenger : passengers) {
            passenger.stopRiding();
            passengersOnOtherSide.add(this.teleportEntity(passenger, link));
        }
        newLevel.getChunkSource().getChunk(Mth.floor((double)link.x) >> 4, Mth.floor((double)link.z) >> 4, ChunkStatus.FULL, true);
        if (entity instanceof ServerPlayer && link.dim.dimension() == SpatialStorageDimensionIds.WORLD_ID) {
            AdvancementTriggers.SPATIAL_EXPLORER.trigger((ServerPlayer)entity);
        }
        entity.changeDimension(new DimensionTransition(newLevel, new Vec3(link.x, link.y, link.z), Vec3.ZERO, entity.getYRot(), entity.getXRot(), transportedEntity -> {
            if (!passengersOnOtherSide.isEmpty()) {
                for (Entity passanger : passengersOnOtherSide) {
                    passanger.startRiding(transportedEntity, true);
                }
            }
        }));
        return entity;
    }

    private void transverseEdges(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, ISpatialVisitor visitor) {
        int x;
        int z;
        for (int y = minY; y < maxY; ++y) {
            for (z = minZ; z < maxZ; ++z) {
                visitor.visit(new BlockPos(minX, y, z));
                visitor.visit(new BlockPos(maxX, y, z));
            }
        }
        for (x = minX; x < maxX; ++x) {
            for (z = minZ; z < maxZ; ++z) {
                visitor.visit(new BlockPos(x, minY, z));
                visitor.visit(new BlockPos(x, maxY, z));
            }
        }
        for (x = minX; x < maxX; ++x) {
            for (int y = minY; y < maxY; ++y) {
                visitor.visit(new BlockPos(x, y, minZ));
                visitor.visit(new BlockPos(x, y, maxZ));
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void swapRegions(ServerLevel srcLevel, int srcX, int srcY, int srcZ, ServerLevel dstLevel, int dstX, int dstY, int dstZ, int scaleX, int scaleY, int scaleZ) {
        MatrixFrameBlock matrixFrameBlock = AEBlocks.MATRIX_FRAME.block();
        this.transverseEdges(dstX - 1, dstY - 1, dstZ - 1, dstX + scaleX + 1, dstY + scaleY + 1, dstZ + scaleZ + 1, new WrapInMatrixFrame(matrixFrameBlock.defaultBlockState(), (Level)dstLevel));
        AABB srcBox = new AABB((double)srcX, (double)srcY, (double)srcZ, (double)(srcX + scaleX + 1), (double)(srcY + scaleY + 1), (double)(srcZ + scaleZ + 1));
        AABB dstBox = new AABB((double)dstX, (double)dstY, (double)dstZ, (double)(dstX + scaleX + 1), (double)(dstY + scaleY + 1), (double)(dstZ + scaleZ + 1));
        CachedPlane cDst = new CachedPlane(dstLevel, dstX, dstY, dstZ, dstX + scaleX, dstY + scaleY, dstZ + scaleZ);
        CachedPlane cSrc = new CachedPlane(srcLevel, srcX, srcY, srcZ, srcX + scaleX, srcY + scaleY, srcZ + scaleZ);
        cSrc.swap(cDst);
        LongSet loadedSrcChunks = this.loadEntityChunksSynchronously(srcLevel, srcBox);
        LongSet loadedDestChunks = this.loadEntityChunksSynchronously(dstLevel, dstBox);
        try {
            List srcE = srcLevel.getEntitiesOfClass(Entity.class, srcBox);
            List dstE = dstLevel.getEntitiesOfClass(Entity.class, dstBox);
            for (Entity e : dstE) {
                this.teleportEntity(e, new TelDestination(srcLevel, srcBox, e.getX(), e.getY(), e.getZ(), -dstX + srcX, -dstY + srcY, -dstZ + srcZ));
            }
            for (Entity e : srcE) {
                this.teleportEntity(e, new TelDestination(dstLevel, dstBox, e.getX(), e.getY(), e.getZ(), -srcX + dstX, -srcY + dstY, -srcZ + dstZ));
            }
        }
        finally {
            SpatialStorageHelper.unloadEntityChunks(srcLevel, loadedSrcChunks);
            SpatialStorageHelper.unloadEntityChunks(dstLevel, loadedDestChunks);
        }
        for (BlockPos pos : cDst.getUpdates()) {
            cSrc.getLevel().updateNeighborsAt(pos, Blocks.AIR);
        }
        for (BlockPos pos : cSrc.getUpdates()) {
            cSrc.getLevel().updateNeighborsAt(pos, Blocks.AIR);
        }
        this.transverseEdges(srcX - 1, srcY - 1, srcZ - 1, srcX + scaleX + 1, srcY + scaleY + 1, srcZ + scaleZ + 1, new TriggerUpdates((Level)srcLevel));
        this.transverseEdges(dstX - 1, dstY - 1, dstZ - 1, dstX + scaleX + 1, dstY + scaleY + 1, dstZ + scaleZ + 1, new TriggerUpdates((Level)dstLevel));
        this.transverseEdges(srcX, srcY, srcZ, srcX + scaleX, srcY + scaleY, srcZ + scaleZ, new TriggerUpdates((Level)srcLevel));
        this.transverseEdges(dstX, dstY, dstZ, dstX + scaleX, dstY + scaleY, dstZ + scaleZ, new TriggerUpdates((Level)dstLevel));
    }

    private LongSet loadEntityChunksSynchronously(ServerLevel level, AABB box) {
        ChunkPos minChunk = new ChunkPos(new BlockPos((int)box.minX, 0, (int)box.minZ));
        ChunkPos maxChunk = new ChunkPos(new BlockPos((int)Math.ceil(box.maxX), 0, (int)Math.ceil(box.maxZ)));
        LongOpenHashSet chunksLoaded = new LongOpenHashSet();
        PersistentEntitySectionManager entityManager = level.entityManager;
        ChunkPos.rangeClosed((ChunkPos)minChunk, (ChunkPos)maxChunk).forEach(chunkPos -> {
            Visibility status = (Visibility)entityManager.chunkVisibility.get(chunkPos.toLong());
            if (!status.isAccessible()) {
                chunksLoaded.add(chunkPos.toLong());
                entityManager.updateChunkStatus(chunkPos, Visibility.TRACKED);
            }
        });
        if (!chunksLoaded.isEmpty()) {
            entityManager.permanentStorage.flush(false);
            entityManager.tick();
        }
        return chunksLoaded;
    }

    private static void unloadEntityChunks(ServerLevel srcLevel, LongSet loadedSrcChunks) {
        loadedSrcChunks.forEach(chunkPos -> srcLevel.entityManager.updateChunkStatus(new ChunkPos(chunkPos), Visibility.HIDDEN));
    }

    private static class TelDestination {
        private final ServerLevel dim;
        private final double x;
        private final double y;
        private final double z;

        TelDestination(ServerLevel dimension, AABB srcBox, double x, double y, double z, int blockEntityX, int blockEntityY, int blockEntityZ) {
            this.dim = dimension;
            this.x = Math.min(srcBox.maxX - 0.5, Math.max(srcBox.minX + 0.5, x + (double)blockEntityX));
            this.y = Math.min(srcBox.maxY - 0.5, Math.max(srcBox.minY + 0.5, y + (double)blockEntityY));
            this.z = Math.min(srcBox.maxZ - 0.5, Math.max(srcBox.minZ + 0.5, z + (double)blockEntityZ));
        }
    }

    private static class WrapInMatrixFrame
    implements ISpatialVisitor {
        private final Level dst;
        private final BlockState state;

        public WrapInMatrixFrame(BlockState state, Level dst2) {
            this.dst = dst2;
            this.state = state;
        }

        @Override
        public void visit(BlockPos pos) {
            this.dst.setBlockAndUpdate(pos, this.state);
        }
    }

    private static class TriggerUpdates
    implements ISpatialVisitor {
        private final Level dst;

        public TriggerUpdates(Level dst2) {
            this.dst = dst2;
        }

        @Override
        public void visit(BlockPos pos) {
            BlockState state = this.dst.getBlockState(pos);
            state.handleNeighborChanged(this.dst, pos, state.getBlock(), pos, false);
        }
    }
}

