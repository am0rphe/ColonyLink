/*
 * Decompiled with CFR 0.152.
 */
package appeng.api.networking.ticking;

import appeng.core.settings.TickRates;

public record TickingRequest(int minTickRate, int maxTickRate, boolean isSleeping, int initialTickRate) {
    public TickingRequest(int minTickRate, int maxTickRate, boolean isSleeping) {
        this(minTickRate, maxTickRate, isSleeping, TickingRequest.getInitialTickDelay(minTickRate, maxTickRate));
    }

    public TickingRequest(TickRates tickRates, boolean isSleeping) {
        this(tickRates.getMin(), tickRates.getMax(), isSleeping);
    }

    private static int getInitialTickDelay(int min, int max) {
        return (min + max) / 2;
    }
}

