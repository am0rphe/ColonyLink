/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Preconditions
 *  com.google.common.collect.ImmutableList
 *  net.minecraft.core.Direction
 *  org.jetbrains.annotations.Nullable
 */
package appeng.me;

import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.pathing.ChannelMode;
import appeng.api.networking.pathing.IPathingService;
import appeng.me.Grid;
import appeng.me.GridNode;
import appeng.me.GridPropagator;
import appeng.me.pathfinding.IPathItem;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.util.Objects;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

public class GridConnection
implements IGridConnection,
IPathItem {
    int usedChannels = 0;
    private int lastUsedChannels = 0;
    private Object visitorIterationNumber = null;
    private GridNode sideA;
    @Nullable
    private Direction fromAtoB;
    private GridNode sideB;

    private GridConnection(GridNode aNode, GridNode bNode, @Nullable Direction fromAtoB) {
        this.sideA = aNode;
        this.fromAtoB = fromAtoB;
        this.sideB = bNode;
    }

    @Override
    public IGridNode getOtherSide(IGridNode gridNode) {
        if (gridNode == this.sideA) {
            return this.sideB;
        }
        if (gridNode == this.sideB) {
            return this.sideA;
        }
        throw new IllegalArgumentException("The given grid node does not participate in this connection.");
    }

    @Override
    public Direction getDirection(IGridNode side) {
        if (this.fromAtoB == null) {
            return null;
        }
        if (this.sideA == side) {
            return this.fromAtoB;
        }
        return this.fromAtoB.getOpposite();
    }

    @Override
    public void destroy() {
        IPathingService p = this.sideA.getInternalGrid().getPathingService();
        p.repath();
        this.sideA.removeConnection(this);
        this.sideB.removeConnection(this);
        this.sideA.validateGrid();
        this.sideB.validateGrid();
    }

    @Override
    public GridNode a() {
        return this.sideA;
    }

    @Override
    public GridNode b() {
        return this.sideB;
    }

    @Override
    public boolean isInWorld() {
        return this.fromAtoB != null;
    }

    @Override
    public int getUsedChannels() {
        return this.lastUsedChannels;
    }

    @Override
    public void setAdHocChannels(int channels) {
        this.usedChannels = channels;
    }

    @Override
    public GridNode getControllerRoute() {
        return this.sideA;
    }

    @Override
    public void setControllerRoute(IPathItem fast) {
        this.usedChannels = 0;
        if (this.sideB == fast) {
            GridNode tmp = this.sideA;
            this.sideA = this.sideB;
            this.sideB = tmp;
            if (this.fromAtoB != null) {
                this.fromAtoB = this.fromAtoB.getOpposite();
            }
        }
    }

    @Override
    public int getMaxChannels() {
        ChannelMode mode = this.sideB.getGrid().getPathingService().getChannelMode();
        if (mode == ChannelMode.INFINITE) {
            return Integer.MAX_VALUE;
        }
        return 32 * mode.getCableCapacityFactor();
    }

    @Override
    public Iterable<IPathItem> getPossibleOptions() {
        return ImmutableList.of((Object)this.a(), (Object)this.b());
    }

    @Override
    public boolean hasFlag(GridFlags flag) {
        return false;
    }

    public int propagateChannelsUpwards() {
        this.usedChannels = this.sideB.getControllerRoute() == this ? this.sideB.usedChannels : 0;
        return this.usedChannels;
    }

    @Override
    public void finalizeChannels() {
        if (this.lastUsedChannels != this.usedChannels) {
            this.lastUsedChannels = this.usedChannels;
            if (this.sideA.getInternalGrid() != null) {
                this.sideA.notifyStatusChange(IGridNodeListener.State.CHANNEL);
            }
            if (this.sideB.getInternalGrid() != null) {
                this.sideB.notifyStatusChange(IGridNodeListener.State.CHANNEL);
            }
        }
    }

    Object getVisitorIterationNumber() {
        return this.visitorIterationNumber;
    }

    void setVisitorIterationNumber(Object visitorIterationNumber) {
        this.visitorIterationNumber = visitorIterationNumber;
    }

    public static GridConnection create(IGridNode aNode, IGridNode bNode, @Nullable Direction fromAtoB) {
        Objects.requireNonNull(aNode, "aNode");
        Objects.requireNonNull(bNode, "bNode");
        Preconditions.checkArgument((aNode != bNode ? 1 : 0) != 0, (Object)"Cannot connect node to itself");
        GridNode a = (GridNode)aNode;
        GridNode b = (GridNode)bNode;
        if (a.hasConnection(b) || b.hasConnection(a)) {
            throw new IllegalStateException("Connection between node [%s] and [%s] on [%s] already exists.".formatted(a, b, fromAtoB));
        }
        GridConnection connection = new GridConnection(a, b, fromAtoB);
        GridConnection.mergeGrids(a, b);
        IPathingService p = connection.sideA.getInternalGrid().getPathingService();
        p.repath();
        connection.sideA.addConnection(connection);
        connection.sideB.addConnection(connection);
        return connection;
    }

    private static void mergeGrids(GridNode a, GridNode b) {
        Grid gridA = a.getMyGrid();
        Grid gridB = b.getMyGrid();
        if (gridA == null && gridB == null) {
            GridConnection.assertNodeIsStandalone(a);
            GridConnection.assertNodeIsStandalone(b);
            Grid grid = Grid.create(a);
            a.setGrid(grid);
            b.setGrid(grid);
        } else if (gridA == null) {
            GridConnection.assertNodeIsStandalone(a);
            a.setGrid(gridB);
        } else if (gridB == null) {
            GridConnection.assertNodeIsStandalone(b);
            b.setGrid(gridA);
        } else if (gridA != gridB) {
            if (GridConnection.isGridABetterThanGridB(gridA, gridB)) {
                GridPropagator gp = new GridPropagator(a.getInternalGrid());
                b.beginVisit(gp);
            } else {
                GridPropagator gp = new GridPropagator(b.getInternalGrid());
                a.beginVisit(gp);
            }
        }
    }

    private static boolean isGridABetterThanGridB(Grid gridA, Grid gridB) {
        if (gridA.getPriority() != gridB.getPriority()) {
            return gridA.getPriority() > gridB.getPriority();
        }
        return gridA.size() >= gridB.size();
    }

    private static void assertNodeIsStandalone(GridNode node) {
        if (!node.hasNoConnections()) {
            throw new IllegalStateException("Grid node " + String.valueOf(node) + " has no grid, but is connected: " + String.valueOf(node.getConnections()));
        }
    }
}

