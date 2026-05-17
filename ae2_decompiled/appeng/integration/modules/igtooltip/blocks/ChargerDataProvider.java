/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.network.chat.Component
 *  net.minecraft.util.Mth
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 */
package appeng.integration.modules.igtooltip.blocks;

import appeng.api.implementations.items.IAEItemPowerStorage;
import appeng.api.integrations.igtooltip.TooltipBuilder;
import appeng.api.integrations.igtooltip.TooltipContext;
import appeng.api.integrations.igtooltip.providers.BodyProvider;
import appeng.api.inventories.InternalInventory;
import appeng.blockentity.misc.ChargerBlockEntity;
import appeng.core.localization.InGameTooltip;
import appeng.util.Platform;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class ChargerDataProvider
implements BodyProvider<ChargerBlockEntity> {
    @Override
    public void buildTooltip(ChargerBlockEntity charger, TooltipContext context, TooltipBuilder tooltip) {
        InternalInventory chargerInventory = charger.getInternalInventory();
        ItemStack chargingItem = chargerInventory.getStackInSlot(0);
        if (!chargingItem.isEmpty()) {
            tooltip.addLine((Component)InGameTooltip.Contains.text(chargingItem.getHoverName().copy().withStyle(ChatFormatting.WHITE)));
            Item item = chargingItem.getItem();
            if (item instanceof IAEItemPowerStorage) {
                IAEItemPowerStorage powerStorage = (IAEItemPowerStorage)item;
                if (Platform.isChargeable(chargingItem)) {
                    int fillRate = Mth.floor((double)(powerStorage.getAECurrentPower(chargingItem) * 100.0 / powerStorage.getAEMaxPower(chargingItem)));
                    tooltip.addLine((Component)InGameTooltip.Charged.text(fillRate));
                }
            }
        }
    }
}

