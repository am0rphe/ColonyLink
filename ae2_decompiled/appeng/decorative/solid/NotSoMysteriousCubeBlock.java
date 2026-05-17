/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.item.Item$TooltipContext
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.TooltipFlag
 */
package appeng.decorative.solid;

import appeng.block.misc.MysteriousCubeBlock;
import appeng.core.localization.GuiText;
import appeng.core.localization.Tooltips;
import appeng.decorative.AEDecorativeBlock;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class NotSoMysteriousCubeBlock
extends AEDecorativeBlock {
    public NotSoMysteriousCubeBlock() {
        super(MysteriousCubeBlock.PROPERTIES);
    }

    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add((Component)Tooltips.of(GuiText.NotSoMysteriousQuote, Tooltips.QUOTE_TEXT, new Object[0]));
    }
}

