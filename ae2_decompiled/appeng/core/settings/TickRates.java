/*
 * Decompiled with CFR 0.152.
 */
package appeng.core.settings;

public enum TickRates {
    Interface(5, 120),
    ImportBus(5, 40),
    ExportBus(5, 60),
    AnnihilationPlane(2, 120),
    METunnel(5, 20),
    Inscriber(1, 20),
    Charger(10, 10),
    IOPort(1, 5),
    VibrationChamber(10, 40),
    StorageBus(5, 60),
    ItemTunnel(5, 60),
    LightTunnel(5, 60);

    private final int defaultMin;
    private final int defaultMax;
    private int min;
    private int max;

    private TickRates(int min, int max) {
        this.defaultMin = min;
        this.defaultMax = max;
        this.min = min;
        this.max = max;
    }

    public int getDefaultMin() {
        return this.defaultMin;
    }

    public int getDefaultMax() {
        return this.defaultMax;
    }

    public int getMax() {
        return this.max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public int getMin() {
        return this.min;
    }

    public void setMin(int min) {
        this.min = min;
    }
}

