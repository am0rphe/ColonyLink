/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.BiMap
 *  com.google.common.collect.HashBiMap
 *  it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
 *  it.unimi.dsi.fastutil.ints.IntArrayList
 *  it.unimi.dsi.fastutil.ints.IntList
 *  it.unimi.dsi.fastutil.longs.LongOpenHashSet
 *  it.unimi.dsi.fastutil.longs.LongSet
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.world.item.crafting.Ingredient
 *  org.jetbrains.annotations.Nullable
 */
package appeng.client.gui.me.common;

import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.ViewItems;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.client.gui.me.common.KeySorters;
import appeng.client.gui.me.common.PinnedKeys;
import appeng.client.gui.me.search.RepoSearch;
import appeng.client.gui.widgets.IScrollSource;
import appeng.client.gui.widgets.ISortSource;
import appeng.core.AELog;
import appeng.menu.me.common.GridInventoryEntry;
import appeng.menu.me.common.IClientRepo;
import appeng.util.prioritylist.IPartitionList;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;

public class Repo
implements IClientRepo {
    public static final Comparator<GridInventoryEntry> AMOUNT_ASC = Comparator.comparingDouble(entry -> (double)entry.getStoredAmount() / (double)entry.getWhat().getAmountPerUnit());
    public static final Comparator<GridInventoryEntry> AMOUNT_DESC = AMOUNT_ASC.reversed();
    private static final Comparator<GridInventoryEntry> PINNED_ROW_COMPARATOR = Comparator.comparing(entry -> {
        PinnedKeys.PinInfo pinInfo = PinnedKeys.getPinInfo(entry.getWhat());
        return pinInfo != null ? pinInfo.since : Instant.MAX;
    });
    private int rowSize = 9;
    private boolean enabled = false;
    private final BiMap<Long, GridInventoryEntry> entries = HashBiMap.create();
    private final ArrayList<GridInventoryEntry> view = new ArrayList();
    private final ArrayList<GridInventoryEntry> pinnedRow = new ArrayList();
    private final Int2ObjectOpenHashMap<List<GridInventoryEntry>> entriesByItemId = new Int2ObjectOpenHashMap();
    private boolean entriesByItemIdNeedsUpdate = true;
    private final RepoSearch search = new RepoSearch();
    private IPartitionList partitionList;
    private Runnable updateViewListener;
    private final IScrollSource src;
    private final ISortSource sortSrc;
    private boolean paused;

    public Repo(IScrollSource src, ISortSource sortSrc) {
        this.src = src;
        this.sortSrc = sortSrc;
    }

    public void setPartitionList(IPartitionList partitionList) {
        if (partitionList != this.partitionList) {
            this.partitionList = partitionList;
            this.updateView();
        }
    }

    @Override
    public final void handleUpdate(boolean fullUpdate, List<GridInventoryEntry> entries) {
        if (fullUpdate) {
            this.clear();
        }
        for (GridInventoryEntry entry : entries) {
            this.handleUpdate(entry);
        }
        this.updateView();
    }

    private void handleUpdate(GridInventoryEntry serverEntry) {
        this.entriesByItemIdNeedsUpdate = true;
        GridInventoryEntry localEntry = (GridInventoryEntry)this.entries.get((Object)serverEntry.getSerial());
        if (localEntry == null) {
            if (serverEntry.getWhat() == null) {
                AELog.warn("First time seeing serial %s, but incomplete info received", serverEntry.getSerial());
                return;
            }
            if (serverEntry.isMeaningful()) {
                this.entries.put((Object)serverEntry.getSerial(), (Object)serverEntry);
            }
            return;
        }
        if (!serverEntry.isMeaningful()) {
            this.entries.remove((Object)serverEntry.getSerial());
        } else if (serverEntry.getWhat() == null) {
            this.entries.put((Object)serverEntry.getSerial(), (Object)new GridInventoryEntry(serverEntry.getSerial(), localEntry.getWhat(), serverEntry.getStoredAmount(), serverEntry.getRequestableAmount(), serverEntry.isCraftable()));
        } else {
            this.entries.put((Object)serverEntry.getSerial(), (Object)serverEntry);
        }
    }

    public final void updateView() {
        if (this.isPaused()) {
            LongOpenHashSet visibleSerials = new LongOpenHashSet(this.view.size());
            this.updateEntriesWhilePaused(this.pinnedRow, (LongSet)visibleSerials);
            this.updateEntriesWhilePaused(this.view, (LongSet)visibleSerials);
            Map<AEKey, IntList> pinnedRowFreeSlots = this.getFreeSlots(this.pinnedRow);
            Map<AEKey, IntList> viewFreeSlots = this.getFreeSlots(this.view);
            ArrayList<GridInventoryEntry> entriesToAdd = new ArrayList<GridInventoryEntry>();
            for (GridInventoryEntry serverEntry : this.entries.values()) {
                if (visibleSerials.contains(serverEntry.getSerial()) || Repo.takeOverSlotOccupiedByRemovedItem(serverEntry, pinnedRowFreeSlots, this.pinnedRow) || Repo.takeOverSlotOccupiedByRemovedItem(serverEntry, viewFreeSlots, this.view)) continue;
                entriesToAdd.add(serverEntry);
            }
            this.addEntriesToView(entriesToAdd);
        } else {
            this.view.clear();
            this.pinnedRow.clear();
            this.view.ensureCapacity(this.entries.size());
            this.pinnedRow.ensureCapacity(this.rowSize);
            this.addEntriesToView(this.entries.values());
        }
        if (!this.isPaused()) {
            this.pinnedRow.sort(PINNED_ROW_COMPARATOR);
            SortOrder sortOrder = this.sortSrc.getSortBy();
            SortDir sortDir = this.sortSrc.getSortDir();
            this.view.sort(this.getComparator(sortOrder, sortDir));
        }
        if (this.updateViewListener != null) {
            this.updateViewListener.run();
        }
    }

    private void addEntriesToView(Collection<GridInventoryEntry> entries) {
        ViewItems viewMode = this.sortSrc.getSortDisplay();
        Set<AEKeyType> typeFilter = this.sortSrc.getSortKeyTypes();
        boolean hasPinnedRow = !PinnedKeys.isEmpty();
        for (GridInventoryEntry entry : entries) {
            if (hasPinnedRow && this.pinnedRow.size() < this.rowSize && PinnedKeys.isPinned(entry.getWhat())) {
                this.pinnedRow.add(entry);
                continue;
            }
            if (this.partitionList != null && !this.partitionList.isListed(entry.getWhat()) || viewMode == ViewItems.CRAFTABLE && !entry.isCraftable() || viewMode == ViewItems.STORED && entry.getStoredAmount() == 0L || !typeFilter.contains(entry.getWhat().getType()) || !this.search.matches(entry)) continue;
            this.view.add(entry);
        }
        if (hasPinnedRow) {
            for (AEKey pinnedKey : PinnedKeys.getPinnedKeys()) {
                PinnedKeys.PinInfo info = PinnedKeys.getPinInfo(pinnedKey);
                if (info.reason == PinnedKeys.PinReason.CRAFTING || !this.pinnedRow.stream().noneMatch(r -> pinnedKey.equals(r.getWhat()))) continue;
                this.pinnedRow.add(new GridInventoryEntry(-1L, pinnedKey, 0L, 0L, false));
            }
        }
    }

    private void updateEntriesWhilePaused(List<GridInventoryEntry> shownEntries, LongSet visibleSerials) {
        for (int i = 0; i < shownEntries.size(); ++i) {
            GridInventoryEntry entry = shownEntries.get(i);
            GridInventoryEntry serverEntry = (GridInventoryEntry)this.entries.get((Object)entry.getSerial());
            entry = serverEntry == null ? new GridInventoryEntry(entry.getSerial(), entry.getWhat(), 0L, 0L, false) : serverEntry;
            visibleSerials.add(entry.getSerial());
            shownEntries.set(i, entry);
        }
    }

    private Map<AEKey, IntList> getFreeSlots(List<GridInventoryEntry> slots) {
        HashMap<AEKey, IntList> freeSlots = new HashMap<AEKey, IntList>();
        for (int i = 0; i < slots.size(); ++i) {
            GridInventoryEntry entry = slots.get(i);
            if (this.entries.containsKey((Object)entry.getSerial())) continue;
            freeSlots.computeIfAbsent(entry.getWhat(), k -> new IntArrayList()).add(i);
        }
        for (IntList list : freeSlots.values()) {
            Collections.reverse(list);
        }
        return freeSlots;
    }

    private static boolean takeOverSlotOccupiedByRemovedItem(GridInventoryEntry serverEntry, Map<AEKey, IntList> freeSlots, List<GridInventoryEntry> slots) {
        IntList freeSlotIndices = freeSlots.get(serverEntry.getWhat());
        if (freeSlotIndices == null) {
            return false;
        }
        int i = freeSlotIndices.removeInt(freeSlotIndices.size() - 1);
        if (freeSlotIndices.size() == 0) {
            freeSlots.remove(serverEntry.getWhat());
        }
        slots.set(i, serverEntry);
        return true;
    }

    private Comparator<? super GridInventoryEntry> getComparator(SortOrder sortOrder, SortDir sortDir) {
        if (sortOrder == SortOrder.AMOUNT) {
            return sortDir == SortDir.ASCENDING ? AMOUNT_ASC : AMOUNT_DESC;
        }
        return Comparator.comparing(GridInventoryEntry::getWhat, this.getKeyComparator(sortOrder, sortDir));
    }

    public List<GridInventoryEntry> getPinnedEntries() {
        return Collections.unmodifiableList(this.pinnedRow);
    }

    @Nullable
    public final GridInventoryEntry get(int idx) {
        if (!this.pinnedRow.isEmpty()) {
            if (idx < this.rowSize) {
                if (idx < this.pinnedRow.size()) {
                    return this.pinnedRow.get(idx);
                }
                return null;
            }
            idx -= this.rowSize;
        }
        if ((idx += this.src.getCurrentScroll() * this.rowSize) >= this.view.size()) {
            return null;
        }
        return this.view.get(idx);
    }

    public final int size() {
        return this.view.size() + this.pinnedRow.size();
    }

    public final void clear() {
        this.entries.clear();
        this.view.clear();
        this.pinnedRow.clear();
        this.entriesByItemId.clear();
        this.entriesByItemIdNeedsUpdate = true;
    }

    public final boolean hasPinnedRow() {
        return !this.pinnedRow.isEmpty();
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public final int getRowSize() {
        return this.rowSize;
    }

    public final void setRowSize(int rowSize) {
        this.rowSize = rowSize;
    }

    public final String getSearchString() {
        return this.search.getSearchString();
    }

    public final void setSearchString(String searchString) {
        this.search.setSearchString(searchString);
    }

    private Comparator<AEKey> getKeyComparator(SortOrder sortBy, SortDir sortDir) {
        return KeySorters.getComparator(sortBy, sortDir);
    }

    public boolean isPaused() {
        return this.paused;
    }

    public void setPaused(boolean paused) {
        if (this.paused != paused) {
            this.paused = paused;
            AELog.debug("Toggling client-repo pause mode to %s", this.paused);
            if (!paused) {
                this.updateView();
            }
        }
    }

    @Override
    public Set<GridInventoryEntry> getAllEntries() {
        return this.entries.values();
    }

    public List<GridInventoryEntry> getByIngredient(Ingredient ingredient) {
        ArrayList<GridInventoryEntry> entries = new ArrayList<GridInventoryEntry>();
        for (int i = 0; i < ingredient.getStackingIds().size(); ++i) {
            int itemId = ingredient.getStackingIds().getInt(i);
            for (GridInventoryEntry entry : this.getByItemId(itemId)) {
                if (!((AEItemKey)entry.getWhat()).matches(ingredient)) continue;
                entries.add(entry);
            }
        }
        return entries;
    }

    private Collection<GridInventoryEntry> getByItemId(int itemId) {
        if (this.entriesByItemIdNeedsUpdate) {
            this.rebuildItemIdToEntries();
            this.entriesByItemIdNeedsUpdate = false;
        }
        return (Collection)this.entriesByItemId.getOrDefault(itemId, List.of());
    }

    private void rebuildItemIdToEntries() {
        this.entriesByItemId.clear();
        for (GridInventoryEntry entry : this.getAllEntries()) {
            AEKey aEKey = entry.getWhat();
            if (!(aEKey instanceof AEItemKey)) continue;
            AEItemKey itemKey = (AEItemKey)aEKey;
            int itemId = BuiltInRegistries.ITEM.getId((Object)itemKey.getItem());
            List currentList = (List)this.entriesByItemId.get(itemId);
            if (currentList == null) {
                this.entriesByItemId.put(itemId, List.of(entry));
                continue;
            }
            if (currentList.size() == 1) {
                ArrayList<GridInventoryEntry> mutableList = new ArrayList<GridInventoryEntry>(10);
                mutableList.addAll(currentList);
                mutableList.add(entry);
                this.entriesByItemId.put(itemId, mutableList);
                continue;
            }
            currentList.add(entry);
        }
    }

    public final void setUpdateViewListener(Runnable updateViewListener) {
        this.updateViewListener = updateViewListener;
    }

    public boolean isCraftable(AEKey what) {
        for (GridInventoryEntry entry : this.entries.values()) {
            if (!entry.isCraftable() || !what.equals(entry.getWhat())) continue;
            return true;
        }
        return false;
    }
}

