/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.longs.LongIterator
 *  it.unimi.dsi.fastutil.objects.Object2LongMap
 *  it.unimi.dsi.fastutil.objects.Object2LongMap$Entry
 *  it.unimi.dsi.fastutil.objects.Object2LongMaps
 *  it.unimi.dsi.fastutil.objects.Object2LongSortedMap
 */
package appeng.api.stacks;

import appeng.api.config.FuzzyMode;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKey2LongMap;
import appeng.api.stacks.FuzzySearch;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMaps;
import it.unimi.dsi.fastutil.objects.Object2LongSortedMap;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

abstract class VariantCounter
implements Iterable<Object2LongMap.Entry<AEKey>> {
    private boolean dropZeros;

    VariantCounter() {
    }

    public boolean isDropZeros() {
        return this.dropZeros;
    }

    public void setDropZeros(boolean dropZeros) {
        this.dropZeros = dropZeros;
    }

    public long get(AEKey key) {
        return this.getRecords().getOrDefault(key, 0L);
    }

    public void add(AEKey key, long amount) {
        this.getRecords().addTo(key, amount);
    }

    public void set(AEKey key, long amount) {
        if (this.dropZeros && amount == 0L) {
            this.getRecords().removeLong(key);
        } else {
            this.getRecords().put(key, amount);
        }
    }

    public long remove(AEKey key) {
        return this.getRecords().removeLong(key);
    }

    public void addAll(VariantCounter other) {
        for (Object2LongMap.Entry entry : other.getRecords().object2LongEntrySet()) {
            this.add((AEKey)entry.getKey(), entry.getLongValue());
        }
    }

    public void removeAll(VariantCounter other) {
        for (Object2LongMap.Entry entry : other.getRecords().object2LongEntrySet()) {
            this.add((AEKey)entry.getKey(), -entry.getLongValue());
        }
    }

    public abstract Collection<Object2LongMap.Entry<AEKey>> findFuzzy(AEKey var1, FuzzyMode var2);

    public int size() {
        if (!this.dropZeros) {
            return this.getRecords().size();
        }
        int size = 0;
        for (Long value : this.getRecords().values()) {
            if (value == 0L) continue;
            ++size;
        }
        return size;
    }

    public boolean isEmpty() {
        if (!this.dropZeros) {
            return this.getRecords().isEmpty();
        }
        for (Long value : this.getRecords().values()) {
            if (value == 0L) continue;
            return false;
        }
        return true;
    }

    @Override
    public Iterator<Object2LongMap.Entry<AEKey>> iterator() {
        if (!this.dropZeros) {
            return Object2LongMaps.fastIterator((Object2LongMap)this.getRecords());
        }
        return new NonDefaultIterator(this);
    }

    abstract AEKey2LongMap getRecords();

    public void reset() {
        if (this.dropZeros) {
            this.getRecords().clear();
        } else {
            this.getRecords().replaceAll((key, value) -> 0L);
        }
    }

    public void clear() {
        this.getRecords().clear();
    }

    public abstract VariantCounter copy();

    public void invert() {
        for (Object2LongMap.Entry entry : this.getRecords().object2LongEntrySet()) {
            entry.setValue(-entry.getLongValue());
        }
    }

    public void removeZeros() {
        LongIterator it = this.getRecords().values().iterator();
        while (it.hasNext()) {
            long entry = it.nextLong();
            if (entry != 0L) continue;
            it.remove();
        }
    }

    private class NonDefaultIterator
    implements Iterator<Object2LongMap.Entry<AEKey>> {
        private final Iterator<Object2LongMap.Entry<AEKey>> parent;
        private Object2LongMap.Entry<AEKey> next;

        public NonDefaultIterator(VariantCounter variantCounter) {
            this.parent = Object2LongMaps.fastIterator((Object2LongMap)variantCounter.getRecords());
            this.next = this.seekNext();
        }

        @Override
        public boolean hasNext() {
            return this.next != null;
        }

        @Override
        public Object2LongMap.Entry<AEKey> next() {
            if (this.next == null) {
                throw new NoSuchElementException();
            }
            Object2LongMap.Entry<AEKey> result = this.next;
            this.next = this.seekNext();
            return result;
        }

        private Object2LongMap.Entry<AEKey> seekNext() {
            while (this.parent.hasNext()) {
                Object2LongMap.Entry<AEKey> entry = this.parent.next();
                if (entry.getLongValue() == 0L) {
                    this.parent.remove();
                    continue;
                }
                return entry;
            }
            return null;
        }
    }

    static class FuzzyVariantMap
    extends VariantCounter {
        private final AEKey2LongMap.AVLTreeMap records = FuzzySearch.createMap2Long();

        FuzzyVariantMap() {
        }

        @Override
        public Collection<Object2LongMap.Entry<AEKey>> findFuzzy(AEKey key, FuzzyMode fuzzy) {
            return ((Object2LongSortedMap)FuzzySearch.findFuzzy(this.records, key, fuzzy)).object2LongEntrySet();
        }

        @Override
        AEKey2LongMap getRecords() {
            return this.records;
        }

        @Override
        public VariantCounter copy() {
            FuzzyVariantMap result = new FuzzyVariantMap();
            result.records.putAll((Map)((Object)this.records));
            return result;
        }
    }

    static class UnorderedVariantMap
    extends VariantCounter {
        private final AEKey2LongMap records = new AEKey2LongMap.OpenHashMap();

        UnorderedVariantMap() {
        }

        @Override
        public Collection<Object2LongMap.Entry<AEKey>> findFuzzy(AEKey filter, FuzzyMode fuzzy) {
            return this.records.object2LongEntrySet();
        }

        @Override
        AEKey2LongMap getRecords() {
            return this.records;
        }

        @Override
        public VariantCounter copy() {
            UnorderedVariantMap result = new UnorderedVariantMap();
            result.records.putAll((Map)((Object)this.records));
            return result;
        }
    }
}

