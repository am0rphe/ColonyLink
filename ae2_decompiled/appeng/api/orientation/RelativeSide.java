/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Direction
 */
package appeng.api.orientation;

import net.minecraft.core.Direction;

public enum RelativeSide {
    FRONT(Direction.NORTH),
    BACK(Direction.SOUTH),
    TOP(Direction.UP),
    BOTTOM(Direction.DOWN),
    LEFT(Direction.WEST),
    RIGHT(Direction.EAST);

    private static final RelativeSide[] BY_UNROTATED_SIDE;
    private final Direction unrotatedSide;

    private RelativeSide(Direction unrotatedSide) {
        this.unrotatedSide = unrotatedSide;
    }

    public static RelativeSide fromUnrotatedSide(Direction side) {
        return BY_UNROTATED_SIDE[side.ordinal()];
    }

    public Direction getUnrotatedSide() {
        return this.unrotatedSide;
    }

    static {
        BY_UNROTATED_SIDE = new RelativeSide[Direction.values().length];
        RelativeSide[] relativeSideArray = RelativeSide.values();
        int n = relativeSideArray.length;
        for (int i = 0; i < n; ++i) {
            RelativeSide side;
            RelativeSide.BY_UNROTATED_SIDE[side.unrotatedSide.ordinal()] = side = relativeSideArray[i];
        }
    }
}

