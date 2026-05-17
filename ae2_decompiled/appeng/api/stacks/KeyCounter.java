/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Iterators
 *  it.unimi.dsi.fastutil.objects.Object2LongMap$Entry
 *  it.unimi.dsi.fastutil.objects.ObjectIterator
 *  it.unimi.dsi.fastutil.objects.Reference2ObjectMap
 *  it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
 *  org.jetbrains.annotations.Nullable
 */
package appeng.api.stacks;

import appeng.api.config.FuzzyMode;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.VariantCounter;
import com.google.common.collect.Iterators;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.jetbrains.annotations.Nullable;

public final class KeyCounter
implements Iterable<Object2LongMap.Entry<AEKey>> {
    private final Reference2ObjectMap<Object, VariantCounter> lists = new Reference2ObjectOpenHashMap();

    public Collection<Object2LongMap.Entry<AEKey>> findFuzzy(AEKey key, FuzzyMode fuzzy) {
        Objects.requireNonNull(key, "key");
        VariantCounter subIndex = this.getSubIndexOrNull(key);
        return subIndex == null ? List.of() : subIndex.findFuzzy(key, fuzzy);
    }

    public void removeZeros() {
        ObjectIterator iterator = this.lists.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry)iterator.next();
            VariantCounter variantList = (VariantCounter)entry.getValue();
            variantList.removeZeros();
            if (!variantList.isEmpty()) continue;
            iterator.remove();
        }
    }

    public void removeEmptySubmaps() {
        this.lists.values().removeIf(VariantCounter::isEmpty);
    }

    public void addAll(KeyCounter other) {
        for (Map.Entry entry : other.lists.entrySet()) {
            VariantCounter ourSubIndex = (VariantCounter)this.lists.get(entry.getKey());
            if (ourSubIndex == null) {
                this.lists.put(entry.getKey(), (Object)((VariantCounter)entry.getValue()).copy());
                continue;
            }
            ourSubIndex.addAll((VariantCounter)entry.getValue());
        }
    }

    public void removeAll(KeyCounter other) {
        for (Map.Entry entry : other.lists.entrySet()) {
            VariantCounter ourSubIndex = (VariantCounter)this.lists.get(entry.getKey());
            if (ourSubIndex == null) {
                VariantCounter copied = ((VariantCounter)entry.getValue()).copy();
                copied.invert();
                this.lists.put(entry.getKey(), (Object)copied);
                continue;
            }
            ourSubIndex.removeAll((VariantCounter)entry.getValue());
        }
    }

    public void add(AEKey key, long amount) {
        Objects.requireNonNull(key, "key");
        this.getSubIndex(key).add(key, amount);
    }

    public void remove(AEKey key, long amount) {
        this.add(key, -amount);
    }

    public long remove(AEKey key) {
        VariantCounter subIndex = this.getSubIndex(key);
        long ret = subIndex.remove(key);
        if (subIndex.isEmpty()) {
            this.lists.remove(key.getPrimaryKey());
        }
        return ret;
    }

    public void set(AEKey key, long amount) {
        this.getSubIndex(key).set(key, amount);
    }

    public long get(AEKey key) {
        Objects.requireNonNull(key);
        VariantCounter subIndex = (VariantCounter)this.lists.get(key.getPrimaryKey());
        if (subIndex == null) {
            return 0L;
        }
        return subIndex.get(key);
    }

    public void reset() {
        for (VariantCounter list : this.lists.values()) {
            list.reset();
        }
    }

    public void clear() {
        for (VariantCounter list : this.lists.values()) {
            list.clear();
        }
    }

    public boolean isEmpty() {
        for (VariantCounter list : this.lists.values()) {
            if (list.isEmpty()) continue;
            return false;
        }
        return true;
    }

    public int size() {
        int tot = 0;
        for (VariantCounter list : this.lists.values()) {
            tot += list.size();
        }
        return tot;
    }

    @Override
    public Iterator<Object2LongMap.Entry<AEKey>> iterator() {
        return Iterators.concat((Iterator)Iterators.transform((Iterator)this.lists.values().iterator(), VariantCounter::iterator));
    }

    private VariantCounter getSubIndex(AEKey key) {
        if (key.getFuzzySearchMaxValue() > 0) {
            return (VariantCounter)this.lists.computeIfAbsent(key.getPrimaryKey(), k -> new VariantCounter.FuzzyVariantMap());
        }
        return (VariantCounter)this.lists.computeIfAbsent(key.getPrimaryKey(), k -> new VariantCounter.UnorderedVariantMap());
    }

    @Nullable
    private VariantCounter getSubIndexOrNull(AEKey key) {
        return (VariantCounter)this.lists.get(key.getPrimaryKey());
    }

    @Nullable
    public AEKey getFirstKey() {
        Object2LongMap.Entry<AEKey> e = this.getFirstEntry();
        return e != null ? (AEKey)e.getKey() : null;
    }

    @Nullable
    public <T extends AEKey> T getFirstKey(Class<T> keyClass) {
        Object2LongMap.Entry<AEKey> e = this.getFirstEntry(keyClass);
        return (T)(e != null ? (AEKey)keyClass.cast(e.getKey()) : null);
    }

    @Nullable
    public Object2LongMap.Entry<AEKey> getFirstEntry() {
        for (VariantCounter value : this.lists.values()) {
            Iterator<Object2LongMap.Entry<AEKey>> it = value.iterator();
            if (!it.hasNext()) continue;
            return it.next();
        }
        return null;
    }

    @Nullable
    public <T extends AEKey> Object2LongMap.Entry<AEKey> getFirstEntry(Class<T> keyClass) {
        for (VariantCounter value : this.lists.values()) {
            Object2LongMap.Entry<AEKey> entry;
            Iterator<Object2LongMap.Entry<AEKey>> it = value.iterator();
            if (!it.hasNext() || !keyClass.isInstance((entry = it.next()).getKey())) continue;
            return entry;
        }
        return null;
    }

    public Set<AEKey> keySet() {
        HashSet<AEKey> keys = new HashSet<AEKey>(this.size());
        for (VariantCounter list : this.lists.values()) {
            for (Object2LongMap.Entry<AEKey> entry : list) {
                keys.add((AEKey)entry.getKey());
            }
        }
        return keys;
    }
}

