/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.MenuType
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.phys.Vec3
 *  org.jetbrains.annotations.Nullable
 */
package appeng.parts.automation;

import appeng.api.config.FuzzyMode;
import appeng.api.config.RedstoneMode;
import appeng.api.config.Setting;
import appeng.api.config.Settings;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.api.stacks.AEKeyType;
import appeng.api.storage.ISubMenuHost;
import appeng.api.util.AECableType;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigManagerBuilder;
import appeng.core.AppEng;
import appeng.core.definitions.AEItems;
import appeng.core.settings.TickRates;
import appeng.helpers.IConfigInvHost;
import appeng.items.parts.PartModels;
import appeng.me.helpers.MachineSource;
import appeng.menu.ISubMenu;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocators;
import appeng.parts.PartModel;
import appeng.parts.automation.UpgradeablePart;
import appeng.util.ConfigInventory;
import appeng.util.Platform;
import appeng.util.prioritylist.IPartitionList;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public abstract class IOBusPart
extends UpgradeablePart
implements IGridTickable,
IConfigInvHost,
ISubMenuHost {
    public static final ResourceLocation MODEL_BASE = AppEng.makeId("part/import_bus_base");
    @PartModels
    public static final IPartModel MODELS_OFF = new PartModel(MODEL_BASE, AppEng.makeId("part/import_bus_off"));
    @PartModels
    public static final IPartModel MODELS_ON = new PartModel(MODEL_BASE, AppEng.makeId("part/import_bus_on"));
    @PartModels
    public static final IPartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE, AppEng.makeId("part/import_bus_has_channel"));
    private final ConfigInventory config;
    @Nullable
    private IPartitionList filter;
    private final TickRates tickRates;
    protected final IActionSource source;
    private boolean lastRedstone = false;
    private boolean pendingPulse = false;

    public IOBusPart(TickRates tickRates, Set<AEKeyType> supportedKeyTypes, IPartItem<?> partItem) {
        super(partItem);
        this.tickRates = tickRates;
        this.source = new MachineSource(this);
        this.config = ConfigInventory.configTypes(63).supportedTypes(supportedKeyTypes).changeListener(this::updateState).build();
        this.getMainNode().addService(IGridTickable.class, this);
    }

    @Override
    protected void registerSettings(IConfigManagerBuilder builder) {
        super.registerSettings(builder);
        builder.registerSetting(Settings.REDSTONE_CONTROLLED, RedstoneMode.IGNORE);
        builder.registerSetting(Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL);
    }

    @Override
    protected int getUpgradeSlots() {
        return 5;
    }

    @Override
    public RedstoneMode getRSMode() {
        return this.getConfigManager().getSetting(Settings.REDSTONE_CONTROLLED);
    }

    private boolean isInPulseMode() {
        return this.getRSMode() == RedstoneMode.SIGNAL_PULSE;
    }

    @Override
    public float getCableConnectionLength(AECableType cable) {
        return 5.0f;
    }

    protected abstract MenuType<?> getMenuType();

    @Override
    public void upgradesChanged() {
        this.updateState();
    }

    @Override
    public void readFromNBT(CompoundTag extra, HolderLookup.Provider registries) {
        super.readFromNBT(extra, registries);
        this.config.readFromChildTag(extra, "config", registries);
        this.filter = null;
        this.pendingPulse = this.isInPulseMode() && extra.getBoolean("pendingPulse");
    }

    @Override
    public void writeToNBT(CompoundTag extra, HolderLookup.Provider registries) {
        super.writeToNBT(extra, registries);
        this.config.writeToChildTag(extra, "config", registries);
        if (this.isInPulseMode() && this.pendingPulse) {
            extra.putBoolean("pendingPulse", true);
        }
    }

    @Override
    public ConfigInventory getConfig() {
        return this.config;
    }

    protected final IPartitionList getFilter() {
        if (this.filter == null) {
            IPartitionList.Builder filterBuilder = IPartitionList.builder();
            filterBuilder.addAll(this.getConfig().keySet());
            if (this.isUpgradedWith(AEItems.FUZZY_CARD)) {
                filterBuilder.fuzzyMode(this.getConfigManager().getSetting(Settings.FUZZY_MODE));
            }
            this.filter = filterBuilder.build();
        }
        return this.filter;
    }

    @Override
    public void onNeighborChanged(BlockGetter level, BlockPos pos, BlockPos neighbor) {
        if (this.isInPulseMode()) {
            boolean hostIsPowered = this.getHost().hasRedstone();
            if (this.lastRedstone != hostIsPowered) {
                this.lastRedstone = hostIsPowered;
                if (this.lastRedstone && !this.pendingPulse) {
                    this.pendingPulse = true;
                    this.getMainNode().ifPresent((grid, node) -> grid.getTickManager().alertDevice((IGridNode)node));
                }
            }
        } else {
            this.updateRedstoneState();
        }
    }

    protected int availableSlots() {
        return Math.min(18 + this.getInstalledUpgrades(AEItems.CAPACITY_CARD) * 9, this.getConfig().size());
    }

    protected int getOperationsPerTick() {
        return switch (this.getInstalledUpgrades(AEItems.SPEED_CARD)) {
            default -> 1;
            case 1 -> 8;
            case 2 -> 32;
            case 3 -> 64;
            case 4 -> 96;
        };
    }

    @Override
    public final TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        if (this.isSleeping()) {
            return TickRateModulation.SLEEP;
        }
        if (!this.canDoBusWork()) {
            return TickRateModulation.IDLE;
        }
        this.pendingPulse = false;
        boolean hasDoneWork = this.doBusWork(node.getGrid());
        if (this.isSleeping()) {
            return TickRateModulation.SLEEP;
        }
        return hasDoneWork ? TickRateModulation.FASTER : TickRateModulation.SLOWER;
    }

    protected final boolean canDoBusWork() {
        if (!this.getMainNode().isActive()) {
            return false;
        }
        BlockEntity self = this.getHost().getBlockEntity();
        BlockPos targetPos = self.getBlockPos().relative(this.getSide());
        return Platform.areBlockEntitiesTicking(self.getLevel(), targetPos);
    }

    private void updateState() {
        this.filter = null;
        this.updateRedstoneState();
    }

    @Override
    protected void onSettingChanged(IConfigManager manager, Setting<?> setting) {
        super.onSettingChanged(manager, setting);
        this.updateRedstoneState();
        if (this.isInPulseMode()) {
            this.lastRedstone = this.getHost().hasRedstone();
        }
    }

    private void updateRedstoneState() {
        if (!this.isInPulseMode()) {
            this.pendingPulse = false;
        }
        this.getMainNode().ifPresent((grid, node) -> {
            if (!this.isSleeping()) {
                grid.getTickManager().wakeDevice((IGridNode)node);
            } else {
                grid.getTickManager().sleepDevice((IGridNode)node);
            }
        });
    }

    @Override
    public final boolean onUseWithoutItem(Player player, Vec3 pos) {
        if (!this.isClientSide()) {
            MenuOpener.open(this.getMenuType(), player, MenuLocators.forPart(this));
        }
        return true;
    }

    @Override
    public final TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(this.tickRates.getMin(), this.tickRates.getMax(), this.isSleeping());
    }

    @Override
    protected boolean isSleeping() {
        if (this.isInPulseMode() && this.pendingPulse) {
            return false;
        }
        return super.isSleeping();
    }

    protected abstract boolean doBusWork(IGrid var1);

    @Override
    public void addToWorld() {
        super.addToWorld();
        this.lastRedstone = this.getHost().hasRedstone();
        if (this.pendingPulse) {
            this.getMainNode().ifPresent((grid, node) -> grid.getTickManager().alertDevice((IGridNode)node));
        }
    }

    @Override
    public void returnToMainMenu(Player player, ISubMenu subMenu) {
        MenuOpener.open(this.getMenuType(), player, subMenu.getLocator(), true);
    }

    @Override
    public ItemStack getMainMenuIcon() {
        return this.getPartItem().asItem().getDefaultInstance();
    }
}

