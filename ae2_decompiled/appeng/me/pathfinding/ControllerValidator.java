/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 */
package appeng.me.pathfinding;

import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridVisitor;
import appeng.api.networking.pathing.ControllerState;
import appeng.blockentity.networking.ControllerBlockEntity;
import java.util.Collection;
import java.util.HashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public class ControllerValidator
implements IGridVisitor {
    public static final int MAX_SIZE = 7;
    private boolean valid = true;
    private int found = 0;
    private int minX;
    private int minY;
    private int minZ;
    private int maxX;
    private int maxY;
    private int maxZ;

    private ControllerValidator(BlockPos pos) {
        this.minX = pos.getX();
        this.maxX = pos.getX();
        this.minY = pos.getY();
        this.maxY = pos.getY();
        this.minZ = pos.getZ();
        this.maxZ = pos.getZ();
    }

    public static ControllerState calculateState(Collection<ControllerBlockEntity> controllers) {
        if (controllers.isEmpty()) {
            return ControllerState.NO_CONTROLLER;
        }
        ControllerBlockEntity startingController = controllers.iterator().next();
        IGridNode startingNode = startingController.getGridNode();
        if (startingNode == null) {
            return ControllerState.CONTROLLER_CONFLICT;
        }
        ControllerValidator cv = new ControllerValidator(startingController.getBlockPos());
        startingNode.beginVisit(cv);
        if (!cv.isValid()) {
            return ControllerState.CONTROLLER_CONFLICT;
        }
        if (cv.getFound() != controllers.size()) {
            return ControllerState.CONTROLLER_CONFLICT;
        }
        if (ControllerValidator.hasControllerCross(controllers)) {
            return ControllerState.CONTROLLER_CONFLICT;
        }
        return ControllerState.CONTROLLER_ONLINE;
    }

    @Override
    public boolean visitNode(IGridNode node) {
        Object object;
        if (this.isValid() && (object = node.getOwner()) instanceof ControllerBlockEntity) {
            ControllerBlockEntity c = (ControllerBlockEntity)object;
            BlockPos pos = c.getBlockPos();
            this.minX = Math.min(pos.getX(), this.minX);
            this.maxX = Math.max(pos.getX(), this.maxX);
            this.minY = Math.min(pos.getY(), this.minY);
            this.maxY = Math.max(pos.getY(), this.maxY);
            this.minZ = Math.min(pos.getZ(), this.minZ);
            this.maxZ = Math.max(pos.getZ(), this.maxZ);
            if (this.maxX - this.minX < 7 && this.maxY - this.minY < 7 && this.maxZ - this.minZ < 7) {
                ++this.found;
                return true;
            }
            this.valid = false;
        }
        return false;
    }

    private static boolean hasControllerCross(Collection<ControllerBlockEntity> controllers) {
        HashSet<BlockPos> posSet = new HashSet<BlockPos>(controllers.size());
        for (ControllerBlockEntity controller : controllers) {
            posSet.add(controller.getBlockPos().immutable());
        }
        for (BlockPos pos : posSet) {
            boolean upDown;
            boolean eastWest;
            boolean northSouth = posSet.contains(pos.relative(Direction.NORTH)) && posSet.contains(pos.relative(Direction.SOUTH));
            if ((northSouth ? 1 : 0) + ((eastWest = posSet.contains(pos.relative(Direction.EAST)) && posSet.contains(pos.relative(Direction.WEST))) ? 1 : 0) + ((upDown = posSet.contains(pos.relative(Direction.UP)) && posSet.contains(pos.relative(Direction.DOWN))) ? 1 : 0) <= 1) continue;
            return true;
        }
        return false;
    }

    public boolean isValid() {
        return this.valid;
    }

    public int getFound() {
        return this.found;
    }
}

