/*
 * Decompiled with CFR 0.152.
 */
package appeng.me.helpers;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.IActionHost;

public final class ActionHostEnergySource
implements IEnergySource {
    private final IActionHost actionHost;

    public ActionHostEnergySource(IActionHost actionHost) {
        this.actionHost = actionHost;
    }

    @Override
    public double extractAEPower(double amt, Actionable mode, PowerMultiplier usePowerMultiplier) {
        IGridNode node = this.actionHost.getActionableNode();
        if (node != null && node.isActive()) {
            IEnergyService energyService = node.getGrid().getEnergyService();
            return energyService.extractAEPower(amt, mode, usePowerMultiplier);
        }
        return 0.0;
    }
}

