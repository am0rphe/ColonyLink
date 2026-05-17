/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Multimap
 *  com.google.common.collect.Sets
 */
package appeng.me.helpers;

import appeng.api.stacks.AEKey;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Set;

public class InterestManager<T> {
    private final Multimap<AEKey, T> container;
    private final Set<T> allStacksWatchers = Sets.newIdentityHashSet();

    public InterestManager(Multimap<AEKey, T> interests) {
        this.container = interests;
    }

    public boolean put(AEKey stack, T iw) {
        return this.container.put((Object)stack, iw);
    }

    public boolean remove(AEKey stack, T iw) {
        return this.container.remove((Object)stack, iw);
    }

    public void setWatchAll(boolean watchAll, T watcher) {
        if (watchAll) {
            this.allStacksWatchers.add(watcher);
        } else {
            this.allStacksWatchers.remove(watcher);
        }
    }

    public boolean containsKey(AEKey stack) {
        return this.container.containsKey((Object)stack);
    }

    public Collection<T> get(AEKey stack) {
        return this.container.get((Object)stack);
    }

    public Collection<T> getAllStacksWatchers() {
        return this.allStacksWatchers;
    }

    public boolean isEmpty() {
        return this.allStacksWatchers.isEmpty() && this.container.isEmpty();
    }
}

