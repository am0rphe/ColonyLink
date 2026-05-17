/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Direction
 *  net.minecraft.world.phys.AABB
 *  org.jetbrains.annotations.Nullable
 */
package appeng.parts;

import appeng.api.parts.IPartCollisionHelper;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

public class BusCollisionHelper
implements IPartCollisionHelper {
    private final List<AABB> boxes;
    private final Direction x;
    private final Direction y;
    private final Direction z;
    private final boolean isVisual;

    public BusCollisionHelper(List<AABB> boxes, Direction x, Direction y, Direction z, boolean visual) {
        this.boxes = boxes;
        this.x = x;
        this.y = y;
        this.z = z;
        this.isVisual = visual;
    }

    public BusCollisionHelper(List<AABB> boxes, @Nullable Direction s, boolean visual) {
        this.boxes = boxes;
        this.isVisual = visual;
        if (s == null) {
            this.x = Direction.EAST;
            this.y = Direction.UP;
            this.z = Direction.SOUTH;
        } else {
            switch (s) {
                case DOWN: {
                    this.x = Direction.EAST;
                    this.y = Direction.NORTH;
                    this.z = Direction.DOWN;
                    break;
                }
                case UP: {
                    this.x = Direction.EAST;
                    this.y = Direction.SOUTH;
                    this.z = Direction.UP;
                    break;
                }
                case EAST: {
                    this.x = Direction.SOUTH;
                    this.y = Direction.UP;
                    this.z = Direction.EAST;
                    break;
                }
                case WEST: {
                    this.x = Direction.NORTH;
                    this.y = Direction.UP;
                    this.z = Direction.WEST;
                    break;
                }
                case NORTH: {
                    this.x = Direction.WEST;
                    this.y = Direction.UP;
                    this.z = Direction.NORTH;
                    break;
                }
                case SOUTH: {
                    this.x = Direction.EAST;
                    this.y = Direction.UP;
                    this.z = Direction.SOUTH;
                    break;
                }
                default: {
                    this.x = Direction.EAST;
                    this.y = Direction.UP;
                    this.z = Direction.SOUTH;
                }
            }
        }
    }

    @Override
    public void addBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        double aX = (minX /= 16.0) * (double)this.x.getStepX() + (minY /= 16.0) * (double)this.y.getStepX() + (minZ /= 16.0) * (double)this.z.getStepX();
        double aY = minX * (double)this.x.getStepY() + minY * (double)this.y.getStepY() + minZ * (double)this.z.getStepY();
        double aZ = minX * (double)this.x.getStepZ() + minY * (double)this.y.getStepZ() + minZ * (double)this.z.getStepZ();
        double bX = (maxX /= 16.0) * (double)this.x.getStepX() + (maxY /= 16.0) * (double)this.y.getStepX() + (maxZ /= 16.0) * (double)this.z.getStepX();
        double bY = maxX * (double)this.x.getStepY() + maxY * (double)this.y.getStepY() + maxZ * (double)this.z.getStepY();
        double bZ = maxX * (double)this.x.getStepZ() + maxY * (double)this.y.getStepZ() + maxZ * (double)this.z.getStepZ();
        if (this.x.getStepX() + this.y.getStepX() + this.z.getStepX() < 0) {
            aX += 1.0;
            bX += 1.0;
        }
        if (this.x.getStepY() + this.y.getStepY() + this.z.getStepY() < 0) {
            aY += 1.0;
            bY += 1.0;
        }
        if (this.x.getStepZ() + this.y.getStepZ() + this.z.getStepZ() < 0) {
            aZ += 1.0;
            bZ += 1.0;
        }
        minX = Math.min(aX, bX);
        minY = Math.min(aY, bY);
        minZ = Math.min(aZ, bZ);
        maxX = Math.max(aX, bX);
        maxY = Math.max(aY, bY);
        maxZ = Math.max(aZ, bZ);
        this.boxes.add(new AABB(minX, minY, minZ, maxX, maxY, maxZ));
    }

    @Override
    public Direction getWorldX() {
        return this.x;
    }

    @Override
    public Direction getWorldY() {
        return this.y;
    }

    @Override
    public Direction getWorldZ() {
        return this.z;
    }

    @Override
    public boolean isBBCollision() {
        return !this.isVisual;
    }
}

