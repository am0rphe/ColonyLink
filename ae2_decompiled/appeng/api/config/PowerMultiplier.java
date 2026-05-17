/*
 * Decompiled with CFR 0.152.
 */
package appeng.api.config;

public enum PowerMultiplier {
    ONE,
    CONFIG;

    public double multiplier = 1.0;

    public double multiply(double in) {
        return in * this.multiplier;
    }

    public double divide(double in) {
        return in / this.multiplier;
    }
}

