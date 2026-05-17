/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.AxeItem
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.Tier
 */
package appeng.items.tools.quartz;

import appeng.items.tools.quartz.QuartzToolType;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;

public class QuartzAxeItem
extends AxeItem {
    public QuartzAxeItem(Item.Properties props, QuartzToolType type) {
        super(type.getToolTier(), props.attributes(QuartzAxeItem.createAttributes((Tier)type.getToolTier(), (float)6.0f, (float)-3.1f)));
    }
}

