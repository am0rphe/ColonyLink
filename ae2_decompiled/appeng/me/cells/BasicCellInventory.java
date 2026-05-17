/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Object2LongMap
 *  it.unimi.dsi.fastutil.objects.Object2LongMap$Entry
 *  it.unimi.dsi.fastutil.objects.Object2LongMaps
 *  it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  org.jetbrains.annotations.Nullable
 */
package appeng.me.cells;

import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.config.IncludeExclude;
import appeng.api.ids.AEComponents;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.StorageCells;
import appeng.api.storage.cells.CellState;
import appeng.api.storage.cells.IBasicCellItem;
import appeng.api.storage.cells.ISaveProvider;
import appeng.api.storage.cells.StorageCell;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.core.definitions.AEItems;
import appeng.util.ConfigInventory;
import appeng.util.prioritylist.FuzzyPriorityList;
import appeng.util.prioritylist.IPartitionList;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMaps;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class BasicCellInventory
implements StorageCell {
    private static final int MAX_ITEM_TYPES = 63;
    @Nullable
    private final ISaveProvider container;
    private final AEKeyType keyType;
    private final IPartitionList partitionList;
    private final IncludeExclude partitionListMode;
    private int maxItemTypes;
    private int storedItems;
    private long storedItemCount;
    private Object2LongMap<AEKey> storedAmounts;
    private final ItemStack i;
    private final IBasicCellItem cellType;
    private final long maxItemsPerType;
    private final boolean hasVoidUpgrade;
    private boolean isPersisted = true;

    private BasicCellInventory(IBasicCellItem cellType, ItemStack o, @Nullable ISaveProvider container) {
        this.i = o;
        this.cellType = cellType;
        this.maxItemTypes = this.cellType.getTotalTypes(this.i);
        if (this.maxItemTypes > 63) {
            this.maxItemTypes = 63;
        }
        if (this.maxItemTypes < 1) {
            this.maxItemTypes = 1;
        }
        this.container = container;
        List<GenericStack> storedStacks = this.getStoredStacks();
        this.storedItems = storedStacks.size();
        this.storedItemCount = storedStacks.stream().mapToLong(GenericStack::amount).sum();
        this.storedAmounts = null;
        this.keyType = cellType.getKeyType();
        IPartitionList.Builder builder = IPartitionList.builder();
        IUpgradeInventory upgrades = this.getUpgradesInventory();
        ConfigInventory config = this.getConfigInventory();
        boolean hasInverter = upgrades.isInstalled(AEItems.INVERTER_CARD);
        boolean isFuzzy = upgrades.isInstalled(AEItems.FUZZY_CARD);
        if (isFuzzy) {
            builder.fuzzyMode(this.getFuzzyMode());
        }
        builder.addAll(config.keySet());
        this.partitionListMode = hasInverter ? IncludeExclude.BLACKLIST : IncludeExclude.WHITELIST;
        this.partitionList = builder.build();
        if (upgrades.isInstalled(AEItems.EQUAL_DISTRIBUTION_CARD)) {
            long maxTypes = Integer.MAX_VALUE;
            if (!isFuzzy && this.partitionListMode == IncludeExclude.WHITELIST && !config.keySet().isEmpty()) {
                maxTypes = config.keySet().size();
            }
            maxTypes = Math.min(maxTypes, (long)this.maxItemTypes);
            long totalStorage = (this.getTotalBytes() - (long)this.getBytesPerType() * maxTypes) * (long)this.keyType.getAmountPerByte();
            this.maxItemsPerType = Math.max(0L, (totalStorage + maxTypes - 1L) / maxTypes);
        } else {
            this.maxItemsPerType = Long.MAX_VALUE;
        }
        this.hasVoidUpgrade = upgrades.isInstalled(AEItems.VOID_CARD);
    }

    private List<GenericStack> getStoredStacks() {
        return (List)this.i.getOrDefault(AEComponents.STORAGE_CELL_INV, List.of());
    }

    public IncludeExclude getPartitionListMode() {
        return this.partitionListMode;
    }

    public boolean isPreformatted() {
        return !this.partitionList.isEmpty();
    }

    public boolean isFuzzy() {
        return this.partitionList instanceof FuzzyPriorityList;
    }

    public static BasicCellInventory createInventory(ItemStack o, @Nullable ISaveProvider container) {
        Objects.requireNonNull(o, "Cannot create cell inventory for null itemstack");
        Item item = o.getItem();
        if (!(item instanceof IBasicCellItem)) {
            return null;
        }
        IBasicCellItem cellType = (IBasicCellItem)item;
        if (!cellType.isStorageCell(o)) {
            return null;
        }
        return new BasicCellInventory(cellType, o, container);
    }

    public static boolean isCell(ItemStack input) {
        return BasicCellInventory.getStorageCell(input) != null;
    }

    private static IBasicCellItem getStorageCell(ItemStack input) {
        Item item;
        if (input != null && (item = input.getItem()) instanceof IBasicCellItem) {
            IBasicCellItem basicCellItem = (IBasicCellItem)item;
            return basicCellItem;
        }
        return null;
    }

    @Override
    public boolean canFitInsideCell() {
        return this.cellType.storableInStorageCell() || this.getAvailableStacks().isEmpty();
    }

    protected Object2LongMap<AEKey> getCellItems() {
        if (this.storedAmounts == null) {
            this.storedAmounts = new Object2LongOpenHashMap();
            this.loadCellItems();
        }
        return this.storedAmounts;
    }

    @Override
    public void persist() {
        if (this.isPersisted) {
            return;
        }
        long itemCount = 0L;
        ArrayList<GenericStack> stacks = new ArrayList<GenericStack>(this.storedAmounts.size());
        for (Object2LongMap.Entry entry : this.storedAmounts.object2LongEntrySet()) {
            long amount = entry.getLongValue();
            itemCount += amount;
            if (amount <= 0L) continue;
            stacks.add(new GenericStack((AEKey)entry.getKey(), amount));
        }
        if (stacks.isEmpty()) {
            this.i.remove(AEComponents.STORAGE_CELL_INV);
        } else {
            this.i.set(AEComponents.STORAGE_CELL_INV, stacks);
        }
        this.storedItems = (short)this.storedAmounts.size();
        this.storedItemCount = itemCount;
        this.isPersisted = true;
    }

    protected void saveChanges() {
        this.storedItems = (short)this.storedAmounts.size();
        this.storedItemCount = 0L;
        for (Long storedAmount : this.storedAmounts.values()) {
            this.storedItemCount += storedAmount.longValue();
        }
        this.isPersisted = false;
        if (this.container != null) {
            this.container.saveChanges();
        } else {
            this.persist();
        }
    }

    private void loadCellItems() {
        List<GenericStack> stacks = this.getStoredStacks();
        for (GenericStack stack : stacks) {
            this.storedAmounts.put((Object)stack.what(), stack.amount());
        }
    }

    @Override
    public void getAvailableStacks(KeyCounter out) {
        for (Object2LongMap.Entry entry : Object2LongMaps.fastIterable(this.getCellItems())) {
            out.add((AEKey)entry.getKey(), entry.getLongValue());
        }
    }

    @Override
    public double getIdleDrain() {
        return this.cellType.getIdleDrain();
    }

    public FuzzyMode getFuzzyMode() {
        return this.cellType.getFuzzyMode(this.i);
    }

    public ConfigInventory getConfigInventory() {
        return this.cellType.getConfigInventory(this.i);
    }

    public IUpgradeInventory getUpgradesInventory() {
        return this.cellType.getUpgrades(this.i);
    }

    public int getBytesPerType() {
        return this.cellType.getBytesPerType(this.i);
    }

    public boolean canHoldNewItem() {
        long bytesFree = this.getFreeBytes();
        return (bytesFree > (long)this.getBytesPerType() || bytesFree == (long)this.getBytesPerType() && this.getUnusedItemCount() > 0) && this.getRemainingItemTypes() > 0L;
    }

    public long getTotalBytes() {
        return this.cellType.getBytes(this.i);
    }

    public long getFreeBytes() {
        return this.getTotalBytes() - this.getUsedBytes();
    }

    public long getTotalItemTypes() {
        return this.maxItemTypes;
    }

    public long getStoredItemCount() {
        return this.storedItemCount;
    }

    public long getStoredItemTypes() {
        return this.storedItems;
    }

    public long getRemainingItemTypes() {
        long basedOnStorage = this.getFreeBytes() / (long)this.getBytesPerType();
        long baseOnTotal = this.getTotalItemTypes() - this.getStoredItemTypes();
        return Math.min(basedOnStorage, baseOnTotal);
    }

    public long getUsedBytes() {
        long bytesForItemCount = (this.getStoredItemCount() + (long)this.getUnusedItemCount()) / (long)this.keyType.getAmountPerByte();
        return this.getStoredItemTypes() * (long)this.getBytesPerType() + bytesForItemCount;
    }

    public long getRemainingItemCount() {
        long remaining = this.getFreeBytes() * (long)this.keyType.getAmountPerByte() + (long)this.getUnusedItemCount();
        return remaining > 0L ? remaining : 0L;
    }

    public int getUnusedItemCount() {
        int div = (int)(this.getStoredItemCount() % (long)this.keyType.getAmountPerByte());
        if (div == 0) {
            return 0;
        }
        return this.keyType.getAmountPerByte() - div;
    }

    @Override
    public CellState getStatus() {
        if (this.getStoredItemTypes() == 0L) {
            return CellState.EMPTY;
        }
        if (this.canHoldNewItem()) {
            return CellState.NOT_EMPTY;
        }
        if (this.getRemainingItemCount() > 0L) {
            return CellState.TYPES_FULL;
        }
        return CellState.FULL;
    }

    @Override
    public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
        if (amount == 0L || !this.keyType.contains(what)) {
            return 0L;
        }
        if (!this.partitionList.matchesFilter(what, this.partitionListMode)) {
            return 0L;
        }
        if (this.cellType.isBlackListed(this.i, what)) {
            return 0L;
        }
        long inserted = this.innerInsert(what, amount, mode);
        if (!this.isPreformatted() && this.hasVoidUpgrade && !this.canHoldNewItem()) {
            return this.getCellItems().containsKey((Object)what) ? amount : inserted;
        }
        return this.hasVoidUpgrade ? amount : inserted;
    }

    private long innerInsert(AEKey what, long amount, Actionable mode) {
        AEItemKey itemKey;
        ItemStack stack;
        StorageCell cellInv;
        if (what instanceof AEItemKey && (cellInv = StorageCells.getCellInventory(stack = (itemKey = (AEItemKey)what).toStack(), null)) != null && !cellInv.canFitInsideCell()) {
            return 0L;
        }
        long currentAmount = this.getCellItems().getLong((Object)what);
        long remainingItemCount = this.getRemainingItemCount();
        if (currentAmount <= 0L) {
            if (!this.canHoldNewItem()) {
                return 0L;
            }
            if ((remainingItemCount -= (long)this.getBytesPerType() * (long)this.keyType.getAmountPerByte()) <= 0L) {
                return 0L;
            }
        }
        if (amount > (remainingItemCount = Math.max(0L, Math.min(this.maxItemsPerType - currentAmount, remainingItemCount)))) {
            amount = remainingItemCount;
        }
        if (mode == Actionable.MODULATE) {
            this.getCellItems().put((Object)what, currentAmount + amount);
            this.saveChanges();
        }
        return amount;
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        long currentAmount = this.getCellItems().getLong((Object)what);
        if (currentAmount > 0L) {
            if (amount >= currentAmount) {
                if (mode == Actionable.MODULATE) {
                    this.getCellItems().remove((Object)what, currentAmount);
                    this.saveChanges();
                }
                return currentAmount;
            }
            if (mode == Actionable.MODULATE) {
                this.getCellItems().put((Object)what, currentAmount - amount);
                this.saveChanges();
            }
            return amount;
        }
        return 0L;
    }

    @Override
    public Component getDescription() {
        return this.i.getHoverName();
    }
}

