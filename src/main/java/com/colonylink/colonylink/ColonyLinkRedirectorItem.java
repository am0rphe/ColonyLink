package com.colonylink.colonylink;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class ColonyLinkRedirectorItem extends BlockItem
{
    public ColonyLinkRedirectorItem()
    {
        super(ColonyLinkRegistry.REDIRECTOR_BLOCK.get(), new Properties());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag)
    {
        super.appendHoverText(stack, context, tooltip, flag);

        tooltip.add(Component.literal("§8──────────────────"));
        tooltip.add(Component.literal("§7Bridges ME network ↔ MineColonies builder inventory"));
        tooltip.add(Component.literal("§8──────────────────"));
        tooltip.add(Component.literal("§eSetup:"));
        tooltip.add(Component.literal("§8 1. §fLink the §fColonyLink Wand §8to a §fWireless Access Point"));
        tooltip.add(Component.literal("§8 2. §fSneak + Right-click §8a §fBuilder's Hut §8with the Wand"));
        tooltip.add(Component.literal("§8 3. §fPlace the Redirector §8adjacent to an §fAE2 cable"));
        tooltip.add(Component.literal("§8 4. §fSneak + Right-click §8the Redirector with the Wand"));
        tooltip.add(Component.literal("§8 5. §fRight-click §8(air) with the Wand → resource GUI"));
        tooltip.add(Component.literal("§8──────────────────"));
        tooltip.add(Component.literal("§7Interactions:"));
        tooltip.add(Component.literal("§8  §fRight-click §8(empty hand) → open buffer GUI"));
        tooltip.add(Component.literal("§8  §fAE2 Wrench §8→ show status"));
        tooltip.add(Component.literal("§8  §fSneak + AE2 Wrench §8→ remove block"));
    }
}