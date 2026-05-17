/*
 * Decompiled with CFR 0.152.
 */
package appeng.client.gui.widgets;

import java.time.Duration;

public class EventRepeater {
    private long nextEventTime = -1L;
    private EventCallback eventCallback = null;
    private final long eventDelay;
    private final long eventInterval;

    public EventRepeater(Duration delay, Duration interval) {
        this.eventDelay = delay.toNanos();
        this.eventInterval = interval.toNanos();
    }

    public void tick() {
        if (this.eventCallback == null) {
            return;
        }
        long nanoTime = System.nanoTime();
        if (nanoTime < this.nextEventTime) {
            return;
        }
        this.nextEventTime = nanoTime + this.eventInterval;
        this.eventCallback.trigger();
    }

    public void repeat(EventCallback callback) {
        long time = System.nanoTime();
        this.eventCallback = callback;
        this.nextEventTime = time + this.eventDelay;
    }

    public boolean isRepeating() {
        return this.eventCallback != null;
    }

    public void stop() {
        this.eventCallback = null;
    }

    @FunctionalInterface
    public static interface EventCallback {
        public void trigger();
    }
}

