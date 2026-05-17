/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.HashMultiset
 *  com.google.common.collect.Multiset
 *  com.google.gson.stream.JsonWriter
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.level.ChunkPos
 *  org.jetbrains.annotations.Nullable
 */
package appeng.me.service;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridService;
import appeng.api.networking.IGridServiceProvider;
import appeng.api.networking.events.statistics.GridChunkEvent;
import appeng.me.InWorldGridNode;
import appeng.util.JsonStreamUtil;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.Nullable;

public class StatisticsService
implements IGridService,
IGridServiceProvider {
    private final IGrid grid;
    private final Map<ServerLevel, Multiset<ChunkPos>> chunks;

    public StatisticsService(IGrid g) {
        this.grid = g;
        this.chunks = new HashMap<ServerLevel, Multiset<ChunkPos>>();
    }

    @Override
    public void removeNode(IGridNode node) {
        if (node instanceof InWorldGridNode) {
            InWorldGridNode inWorldNode = (InWorldGridNode)node;
            this.removeChunk(inWorldNode.getLevel(), inWorldNode.getLocation());
        }
    }

    @Override
    public void addNode(IGridNode node, @Nullable CompoundTag savedData) {
        if (node instanceof InWorldGridNode) {
            InWorldGridNode inWorldNode = (InWorldGridNode)node;
            this.addChunk(inWorldNode.getLevel(), inWorldNode.getLocation());
        }
    }

    public IGrid getGrid() {
        return this.grid;
    }

    public Set<ServerLevel> getLevels() {
        return this.chunks.keySet();
    }

    public Set<ChunkPos> chunks(ServerLevel level) {
        return this.chunks.get(level).elementSet();
    }

    public Map<ServerLevel, Multiset<ChunkPos>> getChunks() {
        return this.chunks;
    }

    private boolean addChunk(ServerLevel level, BlockPos pos) {
        ChunkPos position = new ChunkPos(pos);
        if (!this.getChunks(level).contains((Object)position)) {
            this.grid.postEvent(new GridChunkEvent.GridChunkAdded(level, position));
        }
        return this.getChunks(level).add((Object)position);
    }

    private boolean removeChunk(ServerLevel level, BlockPos pos) {
        ChunkPos position = new ChunkPos(pos);
        boolean ret = this.getChunks(level).remove((Object)position);
        if (ret && !this.getChunks(level).contains((Object)position)) {
            this.grid.postEvent(new GridChunkEvent.GridChunkRemoved(level, position));
        }
        this.clearLevel(level);
        return ret;
    }

    private Multiset<ChunkPos> getChunks(ServerLevel level) {
        return this.chunks.computeIfAbsent(level, l -> HashMultiset.create());
    }

    private void clearLevel(ServerLevel level) {
        if (this.chunks.get(level).isEmpty()) {
            this.chunks.remove(level);
        }
    }

    @Override
    public void debugDump(JsonWriter writer, HolderLookup.Provider registries) throws IOException {
        JsonStreamUtil.writeProperties(Map.of("chunks", this.chunks.keySet().stream().collect(Collectors.toMap(level -> level.dimension().location().toString(), level -> this.chunks.get(level).elementSet().stream().map(JsonStreamUtil::toJson).toList()))), writer);
    }
}

