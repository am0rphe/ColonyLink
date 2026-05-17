/*
 * Decompiled with CFR 0.152.
 */
package appeng.api.networking.pathing;

public enum ChannelMode {
    INFINITE(Integer.MAX_VALUE, 0),
    DEFAULT(8, 1),
    X2(16, 2),
    X3(24, 3),
    X4(32, 4);

    private final int adHocNetworkChannels;
    private final int cableCapacityFactor;

    private ChannelMode(int adHocNetworkChannels, int cableCapacityFactor) {
        this.adHocNetworkChannels = adHocNetworkChannels;
        this.cableCapacityFactor = cableCapacityFactor;
    }

    public int getAdHocNetworkChannels() {
        return this.adHocNetworkChannels;
    }

    public int getCableCapacityFactor() {
        return this.cableCapacityFactor;
    }
}

