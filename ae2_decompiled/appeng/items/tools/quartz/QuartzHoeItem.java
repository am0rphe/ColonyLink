/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.HoeItem
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.Tier
 */
package appeng.items.tools.quartz;

import appeng.items.tools.quartz.QuartzToolType;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;

public class QuartzHoeItem
extends HoeItem {
    public QuartzHoeItem(Item.Properties props, QuartzToolType type) {
        super(type.getToolTier(), props.attributes(QuartzHoeItem.createAttributes((Tier)type.getToolTier(), (float)-2.0f, (float)-1.0f)));
    }
}

