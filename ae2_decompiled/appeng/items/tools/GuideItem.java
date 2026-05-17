/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  guideme.GuidesCommon
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.InteractionResultHolder
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 */
package appeng.items.tools;

import appeng.core.AppEng;
import appeng.items.AEBaseItem;
import guideme.GuidesCommon;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class GuideItem
extends AEBaseItem {
    public static final ResourceLocation GUIDE_ID = AppEng.makeId("guide");

    public GuideItem(Item.Properties properties) {
        super(properties);
    }

    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            GuidesCommon.openGuide((Player)player, (ResourceLocation)GUIDE_ID);
        }
        return new InteractionResultHolder(InteractionResult.FAIL, (Object)stack);
    }
}

