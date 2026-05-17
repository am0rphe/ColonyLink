/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableSet
 *  com.google.common.collect.ImmutableSet$Builder
 *  com.google.common.collect.MultimapBuilder
 *  com.google.common.collect.SetMultimap
 *  com.google.gson.stream.JsonWriter
 *  it.unimi.dsi.fastutil.objects.Reference2IntMap
 *  it.unimi.dsi.fastutil.objects.Reference2IntMap$Entry
 *  it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap
 *  net.minecraft.CrashReportCategory
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.core.HolderLookup$RegistryLookup
 *  net.minecraft.core.RegistryAccess
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.world.level.Level
 *  org.jetbrains.annotations.Nullable
 */
package appeng.me;

import appeng.api.networking.GridServicesInternal;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.IGridService;
import appeng.api.networking.IGridServiceProvider;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.events.GridEvent;
import appeng.api.networking.pathing.IPathingService;
import appeng.api.networking.spatial.ISpatialService;
import appeng.api.networking.storage.IStorageService;
import appeng.api.networking.ticking.ITickManager;
import appeng.core.AELog;
import appeng.hooks.ticking.TickHandler;
import appeng.me.GridEventBus;
import appeng.me.GridNode;
import appeng.me.helpers.GridServiceContainer;
import appeng.me.service.P2PService;
import appeng.parts.AEBasePart;
import appeng.util.IDebugExportable;
import appeng.util.JsonStreamUtil;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SetMultimap;
import com.google.gson.stream.JsonWriter;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.CrashReportCategory;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class Grid
implements IGrid {
    private static final List<IGridNode> ITERATION_BUFFER = new ArrayList<IGridNode>();
    private static int nextSerial = 0;
    private final SetMultimap<Class<?>, IGridNode> machines = MultimapBuilder.hashKeys().hashSetValues().build();
    private final GridServiceContainer services;
    @Nullable
    private GridNode pivot;
    private int priority;
    private final int serialNumber = nextSerial++;

    public static Grid create(GridNode center) {
        Grid grid = new Grid(center);
        TickHandler.instance().addNetwork(grid);
        center.setGrid(grid);
        AELog.grid("Created grid %s with center %s", grid, center);
        return grid;
    }

    private Grid(GridNode center) {
        this.pivot = Objects.requireNonNull(center);
        this.services = GridServicesInternal.createServices(this);
    }

    int getPriority() {
        return this.priority;
    }

    @Override
    public int size() {
        return this.machines.size();
    }

    void remove(GridNode gridNode) {
        for (IGridServiceProvider c : this.services.services().values()) {
            c.removeNode(gridNode);
        }
        Class<?> machineClass = gridNode.getOwner().getClass();
        this.machines.remove(machineClass, (Object)gridNode);
        if (this.pivot == gridNode) {
            Iterator nodesIt = this.machines.values().iterator();
            if (nodesIt.hasNext()) {
                this.pivot = (GridNode)nodesIt.next();
            } else {
                this.pivot = null;
                TickHandler.instance().removeNetwork(this);
                AELog.grid("Removed grid %s", this);
            }
        }
    }

    void add(GridNode gridNode, @Nullable CompoundTag savedData) {
        this.machines.put(gridNode.getOwner().getClass(), (Object)gridNode);
        for (IGridServiceProvider service : this.services.services().values()) {
            service.addNode(gridNode, savedData);
        }
    }

    void saveNodeData(GridNode gridNode, CompoundTag savedData) {
        for (IGridServiceProvider service : this.services.services().values()) {
            service.saveNodeData(gridNode, savedData);
        }
    }

    @Override
    public <C extends IGridService> C getService(Class<C> iface) {
        IGridServiceProvider service = this.services.services().get(iface);
        if (service == null) {
            throw new IllegalArgumentException("Service " + String.valueOf(iface) + " is not registered");
        }
        return (C)((IGridService)((Object)service));
    }

    @Override
    public <T extends GridEvent> T postEvent(T ev) {
        GridEventBus.postEvent(this, ev);
        return ev;
    }

    @Override
    public Iterable<Class<?>> getMachineClasses() {
        return this.machines.keySet();
    }

    @Override
    public Iterable<IGridNode> getMachineNodes(Class<?> machineClass) {
        return this.machines.get(machineClass);
    }

    @Override
    public <T> Set<T> getMachines(Class<T> machineClass) {
        Set nodes = this.machines.get(machineClass);
        ImmutableSet.Builder resultBuilder = ImmutableSet.builder();
        for (IGridNode node : nodes) {
            Object logicalHost = node.getOwner();
            if (!machineClass.isInstance(logicalHost)) continue;
            resultBuilder.add(machineClass.cast(logicalHost));
        }
        return resultBuilder.build();
    }

    @Override
    public <T> Set<T> getActiveMachines(Class<T> machineClass) {
        Set nodes = this.machines.get(machineClass);
        ImmutableSet.Builder resultBuilder = ImmutableSet.builder();
        for (IGridNode node : nodes) {
            Object logicalHost = node.getOwner();
            if (!machineClass.isInstance(logicalHost) || !node.isActive()) continue;
            resultBuilder.add(machineClass.cast(logicalHost));
        }
        return resultBuilder.build();
    }

    public Collection<IGridNode> getNodes() {
        return this.machines.values();
    }

    @Override
    public boolean isEmpty() {
        return this.pivot == null;
    }

    @Override
    public IGridNode getPivot() {
        return this.pivot;
    }

    void setPivot(GridNode pivot) {
        this.pivot = pivot;
    }

    public void onServerStartTick() {
        if (this.pivot == null) {
            return;
        }
        for (IGridServiceProvider gc : this.services.serverStartTickServices()) {
            gc.onServerStartTick();
        }
    }

    public void onLevelStartTick(Level level) {
        if (this.pivot == null) {
            return;
        }
        for (IGridServiceProvider gc : this.services.levelStartTickServices()) {
            gc.onLevelStartTick(level);
        }
    }

    public void onLevelEndTick(Level level) {
        if (this.pivot == null) {
            return;
        }
        for (IGridServiceProvider gc : this.services.levelEndtickServices()) {
            gc.onLevelEndTick(level);
        }
    }

    public void onServerEndTick() {
        if (this.pivot == null) {
            return;
        }
        for (IGridServiceProvider gc : this.services.serverEndTickServices()) {
            gc.onServerEndTick();
        }
    }

    public void setImportantFlag(int i, boolean publicHasPower) {
        int flag = 1 << i;
        this.priority = this.priority & ~flag | (publicHasPower ? flag : 0);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void notifyAllNodes(IGridNodeListener.State state) {
        if (!ITERATION_BUFFER.isEmpty()) {
            throw new IllegalStateException("Recursively trying to notify all nodes is not allowed");
        }
        try {
            ITERATION_BUFFER.addAll((Collection<IGridNode>)this.getNodes());
            for (IGridNode node : ITERATION_BUFFER) {
                ((GridNode)node).notifyStatusChange(state);
            }
        }
        finally {
            ITERATION_BUFFER.clear();
        }
    }

    public void fillCrashReportCategory(CrashReportCategory category) {
        category.setDetail("Nodes", (Object)this.machines.size());
        category.setDetail("Serial number", (Object)this.serialNumber);
        if (AELog.isGridLogEnabled()) {
            category.setDetail("All GridNodes", (Object)this.machines.values().stream().map(Object::toString).collect(Collectors.joining(";")));
        }
        if (this.pivot != null) {
            this.pivot.fillCrashReportCategory(category);
        }
    }

    public String toString() {
        return "Grid #" + this.serialNumber;
    }

    private static String getServiceExportKey(Class<?> service) {
        if (service == IEnergyService.class) {
            return "energyService";
        }
        if (service == ISpatialService.class) {
            return "spatialService";
        }
        if (service == IPathingService.class) {
            return "pathingService";
        }
        if (service == IStorageService.class) {
            return "storageService";
        }
        if (service == ITickManager.class) {
            return "tickManager";
        }
        if (service == P2PService.class) {
            return "p2pService";
        }
        if (service == ICraftingService.class) {
            return "craftingService";
        }
        return service.getName();
    }

    @Override
    public void export(JsonWriter jsonWriter) throws IOException {
        RegistryAccess registries = this.pivot != null ? this.pivot.getLevel().registryAccess() : HolderLookup.Provider.create(Stream.of(new HolderLookup.RegistryLookup[0]));
        jsonWriter.beginObject();
        Map<String, Boolean> properties = Map.of("id", this.serialNumber, "disposed", this.pivot == null);
        JsonStreamUtil.writeProperties(properties, jsonWriter);
        Reference2IntOpenHashMap machineIdMap = new Reference2IntOpenHashMap(this.machines.size());
        for (IGridNode node : this.machines.values()) {
            machineIdMap.put(node.getOwner(), machineIdMap.size());
            Object object = node.getOwner();
            if (!(object instanceof AEBasePart)) continue;
            AEBasePart aEBasePart = (AEBasePart)object;
            machineIdMap.put((Object)aEBasePart.getBlockEntity(), machineIdMap.size());
        }
        Reference2IntOpenHashMap nodeIdMap = new Reference2IntOpenHashMap(this.machines.size());
        for (IGridNode iGridNode : this.machines.values()) {
            nodeIdMap.put((Object)iGridNode, nodeIdMap.size());
        }
        jsonWriter.name("machines");
        this.exportMachines(jsonWriter, (HolderLookup.Provider)registries, (Reference2IntMap<Object>)machineIdMap, (Reference2IntMap<IGridNode>)nodeIdMap);
        jsonWriter.name("nodes");
        this.exportNodes(jsonWriter, (HolderLookup.Provider)registries, (Reference2IntMap<Object>)machineIdMap, (Reference2IntMap<IGridNode>)nodeIdMap);
        jsonWriter.name("services");
        jsonWriter.beginObject();
        for (Map.Entry entry : this.services.services().entrySet()) {
            jsonWriter.name(Grid.getServiceExportKey((Class)entry.getKey()));
            jsonWriter.beginObject();
            ((IGridServiceProvider)entry.getValue()).debugDump(jsonWriter, (HolderLookup.Provider)registries);
            jsonWriter.endObject();
        }
        jsonWriter.endObject();
        jsonWriter.endObject();
    }

    private void exportMachines(JsonWriter jsonWriter, HolderLookup.Provider registries, Reference2IntMap<Object> machineIds, Reference2IntMap<IGridNode> nodeIds) throws IOException {
        jsonWriter.beginArray();
        for (Reference2IntMap.Entry entry : machineIds.reference2IntEntrySet()) {
            jsonWriter.beginObject();
            JsonStreamUtil.writeProperties(Map.of("id", entry.getIntValue()), jsonWriter);
            Object object = entry.getKey();
            if (object instanceof IDebugExportable) {
                IDebugExportable exportable = (IDebugExportable)object;
                exportable.debugExport(jsonWriter, registries, machineIds, nodeIds);
            }
            jsonWriter.endObject();
        }
        jsonWriter.endArray();
    }

    private void exportNodes(JsonWriter jsonWriter, HolderLookup.Provider registries, Reference2IntMap<Object> machineIds, Reference2IntMap<IGridNode> nodeIds) throws IOException {
        jsonWriter.beginArray();
        for (Reference2IntMap.Entry entry : nodeIds.reference2IntEntrySet()) {
            IGridNode node = (IGridNode)entry.getKey();
            ((GridNode)node).debugExport(jsonWriter, registries, machineIds, nodeIds);
        }
        jsonWriter.endArray();
    }

    public int getSerialNumber() {
        return this.serialNumber;
    }
}

