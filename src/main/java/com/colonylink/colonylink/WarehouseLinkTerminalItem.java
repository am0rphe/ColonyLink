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
        tooltip.add(Component.literal("§7Warehouse §f↔ §7AE2 ME bridge + Crafting Table + Domum encoder."));

        if (net.minecraft.client.gui.screens.Screen.hasShiftDown())
        {
            tooltip.add(Component.literal("§eSetup:").withStyle(ChatFormatting.YELLOW));
            tooltip.add(Component.literal("§8 1. Place on a §fcable bus §8(like ME Crafting Terminal)"));
            tooltip.add(Component.literal("§8 2. Insert a §fWarehouse Link Card §8in the dedicated slot"));
            tooltip.add(Component.literal("§8 3. Right-click to open the terminal"));
            tooltip.add(Component.literal("§7Features:").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal("§8  • §fCrafting Table §8tab — vanilla 3×3 crafting"));
            tooltip.add(Component.literal("§8  • §fDomum Encoder §8tab — encode Domum Patterns for AE2"));
            tooltip.add(Component.literal("§8  • §fWarehouse §8↔ §fME §8content visible side by side"));
            tooltip.add(Component.literal("§7Consumes §f1 AE2 channel §7· §f8 AE/t §7idle.")
                    .withStyle(ChatFormatting.GRAY));
        }
        else
        {
            tooltip.add(Component.literal("§8Hold §eShift §8for setup guide.")
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}