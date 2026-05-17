/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Preconditions
 *  com.google.common.collect.HashMultiset
 *  com.google.common.collect.Multiset
 *  com.google.common.collect.Sets
 *  it.unimi.dsi.fastutil.objects.ObjectRBTreeSet
 *  net.minecraft.nbt.CompoundTag
 *  org.jetbrains.annotations.Nullable
 *  org.jetbrains.annotations.VisibleForTesting
 */
package appeng.me.service;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.IGridServiceProvider;
import appeng.api.networking.energy.IAEPowerStorage;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.energy.IEnergyWatcher;
import appeng.api.networking.energy.IEnergyWatcherNode;
import appeng.api.networking.energy.IPassiveEnergyGenerator;
import appeng.api.networking.events.GridPowerIdleChange;
import appeng.api.networking.events.GridPowerStatusChange;
import appeng.api.networking.events.GridPowerStorageStateChanged;
import appeng.api.networking.pathing.IPathingService;
import appeng.me.Grid;
import appeng.me.GridNode;
import appeng.me.energy.EnergyThreshold;
import appeng.me.energy.EnergyWatcher;
import appeng.me.energy.GridEnergyStorage;
import appeng.me.energy.IEnergyOverlayGridConnection;
import appeng.me.service.EnergyOverlayGrid;
import appeng.me.service.PathingService;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.objects.ObjectRBTreeSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

public class EnergyService
implements IEnergyService,
IGridServiceProvider {
    private static final String TAG_STORED_ENERGY = "e";
    private static final Comparator<IAEPowerStorage> COMPARATOR_HIGHEST_PRIORITY_FIRST;
    private static final Comparator<IAEPowerStorage> COMPARATOR_LOWEST_PRIORITY_FIRST;
    private final NavigableSet<EnergyThreshold> interests = Sets.newTreeSet();
    private final double averageLength = 40.0;
    private final SortedSet<IAEPowerStorage> providers = new ObjectRBTreeSet(COMPARATOR_HIGHEST_PRIORITY_FIRST);
    private boolean ongoingExtractOperation = false;
    private final SortedSet<IAEPowerStorage> requesters = new ObjectRBTreeSet(COMPARATOR_LOWEST_PRIORITY_FIRST);
    private boolean ongoingInjectOperation = false;
    private final Multiset<IEnergyOverlayGridConnection> overlayGridConnections = HashMultiset.create();
    final Grid grid;
    private final HashMap<IGridNode, IEnergyWatcher> watchers = new HashMap();
    private final GridEnergyStorage localStorage;
    private int availableTicksSinceUpdate = 0;
    private double globalAvailablePower = 0.0;
    private double providerPowerSum;
    private double drainPerTick = 0.0;
    private double avgDrainPerTick = 0.0;
    private double avgInjectionPerTick = 0.0;
    private double tickDrainPerTick = 0.0;
    private double tickInjectionPerTick = 0.0;
    private boolean publicHasPower = false;
    private boolean hasPower = true;
    private long ticksSinceHasPowerChange = 900L;
    private final PathingService pgc;
    private double lastStoredPower = -1.0;
    private final Set<IPassiveEnergyGenerator> passiveGenerators = Collections.newSetFromMap(new IdentityHashMap());
    EnergyOverlayGrid overlayGrid = null;

    public EnergyService(IGrid g, IPathingService pgc) {
        this.grid = (Grid)g;
        this.pgc = (PathingService)pgc;
        this.localStorage = new GridEnergyStorage(this.grid);
        this.requesters.add(this.localStorage);
        this.providers.add(this.localStorage);
    }

    public void nodeIdlePowerChangeHandler(GridPowerIdleChange ev) {
        GridNode node = (GridNode)ev.node;
        double newDraw = node.getIdlePowerUsage();
        double diffDraw = newDraw - node.getPreviousDraw();
        node.setPreviousDraw(newDraw);
        this.drainPerTick += diffDraw;
    }

    public void storagePowerChangeHandler(GridPowerStorageStateChanged ev) {
        if (ev.storage.isAEPublicPowerStorage()) {
            switch (ev.type) {
                case PROVIDE_POWER: {
                    this.addProvider(ev.storage);
                    break;
                }
                case RECEIVE_POWER: {
                    this.addRequester(ev.storage);
                }
            }
        } else {
            new RuntimeException("Attempt to ask the IEnergyGrid to charge a non public energy store.").printStackTrace();
        }
    }

    @Override
    public void onServerStartTick() {
        for (IPassiveEnergyGenerator passiveGenerator : this.passiveGenerators) {
            IPassiveEnergyGenerator currentGenerator = this.getOverlayGrid().getCurrentPassiveGenerator();
            if (currentGenerator == passiveGenerator) {
                passiveGenerator.setSuppressed(false);
                continue;
            }
            if (currentGenerator == null || passiveGenerator.getRate() > currentGenerator.getRate()) {
                if (currentGenerator != null) {
                    currentGenerator.setSuppressed(true);
                }
                this.getOverlayGrid().setCurrentPassiveGenerator(passiveGenerator);
                passiveGenerator.setSuppressed(false);
                continue;
            }
            passiveGenerator.setSuppressed(true);
        }
    }

    @Override
    public void onServerEndTick() {
        IPassiveEnergyGenerator currentPassiveGenerator = this.getOverlayGrid().getCurrentPassiveGenerator();
        if (currentPassiveGenerator != null && this.passiveGenerators.contains(currentPassiveGenerator)) {
            this.injectPower(currentPassiveGenerator.getRate(), Actionable.MODULATE);
        }
        if (!this.interests.isEmpty()) {
            double oldPower = this.lastStoredPower;
            this.lastStoredPower = this.getStoredPower();
            EnergyThreshold low = new EnergyThreshold(Math.min(oldPower, this.lastStoredPower), Integer.MIN_VALUE);
            EnergyThreshold high = new EnergyThreshold(Math.max(oldPower, this.lastStoredPower), Integer.MAX_VALUE);
            for (EnergyThreshold th : this.interests.subSet(low, true, high, true)) {
                ((EnergyWatcher)th.getEnergyWatcher()).post(this);
            }
        }
        this.avgDrainPerTick *= (this.averageLength - 1.0) / this.averageLength;
        this.avgInjectionPerTick *= (this.averageLength - 1.0) / this.averageLength;
        this.avgDrainPerTick += this.tickDrainPerTick / this.averageLength;
        this.avgInjectionPerTick += this.tickInjectionPerTick / this.averageLength;
        this.tickDrainPerTick = 0.0;
        this.tickInjectionPerTick = 0.0;
        boolean currentlyHasPower = false;
        if (this.drainPerTick > 1.0E-4) {
            double drained = this.extractAEPower(this.getIdlePowerUsage(), Actionable.MODULATE, PowerMultiplier.CONFIG);
            currentlyHasPower = drained >= this.drainPerTick - 0.001;
        } else {
            boolean bl = currentlyHasPower = this.extractAEPower(0.1, Actionable.SIMULATE, PowerMultiplier.CONFIG) > 0.0;
        }
        this.ticksSinceHasPowerChange = currentlyHasPower == this.hasPower ? ++this.ticksSinceHasPowerChange : 0L;
        this.hasPower = currentlyHasPower;
        if (this.hasPower && this.ticksSinceHasPowerChange > 30L) {
            this.publicPowerState(true, this.grid);
        } else if (!this.hasPower) {
            this.publicPowerState(false, this.grid);
        }
        ++this.availableTicksSinceUpdate;
    }

    @Override
    public double extractAEPower(double amt, Actionable mode, PowerMultiplier pm) {
        double toExtract = pm.multiply(amt);
        double extracted = 0.0;
        for (EnergyService service : this.getConnectedServices()) {
            if ((extracted += service.extractProviderPower(toExtract - extracted, mode)) >= toExtract) break;
        }
        return pm.divide(extracted);
    }

    @Override
    public double getIdlePowerUsage() {
        return this.drainPerTick + this.pgc.getChannelPowerUsage();
    }

    @Override
    public double getChannelPowerUsage() {
        return this.pgc.getChannelPowerUsage();
    }

    private void publicPowerState(boolean newState, IGrid grid) {
        if (this.publicHasPower == newState) {
            return;
        }
        this.publicHasPower = newState;
        this.grid.setImportantFlag(0, this.publicHasPower);
        grid.postEvent(new GridPowerStatusChange());
        this.grid.notifyAllNodes(IGridNodeListener.State.POWER);
    }

    @VisibleForTesting
    public void refreshPower() {
        this.availableTicksSinceUpdate = 0;
        this.globalAvailablePower = 0.0;
        for (IAEPowerStorage p : this.providers) {
            this.globalAvailablePower += p.getAECurrentPower();
        }
    }

    public Collection<IEnergyOverlayGridConnection> getOverlayGridConnections() {
        return this.overlayGridConnections;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public double extractProviderPower(double amt, Actionable mode) {
        double extractedPower;
        Preconditions.checkArgument((amt >= 0.0 ? 1 : 0) != 0, (Object)"amt must be >= 0");
        Iterator it = this.providers.iterator();
        this.ongoingExtractOperation = true;
        try {
            double newPower;
            for (extractedPower = 0.0; extractedPower < amt && it.hasNext(); extractedPower += newPower) {
                IAEPowerStorage node = (IAEPowerStorage)it.next();
                double req = amt - extractedPower;
                newPower = node.extractAEPower(req, mode, PowerMultiplier.ONE);
                if (!(newPower < req) || mode != Actionable.MODULATE) continue;
                it.remove();
            }
        }
        finally {
            this.ongoingExtractOperation = false;
        }
        double result = Math.min(extractedPower, amt);
        if (mode == Actionable.MODULATE) {
            if (extractedPower > amt) {
                this.localStorage.injectAEPower(extractedPower - amt, Actionable.MODULATE);
            }
            this.globalAvailablePower -= result;
            this.tickDrainPerTick += result;
        }
        return result;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public double injectProviderPower(double amt, Actionable mode) {
        Preconditions.checkArgument((amt >= 0.0 ? 1 : 0) != 0, (Object)"amt must be >= 0");
        double originalAmount = amt;
        Iterator it = this.requesters.iterator();
        this.ongoingInjectOperation = true;
        try {
            while (amt > 0.0 && it.hasNext()) {
                IAEPowerStorage node = (IAEPowerStorage)it.next();
                amt = node.injectAEPower(amt, mode);
                if (!(amt > 0.0) || mode != Actionable.MODULATE) continue;
                it.remove();
            }
        }
        finally {
            this.ongoingInjectOperation = false;
        }
        double overflow = Math.max(0.0, amt);
        if (mode == Actionable.MODULATE) {
            this.tickInjectionPerTick += originalAmount - overflow;
        }
        return overflow;
    }

    public double getProviderEnergyDemand(double maxRequired) {
        Preconditions.checkArgument((maxRequired >= 0.0 ? 1 : 0) != 0, (Object)"maxRequired must be >= 0");
        double required = 0.0;
        Iterator it = this.requesters.iterator();
        while (required < maxRequired && it.hasNext()) {
            IAEPowerStorage node = (IAEPowerStorage)it.next();
            if (node.getPowerFlow() == AccessRestriction.READ) continue;
            required += Math.max(0.0, node.getAEMaxPower() - node.getAECurrentPower());
        }
        return required;
    }

    @Override
    public double getAvgPowerUsage() {
        return this.avgDrainPerTick;
    }

    @Override
    public double getAvgPowerInjection() {
        return this.avgInjectionPerTick;
    }

    @Override
    public boolean isNetworkPowered() {
        return this.publicHasPower;
    }

    @Override
    public double injectPower(double amt, Actionable mode) {
        EnergyService service;
        double leftover = amt;
        Iterator<EnergyService> iterator = this.getConnectedServices().iterator();
        while (iterator.hasNext() && !((leftover = (service = iterator.next()).injectProviderPower(leftover, mode)) <= 0.0)) {
        }
        return leftover;
    }

    @Override
    public double getStoredPower() {
        if (this.availableTicksSinceUpdate > 90) {
            this.refreshPower();
        }
        return Math.max(0.0, this.globalAvailablePower);
    }

    @Override
    public double getMaxStoredPower() {
        return this.providerPowerSum + this.localStorage.getAEMaxPower();
    }

    @Override
    public double getEnergyDemand(double maxRequired) {
        double required = 0.0;
        for (EnergyService service : this.getConnectedServices()) {
            if ((required += service.getProviderEnergyDemand(maxRequired - required)) >= maxRequired) break;
        }
        return required;
    }

    @Override
    public void removeNode(IGridNode node) {
        IEnergyWatcher watcher;
        IAEPowerStorage ps;
        this.localStorage.removeNode();
        IEnergyOverlayGridConnection gridProvider = node.getService(IEnergyOverlayGridConnection.class);
        if (gridProvider != null) {
            this.overlayGridConnections.remove((Object)gridProvider);
            this.invalidateOverlayEnergyGrid();
        }
        GridNode gridNode = (GridNode)node;
        this.drainPerTick -= gridNode.getPreviousDraw();
        IPassiveEnergyGenerator passiveGenerator = node.getService(IPassiveEnergyGenerator.class);
        if (passiveGenerator != null) {
            this.passiveGenerators.remove(passiveGenerator);
            EnergyOverlayGrid overlayGrid = this.getOverlayGrid();
            if (overlayGrid.getCurrentPassiveGenerator() == passiveGenerator) {
                overlayGrid.setCurrentPassiveGenerator(null);
            }
        }
        if ((ps = node.getService(IAEPowerStorage.class)) != null && ps.isAEPublicPowerStorage()) {
            if (ps.getPowerFlow() != AccessRestriction.WRITE) {
                this.providerPowerSum -= ps.getAEMaxPower();
                this.globalAvailablePower -= ps.getAECurrentPower();
            }
            this.removeProvider(ps);
            this.removeRequester(ps);
        }
        if ((watcher = this.watchers.remove(node)) != null) {
            watcher.reset();
        }
    }

    private void addRequester(IAEPowerStorage requester) {
        Preconditions.checkState((!this.ongoingInjectOperation ? 1 : 0) != 0, (Object)"Cannot modify energy requesters while energy is being injected.");
        if (requester.getPowerFlow().isAllowInsertion()) {
            this.requesters.add(requester);
        }
    }

    private void removeRequester(IAEPowerStorage requester) {
        Preconditions.checkState((!this.ongoingInjectOperation ? 1 : 0) != 0, (Object)"Cannot modify energy requesters while energy is being injected.");
        this.requesters.remove(requester);
    }

    private void addProvider(IAEPowerStorage provider) {
        Preconditions.checkState((!this.ongoingExtractOperation ? 1 : 0) != 0, (Object)"Cannot modify energy providers while energy is being extracted.");
        if (provider.getPowerFlow().isAllowExtraction()) {
            this.providers.add(provider);
        }
    }

    private void removeProvider(IAEPowerStorage provider) {
        Preconditions.checkState((!this.ongoingExtractOperation ? 1 : 0) != 0, (Object)"Cannot modify energy providers while energy is being extracted.");
        this.providers.remove(provider);
    }

    @Override
    public void addNode(IGridNode node, @Nullable CompoundTag storedData) {
        double buffer;
        IEnergyWatcherNode ews;
        IAEPowerStorage ps;
        this.localStorage.addNode();
        IEnergyOverlayGridConnection gridProvider = node.getService(IEnergyOverlayGridConnection.class);
        if (gridProvider != null) {
            this.overlayGridConnections.add((Object)gridProvider);
            this.invalidateOverlayEnergyGrid();
        }
        GridNode gridNode = (GridNode)node;
        gridNode.setPreviousDraw(node.getIdlePowerUsage());
        this.drainPerTick += gridNode.getPreviousDraw();
        IPassiveEnergyGenerator passiveGenerator = node.getService(IPassiveEnergyGenerator.class);
        if (passiveGenerator != null) {
            this.passiveGenerators.add(passiveGenerator);
        }
        if ((ps = node.getService(IAEPowerStorage.class)) != null && ps.isAEPublicPowerStorage()) {
            AccessRestriction powerFlow = ps.getPowerFlow();
            if (powerFlow.isAllowExtraction()) {
                this.globalAvailablePower += ps.getAECurrentPower();
                this.providerPowerSum += ps.getAEMaxPower();
            }
            this.addProvider(ps);
            this.addRequester(ps);
        }
        if ((ews = node.getService(IEnergyWatcherNode.class)) != null) {
            EnergyWatcher iw = new EnergyWatcher(this, ews);
            this.watchers.put(node, iw);
            ews.updateWatcher(iw);
        }
        if (storedData != null && storedData.contains(TAG_STORED_ENERGY, 6) && (buffer = storedData.getDouble(TAG_STORED_ENERGY)) > 0.0) {
            this.localStorage.injectAEPower(buffer, Actionable.MODULATE);
        }
    }

    @Override
    public void saveNodeData(IGridNode gridNode, CompoundTag savedData) {
        double perNodeStorage = this.localStorage.getNodeEnergyShare();
        if (perNodeStorage > 0.0) {
            savedData.putDouble(TAG_STORED_ENERGY, perNodeStorage);
        }
    }

    public boolean registerEnergyInterest(EnergyThreshold threshold) {
        return this.interests.add(threshold);
    }

    public boolean unregisterEnergyInterest(EnergyThreshold threshold) {
        return this.interests.remove(threshold);
    }

    public void invalidateOverlayEnergyGrid() {
        if (this.overlayGrid != null) {
            this.overlayGrid.invalidate();
        }
    }

    private List<EnergyService> getConnectedServices() {
        return this.getOverlayGrid().energyServices;
    }

    private EnergyOverlayGrid getOverlayGrid() {
        if (this.overlayGrid == null) {
            EnergyOverlayGrid.buildCache(this);
        }
        return Objects.requireNonNull(this.overlayGrid);
    }

    static {
        GridHelper.addGridServiceEventHandler(GridPowerIdleChange.class, IEnergyService.class, (service, event) -> ((EnergyService)service).nodeIdlePowerChangeHandler((GridPowerIdleChange)event));
        GridHelper.addGridServiceEventHandler(GridPowerStorageStateChanged.class, IEnergyService.class, (service, event) -> ((EnergyService)service).storagePowerChangeHandler((GridPowerStorageStateChanged)event));
        COMPARATOR_HIGHEST_PRIORITY_FIRST = (o1, o2) -> {
            int cmp = Integer.compare(o2.getPriority(), o1.getPriority());
            return cmp != 0 ? cmp : Integer.compare(System.identityHashCode(o2), System.identityHashCode(o1));
        };
        COMPARATOR_LOWEST_PRIORITY_FIRST = (o1, o2) -> -COMPARATOR_HIGHEST_PRIORITY_FIRST.compare((IAEPowerStorage)o1, (IAEPowerStorage)o2);
    }
}

