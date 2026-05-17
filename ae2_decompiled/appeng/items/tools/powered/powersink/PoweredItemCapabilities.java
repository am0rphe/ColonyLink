/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.ItemStack
 *  net.neoforged.neoforge.energy.IEnergyStorage
 */
package appeng.items.tools.powered.powersink;

import appeng.api.config.Actionable;
import appeng.api.config.PowerUnit;
import appeng.api.implementations.items.IAEItemPowerStorage;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.energy.IEnergyStorage;

public class PoweredItemCapabilities
implements IEnergyStorage {
    private final ItemStack is;
    private final IAEItemPowerStorage item;

    public PoweredItemCapabilities(ItemStack is, IAEItemPowerStorage item) {
        this.is = is;
        this.item = item;
    }

    public int receiveEnergy(int maxReceive, boolean simulate) {
        double convertedOffer = PowerUnit.FE.convertTo(PowerUnit.AE, maxReceive);
        double overflow = this.item.injectAEPower(this.is, convertedOffer, simulate ? Actionable.SIMULATE : Actionable.MODULATE);
        return maxReceive - (int)PowerUnit.AE.convertTo(PowerUnit.FE, overflow);
    }

    public int extractEnergy(int maxExtract, boolean simulate) {
        return 0;
    }

    public int getEnergyStored() {
        return (int)PowerUnit.AE.convertTo(PowerUnit.FE, this.item.getAECurrentPower(this.is));
    }

    public int getMaxEnergyStored() {
        return (int)PowerUnit.AE.convertTo(PowerUnit.FE, this.item.getAEMaxPower(this.is));
    }

    public boolean canExtract() {
        return false;
    }

    public boolean canReceive() {
        return true;
    }
}

