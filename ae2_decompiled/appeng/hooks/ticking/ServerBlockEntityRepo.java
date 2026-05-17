/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.longs.Long2ObjectMap
 *  it.unimi.dsi.fastutil.longs.Long2ObjectMap$Entry
 *  it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
 *  it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
 *  net.minecraft.ChatFormatting
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.level.ChunkPos
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.LevelAccessor
 *  net.minecraft.world.level.block.entity.BlockEntity
 */
package appeng.hooks.ticking;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;

class ServerBlockEntityRepo {
    private final Map<LevelAccessor, Long2ObjectMap<List<FirstTickInfo<?>>>> blockEntities = new Object2ObjectOpenHashMap();

    ServerBlockEntityRepo() {
    }

    void clear() {
        this.blockEntities.clear();
    }

    synchronized <T extends BlockEntity> void addBlockEntity(T blockEntity, Consumer<? super T> initFunction) {
        Level level = blockEntity.getLevel();
        int x = blockEntity.getBlockPos().getX() >> 4;
        int z = blockEntity.getBlockPos().getZ() >> 4;
        long chunkPos = ChunkPos.asLong((int)x, (int)z);
        Long2ObjectMap worldQueue = this.blockEntities.computeIfAbsent((LevelAccessor)level, key -> new Long2ObjectOpenHashMap());
        ((List)worldQueue.computeIfAbsent(chunkPos, key -> new ArrayList())).add(new FirstTickInfo<T>(blockEntity, initFunction));
    }

    synchronized void removeLevel(LevelAccessor level) {
        this.blockEntities.remove(level);
    }

    synchronized void removeChunk(LevelAccessor level, long chunkPos) {
        Map queue = (Map)this.blockEntities.get(level);
        if (queue != null) {
            queue.remove(chunkPos);
        }
    }

    public Long2ObjectMap<List<FirstTickInfo<?>>> getBlockEntities(LevelAccessor level) {
        return this.blockEntities.get(level);
    }

    public List<Component> getReport() {
        ArrayList<Component> result = new ArrayList<Component>();
        for (Map.Entry<LevelAccessor, Long2ObjectMap<List<FirstTickInfo<?>>>> levelEntry : this.blockEntities.entrySet()) {
            if (levelEntry.getValue().isEmpty()) continue;
            LevelAccessor level = levelEntry.getKey();
            String levelName = level.toString();
            if (level instanceof ServerLevel) {
                ServerLevel serverLevel = (ServerLevel)level;
                levelName = serverLevel.dimension().location().toString();
            }
            result.add((Component)Component.literal((String)levelName).withStyle(ChatFormatting.BOLD));
            for (Long2ObjectMap.Entry chunkEntry : levelEntry.getValue().long2ObjectEntrySet()) {
                ChunkPos chunkPos = new ChunkPos(chunkEntry.getLongKey());
                MutableComponent line = Component.literal((String)(chunkPos.x + "," + chunkPos.z + ": ")).withStyle(ChatFormatting.BOLD).append(Integer.toString(((List)chunkEntry.getValue()).size()));
                result.add((Component)line);
            }
        }
        return result;
    }

    record FirstTickInfo<T extends BlockEntity>(T blockEntity, Consumer<? super T> initFunction) {
        void callInit() {
            this.initFunction.accept(this.blockEntity);
        }
    }
}

