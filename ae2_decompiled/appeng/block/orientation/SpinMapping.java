/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Direction
 */
package appeng.block.orientation;

import net.minecraft.core.Direction;

public final class SpinMapping {
    private static final Direction[][] SPIN_DIRECTIONS = new Direction[][]{{Direction.NORTH, Direction.WEST, Direction.SOUTH, Direction.EAST}, {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST}, {Direction.UP, Direction.WEST, Direction.DOWN, Direction.EAST}, {Direction.UP, Direction.EAST, Direction.DOWN, Direction.WEST}, {Direction.UP, Direction.SOUTH, Direction.DOWN, Direction.NORTH}, {Direction.UP, Direction.NORTH, Direction.DOWN, Direction.SOUTH}};

    private SpinMapping() {
    }

    public static int getSpinFromUp(Direction facing, Direction up) {
        Direction[] spinDirs = SPIN_DIRECTIONS[facing.ordinal()];
        for (int i = 0; i < spinDirs.length; ++i) {
            if (spinDirs[i] != up) continue;
            return i;
        }
        return 0;
    }

    public static Direction getUpFromSpin(Direction facing, int spin) {
        return SPIN_DIRECTIONS[facing.ordinal()][spin];
    }
}

