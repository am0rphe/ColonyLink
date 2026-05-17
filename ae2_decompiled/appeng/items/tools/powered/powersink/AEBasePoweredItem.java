/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.util.Mth
 *  net.minecraft.world.item.CreativeModeTab$ItemDisplayParameters
 *  net.minecraft.world.item.CreativeModeTab$Output
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.Item$TooltipContext
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.level.ItemLike
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.OnlyIn
 */
package appeng.items.tools.powered.powersink;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.ids.AEComponents;
import appeng.api.implementations.items.IAEItemPowerStorage;
import appeng.core.localization.Tooltips;
import appeng.items.AEBaseItem;
import java.util.List;
import java.util.function.DoubleSupplier;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ItemLike;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public abstract class AEBasePoweredItem
extends AEBaseItem
implements IAEItemPowerStorage {
    private static final double MIN_POWER = 1.0E-4;
    private final DoubleSupplier powerCapacity;

    public AEBasePoweredItem(DoubleSupplier powerCapacity, Item.Properties props) {
        super(props);
        this.powerCapacity = powerCapacity;
    }

    @OnlyIn(value=Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> lines, TooltipFlag advancedTooltips) {
        double storedEnergy = this.getAECurrentPower(stack);
        double energyCapacity = this.getAEMaxPower(stack);
        lines.add(Tooltips.energyStorageComponent(storedEnergy, energyCapacity));
    }

    @Override
    public void addToMainCreativeTab(CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output) {
        super.addToMainCreativeTab(parameters, output);
        ItemStack charged = new ItemStack((ItemLike)this, 1);
        this.injectAEPower(charged, this.getAEMaxPower(charged), Actionable.MODULATE);
        output.accept(charged);
    }

    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged || !ItemStack.isSameItem((ItemStack)oldStack, (ItemStack)newStack);
    }

    public int getBarWidth(ItemStack stack) {
        double filled = this.getAECurrentPower(stack) / this.getAEMaxPower(stack);
        return Mth.clamp((int)((int)Math.round(filled * 13.0)), (int)0, (int)13);
    }

    public int getBarColor(ItemStack stack) {
        return Mth.hsvToRgb((float)0.33333334f, (float)1.0f, (float)1.0f);
    }

    @Override
    public double injectAEPower(ItemStack stack, double amount, Actionable mode) {
        double maxStorage = this.getAEMaxPower(stack);
        double currentStorage = this.getAECurrentPower(stack);
        double required = maxStorage - currentStorage;
        double overflow = Math.max(0.0, Math.min(amount - required, amount));
        if (mode == Actionable.MODULATE) {
            double toAdd = Math.min(amount, required);
            this.setAECurrentPower(stack, currentStorage + toAdd);
        }
        return overflow;
    }

    @Override
    public double extractAEPower(ItemStack stack, double amount, Actionable mode) {
        double currentStorage = this.getAECurrentPower(stack);
        double fulfillable = Math.min(amount, currentStorage);
        if (mode == Actionable.MODULATE) {
            this.setAECurrentPower(stack, currentStorage - fulfillable);
        }
        return fulfillable;
    }

    @Override
    public double getAEMaxPower(ItemStack stack) {
        return (Double)stack.getOrDefault(AEComponents.ENERGY_CAPACITY, (Object)this.powerCapacity.getAsDouble());
    }

    protected final void setAEMaxPower(ItemStack stack, double maxPower) {
        double defaultCapacity = this.powerCapacity.getAsDouble();
        if (Math.abs(maxPower - defaultCapacity) < 1.0E-4) {
            stack.remove(AEComponents.ENERGY_CAPACITY);
        } else {
            stack.set(AEComponents.ENERGY_CAPACITY, (Object)maxPower);
        }
        double currentPower = this.getAECurrentPower(stack);
        if (currentPower > maxPower) {
            this.setAECurrentPower(stack, maxPower);
        }
    }

    protected final void setAEMaxPowerMultiplier(ItemStack stack, int multiplier) {
        multiplier = Mth.clamp((int)multiplier, (int)1, (int)100);
        this.setAEMaxPower(stack, (double)multiplier * this.powerCapacity.getAsDouble());
    }

    protected final void resetAEMaxPower(ItemStack stack) {
        this.setAEMaxPower(stack, this.powerCapacity.getAsDouble());
    }

    @Override
    public double getAECurrentPower(ItemStack is) {
        return (Double)is.getOrDefault(AEComponents.STORED_ENERGY, (Object)0.0);
    }

    protected final void setAECurrentPower(ItemStack stack, double power) {
        if (power < 1.0E-4) {
            stack.remove(AEComponents.STORED_ENERGY);
        } else {
            stack.set(AEComponents.STORED_ENERGY, (Object)power);
        }
    }

    @Override
    public AccessRestriction getPowerFlow(ItemStack is) {
        return AccessRestriction.WRITE;
    }
}

