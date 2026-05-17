/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Object2LongMap$Entry
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.phys.Vec3
 *  org.jetbrains.annotations.Nullable
 */
package appeng.parts.automation;

import appeng.api.config.FuzzyMode;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.IGrid;
import appeng.api.networking.IStackWatcher;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.crafting.ICraftingWatcherNode;
import appeng.api.networking.storage.IStorageWatcherNode;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.util.IConfigManagerBuilder;
import appeng.core.AppEng;
import appeng.core.definitions.AEItems;
import appeng.helpers.IConfigInvHost;
import appeng.hooks.ticking.TickHandler;
import appeng.items.parts.PartModels;
import appeng.menu.MenuOpener;
import appeng.menu.implementations.StorageLevelEmitterMenu;
import appeng.menu.locator.MenuLocators;
import appeng.parts.PartModel;
import appeng.parts.automation.AbstractLevelEmitterPart;
import appeng.util.ConfigInventory;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class StorageLevelEmitterPart
extends AbstractLevelEmitterPart
implements IConfigInvHost,
ICraftingProvider {
    @PartModels
    public static final ResourceLocation MODEL_BASE_OFF = AppEng.makeId("part/level_emitter_base_off");
    @PartModels
    public static final ResourceLocation MODEL_BASE_ON = AppEng.makeId("part/level_emitter_base_on");
    @PartModels
    public static final ResourceLocation MODEL_STATUS_OFF = AppEng.makeId("part/level_emitter_status_off");
    @PartModels
    public static final ResourceLocation MODEL_STATUS_ON = AppEng.makeId("part/level_emitter_status_on");
    @PartModels
    public static final ResourceLocation MODEL_STATUS_HAS_CHANNEL = AppEng.makeId("part/level_emitter_status_has_channel");
    public static final PartModel MODEL_OFF_OFF = new PartModel(MODEL_BASE_OFF, MODEL_STATUS_OFF);
    public static final PartModel MODEL_OFF_ON = new PartModel(MODEL_BASE_OFF, MODEL_STATUS_ON);
    public static final PartModel MODEL_OFF_HAS_CHANNEL = new PartModel(MODEL_BASE_OFF, MODEL_STATUS_HAS_CHANNEL);
    public static final PartModel MODEL_ON_OFF = new PartModel(MODEL_BASE_ON, MODEL_STATUS_OFF);
    public static final PartModel MODEL_ON_ON = new PartModel(MODEL_BASE_ON, MODEL_STATUS_ON);
    public static final PartModel MODEL_ON_HAS_CHANNEL = new PartModel(MODEL_BASE_ON, MODEL_STATUS_HAS_CHANNEL);
    private final ConfigInventory config = ConfigInventory.configTypes(1).changeListener(this::configureWatchers).build();
    private IStackWatcher storageWatcher;
    private IStackWatcher craftingWatcher;
    private long lastUpdateTick = -1L;
    private final IStorageWatcherNode stackWatcherNode = new IStorageWatcherNode(){

        @Override
        public void updateWatcher(IStackWatcher newWatcher) {
            StorageLevelEmitterPart.this.storageWatcher = newWatcher;
            StorageLevelEmitterPart.this.configureWatchers();
        }

        @Override
        public void onStackChange(AEKey what, long amount) {
            if (what.equals(StorageLevelEmitterPart.this.getConfiguredKey()) && !StorageLevelEmitterPart.this.isUpgradedWith(AEItems.FUZZY_CARD)) {
                StorageLevelEmitterPart.this.lastReportedValue = amount;
                StorageLevelEmitterPart.this.updateState();
            } else {
                long currentTick = TickHandler.instance().getCurrentTick();
                if (currentTick != StorageLevelEmitterPart.this.lastUpdateTick) {
                    StorageLevelEmitterPart.this.lastUpdateTick = currentTick;
                    StorageLevelEmitterPart.this.updateReportingValue(StorageLevelEmitterPart.this.getGridNode().getGrid());
                }
            }
        }
    };
    private final ICraftingWatcherNode craftingWatcherNode = new ICraftingWatcherNode(){

        @Override
        public void updateWatcher(IStackWatcher newWatcher) {
            StorageLevelEmitterPart.this.craftingWatcher = newWatcher;
            StorageLevelEmitterPart.this.configureWatchers();
        }

        @Override
        public void onRequestChange(AEKey what) {
            StorageLevelEmitterPart.this.updateState();
        }

        @Override
        public void onCraftableChange(AEKey what) {
        }
    };

    public StorageLevelEmitterPart(IPartItem<?> partItem) {
        super(partItem);
        this.getMainNode().addService(IStorageWatcherNode.class, this.stackWatcherNode);
        this.getMainNode().addService(ICraftingWatcherNode.class, this.craftingWatcherNode);
        this.getMainNode().addService(ICraftingProvider.class, this);
    }

    @Override
    protected void registerSettings(IConfigManagerBuilder builder) {
        super.registerSettings(builder);
        builder.registerSetting(Settings.CRAFT_VIA_REDSTONE, YesNo.NO);
        builder.registerSetting(Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL);
    }

    @Nullable
    private AEKey getConfiguredKey() {
        return this.config.getKey(0);
    }

    @Override
    protected final int getUpgradeSlots() {
        return 1;
    }

    @Override
    public final void upgradesChanged() {
        this.configureWatchers();
    }

    @Override
    protected boolean hasDirectOutput() {
        return this.isUpgradedWith(AEItems.CRAFTING_CARD);
    }

    @Override
    protected boolean getDirectOutput() {
        IGrid grid = this.getMainNode().getGrid();
        if (grid != null) {
            if (this.getConfiguredKey() != null) {
                return grid.getCraftingService().isRequesting(this.getConfiguredKey());
            }
            return grid.getCraftingService().isRequestingAny();
        }
        return false;
    }

    @Override
    public List<IPatternDetails> getAvailablePatterns() {
        return List.of();
    }

    @Override
    public boolean pushPattern(IPatternDetails patternDetails, KeyCounter[] inputHolder) {
        return false;
    }

    @Override
    public boolean isBusy() {
        return true;
    }

    @Override
    public Set<AEKey> getEmitableItems() {
        if (this.isUpgradedWith(AEItems.CRAFTING_CARD) && this.getConfigManager().getSetting(Settings.CRAFT_VIA_REDSTONE) == YesNo.YES && this.getConfiguredKey() != null) {
            return Set.of(this.getConfiguredKey());
        }
        return Set.of();
    }

    @Override
    protected void onReportingValueChanged() {
        this.getMainNode().ifPresent(this::updateReportingValue);
    }

    @Override
    protected void configureWatchers() {
        AEKey myStack = this.getConfiguredKey();
        if (this.storageWatcher != null) {
            this.storageWatcher.reset();
        }
        if (this.craftingWatcher != null) {
            this.craftingWatcher.reset();
        }
        ICraftingProvider.requestUpdate(this.getMainNode());
        if (this.isUpgradedWith(AEItems.CRAFTING_CARD)) {
            if (this.craftingWatcher != null) {
                if (myStack == null) {
                    this.craftingWatcher.setWatchAll(true);
                } else {
                    this.craftingWatcher.add(myStack);
                }
            }
        } else {
            if (this.storageWatcher != null) {
                if (this.isUpgradedWith(AEItems.FUZZY_CARD) || myStack == null) {
                    this.storageWatcher.setWatchAll(true);
                } else {
                    this.storageWatcher.add(myStack);
                }
            }
            this.getMainNode().ifPresent(this::updateReportingValue);
        }
        this.updateState();
    }

    private void updateReportingValue(IGrid grid) {
        KeyCounter stacks = grid.getStorageService().getCachedInventory();
        AEKey myStack = this.getConfiguredKey();
        if (myStack == null) {
            this.lastReportedValue = 0L;
            for (Object2LongMap.Entry<AEKey> st : stacks) {
                this.lastReportedValue += st.getLongValue();
                if (this.lastReportedValue <= this.getReportingValue()) continue;
                break;
            }
        } else if (this.isUpgradedWith(AEItems.FUZZY_CARD)) {
            this.lastReportedValue = 0L;
            FuzzyMode fzMode = this.getConfigManager().getSetting(Settings.FUZZY_MODE);
            Collection<Object2LongMap.Entry<AEKey>> fuzzyList = stacks.findFuzzy(myStack, fzMode);
            for (Object2LongMap.Entry<AEKey> st : fuzzyList) {
                this.lastReportedValue += st.getLongValue();
                if (this.lastReportedValue <= this.getReportingValue()) continue;
                break;
            }
        } else {
            this.lastReportedValue = stacks.get(myStack);
        }
        this.updateState();
    }

    @Override
    public void readFromNBT(CompoundTag data, HolderLookup.Provider registries) {
        super.readFromNBT(data, registries);
        this.config.readFromChildTag(data, "config", registries);
    }

    @Override
    public void writeToNBT(CompoundTag data, HolderLookup.Provider registries) {
        super.writeToNBT(data, registries);
        this.config.writeToChildTag(data, "config", registries);
    }

    @Override
    public boolean onUseWithoutItem(Player player, Vec3 pos) {
        if (!this.isClientSide()) {
            MenuOpener.open(StorageLevelEmitterMenu.TYPE, player, MenuLocators.forPart(this));
        }
        return true;
    }

    @Override
    public ConfigInventory getConfig() {
        return this.config;
    }

    @Override
    public IPartModel getStaticModels() {
        if (this.isActive() && this.isPowered()) {
            return this.isLevelEmitterOn() ? MODEL_ON_HAS_CHANNEL : MODEL_OFF_HAS_CHANNEL;
        }
        if (this.isPowered()) {
            return this.isLevelEmitterOn() ? MODEL_ON_ON : MODEL_OFF_ON;
        }
        return this.isLevelEmitterOn() ? MODEL_ON_OFF : MODEL_OFF_OFF;
    }
}

