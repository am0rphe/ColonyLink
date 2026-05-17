/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Preconditions
 *  com.google.common.base.Stopwatch
 *  com.google.common.collect.LinkedListMultimap
 *  com.google.common.collect.Multimap
 *  it.unimi.dsi.fastutil.longs.Long2ObjectMap
 *  net.minecraft.CrashReport
 *  net.minecraft.CrashReportCategory
 *  net.minecraft.ReportedException
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.level.ChunkPos
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.LevelAccessor
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.chunk.ChunkAccess
 *  net.neoforged.bus.api.EventPriority
 *  net.neoforged.neoforge.common.NeoForge
 *  net.neoforged.neoforge.event.level.ChunkEvent$Unload
 *  net.neoforged.neoforge.event.level.LevelEvent$Unload
 *  net.neoforged.neoforge.event.tick.LevelTickEvent$Post
 *  net.neoforged.neoforge.event.tick.LevelTickEvent$Pre
 *  net.neoforged.neoforge.event.tick.ServerTickEvent$Post
 *  net.neoforged.neoforge.event.tick.ServerTickEvent$Pre
 */
package appeng.hooks.ticking;

import appeng.api.networking.IGridNode;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.crafting.CraftingCalculation;
import appeng.hooks.ticking.ServerBlockEntityRepo;
import appeng.hooks.ticking.ServerGridRepo;
import appeng.me.Grid;
import appeng.me.GridNode;
import appeng.util.ILevelRunnable;
import appeng.util.Platform;
import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public class TickHandler {
    private static final int TIME_LIMIT_PROCESS_QUEUE_MILLISECONDS = 25;
    private static final TickHandler INSTANCE = new TickHandler();
    private final Queue<ILevelRunnable> serverQueue = new ArrayDeque<ILevelRunnable>();
    private final Multimap<LevelAccessor, CraftingCalculation> craftingJobs = LinkedListMultimap.create();
    private final Map<LevelAccessor, Queue<ILevelRunnable>> callQueue = new HashMap<LevelAccessor, Queue<ILevelRunnable>>();
    private final ServerBlockEntityRepo blockEntities = new ServerBlockEntityRepo();
    private final ServerGridRepo grids = new ServerGridRepo();
    private final Stopwatch stopWatch = Stopwatch.createUnstarted();
    private int processQueueElementsProcessed = 0;
    private int processQueueElementsRemaining = 0;
    private long tickCounter;

    public static TickHandler instance() {
        return INSTANCE;
    }

    private TickHandler() {
    }

    public void init() {
        NeoForge.EVENT_BUS.addListener(this::onServerTickStart);
        NeoForge.EVENT_BUS.addListener(this::onServerTickEnd);
        NeoForge.EVENT_BUS.addListener(this::onServerLevelTickStart);
        NeoForge.EVENT_BUS.addListener(this::onServerLevelTickEnd);
        NeoForge.EVENT_BUS.addListener(this::onUnloadChunk);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, this::onUnloadLevel);
    }

    public void addCallable(LevelAccessor level, Runnable c) {
        this.addCallable(level, (Level ignored) -> c.run());
    }

    public void addCallable(LevelAccessor level, ILevelRunnable c) {
        Preconditions.checkArgument((level == null || !level.isClientSide() ? 1 : 0) != 0, (Object)"Can only register serverside callbacks");
        if (level == null) {
            this.serverQueue.add(c);
        } else {
            Queue<ILevelRunnable> queue = this.callQueue.get(level);
            if (queue == null) {
                queue = new ArrayDeque<ILevelRunnable>();
                this.callQueue.put(level, queue);
            }
            queue.add(c);
        }
    }

    public <T extends BlockEntity> void addInit(T blockEntity, Consumer<? super T> initFunction) {
        Objects.requireNonNull(blockEntity);
        if (!blockEntity.getLevel().isClientSide()) {
            this.blockEntities.addBlockEntity(blockEntity, initFunction);
        }
    }

    public void addNetwork(Grid grid) {
        Platform.assertServerThread();
        this.grids.addNetwork(grid);
    }

    public void removeNetwork(Grid grid) {
        Platform.assertServerThread();
        this.grids.removeNetwork(grid);
    }

    public Set<Grid> getGridList() {
        Platform.assertServerThread();
        return this.grids.getNetworks();
    }

    public void shutdown() {
        Platform.assertServerThread();
        this.blockEntities.clear();
        this.grids.clear();
    }

    public void onUnloadChunk(ChunkEvent.Unload ev) {
        LevelAccessor level = ev.getLevel();
        ChunkAccess chunk = ev.getChunk();
        if (!level.isClientSide()) {
            this.blockEntities.removeChunk(level, chunk.getPos().toLong());
        }
    }

    public void onUnloadLevel(LevelEvent.Unload ev) {
        LevelAccessor level = ev.getLevel();
        if (level.isClientSide()) {
            return;
        }
        ArrayList<GridNode> toDestroy = new ArrayList<GridNode>();
        this.grids.updateNetworks();
        for (Grid g : this.grids.getNetworks()) {
            for (IGridNode n : g.getNodes()) {
                if (n.getLevel() != level) continue;
                toDestroy.add((GridNode)n);
            }
        }
        for (GridNode n : toDestroy) {
            n.destroy();
        }
        this.blockEntities.removeLevel(level);
        this.callQueue.remove(level);
    }

    private void onServerLevelTickStart(LevelTickEvent.Pre event) {
        Level level = event.getLevel();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel level2 = (ServerLevel)level;
        Queue<ILevelRunnable> queue = this.callQueue.remove(level2);
        this.processQueueElementsRemaining += this.processQueue(queue, (Level)level2);
        Queue<ILevelRunnable> newQueue = this.callQueue.put((LevelAccessor)level2, queue);
        if (newQueue != null) {
            queue.addAll(newQueue);
        }
        this.grids.updateNetworks();
        for (Grid g : this.grids.getNetworks()) {
            try {
                g.onLevelStartTick((Level)level2);
            }
            catch (Throwable t) {
                CrashReport crashReport = CrashReport.forThrowable((Throwable)t, (String)"Ticking grid on start of level tick");
                g.fillCrashReportCategory(crashReport.addCategory("Grid being ticked"));
                level2.fillReportDetails(crashReport);
                throw new ReportedException(crashReport);
            }
        }
    }

    private void onServerLevelTickEnd(LevelTickEvent.Post event) {
        Level level = event.getLevel();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel level2 = (ServerLevel)level;
        this.simulateCraftingJobs((LevelAccessor)level2);
        this.readyBlockEntities(level2);
        for (Grid g : this.grids.getNetworks()) {
            try {
                g.onLevelEndTick((Level)level2);
            }
            catch (Throwable t) {
                CrashReport crashReport = CrashReport.forThrowable((Throwable)t, (String)"Ticking grid on end of level tick");
                g.fillCrashReportCategory(crashReport.addCategory("Grid being ticked"));
                level2.fillReportDetails(crashReport);
                throw new ReportedException(crashReport);
            }
        }
    }

    private void onServerTickStart(ServerTickEvent.Pre event) {
        this.processQueueElementsProcessed = 0;
        this.processQueueElementsRemaining = 0;
        this.stopWatch.reset();
        for (Grid g : this.grids.getNetworks()) {
            try {
                g.onServerStartTick();
            }
            catch (Throwable t) {
                CrashReport crashReport = CrashReport.forThrowable((Throwable)t, (String)"Ticking grid on start of server tick");
                g.fillCrashReportCategory(crashReport.addCategory("Grid being ticked"));
                throw new ReportedException(crashReport);
            }
        }
    }

    private void onServerTickEnd(ServerTickEvent.Post event) {
        for (Grid g : this.grids.getNetworks()) {
            try {
                g.onServerEndTick();
            }
            catch (Throwable t) {
                CrashReport crashReport = CrashReport.forThrowable((Throwable)t, (String)"Ticking grid on end of server tick");
                g.fillCrashReportCategory(crashReport.addCategory("Grid being ticked"));
                throw new ReportedException(crashReport);
            }
        }
        this.processQueueElementsRemaining += this.processQueue(this.serverQueue, null);
        if (this.stopWatch.elapsed(TimeUnit.MILLISECONDS) > 25L) {
            AELog.warn("Exceeded time limit of %d ms after processing %d queued tick callbacks (%d remain)", 25, this.processQueueElementsProcessed, this.processQueueElementsRemaining);
        }
        ++this.tickCounter;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void registerCraftingSimulation(Level level, CraftingCalculation craftingCalculation) {
        Preconditions.checkArgument((!level.isClientSide ? 1 : 0) != 0, (Object)"Trying to register a crafting job for a client-level");
        Multimap<LevelAccessor, CraftingCalculation> multimap = this.craftingJobs;
        synchronized (multimap) {
            this.craftingJobs.put((Object)level, (Object)craftingCalculation);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void simulateCraftingJobs(LevelAccessor level) {
        Multimap<LevelAccessor, CraftingCalculation> multimap = this.craftingJobs;
        synchronized (multimap) {
            Collection jobSet = this.craftingJobs.get((Object)level);
            if (!jobSet.isEmpty()) {
                int jobSize = jobSet.size();
                int microSecondsPerTick = AEConfig.instance().getCraftingCalculationTimePerTick() * 1000;
                int simTime = Math.max(1, microSecondsPerTick / jobSize);
                Iterator i = jobSet.iterator();
                while (i.hasNext()) {
                    CraftingCalculation cj = (CraftingCalculation)i.next();
                    if (cj.simulateFor(simTime)) continue;
                    i.remove();
                }
            }
        }
    }

    private void readyBlockEntities(ServerLevel level) {
        long[] workSet;
        Long2ObjectMap<List<ServerBlockEntityRepo.FirstTickInfo<?>>> levelQueue = this.blockEntities.getBlockEntities((LevelAccessor)level);
        if (levelQueue == null) {
            return;
        }
        for (long packedChunkPos : workSet = levelQueue.keySet().toLongArray()) {
            if (!Platform.areBlockEntitiesTicking((Level)level, packedChunkPos)) continue;
            List chunkQueue = (List)levelQueue.remove(packedChunkPos);
            if (chunkQueue == null) {
                AELog.warn("Chunk %s was unloaded while we were readying block entities", new ChunkPos(packedChunkPos));
                continue;
            }
            for (ServerBlockEntityRepo.FirstTickInfo info : chunkQueue) {
                if (info.blockEntity().isRemoved()) continue;
                try {
                    info.callInit();
                }
                catch (Throwable t) {
                    CrashReport crashReport = CrashReport.forThrowable((Throwable)t, (String)"Readying AE2 block entity");
                    CrashReportCategory category = crashReport.addCategory("Block entity being readied");
                    category.setDetail("World", () -> level.dimension().location().toString());
                    info.blockEntity().fillCrashReportCategory(category);
                    throw new ReportedException(crashReport);
                }
            }
        }
    }

    private int processQueue(Queue<ILevelRunnable> queue, Level level) {
        if (queue == null) {
            return 0;
        }
        this.stopWatch.start();
        while (!queue.isEmpty()) {
            try {
                queue.poll().call(level);
                ++this.processQueueElementsProcessed;
                if (this.stopWatch.elapsed(TimeUnit.MILLISECONDS) <= 25L) continue;
                break;
            }
            catch (Exception e) {
                AELog.warn(e);
            }
        }
        this.stopWatch.stop();
        return queue.size();
    }

    public long getCurrentTick() {
        return this.tickCounter;
    }

    public List<Component> getBlockEntityReport() {
        return this.blockEntities.getReport();
    }
}

