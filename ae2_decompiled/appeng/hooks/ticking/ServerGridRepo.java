/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
 *  it.unimi.dsi.fastutil.objects.ObjectSet
 */
package appeng.hooks.ticking;

import appeng.me.Grid;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

class ServerGridRepo {
    private final ObjectSet<Grid> networks = new ObjectOpenHashSet();
    private final ObjectSet<Grid> toAdd = new ObjectOpenHashSet();
    private final ObjectSet<Grid> toRemove = new ObjectOpenHashSet();

    ServerGridRepo() {
    }

    void clear() {
        this.networks.clear();
        this.toAdd.clear();
        this.toRemove.clear();
    }

    synchronized void addNetwork(Grid g) {
        Objects.requireNonNull(g);
        this.toAdd.add((Object)g);
        this.toRemove.remove((Object)g);
    }

    synchronized void removeNetwork(Grid g) {
        Objects.requireNonNull(g);
        this.toRemove.add((Object)g);
        this.toAdd.remove((Object)g);
    }

    synchronized void updateNetworks() {
        this.networks.removeAll(this.toRemove);
        this.toRemove.clear();
        this.networks.addAll(this.toAdd);
        this.toAdd.clear();
    }

    public Set<Grid> getNetworks() {
        return Collections.unmodifiableSet(this.networks);
    }
}

