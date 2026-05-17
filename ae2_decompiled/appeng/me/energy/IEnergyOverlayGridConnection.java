/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.ApiStatus$Internal
 */
package appeng.me.energy;

import appeng.api.networking.IGridNodeService;
import appeng.me.service.EnergyService;
import java.util.Collection;
import org.jetbrains.annotations.ApiStatus;

@FunctionalInterface
@ApiStatus.Internal
public interface IEnergyOverlayGridConnection
extends IGridNodeService {
    public Collection<EnergyService> connectedEnergyServices();
}

