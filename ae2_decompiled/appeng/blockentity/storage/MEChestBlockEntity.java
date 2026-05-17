/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockState
 *  net.neoforged.neoforge.fluids.FluidStack
 *  net.neoforged.neoforge.fluids.IFluidTank
 *  net.neoforged.neoforge.fluids.capability.IFluidHandler
 *  net.neoforged.neoforge.fluids.capability.IFluidHandler$FluidAction
 *  org.jetbrains.annotations.Nullable
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package appeng.blockentity.storage;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.Settings;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.ViewItems;
import appeng.api.implementations.blockentities.IColorableBlockEntity;
import appeng.api.implementations.blockentities.IMEChest;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.events.GridPowerStorageStateChanged;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.storage.ILinkStatus;
import appeng.api.storage.IStorageMounts;
import appeng.api.storage.IStorageProvider;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.MEStorage;
import appeng.api.storage.StorageCells;
import appeng.api.storage.StorageHelper;
import appeng.api.storage.SupplierStorage;
import appeng.api.storage.cells.CellState;
import appeng.api.storage.cells.ICellHandler;
import appeng.api.storage.cells.StorageCell;
import appeng.api.util.AEColor;
import appeng.api.util.IConfigManager;
import appeng.api.util.KeyTypeSelection;
import appeng.api.util.KeyTypeSelectionHost;
import appeng.blockentity.ServerTickingBlockEntity;
import appeng.blockentity.grid.AENetworkedPoweredBlockEntity;
import appeng.core.definitions.AEBlocks;
import appeng.core.localization.GuiText;
import appeng.core.localization.PlayerMessages;
import appeng.helpers.IPriorityHost;
import appeng.me.helpers.MachineSource;
import appeng.me.storage.DelegatingMEInventory;
import appeng.menu.ISubMenu;
import appeng.menu.MenuOpener;
import appeng.menu.implementations.MEChestMenu;
import appeng.menu.locator.MenuLocators;
import appeng.menu.me.items.BasicCellChestMenu;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.CombinedInternalInventory;
import appeng.util.inv.filter.IAEItemFilter;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MEChestBlockEntity
extends AENetworkedPoweredBlockEntity
implements IMEChest,
ITerminalHost,
IPriorityHost,
IColorableBlockEntity,
ServerTickingBlockEntity,
IStorageProvider,
KeyTypeSelectionHost {
    private static final Logger LOG = LoggerFactory.getLogger(MEChestBlockEntity.class);
    private final AppEngInternalInventory inputInventory = new AppEngInternalInventory(this, 1);
    private final AppEngInternalInventory cellInventory = new AppEngInternalInventory(this, 1);
    private final InternalInventory internalInventory = new CombinedInternalInventory(this.inputInventory, this.cellInventory);
    private final IActionSource mySrc = new MachineSource(this);
    private final IConfigManager config = IConfigManager.builder(this::saveChanges).registerSetting(Settings.SORT_BY, SortOrder.NAME).registerSetting(Settings.VIEW_MODE, ViewItems.ALL).registerSetting(Settings.SORT_DIRECTION, SortDir.ASCENDING).build();
    private final KeyTypeSelection keyTypeSelection = new KeyTypeSelection(this::saveChanges, keyType -> true);
    private int priority = 0;
    private CellState clientCellState = CellState.ABSENT;
    private boolean clientPowered;
    private Item cellItem = Items.AIR;
    private boolean wasOnline = false;
    private AEColor paintedColor = AEColor.TRANSPARENT;
    private boolean isCached = false;
    private ChestMonitorHandler cellHandler;
    private IFluidHandler fluidHandler;
    private double idlePowerUsage;

    public MEChestBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
        this.setInternalMaxPower(PowerMultiplier.CONFIG.multiply(500.0));
        this.getMainNode().addService(IStorageProvider.class, this).setFlags(GridFlags.REQUIRE_CHANNEL);
        this.setInternalPublicPowerStorage(true);
        this.setInternalPowerFlow(AccessRestriction.WRITE);
        this.inputInventory.setFilter(new InputInventoryFilter());
        this.cellInventory.setFilter(new CellInventoryFilter());
    }

    public ItemStack getCell() {
        return this.cellInventory.getStackInSlot(0);
    }

    public void setCell(ItemStack stack) {
        this.cellInventory.setItemDirect(0, Objects.requireNonNull(stack));
    }

    @Override
    protected void emitPowerStateEvent(GridPowerStorageStateChanged.PowerEventType x) {
        if (x == GridPowerStorageStateChanged.PowerEventType.RECEIVE_POWER) {
            this.getMainNode().ifPresent(grid -> grid.postEvent(new GridPowerStorageStateChanged(this, GridPowerStorageStateChanged.PowerEventType.RECEIVE_POWER)));
        } else {
            this.recalculateDisplay();
        }
    }

    private void recalculateDisplay() {
        boolean powered;
        boolean changed = false;
        CellState cellState = this.getCellStatus(0);
        if (this.clientCellState != cellState) {
            this.clientCellState = cellState;
            changed = true;
        }
        if (this.clientPowered != (powered = this.isPowered())) {
            this.clientPowered = powered;
            changed = true;
        }
        if (changed) {
            this.markForUpdate();
        }
    }

    @Override
    public int getCellCount() {
        return 1;
    }

    private void updateHandler() {
        if (!this.isCached) {
            this.cellHandler = null;
            this.fluidHandler = null;
            ItemStack is = this.getCell();
            if (!is.isEmpty()) {
                this.isCached = true;
                StorageCell newCell = StorageCells.getCellInventory(is, this::onCellContentChanged);
                if (newCell != null) {
                    this.idlePowerUsage = 1.0 + newCell.getIdleDrain();
                    this.cellHandler = this.wrap(newCell);
                    this.getMainNode().setIdlePowerUsage(this.idlePowerUsage);
                    if (this.cellHandler != null) {
                        this.fluidHandler = new FluidHandler();
                    }
                }
            }
        }
    }

    private ChestMonitorHandler wrap(StorageCell cellInventory) {
        if (cellInventory == null) {
            return null;
        }
        return new ChestMonitorHandler(cellInventory);
    }

    @Override
    public ILinkStatus getLinkStatus() {
        this.updateHandler();
        if (this.cellHandler == null) {
            return ILinkStatus.ofDisconnected((Component)PlayerMessages.ChestCannotReadStorageCell.text());
        }
        if (!this.isPowered()) {
            return ILinkStatus.ofDisconnected((Component)GuiText.OutOfPower.text());
        }
        return ILinkStatus.ofConnected();
    }

    @Override
    public CellState getCellStatus(int slot) {
        if (this.isClientSide()) {
            return this.clientCellState;
        }
        this.updateHandler();
        ItemStack cell = this.getCell();
        ICellHandler ch = StorageCells.getHandler(cell);
        if (this.cellHandler != null && ch != null) {
            return this.cellHandler.cellInventory.getStatus();
        }
        return CellState.ABSENT;
    }

    @Override
    @Nullable
    public Item getCellItem(int slot) {
        if (slot != 0) {
            return null;
        }
        if (this.level == null || this.level.isClientSide) {
            return this.cellItem;
        }
        ItemStack cell = this.getCell();
        return cell.isEmpty() ? null : cell.getItem();
    }

    @Override
    @Nullable
    public MEStorage getCellInventory(int slot) {
        if (slot == 0 && this.cellHandler != null) {
            return this.cellHandler;
        }
        return null;
    }

    @Override
    @Nullable
    public StorageCell getOriginalCellInventory(int slot) {
        if (slot == 0 && this.cellHandler != null) {
            return this.cellHandler.cellInventory;
        }
        return null;
    }

    @Override
    public boolean isPowered() {
        if (this.isClientSide()) {
            return this.clientPowered;
        }
        if (this.getMainNode().isPowered()) {
            return true;
        }
        return this.getAECurrentPower() > 1.0;
    }

    @Override
    public boolean isCellBlinking(int slot) {
        return false;
    }

    @Override
    protected double extractAEPower(double amt, Actionable mode) {
        IEnergyService eg;
        double stash = 0.0;
        IGrid grid = this.getMainNode().getGrid();
        if (grid != null && (stash = (eg = grid.getEnergyService()).extractAEPower(amt, mode, PowerMultiplier.ONE)) >= amt) {
            return stash;
        }
        return super.extractAEPower(amt - stash, mode) + stash;
    }

    @Override
    public void serverTick() {
        IGrid grid = this.getMainNode().getGrid();
        if (grid == null || !grid.getEnergyService().isNetworkPowered()) {
            this.extractAEPower(this.idlePowerUsage, Actionable.MODULATE, PowerMultiplier.CONFIG);
            this.recalculateDisplay();
        }
        if (!this.inputInventory.isEmpty()) {
            this.tryToStoreContents();
        }
    }

    @Override
    protected void writeToStream(RegistryFriendlyByteBuf data) {
        super.writeToStream(data);
        this.clientCellState = this.getCellStatus(0);
        data.writeEnum((Enum)this.clientCellState);
        this.clientPowered = this.isPowered();
        data.writeBoolean(this.clientPowered);
        data.writeByte(this.paintedColor.ordinal());
        data.writeVarInt(Item.getId((Item)this.getCell().getItem()));
    }

    @Override
    protected boolean readFromStream(RegistryFriendlyByteBuf data) {
        boolean c = super.readFromStream(data);
        CellState oldCellState = this.clientCellState;
        boolean oldPowered = this.clientPowered;
        AEColor oldColor = this.paintedColor;
        Item oldCellItem = this.cellItem;
        this.clientCellState = (CellState)data.readEnum(CellState.class);
        this.clientPowered = data.readBoolean();
        this.paintedColor = (AEColor)data.readEnum(AEColor.class);
        this.cellItem = Item.byId((int)data.readVarInt());
        return c || oldCellState != this.clientCellState || oldPowered != this.clientPowered || oldColor != this.paintedColor || oldCellItem != this.cellItem;
    }

    @Override
    protected void saveVisualState(CompoundTag data) {
        super.saveVisualState(data);
        data.putBoolean("powered", this.isPowered());
        data.putString("cellStatus", this.getCellStatus(0).name());
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey((Object)this.getCell().getItem());
        data.putString("cellId", itemId.toString());
        data.putString("color", this.paintedColor.name());
    }

    @Override
    protected void loadVisualState(CompoundTag data) {
        super.loadVisualState(data);
        this.clientPowered = data.getBoolean("powered");
        try {
            this.clientCellState = CellState.valueOf(data.getString("cellStatus"));
        }
        catch (Exception e) {
            this.clientCellState = CellState.ABSENT;
            LOG.warn("Couldn't read cell status for {} from {}", (Object)this, (Object)data);
        }
        try {
            this.cellItem = (Item)BuiltInRegistries.ITEM.get(ResourceLocation.parse((String)data.getString("cellId")));
        }
        catch (Exception e) {
            LOG.warn("Couldn't read cell item for {} from {}", (Object)this, (Object)data);
            this.cellItem = Items.AIR;
        }
        try {
            this.paintedColor = AEColor.valueOf(data.getString("color"));
        }
        catch (IllegalArgumentException ignore) {
            LOG.warn("Invalid painted color in visual data for {}: {}", (Object)this, (Object)data);
            this.paintedColor = AEColor.TRANSPARENT;
        }
    }

    @Override
    public void loadTag(CompoundTag data, HolderLookup.Provider registries) {
        super.loadTag(data, registries);
        this.config.readFromNBT(data, registries);
        this.keyTypeSelection.readFromNBT(data, registries);
        this.priority = data.getInt("priority");
        if (data.contains("paintedColor")) {
            this.paintedColor = AEColor.values()[data.getByte("paintedColor")];
        }
    }

    @Override
    public void saveAdditional(CompoundTag data, HolderLookup.Provider registries) {
        super.saveAdditional(data, registries);
        this.config.writeToNBT(data, registries);
        this.keyTypeSelection.writeToNBT(data);
        data.putInt("priority", this.priority);
        data.putByte("paintedColor", (byte)this.paintedColor.ordinal());
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        boolean currentOnline = this.getMainNode().isOnline();
        if (this.wasOnline != currentOnline) {
            this.wasOnline = currentOnline;
            IStorageProvider.requestUpdate(this.getMainNode());
            this.recalculateDisplay();
        }
    }

    @Override
    public MEStorage getInventory() {
        return new SupplierStorage(() -> {
            this.updateHandler();
            return this.cellHandler;
        });
    }

    @Override
    public InternalInventory getInternalInventory() {
        return this.internalInventory;
    }

    @Override
    public void onChangeInventory(AppEngInternalInventory inv, int slot) {
        if (inv == this.cellInventory) {
            this.cellHandler = null;
            this.isCached = false;
            IStorageProvider.requestUpdate(this.getMainNode());
            if (this.level != null) {
                this.invalidateCapabilities();
                this.markForUpdate();
            }
        }
        if (inv == this.inputInventory && !inv.getStackInSlot(slot).isEmpty()) {
            this.tryToStoreContents();
        }
    }

    @Override
    protected InternalInventory getExposedInventoryForSide(Direction side) {
        if (side == this.getFront()) {
            return this.cellInventory;
        }
        return this.inputInventory;
    }

    private void tryToStoreContents() {
        if (!this.inputInventory.isEmpty()) {
            this.updateHandler();
            if (this.cellHandler != null) {
                ItemStack stack = this.inputInventory.getStackInSlot(0);
                if (stack.isEmpty()) {
                    return;
                }
                long inserted = StorageHelper.poweredInsert(this, this.cellHandler, AEItemKey.of(stack), stack.getCount(), this.mySrc);
                if (inserted >= (long)stack.getCount()) {
                    this.inputInventory.setItemDirect(0, ItemStack.EMPTY);
                } else {
                    stack.shrink((int)inserted);
                    this.inputInventory.setItemDirect(0, stack);
                }
            }
        }
    }

    @Override
    public void mountInventories(IStorageMounts storageMounts) {
        if (this.getMainNode().isOnline()) {
            this.updateHandler();
            if (this.cellHandler != null) {
                storageMounts.mount(this.cellHandler, this.priority);
            }
        }
    }

    @Override
    public int getPriority() {
        return this.priority;
    }

    @Override
    public void setPriority(int newValue) {
        this.priority = newValue;
        this.cellHandler = null;
        this.isCached = false;
        IStorageProvider.requestUpdate(this.getMainNode());
    }

    private void blinkCell(int slot) {
        this.recalculateDisplay();
    }

    @Override
    public IConfigManager getConfigManager() {
        return this.config;
    }

    @Override
    public KeyTypeSelection getKeyTypeSelection() {
        return this.keyTypeSelection;
    }

    public boolean openGui(Player p) {
        ICellHandler ch;
        this.updateHandler();
        if (this.cellHandler != null && (ch = StorageCells.getHandler(this.getCell())) != null) {
            MenuOpener.open(BasicCellChestMenu.TYPE, p, MenuLocators.forBlockEntity(this));
            return true;
        }
        return false;
    }

    @Override
    public AEColor getColor() {
        return this.paintedColor;
    }

    @Override
    public boolean recolourBlock(Direction side, AEColor newPaintedColor, Player who) {
        if (this.paintedColor == newPaintedColor) {
            return false;
        }
        this.paintedColor = newPaintedColor;
        this.saveChanges();
        this.markForUpdate();
        return true;
    }

    private void onCellContentChanged() {
        if (this.cellHandler != null) {
            this.cellHandler.cellInventory.persist();
        }
        this.level.blockEntityChanged(this.worldPosition);
    }

    public void openCellInventoryMenu(Player player) {
        MenuOpener.open(MEChestMenu.TYPE, player, MenuLocators.forBlockEntity(this));
    }

    @Nullable
    public IFluidHandler getFluidHandler(Direction side) {
        if (side != this.getFront()) {
            return this.fluidHandler;
        }
        return null;
    }

    @Nullable
    public MEStorage getMEStorage(Direction side) {
        if (side != this.getFront()) {
            return this.getInventory();
        }
        return null;
    }

    @Override
    public ItemStack getMainMenuIcon() {
        return AEBlocks.ME_CHEST.stack();
    }

    @Override
    public void returnToMainMenu(Player player, ISubMenu subMenu) {
        MenuOpener.returnTo(MEChestMenu.TYPE, player, MenuLocators.forBlockEntity(this));
    }

    private class InputInventoryFilter
    implements IAEItemFilter {
        private InputInventoryFilter() {
        }

        @Override
        public boolean allowExtract(InternalInventory inv, int slot, int amount) {
            return false;
        }

        @Override
        public boolean allowInsert(InternalInventory inv, int slot, ItemStack stack) {
            if (MEChestBlockEntity.this.isPowered()) {
                MEChestBlockEntity.this.updateHandler();
                if (MEChestBlockEntity.this.cellHandler == null) {
                    return false;
                }
                AEItemKey what = AEItemKey.of(stack);
                if (what == null) {
                    return false;
                }
                return MEChestBlockEntity.this.cellHandler.insert(what, stack.getCount(), Actionable.SIMULATE, MEChestBlockEntity.this.mySrc) > 0L;
            }
            return false;
        }
    }

    private static class CellInventoryFilter
    implements IAEItemFilter {
        private CellInventoryFilter() {
        }

        @Override
        public boolean allowExtract(InternalInventory inv, int slot, int amount) {
            return true;
        }

        @Override
        public boolean allowInsert(InternalInventory inv, int slot, ItemStack stack) {
            return StorageCells.getHandler(stack) != null;
        }
    }

    private class ChestMonitorHandler
    extends DelegatingMEInventory {
        private final StorageCell cellInventory;

        public ChestMonitorHandler(StorageCell cellInventory) {
            super(cellInventory);
            this.cellInventory = cellInventory;
        }

        @Override
        public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
            long inserted = super.insert(what, amount, mode, source);
            if (inserted > 0L && mode == Actionable.MODULATE) {
                MEChestBlockEntity.this.blinkCell(0);
            }
            return inserted;
        }

        @Override
        public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
            long extracted = super.extract(what, amount, mode, source);
            if (extracted > 0L && mode == Actionable.MODULATE) {
                MEChestBlockEntity.this.blinkCell(0);
            }
            return extracted;
        }
    }

    private class FluidHandler
    implements IFluidHandler,
    IFluidTank {
        private FluidHandler() {
        }

        private boolean canAcceptLiquids() {
            return MEChestBlockEntity.this.cellHandler != null;
        }

        public FluidStack getFluid() {
            return FluidStack.EMPTY;
        }

        public int getFluidAmount() {
            return 0;
        }

        public int getCapacity() {
            return this.canAcceptLiquids() ? 1000 : 0;
        }

        public boolean isFluidValid(FluidStack stack) {
            return this.canAcceptLiquids();
        }

        public int getTanks() {
            return 1;
        }

        public FluidStack getFluidInTank(int tank) {
            return FluidStack.EMPTY;
        }

        public int getTankCapacity(int tank) {
            return tank == 0 ? 1000 : 0;
        }

        public boolean isFluidValid(int tank, FluidStack stack) {
            return tank == 0;
        }

        public int fill(FluidStack resource, IFluidHandler.FluidAction action) {
            AEFluidKey what;
            MEChestBlockEntity.this.updateHandler();
            if (this.canAcceptLiquids() && (what = AEFluidKey.of(resource)) != null) {
                return (int)StorageHelper.poweredInsert(MEChestBlockEntity.this, MEChestBlockEntity.this.cellHandler, what, resource.getAmount(), MEChestBlockEntity.this.mySrc, Actionable.of(action));
            }
            return 0;
        }

        public FluidStack drain(FluidStack resource, IFluidHandler.FluidAction action) {
            return FluidStack.EMPTY;
        }

        public FluidStack drain(int maxDrain, IFluidHandler.FluidAction action) {
            return FluidStack.EMPTY;
        }
    }
}

