/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.Item$TooltipContext
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.item.context.UseOnContext
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.OnlyIn
 */
package appeng.items.materials;

import appeng.api.parts.IPartHost;
import appeng.api.parts.SelectedPart;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.upgrades.Upgrades;
import appeng.core.localization.ButtonToolTips;
import appeng.core.localization.PlayerMessages;
import appeng.items.AEBaseItem;
import appeng.util.InteractionUtil;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class UpgradeCardItem
extends AEBaseItem {
    public UpgradeCardItem(Item.Properties properties) {
        super(properties);
    }

    @OnlyIn(value=Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> lines, TooltipFlag advancedTooltips) {
        super.appendHoverText(stack, context, lines, advancedTooltips);
        List<Component> supportedBy = Upgrades.getTooltipLinesForCard((ItemLike)this);
        if (!supportedBy.isEmpty()) {
            lines.add((Component)ButtonToolTips.SupportedBy.text());
            lines.addAll(supportedBy);
        }
    }

    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Player player = context.getPlayer();
        InteractionHand hand = context.getHand();
        if (player != null && InteractionUtil.isInAlternateUseMode(player)) {
            BlockEntity te = context.getLevel().getBlockEntity(context.getClickedPos());
            IUpgradeInventory upgrades = null;
            if (te instanceof IPartHost) {
                SelectedPart sp = ((IPartHost)te).selectPartWorld(context.getClickLocation());
                if (sp.part instanceof IUpgradeableObject) {
                    upgrades = ((IUpgradeableObject)((Object)sp.part)).getUpgrades();
                }
            } else if (te instanceof IUpgradeableObject) {
                upgrades = ((IUpgradeableObject)te).getUpgrades();
            }
            if (upgrades != null && upgrades.size() > 0) {
                ItemStack heldStack = player.getItemInHand(hand);
                boolean isFull = true;
                for (int i = 0; i < upgrades.size(); ++i) {
                    if (!upgrades.getStackInSlot(i).isEmpty()) continue;
                    isFull = false;
                    break;
                }
                if (isFull) {
                    player.displayClientMessage((Component)PlayerMessages.MaxUpgradesInstalled.text(), true);
                    return InteractionResult.FAIL;
                }
                int maxInstalled = upgrades.getMaxInstalled((ItemLike)heldStack.getItem());
                int installed = upgrades.getInstalledUpgrades((ItemLike)heldStack.getItem());
                if (maxInstalled <= 0) {
                    player.displayClientMessage((Component)PlayerMessages.UnsupportedUpgrade.text(), true);
                    return InteractionResult.FAIL;
                }
                if (installed >= maxInstalled) {
                    player.displayClientMessage((Component)PlayerMessages.MaxUpgradesOfTypeInstalled.text(), true);
                    return InteractionResult.FAIL;
                }
                if (player.getCommandSenderWorld().isClientSide()) {
                    return InteractionResult.PASS;
                }
                player.setItemInHand(hand, upgrades.addItems(heldStack));
                return InteractionResult.sidedSuccess((boolean)player.getCommandSenderWorld().isClientSide());
            }
        }
        return super.onItemUseFirst(stack, context);
    }
}

