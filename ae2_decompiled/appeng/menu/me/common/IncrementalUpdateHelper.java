/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.BiMap
 *  com.google.common.collect.HashBiMap
 *  org.jetbrains.annotations.Nullable
 */
package appeng.menu.me.common;

import appeng.api.stacks.AEKey;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;
import org.jetbrains.annotations.Nullable;

public class IncrementalUpdateHelper
implements Iterable<AEKey> {
    private final BiMap<AEKey, Long> mapping;
    private final Set<AEKey> changes = new HashSet<AEKey>();
    private long serial;
    private boolean fullUpdate = true;

    public IncrementalUpdateHelper() {
        this.mapping = HashBiMap.create();
    }

    @Nullable
    public Long getSerial(AEKey stack) {
        return (Long)this.mapping.get((Object)stack);
    }

    public long getOrAssignSerial(AEKey key) {
        return (Long)this.mapping.computeIfAbsent((Object)key, k -> ++this.serial);
    }

    public AEKey getBySerial(long serial) {
        return (AEKey)this.mapping.inverse().get((Object)serial);
    }

    public void clear() {
        this.changes.clear();
        this.fullUpdate = true;
    }

    public void reset() {
        this.clear();
        this.serial = 0L;
        this.mapping.clear();
    }

    public void addChange(AEKey entry) {
        if (!this.changes.add(entry)) {
            this.changes.remove(entry);
            this.changes.add(entry);
        }
    }

    public void removeSerial(AEKey what) {
        this.mapping.remove((Object)what);
    }

    public void commitChanges() {
        this.changes.clear();
        this.fullUpdate = false;
    }

    public boolean hasChanges() {
        return this.fullUpdate || !this.changes.isEmpty();
    }

    public boolean isFullUpdate() {
        return this.fullUpdate;
    }

    @Override
    public Iterator<AEKey> iterator() {
        return this.changes.iterator();
    }

    @Override
    public void forEach(Consumer<? super AEKey> action) {
        this.changes.forEach(action);
    }

    @Override
    public Spliterator<AEKey> spliterator() {
        return this.changes.spliterator();
    }
}

