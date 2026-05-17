/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.math.Transformation
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Direction$Axis
 *  net.minecraft.core.Direction$AxisDirection
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.state.BlockState
 *  org.joml.Matrix4f
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 */
package appeng.api.orientation;

import appeng.api.orientation.IOrientationStrategy;
import appeng.api.orientation.RelativeSide;
import com.mojang.math.Transformation;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;

public enum BlockOrientation {
    DOWN_NORTH(90, 0, 0, 0),
    DOWN_WEST(90, 0, 270, 1),
    DOWN_SOUTH(90, 0, 180, 2),
    DOWN_EAST(90, 0, 90, 3),
    UP_NORTH(270, 0, 180, 0),
    UP_EAST(270, 0, 90, 1),
    UP_SOUTH(270, 0, 0, 2),
    UP_WEST(270, 0, 270, 3),
    NORTH_UP(0, 0, 0, 0),
    NORTH_WEST(0, 0, 270, 1),
    NORTH_DOWN(0, 0, 180, 2),
    NORTH_EAST(0, 0, 90, 3),
    SOUTH_UP(0, 180, 0, 0),
    SOUTH_EAST(0, 180, 90, 1),
    SOUTH_DOWN(0, 180, 180, 2),
    SOUTH_WEST(0, 180, 270, 3),
    WEST_UP(0, 270, 0, 0),
    WEST_SOUTH(0, 270, 270, 1),
    WEST_DOWN(0, 270, 180, 2),
    WEST_NORTH(0, 270, 90, 3),
    EAST_UP(0, 90, 0, 0),
    EAST_NORTH(0, 90, 270, 1),
    EAST_DOWN(0, 90, 180, 2),
    EAST_SOUTH(0, 90, 90, 3);

    private final int angleX;
    private final int angleY;
    private final int angleZ;
    private final Quaternionf quaternion;
    private final Transformation transformation;
    private final int spin;
    private final Direction[] rotatedSideTo;
    private final Direction[] rotatedSideFrom;

    private BlockOrientation(int angleX, int angleY, int angleZ, int spin) {
        this.angleX = angleX;
        this.angleY = angleY;
        this.angleZ = angleZ;
        this.quaternion = new Quaternionf().rotateYXZ((float)(-angleY) * ((float)Math.PI / 180), (float)(-angleX) * ((float)Math.PI / 180), (float)(-angleZ) * ((float)Math.PI / 180));
        if (angleX == 0 && angleY == 0 && angleZ == 0) {
            this.transformation = Transformation.identity();
        } else {
            Matrix4f rotationMatrix = new Matrix4f().identity().rotate((Quaternionfc)this.quaternion);
            this.transformation = new Transformation(rotationMatrix);
        }
        this.spin = spin;
        this.rotatedSideTo = new Direction[Direction.values().length];
        this.rotatedSideFrom = new Direction[Direction.values().length];
        for (Direction direction : Direction.values()) {
            Direction rotatedTo;
            Vector3f normal = direction.step();
            normal.rotate((Quaternionfc)this.quaternion);
            this.rotatedSideTo[direction.ordinal()] = rotatedTo = Direction.getNearest((float)normal.x(), (float)normal.y(), (float)normal.z());
            this.rotatedSideFrom[rotatedTo.ordinal()] = direction;
        }
    }

    public void setOn(BlockEntity be) {
        this.setOn(be.getLevel(), be.getBlockPos());
    }

    public void setOn(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        IOrientationStrategy strategy = IOrientationStrategy.get(state);
        BlockState newState = strategy.setOrientation(state, this.getSide(RelativeSide.FRONT), this.getSpin());
        if (newState != state) {
            level.setBlockAndUpdate(pos, newState);
        }
    }

    public boolean isRedundant() {
        return this.angleX == 0 && this.angleY == 0 && this.angleZ == 0;
    }

    public Quaternionf getQuaternion() {
        return this.quaternion;
    }

    public Transformation getTransformation() {
        return this.transformation;
    }

    public Direction rotate(Direction facing) {
        return this.rotatedSideTo[facing.ordinal()];
    }

    public Direction resultingRotate(Direction facing) {
        return this.rotatedSideFrom[facing.ordinal()];
    }

    public int getAngleX() {
        return this.angleX;
    }

    public int getAngleY() {
        return this.angleY;
    }

    public int getAngleZ() {
        return this.angleZ;
    }

    public int getSpin() {
        return this.spin;
    }

    public static BlockOrientation get(Direction facing) {
        return BlockOrientation.get(facing, 0);
    }

    public static BlockOrientation get(Direction front, Direction top) {
        int offset;
        for (int i = offset = front.ordinal() * 4; i < offset + 4; ++i) {
            BlockOrientation orientation = BlockOrientation.values()[i];
            if (orientation.getSide(RelativeSide.TOP) != top) continue;
            return orientation;
        }
        return BlockOrientation.values()[offset];
    }

    public static BlockOrientation get(Direction facing, int spin) {
        return BlockOrientation.values()[facing.ordinal() * 4 + spin];
    }

    public static BlockOrientation get(BlockEntity blockEntity) {
        BlockState blockState = blockEntity.getBlockState();
        return BlockOrientation.get(blockState);
    }

    public static BlockOrientation get(BlockState state) {
        IOrientationStrategy strategy = IOrientationStrategy.get(state);
        return BlockOrientation.get(strategy, state);
    }

    public static BlockOrientation get(IOrientationStrategy strategy, BlockState state) {
        Direction facing = strategy.getFacing(state);
        int spin = strategy.getSpin(state);
        return BlockOrientation.get(facing, spin);
    }

    public Direction getSide(RelativeSide side) {
        return this.rotate(side.getUnrotatedSide());
    }

    public RelativeSide getRelativeSide(Direction side) {
        return RelativeSide.fromUnrotatedSide(this.resultingRotate(side));
    }

    public Set<Direction> getSides(Set<RelativeSide> relativeSides) {
        EnumSet<Direction> result = EnumSet.noneOf(Direction.class);
        for (RelativeSide relativeSide : relativeSides) {
            result.add(this.getSide(relativeSide));
        }
        return result;
    }

    public Set<RelativeSide> getRelativeSides(Set<Direction> sides) {
        EnumSet<RelativeSide> result = EnumSet.noneOf(RelativeSide.class);
        for (Direction side : sides) {
            result.add(this.getRelativeSide(side));
        }
        return result;
    }

    public BlockOrientation rotateClockwiseAround(Direction side) {
        return this.rotateClockwiseAround(side.getAxis(), side.getAxisDirection());
    }

    public BlockOrientation rotateClockwiseAround(Direction.Axis axis, Direction.AxisDirection direction) {
        Direction newUp;
        Direction newFacing;
        Direction facing = this.getSide(RelativeSide.FRONT);
        Direction up = this.getSide(RelativeSide.TOP);
        if (direction == Direction.AxisDirection.POSITIVE) {
            newFacing = facing.getClockWise(axis);
            newUp = up.getClockWise(axis);
        } else {
            newFacing = facing.getCounterClockWise(axis);
            newUp = up.getCounterClockWise(axis);
        }
        return BlockOrientation.get(newFacing, newUp);
    }
}

