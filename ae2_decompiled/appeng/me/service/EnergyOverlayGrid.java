/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet
 *  org.jetbrains.annotations.Nullable
 */
package appeng.me.service;

import appeng.api.networking.energy.IPassiveEnergyGenerator;
import appeng.core.AELog;
import appeng.me.energy.IEnergyOverlayGridConnection;
import appeng.me.service.EnergyService;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import org.jetbrains.annotations.Nullable;

class EnergyOverlayGrid {
    private static final Comparator<EnergyService> SERVICE_COMPARATOR = Comparator.comparingDouble(EnergyService::getMaxStoredPower).reversed();
    final List<EnergyService> energyServices;
    @Nullable
    private IPassiveEnergyGenerator currentPassiveGenerator;

    private EnergyOverlayGrid(List<EnergyService> energyServices) {
        this.energyServices = energyServices;
    }

    void invalidate() {
        this.currentPassiveGenerator = null;
        for (EnergyService service : this.energyServices) {
            service.overlayGrid = null;
        }
    }

    @Nullable
    public IPassiveEnergyGenerator getCurrentPassiveGenerator() {
        return this.currentPassiveGenerator;
    }

    public void setCurrentPassiveGenerator(@Nullable IPassiveEnergyGenerator currentPassiveGenerator) {
        this.currentPassiveGenerator = currentPassiveGenerator;
    }

    static void buildCache(EnergyService startingService) {
        ReferenceOpenHashSet connectedServices = new ReferenceOpenHashSet();
        ObjectArrayList services = new ObjectArrayList();
        services.add((Object)startingService);
        while (!services.isEmpty()) {
            EnergyService service = (EnergyService)services.pop();
            if (!connectedServices.add((Object)service)) continue;
            for (IEnergyOverlayGridConnection provider : service.getOverlayGridConnections()) {
                services.addAll(provider.connectedEnergyServices());
            }
        }
        ArrayList<EnergyService> sortedServices = new ArrayList<EnergyService>((Collection<EnergyService>)connectedServices);
        sortedServices.sort(SERVICE_COMPARATOR);
        EnergyOverlayGrid overlayGrid = new EnergyOverlayGrid(List.copyOf(sortedServices));
        for (EnergyService service : sortedServices) {
            if (service.overlayGrid != null) {
                AELog.error("Grid %s energy service already has a power graph assigned to it!", service.grid);
            }
            service.overlayGrid = overlayGrid;
        }
    }
}

