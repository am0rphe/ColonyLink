/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.level.ChunkPos
 */
package appeng.api.networking.events.statistics;

import appeng.api.networking.events.statistics.GridStatisticsEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

public abstract class GridChunkEvent
extends GridStatisticsEvent {
    private final ServerLevel level;
    private final ChunkPos chunkPos;

    public GridChunkEvent(ServerLevel level, ChunkPos chunkPos) {
        this.level = level;
        this.chunkPos = chunkPos;
    }

    public ServerLevel getLevel() {
        return this.level;
    }

    public ChunkPos getChunkPos() {
        return this.chunkPos;
    }

    public static class GridChunkRemoved
    extends GridChunkEvent {
        public GridChunkRemoved(ServerLevel level, ChunkPos chunkPos) {
            super(level, chunkPos);
        }
    }

    public static class GridChunkAdded
    extends GridChunkEvent {
        public GridChunkAdded(ServerLevel level, ChunkPos chunkPos) {
            super(level, chunkPos);
        }
    }
}

