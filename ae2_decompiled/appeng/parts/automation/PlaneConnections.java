/*
 * Decompiled with CFR 0.152.
 */
package appeng.parts.automation;

import java.util.ArrayList;
import java.util.List;

public final class PlaneConnections {
    private final boolean up;
    private final boolean right;
    private final boolean down;
    private final boolean left;
    private static final int BITMASK_UP = 8;
    private static final int BITMASK_RIGHT = 4;
    private static final int BITMASK_DOWN = 2;
    private static final int BITMASK_LEFT = 1;
    public static final List<PlaneConnections> PERMUTATIONS = PlaneConnections.generatePermutations();

    private static List<PlaneConnections> generatePermutations() {
        ArrayList<PlaneConnections> connections = new ArrayList<PlaneConnections>(16);
        for (int i = 0; i < 16; ++i) {
            boolean up = (i & 8) != 0;
            boolean right = (i & 4) != 0;
            boolean down = (i & 2) != 0;
            boolean left = (i & 1) != 0;
            connections.add(new PlaneConnections(up, right, down, left));
        }
        return connections;
    }

    private PlaneConnections(boolean up, boolean right, boolean down, boolean left) {
        this.up = up;
        this.right = right;
        this.down = down;
        this.left = left;
    }

    public static PlaneConnections of(boolean up, boolean right, boolean down, boolean left) {
        return PERMUTATIONS.get(PlaneConnections.getIndex(up, right, down, left));
    }

    public boolean isUp() {
        return this.up;
    }

    public boolean isRight() {
        return this.right;
    }

    public boolean isDown() {
        return this.down;
    }

    public boolean isLeft() {
        return this.left;
    }

    public int getIndex() {
        return PlaneConnections.getIndex(this.up, this.right, this.down, this.left);
    }

    private static int getIndex(boolean up, boolean right, boolean down, boolean left) {
        return (up ? 8 : 0) + (right ? 4 : 0) + (left ? 1 : 0) + (down ? 2 : 0);
    }

    public boolean equals(Object o) {
        return this == o;
    }

    public int hashCode() {
        int result = this.up ? 1 : 0;
        result = 31 * result + (this.right ? 1 : 0);
        result = 31 * result + (this.down ? 1 : 0);
        result = 31 * result + (this.left ? 1 : 0);
        return result;
    }
}

