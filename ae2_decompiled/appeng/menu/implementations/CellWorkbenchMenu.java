/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Iterators
 *  it.unimi.dsi.fastutil.shorts.ShortSet
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.inventory.MenuType
 *  net.minecraft.world.item.ItemStack
 *  org.jetbrains.annotations.NotNull
 */
package appeng.menu.implementations;

import appeng.api.config.CopyMode;
import appeng.api.config.FuzzyMode;
import appeng.api.config.Settings;
import appeng.api.inventories.ISegmentedInventory;
import appeng.api.inventories.InternalInventory;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.StorageCells;
import appeng.api.storage.cells.ICellWorkbenchItem;
import appeng.api.storage.cells.StorageCell;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.util.IConfigManager;
import appeng.blockentity.misc.CellWorkbenchBlockEntity;
import appeng.helpers.externalstorage.GenericStackInv;
import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.implementations.UpgradeableMenu;
import appeng.menu.slot.CellPartitionSlot;
import appeng.menu.slot.IPartitionSlotHost;
import appeng.menu.slot.OptionalRestrictedInputSlot;
import appeng.menu.slot.RestrictedInputSlot;
import appeng.util.ConfigMenuInventory;
import appeng.util.EnumCycler;
import appeng.util.inv.SupplierInternalInventory;
import com.google.common.collect.Iterators;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CellWorkbenchMenu
extends UpgradeableMenu<CellWorkbenchBlockEntity>
implements IPartitionSlotHost {
    public static final String ACTION_NEXT_COPYMODE = "nextCopyMode";
    public static final String ACTION_PARTITION = "partition";
    public static final String ACTION_CLEAR = "clear";
    public static final String ACTION_SET_FUZZY_MODE = "setFuzzyMode";
    public static final MenuType<CellWorkbenchMenu> TYPE = MenuTypeBuilder.create(CellWorkbenchMenu::new, CellWorkbenchBlockEntity.class).build("cellworkbench");
    @GuiSync(value=2)
    public CopyMode copyMode = CopyMode.CLEAR_ON_REMOVE;

    public CellWorkbenchMenu(int id, Inventory ip, CellWorkbenchBlockEntity te) {
        super((MenuType<?>)TYPE, id, ip, te);
        this.registerClientAction(ACTION_NEXT_COPYMODE, this::nextWorkBenchCopyMode);
        this.registerClientAction(ACTION_PARTITION, this::partition);
        this.registerClientAction(ACTION_CLEAR, this::clear);
        this.registerClientAction(ACTION_SET_FUZZY_MODE, FuzzyMode.class, this::setCellFuzzyMode);
    }

    public void setCellFuzzyMode(FuzzyMode fuzzyMode) {
        if (this.isClientSide()) {
            this.sendClientAction(ACTION_SET_FUZZY_MODE, fuzzyMode);
            return;
        }
        ICellWorkbenchItem cwi = ((CellWorkbenchBlockEntity)this.getHost()).getCell();
        if (cwi != null) {
            cwi.setFuzzyMode(this.getWorkbenchItem(), fuzzyMode);
        }
    }

    public void nextWorkBenchCopyMode() {
        if (this.isClientSide()) {
            this.sendClientAction(ACTION_NEXT_COPYMODE);
        } else {
            ((CellWorkbenchBlockEntity)this.getHost()).getConfigManager().putSetting(Settings.COPY_MODE, EnumCycler.next(this.getWorkBenchCopyMode()));
        }
    }

    private CopyMode getWorkBenchCopyMode() {
        return ((CellWorkbenchBlockEntity)this.getHost()).getConfigManager().getSetting(Settings.COPY_MODE);
    }

    @Override
    protected void setupInventorySlots() {
        InternalInventory cell = ((CellWorkbenchBlockEntity)this.getHost()).getSubInventory(ISegmentedInventory.CELLS);
        this.addSlot(new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.WORKBENCH_CELL, cell, 0), SlotSemantics.STORAGE_CELL);
    }

    @Override
    protected void setupConfig() {
        ConfigMenuInventory inv = this.getConfigInventory().createMenuWrapper();
        for (int slot = 0; slot < 63; ++slot) {
            this.addSlot(new CellPartitionSlot(inv, this, slot), SlotSemantics.CONFIG);
        }
    }

    @Override
    protected void setupUpgrades() {
        SupplierInternalInventory<IUpgradeInventory> upgradeInventory = new SupplierInternalInventory<IUpgradeInventory>(this::getUpgrades);
        for (int i = 0; i < 8; ++i) {
            OptionalRestrictedInputSlot slot = new OptionalRestrictedInputSlot(RestrictedInputSlot.PlacableItemType.UPGRADES, upgradeInventory, this, i, i, this.getPlayerInventory());
            this.addSlot(slot, SlotSemantics.UPGRADE);
        }
    }

    public ItemStack getWorkbenchItem() {
        InternalInventory cells = Objects.requireNonNull(((CellWorkbenchBlockEntity)this.getHost()).getSubInventory(ISegmentedInventory.CELLS));
        return cells.getStackInSlot(0);
    }

    @Override
    protected void loadSettingsFromHost(IConfigManager cm) {
        this.setCopyMode(this.getWorkBenchCopyMode());
        this.setFuzzyMode(this.getWorkBenchFuzzyMode());
    }

    @Override
    public boolean isSlotEnabled(int idx) {
        return idx < this.getUpgrades().size();
    }

    @Override
    public boolean isPartitionSlotEnabled(int idx) {
        ICellWorkbenchItem cwi = ((CellWorkbenchBlockEntity)this.getHost()).getCell();
        if (cwi != null && this.getCopyMode() == CopyMode.CLEAR_ON_REMOVE) {
            return idx < cwi.getConfigInventory(this.getWorkbenchItem()).size();
        }
        return this.getCopyMode() == CopyMode.KEEP_ON_REMOVE;
    }

    @Override
    public void onServerDataSync(ShortSet updatedFields) {
        super.onServerDataSync(updatedFields);
        ((CellWorkbenchBlockEntity)this.getHost()).getConfigManager().putSetting(Settings.COPY_MODE, this.getCopyMode());
    }

    public void clear() {
        if (this.isClientSide()) {
            this.sendClientAction(ACTION_CLEAR);
        } else {
            this.getConfigInventory().clear();
            this.broadcastChanges();
        }
    }

    private FuzzyMode getWorkBenchFuzzyMode() {
        ICellWorkbenchItem cwi = ((CellWorkbenchBlockEntity)this.getHost()).getCell();
        if (cwi != null) {
            return cwi.getFuzzyMode(this.getWorkbenchItem());
        }
        return FuzzyMode.IGNORE_ALL;
    }

    public void partition() {
        if (this.isClientSide()) {
            this.sendClientAction(ACTION_PARTITION);
            return;
        }
        GenericStackInv inv = this.getConfigInventory();
        ItemStack is = this.getWorkbenchItem();
        Iterator<? extends AEKey> it = this.iterateCellStacks(is);
        for (int x = 0; x < inv.size(); ++x) {
            if (it.hasNext()) {
                inv.setStack(x, new GenericStack(it.next(), 0L));
                continue;
            }
            inv.setStack(x, null);
        }
        this.broadcastChanges();
    }

    private GenericStackInv getConfigInventory() {
        return Objects.requireNonNull(((CellWorkbenchBlockEntity)this.getHost()).getConfig());
    }

    @NotNull
    private Iterator<? extends AEKey> iterateCellStacks(ItemStack is) {
        StorageCell cellInv = StorageCells.getCellInventory(is, null);
        Iterator i = cellInv != null ? Iterators.transform(cellInv.getAvailableStacks().iterator(), Map.Entry::getKey) : Collections.emptyIterator();
        return i;
    }

    public CopyMode getCopyMode() {
        return this.copyMode;
    }

    private void setCopyMode(CopyMode copyMode) {
        this.copyMode = copyMode;
    }
}

