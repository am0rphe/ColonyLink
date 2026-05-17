/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.PickaxeItem
 *  net.minecraft.world.item.Tier
 */
package appeng.items.tools.quartz;

import appeng.items.tools.quartz.QuartzToolType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tier;

public class QuartzPickaxeItem
extends PickaxeItem {
    public QuartzPickaxeItem(Item.Properties props, QuartzToolType type) {
        super(type.getToolTier(), props.attributes(QuartzPickaxeItem.createAttributes((Tier)type.getToolTier(), (float)1.0f, (float)-2.8f)));
    }
}

