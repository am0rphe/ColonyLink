/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Direction
 *  net.minecraft.world.item.context.BlockPlaceContext
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.IntegerProperty
 *  net.minecraft.world.level.block.state.properties.Property
 */
package appeng.api.orientation;

import appeng.api.orientation.BlockOrientation;
import appeng.api.orientation.IOrientableBlock;
import appeng.api.orientation.OrientationStrategies;
import appeng.api.orientation.RelativeSide;
import appeng.block.orientation.SpinMapping;
import java.util.Collection;
import java.util.stream.Stream;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;

public interface IOrientationStrategy {
    public static final IntegerProperty SPIN = IntegerProperty.create((String)"spin", (int)0, (int)3);

    public static IOrientationStrategy get(BlockState state) {
        Block block = state.getBlock();
        if (block instanceof IOrientableBlock) {
            IOrientableBlock orientableBlock = (IOrientableBlock)block;
            return orientableBlock.getOrientationStrategy();
        }
        return OrientationStrategies.none();
    }

    default public Direction getFacing(BlockState state) {
        return Direction.NORTH;
    }

    default public int getSpin(BlockState state) {
        return 0;
    }

    default public BlockState setFacing(BlockState state, Direction facing) {
        return state;
    }

    default public BlockState setSpin(BlockState state, int spin) {
        return state;
    }

    default public BlockState setUp(BlockState state, Direction up) {
        Direction facing = this.getFacing(state);
        int spin = SpinMapping.getSpinFromUp(facing, up);
        return this.setSpin(state, spin);
    }

    default public BlockState setOrientation(BlockState state, Direction facing, int spin) {
        return this.setSpin(this.setFacing(state, facing), spin);
    }

    default public BlockState setOrientation(BlockState state, Direction facing, Direction up) {
        return this.setUp(this.setFacing(state, facing), up);
    }

    default public Direction getSide(BlockState state, RelativeSide side) {
        return BlockOrientation.get(this, state).rotate(side.getUnrotatedSide());
    }

    default public BlockState getStateForPlacement(BlockState state, BlockPlaceContext context) {
        return state;
    }

    default public Stream<BlockState> getAllStates(BlockState baseState) {
        Stream<BlockState> result = Stream.of(baseState);
        for (Property<?> property : this.getProperties()) {
            result = IOrientationStrategy.enumerateValues(result, property);
        }
        return result;
    }

    default public boolean allowsPlayerRotation() {
        return true;
    }

    public Collection<Property<?>> getProperties();

    private static <T extends Comparable<T>> Stream<BlockState> enumerateValues(Stream<BlockState> stream, Property<T> property) {
        return stream.flatMap(baseState -> property.getPossibleValues().stream().map(value -> (BlockState)baseState.setValue(property, value)));
    }
}

