/*
 * Decompiled with CFR 0.152.
 */
package appeng.util;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.IntFunction;

public class CowMap<K, V> {
    private final IntFunction<? extends Map<K, V>> mapSupplier;
    private volatile Map<K, V> map;

    public CowMap(IntFunction<? extends Map<K, V>> mapSupplier) {
        this.mapSupplier = mapSupplier;
        this.map = Collections.unmodifiableMap(mapSupplier.apply(0));
    }

    public static <K, V> CowMap<K, V> identityHashMap() {
        return new CowMap<K, V>(IdentityHashMap::new);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void putIfAbsent(K key, V value) throws IllegalArgumentException {
        Objects.requireNonNull(key, "Key may not be null");
        Objects.requireNonNull(value, "Value may not be null");
        CowMap cowMap = this;
        synchronized (cowMap) {
            if (this.map.containsKey(key)) {
                throw new IllegalArgumentException("Map already contains a value for the following key: " + String.valueOf(key));
            }
            Map<K, V> newMap = this.mapSupplier.apply(this.map.size() + 1);
            newMap.putAll(this.map);
            newMap.put(key, value);
            this.map = Collections.unmodifiableMap(newMap);
        }
    }

    public Map<K, V> getMap() {
        return this.map;
    }
}

