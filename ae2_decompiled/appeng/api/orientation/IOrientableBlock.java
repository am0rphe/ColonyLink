/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Direction
 *  net.minecraft.world.level.block.state.BlockState
 */
package appeng.api.orientation;

import appeng.api.orientation.BlockOrientation;
import appeng.api.orientation.IOrientationStrategy;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public interface IOrientableBlock {
    public IOrientationStrategy getOrientationStrategy();

    default public BlockOrientation getOrientation(BlockState state) {
        IOrientationStrategy strategy = this.getOrientationStrategy();
        Direction facing = strategy.getFacing(state);
        int spin = strategy.getSpin(state);
        return BlockOrientation.get(facing, spin);
    }
}

