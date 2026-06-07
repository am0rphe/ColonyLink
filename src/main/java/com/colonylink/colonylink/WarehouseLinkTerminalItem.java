package com.colonylink.colonylink;

import appeng.api.parts.IPartItem;
import appeng.api.parts.PartHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;

/**
 * Item for the Warehouse Link Terminal Part.
 *
 * Implements IPartItem<WarehouseLinkTerminalPart> so AE2 knows:
 *   - which Part class to instantiate (getPartClass / createPart)
 *   - how to place it on a cable bus (useOn → PartHelper.usePartItem)
 *
 * This item appears in the player's inventory and is placed on cable buses
 * exactly like the ME Crafting Terminal.
 */
public class WarehouseLinkTerminalItem extends Item
        implements IPartItem<WarehouseLinkTerminalPart>
{
    public WarehouseLinkTerminalItem()
    {
        super(new Item.Properties().stacksTo(1));
    }

    // ── IPartItem ─────────────────────────────────────────────────────────────

    @Override
    public Class<WarehouseLinkTerminalPart> getPartClass()
    {
        return WarehouseLinkTerminalPart.class;
    }

    @Override
    public WarehouseLinkTerminalPart createPart()
    {
        return new WarehouseLinkTerminalPart(this);
    }

    // ── Placement on cable bus ────────────────────────────────────────────────

    @Override
    public InteractionResult useOn(UseOnContext ctx)
    {
        return PartHelper.usePartItem(ctx);
    }

    // ── Tooltip ───────────────────────────────────────────────────────────────

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, TooltipContext ctx,
                                List<Component> tooltip, TooltipFlag flag)
    {
        tooltip.add(Component.translatable("colonylink.term_item.line1"));

        if (net.minecraft.client.gui.screens.Screen.hasShiftDown())
        {
            tooltip.add(Component.translatable("colonylink.tip.setup").withStyle(ChatFormatting.YELLOW));
            tooltip.add(Component.translatable("colonylink.term_item.setup1"));
            tooltip.add(Component.translatable("colonylink.term_item.setup2"));
            tooltip.add(Component.translatable("colonylink.term_item.setup3"));
            tooltip.add(Component.translatable("colonylink.term_item.features").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("colonylink.term_item.feat1"));
            tooltip.add(Component.translatable("colonylink.term_item.feat2"));
            tooltip.add(Component.translatable("colonylink.term_item.feat3"));
            tooltip.add(Component.translatable("colonylink.term_item.consumes")
                    .withStyle(ChatFormatting.GRAY));
        }
        else
        {
            tooltip.add(Component.translatable("colonylink.tip.hold_shift_setup")
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}