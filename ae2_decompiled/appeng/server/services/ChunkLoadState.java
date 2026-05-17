/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.longs.Long2ObjectMap
 *  it.unimi.dsi.fastutil.longs.Long2ObjectMap$Entry
 *  it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
 *  it.unimi.dsi.fastutil.longs.LongOpenHashSet
 *  it.unimi.dsi.fastutil.longs.LongSet
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.LongArrayTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.level.ChunkPos
 *  net.minecraft.world.level.saveddata.SavedData$Factory
 */
package appeng.server.services;

import appeng.core.worlddata.AESavedData;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;

class ChunkLoadState
extends AESavedData {
    public static final String NAME = "ae2_chunk_load_state";
    private final ServerLevel level;
    private final Long2ObjectMap<Set<BlockPos>> forceLoadedChunks = new Long2ObjectOpenHashMap();

    public static ChunkLoadState get(ServerLevel level) {
        return (ChunkLoadState)level.getDataStorage().computeIfAbsent(new SavedData.Factory(() -> new ChunkLoadState(level), (tag, provider) -> new ChunkLoadState(level, (CompoundTag)tag), null), NAME);
    }

    private ChunkLoadState(ServerLevel level) {
        this.level = level;
    }

    private ChunkLoadState(ServerLevel level, CompoundTag tag) {
        this(level);
        ListTag forcedChunks = tag.getList("forcedChunks", 10);
        for (int i = 0; i < forcedChunks.size(); ++i) {
            CompoundTag forcedChunk = forcedChunks.getCompound(i);
            ChunkPos chunkPos = new ChunkPos(forcedChunk.getInt("cx"), forcedChunk.getInt("cz"));
            HashSet<BlockPos> blockSet = new HashSet<BlockPos>();
            for (long blockPos : forcedChunk.getLongArray("blocks")) {
                blockSet.add(BlockPos.of((long)blockPos));
            }
            this.forceLoadedChunks.put(chunkPos.toLong(), blockSet);
        }
    }

    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag forcedChunks = new ListTag();
        for (Long2ObjectMap.Entry entry : this.forceLoadedChunks.long2ObjectEntrySet()) {
            ChunkPos chunkPos = new ChunkPos(entry.getLongKey());
            CompoundTag forcedChunk = new CompoundTag();
            forcedChunk.putInt("cx", chunkPos.x);
            forcedChunk.putInt("cz", chunkPos.z);
            LongArrayTag list = new LongArrayTag(((Set)entry.getValue()).stream().map(BlockPos::asLong).toList());
            forcedChunk.put("blocks", (Tag)list);
            forcedChunks.add((Object)forcedChunk);
        }
        tag.put("forcedChunks", (Tag)forcedChunks);
        return tag;
    }

    public void forceChunk(ChunkPos chunkPos, BlockPos sourcePos) {
        long chunk = chunkPos.toLong();
        ((Set)this.forceLoadedChunks.computeIfAbsent(chunk, pos -> new HashSet())).add(sourcePos.immutable());
        this.level.setChunkForced(chunkPos.x, chunkPos.z, true);
        this.setDirty();
    }

    public void releaseChunk(ChunkPos chunkPos, BlockPos sourcePos) {
        Set map = (Set)this.forceLoadedChunks.get(chunkPos.toLong());
        if (map == null) {
            return;
        }
        map.remove(sourcePos);
        if (map.isEmpty()) {
            this.forceLoadedChunks.remove(chunkPos.toLong());
            this.level.setChunkForced(chunkPos.x, chunkPos.z, false);
        }
        this.setDirty();
    }

    public void releaseAll(BlockPos sourcePos) {
        long[] relevantChunks;
        for (long chunk : relevantChunks = this.forceLoadedChunks.long2ObjectEntrySet().stream().filter(entry -> ((Set)entry.getValue()).contains(sourcePos)).mapToLong(Long2ObjectMap.Entry::getLongKey).toArray()) {
            this.releaseChunk(new ChunkPos(chunk), sourcePos);
        }
    }

    public Map<BlockPos, LongSet> getAllBlocks() {
        HashMap<BlockPos, LongSet> blocks = new HashMap<BlockPos, LongSet>();
        for (Long2ObjectMap.Entry entry : this.forceLoadedChunks.long2ObjectEntrySet()) {
            for (BlockPos blockPos : (Set)entry.getValue()) {
                blocks.computeIfAbsent(blockPos, pos -> new LongOpenHashSet()).add(entry.getLongKey());
            }
        }
        return blocks;
    }

    public boolean isForceLoaded(int cx, int cz) {
        return this.forceLoadedChunks.containsKey(ChunkPos.asLong((int)cx, (int)cz));
    }
}

