/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMap
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Holder
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.core.SectionPos
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ThreadedLevelLightEngine
 *  net.minecraft.world.entity.ai.village.poi.PoiManager$Occupancy
 *  net.minecraft.world.entity.ai.village.poi.PoiRecord
 *  net.minecraft.world.entity.ai.village.poi.PoiSection
 *  net.minecraft.world.entity.ai.village.poi.PoiType
 *  net.minecraft.world.level.ChunkPos
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.chunk.ChunkAccess
 *  net.minecraft.world.level.chunk.LevelChunk
 *  net.minecraft.world.level.chunk.LevelChunkSection
 *  net.minecraft.world.level.lighting.LevelLightEngine
 *  net.minecraft.world.ticks.LevelChunkTicks
 *  net.minecraft.world.ticks.ScheduledTick
 */
package appeng.spatial;

import appeng.api.ids.AETags;
import appeng.api.movable.BlockEntityMoveStrategies;
import appeng.api.movable.IBlockEntityMoveStrategy;
import appeng.block.spatial.MatrixFrameBlock;
import appeng.core.AELog;
import appeng.core.definitions.AEBlocks;
import appeng.server.services.compass.ServerCompassService;
import appeng.util.Platform;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ThreadedLevelLightEngine;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.entity.ai.village.poi.PoiSection;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.ticks.LevelChunkTicks;
import net.minecraft.world.ticks.ScheduledTick;

public class CachedPlane {
    private final int x_size;
    private final int z_size;
    private final int cx_size;
    private final int cz_size;
    private final int x_offset;
    private final int y_offset;
    private final int z_offset;
    private final int y_size;
    private final LevelChunk[][] myChunks;
    private final Column[][] myColumns;
    private final List<BlockEntityMoveRecord> blockEntities = new ArrayList<BlockEntityMoveRecord>();
    private final List<ScheduledTick<Block>> ticks = new ArrayList<ScheduledTick<Block>>();
    private final ServerLevel level;
    private final List<BlockPos> updates = new ArrayList<BlockPos>();
    private final BlockState matrixBlockState;
    private final List<PoiMoveRecord> poiMoveRecords = new ArrayList<PoiMoveRecord>();

    public CachedPlane(ServerLevel level, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        MatrixFrameBlock matrixFrameBlock = AEBlocks.MATRIX_FRAME.block();
        this.matrixBlockState = matrixFrameBlock != null ? matrixFrameBlock.defaultBlockState() : null;
        this.level = level;
        this.x_size = maxX - minX + 1;
        this.y_size = maxY - minY + 1;
        this.z_size = maxZ - minZ + 1;
        this.x_offset = minX;
        this.y_offset = minY;
        this.z_offset = minZ;
        int minCX = minX >> 4;
        int minCY = minY >> 4;
        int minCZ = minZ >> 4;
        int maxCX = maxX >> 4;
        int maxCY = maxY >> 4;
        int maxCZ = maxZ >> 4;
        this.cx_size = maxCX - minCX + 1;
        int cy_size = maxCY - minCY + 1;
        this.cz_size = maxCZ - minCZ + 1;
        this.myChunks = new LevelChunk[this.cx_size][this.cz_size];
        this.myColumns = new Column[this.x_size][this.z_size];
        for (int x = 0; x < this.x_size; ++x) {
            for (int z = 0; z < this.z_size; ++z) {
                this.myColumns[x][z] = new Column(level.getChunk(minX + x >> 4, minZ + z >> 4), minX + x & 0xF, minZ + z & 0xF);
            }
        }
        for (int cx = 0; cx < this.cx_size; ++cx) {
            for (int cz = 0; cz < this.cz_size; ++cz) {
                BlockPos pos;
                LevelChunk c;
                this.myChunks[cx][cz] = c = level.getChunk(minCX + cx, minCZ + cz);
                Set chunkBlockEntities = c.getBlockEntities().entrySet();
                ArrayList<BlockEntity> blockEntities = new ArrayList<BlockEntity>(chunkBlockEntities.size());
                for (Map.Entry entity : chunkBlockEntities) {
                    pos = (BlockPos)entity.getKey();
                    if (pos.getX() < minX || pos.getX() > maxX || pos.getY() < minY || pos.getY() > maxY || pos.getZ() < minZ || pos.getZ() > maxZ) continue;
                    blockEntities.add((BlockEntity)entity.getValue());
                }
                for (BlockEntity blockEntity : blockEntities) {
                    pos = blockEntity.getBlockPos();
                    if (blockEntity.getBlockState().is(AETags.SPATIAL_BLACKLIST)) continue;
                    IBlockEntityMoveStrategy strategy = BlockEntityMoveStrategies.get(blockEntity);
                    CompoundTag savedData = strategy.beginMove(blockEntity, (HolderLookup.Provider)level.registryAccess());
                    LevelChunkSection section = c.getSection(c.getSectionIndex(pos.getY()));
                    int sx = pos.getX() & 0xF;
                    int sy = pos.getY() & 0xF;
                    int sz = pos.getZ() & 0xF;
                    BlockState state = section.getBlockState(sx, sy, sz);
                    if (savedData != null) {
                        this.blockEntities.add(new BlockEntityMoveRecord(strategy, blockEntity, savedData, pos, state));
                        section.setBlockState(sx, sy, sz, Blocks.AIR.defaultBlockState());
                        c.removeBlockEntity(pos);
                        continue;
                    }
                    if (state.isAir()) {
                        level.removeBlock(pos, false);
                        continue;
                    }
                    this.myColumns[pos.getX() - minX][pos.getZ() - minZ].setSkip(pos.getY());
                }
                LevelChunkTicks pending = (LevelChunkTicks)c.getBlockTicks();
                pending.getAll().forEach(entry -> {
                    BlockPos pos = entry.pos();
                    if (pos.getX() >= minX && pos.getX() <= maxX && pos.getY() >= minY && pos.getY() <= maxY && pos.getZ() >= minZ && pos.getZ() <= maxZ) {
                        this.ticks.add((ScheduledTick<Block>)entry);
                    }
                });
                for (int cy = 0; cy < cy_size; ++cy) {
                    PoiSection poiSection = level.getPoiManager().getOrLoad(SectionPos.of((ChunkPos)c.getPos(), (int)(minCY + cy)).asLong()).orElse(null);
                    if (poiSection == null) continue;
                    Iterator poiRecords = poiSection.getRecords(poiType -> true, PoiManager.Occupancy.ANY).iterator();
                    while (poiRecords.hasNext()) {
                        PoiRecord poiRecord = (PoiRecord)poiRecords.next();
                        BlockPos pos2 = poiRecord.getPos();
                        if (pos2.getX() < minX || pos2.getX() > maxX || pos2.getY() < minY || pos2.getY() > maxY || pos2.getZ() < minZ || pos2.getZ() > maxZ) continue;
                        this.poiMoveRecords.add(new PoiMoveRecord(poiRecord.getPos().offset(-this.x_offset, -this.y_offset, -this.z_offset), (Holder<PoiType>)poiRecord.getPoiType()));
                        poiSection.remove(poiRecord.getPos());
                    }
                }
            }
        }
    }

    void swap(CachedPlane dst) {
        if (dst.x_size == this.x_size && dst.y_size == this.y_size && dst.z_size == this.z_size) {
            AELog.info("Block Copy Scale: " + this.x_size + ", " + this.y_size + ", " + this.z_size, new Object[0]);
            long startTime = System.nanoTime();
            for (int x = 0; x < this.x_size; ++x) {
                for (int z = 0; z < this.z_size; ++z) {
                    Column srcCol = this.myColumns[x][z];
                    Column dstCol = dst.myColumns[x][z];
                    for (int y = 0; y < this.y_size; ++y) {
                        int n = this.y_offset + y;
                        int dst_y = dst.y_offset + y;
                        if (srcCol.doNotSkip(n) && dstCol.doNotSkip(dst_y)) {
                            BlockState dstState;
                            LevelChunkSection srcSection = srcCol.getSection(n);
                            LevelChunkSection dstSection = dstCol.getSection(dst_y);
                            BlockState srcState = srcSection.getBlockState(srcCol.x, SectionPos.sectionRelative((int)n), srcCol.z);
                            if (srcState == this.matrixBlockState) {
                                srcState = Blocks.AIR.defaultBlockState();
                            }
                            if ((dstState = dstSection.getBlockState(dstCol.x, SectionPos.sectionRelative((int)dst_y), dstCol.z)) == this.matrixBlockState) {
                                dstState = Blocks.AIR.defaultBlockState();
                            }
                            srcSection.setBlockState(srcCol.x, SectionPos.sectionRelative((int)n), srcCol.z, dstState);
                            dstSection.setBlockState(dstCol.x, SectionPos.sectionRelative((int)dst_y), dstCol.z, srcState);
                            continue;
                        }
                        this.markForUpdate(this.x_offset + x, n, this.z_offset + z);
                        dst.markForUpdate(dst.x_offset + x, dst_y, dst.z_offset + z);
                    }
                }
            }
            long endTime = System.nanoTime();
            long duration = endTime - startTime;
            AELog.info("Block Copy Time: " + duration, new Object[0]);
            for (BlockEntityMoveRecord blockEntityMoveRecord : this.blockEntities) {
                BlockPos pos = blockEntityMoveRecord.blockEntity().getBlockPos();
                dst.addBlockEntity(pos.getX() - this.x_offset, pos.getY() - this.y_offset, pos.getZ() - this.z_offset, blockEntityMoveRecord);
            }
            for (BlockEntityMoveRecord blockEntityMoveRecord : dst.blockEntities) {
                BlockPos pos = blockEntityMoveRecord.blockEntity().getBlockPos();
                this.addBlockEntity(pos.getX() - dst.x_offset, pos.getY() - dst.y_offset, pos.getZ() - dst.z_offset, blockEntityMoveRecord);
            }
            for (ScheduledTick scheduledTick : this.ticks) {
                BlockPos movedPos = scheduledTick.pos().offset(-this.x_offset, -this.y_offset, -this.z_offset);
                dst.addTick(movedPos, (ScheduledTick<Block>)scheduledTick);
            }
            for (ScheduledTick scheduledTick : dst.ticks) {
                BlockPos movedPos = scheduledTick.pos().offset(-dst.x_offset, -dst.y_offset, -dst.z_offset);
                this.addTick(movedPos, (ScheduledTick<Block>)scheduledTick);
            }
            for (PoiMoveRecord poiMoveRecord : this.poiMoveRecords) {
                dst.addPoi(poiMoveRecord);
            }
            for (PoiMoveRecord poiMoveRecord : dst.poiMoveRecords) {
                this.addPoi(poiMoveRecord);
            }
            startTime = System.nanoTime();
            this.updateChunks();
            dst.updateChunks();
            endTime = System.nanoTime();
            duration = endTime - startTime;
            AELog.info("Update Time: " + duration, new Object[0]);
        }
    }

    private void markForUpdate(int x, int y, int z) {
        this.updates.add(new BlockPos(x, y, z));
        for (Direction d : Direction.values()) {
            this.updates.add(new BlockPos(x + d.getStepX(), y + d.getStepY(), z + d.getStepZ()));
        }
    }

    private void addTick(BlockPos pos, ScheduledTick<Block> tick) {
        this.level.getBlockTicks().schedule(new ScheduledTick((Object)((Block)tick.type()), pos, tick.triggerTick(), tick.priority(), tick.subTickOrder()));
    }

    private void addBlockEntity(int x, int y, int z, BlockEntityMoveRecord moveRecord) {
        try {
            boolean success;
            BlockPos originalPos = moveRecord.pos();
            Column c = this.myColumns[x][z];
            if (!c.doNotSkip(y + this.y_offset)) {
                AELog.warn("Block entity %s was queued to be moved from %s, but it's position then skipped during the move.", moveRecord.blockEntity(), originalPos);
                return;
            }
            BlockPos newPosition = new BlockPos(x + this.x_offset, y + this.y_offset, z + this.z_offset);
            ChunkAccess chunk = this.level.getChunk(newPosition);
            LevelChunkSection section = chunk.getSection(chunk.getSectionIndex(newPosition.getY()));
            section.setBlockState(newPosition.getX() & 0xF, newPosition.getY() & 0xF, newPosition.getZ() & 0xF, moveRecord.state);
            IBlockEntityMoveStrategy strategy = moveRecord.strategy();
            try {
                success = strategy.completeMove(moveRecord.blockEntity(), moveRecord.state(), moveRecord.savedData(), (Level)this.level, newPosition);
            }
            catch (Throwable e) {
                AELog.warn(e);
                success = false;
            }
            if (!success) {
                this.attemptRecovery(x, y, z, moveRecord, c);
            }
        }
        catch (Throwable e) {
            AELog.warn(e);
        }
    }

    private void addPoi(PoiMoveRecord record) {
        this.level.getPoiManager().add(record.relativePos.offset(this.x_offset, this.y_offset, this.z_offset), record.poiType);
    }

    private void attemptRecovery(int x, int y, int z, BlockEntityMoveRecord moveRecord, Column c) {
        BlockPos pos = new BlockPos(x, y, z);
        BlockEntityType type = moveRecord.blockEntity().getType();
        AELog.debug("Trying to recover BE %s @ %s", BlockEntityType.getKey((BlockEntityType)type), pos);
        BlockState blockState = moveRecord.blockEntity().getBlockState();
        BlockEntity recoveredEntity = BlockEntity.loadStatic((BlockPos)pos, (BlockState)blockState, (CompoundTag)moveRecord.savedData(), (HolderLookup.Provider)this.level.registryAccess());
        if (recoveredEntity != null) {
            this.level.setBlock(pos, blockState, 3);
            c.c.addAndRegisterBlockEntity(recoveredEntity);
            this.level.sendBlockUpdated(pos, this.level.getBlockState(pos), this.level.getBlockState(pos), z);
        } else {
            AELog.warn("Failed to recover BE %s @ %s", BlockEntityType.getKey((BlockEntityType)type), pos);
        }
    }

    private void updateChunks() {
        LevelLightEngine lightManager = this.level.getLightEngine();
        if (lightManager instanceof ThreadedLevelLightEngine) {
            ThreadedLevelLightEngine serverLightManager = (ThreadedLevelLightEngine)lightManager;
            for (int x = 0; x < this.cx_size; ++x) {
                for (int z = 0; z < this.cz_size; ++z) {
                    LevelChunk c = this.myChunks[x][z];
                    serverLightManager.lightChunk((ChunkAccess)c, false);
                    c.setUnsaved(true);
                }
            }
        }
        for (int x = 0; x < this.cx_size; ++x) {
            for (int z = 0; z < this.cz_size; ++z) {
                LevelChunk c = this.myChunks[x][z];
                ServerCompassService.updateArea(this.getLevel(), (ChunkAccess)c);
                Packet<?> cdp = Platform.getFullChunkPacket(c);
                this.level.getChunkSource().chunkMap.getPlayers(c.getPos(), false).forEach(spe -> spe.connection.send(cdp));
            }
        }
        this.level.getChunkSource().tick(() -> false, false);
    }

    List<BlockPos> getUpdates() {
        return this.updates;
    }

    ServerLevel getLevel() {
        return this.level;
    }

    private static class Column {
        private final int x;
        private final int z;
        private final LevelChunk c;
        private List<Integer> skipThese = null;
        private Int2ObjectMap<BlockState> savedBlockStates = null;

        public Column(LevelChunk chunk, int x, int z) {
            this.x = x;
            this.z = z;
            this.c = chunk;
        }

        private boolean doNotSkip(int y) {
            BlockState blockState = this.getSection(y).getBlockState(this.x, SectionPos.sectionRelative((int)y), this.z);
            if (blockState.is(AETags.SPATIAL_BLACKLIST)) {
                return false;
            }
            return this.skipThese == null || !this.skipThese.contains(y);
        }

        private void setSkip(int y) {
            if (this.skipThese == null) {
                this.skipThese = new ArrayList<Integer>();
            }
            this.skipThese.add(y);
        }

        public LevelChunkSection getSection(int y) {
            return this.c.getSection(this.c.getSectionIndexFromSectionY(SectionPos.blockToSectionCoord((int)y)));
        }
    }

    private record BlockEntityMoveRecord(IBlockEntityMoveStrategy strategy, BlockEntity blockEntity, CompoundTag savedData, BlockPos pos, BlockState state) {
    }

    private record PoiMoveRecord(BlockPos relativePos, Holder<PoiType> poiType) {
    }

    private static class BlockStorageData {
        public BlockState state;

        private BlockStorageData() {
        }
    }
}

