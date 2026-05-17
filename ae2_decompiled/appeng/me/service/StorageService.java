/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Preconditions
 *  com.google.common.collect.HashMultimap
 *  com.google.common.collect.Multimap
 *  com.google.common.collect.SetMultimap
 *  com.google.common.math.StatsAccumulator
 *  com.google.gson.Gson
 *  com.google.gson.JsonElement
 *  com.google.gson.stream.JsonWriter
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JsonOps
 *  it.unimi.dsi.fastutil.objects.Object2LongMap
 *  it.unimi.dsi.fastutil.objects.Object2LongMap$Entry
 *  it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.NbtOps
 *  org.jetbrains.annotations.Nullable
 */
package appeng.me.service;

import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridServiceProvider;
import appeng.api.networking.storage.IStorageService;
import appeng.api.networking.storage.IStorageWatcherNode;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.IStorageMounts;
import appeng.api.storage.IStorageProvider;
import appeng.api.storage.MEStorage;
import appeng.me.helpers.InterestManager;
import appeng.me.helpers.StackWatcher;
import appeng.me.storage.NetworkStorage;
import appeng.util.JsonStreamUtil;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;
import com.google.common.math.StatsAccumulator;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.stream.JsonWriter;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import org.jetbrains.annotations.Nullable;

public class StorageService
implements IStorageService,
IGridServiceProvider {
    private static final Gson GSON = new Gson();
    private final Map<IGridNode, ProviderState> nodeProviders = new IdentityHashMap<IGridNode, ProviderState>();
    private final List<ProviderState> globalProviders = new ArrayList<ProviderState>();
    private final SetMultimap<AEKey, StackWatcher<IStorageWatcherNode>> interests = HashMultimap.create();
    private final InterestManager<StackWatcher<IStorageWatcherNode>> interestManager = new InterestManager<StackWatcher<IStorageWatcherNode>>((Multimap<AEKey, StackWatcher<IStorageWatcherNode>>)this.interests);
    private final NetworkStorage storage;
    private final KeyCounter cachedAvailableStacks = new KeyCounter();
    private final Object2LongMap<AEKey> cachedAvailableAmounts = new Object2LongOpenHashMap();
    private boolean cachedStacksNeedUpdate = true;
    private final Map<IGridNode, StackWatcher<IStorageWatcherNode>> watchers = new IdentityHashMap<IGridNode, StackWatcher<IStorageWatcherNode>>();
    private final StatsAccumulator inventoryRefreshStats = new StatsAccumulator();

    public StorageService() {
        this.storage = new NetworkStorage();
    }

    @Override
    public void onServerEndTick() {
        if (this.interestManager.isEmpty()) {
            this.cachedStacksNeedUpdate = true;
        } else {
            this.updateCachedStacks();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void updateCachedStacks() {
        long time = System.nanoTime();
        try {
            this.cachedStacksNeedUpdate = false;
            this.cachedAvailableStacks.clear();
            this.storage.getAvailableStacks(this.cachedAvailableStacks);
            this.cachedAvailableStacks.removeEmptySubmaps();
            for (Object2LongMap.Entry entry : this.cachedAvailableStacks) {
                AEKey what = (AEKey)entry.getKey();
                long newAmount = entry.getLongValue();
                if (newAmount == this.cachedAvailableAmounts.getLong((Object)what)) continue;
                this.postWatcherUpdate(what, newAmount);
            }
            for (AEKey what : this.cachedAvailableAmounts.keySet()) {
                long newAmount = this.cachedAvailableStacks.get(what);
                if (newAmount != 0L) continue;
                this.postWatcherUpdate(what, newAmount);
            }
            this.cachedAvailableAmounts.clear();
            for (Object2LongMap.Entry entry : this.cachedAvailableStacks) {
                this.cachedAvailableAmounts.put((Object)((AEKey)entry.getKey()), entry.getLongValue());
            }
        }
        finally {
            this.inventoryRefreshStats.add((double)(System.nanoTime() - time));
        }
    }

    private void postWatcherUpdate(AEKey what, long newAmount) {
        for (StackWatcher<IStorageWatcherNode> watcher : this.interestManager.get(what)) {
            watcher.getHost().onStackChange(what, newAmount);
        }
        for (StackWatcher<IStorageWatcherNode> watcher : this.interestManager.getAllStacksWatchers()) {
            watcher.getHost().onStackChange(what, newAmount);
        }
    }

    @Override
    public void addNode(IGridNode node, @Nullable CompoundTag savedData) {
        IStorageWatcherNode watcher;
        IStorageProvider storageProvider = node.getService(IStorageProvider.class);
        if (storageProvider != null) {
            ProviderState state = new ProviderState(storageProvider);
            this.nodeProviders.put(node, state);
            state.mount();
        }
        if ((watcher = node.getService(IStorageWatcherNode.class)) != null) {
            StackWatcher<IStorageWatcherNode> iw = new StackWatcher<IStorageWatcherNode>(this.interestManager, watcher);
            this.watchers.put(node, iw);
            watcher.updateWatcher(iw);
        }
    }

    @Override
    public void removeNode(IGridNode node) {
        ProviderState providerState;
        StackWatcher<IStorageWatcherNode> watcher = this.watchers.remove(node);
        if (watcher != null) {
            watcher.destroy();
        }
        if ((providerState = this.nodeProviders.remove(node)) != null) {
            providerState.unmount();
        }
    }

    @Override
    public MEStorage getInventory() {
        return this.storage;
    }

    @Override
    public KeyCounter getCachedInventory() {
        if (this.cachedStacksNeedUpdate) {
            this.updateCachedStacks();
        }
        return this.cachedAvailableStacks;
    }

    @Override
    public void addGlobalStorageProvider(IStorageProvider provider) {
        for (ProviderState state : this.globalProviders) {
            if (state.provider != provider) continue;
            throw new IllegalArgumentException("Duplicate storage provider registration for " + String.valueOf(provider));
        }
        ProviderState state = new ProviderState(provider);
        this.globalProviders.add(state);
        state.mount();
    }

    @Override
    public void removeGlobalStorageProvider(IStorageProvider provider) {
        Iterator<ProviderState> it = this.globalProviders.iterator();
        while (it.hasNext()) {
            ProviderState state = it.next();
            if (state.provider != provider) continue;
            it.remove();
            state.unmount();
        }
    }

    @Override
    public void refreshNodeStorageProvider(IGridNode node) {
        ProviderState state = this.nodeProviders.get(node);
        if (state == null) {
            throw new IllegalArgumentException("The given node is not part of this grid or has no storage provider.");
        }
        state.update();
    }

    @Override
    public void refreshGlobalStorageProvider(IStorageProvider provider) {
        for (ProviderState state : this.globalProviders) {
            if (state.provider != provider) continue;
            state.update();
            return;
        }
        throw new IllegalArgumentException("Storage provider " + String.valueOf(provider) + " is not part of this grid.");
    }

    @Override
    public void invalidateCache() {
        this.cachedStacksNeedUpdate = true;
    }

    @Override
    public void debugDump(JsonWriter writer, HolderLookup.Provider registries) throws IOException {
        JsonStreamUtil.writeProperties(Map.of("inventoryRefreshTime", JsonStreamUtil.toMap(this.inventoryRefreshStats)), writer);
        writer.name("cachedAvailableStacks");
        writer.beginArray();
        for (Object2LongMap.Entry<AEKey> entry : this.cachedAvailableStacks) {
            writer.beginObject();
            writer.name("key");
            CompoundTag serializedKey = ((AEKey)entry.getKey()).toTagGeneric(registries);
            JsonElement jsonKey = (JsonElement)Dynamic.convert((DynamicOps)NbtOps.INSTANCE, (DynamicOps)JsonOps.INSTANCE, (Object)serializedKey);
            GSON.toJson(jsonKey, writer);
            writer.name("amount");
            writer.value(entry.getLongValue());
            writer.endObject();
        }
        writer.endArray();
    }

    private class ProviderState
    implements IStorageMounts {
        private final IStorageProvider provider;
        private final Set<MEStorage> inventories = new HashSet<MEStorage>();
        private boolean mounted;

        public ProviderState(IStorageProvider provider) {
            this.provider = provider;
        }

        private void mount() {
            Preconditions.checkState((!this.mounted ? 1 : 0) != 0, (Object)"Can't mount a provider's inventories when it's already mounted");
            this.mounted = true;
            this.provider.mountInventories(this);
        }

        @Override
        public void mount(MEStorage inventory, int priority) {
            Preconditions.checkState((boolean)this.mounted, (Object)"Cannot use StorageMounts after the storage has been unmounted.");
            if (!this.inventories.add(inventory)) {
                throw new IllegalStateException("Cannot mount the same inventory twice.");
            }
            StorageService.this.storage.mount(priority, inventory);
        }

        public void update() {
            this.unmount();
            this.mount();
        }

        public void unmount() {
            if (!this.mounted) {
                return;
            }
            this.mounted = false;
            for (MEStorage inventory : this.inventories) {
                this.unmount(inventory);
            }
            this.inventories.clear();
        }

        private void unmount(MEStorage inventory) {
            StorageService.this.storage.unmount(inventory);
        }
    }
}

