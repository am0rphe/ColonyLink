/*
 * Decompiled with CFR 0.152.
 */
package appeng.api.storage.cells;

public enum CellState {
    ABSENT(0),
    EMPTY(65280),
    NOT_EMPTY(43775),
    TYPES_FULL(0xFFAA00),
    FULL(0xFF0000);

    private final int stateColor;

    private CellState(int stateColor) {
        this.stateColor = stateColor;
    }

    public int getStateColor() {
        return this.stateColor;
    }
}

