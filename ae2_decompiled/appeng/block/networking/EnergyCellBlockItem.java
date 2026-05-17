/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.Item$TooltipContext
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.level.block.Block
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.OnlyIn
 */
package appeng.block.networking;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.ids.AEComponents;
import appeng.api.implementations.items.IAEItemPowerStorage;
import appeng.block.AEBaseBlockItem;
import appeng.block.networking.EnergyCellBlock;
import appeng.core.localization.Tooltips;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class EnergyCellBlockItem
extends AEBaseBlockItem
implements IAEItemPowerStorage {
    public EnergyCellBlockItem(Block block, Item.Properties props) {
        super(block, props);
    }

    @Override
    @OnlyIn(value=Dist.CLIENT)
    public void addCheckedInformation(ItemStack stack, Item.TooltipContext context, List<Component> lines, TooltipFlag advancedTooltips) {
        double storedEnergy = this.getAECurrentPower(stack);
        double maxEnergy = this.getAEMaxPower(stack);
        lines.add(Tooltips.energyStorageComponent(storedEnergy, maxEnergy));
    }

    @Override
    public double injectAEPower(ItemStack is, double amount, Actionable mode) {
        double internalCurrentPower = this.getAECurrentPower(is);
        double internalMaxPower = this.getAEMaxPower(is);
        double required = internalMaxPower - internalCurrentPower;
        double overflow = Math.max(0.0, Math.min(amount - required, amount));
        if (mode == Actionable.MODULATE) {
            double toAdd = Math.min(required, amount);
            double newPowerStored = internalCurrentPower + toAdd;
            this.setAECurrentPower(is, newPowerStored);
        }
        return overflow;
    }

    @Override
    public double extractAEPower(ItemStack is, double amount, Actionable mode) {
        double internalCurrentPower = this.getAECurrentPower(is);
        double fulfillable = Math.min(amount, internalCurrentPower);
        if (mode == Actionable.MODULATE) {
            double newPowerStored = internalCurrentPower - fulfillable;
            this.setAECurrentPower(is, newPowerStored);
        }
        return fulfillable;
    }

    @Override
    public double getAEMaxPower(ItemStack is) {
        return this.getMaxEnergyCapacity();
    }

    @Override
    public double getAECurrentPower(ItemStack is) {
        return (Double)is.getOrDefault(AEComponents.STORED_ENERGY, (Object)0.0);
    }

    @Override
    public AccessRestriction getPowerFlow(ItemStack is) {
        return AccessRestriction.WRITE;
    }

    @Override
    public double getChargeRate(ItemStack stack) {
        return ((EnergyCellBlock)this.getBlock()).getChargeRate();
    }

    private double getMaxEnergyCapacity() {
        return ((EnergyCellBlock)this.getBlock()).getMaxPower();
    }

    private void setAECurrentPower(ItemStack is, double amt) {
        if (amt < 1.0E-5) {
            is.remove(AEComponents.STORED_ENERGY);
        } else {
            is.set(AEComponents.STORED_ENERGY, (Object)amt);
        }
    }
}

