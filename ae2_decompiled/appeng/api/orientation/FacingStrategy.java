/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Direction
 *  net.minecraft.world.item.context.BlockPlaceContext
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.DirectionProperty
 *  net.minecraft.world.level.block.state.properties.Property
 */
package appeng.api.orientation;

import appeng.api.orientation.IOrientationStrategy;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;

public class FacingStrategy
implements IOrientationStrategy {
    private final DirectionProperty property;
    private final List<Property<?>> properties;
    private final boolean allowsPlayerRotation;

    protected FacingStrategy(DirectionProperty property) {
        this(property, true);
    }

    protected FacingStrategy(DirectionProperty property, boolean allowsPlayerRotation) {
        this.property = property;
        this.properties = Collections.singletonList(property);
        this.allowsPlayerRotation = allowsPlayerRotation;
    }

    @Override
    public Direction getFacing(BlockState state) {
        return (Direction)state.getValue((Property)this.property);
    }

    @Override
    public BlockState setFacing(BlockState state, Direction facing) {
        if (!this.property.getPossibleValues().contains(facing)) {
            return state;
        }
        return (BlockState)state.setValue((Property)this.property, (Comparable)facing);
    }

    @Override
    public BlockState getStateForPlacement(BlockState state, BlockPlaceContext context) {
        return this.setFacing(state, context.getClickedFace());
    }

    @Override
    public boolean allowsPlayerRotation() {
        return this.allowsPlayerRotation;
    }

    @Override
    public Collection<Property<?>> getProperties() {
        return this.properties;
    }
}

