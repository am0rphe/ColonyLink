/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.HashMultimap
 *  com.google.common.collect.ImmutableSet
 *  com.google.common.collect.ImmutableSet$Builder
 *  com.google.common.collect.Multimap
 *  com.google.common.collect.Sets
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.world.level.Level
 *  org.apache.commons.lang3.mutable.MutableObject
 *  org.jetbrains.annotations.Nullable
 */
package appeng.me.service;

import appeng.api.config.Actionable;
import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridServiceProvider;
import appeng.api.networking.crafting.CalculationStrategy;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.networking.crafting.ICraftingSimulationRequester;
import appeng.api.networking.crafting.ICraftingSubmitResult;
import appeng.api.networking.crafting.ICraftingWatcherNode;
import appeng.api.networking.crafting.UnsuitableCpus;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.events.GridCraftingCpuChange;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageService;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.AEKeyFilter;
import appeng.blockentity.crafting.CraftingBlockEntity;
import appeng.crafting.CraftingCalculation;
import appeng.crafting.CraftingLink;
import appeng.crafting.CraftingLinkNexus;
import appeng.crafting.execution.CraftingSubmitResult;
import appeng.hooks.ticking.TickHandler;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.me.helpers.InterestManager;
import appeng.me.helpers.StackWatcher;
import appeng.me.service.helpers.CraftingServiceStorage;
import appeng.me.service.helpers.NetworkCraftingProviders;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;

public class CraftingService
implements ICraftingService,
IGridServiceProvider {
    private static final Comparator<CraftingCPUCluster> FAST_FIRST_COMPARATOR = Comparator.comparingInt(CraftingCPUCluster::getCoProcessors).reversed().thenComparingLong(CraftingCPUCluster::getAvailableStorage);
    private static final Comparator<CraftingCPUCluster> FAST_LAST_COMPARATOR = Comparator.comparingInt(CraftingCPUCluster::getCoProcessors).thenComparingLong(CraftingCPUCluster::getAvailableStorage);
    private static final ExecutorService CRAFTING_POOL;
    private final Set<CraftingCPUCluster> craftingCPUClusters = new HashSet<CraftingCPUCluster>();
    private final Map<IGridNode, StackWatcher<ICraftingWatcherNode>> craftingWatchers = new HashMap<IGridNode, StackWatcher<ICraftingWatcherNode>>();
    private final IGrid grid;
    private final NetworkCraftingProviders craftingProviders = new NetworkCraftingProviders();
    private final Map<UUID, CraftingLinkNexus> craftingLinks = new HashMap<UUID, CraftingLinkNexus>();
    private final Multimap<AEKey, StackWatcher<ICraftingWatcherNode>> interests = HashMultimap.create();
    private final InterestManager<StackWatcher<ICraftingWatcherNode>> interestManager = new InterestManager<StackWatcher<ICraftingWatcherNode>>(this.interests);
    private final IEnergyService energyGrid;
    private final Set<AEKey> currentlyCrafting = new HashSet<AEKey>();
    private final Set<AEKey> currentlyCraftable = new HashSet<AEKey>();
    private long lastProcessedCraftingLogicChangeTick;
    private long lastProcessedCraftableChangeTick;
    private boolean updateList = false;

    public CraftingService(IGrid grid, IStorageService storageGrid, IEnergyService energyGrid) {
        this.grid = grid;
        this.energyGrid = energyGrid;
        this.lastProcessedCraftingLogicChangeTick = TickHandler.instance().getCurrentTick();
        this.lastProcessedCraftableChangeTick = TickHandler.instance().getCurrentTick();
        storageGrid.addGlobalStorageProvider(new CraftingServiceStorage(this));
    }

    @Override
    public void onServerEndTick() {
        if (this.updateList) {
            this.updateList = false;
            this.updateCPUClusters();
            this.lastProcessedCraftingLogicChangeTick = -1L;
        }
        this.craftingLinks.values().removeIf(nexus -> nexus.isDead(this.grid, this));
        long latestChange = 0L;
        for (CraftingCPUCluster craftingCPUCluster : this.craftingCPUClusters) {
            craftingCPUCluster.craftingLogic.tickCraftingLogic(this.energyGrid, this);
            latestChange = Math.max(latestChange, craftingCPUCluster.craftingLogic.getLastModifiedOnTick());
        }
        if (latestChange != this.lastProcessedCraftingLogicChangeTick) {
            this.lastProcessedCraftingLogicChangeTick = latestChange;
            HashSet<AEKey> previouslyCrafting = this.currentlyCrafting.isEmpty() ? Set.of() : new HashSet<AEKey>(this.currentlyCrafting);
            this.currentlyCrafting.clear();
            for (CraftingCPUCluster cpu : this.craftingCPUClusters) {
                cpu.craftingLogic.getAllWaitingFor(this.currentlyCrafting);
            }
            if (!(this.interests.isEmpty() || previouslyCrafting.isEmpty() && this.currentlyCrafting.isEmpty())) {
                HashSet hashSet = new HashSet();
                hashSet.addAll(Sets.difference(previouslyCrafting, this.currentlyCrafting));
                hashSet.addAll(Sets.difference(this.currentlyCrafting, previouslyCrafting));
                for (AEKey what : hashSet) {
                    for (StackWatcher<ICraftingWatcherNode> watcher : this.interestManager.get(what)) {
                        watcher.getHost().onRequestChange(what);
                    }
                    for (StackWatcher<ICraftingWatcherNode> watcher : this.interestManager.getAllStacksWatchers()) {
                        watcher.getHost().onRequestChange(what);
                    }
                }
            }
        }
        if (this.lastProcessedCraftableChangeTick != this.craftingProviders.getLastModifiedOnTick()) {
            this.lastProcessedCraftableChangeTick = this.craftingProviders.getLastModifiedOnTick();
            if (!(this.currentlyCraftable.isEmpty() && this.craftingProviders.getCraftableKeys().isEmpty() && this.craftingProviders.getEmittableKeys().isEmpty())) {
                HashSet<AEKey> previouslyCraftable = this.currentlyCraftable.isEmpty() ? Set.of() : new HashSet<AEKey>(this.currentlyCraftable);
                this.currentlyCraftable.clear();
                this.currentlyCraftable.addAll(this.craftingProviders.getCraftableKeys());
                this.currentlyCraftable.addAll(this.craftingProviders.getEmittableKeys());
                if (!this.interests.isEmpty()) {
                    HashSet hashSet = new HashSet();
                    hashSet.addAll(Sets.difference(previouslyCraftable, this.currentlyCraftable));
                    hashSet.addAll(Sets.difference(this.currentlyCraftable, previouslyCraftable));
                    for (AEKey what : hashSet) {
                        for (StackWatcher<ICraftingWatcherNode> watcher : this.interestManager.get(what)) {
                            watcher.getHost().onCraftableChange(what);
                        }
                        for (StackWatcher<ICraftingWatcherNode> watcher : this.interestManager.getAllStacksWatchers()) {
                            watcher.getHost().onCraftableChange(what);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void removeNode(IGridNode gridNode) {
        ICraftingRequester requester;
        StackWatcher<ICraftingWatcherNode> craftingWatcher = this.craftingWatchers.remove(gridNode);
        if (craftingWatcher != null) {
            craftingWatcher.destroy();
        }
        if ((requester = gridNode.getService(ICraftingRequester.class)) != null) {
            for (CraftingLinkNexus link : this.craftingLinks.values()) {
                if (!link.isRequester(requester)) continue;
                link.removeNode();
            }
        }
        this.craftingProviders.removeProvider(gridNode);
        if (gridNode.getOwner() instanceof CraftingBlockEntity) {
            this.updateList = true;
        }
    }

    @Override
    public void addNode(IGridNode gridNode, @Nullable CompoundTag savedData) {
        ICraftingRequester craftingRequester;
        this.craftingProviders.removeProvider(gridNode);
        this.craftingProviders.addProvider(gridNode);
        ICraftingWatcherNode watchingNode = gridNode.getService(ICraftingWatcherNode.class);
        if (watchingNode != null) {
            StackWatcher<ICraftingWatcherNode> watcher = new StackWatcher<ICraftingWatcherNode>(this.interestManager, watchingNode);
            this.craftingWatchers.put(gridNode, watcher);
            watchingNode.updateWatcher(watcher);
        }
        if ((craftingRequester = gridNode.getService(ICraftingRequester.class)) != null) {
            for (ICraftingLink link : craftingRequester.getRequestedJobs()) {
                if (!(link instanceof CraftingLink)) continue;
                this.addLink((CraftingLink)link);
            }
        }
        if (gridNode.getOwner() instanceof CraftingBlockEntity) {
            this.updateList = true;
        }
    }

    @Override
    public Set<AEKey> getCraftables(AEKeyFilter filter) {
        return this.craftingProviders.getCraftables(filter);
    }

    private void updateCPUClusters() {
        this.craftingCPUClusters.clear();
        for (CraftingBlockEntity blockEntity : this.grid.getMachines(CraftingBlockEntity.class)) {
            CraftingCPUCluster cluster = blockEntity.getCluster();
            if (cluster == null) continue;
            this.craftingCPUClusters.add(cluster);
            ICraftingLink maybeLink = cluster.craftingLogic.getLastLink();
            if (maybeLink == null) continue;
            this.addLink((CraftingLink)maybeLink);
        }
    }

    public void addLink(CraftingLink link) {
        if (link.isStandalone()) {
            return;
        }
        CraftingLinkNexus nexus = this.craftingLinks.get(link.getCraftingID());
        if (nexus == null) {
            nexus = new CraftingLinkNexus(link.getCraftingID());
            this.craftingLinks.put(link.getCraftingID(), nexus);
        }
        link.setNexus(nexus);
    }

    public long insertIntoCpus(AEKey what, long amount, Actionable type) {
        long inserted = 0L;
        for (CraftingCPUCluster cpu : this.craftingCPUClusters) {
            inserted += cpu.craftingLogic.insert(what, amount - inserted, type);
        }
        return inserted;
    }

    @Override
    public Collection<IPatternDetails> getCraftingFor(AEKey whatToCraft) {
        return this.craftingProviders.getCraftingFor(whatToCraft);
    }

    @Override
    public void refreshNodeCraftingProvider(IGridNode node) {
        this.craftingProviders.removeProvider(node);
        this.craftingProviders.addProvider(node);
    }

    @Override
    public void addGlobalCraftingProvider(ICraftingProvider cc) {
        this.craftingProviders.addProvider(cc);
    }

    @Override
    public void removeGlobalCraftingProvider(ICraftingProvider cc) {
        this.craftingProviders.removeProvider(cc);
    }

    @Override
    public void refreshGlobalCraftingProvider(ICraftingProvider cc) {
        this.craftingProviders.removeProvider(cc);
        this.craftingProviders.addProvider(cc);
    }

    @Override
    @Nullable
    public AEKey getFuzzyCraftable(AEKey whatToCraft, AEKeyFilter filter) {
        return this.craftingProviders.getFuzzyCraftable(whatToCraft, filter);
    }

    @Override
    public Future<ICraftingPlan> beginCraftingCalculation(Level level, ICraftingSimulationRequester simRequester, AEKey what, long amount, CalculationStrategy strategy) {
        if (level == null || simRequester == null) {
            throw new IllegalArgumentException("Invalid Crafting Job Request");
        }
        CraftingCalculation job = new CraftingCalculation(level, this.grid, simRequester, new GenericStack(what, amount), strategy);
        return CRAFTING_POOL.submit(job::run);
    }

    @Override
    public ICraftingSubmitResult submitJob(ICraftingPlan job, ICraftingRequester requestingMachine, ICraftingCPU target, boolean prioritizePower, IActionSource src) {
        CraftingCPUCluster cpuCluster;
        if (job.simulation()) {
            return CraftingSubmitResult.INCOMPLETE_PLAN;
        }
        if (target instanceof CraftingCPUCluster) {
            cpuCluster = (CraftingCPUCluster)target;
        } else {
            MutableObject unsuitableCpusResult = new MutableObject();
            cpuCluster = this.findSuitableCraftingCPU(job, prioritizePower, src, (MutableObject<UnsuitableCpus>)unsuitableCpusResult);
            if (cpuCluster == null) {
                UnsuitableCpus unsuitableCpus = (UnsuitableCpus)unsuitableCpusResult.getValue();
                if (unsuitableCpus == null) {
                    return CraftingSubmitResult.NO_CPU_FOUND;
                }
                return CraftingSubmitResult.noSuitableCpu(unsuitableCpus);
            }
        }
        return cpuCluster.submitJob(this.grid, job, src, requestingMachine);
    }

    @Nullable
    private CraftingCPUCluster findSuitableCraftingCPU(ICraftingPlan job, boolean prioritizePower, IActionSource src, MutableObject<UnsuitableCpus> unsuitableCpus) {
        ArrayList<CraftingCPUCluster> validCpusClusters = new ArrayList<CraftingCPUCluster>(this.craftingCPUClusters.size());
        int offline = 0;
        int busy = 0;
        int tooSmall = 0;
        int excluded = 0;
        for (CraftingCPUCluster cpu : this.craftingCPUClusters) {
            if (!cpu.isActive()) {
                ++offline;
                continue;
            }
            if (cpu.isBusy()) {
                ++busy;
                continue;
            }
            if (cpu.getAvailableStorage() < job.bytes()) {
                ++tooSmall;
                continue;
            }
            if (!cpu.canBeAutoSelectedFor(src)) {
                ++excluded;
                continue;
            }
            validCpusClusters.add(cpu);
        }
        if (validCpusClusters.isEmpty()) {
            if (offline > 0 || busy > 0 || tooSmall > 0 || excluded > 0) {
                unsuitableCpus.setValue((Object)new UnsuitableCpus(offline, busy, tooSmall, excluded));
            }
            return null;
        }
        validCpusClusters.sort((a, b) -> {
            boolean secondPreferred;
            boolean firstPreferred = a.isPreferredFor(src);
            if (firstPreferred != (secondPreferred = b.isPreferredFor(src))) {
                return Boolean.compare(secondPreferred, firstPreferred);
            }
            if (prioritizePower) {
                return FAST_FIRST_COMPARATOR.compare((CraftingCPUCluster)a, (CraftingCPUCluster)b);
            }
            return FAST_LAST_COMPARATOR.compare((CraftingCPUCluster)a, (CraftingCPUCluster)b);
        });
        return (CraftingCPUCluster)validCpusClusters.get(0);
    }

    @Override
    public ImmutableSet<ICraftingCPU> getCpus() {
        ImmutableSet.Builder cpus = ImmutableSet.builder();
        for (CraftingCPUCluster cpu : this.craftingCPUClusters) {
            if (!cpu.isActive() || cpu.isDestroyed()) continue;
            cpus.add((Object)cpu);
        }
        return cpus.build();
    }

    @Override
    public boolean canEmitFor(AEKey someItem) {
        return this.craftingProviders.canEmitFor(someItem);
    }

    @Override
    public boolean isRequesting(AEKey what) {
        return this.currentlyCrafting.contains(what);
    }

    @Override
    public long getRequestedAmount(AEKey what) {
        long requested = 0L;
        for (CraftingCPUCluster cluster : this.craftingCPUClusters) {
            requested += cluster.craftingLogic.getWaitingFor(what);
        }
        return requested;
    }

    @Override
    public boolean isRequestingAny() {
        return !this.currentlyCrafting.isEmpty();
    }

    public Iterable<ICraftingProvider> getProviders(IPatternDetails key) {
        return this.craftingProviders.getMediums(key);
    }

    public boolean hasCpu(ICraftingCPU cpu) {
        return this.craftingCPUClusters.contains(cpu);
    }

    static {
        ThreadFactory factory = ar -> {
            Thread crafting = new Thread(ar, "AE Crafting Calculator");
            crafting.setDaemon(true);
            return crafting;
        };
        CRAFTING_POOL = Executors.newCachedThreadPool(factory);
        GridHelper.addGridServiceEventHandler(GridCraftingCpuChange.class, ICraftingService.class, (service, event) -> {
            ((CraftingService)service).updateList = true;
        });
    }
}

