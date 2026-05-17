/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Preconditions
 *  com.google.common.collect.ClassToInstanceMap
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.MutableClassToInstanceMap
 *  com.google.gson.stream.JsonWriter
 *  it.unimi.dsi.fastutil.objects.Reference2IntMap
 *  net.minecraft.CrashReportCategory
 *  net.minecraft.core.Direction
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  org.jetbrains.annotations.Nullable
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package appeng.me;

import appeng.api.features.IPlayerRegistry;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridConnectionVisitor;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.IGridNodeService;
import appeng.api.networking.IGridVisitor;
import appeng.api.networking.events.GridPowerIdleChange;
import appeng.api.networking.pathing.ChannelMode;
import appeng.api.parts.IPart;
import appeng.api.stacks.AEItemKey;
import appeng.api.util.AEColor;
import appeng.blockentity.networking.ControllerBlockEntity;
import appeng.core.AELog;
import appeng.me.Grid;
import appeng.me.GridConnection;
import appeng.me.GridPropagator;
import appeng.me.GridSplitDetector;
import appeng.me.pathfinding.IPathItem;
import appeng.util.IDebugExportable;
import appeng.util.JsonStreamUtil;
import com.google.common.base.Preconditions;
import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.MutableClassToInstanceMap;
import com.google.gson.stream.JsonWriter;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import net.minecraft.CrashReportCategory;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GridNode
implements IGridNode,
IPathItem,
IDebugExportable {
    private static final Logger LOG = LoggerFactory.getLogger(GridNode.class);
    private final ServerLevel level;
    private final Object owner;
    protected final IGridNodeListener<?> listener;
    private boolean ready;
    protected final List<GridConnection> connections = new ArrayList<GridConnection>();
    private double previousDraw = 0.0;
    private double idlePowerUsage = 1.0;
    @Nullable
    private AEItemKey visualRepresentation = null;
    private AEColor gridColor = AEColor.TRANSPARENT;
    private int owningPlayerId = -1;
    private Grid myGrid;
    private Object visitorIterationNumber = null;
    int usedChannels = 0;
    private int lastUsedChannels = 0;
    @Nullable
    private GridNode highestSimilarAncestor = null;
    private int subtreeMaxChannels;
    private boolean subtreeAllowsCompressedChannels;
    private final EnumSet<GridFlags> flags;
    private ClassToInstanceMap<IGridNodeService> services;
    @Nullable
    private CompoundTag savedData;

    public <T> GridNode(ServerLevel level, T owner, IGridNodeListener<T> listener, Set<GridFlags> flags) {
        this.level = level;
        this.owner = owner;
        this.listener = listener;
        this.flags = EnumSet.noneOf(GridFlags.class);
        this.flags.addAll(flags);
        this.services = null;
    }

    Grid getMyGrid() {
        return this.myGrid;
    }

    public <T> void callListener(ListenerCallback<T> callback) {
        Object typedOwner = this.owner;
        IGridNodeListener<?> typedListener = this.listener;
        callback.call(typedListener, typedOwner, this);
    }

    public void notifyStatusChange(IGridNodeListener.State reason) {
        this.callListener((listener, owner, node) -> listener.onStateChanged(owner, node, reason));
    }

    void addConnection(IGridConnection gridConnection) {
        this.connections.add((GridConnection)gridConnection);
        if (gridConnection.isInWorld()) {
            this.callListener(IGridNodeListener::onInWorldConnectionChanged);
        }
    }

    void removeConnection(IGridConnection gridConnection) {
        this.connections.remove((GridConnection)gridConnection);
        if (gridConnection.isInWorld()) {
            this.callListener(IGridNodeListener::onInWorldConnectionChanged);
        }
    }

    boolean hasConnection(IGridNode otherSide) {
        for (IGridConnection iGridConnection : this.connections) {
            if (iGridConnection.a() != otherSide && iGridConnection.b() != otherSide) continue;
            return true;
        }
        return false;
    }

    void validateGrid() {
        if (!this.ready) {
            return;
        }
        GridSplitDetector gsd = new GridSplitDetector(this.getInternalGrid().getPivot());
        this.beginVisit(gsd);
        if (!gsd.isPivotFound()) {
            GridPropagator gp = new GridPropagator(Grid.create(this));
            this.beginVisit(gp);
        }
    }

    public Grid getInternalGrid() {
        if (this.myGrid == null) {
            Grid.create(this);
            return Objects.requireNonNull(this.myGrid);
        }
        return this.myGrid;
    }

    @Override
    public void beginVisit(IGridVisitor g) {
        Object tracker = new Object();
        ArrayDeque<GridNode> nextRun = new ArrayDeque<GridNode>();
        nextRun.add(this);
        this.visitorIterationNumber = tracker;
        if (g instanceof IGridConnectionVisitor) {
            IGridConnectionVisitor gcv = (IGridConnectionVisitor)g;
            ArrayDeque<IGridConnection> nextConn = new ArrayDeque<IGridConnection>();
            while (!nextRun.isEmpty()) {
                while (!nextConn.isEmpty()) {
                    gcv.visitConnection((IGridConnection)nextConn.poll());
                }
                ArrayDeque<GridNode> thisRun = nextRun;
                nextRun = new ArrayDeque();
                for (GridNode n : thisRun) {
                    n.visitorConnection(tracker, g, nextRun, nextConn);
                }
            }
        } else {
            while (!nextRun.isEmpty()) {
                ArrayDeque<GridNode> thisRun = nextRun;
                nextRun = new ArrayDeque();
                for (GridNode n : thisRun) {
                    n.visitorNode(tracker, g, nextRun);
                }
            }
        }
    }

    protected final void updateState() {
        if (this.ready) {
            this.findInWorldConnections();
            this.getInternalGrid();
        }
    }

    public void setOwningPlayerId(int ownerPlayerId) {
        if (ownerPlayerId >= 0 && this.owningPlayerId != ownerPlayerId) {
            this.owningPlayerId = ownerPlayerId;
            if (this.ready) {
                this.callListener(IGridNodeListener::onOwnerChanged);
            }
        }
    }

    public void setIdlePowerUsage(double usagePerTick) {
        this.idlePowerUsage = usagePerTick;
        if (this.myGrid != null && this.ready) {
            this.myGrid.postEvent(new GridPowerIdleChange(this));
        }
    }

    public void setVisualRepresentation(@Nullable AEItemKey visualRepresentation) {
        this.visualRepresentation = visualRepresentation;
    }

    public void setGridColor(AEColor color) {
        this.gridColor = Objects.requireNonNull(color);
        this.updateState();
    }

    @Override
    public IGrid getGrid() {
        if (this.myGrid == null) {
            throw new IllegalStateException("A node is being used after it has been destroyed.");
        }
        return this.myGrid;
    }

    void setGrid(Grid grid) {
        if (this.myGrid == grid) {
            return;
        }
        if (this.myGrid != null) {
            this.savedData = new CompoundTag();
            this.myGrid.saveNodeData(this, this.savedData);
            this.myGrid.remove(this);
        }
        boolean wasPowered = this.isPowered();
        this.myGrid = grid;
        this.myGrid.add(this, this.savedData);
        this.callListener(IGridNodeListener::onGridChanged);
        if (wasPowered != this.isPowered()) {
            this.notifyStatusChange(IGridNodeListener.State.POWER);
        }
    }

    public void destroy() {
        GridNode otherSide;
        this.ready = false;
        boolean movedPivot = false;
        for (GridConnection connection : this.connections) {
            otherSide = (GridNode)connection.getOtherSide(this);
            if (!movedPivot && connection.a() != this && this.myGrid != null) {
                this.myGrid.setPivot(connection.a());
                movedPivot = true;
            }
            otherSide.removeConnection(connection);
        }
        for (GridConnection connection : this.connections) {
            otherSide = (GridNode)connection.getOtherSide(this);
            if (!movedPivot && this.myGrid != null && this.myGrid.getPivot() == this) {
                this.myGrid.setPivot(otherSide);
                movedPivot = true;
            }
            otherSide.validateGrid();
            otherSide.getInternalGrid().getPathingService().repath();
        }
        this.connections.clear();
        AELog.grid("Destroyed node %s in grid %s", this, this.myGrid);
        if (this.myGrid != null) {
            this.myGrid.remove(this);
            this.myGrid = null;
        }
    }

    void markReady() {
        Preconditions.checkState((!this.ready ? 1 : 0) != 0);
        this.ready = true;
        this.updateState();
    }

    public EnumSet<Direction> getConnectedSides() {
        EnumSet<Direction> result = EnumSet.noneOf(Direction.class);
        for (IGridConnection iGridConnection : this.connections) {
            if (!iGridConnection.isInWorld()) continue;
            result.add(iGridConnection.getDirection(this));
        }
        return result;
    }

    @Override
    public Map<Direction, IGridConnection> getInWorldConnections() {
        EnumMap<Direction, IGridConnection> result = new EnumMap<Direction, IGridConnection>(Direction.class);
        for (IGridConnection iGridConnection : this.connections) {
            Direction direction = iGridConnection.getDirection(this);
            if (direction == null) continue;
            result.put(direction, iGridConnection);
        }
        return result;
    }

    @Override
    public List<IGridConnection> getConnections() {
        return ImmutableList.copyOf(this.connections);
    }

    public boolean hasNoConnections() {
        return this.connections.isEmpty();
    }

    @Override
    public boolean hasGridBooted() {
        if (this.myGrid == null) {
            return false;
        }
        return !this.myGrid.getPathingService().isNetworkBooting();
    }

    @Override
    public boolean isPowered() {
        if (this.myGrid == null) {
            return false;
        }
        return this.myGrid.getEnergyService().isNetworkPowered();
    }

    public void loadFromNBT(String name, CompoundTag nodeDataContainer) {
        this.owningPlayerId = -1;
        CompoundTag oldNodeData = this.savedData;
        Tag tag = nodeDataContainer.get(name);
        if (tag instanceof CompoundTag) {
            CompoundTag newNodeData;
            this.savedData = newNodeData = (CompoundTag)tag;
            if (newNodeData.contains("p", 3)) {
                this.owningPlayerId = newNodeData.getInt("p");
            }
        } else {
            this.savedData = null;
        }
        if (this.ready && this.myGrid != null && !this.areTagsEqualIgnoringPlayerId(this.savedData, oldNodeData)) {
            AELog.debug("Resetting grid node %s to reload NBT", this);
            this.destroy();
            this.markReady();
        }
    }

    private boolean areTagsEqualIgnoringPlayerId(CompoundTag newData, CompoundTag oldData) {
        Set newKeys = newData != null ? newData.getAllKeys() : Set.of();
        Set oldKeys = oldData != null ? oldData.getAllKeys() : Set.of();
        for (String newKey : newKeys) {
            Tag oldTag;
            if ("p".equals(newKey)) continue;
            Tag newTag = newData.get(newKey);
            if (Objects.equals(newTag, oldTag = oldData != null ? oldData.get(newKey) : null)) continue;
            return false;
        }
        for (String oldKey : oldKeys) {
            if ("p".equals(oldKey) || newKeys.contains(oldKey)) continue;
            return false;
        }
        return true;
    }

    public void saveToNBT(String name, CompoundTag nodeData) {
        if (this.myGrid != null) {
            CompoundTag node = new CompoundTag();
            node.putInt("p", this.owningPlayerId);
            this.myGrid.saveNodeData(this, node);
            nodeData.put(name, (Tag)node);
        } else {
            nodeData.remove(name);
        }
    }

    @Override
    public boolean meetsChannelRequirements() {
        return !this.flags.contains((Object)GridFlags.REQUIRE_CHANNEL) || this.getUsedChannels() > 0;
    }

    @Override
    public boolean hasFlag(GridFlags flag) {
        return this.flags.contains((Object)flag);
    }

    @Override
    public double getIdlePowerUsage() {
        return this.idlePowerUsage;
    }

    @Override
    @Nullable
    public AEItemKey getVisualRepresentation() {
        return this.visualRepresentation;
    }

    @Override
    public AEColor getGridColor() {
        return this.gridColor;
    }

    @Override
    public int getOwningPlayerId() {
        return this.owningPlayerId;
    }

    @Override
    public UUID getOwningPlayerProfileId() {
        if (this.owningPlayerId == -1) {
            return null;
        }
        IPlayerRegistry mapping = IPlayerRegistry.getMapping((Level)this.level);
        return mapping != null ? mapping.getProfileId(this.owningPlayerId) : null;
    }

    protected void findInWorldConnections() {
    }

    private void visitorConnection(Object tracker, IGridVisitor g, Deque<GridNode> nextRun, Deque<IGridConnection> nextConnections) {
        if (g.visitNode(this)) {
            for (IGridConnection gc : this.getConnections()) {
                GridNode gn = (GridNode)gc.getOtherSide(this);
                GridConnection gcc = (GridConnection)gc;
                if (gcc.getVisitorIterationNumber() != tracker) {
                    gcc.setVisitorIterationNumber(tracker);
                    nextConnections.add(gc);
                }
                if (tracker == gn.visitorIterationNumber) continue;
                gn.visitorIterationNumber = tracker;
                nextRun.add(gn);
            }
        }
    }

    private void visitorNode(Object tracker, IGridVisitor g, Deque<GridNode> nextRun) {
        if (g.visitNode(this)) {
            for (IGridConnection gc : this.getConnections()) {
                GridNode gn = (GridNode)gc.getOtherSide(this);
                if (tracker == gn.visitorIterationNumber) continue;
                gn.visitorIterationNumber = tracker;
                nextRun.add(gn);
            }
        }
    }

    @Override
    public void setAdHocChannels(int channels) {
        this.usedChannels = channels;
    }

    @Override
    public IPathItem getControllerRoute() {
        if (this.connections.isEmpty()) {
            throw new IllegalStateException("Node %s has no connections, cannot have a controller route!".formatted(this));
        }
        return this.connections.getFirst();
    }

    @Nullable
    public GridNode getHighestSimilarAncestor() {
        return this.highestSimilarAncestor;
    }

    public boolean getSubtreeAllowsCompressedChannels() {
        return this.subtreeAllowsCompressedChannels;
    }

    @Override
    public void setControllerRoute(IPathItem fast) {
        this.usedChannels = 0;
        GridNode nodeParent = (GridNode)fast.getControllerRoute();
        if (nodeParent.getOwner() instanceof ControllerBlockEntity) {
            this.highestSimilarAncestor = null;
            this.subtreeMaxChannels = this.getMaxChannels();
            this.subtreeAllowsCompressedChannels = !this.hasFlag(GridFlags.CANNOT_CARRY_COMPRESSED);
        } else {
            this.highestSimilarAncestor = nodeParent.highestSimilarAncestor == null ? nodeParent : (nodeParent.subtreeMaxChannels == nodeParent.highestSimilarAncestor.subtreeMaxChannels ? nodeParent.highestSimilarAncestor : nodeParent);
            this.subtreeMaxChannels = Math.min(nodeParent.subtreeMaxChannels, this.getMaxChannels());
            this.subtreeAllowsCompressedChannels = nodeParent.subtreeAllowsCompressedChannels && !this.hasFlag(GridFlags.CANNOT_CARRY_COMPRESSED);
        }
        GridConnection connection = (GridConnection)fast;
        int idx = this.connections.indexOf(connection);
        if (idx > 0) {
            this.connections.remove(connection);
            this.connections.add(0, connection);
        }
    }

    @Override
    public int getUsedChannels() {
        return this.lastUsedChannels;
    }

    @Override
    public int getMaxChannels() {
        if (this.flags.contains((Object)GridFlags.CANNOT_CARRY)) {
            return 0;
        }
        ChannelMode channelMode = this.myGrid.getPathingService().getChannelMode();
        if (channelMode == ChannelMode.INFINITE) {
            return Integer.MAX_VALUE;
        }
        if (!this.flags.contains((Object)GridFlags.DENSE_CAPACITY)) {
            return 8 * channelMode.getCableCapacityFactor();
        }
        return 32 * channelMode.getCableCapacityFactor();
    }

    @Override
    public Iterable<IPathItem> getPossibleOptions() {
        return ImmutableList.copyOf(this.connections);
    }

    public int propagateChannelsUpwards(boolean consumesChannel) {
        this.usedChannels = 0;
        for (GridConnection connection : this.connections) {
            if (connection.getControllerRoute() != this) continue;
            this.usedChannels += connection.usedChannels;
        }
        if (consumesChannel) {
            ++this.usedChannels;
        }
        if (this.usedChannels > this.getMaxChannels()) {
            LOG.error("Internal channel assignment error. Grid node {} has {} channels passing through it but it only supports up to {}. Please open an issue on the AE2 repository.", new Object[]{this, this.usedChannels, this.getMaxChannels()});
        }
        return this.usedChannels;
    }

    public void incrementChannelCount(int usedChannels) {
        this.usedChannels += usedChannels;
    }

    @Override
    public void finalizeChannels() {
        this.highestSimilarAncestor = null;
        if (this.hasFlag(GridFlags.CANNOT_CARRY)) {
            return;
        }
        if (this.lastUsedChannels != this.usedChannels) {
            this.lastUsedChannels = this.usedChannels;
            if (this.getInternalGrid() != null) {
                this.notifyStatusChange(IGridNodeListener.State.CHANNEL);
            }
        }
    }

    public double getPreviousDraw() {
        return this.previousDraw;
    }

    public void setPreviousDraw(double previousDraw) {
        this.previousDraw = previousDraw;
    }

    @Override
    @Nullable
    public <T extends IGridNodeService> T getService(Class<T> serviceClass) {
        return (T)(this.services != null ? (IGridNodeService)this.services.getInstance(serviceClass) : null);
    }

    public <T extends IGridNodeService> void addService(Class<T> serviceClass, T service) {
        if (this.services == null) {
            this.services = MutableClassToInstanceMap.create();
        }
        this.services.putInstance(serviceClass, service);
    }

    @Override
    public Object getOwner() {
        return this.owner;
    }

    @Override
    public ServerLevel getLevel() {
        return this.level;
    }

    public String toString() {
        Object object = this.getOwner();
        if (object instanceof BlockEntity) {
            BlockEntity blockEntity = (BlockEntity)object;
            ResourceLocation beType = BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey((Object)blockEntity.getType());
            return "node@" + Integer.toHexString(this.hashCode()) + " hosted by " + String.valueOf(beType);
        }
        return "node@" + Integer.toHexString(this.hashCode()) + " hosted by " + this.getOwner().getClass().getName();
    }

    @Override
    public void fillCrashReportCategory(CrashReportCategory category) {
        category.setDetail("Node", (Object)this.toString());
        Object object = this.getOwner();
        if (object instanceof IPart) {
            IPart part = (IPart)object;
            part.addEntityCrashInfo(category);
        } else {
            object = this.getOwner();
            if (object instanceof BlockEntity) {
                BlockEntity blockEntity = (BlockEntity)object;
                blockEntity.fillCrashReportCategory(category);
                Level level = blockEntity.getLevel();
                if (level != null) {
                    category.setDetail("Level", (Object)level.dimension());
                }
            }
        }
    }

    @Override
    public final void debugExport(JsonWriter writer, HolderLookup.Provider registries, Reference2IntMap<Object> machineIds, Reference2IntMap<IGridNode> nodeIds) throws IOException {
        writer.beginObject();
        this.exportProperties(writer, machineIds, nodeIds);
        writer.endObject();
    }

    protected void exportProperties(JsonWriter writer, Reference2IntMap<Object> machineIds, Reference2IntMap<IGridNode> nodeIds) throws IOException {
        int id = nodeIds.getInt((Object)this);
        int machineId = machineIds.getInt(this.owner);
        JsonStreamUtil.writeProperties(Map.of("id", id, "owner", machineId), writer);
        writer.name("level");
        writer.value(this.level.dimension().location().toString());
    }

    @FunctionalInterface
    public static interface ListenerCallback<T> {
        public void call(IGridNodeListener<T> var1, T var2, IGridNode var3);
    }
}

