/*
 * Decompiled with CFR 0.152.
 */
package appeng.client.gui.layout;

import appeng.client.Point;

public enum SlotGridLayout {
    IO_BUS_CONFIG{
        private final Point[] OFFSETS = new Point[]{new Point(0, 0), new Point(-18, 0), new Point(18, 0), new Point(0, -18), new Point(0, 18), new Point(-18, -18), new Point(18, -18), new Point(-18, 18), new Point(18, 18)};

        @Override
        public Point getPosition(int x, int y, int semanticIdx) {
            return this.OFFSETS[Math.max(semanticIdx, 0) % this.OFFSETS.length].move(x, y);
        }
    }
    ,
    BREAK_AFTER_9COLS{

        @Override
        public Point getPosition(int x, int y, int semanticIdx) {
            return SlotGridLayout.getRowBreakPosition(x, y, semanticIdx, 9);
        }
    }
    ,
    BREAK_AFTER_3COLS{

        @Override
        public Point getPosition(int x, int y, int semanticIdx) {
            return SlotGridLayout.getRowBreakPosition(x, y, semanticIdx, 3);
        }
    }
    ,
    BREAK_AFTER_2COLS{

        @Override
        public Point getPosition(int x, int y, int semanticIdx) {
            return SlotGridLayout.getRowBreakPosition(x, y, semanticIdx, 2);
        }
    }
    ,
    HORIZONTAL{

        @Override
        public Point getPosition(int x, int y, int semanticIdx) {
            return new Point(x, y).move(semanticIdx * 18, 0);
        }
    }
    ,
    VERTICAL{

        @Override
        public Point getPosition(int x, int y, int semanticIdx) {
            return new Point(x, y).move(0, semanticIdx * 18);
        }
    };


    private static Point getRowBreakPosition(int x, int y, int semanticIdx, int cols) {
        int row = semanticIdx / cols;
        int col = semanticIdx % cols;
        return new Point(x, y).move(col * 18, row * 18);
    }

    public abstract Point getPosition(int var1, int var2, int var3);
}

