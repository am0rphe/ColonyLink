/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.context.CommandContext
 *  net.minecraft.commands.CommandSourceStack
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.level.ChunkPos
 *  net.minecraft.world.level.LevelAccessor
 *  net.minecraft.world.level.chunk.ChunkAccess
 *  net.minecraft.world.level.levelgen.Heightmap$Types
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.neoforge.common.NeoForge
 *  net.neoforged.neoforge.event.level.ChunkEvent$Load
 *  net.neoforged.neoforge.event.level.ChunkEvent$Unload
 */
package appeng.server.subcommands;

import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.server.ISubCommand;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.ChunkEvent;

public class ChunkLogger
implements ISubCommand {
    private boolean enabled = false;

    private void displayStack() {
        if (AEConfig.instance().isChunkLoggerTraceEnabled()) {
            boolean output = false;
            for (StackTraceElement e : Thread.currentThread().getStackTrace()) {
                if (output) {
                    AELog.info("\t\t" + e.getClassName() + "." + e.getMethodName() + " (" + e.getLineNumber() + ")", new Object[0]);
                    continue;
                }
                output = e.getClassName().contains("EventBus") && e.getMethodName().contains("post");
            }
        }
    }

    @SubscribeEvent
    public void onChunkLoadEvent(ChunkEvent.Load event) {
        LevelAccessor levelAccessor = event.getLevel();
        if (levelAccessor instanceof ServerLevel) {
            ServerLevel level = (ServerLevel)levelAccessor;
            ChunkAccess chunk = event.getChunk();
            ChunkPos chunkPos = chunk.getPos();
            String center = ChunkLogger.getCenter(chunk);
            AELog.info("Loaded chunk " + chunkPos.x + "," + chunkPos.z + " [center: " + center + "] in " + String.valueOf(level.dimension().location()), new Object[0]);
            this.displayStack();
        }
    }

    @SubscribeEvent
    public void onChunkUnloadEvent(ChunkEvent.Unload event) {
        LevelAccessor levelAccessor = event.getLevel();
        if (levelAccessor instanceof ServerLevel) {
            ServerLevel level = (ServerLevel)levelAccessor;
            ChunkAccess chunk = event.getChunk();
            ChunkPos chunkPos = chunk.getPos();
            String center = ChunkLogger.getCenter(chunk);
            AELog.info("Unloaded chunk " + chunkPos.x + "," + chunkPos.z + " [center: " + center + "] in " + String.valueOf(level.dimension().location()), new Object[0]);
            this.displayStack();
        }
    }

    private static String getCenter(ChunkAccess chunk) {
        ChunkPos chunkPos = chunk.getPos();
        int x = chunkPos.getMiddleBlockX();
        int z = chunkPos.getMiddleBlockZ();
        int y = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, x, z) + 1;
        return x + " " + y + " " + z;
    }

    @Override
    public void call(MinecraftServer srv, CommandContext<CommandSourceStack> data, CommandSourceStack sender) {
        boolean bl = this.enabled = !this.enabled;
        if (this.enabled) {
            NeoForge.EVENT_BUS.register((Object)this);
            sender.sendSuccess(() -> Component.translatable((String)"commands.ae2.ChunkLoggerOn"), true);
        } else {
            NeoForge.EVENT_BUS.unregister((Object)this);
            sender.sendSuccess(() -> Component.translatable((String)"commands.ae2.ChunkLoggerOff"), true);
        }
    }
}

