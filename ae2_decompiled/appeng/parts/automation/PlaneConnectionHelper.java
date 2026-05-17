/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  org.jetbrains.annotations.Nullable
 */
package appeng.parts.automation;

import appeng.api.parts.IPart;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartHost;
import appeng.parts.AEBasePart;
import appeng.parts.automation.PlaneConnections;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public final class PlaneConnectionHelper {
    private final AEBasePart part;

    public PlaneConnectionHelper(AEBasePart part) {
        this.part = part;
    }

    public PlaneConnections getConnections() {
        Direction facingUp;
        Direction facingRight;
        BlockEntity hostBlockEntity = this.getHostBlockEntity();
        Direction side = this.part.getSide();
        switch (side) {
            case UP: {
                facingRight = Direction.EAST;
                facingUp = Direction.NORTH;
                break;
            }
            case DOWN: {
                facingRight = Direction.WEST;
                facingUp = Direction.NORTH;
                break;
            }
            case NORTH: {
                facingRight = Direction.WEST;
                facingUp = Direction.UP;
                break;
            }
            case SOUTH: {
                facingRight = Direction.EAST;
                facingUp = Direction.UP;
                break;
            }
            case WEST: {
                facingRight = Direction.SOUTH;
                facingUp = Direction.UP;
                break;
            }
            case EAST: {
                facingRight = Direction.NORTH;
                facingUp = Direction.UP;
                break;
            }
            default: {
                return PlaneConnections.of(false, false, false, false);
            }
        }
        boolean left = false;
        boolean right = false;
        boolean down = false;
        boolean up = false;
        if (hostBlockEntity != null) {
            BlockPos pos;
            Level level = hostBlockEntity.getLevel();
            if (this.isCompatiblePlaneAdjacent(level.getBlockEntity((pos = hostBlockEntity.getBlockPos()).relative(facingRight.getOpposite())))) {
                left = true;
            }
            if (this.isCompatiblePlaneAdjacent(level.getBlockEntity(pos.relative(facingRight)))) {
                right = true;
            }
            if (this.isCompatiblePlaneAdjacent(level.getBlockEntity(pos.relative(facingUp.getOpposite())))) {
                down = true;
            }
            if (this.isCompatiblePlaneAdjacent(level.getBlockEntity(pos.relative(facingUp)))) {
                up = true;
            }
        }
        return PlaneConnections.of(up, right, down, left);
    }

    public void getBoxes(IPartCollisionHelper bch) {
        boolean minX = true;
        boolean minY = true;
        int maxX = 15;
        int maxY = 15;
        BlockEntity hostEntity = this.getHostBlockEntity();
        if (hostEntity != null) {
            Level level = hostEntity.getLevel();
            BlockPos pos = hostEntity.getBlockPos();
            Direction e = bch.getWorldX();
            Direction u = bch.getWorldY();
            if (this.isCompatiblePlaneAdjacent(level.getBlockEntity(pos.relative(e.getOpposite())))) {
                minX = false;
            }
            if (this.isCompatiblePlaneAdjacent(level.getBlockEntity(pos.relative(e)))) {
                maxX = 16;
            }
            if (this.isCompatiblePlaneAdjacent(level.getBlockEntity(pos.relative(u.getOpposite())))) {
                minY = false;
            }
            if (this.isCompatiblePlaneAdjacent(level.getBlockEntity(pos.relative(u)))) {
                maxY = 16;
            }
        }
        bch.addBox(5.0, 5.0, 14.0, 11.0, 11.0, 15.0);
        bch.addBox((double)minX, (double)minY, 15.0, maxX, maxY, 16.0);
    }

    public void updateConnections() {
        BlockEntity host = this.getHostBlockEntity();
        if (host != null) {
            host.requestModelDataUpdate();
        }
    }

    private boolean isCompatiblePlaneAdjacent(@Nullable BlockEntity adjacentBlockEntity) {
        if (adjacentBlockEntity instanceof IPartHost) {
            IPart p = ((IPartHost)adjacentBlockEntity).getPart(this.part.getSide());
            return p != null && p.getClass() == this.part.getClass();
        }
        return false;
    }

    private BlockEntity getHostBlockEntity() {
        IPartHost host = this.part.getHost();
        if (host != null) {
            return host.getBlockEntity();
        }
        return null;
    }
}

