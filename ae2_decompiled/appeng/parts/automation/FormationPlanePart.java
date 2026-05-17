/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.MenuType
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.phys.Vec3
 *  net.neoforged.neoforge.client.model.data.ModelData
 *  org.jetbrains.annotations.Nullable
 */
package appeng.parts.automation;

import appeng.api.behaviors.PlacementStrategy;
import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.config.IncludeExclude;
import appeng.api.config.Setting;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.security.IActionSource;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.api.stacks.AEKey;
import appeng.api.storage.IStorageMounts;
import appeng.api.storage.IStorageProvider;
import appeng.api.storage.MEStorage;
import appeng.api.util.AECableType;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigManagerBuilder;
import appeng.core.definitions.AEItems;
import appeng.helpers.IConfigInvHost;
import appeng.helpers.IPriorityHost;
import appeng.items.parts.PartModels;
import appeng.menu.ISubMenu;
import appeng.menu.MenuOpener;
import appeng.menu.implementations.FormationPlaneMenu;
import appeng.menu.locator.MenuLocators;
import appeng.parts.automation.PlaneConnectionHelper;
import appeng.parts.automation.PlaneConnections;
import appeng.parts.automation.PlaneModelData;
import appeng.parts.automation.PlaneModels;
import appeng.parts.automation.StackWorldBehaviors;
import appeng.parts.automation.UpgradeablePart;
import appeng.util.ConfigInventory;
import appeng.util.prioritylist.IPartitionList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

public class FormationPlanePart
extends UpgradeablePart
implements IStorageProvider,
IPriorityHost,
IConfigInvHost {
    private static final PlaneModels MODELS = new PlaneModels("part/formation_plane", "part/formation_plane_on");
    private boolean wasOnline = false;
    private int priority = 0;
    private final PlaneConnectionHelper connectionHelper = new PlaneConnectionHelper(this);
    private final MEStorage inventory = new InWorldStorage();
    private final ConfigInventory config;
    @Nullable
    private PlacementStrategy placementStrategies;
    private IncludeExclude filterMode = IncludeExclude.WHITELIST;
    private IPartitionList filter;

    public FormationPlanePart(IPartItem<?> partItem) {
        super(partItem);
        this.getMainNode().addService(IStorageProvider.class, this);
        this.config = ConfigInventory.configTypes(63).supportedTypes(StackWorldBehaviors.withPlacementStrategy()).changeListener(this::updateFilter).build();
    }

    @Override
    protected void registerSettings(IConfigManagerBuilder builder) {
        super.registerSettings(builder);
        builder.registerSetting(Settings.PLACE_BLOCK, YesNo.YES);
        builder.registerSetting(Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL);
    }

    protected final PlacementStrategy getPlacementStrategies() {
        if (this.placementStrategies == null) {
            IGridNode node = this.getMainNode().getNode();
            if (node == null) {
                return PlacementStrategy.noop();
            }
            BlockEntity self = this.getHost().getBlockEntity();
            BlockPos pos = self.getBlockPos().relative(this.getSide());
            Direction side = this.getSide().getOpposite();
            UUID owningPlayerId = this.getMainNode().getNode().getOwningPlayerProfileId();
            this.placementStrategies = StackWorldBehaviors.createPlacementStrategies((ServerLevel)self.getLevel(), pos, side, self, owningPlayerId);
        }
        return this.placementStrategies;
    }

    protected final void updateFilter() {
        this.filter = this.createFilter();
        this.filterMode = this.isUpgradedWith(AEItems.INVERTER_CARD) ? IncludeExclude.BLACKLIST : IncludeExclude.WHITELIST;
    }

    @Override
    protected int getUpgradeSlots() {
        return 5;
    }

    @Override
    public void upgradesChanged() {
        this.updateFilter();
    }

    @Override
    public void onSettingChanged(IConfigManager manager, Setting<?> setting) {
        this.getHost().markForSave();
    }

    private void remountStorage() {
        IStorageProvider.requestUpdate(this.getMainNode());
    }

    @Override
    protected void onMainNodeStateChanged(IGridNodeListener.State reason) {
        boolean currentOnline = this.getMainNode().isOnline();
        if (this.wasOnline != currentOnline) {
            this.wasOnline = currentOnline;
            this.remountStorage();
            this.getHost().markForUpdate();
        }
    }

    @Override
    public void getBoxes(IPartCollisionHelper bch) {
        this.connectionHelper.getBoxes(bch);
    }

    public PlaneConnections getConnections() {
        return this.connectionHelper.getConnections();
    }

    @Override
    public void onNeighborChanged(BlockGetter level, BlockPos pos, BlockPos neighbor) {
        if (pos.relative(this.getSide()).equals((Object)neighbor)) {
            if (!this.isClientSide()) {
                this.getPlacementStrategies().clearBlocked();
            }
        } else {
            this.connectionHelper.updateConnections();
        }
    }

    @Override
    public void onUpdateShape(Direction side) {
        Direction ourSide = this.getSide();
        if (side.equals((Object)ourSide)) {
            if (!this.isClientSide()) {
                this.getPlacementStrategies().clearBlocked();
            }
        } else if (ourSide.getAxis() != side.getAxis()) {
            this.connectionHelper.updateConnections();
        }
    }

    protected long placeInWorld(AEKey what, long amount, Actionable type) {
        YesNo placeBlock = this.getConfigManager().getSetting(Settings.PLACE_BLOCK);
        return this.getPlacementStrategies().placeInWorld(what, amount, type, placeBlock != YesNo.YES);
    }

    @Override
    public float getCableConnectionLength(AECableType cable) {
        return 1.0f;
    }

    @Override
    public void readFromNBT(CompoundTag data, HolderLookup.Provider registries) {
        super.readFromNBT(data, registries);
        this.priority = data.getInt("priority");
        this.config.readFromChildTag(data, "config", registries);
        this.remountStorage();
    }

    @Override
    public void writeToNBT(CompoundTag data, HolderLookup.Provider registries) {
        super.writeToNBT(data, registries);
        data.putInt("priority", this.getPriority());
        this.config.writeToChildTag(data, "config", registries);
    }

    @Override
    public int getPriority() {
        return this.priority;
    }

    @Override
    public void setPriority(int newValue) {
        this.priority = newValue;
        this.getHost().markForSave();
        this.remountStorage();
    }

    @Override
    public void mountInventories(IStorageMounts mounts) {
        if (this.getMainNode().isOnline()) {
            this.updateFilter();
            mounts.mount(this.inventory, this.priority);
        }
    }

    @Override
    public void returnToMainMenu(Player player, ISubMenu subMenu) {
        MenuOpener.returnTo(this.getMenuType(), player, MenuLocators.forPart(this));
    }

    @Override
    public ItemStack getMainMenuIcon() {
        return new ItemStack(this.getPartItem());
    }

    private void openConfigMenu(Player player) {
        MenuOpener.open(this.getMenuType(), player, MenuLocators.forPart(this));
    }

    protected MenuType<?> getMenuType() {
        return FormationPlaneMenu.TYPE;
    }

    private IPartitionList createFilter() {
        IPartitionList.Builder builder = IPartitionList.builder();
        if (this.isUpgradedWith(AEItems.FUZZY_CARD)) {
            builder.fuzzyMode(this.getConfigManager().getSetting(Settings.FUZZY_MODE));
        }
        int slotsToUse = 18 + this.getInstalledUpgrades(AEItems.CAPACITY_CARD) * 9;
        for (int x = 0; x < this.config.size() && x < slotsToUse; ++x) {
            builder.add(this.config.getKey(x));
        }
        return builder.build();
    }

    @Override
    public boolean onUseWithoutItem(Player player, Vec3 pos) {
        if (!this.isClientSide()) {
            this.openConfigMenu(player);
        }
        return true;
    }

    @Override
    public ConfigInventory getConfig() {
        return this.config;
    }

    @PartModels
    public static List<IPartModel> getModels() {
        return MODELS.getModels();
    }

    @Override
    public IPartModel getStaticModels() {
        return MODELS.getModel(this.isPowered(), this.isActive());
    }

    @Override
    public ModelData getModelData() {
        return ModelData.builder().with(PlaneModelData.CONNECTIONS, (Object)this.getConnections()).build();
    }

    class InWorldStorage
    implements MEStorage {
        InWorldStorage() {
        }

        @Override
        public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
            if (FormationPlanePart.this.filter != null && !FormationPlanePart.this.filter.matchesFilter(what, FormationPlanePart.this.filterMode)) {
                return 0L;
            }
            return FormationPlanePart.this.placeInWorld(what, amount, mode);
        }

        @Override
        public Component getDescription() {
            return FormationPlanePart.this.getPartItem().asItem().getDescription();
        }
    }
}

