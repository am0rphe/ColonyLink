/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Stopwatch
 *  com.google.common.collect.Iterators
 *  net.minecraft.CrashReport
 *  net.minecraft.CrashReportCategory
 *  net.minecraft.ReportedException
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.level.Level
 *  org.jetbrains.annotations.Nullable
 */
package appeng.me.service;

import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridServiceProvider;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.ITickManager;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.me.service.helpers.TickTracker;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Iterators;
import java.util.IdentityHashMap;
import java.util.LongSummaryStatistics;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.concurrent.TimeUnit;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class TickManagerService
implements ITickManager,
IGridServiceProvider {
    public static boolean MONITORING_ENABLED = false;
    private static final int TICK_RATE_SPEED_UP_FACTOR = 2;
    private static final int TICK_RATE_SLOW_DOWN_FACTOR = 1;
    private final Map<IGridNode, TickTracker> alertable = new IdentityHashMap<IGridNode, TickTracker>();
    private final Map<IGridNode, TickTracker> sleeping = new IdentityHashMap<IGridNode, TickTracker>();
    private final Map<IGridNode, TickTracker> awake = new IdentityHashMap<IGridNode, TickTracker>();
    private final Map<Level, PriorityQueue<TickTracker>> upcomingTicks = new IdentityHashMap<Level, PriorityQueue<TickTracker>>();
    private PriorityQueue<TickTracker> currentlyTickingQueue = null;
    private long currentTick = 0L;
    private final Stopwatch stopWatch = Stopwatch.createUnstarted();
    @Nullable
    private IGridNode currentlyTicking;

    @Override
    public void onServerStartTick() {
        ++this.currentTick;
    }

    @Override
    public void onLevelEndTick(Level level) {
        this.tickLevelQueue(level);
    }

    @Override
    public void onServerEndTick() {
        this.tickLevelQueue(null);
    }

    private void tickLevelQueue(@Nullable Level level) {
        PriorityQueue<TickTracker> queue = this.upcomingTicks.get(level);
        if (queue != null) {
            this.currentlyTickingQueue = queue;
            try {
                this.tickQueue(queue);
            }
            finally {
                this.currentlyTickingQueue = null;
            }
            if (queue.isEmpty()) {
                this.upcomingTicks.remove(level);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void tickQueue(PriorityQueue<TickTracker> queue) {
        TickTracker tt;
        while (!queue.isEmpty() && (tt = queue.peek()).getNextTick() <= this.currentTick) {
            TickRateModulation mod;
            if (queue.poll() != tt) {
                throw new IllegalStateException();
            }
            int diff = (int)(this.currentTick - tt.getLastTick());
            this.currentlyTicking = tt.getNode();
            try {
                mod = this.unsafeTickingRequest(tt, diff);
            }
            finally {
                this.currentlyTicking = null;
            }
            tt.setLastTick(this.currentTick);
            int newRate = switch (mod) {
                default -> throw new MatchException(null, null);
                case TickRateModulation.URGENT -> tt.getRequest().minTickRate();
                case TickRateModulation.FASTER -> tt.getCurrentRate() - 2;
                case TickRateModulation.IDLE, TickRateModulation.SLEEP -> tt.getRequest().maxTickRate();
                case TickRateModulation.SLOWER -> tt.getCurrentRate() + 1;
                case TickRateModulation.SAME -> tt.getCurrentRate();
            };
            tt.setCurrentRate(newRate);
            if (mod == TickRateModulation.SLEEP) {
                this.sleepDevice(tt.getNode());
                continue;
            }
            if (!this.awake.containsKey(tt.getNode())) continue;
            queue.add(tt);
        }
    }

    @Override
    public void removeNode(IGridNode gridNode) {
        IGridTickable tickable = gridNode.getService(IGridTickable.class);
        if (tickable != null) {
            this.alertable.remove(gridNode);
            this.sleeping.remove(gridNode);
            TickTracker tt = this.awake.remove(gridNode);
            this.removeFromQueue(gridNode, tt);
        }
    }

    @Override
    public void addNode(IGridNode gridNode, @Nullable CompoundTag savedData) {
        IGridTickable tickable = gridNode.getService(IGridTickable.class);
        if (tickable != null) {
            TickingRequest tr = tickable.getTickingRequest(gridNode);
            Objects.requireNonNull(tr);
            TickTracker tt = new TickTracker(tr, gridNode, tickable, this.currentTick);
            this.alertable.put(gridNode, tt);
            if (tr.isSleeping()) {
                this.sleeping.put(gridNode, tt);
            } else {
                this.awake.put(gridNode, tt);
                this.addToQueue(gridNode, tt);
            }
        }
    }

    @Override
    public boolean alertDevice(IGridNode node) {
        Objects.requireNonNull(node);
        if (node == this.currentlyTicking) {
            return false;
        }
        TickTracker tt = this.alertable.get(node);
        if (tt == null) {
            if (this.sleeping.containsKey(node) || this.awake.containsKey(node)) {
                throw new IllegalArgumentException("Trying to alert a node that isn't alertable");
            }
            return false;
        }
        this.sleeping.remove(node);
        this.awake.put(node, tt);
        tt.setTickOnNextTick();
        this.updateQueuePosition(node, tt);
        return true;
    }

    @Override
    public boolean sleepDevice(IGridNode node) {
        Objects.requireNonNull(node);
        if (node == this.currentlyTicking) {
            return false;
        }
        TickTracker tracker = this.awake.remove(node);
        if (tracker != null) {
            tracker.setCurrentRate(tracker.getRequest().maxTickRate());
            this.sleeping.put(node, tracker);
            this.removeFromQueue(node, tracker);
            return true;
        }
        return false;
    }

    @Override
    public boolean wakeDevice(IGridNode node) {
        Objects.requireNonNull(node);
        if (node == this.currentlyTicking) {
            return false;
        }
        if (this.sleeping.containsKey(node)) {
            TickTracker tt = this.sleeping.get(node);
            this.sleeping.remove(node);
            this.awake.put(node, tt);
            this.updateQueuePosition(node, tt);
            return true;
        }
        return false;
    }

    public long getAverageTime(IGridNode node) {
        LongSummaryStatistics stats = this.getStatistics(node);
        if (stats == null) {
            return 0L;
        }
        return (long)stats.getAverage();
    }

    public long getOverallTime(IGridNode node) {
        LongSummaryStatistics stats = this.getStatistics(node);
        if (stats == null) {
            return 0L;
        }
        return stats.getSum();
    }

    public long getMaximumTime(IGridNode node) {
        LongSummaryStatistics stats = this.getStatistics(node);
        if (stats == null) {
            return 0L;
        }
        return stats.getMax();
    }

    private LongSummaryStatistics getStatistics(IGridNode node) {
        TickTracker tt = this.awake.get(node);
        if (tt == null) {
            tt = this.sleeping.get(node);
        }
        if (tt == null) {
            return null;
        }
        return tt.getStatistics();
    }

    private PriorityQueue<TickTracker> getQueue(@Nullable Level level) {
        return this.upcomingTicks.computeIfAbsent(level, key -> new PriorityQueue());
    }

    private void addToQueue(IGridNode node, TickTracker tt) {
        PriorityQueue<TickTracker> queue = this.getQueue((Level)node.getLevel());
        queue.add(tt);
    }

    private void removeFromQueue(IGridNode node, TickTracker tt) {
        ServerLevel level = node.getLevel();
        PriorityQueue<TickTracker> queue = this.getQueue((Level)level);
        queue.remove(tt);
        if (this.currentlyTickingQueue != queue && queue.isEmpty()) {
            this.upcomingTicks.remove(level);
        }
    }

    private void updateQueuePosition(IGridNode node, TickTracker tt) {
        this.removeFromQueue(node, tt);
        this.addToQueue(node, tt);
    }

    private TickRateModulation unsafeTickingRequest(TickTracker tt, int diff) {
        try {
            if (!MONITORING_ENABLED) {
                return tt.getGridTickable().tickingRequest(tt.getNode(), diff);
            }
            this.stopWatch.reset().start();
            TickRateModulation mod = tt.getGridTickable().tickingRequest(tt.getNode(), diff);
            this.stopWatch.stop();
            long elapsedTime = this.stopWatch.elapsed(TimeUnit.NANOSECONDS);
            tt.getStatistics().accept(elapsedTime);
            return mod;
        }
        catch (Throwable t) {
            CrashReport report = CrashReport.forThrowable((Throwable)t, (String)"Ticking GridNode");
            CrashReportCategory category = report.addCategory(tt.getGridTickable().getClass().getSimpleName() + " being ticked.");
            tt.addEntityCrashInfo(category);
            throw new ReportedException(report);
        }
    }

    public NodeStatus getStatus(IGridNode node) {
        TickTracker tracker;
        TickTracker sleepingTracker = this.sleeping.get(node);
        TickTracker awakeTracker = this.awake.get(node);
        TickTracker alertableTracker = this.alertable.get(node);
        boolean isQueued = false;
        PriorityQueue<TickTracker> tickQueue = this.upcomingTicks.get(node.getLevel());
        if (awakeTracker != null && tickQueue != null) {
            isQueued = Iterators.contains(tickQueue.iterator(), (Object)awakeTracker);
        }
        if ((tracker = awakeTracker) == null) {
            tracker = alertableTracker;
        }
        if (tracker == null) {
            tracker = sleepingTracker;
        }
        int currentRate = tracker != null ? tracker.getCurrentRate() : 0;
        long lastTick = tracker != null ? tracker.getLastTick() : 0L;
        return new NodeStatus(alertableTracker != null, sleepingTracker != null, awakeTracker != null, isQueued, currentRate, this.currentTick - lastTick);
    }

    public record NodeStatus(boolean alertable, boolean sleeping, boolean awake, boolean queued, int currentRate, long lastTick) {
    }
}

