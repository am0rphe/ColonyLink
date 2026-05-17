/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.item.BlockItem
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.Item$TooltipContext
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.level.block.Block
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.OnlyIn
 */
package appeng.block;

import appeng.block.AEBaseBlock;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class AEBaseBlockItem
extends BlockItem {
    private final AEBaseBlock blockType;

    public AEBaseBlockItem(Block id, Item.Properties props) {
        super(id, props);
        this.blockType = (AEBaseBlock)id;
    }

    @OnlyIn(value=Dist.CLIENT)
    public final void appendHoverText(ItemStack itemStack, Item.TooltipContext context, List<Component> toolTip, TooltipFlag advancedTooltips) {
        this.addCheckedInformation(itemStack, context, toolTip, advancedTooltips);
    }

    @OnlyIn(value=Dist.CLIENT)
    public void addCheckedInformation(ItemStack itemStack, Item.TooltipContext context, List<Component> toolTip, TooltipFlag advancedTooltips) {
        this.blockType.appendHoverText(itemStack, context, toolTip, advancedTooltips);
    }

    public boolean isBookEnchantable(ItemStack itemstack1, ItemStack itemstack2) {
        return false;
    }

    public String getDescriptionId(ItemStack is) {
        return this.blockType.getDescriptionId();
    }
}

