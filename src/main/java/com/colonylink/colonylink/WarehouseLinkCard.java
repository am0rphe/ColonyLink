package com.colonylink.colonylink;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;

public class WarehouseLinkCard extends Item
{
    public WarehouseLinkCard()
    {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag)
    {
        tooltip.add(Component.translatable("colonylink.card.line1"));
        if (net.minecraft.client.gui.screens.Screen.hasShiftDown())
        {
            tooltip.add(Component.translatable("colonylink.card.insert"));
            tooltip.add(Component.translatable("colonylink.card.enables"));
            tooltip.add(Component.translatable("colonylink.card.send"));
        }
        else
        {
            tooltip.add(Component.translatable("colonylink.tip.hold_shift_details"));
        }
    }
}