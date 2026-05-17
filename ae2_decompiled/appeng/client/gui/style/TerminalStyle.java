/*
 * Decompiled with CFR 0.152.
 */
package appeng.client.gui.style;

import appeng.client.Point;
import appeng.client.gui.style.Blitter;

public class TerminalStyle {
    private Blitter header;
    private Blitter firstRow;
    private Blitter row;
    private Blitter lastRow;
    private Blitter bottom;
    private int slotsPerRow;
    private boolean sortable = true;
    private boolean supportsAutoCrafting = false;
    private boolean showTooltipsWithItemInHand;

    public Blitter getHeader() {
        return this.header;
    }

    public void setHeader(Blitter header) {
        this.header = header;
    }

    public Blitter getFirstRow() {
        return this.firstRow;
    }

    public void setFirstRow(Blitter firstRow) {
        this.firstRow = firstRow;
    }

    public Blitter getRow() {
        return this.row;
    }

    public void setRow(Blitter row) {
        this.row = row;
    }

    public Blitter getLastRow() {
        return this.lastRow;
    }

    public void setLastRow(Blitter lastRow) {
        this.lastRow = lastRow;
    }

    public Blitter getBottom() {
        return this.bottom;
    }

    public void setBottom(Blitter bottom) {
        this.bottom = bottom;
    }

    public int getSlotsPerRow() {
        return this.slotsPerRow;
    }

    public void setSlotsPerRow(int slotsPerRow) {
        this.slotsPerRow = slotsPerRow;
    }

    public boolean isSortable() {
        return this.sortable;
    }

    public void setSortable(boolean sortable) {
        this.sortable = sortable;
    }

    public int getScreenWidth() {
        int screenWidth = this.header.getSrcWidth();
        screenWidth = Math.max(screenWidth, this.firstRow.getSrcWidth());
        screenWidth = Math.max(screenWidth, this.row.getSrcWidth());
        screenWidth = Math.max(screenWidth, this.lastRow.getSrcWidth());
        screenWidth = Math.max(screenWidth, this.bottom.getSrcWidth());
        return screenWidth;
    }

    public int getPossibleRows(int availableHeight) {
        return (availableHeight - this.header.getSrcHeight() - this.bottom.getSrcHeight()) / this.row.getSrcHeight();
    }

    public Point getSlotPos(int row, int col) {
        int x = 7 + col * 18;
        int y = this.header.getSrcHeight();
        if (row > 0) {
            y += this.firstRow.getSrcHeight();
            y += (row - 1) * this.row.getSrcHeight();
        }
        return new Point(x, y).move(1, 1);
    }

    public int getScreenHeight(int rows) {
        int result = this.header.getSrcHeight();
        result += this.firstRow.getSrcHeight();
        result += Math.max(0, rows - 2) * this.row.getSrcHeight();
        result += this.lastRow.getSrcHeight();
        return result += this.bottom.getSrcHeight();
    }

    public boolean isSupportsAutoCrafting() {
        return this.supportsAutoCrafting;
    }

    public void setSupportsAutoCrafting(boolean supportsAutoCrafting) {
        this.supportsAutoCrafting = supportsAutoCrafting;
    }

    public boolean isShowTooltipsWithItemInHand() {
        return this.showTooltipsWithItemInHand;
    }

    public void setShowTooltipsWithItemInHand(boolean showTooltipsWithItemInHand) {
        this.showTooltipsWithItemInHand = showTooltipsWithItemInHand;
    }

    public void validate() {
        if (this.header == null) {
            throw new RuntimeException("terminalStyle.header is missing");
        }
        if (this.firstRow == null) {
            throw new RuntimeException("terminalStyle.firstRow is missing");
        }
        if (this.row == null) {
            throw new RuntimeException("terminalStyle.row is missing");
        }
        if (this.lastRow == null) {
            throw new RuntimeException("terminalStyle.lastRow is missing");
        }
        if (this.bottom == null) {
            throw new RuntimeException("terminalStyle.bottom is missing");
        }
    }
}

