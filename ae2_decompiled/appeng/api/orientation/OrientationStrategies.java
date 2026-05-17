/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.level.block.state.properties.BlockStateProperties
 */
package appeng.api.orientation;

import appeng.api.orientation.FacingStrategy;
import appeng.api.orientation.FacingWithSpinStrategy;
import appeng.api.orientation.HorizontalFacingStrategy;
import appeng.api.orientation.IOrientationStrategy;
import appeng.api.orientation.NoOrientationStrategy;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public final class OrientationStrategies {
    private static final IOrientationStrategy none = new NoOrientationStrategy();
    private static final IOrientationStrategy horizontalFacing = new HorizontalFacingStrategy();
    private static final IOrientationStrategy facing = new FacingStrategy(BlockStateProperties.FACING);
    private static final IOrientationStrategy facingNoPlayerRotation = new FacingStrategy(BlockStateProperties.FACING, false);
    private static final IOrientationStrategy full = new FacingWithSpinStrategy();

    public static IOrientationStrategy none() {
        return none;
    }

    public static IOrientationStrategy horizontalFacing() {
        return horizontalFacing;
    }

    public static IOrientationStrategy facing() {
        return facing;
    }

    public static IOrientationStrategy facingNoPlayerRotation() {
        return facingNoPlayerRotation;
    }

    public static IOrientationStrategy full() {
        return full;
    }
}

