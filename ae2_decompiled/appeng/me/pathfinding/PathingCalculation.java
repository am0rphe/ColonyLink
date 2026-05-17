/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package appeng.me.pathfinding;

import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridMultiblock;
import appeng.api.networking.IGridNode;
import appeng.blockentity.networking.ControllerBlockEntity;
import appeng.me.GridConnection;
import appeng.me.GridNode;
import appeng.me.pathfinding.IPathItem;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PathingCalculation {
    private static final Logger LOG = LoggerFactory.getLogger(PathingCalculation.class);
    private final IGrid grid;
    private final Set<GridNode> multiblocksWithChannel = new HashSet<GridNode>();
    private final Queue<IPathItem>[] queues = new Queue[]{new ArrayDeque(), new ArrayDeque(), new ArrayDeque()};
    private final Set<IPathItem> visited = new HashSet<IPathItem>();
    private final Reference2IntOpenHashMap<GridNode> channelBottlenecks = new Reference2IntOpenHashMap();
    private final Set<GridNode> channelNodes = new HashSet<GridNode>();
    private int channelsInUse = 0;
    private int channelsByBlocks = 0;
    private static final Object SUBTREE_END = new Object();

    public PathingCalculation(IGrid grid) {
        this.grid = grid;
        for (IGridNode node : grid.getMachineNodes(ControllerBlockEntity.class)) {
            this.visited.add((IPathItem)((Object)node));
            for (IGridConnection gcc : node.getConnections()) {
                GridConnection gc = (GridConnection)gcc;
                if (gc.getOtherSide(node).getOwner() instanceof ControllerBlockEntity) continue;
                this.enqueue(gc, 0);
                gc.setControllerRoute((GridNode)node);
            }
        }
    }

    private void enqueue(IPathItem pathItem, int queueIndex) {
        this.visited.add(pathItem);
        int possibleIndex = pathItem instanceof GridConnection ? 0 : (pathItem.hasFlag(GridFlags.DENSE_CAPACITY) ? 0 : (pathItem.hasFlag(GridFlags.PREFERRED) ? 1 : 2));
        int index = Math.max(possibleIndex, queueIndex);
        this.queues[index].add(pathItem);
    }

    public void compute() {
        for (int i = 0; i < 3; ++i) {
            this.processQueue(this.queues[i], i);
        }
        this.propagateAssignments();
    }

    private void processQueue(Queue<IPathItem> oldOpen, int queueIndex) {
        while (!oldOpen.isEmpty()) {
            IPathItem i = oldOpen.poll();
            for (IPathItem pi : i.getPossibleOptions()) {
                IGridMultiblock multiblock;
                boolean worked;
                if (this.visited.contains(pi)) continue;
                pi.setControllerRoute(i);
                if (pi.hasFlag(GridFlags.REQUIRE_CHANNEL) && !this.multiblocksWithChannel.contains(pi) && (worked = this.tryUseChannel((GridNode)pi)) && pi.hasFlag(GridFlags.MULTIBLOCK) && (multiblock = ((IGridNode)((Object)pi)).getService(IGridMultiblock.class)) != null) {
                    Iterator<IGridNode> oni = multiblock.getMultiblockNodes();
                    while (oni.hasNext()) {
                        IGridNode otherNodes = oni.next();
                        if (otherNodes == null) {
                            LOG.error("Skipping null node returned by grid multiblock node {}", (Object)multiblock);
                            continue;
                        }
                        if (otherNodes == pi) continue;
                        this.multiblocksWithChannel.add((GridNode)otherNodes);
                    }
                }
                this.enqueue(pi, queueIndex);
            }
        }
    }

    private boolean tryUseChannel(GridNode start) {
        GridNode pi;
        if (start.hasFlag(GridFlags.COMPRESSED_CHANNEL) && !start.getSubtreeAllowsCompressedChannels()) {
            return false;
        }
        for (pi = start; pi != null; pi = pi.getHighestSimilarAncestor()) {
            if (this.channelBottlenecks.getOrDefault((Object)pi, 0) < pi.getMaxChannels()) continue;
            return false;
        }
        for (pi = start; pi != null; pi = pi.getHighestSimilarAncestor()) {
            this.channelBottlenecks.addTo((Object)pi, 1);
        }
        this.channelNodes.add(start);
        return true;
    }

    private void propagateAssignments() {
        ArrayList<Object> stack = new ArrayList<Object>();
        HashSet<IPathItem> controllerNodes = new HashSet<IPathItem>();
        for (IGridNode node : this.grid.getMachineNodes(ControllerBlockEntity.class)) {
            controllerNodes.add((IPathItem)((Object)node));
            for (IGridConnection gcc : node.getConnections()) {
                GridConnection gc = (GridConnection)gcc;
                if (gc.getOtherSide(node).getOwner() instanceof ControllerBlockEntity) continue;
                stack.add(gc);
            }
        }
        while (!stack.isEmpty()) {
            Object current = stack.getLast();
            if (current == SUBTREE_END) {
                stack.removeLast();
                IPathItem item = (IPathItem)stack.removeLast();
                if (item instanceof GridNode) {
                    GridNode node = (GridNode)item;
                    boolean hasChannel = this.channelNodes.contains(item);
                    this.channelsByBlocks += node.propagateChannelsUpwards(hasChannel);
                    if (!hasChannel) continue;
                    ++this.channelsInUse;
                    continue;
                }
                this.channelsByBlocks += ((GridConnection)item).propagateChannelsUpwards();
                continue;
            }
            stack.add(SUBTREE_END);
            for (IPathItem pi : ((IPathItem)current).getPossibleOptions()) {
                if (controllerNodes.contains(pi) || pi.getControllerRoute() != current) continue;
                stack.add(pi);
            }
        }
        for (GridNode multiblockNode : this.multiblocksWithChannel) {
            multiblockNode.incrementChannelCount(1);
        }
    }

    public int getChannelsInUse() {
        return this.channelsInUse;
    }

    public int getChannelsByBlocks() {
        return this.channelsByBlocks;
    }
}

