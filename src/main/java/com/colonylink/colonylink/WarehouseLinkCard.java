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
        tooltip.add(Component.literal("§7Links a Redirector to the colony Warehouse."));
        if (net.minecraft.client.gui.screens.Screen.hasShiftDown())
        {
            tooltip.add(Component.literal("§8  • Insert into the Redirector GUI slot."));
            tooltip.add(Component.literal("§8  • Enables §fCheck Warehouse §8in the Clipboard."));
            tooltip.add(Component.literal("§8  • Send pulls from Warehouse racks first."));
        }
        else
        {
            tooltip.add(Component.literal("§8Hold §eShift §8for details."));
        }
    }
}