/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Direction
 */
package appeng.decorative.solid;

import java.util.Arrays;
import java.util.Objects;
import net.minecraft.core.Direction;

public final class GlassState {
    public static final GlassState DEFAULT;
    private final int[] masks;
    private final boolean[] adjacentGlassBlocks;

    public GlassState(int[] masks, boolean[] adjacentGlassBlocks) {
        this.masks = (int[])masks.clone();
        this.adjacentGlassBlocks = (boolean[])adjacentGlassBlocks.clone();
    }

    public int getMask(Direction side) {
        return this.masks[side.get3DDataValue()];
    }

    public boolean hasAdjacentGlassBlock(Direction side) {
        return this.adjacentGlassBlocks[side.get3DDataValue()];
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof GlassState)) {
            return false;
        }
        GlassState that = (GlassState)o;
        return Arrays.equals(this.masks, that.masks) && Arrays.equals(this.adjacentGlassBlocks, that.adjacentGlassBlocks);
    }

    public int hashCode() {
        return Objects.hash(Arrays.hashCode(this.masks), Arrays.hashCode(this.adjacentGlassBlocks));
    }

    static {
        int[] masks = new int[6];
        Arrays.fill(masks, 15);
        boolean[] adjacentGlassBlocks = new boolean[6];
        DEFAULT = new GlassState(masks, adjacentGlassBlocks);
    }
}

