/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.CrashReportCategory
 *  net.minecraft.util.Mth
 */
package appeng.me.service.helpers;

import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickingRequest;
import java.util.LongSummaryStatistics;
import net.minecraft.CrashReportCategory;
import net.minecraft.util.Mth;

public class TickTracker
implements Comparable<TickTracker> {
    private final TickingRequest request;
    private final IGridTickable gt;
    private final IGridNode node;
    private final LongSummaryStatistics statistics;
    private long lastTick;
    private int currentRate;

    public TickTracker(TickingRequest req, IGridNode node, IGridTickable gt, long currentTick) {
        this.request = req;
        this.gt = gt;
        this.node = node;
        this.setCurrentRate(req.initialTickRate());
        this.setLastTick(currentTick);
        this.statistics = new LongSummaryStatistics();
    }

    @Override
    public int compareTo(TickTracker t) {
        int next = Long.compare(this.getNextTick(), t.getNextTick());
        if (next != 0) {
            return next;
        }
        int last = Long.compare(this.getLastTick(), t.getLastTick());
        if (last != 0) {
            return last;
        }
        return Integer.compare(this.getCurrentRate(), t.getCurrentRate());
    }

    public void addEntityCrashInfo(CrashReportCategory category) {
        this.node.fillCrashReportCategory(category);
        category.setDetail("CurrentTickRate", (Object)this.getCurrentRate());
        category.setDetail("MinTickRate", (Object)this.getRequest().minTickRate());
        category.setDetail("MaxTickRate", (Object)this.getRequest().maxTickRate());
        category.setDetail("ConnectedSides", this.getNode().getConnectedSides());
    }

    public int getCurrentRate() {
        return this.currentRate;
    }

    public void setCurrentRate(int currentRate) {
        this.currentRate = Mth.clamp((int)currentRate, (int)this.request.minTickRate(), (int)this.request.maxTickRate());
    }

    public void setTickOnNextTick() {
        this.currentRate = 1;
    }

    public long getNextTick() {
        return this.lastTick + (long)this.currentRate;
    }

    public long getLastTick() {
        return this.lastTick;
    }

    public void setLastTick(long lastTick) {
        this.lastTick = lastTick;
    }

    public IGridNode getNode() {
        return this.node;
    }

    public IGridTickable getGridTickable() {
        return this.gt;
    }

    public TickingRequest getRequest() {
        return this.request;
    }

    public LongSummaryStatistics getStatistics() {
        return this.statistics;
    }
}

