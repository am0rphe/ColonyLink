/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResultHolder
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 */
package appeng.block.crafting;

import appeng.block.AEBaseBlockItem;
import appeng.core.definitions.AEBlocks;
import appeng.recipes.game.CraftingUnitTransformRecipe;
import appeng.util.InteractionUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class CraftingBlockItem
extends AEBaseBlockItem {
    public CraftingBlockItem(Block id, Item.Properties props) {
        super(id, props);
    }

    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (InteractionUtil.isInAlternateUseMode(player)) {
            ItemStack stack = player.getItemInHand(hand);
            ItemStack removedUpgrade = CraftingUnitTransformRecipe.getRemovedUpgrade(level, this.getBlock());
            if (removedUpgrade.isEmpty()) {
                return super.use(level, player, hand);
            }
            int itemCount = stack.getCount();
            player.setItemInHand(hand, ItemStack.EMPTY);
            Inventory inv = player.getInventory();
            inv.placeItemBackInInventory(removedUpgrade.copyWithCount(removedUpgrade.getCount() * itemCount));
            inv.placeItemBackInInventory(AEBlocks.CRAFTING_UNIT.stack(itemCount));
            return InteractionResultHolder.sidedSuccess((Object)player.getItemInHand(hand), (boolean)level.isClientSide());
        }
        return super.use(level, player, hand);
    }
}

