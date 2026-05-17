/*
 * Decompiled with CFR 0.152.
 */
package appeng.me.helpers;

import appeng.api.networking.IStackWatcher;
import appeng.api.stacks.AEKey;
import appeng.me.helpers.InterestManager;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class StackWatcher<T>
implements IStackWatcher {
    private final InterestManager<StackWatcher<T>> interestManager;
    private final T myHost;
    private final Set<AEKey> myInterests = new HashSet<AEKey>();
    private boolean destroyed = false;

    public StackWatcher(InterestManager<StackWatcher<T>> interestManager, T host) {
        this.interestManager = interestManager;
        this.myHost = host;
    }

    public T getHost() {
        return this.myHost;
    }

    @Override
    public void setWatchAll(boolean watchAll) {
        if (!this.destroyed) {
            this.interestManager.setWatchAll(watchAll, this);
        }
    }

    @Override
    public void add(AEKey e) {
        if (!this.destroyed && this.myInterests.add(e)) {
            this.interestManager.put(e, this);
        }
    }

    @Override
    public void remove(AEKey o) {
        if (!this.destroyed && this.myInterests.remove(o)) {
            this.interestManager.remove(o, this);
        }
    }

    @Override
    public void reset() {
        this.setWatchAll(false);
        Iterator<AEKey> i = this.myInterests.iterator();
        while (i.hasNext()) {
            this.interestManager.remove(i.next(), this);
            i.remove();
        }
    }

    public void destroy() {
        this.reset();
        this.destroyed = true;
    }
}

