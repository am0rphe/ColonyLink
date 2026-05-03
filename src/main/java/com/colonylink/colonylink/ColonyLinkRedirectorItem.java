package com.colonylink.colonylink;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;

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
        tooltip.add(Component.literal("§8 1. §fPlace §8next to a §fME Controller"));
        tooltip.add(Component.literal("§8 2. §fSneak + Right-click §8on face touching an inventory"));
        tooltip.add(Component.literal("§8 3. §fSneak + Right-click §8with §fColonyLink Wand §8to link"));
        tooltip.add(Component.literal("§8 4. §fRight-click §8a §fBuilder's Hut §8with Wand → GUI"));
        tooltip.add(Component.literal("§8──────────────────"));
        tooltip.add(Component.literal("§7Interactions:"));
        tooltip.add(Component.literal("§8  §fRight-click §8(empty hand) → show status"));
        tooltip.add(Component.literal("§8  §fSneak + Right-click §8(empty hand) → link inventory"));
        tooltip.add(Component.literal("§8  §fAE2 Wrench §8→ show status"));
        tooltip.add(Component.literal("§8  §fSneak + AE2 Wrench §8→ remove block"));
    }

    @Override
    public InteractionResult useOn(UseOnContext context)
    {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        BlockPos pos = context.getClickedPos();

        if (level.isClientSide() || player == null) return super.useOn(context);

        if (player.isShiftKeyDown())
        {
            var handler = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, null);
            if (handler != null)
            {
                context.getItemInHand().update(
                        net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                        net.minecraft.world.item.component.CustomData.EMPTY,
                        data -> {
                            var tag = data.copyTag();
                            tag.putInt("target_x", pos.getX());
                            tag.putInt("target_y", pos.getY());
                            tag.putInt("target_z", pos.getZ());
                            return net.minecraft.world.item.component.CustomData.of(tag);
                        }
                );
                player.sendSystemMessage(Component.literal("§aTarget inventory stored at " + pos.toShortString()));
                player.sendSystemMessage(Component.literal("§eNow place the redirector adjacent to a ME Controller."));
                return InteractionResult.SUCCESS;
            }
            else
            {
                player.sendSystemMessage(Component.literal("§cNo inventory found here!"));
                return InteractionResult.FAIL;
            }
        }

        return super.useOn(context);
    }

    @Override
    protected boolean placeBlock(BlockPlaceContext context, BlockState state)
    {
        boolean placed = super.placeBlock(context, state);

        if (placed)
        {
            Level level = context.getLevel();
            BlockPos pos = context.getClickedPos();

            var data = context.getItemInHand().get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
            if (data != null)
            {
                var tag = data.copyTag();
                if (tag.contains("target_x"))
                {
                    BlockPos targetPos = new BlockPos(
                            tag.getInt("target_x"),
                            tag.getInt("target_y"),
                            tag.getInt("target_z")
                    );

                    var be = level.getBlockEntity(pos);
                    if (be instanceof ColonyLinkRedirectorBlockEntity redirector)
                    {
                        redirector.setTargetInventoryPos(targetPos);
                        redirector.updateState();
                    }
                }
            }
        }

        return placed;
    }
}