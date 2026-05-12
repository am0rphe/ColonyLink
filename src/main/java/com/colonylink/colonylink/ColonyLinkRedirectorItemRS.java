package com.colonylink.colonylink;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import com.refinedmods.refinedstorage.common.support.NetworkNodeBlockItem;

import java.util.List;

public class ColonyLinkRedirectorItemRS extends NetworkNodeBlockItem
{
    public ColonyLinkRedirectorItemRS()
    {
        super(ColonyLinkRegistry.REDIRECTOR_BLOCK_RS.get(),
                new Properties(),
                Component.translatable("block.colonylink.colony_link_redirector_rs"));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag)
    {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.literal("§8──────────────────"));
        tooltip.add(Component.literal("§7Bridges RS2 network ↔ MineColonies builder inventory"));
        tooltip.add(Component.literal("§8──────────────────"));
        tooltip.add(Component.literal("§eSetup:"));
        tooltip.add(Component.literal("§8 1. §fSneak + Right-click §8a §fWireless Transmitter §8with the RS Wand"));
        tooltip.add(Component.literal("§8 2. §fSneak + Right-click §8a §fBuilder's Hut §8with the RS Wand"));
        tooltip.add(Component.literal("§8 3. §fPlace the Redirector RS §8adjacent to the §fRS2 network"));
        tooltip.add(Component.literal("§8 4. §fSneak + Right-click §8the Redirector with the RS Wand"));
        tooltip.add(Component.literal("§8 5. §fRight-click §8(air) with the RS Wand → resource GUI"));
        tooltip.add(Component.literal("§8──────────────────"));
        tooltip.add(Component.literal("§7Interactions:"));
        tooltip.add(Component.literal("§8  §fRight-click §8(empty hand) → open buffer GUI"));
        tooltip.add(Component.literal("§8  §fWrench §8→ show status / remove"));
    }
}