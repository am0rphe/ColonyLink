package com.colonylink.colonylink;

import appeng.api.parts.IPartItem;
import appeng.api.parts.PartHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;

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
        // PartHelper.usePartItem handles all the logic:
        // - checks if the targeted block is a cable bus (IPartHost)
        // - calls IPartHost.addPart(this, side, player)
        // - plays placement sound
        return PartHelper.usePartItem(ctx);
    }

    // ── Tooltip ───────────────────────────────────────────────────────────────

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx,
                                List<Component> tooltip, TooltipFlag flag)
    {
        super.appendHoverText(stack, ctx, tooltip, flag);
        tooltip.add(Component.literal("§8──────────────────"));
        tooltip.add(Component.literal("§7Bidirectional bridge: MineColonies Warehouse ↔ AE2 ME"));
        tooltip.add(Component.literal("§7Includes a 3×3 crafting table."));
        tooltip.add(Component.literal("§8──────────────────"));
        tooltip.add(Component.literal("§eSetup:"));
        tooltip.add(Component.literal("§8 1. §fPlace on a cable bus §8(like the ME Crafting Terminal)"));
        tooltip.add(Component.literal("§8 2. §fInsert a §fWarehouse Link Card §8in the dedicated slot"));
        tooltip.add(Component.literal("§8 3. §fRight-click §8to open the terminal"));
        tooltip.add(Component.literal("§8──────────────────"));
        tooltip.add(Component.literal("§7Consumes §f1 AE2 channel §7· §f8 AE/t §7idle"));
    }
}