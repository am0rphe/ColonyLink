/*
 * Decompiled with CFR 0.152.
 */
package appeng.init.internal;

import appeng.api.networking.GridServices;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.pathing.IPathingService;
import appeng.api.networking.spatial.ISpatialService;
import appeng.api.networking.storage.IStorageService;
import appeng.api.networking.ticking.ITickManager;
import appeng.me.service.CraftingService;
import appeng.me.service.EnergyService;
import appeng.me.service.P2PService;
import appeng.me.service.PathingService;
import appeng.me.service.SpatialPylonService;
import appeng.me.service.StatisticsService;
import appeng.me.service.StorageService;
import appeng.me.service.TickManagerService;

public final class InitGridServices {
    private InitGridServices() {
    }

    public static void init() {
        GridServices.register(ITickManager.class, TickManagerService.class);
        GridServices.register(IPathingService.class, PathingService.class);
        GridServices.register(IEnergyService.class, EnergyService.class);
        GridServices.register(IStorageService.class, StorageService.class);
        GridServices.register(P2PService.class, P2PService.class);
        GridServices.register(ISpatialService.class, SpatialPylonService.class);
        GridServices.register(ICraftingService.class, CraftingService.class);
        GridServices.register(StatisticsService.class, StatisticsService.class);
    }
}

