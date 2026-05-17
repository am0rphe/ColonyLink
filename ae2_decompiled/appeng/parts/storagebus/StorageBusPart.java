/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.core.component.DataComponentMap
 *  net.minecraft.core.component.DataComponentMap$Builder
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.MenuType
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.phys.Vec3
 *  net.neoforged.neoforge.capabilities.ICapabilityInvalidationListener
 *  org.jetbrains.annotations.Nullable
 */
package appeng.parts.storagebus;

import appeng.api.AECapabilities;
import appeng.api.behaviors.ExternalStorageStrategy;
import appeng.api.config.AccessRestriction;
import appeng.api.config.FuzzyMode;
import appeng.api.config.IncludeExclude;
import appeng.api.config.Setting;
import appeng.api.config.Settings;
import appeng.api.config.StorageFilter;
import appeng.api.config.YesNo;
import appeng.api.features.IPlayerRegistry;
import appeng.api.ids.AEComponents;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.ITickManager;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.api.stacks.AEKeyType;
import appeng.api.storage.IStorageMounts;
import appeng.api.storage.IStorageProvider;
import appeng.api.storage.MEStorage;
import appeng.api.util.AECableType;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigManagerBuilder;
import appeng.core.AppEng;
import appeng.core.definitions.AEItems;
import appeng.core.settings.TickRates;
import appeng.core.stats.AdvancementTriggers;
import appeng.helpers.IConfigInvHost;
import appeng.helpers.IPriorityHost;
import appeng.helpers.InterfaceLogicHost;
import appeng.items.parts.PartModels;
import appeng.me.helpers.MachineSource;
import appeng.me.storage.CompositeStorage;
import appeng.me.storage.ITickingMonitor;
import appeng.me.storage.MEInventoryHandler;
import appeng.me.storage.NullInventory;
import appeng.menu.ISubMenu;
import appeng.menu.MenuOpener;
import appeng.menu.implementations.StorageBusMenu;
import appeng.menu.locator.MenuLocators;
import appeng.parts.PartAdjacentApi;
import appeng.parts.PartModel;
import appeng.parts.automation.StackWorldBehaviors;
import appeng.parts.automation.UpgradeablePart;
import appeng.util.ConfigInventory;
import appeng.util.Platform;
import appeng.util.SettingsFrom;
import appeng.util.prioritylist.IPartitionList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.ICapabilityInvalidationListener;
import org.jetbrains.annotations.Nullable;

public class StorageBusPart
extends UpgradeablePart
implements IGridTickable,
IStorageProvider,
IPriorityHost,
IConfigInvHost {
    public static final ResourceLocation MODEL_BASE = AppEng.makeId("part/storage_bus_base");
    @PartModels
    public static final IPartModel MODELS_OFF = new PartModel(MODEL_BASE, AppEng.makeId("part/storage_bus_off"));
    @PartModels
    public static final IPartModel MODELS_ON = new PartModel(MODEL_BASE, AppEng.makeId("part/storage_bus_on"));
    @PartModels
    public static final IPartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE, AppEng.makeId("part/storage_bus_has_channel"));
    protected final IActionSource source;
    private final ConfigInventory config = ConfigInventory.configTypes(63).changeListener(this::onConfigurationChanged).build();
    private final StorageBusInventory handler = new StorageBusInventory(NullInventory.of());
    @Nullable
    private Component handlerDescription;
    private final PartAdjacentApi<MEStorage> adjacentStorageAccessor;
    @Nullable
    private Map<AEKeyType, ExternalStorageStrategy> externalStorageStrategies;
    private boolean wasOnline = false;
    private int priority = 0;
    private PendingUpdateStatus updateStatus = PendingUpdateStatus.FAST_UPDATE;
    private ITickingMonitor monitor = null;
    private final ICapabilityInvalidationListener capabilityListener = () -> {
        if (!PartAdjacentApi.isPartValid(this)) {
            return false;
        }
        this.onCapabilityInvalidation();
        return true;
    };

    public StorageBusPart(IPartItem<?> partItem) {
        super(partItem);
        this.adjacentStorageAccessor = new PartAdjacentApi<MEStorage>(this, AECapabilities.ME_STORAGE);
        this.source = new MachineSource(this);
        this.getMainNode().addService(IStorageProvider.class, this).addService(IGridTickable.class, this);
    }

    @Override
    protected void registerSettings(IConfigManagerBuilder builder) {
        super.registerSettings(builder);
        builder.registerSetting(Settings.ACCESS, AccessRestriction.READ_WRITE);
        builder.registerSetting(Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL);
        builder.registerSetting(Settings.STORAGE_FILTER, StorageFilter.EXTRACTABLE_ONLY);
        builder.registerSetting(Settings.FILTER_ON_EXTRACT, YesNo.YES);
    }

    @Override
    public void addToWorld() {
        super.addToWorld();
        Level level = this.getLevel();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            BlockPos targetPos = this.getBlockEntity().getBlockPos().relative(this.getSide());
            serverLevel.registerCapabilityListener(targetPos, this.capabilityListener);
        }
    }

    @Override
    protected final void onMainNodeStateChanged(IGridNodeListener.State reason) {
        boolean currentOnline = this.getMainNode().isOnline();
        if (this.wasOnline != currentOnline) {
            this.wasOnline = currentOnline;
            this.getHost().markForUpdate();
            this.remountStorage();
        }
    }

    private void remountStorage() {
        IStorageProvider.requestUpdate(this.getMainNode());
    }

    @Override
    public void onSettingChanged(IConfigManager manager, Setting<?> setting) {
        this.onConfigurationChanged();
        this.getHost().markForSave();
    }

    @Override
    public final void upgradesChanged() {
        super.upgradesChanged();
        this.onConfigurationChanged();
    }

    private void scheduleUpdate() {
        if (this.isClientSide()) {
            return;
        }
        this.updateStatus = PendingUpdateStatus.FAST_UPDATE;
        this.getMainNode().ifPresent((grid, node) -> grid.getTickManager().alertDevice((IGridNode)node));
    }

    @Override
    public void readFromNBT(CompoundTag data, HolderLookup.Provider registries) {
        super.readFromNBT(data, registries);
        this.priority = data.getInt("priority");
        this.config.readFromChildTag(data, "config", registries);
    }

    @Override
    public void writeToNBT(CompoundTag data, HolderLookup.Provider registries) {
        super.writeToNBT(data, registries);
        data.putInt("priority", this.priority);
        this.config.writeToChildTag(data, "config", registries);
    }

    @Override
    public final boolean onUseWithoutItem(Player player, Vec3 pos) {
        if (!this.isClientSide()) {
            this.openConfigMenu(player);
        }
        return true;
    }

    protected final void openConfigMenu(Player player) {
        MenuOpener.open(this.getMenuType(), player, MenuLocators.forPart(this));
    }

    @Override
    public void returnToMainMenu(Player player, ISubMenu subMenu) {
        MenuOpener.returnTo(this.getMenuType(), player, MenuLocators.forPart(this));
    }

    @Override
    public ItemStack getMainMenuIcon() {
        return new ItemStack(this.getPartItem());
    }

    public MenuType<?> getMenuType() {
        return StorageBusMenu.TYPE;
    }

    @Override
    public final void getBoxes(IPartCollisionHelper bch) {
        bch.addBox(3.0, 3.0, 15.0, 13.0, 13.0, 16.0);
        bch.addBox(2.0, 2.0, 14.0, 14.0, 14.0, 15.0);
        bch.addBox(5.0, 5.0, 12.0, 11.0, 11.0, 14.0);
    }

    @Override
    protected final int getUpgradeSlots() {
        return 5;
    }

    @Override
    public final float getCableConnectionLength(AECableType cable) {
        return 4.0f;
    }

    @Override
    public final void onNeighborChanged(BlockGetter level, BlockPos pos, BlockPos neighbor) {
        if (pos.relative(this.getSide()).equals((Object)neighbor) && !this.isClientSide()) {
            this.getMainNode().ifPresent((grid, node) -> grid.getTickManager().alertDevice((IGridNode)node));
        }
    }

    @Override
    public final TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(TickRates.StorageBus, false);
    }

    @Override
    public final TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        if (this.updateStatus != PendingUpdateStatus.NO_UPDATE) {
            this.updateTarget(false);
        }
        if (this.monitor != null) {
            return this.monitor.onTick();
        }
        return this.updateStatus == PendingUpdateStatus.SLOW_UPDATE ? TickRateModulation.IDLE : TickRateModulation.SLEEP;
    }

    public MEStorage getInternalHandler() {
        return this.handler.getDelegate();
    }

    private boolean hasRegisteredCellToNetwork() {
        return this.getMainNode().isOnline() && !(this.handler.getDelegate() instanceof NullInventory);
    }

    public Component getConnectedToDescription() {
        return this.handlerDescription;
    }

    protected void onConfigurationChanged() {
        if (this.getMainNode().isReady()) {
            this.updateTarget(true);
        }
    }

    private void onCapabilityInvalidation() {
        this.handler.setDelegate(NullInventory.of());
        this.scheduleUpdate();
    }

    private void updateTarget(boolean forceFullUpdate) {
        ITickingMonitor tickingMonitor;
        MEStorage newInventory;
        MEStorage mEStorage;
        if (this.isClientSide()) {
            return;
        }
        MEStorage foundMonitor = null;
        Map<AEKeyType, MEStorage> foundExternalApi = Collections.emptyMap();
        if (Platform.areBlockEntitiesTicking(this.getLevel(), this.getBlockEntity().getBlockPos().relative(this.getSide()))) {
            this.updateStatus = PendingUpdateStatus.NO_UPDATE;
            foundMonitor = this.adjacentStorageAccessor.find();
            if (foundMonitor == null) {
                foundExternalApi = new IdentityHashMap(2);
                this.findExternalStorages(foundExternalApi);
            }
        } else {
            this.updateStatus = PendingUpdateStatus.SLOW_UPDATE;
        }
        if (!forceFullUpdate && (mEStorage = this.handler.getDelegate()) instanceof CompositeStorage) {
            CompositeStorage compositeStorage = (CompositeStorage)mEStorage;
            if (!foundExternalApi.isEmpty()) {
                compositeStorage.setStorages(foundExternalApi);
                this.handlerDescription = compositeStorage.getDescription();
                return;
            }
        }
        if (!forceFullUpdate && foundMonitor == this.handler.getDelegate()) {
            return;
        }
        boolean wasSleeping = this.monitor == null;
        boolean wasRegistered = this.hasRegisteredCellToNetwork();
        if (foundMonitor != null) {
            newInventory = foundMonitor;
            this.checkStorageBusOnInterface();
            this.handlerDescription = newInventory.getDescription();
        } else if (!foundExternalApi.isEmpty()) {
            newInventory = new CompositeStorage(foundExternalApi);
            this.handlerDescription = newInventory.getDescription();
        } else {
            newInventory = NullInventory.of();
            this.handlerDescription = null;
        }
        this.handler.setDelegate(newInventory);
        this.handler.setAccessRestriction(this.getConfigManager().getSetting(Settings.ACCESS));
        this.handler.setWhitelist(this.isUpgradedWith(AEItems.INVERTER_CARD) ? IncludeExclude.BLACKLIST : IncludeExclude.WHITELIST);
        this.handler.setPartitionList(this.createFilter());
        this.handler.setVoidOverflow(this.isUpgradedWith(AEItems.VOID_CARD));
        boolean filterOnExtract = this.getConfigManager().getSetting(Settings.FILTER_ON_EXTRACT) == YesNo.YES;
        this.handler.setExtractFiltering(filterOnExtract, this.isExtractableOnly() && filterOnExtract);
        this.monitor = newInventory instanceof ITickingMonitor ? (tickingMonitor = (ITickingMonitor)((Object)newInventory)) : null;
        if (wasSleeping != (this.monitor == null)) {
            this.getMainNode().ifPresent((grid, node) -> {
                ITickManager tm = grid.getTickManager();
                if (this.monitor == null) {
                    tm.sleepDevice((IGridNode)node);
                } else {
                    tm.wakeDevice((IGridNode)node);
                }
            });
        }
        if (wasRegistered != this.hasRegisteredCellToNetwork()) {
            this.remountStorage();
        }
    }

    private boolean isExtractableOnly() {
        return this.getConfigManager().getSetting(Settings.STORAGE_FILTER) == StorageFilter.EXTRACTABLE_ONLY;
    }

    private IPartitionList createFilter() {
        IPartitionList.Builder filterBuilder = IPartitionList.builder();
        if (this.isUpgradedWith(AEItems.FUZZY_CARD)) {
            filterBuilder.fuzzyMode(this.getConfigManager().getSetting(Settings.FUZZY_MODE));
        }
        int slotsToUse = 18 + this.getInstalledUpgrades(AEItems.CAPACITY_CARD) * 9;
        for (int x = 0; x < this.config.size() && x < slotsToUse; ++x) {
            filterBuilder.add(this.config.getKey(x));
        }
        return filterBuilder.build();
    }

    private void findExternalStorages(Map<AEKeyType, MEStorage> storages) {
        boolean extractableOnly = this.isExtractableOnly();
        for (Map.Entry<AEKeyType, ExternalStorageStrategy> entry : this.getExternalStorageStrategies().entrySet()) {
            MEStorage wrapper = entry.getValue().createWrapper(extractableOnly, this::invalidateOnExternalStorageChange);
            if (wrapper == null) continue;
            storages.put(entry.getKey(), wrapper);
        }
    }

    private void invalidateOnExternalStorageChange() {
        this.getMainNode().ifPresent((grid, node) -> grid.getTickManager().alertDevice((IGridNode)node));
    }

    private void checkStorageBusOnInterface() {
        MinecraftServer server;
        ServerPlayer player;
        BlockEntity targetBe;
        Direction oppositeSide = this.getSide().getOpposite();
        BlockPos targetPos = this.getBlockEntity().getBlockPos().relative(this.getSide());
        Object targetHost = targetBe = this.getLevel().getBlockEntity(targetPos);
        if (targetBe instanceof IPartHost) {
            IPartHost partHost = (IPartHost)targetBe;
            targetHost = partHost.getPart(oppositeSide);
        }
        if (targetHost instanceof InterfaceLogicHost && (player = IPlayerRegistry.getConnected(server = this.getLevel().getServer(), this.getActionableNode().getOwningPlayerId())) != null) {
            AdvancementTriggers.RECURSIVE.trigger(player);
        }
    }

    @Override
    public void mountInventories(IStorageMounts mounts) {
        if (this.hasRegisteredCellToNetwork()) {
            mounts.mount(this.handler, this.priority);
        }
    }

    @Override
    public final int getPriority() {
        return this.priority;
    }

    @Override
    public final void setPriority(int newValue) {
        this.priority = newValue;
        this.getHost().markForSave();
        this.remountStorage();
    }

    @Override
    public ConfigInventory getConfig() {
        return this.config;
    }

    @Override
    public void importSettings(SettingsFrom mode, DataComponentMap input, @Nullable Player player) {
        super.importSettings(mode, input, player);
        List configInv = (List)input.get(AEComponents.EXPORTED_CONFIG_INV);
        if (configInv != null) {
            this.config.readFromList(configInv);
        }
    }

    @Override
    public void exportSettings(SettingsFrom mode, DataComponentMap.Builder builder) {
        super.exportSettings(mode, builder);
        if (mode == SettingsFrom.MEMORY_CARD) {
            builder.set(AEComponents.EXPORTED_CONFIG_INV, this.config.toList());
        }
    }

    @Override
    public IPartModel getStaticModels() {
        if (this.isActive() && this.isPowered()) {
            return MODELS_HAS_CHANNEL;
        }
        if (this.isPowered()) {
            return MODELS_ON;
        }
        return MODELS_OFF;
    }

    private Map<AEKeyType, ExternalStorageStrategy> getExternalStorageStrategies() {
        if (this.externalStorageStrategies == null) {
            BlockEntity host = this.getHost().getBlockEntity();
            this.externalStorageStrategies = StackWorldBehaviors.createExternalStorageStrategies((ServerLevel)host.getLevel(), host.getBlockPos().relative(this.getSide()), this.getSide().getOpposite());
        }
        return this.externalStorageStrategies;
    }

    private static class StorageBusInventory
    extends MEInventoryHandler {
        public StorageBusInventory(MEStorage inventory) {
            super(inventory);
        }

        @Override
        protected MEStorage getDelegate() {
            return super.getDelegate();
        }

        @Override
        protected void setDelegate(MEStorage delegate) {
            super.setDelegate(delegate);
        }

        public void setAccessRestriction(AccessRestriction setting) {
            this.setAllowExtraction(setting.isAllowExtraction());
            this.setAllowInsertion(setting.isAllowInsertion());
        }
    }

    private static enum PendingUpdateStatus {
        FAST_UPDATE,
        SLOW_UPDATE,
        NO_UPDATE;

    }
}

