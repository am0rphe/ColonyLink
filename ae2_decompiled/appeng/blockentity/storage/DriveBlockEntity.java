/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockState
 *  net.neoforged.neoforge.client.model.data.ModelData
 *  org.jetbrains.annotations.Nullable
 */
package appeng.blockentity.storage;

import appeng.api.implementations.blockentities.IChestOrDrive;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNodeListener;
import appeng.api.orientation.BlockOrientation;
import appeng.api.orientation.RelativeSide;
import appeng.api.storage.IStorageMounts;
import appeng.api.storage.IStorageProvider;
import appeng.api.storage.MEStorage;
import appeng.api.storage.StorageCells;
import appeng.api.storage.cells.CellState;
import appeng.api.storage.cells.StorageCell;
import appeng.api.util.AECableType;
import appeng.blockentity.grid.AENetworkedInvBlockEntity;
import appeng.blockentity.inventory.AppEngCellInventory;
import appeng.client.render.model.DriveModelData;
import appeng.core.AELog;
import appeng.core.definitions.AEBlocks;
import appeng.helpers.IPriorityHost;
import appeng.me.storage.DriveWatcher;
import appeng.menu.ISubMenu;
import appeng.menu.MenuOpener;
import appeng.menu.implementations.DriveMenu;
import appeng.menu.locator.MenuLocators;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.filter.IAEItemFilter;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

public class DriveBlockEntity
extends AENetworkedInvBlockEntity
implements IChestOrDrive,
IPriorityHost,
IStorageProvider {
    private final AppEngCellInventory inv = new AppEngCellInventory(this, this.getCellCount());
    private final DriveWatcher[] invBySlot = new DriveWatcher[this.getCellCount()];
    private boolean isCached = false;
    private int priority = 0;
    private boolean wasOnline = false;
    private final Item[] clientSideCellItems = new Item[this.getCellCount()];
    private final CellState[] clientSideCellState = new CellState[this.getCellCount()];
    private boolean clientSideOnline;

    public DriveBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
        this.getMainNode().addService(IStorageProvider.class, this).setFlags(GridFlags.REQUIRE_CHANNEL);
        this.inv.setFilter(new CellValidInventoryFilter());
        Arrays.fill((Object[])this.clientSideCellState, (Object)CellState.ABSENT);
    }

    @Override
    public Set<Direction> getGridConnectableSides(BlockOrientation orientation) {
        return EnumSet.complementOf(EnumSet.of(orientation.getSide(RelativeSide.FRONT)));
    }

    @Override
    protected void writeToStream(RegistryFriendlyByteBuf data) {
        int i;
        super.writeToStream(data);
        this.updateClientSideState();
        int packedState = 0;
        for (i = 0; i < this.getCellCount(); ++i) {
            packedState |= this.clientSideCellState[i].ordinal() << i * 3;
        }
        if (this.clientSideOnline) {
            packedState |= Integer.MIN_VALUE;
        }
        data.writeInt(packedState);
        for (i = 0; i < this.getCellCount(); ++i) {
            data.writeVarInt(BuiltInRegistries.ITEM.getId((Object)this.getCellItem(i)));
        }
    }

    @Override
    protected void saveVisualState(CompoundTag data) {
        super.saveVisualState(data);
        data.putBoolean("online", this.isPowered());
        for (int i = 0; i < this.getCellCount(); ++i) {
            Item cellItem = this.getCellItem(i);
            if (cellItem == null) continue;
            CompoundTag cellData = new CompoundTag();
            cellData.putString("id", BuiltInRegistries.ITEM.getKey((Object)cellItem).toString());
            CellState cellState = this.getCellStatus(i);
            cellData.putString("state", cellState.name().toLowerCase(Locale.ROOT));
            data.put("cell" + i, (Tag)cellData);
        }
    }

    @Override
    protected boolean readFromStream(RegistryFriendlyByteBuf data) {
        boolean online;
        boolean changed = super.readFromStream(data);
        int packedState = data.readInt();
        for (int i = 0; i < this.getCellCount(); ++i) {
            int cellStateOrdinal = packedState >> i * 3 & 7;
            CellState cellState = CellState.values()[cellStateOrdinal];
            if (this.clientSideCellState[i] == cellState) continue;
            this.clientSideCellState[i] = cellState;
            changed = true;
        }
        boolean bl = online = (packedState & Integer.MIN_VALUE) != 0;
        if (this.clientSideOnline != online) {
            this.clientSideOnline = online;
            changed = true;
        }
        for (int i = 0; i < this.getCellCount(); ++i) {
            Item item;
            int itemId = data.readVarInt();
            Item item2 = item = itemId == 0 ? null : (Item)BuiltInRegistries.ITEM.byId(itemId);
            if (itemId != 0 && item == Items.AIR) {
                AELog.warn("Received unknown item id from server for disk drive %s: %d", this, itemId);
            }
            if (this.clientSideCellItems[i] == item) continue;
            this.clientSideCellItems[i] = item;
            changed = true;
        }
        return changed;
    }

    @Override
    protected void loadVisualState(CompoundTag data) {
        super.loadVisualState(data);
        this.clientSideOnline = data.getBoolean("online");
        for (int i = 0; i < this.getCellCount(); ++i) {
            this.clientSideCellItems[i] = null;
            this.clientSideCellState[i] = CellState.ABSENT;
            String tagName = "cell" + i;
            if (!data.contains(tagName, 10)) continue;
            CompoundTag cellData = data.getCompound(tagName);
            ResourceLocation id = ResourceLocation.parse((String)cellData.getString("id"));
            String cellStateName = cellData.getString("state");
            this.clientSideCellItems[i] = BuiltInRegistries.ITEM.getOptional(id).orElse(null);
            try {
                this.clientSideCellState[i] = CellState.valueOf(cellStateName.toUpperCase(Locale.ROOT));
                continue;
            }
            catch (IllegalArgumentException e) {
                AELog.warn("Cannot parse cell state for cell %d: %s", i, cellStateName);
            }
        }
    }

    @Override
    public int getCellCount() {
        return 10;
    }

    @Override
    @Nullable
    public Item getCellItem(int slot) {
        if (this.level == null || this.level.isClientSide) {
            return this.clientSideCellItems[slot];
        }
        ItemStack stackInSlot = this.inv.getStackInSlot(slot);
        if (!stackInSlot.isEmpty()) {
            return stackInSlot.getItem();
        }
        return null;
    }

    @Override
    public CellState getCellStatus(int slot) {
        if (this.isClientSide()) {
            return this.clientSideCellState[slot];
        }
        DriveWatcher handler = this.invBySlot[slot];
        if (handler == null) {
            return CellState.ABSENT;
        }
        return handler.getStatus();
    }

    @Override
    @Nullable
    public MEStorage getCellInventory(int slot) {
        return this.invBySlot[slot];
    }

    @Override
    @Nullable
    public StorageCell getOriginalCellInventory(int slot) {
        DriveWatcher handler = this.invBySlot[slot];
        if (handler != null) {
            return handler.getCell();
        }
        return null;
    }

    @Override
    public boolean isPowered() {
        if (this.isClientSide()) {
            return this.clientSideOnline;
        }
        return this.getMainNode().isOnline();
    }

    @Override
    public boolean isCellBlinking(int slot) {
        return false;
    }

    @Override
    public void loadTag(CompoundTag data, HolderLookup.Provider registries) {
        super.loadTag(data, registries);
        this.isCached = false;
        this.priority = data.getInt("priority");
    }

    @Override
    public void saveAdditional(CompoundTag data, HolderLookup.Provider registries) {
        super.saveAdditional(data, registries);
        data.putInt("priority", this.priority);
    }

    private void updateVisualStateIfNeeded() {
        if (this.updateClientSideState()) {
            this.markForUpdate();
        }
    }

    private boolean updateClientSideState() {
        if (this.isClientSide()) {
            return false;
        }
        this.updateState();
        boolean changed = false;
        boolean online = this.getMainNode().isOnline();
        if (online != this.clientSideOnline) {
            this.clientSideOnline = online;
            changed = true;
        }
        for (int x = 0; x < this.getCellCount(); ++x) {
            CellState cellState;
            Item cellItem = this.getCellItem(x);
            if (cellItem != this.clientSideCellItems[x]) {
                this.clientSideCellItems[x] = cellItem;
                changed = true;
            }
            if ((cellState = this.getCellStatus(x)) == this.clientSideCellState[x]) continue;
            this.clientSideCellState[x] = cellState;
            changed = true;
        }
        return changed;
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        boolean currentOnline = this.getMainNode().isOnline();
        if (this.wasOnline != currentOnline) {
            this.wasOnline = currentOnline;
            IStorageProvider.requestUpdate(this.getMainNode());
            this.updateVisualStateIfNeeded();
        }
    }

    @Override
    public AECableType getCableConnectionType(Direction dir) {
        return AECableType.SMART;
    }

    @Override
    public InternalInventory getInternalInventory() {
        return this.inv;
    }

    @Override
    public void onChangeInventory(AppEngInternalInventory inv, int slot) {
        if (this.isCached) {
            this.isCached = false;
            this.updateState();
        }
        IStorageProvider.requestUpdate(this.getMainNode());
        this.markForUpdate();
    }

    private void updateState() {
        if (!this.isCached) {
            double power = 2.0;
            for (int slot = 0; slot < this.inv.size(); ++slot) {
                power += this.updateStateForSlot(slot);
            }
            this.getMainNode().setIdlePowerUsage(power);
            this.isCached = true;
        }
    }

    private double updateStateForSlot(int slot) {
        StorageCell cell;
        this.invBySlot[slot] = null;
        this.inv.setHandler(slot, null);
        ItemStack is = this.inv.getStackInSlot(slot);
        if (!is.isEmpty() && (cell = StorageCells.getCellInventory(is, this::onCellContentChanged)) != null) {
            DriveWatcher driveWatcher;
            this.inv.setHandler(slot, cell);
            this.invBySlot[slot] = driveWatcher = new DriveWatcher(cell, () -> this.blinkCell(slot));
            return cell.getIdleDrain();
        }
        return 0.0;
    }

    @Override
    public void onReady() {
        super.onReady();
        this.updateState();
    }

    @Override
    public void mountInventories(IStorageMounts storageMounts) {
        if (this.getMainNode().isOnline()) {
            this.updateState();
            for (DriveWatcher inventory : this.invBySlot) {
                if (inventory == null) continue;
                storageMounts.mount(inventory, this.priority);
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
        this.saveChanges();
        this.isCached = false;
        this.updateState();
        IStorageProvider.requestUpdate(this.getMainNode());
    }

    private void blinkCell(int slot) {
        this.updateVisualStateIfNeeded();
    }

    private void onCellContentChanged() {
        this.level.blockEntityChanged(this.worldPosition);
    }

    @Override
    public ModelData getModelData() {
        Item[] cells = new Item[this.getCellCount()];
        for (int i = 0; i < this.getCellCount(); ++i) {
            cells[i] = this.getCellItem(i);
        }
        return DriveModelData.create(cells);
    }

    public void openMenu(Player player) {
        MenuOpener.open(DriveMenu.TYPE, player, MenuLocators.forBlockEntity(this));
    }

    @Override
    public void returnToMainMenu(Player player, ISubMenu subMenu) {
        MenuOpener.returnTo(DriveMenu.TYPE, player, MenuLocators.forBlockEntity(this));
    }

    @Override
    public ItemStack getMainMenuIcon() {
        return AEBlocks.DRIVE.stack();
    }

    private static class CellValidInventoryFilter
    implements IAEItemFilter {
        private CellValidInventoryFilter() {
        }

        @Override
        public boolean allowExtract(InternalInventory inv, int slot, int amount) {
            return true;
        }

        @Override
        public boolean allowInsert(InternalInventory inv, int slot, ItemStack stack) {
            return !stack.isEmpty() && StorageCells.isCellHandled(stack);
        }
    }
}

