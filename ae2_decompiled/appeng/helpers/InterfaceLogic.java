/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableSet
 *  it.unimi.dsi.fastutil.objects.Object2LongMap$Entry
 *  net.minecraft.core.Direction
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 *  org.jetbrains.annotations.Nullable
 */
package appeng.helpers;

import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.config.Settings;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.MEStorage;
import appeng.api.storage.StorageHelper;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.upgrades.UpgradeInventories;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalBlockPos;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.core.definitions.AEItems;
import appeng.core.settings.TickRates;
import appeng.helpers.InterfaceLogicHost;
import appeng.helpers.MultiCraftingTracker;
import appeng.me.helpers.MachineSource;
import appeng.me.storage.DelegatingMEInventory;
import appeng.util.ConfigInventory;
import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

public class InterfaceLogic
implements ICraftingRequester,
IUpgradeableObject,
IConfigurableObject {
    @Nullable
    private InterfaceInventory localInvHandler;
    @Nullable
    private MEStorage networkStorage;
    protected final InterfaceLogicHost host;
    protected final IManagedGridNode mainNode;
    protected final IActionSource actionSource;
    protected final IActionSource interfaceRequestSource;
    private final MultiCraftingTracker craftingTracker;
    private final IUpgradeInventory upgrades;
    private final IConfigManager cm;
    private final GenericStack[] plannedWork;
    private int priority;
    private final ConfigInventory config;
    private boolean hasConfig = false;
    private final ConfigInventory storage;

    public InterfaceLogic(IManagedGridNode gridNode, InterfaceLogicHost host, Item is) {
        this(gridNode, host, is, 9);
    }

    public InterfaceLogic(IManagedGridNode gridNode, InterfaceLogicHost host, Item is, int slots) {
        this.host = host;
        this.config = ConfigInventory.configStacks(slots).changeListener(this::onConfigRowChanged).build();
        this.storage = ConfigInventory.storage(slots).slotFilter(this::isAllowedInStorageSlot).changeListener(this::onStorageChanged).build();
        this.mainNode = gridNode.setFlags(GridFlags.REQUIRE_CHANNEL).addService(IGridTickable.class, new Ticker());
        this.actionSource = new MachineSource(this.mainNode::getNode);
        this.interfaceRequestSource = new InterfaceRequestSource(this, this.mainNode::getNode);
        gridNode.addService(ICraftingRequester.class, this);
        this.upgrades = UpgradeInventories.forMachine((ItemLike)is, 1, this::onUpgradesChanged);
        this.craftingTracker = new MultiCraftingTracker(this, slots);
        this.cm = IConfigManager.builder(this::onConfigChanged).registerSetting(Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL).build();
        this.plannedWork = new GenericStack[slots];
        this.getConfig().useRegisteredCapacities();
        this.getStorage().useRegisteredCapacities();
    }

    private boolean isAllowedInStorageSlot(int slot, AEKey what) {
        if (slot < this.config.size()) {
            AEKey configured = this.config.getKey(slot);
            if (configured == null || configured.equals(what)) {
                return true;
            }
            if (this.upgrades.isInstalled(AEItems.FUZZY_CARD)) {
                FuzzyMode fuzzyMode = this.getConfigManager().getSetting(Settings.FUZZY_MODE);
                return configured.fuzzyEquals(what, fuzzyMode);
            }
            return false;
        }
        return true;
    }

    public int getPriority() {
        return this.priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
        this.host.saveChanges();
    }

    private void readConfig() {
        this.hasConfig = !this.config.isEmpty();
        this.updatePlan();
        this.notifyNeighbors();
    }

    public void writeToNBT(CompoundTag tag, HolderLookup.Provider registries) {
        this.config.writeToChildTag(tag, "config", registries);
        this.storage.writeToChildTag(tag, "storage", registries);
        this.upgrades.writeToNBT(tag, "upgrades", registries);
        this.cm.writeToNBT(tag, registries);
        this.craftingTracker.writeToNBT(tag);
        tag.putInt("priority", this.priority);
    }

    public void readFromNBT(CompoundTag tag, HolderLookup.Provider registries) {
        this.craftingTracker.readFromNBT(tag);
        this.upgrades.readFromNBT(tag, "upgrades", registries);
        this.config.readFromChildTag(tag, "config", registries);
        this.storage.readFromChildTag(tag, "storage", registries);
        this.cm.readFromNBT(tag, registries);
        this.readConfig();
        this.priority = tag.getInt("priority");
    }

    protected final OptionalInt getRequestInterfacePriority(IActionSource src) {
        return src.context(InterfaceRequestContext.class).map(ctx -> OptionalInt.of(ctx.getPriority())).orElseGet(OptionalInt::empty);
    }

    protected final boolean isSameGrid(IActionSource src) {
        IGrid otherGrid = src.machine().map(IActionHost::getActionableNode).map(IGridNode::getGrid).orElse(null);
        return otherGrid == this.mainNode.getGrid();
    }

    protected final boolean hasWorkToDo() {
        for (GenericStack requiredWork : this.plannedWork) {
            if (requiredWork == null) continue;
            return true;
        }
        return false;
    }

    public void notifyNeighbors() {
        if (this.mainNode.isActive()) {
            this.mainNode.ifPresent((grid, node) -> grid.getTickManager().wakeDevice((IGridNode)node));
        }
        this.host.getBlockEntity().invalidateCapabilities();
    }

    public void gridChanged() {
        this.networkStorage = this.mainNode.getGrid().getStorageService().getInventory();
        this.notifyNeighbors();
    }

    @Override
    public IConfigManager getConfigManager() {
        return this.cm;
    }

    public ConfigInventory getStorage() {
        return this.storage;
    }

    public ConfigInventory getConfig() {
        return this.config;
    }

    public MEStorage getInventory() {
        if (this.hasConfig) {
            return this.getLocalInventory();
        }
        return this.networkStorage;
    }

    private MEStorage getLocalInventory() {
        if (this.localInvHandler == null) {
            this.localInvHandler = new InterfaceInventory();
        }
        return this.localInvHandler;
    }

    private boolean updateStorage() {
        boolean didSomething = false;
        for (int x = 0; x < this.plannedWork.length; ++x) {
            GenericStack work = this.plannedWork[x];
            if (work == null) continue;
            int amount = (int)work.amount();
            didSomething = this.usePlan(x, work.what(), amount) || didSomething;
        }
        return didSomething;
    }

    private boolean usePlan(int x, AEKey what, int amount) {
        boolean changed = this.tryUsePlan(x, what, amount);
        if (changed) {
            this.updatePlan(x);
        }
        return changed;
    }

    @Override
    public ImmutableSet<ICraftingLink> getRequestedJobs() {
        return this.craftingTracker.getRequestedJobs();
    }

    @Override
    public long insertCraftedItems(ICraftingLink link, AEKey what, long amount, Actionable mode) {
        int slot = this.craftingTracker.getSlot(link);
        return this.storage.insert(slot, what, amount, mode);
    }

    @Override
    public void jobStateChange(ICraftingLink link) {
        this.craftingTracker.jobStateChange(link);
    }

    @Override
    public IUpgradeInventory getUpgrades() {
        return this.upgrades;
    }

    @Override
    @Nullable
    public IGridNode getActionableNode() {
        return this.mainNode.getNode();
    }

    private void updatePlan() {
        boolean hadWork = this.hasWorkToDo();
        for (int x = 0; x < this.config.size(); ++x) {
            this.updatePlan(x);
        }
        boolean hasWork = this.hasWorkToDo();
        if (hadWork != hasWork) {
            this.mainNode.ifPresent((grid, node) -> {
                if (hasWork) {
                    grid.getTickManager().alertDevice((IGridNode)node);
                } else {
                    grid.getTickManager().sleepDevice((IGridNode)node);
                }
            });
        }
    }

    private void updatePlan(int slot) {
        GenericStack req = this.config.getStack(slot);
        GenericStack stored = this.storage.getStack(slot);
        this.plannedWork[slot] = req == null && stored != null ? new GenericStack(stored.what(), -stored.amount()) : (req != null ? (stored == null ? req : (this.storedRequestEquals(req.what(), stored.what()) ? (req.amount() != stored.amount() ? new GenericStack(req.what(), req.amount() - stored.amount()) : null) : new GenericStack(stored.what(), -stored.amount()))) : null);
    }

    private boolean storedRequestEquals(AEKey request, AEKey stored) {
        if (this.upgrades.isInstalled(AEItems.FUZZY_CARD) && request.supportsFuzzyRangeSearch()) {
            return request.fuzzyEquals(stored, this.cm.getSetting(Settings.FUZZY_MODE));
        }
        return request.equals(stored);
    }

    private boolean tryUsePlan(int slot, AEKey what, int amount) {
        IGrid grid = this.mainNode.getGrid();
        if (grid == null) {
            return false;
        }
        MEStorage networkInv = grid.getStorageService().getInventory();
        IEnergyService energySrc = grid.getEnergyService();
        if (amount < 0) {
            amount = -amount;
            GenericStack inSlot = this.storage.getStack(slot);
            if (!what.matches(inSlot) || inSlot.amount() < (long)amount) {
                return true;
            }
            int inserted = (int)StorageHelper.poweredInsert(energySrc, networkInv, what, amount, this.interfaceRequestSource);
            if (inserted > 0) {
                this.storage.extract(slot, what, inserted, Actionable.MODULATE);
            }
            return inserted > 0;
        }
        if (this.craftingTracker.isBusy(slot)) {
            return this.handleCrafting(slot, what, amount);
        }
        if (amount > 0) {
            if (this.storage.insert(slot, what, amount, Actionable.SIMULATE) != (long)amount) {
                return true;
            }
            if (this.acquireFromNetwork(energySrc, networkInv, slot, what, amount)) {
                return true;
            }
            if (this.storage.getStack(slot) == null && this.upgrades.isInstalled(AEItems.FUZZY_CARD)) {
                FuzzyMode fuzzyMode = this.getConfigManager().getSetting(Settings.FUZZY_MODE);
                for (Object2LongMap.Entry<AEKey> entry : grid.getStorageService().getCachedInventory().findFuzzy(what, fuzzyMode)) {
                    long maxAmount = this.storage.insert(slot, (AEKey)entry.getKey(), amount, Actionable.SIMULATE);
                    if (!this.acquireFromNetwork(energySrc, networkInv, slot, (AEKey)entry.getKey(), maxAmount)) continue;
                    return true;
                }
            }
            return this.handleCrafting(slot, what, amount);
        }
        return false;
    }

    private boolean acquireFromNetwork(IEnergyService energySrc, MEStorage networkInv, int slot, AEKey what, long amount) {
        long acquired = StorageHelper.poweredExtraction(energySrc, networkInv, what, amount, this.interfaceRequestSource);
        if (acquired > 0L) {
            long inserted = this.storage.insert(slot, what, acquired, Actionable.MODULATE);
            if (inserted < acquired) {
                throw new IllegalStateException("bad attempt at managing inventory. Voided items: " + inserted);
            }
            return true;
        }
        return false;
    }

    private boolean handleCrafting(int x, AEKey key, long amount) {
        IGrid grid = this.mainNode.getGrid();
        if (grid != null && this.upgrades.isInstalled(AEItems.CRAFTING_CARD) && key != null) {
            return this.craftingTracker.handleCrafting(x, key, amount, this.host.getBlockEntity().getLevel(), grid.getCraftingService(), this.actionSource);
        }
        return false;
    }

    private void cancelCrafting() {
        this.craftingTracker.cancel();
    }

    private void onConfigChanged() {
        this.host.saveChanges();
        this.updatePlan();
    }

    private void onUpgradesChanged() {
        this.host.saveChanges();
        if (!this.upgrades.isInstalled(AEItems.CRAFTING_CARD)) {
            this.cancelCrafting();
        }
        this.updatePlan();
    }

    private void onConfigRowChanged() {
        this.host.saveChanges();
        this.readConfig();
    }

    private void onStorageChanged() {
        this.host.saveChanges();
        this.updatePlan();
    }

    public void addDrops(List<ItemStack> drops) {
        for (ItemStack is : this.upgrades) {
            if (is.isEmpty()) continue;
            drops.add(is);
        }
        for (int i = 0; i < this.storage.size(); ++i) {
            GenericStack stack = this.storage.getStack(i);
            if (stack == null) continue;
            stack.what().addDrops(stack.amount(), drops, this.host.getBlockEntity().getLevel(), this.host.getBlockEntity().getBlockPos());
        }
    }

    public void clearContent() {
        this.upgrades.clear();
        this.storage.clear();
    }

    public AECableType getCableConnectionType(Direction dir) {
        return AECableType.SMART;
    }

    public DimensionalBlockPos getLocation() {
        return new DimensionalBlockPos(this.host.getBlockEntity());
    }

    private class Ticker
    implements IGridTickable {
        private Ticker() {
        }

        @Override
        public TickingRequest getTickingRequest(IGridNode node) {
            return new TickingRequest(TickRates.Interface, !InterfaceLogic.this.hasWorkToDo());
        }

        @Override
        public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
            if (!InterfaceLogic.this.mainNode.isActive()) {
                return TickRateModulation.SLEEP;
            }
            boolean couldDoWork = InterfaceLogic.this.updateStorage();
            return InterfaceLogic.this.hasWorkToDo() ? (couldDoWork ? TickRateModulation.URGENT : TickRateModulation.SLOWER) : TickRateModulation.SLEEP;
        }
    }

    private class InterfaceRequestSource
    extends MachineSource {
        private final InterfaceRequestContext context;

        InterfaceRequestSource(InterfaceLogic interfaceLogic, IActionHost v) {
            super(v);
            this.context = interfaceLogic.new InterfaceRequestContext();
        }

        @Override
        public <T> Optional<T> context(Class<T> key) {
            if (key == InterfaceRequestContext.class) {
                return Optional.of(key.cast(this.context));
            }
            return super.context(key);
        }
    }

    private class InterfaceRequestContext {
        private InterfaceRequestContext() {
        }

        public int getPriority() {
            return InterfaceLogic.this.priority;
        }
    }

    private class InterfaceInventory
    extends DelegatingMEInventory {
        InterfaceInventory() {
            super(InterfaceLogic.this.storage);
        }

        @Override
        public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
            if (InterfaceLogic.this.getRequestInterfacePriority(source).isPresent() && InterfaceLogic.this.isSameGrid(source)) {
                return 0L;
            }
            return super.insert(what, amount, mode, source);
        }

        @Override
        public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
            OptionalInt requestPriority = InterfaceLogic.this.getRequestInterfacePriority(source);
            if (requestPriority.isPresent() && requestPriority.getAsInt() <= InterfaceLogic.this.getPriority() && InterfaceLogic.this.isSameGrid(source)) {
                return 0L;
            }
            return super.extract(what, amount, mode, source);
        }

        @Override
        public Component getDescription() {
            return InterfaceLogic.this.host.getMainMenuIcon().getHoverName();
        }
    }
}

