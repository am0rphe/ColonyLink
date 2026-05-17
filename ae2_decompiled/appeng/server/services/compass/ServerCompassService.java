/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.cache.CacheBuilder
 *  com.google.common.cache.CacheLoader
 *  com.google.common.cache.LoadingCache
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Vec3i
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.level.ChunkPos
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.chunk.ChunkAccess
 *  net.minecraft.world.level.chunk.LevelChunk
 *  net.minecraft.world.level.chunk.LevelChunkSection
 *  org.jetbrains.annotations.Nullable
 */
package appeng.server.services.compass;

import appeng.blockentity.misc.MysteriousCubeBlockEntity;
import appeng.core.definitions.AEBlocks;
import appeng.server.services.compass.CompassRegion;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.jetbrains.annotations.Nullable;

public final class ServerCompassService {
    private static final int MAX_RANGE = 174;
    private static final int CHUNK_SIZE = 16;
    private static final LoadingCache<Query, Optional<BlockPos>> CLOSEST_METEORITE_CACHE = CacheBuilder.newBuilder().maximumSize(100L).weakKeys().expireAfterWrite(5L, TimeUnit.SECONDS).build((CacheLoader)new CacheLoader<Query, Optional<BlockPos>>(){

        public Optional<BlockPos> load(Query query) {
            return Optional.ofNullable(ServerCompassService.findClosestMeteoritePos(query.level, query.chunk));
        }
    });

    public static Optional<BlockPos> getClosestMeteorite(ServerLevel level, ChunkPos chunkPos) {
        return (Optional)CLOSEST_METEORITE_CACHE.getUnchecked((Object)new Query(level, chunkPos));
    }

    @Nullable
    private static BlockPos findClosestMeteoritePos(ServerLevel level, ChunkPos originChunkPos) {
        ChunkPos chunkPos = ServerCompassService.findClosestMeteoriteChunk(level, originChunkPos);
        if (chunkPos == null) {
            return null;
        }
        LevelChunk chunk = level.getChunkSource().getChunk(chunkPos.x, chunkPos.z, false);
        if (chunk == null) {
            return chunkPos.getMiddleBlockPosition(0);
        }
        BlockPos sourcePos = originChunkPos.getMiddleBlockPosition(0);
        double closestDistanceSq = Double.MAX_VALUE;
        BlockPos chosenPos = chunkPos.getMiddleBlockPosition(0);
        for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
            BlockPos bePos;
            double distSq;
            if (!(blockEntity instanceof MysteriousCubeBlockEntity) || !((distSq = sourcePos.distSqr((Vec3i)(bePos = blockEntity.getBlockPos().atY(0)))) < closestDistanceSq)) continue;
            chosenPos = bePos;
            closestDistanceSq = distSq;
        }
        return chosenPos;
    }

    @Nullable
    private static ChunkPos findClosestMeteoriteChunk(ServerLevel level, ChunkPos chunkPos) {
        int cz;
        int cx;
        CompassRegion cr = CompassRegion.get(level, chunkPos);
        if (cr.hasCompassTarget(cx = chunkPos.x, cz = chunkPos.z)) {
            return chunkPos;
        }
        for (int offset = 1; offset < 174; ++offset) {
            int closeness;
            int minX = cx - offset;
            int minZ = cz - offset;
            int maxX = cx + offset;
            int maxZ = cz + offset;
            int closest = Integer.MAX_VALUE;
            int chosen_x = cx;
            int chosen_z = cz;
            for (int z = minZ; z <= maxZ; ++z) {
                if (cr.hasCompassTarget(minX, z) && (closeness = ServerCompassService.dist(cx, cz, minX, z)) < closest) {
                    closest = closeness;
                    chosen_x = minX;
                    chosen_z = z;
                }
                if (!cr.hasCompassTarget(maxX, z) || (closeness = ServerCompassService.dist(cx, cz, maxX, z)) >= closest) continue;
                closest = closeness;
                chosen_x = maxX;
                chosen_z = z;
            }
            for (int x = minX + 1; x < maxX; ++x) {
                if (cr.hasCompassTarget(x, minZ) && (closeness = ServerCompassService.dist(cx, cz, x, minZ)) < closest) {
                    closest = closeness;
                    chosen_x = x;
                    chosen_z = minZ;
                }
                if (!cr.hasCompassTarget(x, maxZ) || (closeness = ServerCompassService.dist(cx, cz, x, maxZ)) >= closest) continue;
                closest = closeness;
                chosen_x = x;
                chosen_z = maxZ;
            }
            if (closest >= Integer.MAX_VALUE) continue;
            return new ChunkPos(chosen_x, chosen_z);
        }
        return null;
    }

    public static void updateArea(ServerLevel level, ChunkAccess chunk) {
        CompassRegion compassRegion = CompassRegion.get(level, chunk.getPos());
        for (int i = 0; i < level.getSectionsCount(); ++i) {
            ServerCompassService.updateArea(compassRegion, chunk, i);
        }
    }

    public static void notifyBlockChange(ServerLevel level, BlockPos pos) {
        ChunkAccess chunk = level.getChunk(pos);
        CompassRegion compassRegion = CompassRegion.get(level, chunk.getPos());
        ServerCompassService.updateArea(compassRegion, chunk, level.getSectionIndex(pos.getY()));
    }

    private static void updateArea(CompassRegion compassRegion, ChunkAccess chunk, int sectionIndex) {
        int cx = chunk.getPos().x;
        int cz = chunk.getPos().z;
        LevelChunkSection section = chunk.getSections()[sectionIndex];
        if (section.hasOnlyAir()) {
            compassRegion.setHasCompassTarget(cx, cz, sectionIndex, false);
            return;
        }
        BlockState desiredState = AEBlocks.MYSTERIOUS_CUBE.block().defaultBlockState();
        AtomicInteger blockCount = new AtomicInteger(0);
        section.getStates().count((state, count) -> {
            if (state == desiredState) {
                blockCount.getAndIncrement();
            }
        });
        compassRegion.setHasCompassTarget(cx, cz, sectionIndex, blockCount.get() > 0);
    }

    private static int dist(int ax, int az, int bx, int bz) {
        int up = (bz - az) * 16;
        int side = (bx - ax) * 16;
        return up * up + side * side;
    }

    private record Query(ServerLevel level, ChunkPos chunk) {
    }
}

