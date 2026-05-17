/*
 * Decompiled with CFR 0.152.
 */
package appeng.api.config;

public enum TerminalStyle {
    SMALL(1),
    MEDIUM(2),
    TALL(3),
    FULL(4);

    private final int multiplier;

    private TerminalStyle(int multiplier) {
        this.multiplier = multiplier;
    }

    public int getRows(int maxRows) {
        return maxRows * this.multiplier / 4;
    }
}

