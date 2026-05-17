/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Holder
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.Item$TooltipContext
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.SwordItem
 *  net.minecraft.world.item.Tier
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.item.enchantment.Enchantment
 *  net.minecraft.world.item.enchantment.Enchantments
 */
package appeng.items.tools.fluix;

import appeng.hooks.IntrinsicEnchantItem;
import appeng.items.tools.fluix.FluixToolType;
import appeng.items.tools.fluix.IntrinsicEnchantment;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

public class FluixSwordItem
extends SwordItem
implements IntrinsicEnchantItem {
    private final IntrinsicEnchantment intrinsicEnchantment = new IntrinsicEnchantment((ResourceKey<Enchantment>)Enchantments.LOOTING, 1);

    public FluixSwordItem(Item.Properties props) {
        super(FluixToolType.FLUIX.getToolTier(), props.attributes(FluixSwordItem.createAttributes((Tier)FluixToolType.FLUIX.getToolTier(), (int)3, (float)-2.4f)));
    }

    @Override
    public int getIntrinsicEnchantLevel(ItemStack stack, Holder<Enchantment> enchantment) {
        return this.intrinsicEnchantment.getLevel(enchantment);
    }

    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        this.intrinsicEnchantment.appendHoverText(context, tooltipComponents);
    }

    public boolean isFoil(ItemStack stack) {
        return true;
    }
}

