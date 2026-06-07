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
        tooltip.add(Component.translatable("colonylink.redir_item.line1"));

        if (net.minecraft.client.gui.screens.Screen.hasShiftDown())
        {
            tooltip.add(Component.translatable("colonylink.tip.setup").withStyle(
                    net.minecraft.ChatFormatting.YELLOW));
            tooltip.add(Component.translatable("colonylink.redir_item.setup1"));
            tooltip.add(Component.translatable("colonylink.redir_item.setup2"));
            tooltip.add(Component.translatable("colonylink.redir_item.setup3"));
            tooltip.add(Component.translatable("colonylink.redir_item.setup4"));
            tooltip.add(Component.translatable("colonylink.redir_item.setup5"));
            tooltip.add(Component.translatable("colonylink.redir_item.interactions").withStyle(
                    net.minecraft.ChatFormatting.GRAY));
            tooltip.add(Component.translatable("colonylink.redir_item.int1"));
            tooltip.add(Component.translatable("colonylink.redir_item.int2"));
            tooltip.add(Component.translatable("colonylink.redir_item.int3"));
        }
        else
        {
            tooltip.add(Component.translatable("colonylink.tip.hold_shift_setup")
                    .withStyle(net.minecraft.ChatFormatting.DARK_GRAY));
        }
    }
}