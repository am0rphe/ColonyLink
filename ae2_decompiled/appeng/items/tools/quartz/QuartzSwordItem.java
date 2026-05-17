/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.SwordItem
 *  net.minecraft.world.item.Tier
 */
package appeng.items.tools.quartz;

import appeng.items.tools.quartz.QuartzToolType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;

public class QuartzSwordItem
extends SwordItem {
    public QuartzSwordItem(Item.Properties props, QuartzToolType type) {
        super(type.getToolTier(), props.attributes(QuartzSwordItem.createAttributes((Tier)type.getToolTier(), (int)3, (float)-2.4f)));
    }
}

