/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Direction
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.context.BlockPlaceContext
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.BlockStateProperties
 *  net.minecraft.world.level.block.state.properties.Property
 */
package appeng.api.orientation;

import appeng.api.orientation.IOrientationStrategy;
import java.util.Collection;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;

public class FacingWithSpinStrategy
implements IOrientationStrategy {
    private final List<Property<?>> properties = List.of(BlockStateProperties.FACING, SPIN);

    protected FacingWithSpinStrategy() {
    }

    @Override
    public Direction getFacing(BlockState state) {
        return (Direction)state.getValue((Property)BlockStateProperties.FACING);
    }

    @Override
    public int getSpin(BlockState state) {
        return (Integer)state.getValue((Property)SPIN);
    }

    @Override
    public BlockState setFacing(BlockState state, Direction facing) {
        return (BlockState)state.setValue((Property)BlockStateProperties.FACING, (Comparable)facing);
    }

    @Override
    public BlockState setSpin(BlockState state, int spin) {
        return (BlockState)state.setValue((Property)SPIN, (Comparable)Integer.valueOf(spin));
    }

    @Override
    public BlockState getStateForPlacement(BlockState state, BlockPlaceContext context) {
        Direction up = Direction.UP;
        Direction forward = context.getHorizontalDirection().getOpposite();
        Player player = context.getPlayer();
        if (player != null) {
            if (player.getXRot() > 65.0f) {
                up = forward.getOpposite();
                forward = Direction.UP;
            } else if (player.getXRot() < -65.0f) {
                up = forward.getOpposite();
                forward = Direction.DOWN;
            }
        }
        return this.setOrientation(state, forward, up);
    }

    @Override
    public Collection<Property<?>> getProperties() {
        return this.properties;
    }
}

