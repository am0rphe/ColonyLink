package com.colonylink.colonylink;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class WarehouseLinkCard extends Item
{
    public WarehouseLinkCard()
    {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag)
    {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.literal("§8──────────────────"));
        tooltip.add(Component.literal("§7Links the Colony Link Redirector to the colony Warehouse."));
        tooltip.add(Component.literal("§8──────────────────"));
        tooltip.add(Component.literal("§eUsage:"));
        tooltip.add(Component.literal("§8  Insert into the dedicated slot of the Redirector GUI."));
        tooltip.add(Component.literal("§8  Enables §fCheck Warehouse §8button in the Clipboard GUI."));
        tooltip.add(Component.literal("§8  Allows Send to pull items from Warehouse racks first."));
    }
}