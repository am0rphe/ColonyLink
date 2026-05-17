/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.level.ChunkPos
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.neoforged.neoforge.common.world.chunk.LoadingValidationCallback
 *  net.neoforged.neoforge.common.world.chunk.RegisterTicketControllersEvent
 *  net.neoforged.neoforge.common.world.chunk.TicketController
 *  net.neoforged.neoforge.common.world.chunk.TicketHelper
 *  net.neoforged.neoforge.event.server.ServerAboutToStartEvent
 *  net.neoforged.neoforge.event.server.ServerStoppingEvent
 */
package appeng.server.services;

import appeng.blockentity.spatial.SpatialAnchorBlockEntity;
import appeng.core.AppEng;
import appeng.server.services.ChunkLoadState;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.common.world.chunk.LoadingValidationCallback;
import net.neoforged.neoforge.common.world.chunk.RegisterTicketControllersEvent;
import net.neoforged.neoforge.common.world.chunk.TicketController;
import net.neoforged.neoforge.common.world.chunk.TicketHelper;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

public class ChunkLoadingService
implements LoadingValidationCallback {
    private static final ChunkLoadingService INSTANCE = new ChunkLoadingService();
    private boolean running = true;
    private final TicketController controller = new TicketController(AppEng.makeId("default"), (LoadingValidationCallback)this);

    public void register(RegisterTicketControllersEvent event) {
        event.register(this.controller);
    }

    public void onServerAboutToStart(ServerAboutToStartEvent evt) {
        this.running = true;
    }

    public void onServerStopping(ServerStoppingEvent event) {
        this.running = false;
    }

    public static ChunkLoadingService getInstance() {
        return INSTANCE;
    }

    public void validateTickets(ServerLevel level, TicketHelper ticketHelper) {
        ticketHelper.getBlockTickets().forEach((blockPos, chunks) -> {
            BlockEntity blockEntity = level.getBlockEntity(blockPos);
            if (blockEntity instanceof SpatialAnchorBlockEntity) {
                SpatialAnchorBlockEntity anchor = (SpatialAnchorBlockEntity)blockEntity;
                for (Long chunk : chunks.ticking()) {
                    anchor.registerChunk(new ChunkPos(chunk.longValue()));
                }
            } else {
                ticketHelper.removeAllTickets(blockPos);
            }
        });
    }

    public boolean forceChunk(ServerLevel level, BlockPos owner, ChunkPos position) {
        if (this.running) {
            return this.controller.forceChunk(level, owner, position.x, position.z, true, true);
        }
        return false;
    }

    public boolean releaseChunk(ServerLevel level, BlockPos owner, ChunkPos position) {
        if (this.running) {
            return this.controller.forceChunk(level, owner, position.x, position.z, false, true);
        }
        return false;
    }

    public boolean isChunkForced(ServerLevel level, int chunkX, int chunkZ) {
        return ChunkLoadState.get(level).isForceLoaded(chunkX, chunkZ);
    }
}

