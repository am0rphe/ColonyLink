/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ShovelItem
 *  net.minecraft.world.item.Tier
 */
package appeng.items.tools.quartz;

import appeng.items.tools.quartz.QuartzToolType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.Tier;

public class QuartzSpadeItem
extends ShovelItem {
    public QuartzSpadeItem(Item.Properties props, QuartzToolType type) {
        super(type.getToolTier(), props.attributes(QuartzSpadeItem.createAttributes((Tier)type.getToolTier(), (float)1.5f, (float)-3.0f)));
    }
}

