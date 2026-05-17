/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.stream.JsonWriter
 *  it.unimi.dsi.fastutil.objects.Reference2IntMap
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.BlockPos$MutableBlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Vec3i
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.level.Level
 */
package appeng.me;

import appeng.api.networking.GridFlags;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.util.AEColor;
import appeng.core.AELog;
import appeng.me.GridConnection;
import appeng.me.GridNode;
import com.google.gson.stream.JsonWriter;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import java.io.IOException;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public class InWorldGridNode
extends GridNode {
    private final BlockPos location;
    private final EnumSet<Direction> exposedOnSides = EnumSet.noneOf(Direction.class);

    public <T> InWorldGridNode(ServerLevel level, BlockPos location, T owner, IGridNodeListener<T> listener, Set<GridFlags> flags) {
        super(level, owner, listener, flags);
        this.location = location;
    }

    @Override
    protected void findInWorldConnections() {
        this.cleanupConnections();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        block0: for (Direction direction : this.exposedOnSides) {
            pos.setWithOffset((Vec3i)this.location, direction);
            GridNode adjacentNode = (GridNode)GridHelper.getExposedNode((Level)this.getLevel(), (BlockPos)pos, direction.getOpposite());
            if (adjacentNode == null || !this.hasCompatibleColor(adjacentNode)) continue;
            for (GridConnection c : this.connections) {
                if (!c.isInWorld() || c.getDirection(this) != direction) continue;
                IGridNode os = c.getOtherSide(this);
                if (os == adjacentNode) continue block0;
                AELog.warn("Grid node %s did not disconnect properly and is now replaced with %s", os, adjacentNode);
                c.destroy();
                break;
            }
            GridConnection.create(this, adjacentNode, direction);
        }
    }

    @Override
    public String toString() {
        return super.toString() + " @ " + this.location.getX() + "," + this.location.getY() + "," + this.location.getZ();
    }

    @Override
    protected void exportProperties(JsonWriter writer, Reference2IntMap<Object> machineIds, Reference2IntMap<IGridNode> nodeIds) throws IOException {
        super.exportProperties(writer, machineIds, nodeIds);
        writer.name("location");
        writer.beginArray();
        writer.value((long)this.location.getX());
        writer.value((long)this.location.getY());
        writer.value((long)this.location.getZ());
        writer.endArray();
        writer.name("exposedSides");
        StringBuilder sidesSet = new StringBuilder();
        for (Direction side : this.exposedOnSides) {
            sidesSet.append(side.name().charAt(0));
        }
        writer.value(sidesSet.toString());
    }

    private void cleanupConnections() {
        for (IGridConnection connection : this.getConnections()) {
            InWorldGridNode otherInWorldNode;
            if (!connection.isInWorld()) continue;
            Direction ourSide = connection.getDirection(this);
            if (!this.isExposedOnSide(ourSide)) {
                connection.destroy();
                continue;
            }
            Direction theirSide = ourSide.getOpposite();
            IGridNode otherNode = connection.getOtherSide(this);
            if (otherNode instanceof InWorldGridNode && (otherInWorldNode = (InWorldGridNode)otherNode).isExposedOnSide(theirSide) && this.hasCompatibleColor(otherNode)) continue;
            connection.destroy();
        }
    }

    private boolean hasCompatibleColor(IGridNode otherNode) {
        AEColor ourColor = this.getGridColor();
        AEColor theirColor = otherNode.getGridColor();
        return ourColor == AEColor.TRANSPARENT || theirColor == AEColor.TRANSPARENT || ourColor == theirColor;
    }

    public BlockPos getLocation() {
        return this.location;
    }

    public void setExposedOnSides(Set<Direction> directions) {
        if (!this.exposedOnSides.equals(directions)) {
            this.exposedOnSides.clear();
            this.exposedOnSides.addAll(directions);
            this.updateState();
        }
    }

    public boolean isExposedOnSide(Direction side) {
        return this.getMyGrid() != null && this.exposedOnSides.contains(side);
    }
}

