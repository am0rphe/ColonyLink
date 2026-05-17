/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Object2LongMap$Entry
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockState
 *  org.jetbrains.annotations.Nullable
 */
package appeng.blockentity.storage;

import appeng.api.config.Actionable;
import appeng.api.config.FullnessMode;
import appeng.api.config.OperationMode;
import appeng.api.config.RedstoneMode;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.inventories.ISegmentedInventory;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import appeng.api.storage.StorageCells;
import appeng.api.storage.StorageHelper;
import appeng.api.storage.cells.CellState;
import appeng.api.storage.cells.StorageCell;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.upgrades.UpgradeInventories;
import appeng.api.util.AECableType;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.blockentity.grid.AENetworkedInvBlockEntity;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.settings.TickRates;
import appeng.me.helpers.MachineSource;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.CombinedInternalInventory;
import appeng.util.inv.FilteredInternalInventory;
import appeng.util.inv.filter.AEItemFilters;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class IOPortBlockEntity
extends AENetworkedInvBlockEntity
implements IUpgradeableObject,
IConfigurableObject,
IGridTickable {
    private static final int NUMBER_OF_CELL_SLOTS = 6;
    private static final int NUMBER_OF_UPGRADE_SLOTS = 3;
    private final IConfigManager manager;
    private final AppEngInternalInventory inputCells = new AppEngInternalInventory(this, 6);
    private final AppEngInternalInventory outputCells = new AppEngInternalInventory(this, 6);
    private final InternalInventory combinedInventory = new CombinedInternalInventory(this.inputCells, this.outputCells);
    private final InternalInventory inputCellsExt = new FilteredInternalInventory(this.inputCells, AEItemFilters.INSERT_ONLY);
    private final InternalInventory outputCellsExt = new FilteredInternalInventory(this.outputCells, AEItemFilters.EXTRACT_ONLY);
    private final IUpgradeInventory upgrades;
    private final IActionSource mySrc;
    private YesNo lastRedstoneState;
    private boolean isActive = false;

    public IOPortBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
        this.getMainNode().setFlags(GridFlags.REQUIRE_CHANNEL).addService(IGridTickable.class, this);
        this.manager = IConfigManager.builder(this::updateTask).registerSetting(Settings.REDSTONE_CONTROLLED, RedstoneMode.IGNORE).registerSetting(Settings.FULLNESS_MODE, FullnessMode.EMPTY).registerSetting(Settings.OPERATION_MODE, OperationMode.EMPTY).build();
        this.mySrc = new MachineSource(this);
        this.lastRedstoneState = YesNo.UNDECIDED;
        this.upgrades = UpgradeInventories.forMachine(AEBlocks.IO_PORT, 3, this::saveChanges);
    }

    @Override
    public void saveAdditional(CompoundTag data, HolderLookup.Provider registries) {
        super.saveAdditional(data, registries);
        this.manager.writeToNBT(data, registries);
        this.upgrades.writeToNBT(data, "upgrades", registries);
        data.putInt("lastRedstoneState", this.lastRedstoneState.ordinal());
    }

    @Override
    public void loadTag(CompoundTag data, HolderLookup.Provider registries) {
        super.loadTag(data, registries);
        this.manager.readFromNBT(data, registries);
        this.upgrades.readFromNBT(data, "upgrades", registries);
        if (data.contains("lastRedstoneState")) {
            this.lastRedstoneState = YesNo.values()[data.getInt("lastRedstoneState")];
        }
    }

    @Override
    protected void writeToStream(RegistryFriendlyByteBuf data) {
        super.writeToStream(data);
        data.writeBoolean(this.isActive());
    }

    @Override
    protected boolean readFromStream(RegistryFriendlyByteBuf data) {
        boolean ret = super.readFromStream(data);
        boolean isActive = data.readBoolean();
        ret = isActive != this.isActive || ret;
        this.isActive = isActive;
        return ret;
    }

    @Override
    public AECableType getCableConnectionType(Direction dir) {
        return AECableType.SMART;
    }

    private void updateTask() {
        this.getMainNode().ifPresent((grid, node) -> {
            if (this.hasWork()) {
                grid.getTickManager().wakeDevice((IGridNode)node);
            } else {
                grid.getTickManager().sleepDevice((IGridNode)node);
            }
        });
    }

    public void updateRedstoneState() {
        YesNo currentState;
        YesNo yesNo = currentState = this.level.getBestNeighborSignal(this.worldPosition) != 0 ? YesNo.YES : YesNo.NO;
        if (this.lastRedstoneState != currentState) {
            this.lastRedstoneState = currentState;
            this.updateTask();
        }
    }

    private boolean getRedstoneState() {
        if (this.lastRedstoneState == YesNo.UNDECIDED) {
            this.updateRedstoneState();
        }
        return this.lastRedstoneState == YesNo.YES;
    }

    private boolean isEnabled() {
        if (!this.upgrades.isInstalled(AEItems.REDSTONE_CARD)) {
            return true;
        }
        RedstoneMode rs = this.manager.getSetting(Settings.REDSTONE_CONTROLLED);
        if (rs == RedstoneMode.HIGH_SIGNAL) {
            return this.getRedstoneState();
        }
        return !this.getRedstoneState();
    }

    public boolean isActive() {
        if (this.level != null && !this.level.isClientSide) {
            return this.getMainNode().isOnline();
        }
        return this.isActive;
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        if (reason != IGridNodeListener.State.GRID_BOOT) {
            this.markForUpdate();
        }
    }

    @Override
    public IConfigManager getConfigManager() {
        return this.manager;
    }

    @Override
    public IUpgradeInventory getUpgrades() {
        return this.upgrades;
    }

    @Override
    @Nullable
    public InternalInventory getSubInventory(ResourceLocation id) {
        if (id.equals((Object)ISegmentedInventory.UPGRADES)) {
            return this.upgrades;
        }
        if (id.equals((Object)ISegmentedInventory.CELLS)) {
            return this.combinedInventory;
        }
        return super.getSubInventory(id);
    }

    private boolean hasWork() {
        if (this.isEnabled()) {
            return !this.inputCells.isEmpty();
        }
        return false;
    }

    @Override
    public InternalInventory getInternalInventory() {
        return this.combinedInventory;
    }

    @Override
    public void onChangeInventory(AppEngInternalInventory inv, int slot) {
        if (this.inputCells == inv) {
            this.updateTask();
        }
    }

    @Override
    protected InternalInventory getExposedInventoryForSide(Direction facing) {
        if (facing == this.getTop() || facing == this.getTop().getOpposite()) {
            return this.inputCellsExt;
        }
        return this.outputCellsExt;
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(TickRates.IOPort, !this.hasWork());
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        if (!this.getMainNode().isActive()) {
            return TickRateModulation.IDLE;
        }
        TickRateModulation ret = TickRateModulation.SLEEP;
        long itemsToMove = 256L;
        switch (this.upgrades.getInstalledUpgrades(AEItems.SPEED_CARD)) {
            case 1: {
                itemsToMove *= 2L;
                break;
            }
            case 2: {
                itemsToMove *= 4L;
                break;
            }
            case 3: {
                itemsToMove *= 8L;
            }
        }
        IGrid grid = this.getMainNode().getGrid();
        if (grid == null) {
            return TickRateModulation.IDLE;
        }
        for (int x = 0; x < 6; ++x) {
            ItemStack cell = this.inputCells.getStackInSlot(x);
            StorageCell cellInv = StorageCells.getCellInventory(cell, null);
            if (cellInv == null) {
                this.moveSlot(x);
                continue;
            }
            if (itemsToMove > 0L) {
                ret = (itemsToMove = this.transferContents(grid, cellInv, itemsToMove)) > 0L ? TickRateModulation.IDLE : TickRateModulation.URGENT;
            }
            if (itemsToMove <= 0L || !this.matchesFullnessMode(cellInv) || !this.moveSlot(x)) continue;
            ret = TickRateModulation.URGENT;
        }
        return ret;
    }

    public boolean matchesFullnessMode(StorageCell inv) {
        return switch (this.manager.getSetting(Settings.FULLNESS_MODE)) {
            default -> throw new MatchException(null, null);
            case FullnessMode.HALF -> true;
            case FullnessMode.EMPTY -> {
                if (inv.getStatus() == CellState.EMPTY) {
                    yield true;
                }
                yield false;
            }
            case FullnessMode.FULL -> inv.getStatus() == CellState.FULL;
        };
    }

    private long transferContents(IGrid grid, StorageCell cellInv, long itemsToMove) {
        boolean didStuff;
        MEStorage destination;
        KeyCounter srcList;
        MEStorage src;
        MEStorage networkInv = grid.getStorageService().getInventory();
        if (this.manager.getSetting(Settings.OPERATION_MODE) == OperationMode.EMPTY) {
            src = cellInv;
            srcList = cellInv.getAvailableStacks();
            destination = networkInv;
        } else {
            src = networkInv;
            srcList = grid.getStorageService().getCachedInventory();
            destination = cellInv;
        }
        IEnergyService energy = grid.getEnergyService();
        block0: do {
            didStuff = false;
            for (Object2LongMap.Entry<AEKey> srcEntry : srcList) {
                AEKey what;
                long possible;
                long totalStackSize = srcEntry.getLongValue();
                if (totalStackSize <= 0L || (possible = destination.insert(what = (AEKey)srcEntry.getKey(), totalStackSize, Actionable.SIMULATE, this.mySrc)) <= 0L) continue;
                possible = Math.min(possible, itemsToMove * (long)what.getAmountPerOperation());
                if ((possible = src.extract(what, possible, Actionable.MODULATE, this.mySrc)) <= 0L) continue;
                long inserted = StorageHelper.poweredInsert(energy, destination, what, possible, this.mySrc);
                if (inserted < possible) {
                    src.insert(what, possible - inserted, Actionable.MODULATE, this.mySrc);
                }
                if (inserted <= 0L) continue block0;
                itemsToMove -= Math.max(1L, inserted / (long)what.getAmountPerOperation());
                didStuff = true;
                continue block0;
            }
        } while (itemsToMove > 0L && didStuff);
        return itemsToMove;
    }

    private boolean moveSlot(int x) {
        if (this.outputCells.addItems(this.inputCells.getStackInSlot(x)).isEmpty()) {
            this.inputCells.setItemDirect(x, ItemStack.EMPTY);
            return true;
        }
        return false;
    }

    @Override
    public void addAdditionalDrops(Level level, BlockPos pos, List<ItemStack> drops) {
        super.addAdditionalDrops(level, pos, drops);
        for (ItemStack upgrade : this.upgrades) {
            drops.add(upgrade);
        }
    }

    @Override
    public void clearContent() {
        super.clearContent();
        this.upgrades.clear();
    }
}

