/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.neoforged.neoforge.energy.IEnergyStorage
 */
package appeng.parts.networking;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.PowerUnit;
import appeng.api.networking.IGrid;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.api.util.AECableType;
import appeng.blockentity.powersink.IExternalPowerSink;
import appeng.core.AppEng;
import appeng.helpers.ForgeEnergyAdapter;
import appeng.items.parts.PartModels;
import appeng.parts.AEBasePart;
import appeng.parts.PartModel;
import net.neoforged.neoforge.energy.IEnergyStorage;

public class EnergyAcceptorPart
extends AEBasePart
implements IExternalPowerSink {
    @PartModels
    private static final IPartModel MODELS = new PartModel(AppEng.makeId("part/energy_acceptor"));
    private ForgeEnergyAdapter forgeEnergyAdapter;

    public EnergyAcceptorPart(IPartItem<?> partItem) {
        super(partItem);
        this.getMainNode().setIdlePowerUsage(0.0);
        this.forgeEnergyAdapter = new ForgeEnergyAdapter(this);
    }

    public IEnergyStorage getEnergyStorage() {
        return this.forgeEnergyAdapter;
    }

    @Override
    public void getBoxes(IPartCollisionHelper bch) {
        bch.addBox(2.0, 2.0, 14.0, 14.0, 14.0, 16.0);
        bch.addBox(4.0, 4.0, 12.0, 12.0, 12.0, 14.0);
    }

    @Override
    public float getCableConnectionLength(AECableType cable) {
        return 2.0f;
    }

    @Override
    public IPartModel getStaticModels() {
        return MODELS;
    }

    @Override
    public final double getExternalPowerDemand(PowerUnit externalUnit, double maxPowerRequired) {
        return PowerUnit.AE.convertTo(externalUnit, Math.max(0.0, this.getFunnelPowerDemand(externalUnit.convertTo(PowerUnit.AE, maxPowerRequired))));
    }

    protected double getFunnelPowerDemand(double maxRequired) {
        IGrid grid = this.getMainNode().getGrid();
        if (grid != null) {
            return grid.getEnergyService().getEnergyDemand(maxRequired);
        }
        return 0.0;
    }

    @Override
    public final double injectExternalPower(PowerUnit input, double amt, Actionable mode) {
        return PowerUnit.AE.convertTo(input, this.funnelPowerIntoStorage(input.convertTo(PowerUnit.AE, amt), mode));
    }

    protected double funnelPowerIntoStorage(double power, Actionable mode) {
        IGrid grid = this.getMainNode().getGrid();
        if (grid != null) {
            return grid.getEnergyService().injectPower(power, mode);
        }
        return power;
    }

    @Override
    public final double injectAEPower(double amt, Actionable mode) {
        return amt;
    }

    @Override
    public final double getAEMaxPower() {
        return 0.0;
    }

    @Override
    public final double getAECurrentPower() {
        return 0.0;
    }

    @Override
    public final boolean isAEPublicPowerStorage() {
        return false;
    }

    @Override
    public final AccessRestriction getPowerFlow() {
        return AccessRestriction.READ_WRITE;
    }

    @Override
    public final double extractAEPower(double amt, Actionable mode, PowerMultiplier multiplier) {
        return 0.0;
    }
}

