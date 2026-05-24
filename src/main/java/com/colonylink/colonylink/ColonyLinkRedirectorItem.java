package com.colonylink.colonylink;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;

public class ColonyLinkRedirectorItem extends BlockItem
{
    public ColonyLinkRedirectorItem()
    {
        super(ColonyLinkRegistry.REDIRECTOR_BLOCK.get(), new Properties());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                List<Component> tooltip, TooltipFlag flag)
    {
        tooltip.add(Component.literal("§7Bridges §fME network §7↔ §fMineColonies §7builder."));

        if (net.minecraft.client.gui.screens.Screen.hasShiftDown())
        {
            tooltip.add(Component.literal("§eSetup:").withStyle(
                    net.minecraft.ChatFormatting.YELLOW));
            tooltip.add(Component.literal("§8 1. Link §fClipboard §8to a §fWireless Access Point"));
            tooltip.add(Component.literal("§8 2. Sneak+click a §fBuilder's Hut §8with the Clipboard"));
            tooltip.add(Component.literal("§8 3. Place Redirector adjacent to an §fAE2 cable"));
            tooltip.add(Component.literal("§8 4. Sneak+click §fthe Redirector §8with the Clipboard"));
            tooltip.add(Component.literal("§8 5. Right-click (air) with Clipboard → resource GUI"));
            tooltip.add(Component.literal("§7Interactions:").withStyle(
                    net.minecraft.ChatFormatting.GRAY));
            tooltip.add(Component.literal("§8  §fRight-click §8(empty hand) → open buffer GUI"));
            tooltip.add(Component.literal("§8  §fAE2 Wrench §8→ show status / §fSneak §8→ remove"));
            tooltip.add(Component.literal("§8  Insert §fDomum Patterns §8in buffer for AE2 crafting"));
        }
        else
        {
            tooltip.add(Component.literal("§8Hold §eShift §8for setup guide.")
                    .withStyle(net.minecraft.ChatFormatting.DARK_GRAY));
        }
    }
}