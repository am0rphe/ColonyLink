/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Object2LongMap$Entry
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 */
package appeng.me.storage;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import appeng.core.localization.GuiText;
import appeng.me.storage.ITickingMonitor;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import java.util.Map;
import java.util.Objects;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class CompositeStorage
implements MEStorage,
ITickingMonitor {
    private final InventoryCache cache;
    private Map<AEKeyType, MEStorage> storages;
    private boolean forceCacheRebuild = true;

    public CompositeStorage(Map<AEKeyType, MEStorage> storages) {
        this.storages = storages;
        this.cache = new InventoryCache();
    }

    public void setStorages(Map<AEKeyType, MEStorage> storages) {
        this.storages = Objects.requireNonNull(storages);
    }

    @Override
    public boolean isPreferredStorageFor(AEKey what, IActionSource source) {
        MEStorage storage = this.storages.get(what.getType());
        return storage != null && storage.isPreferredStorageFor(what, source);
    }

    @Override
    public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
        long inserted;
        MEStorage storage = this.storages.get(what.getType());
        long l = inserted = storage != null ? storage.insert(what, amount, mode, source) : 0L;
        if (inserted > 0L && mode == Actionable.MODULATE) {
            this.forceCacheRebuild = true;
        }
        return inserted;
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        long extracted;
        MEStorage storage = this.storages.get(what.getType());
        long l = extracted = storage != null ? storage.extract(what, amount, mode, source) : 0L;
        if (extracted > 0L && mode == Actionable.MODULATE) {
            this.forceCacheRebuild = true;
        }
        return extracted;
    }

    @Override
    public Component getDescription() {
        MutableComponent types = Component.literal((String)"");
        boolean first = true;
        for (AEKeyType keyType : this.storages.keySet()) {
            if (!first) {
                types.append(", ");
            } else {
                first = false;
            }
            types.append(keyType.getDescription());
        }
        return GuiText.ExternalStorage.text(types);
    }

    @Override
    public TickRateModulation onTick() {
        this.forceCacheRebuild = false;
        boolean changed = this.cache.update();
        if (changed) {
            return TickRateModulation.URGENT;
        }
        return TickRateModulation.SLOWER;
    }

    @Override
    public void getAvailableStacks(KeyCounter out) {
        if (this.forceCacheRebuild) {
            this.forceCacheRebuild = false;
            this.cache.update();
        }
        this.cache.getAvailableKeys(out);
    }

    private class InventoryCache {
        private KeyCounter frontBuffer = new KeyCounter();
        private KeyCounter backBuffer = new KeyCounter();

        private InventoryCache() {
        }

        public boolean update() {
            KeyCounter tmp = this.backBuffer;
            this.backBuffer = this.frontBuffer;
            this.frontBuffer = tmp;
            this.frontBuffer.reset();
            for (MEStorage storage : CompositeStorage.this.storages.values()) {
                storage.getAvailableStacks(this.frontBuffer);
            }
            boolean changed = false;
            for (Object2LongMap.Entry<AEKey> entry : this.frontBuffer) {
                long old = this.backBuffer.get((AEKey)entry.getKey());
                if (old != 0L && old == entry.getLongValue()) continue;
                changed = true;
            }
            for (Object2LongMap.Entry<AEKey> oldEntry : this.backBuffer) {
                if (this.frontBuffer.get((AEKey)oldEntry.getKey()) != 0L) continue;
                changed = true;
            }
            this.frontBuffer.removeZeros();
            return changed;
        }

        public void getAvailableKeys(KeyCounter out) {
            out.addAll(this.frontBuffer);
        }

        public boolean contains(AEKey what) {
            return this.frontBuffer.get(what) > 0L;
        }
    }
}

