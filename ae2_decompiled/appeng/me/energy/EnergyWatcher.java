/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Preconditions
 */
package appeng.me.energy;

import appeng.api.networking.energy.IEnergyWatcher;
import appeng.api.networking.energy.IEnergyWatcherNode;
import appeng.me.energy.EnergyThreshold;
import appeng.me.service.EnergyService;
import com.google.common.base.Preconditions;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class EnergyWatcher
implements IEnergyWatcher {
    private final EnergyService service;
    private final IEnergyWatcherNode watcherHost;
    private final Set<EnergyThreshold> myInterests = new HashSet<EnergyThreshold>();

    public EnergyWatcher(EnergyService service, IEnergyWatcherNode host) {
        this.service = service;
        this.watcherHost = host;
    }

    public void post(EnergyService service) {
        this.watcherHost.onThresholdPass(service);
    }

    public IEnergyWatcherNode getHost() {
        return this.watcherHost;
    }

    @Override
    public boolean add(double amount) {
        Preconditions.checkArgument((amount >= 0.0 ? 1 : 0) != 0, (Object)"amount must be >= 0");
        EnergyThreshold eh = new EnergyThreshold(amount, this);
        if (this.myInterests.contains(eh)) {
            return false;
        }
        return this.service.registerEnergyInterest(eh) && this.myInterests.add(eh);
    }

    @Override
    public boolean remove(double amount) {
        Preconditions.checkArgument((amount >= 0.0 ? 1 : 0) != 0, (Object)"amount must be >= 0");
        EnergyThreshold eh = new EnergyThreshold(amount, this);
        return this.myInterests.remove(eh) && this.service.unregisterEnergyInterest(eh);
    }

    @Override
    public void reset() {
        Iterator<EnergyThreshold> iterator = this.myInterests.iterator();
        while (iterator.hasNext()) {
            EnergyThreshold threshold = iterator.next();
            this.service.unregisterEnergyInterest(threshold);
            iterator.remove();
        }
    }
}

