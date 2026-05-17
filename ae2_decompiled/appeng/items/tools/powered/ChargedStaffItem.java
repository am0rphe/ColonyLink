/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.phys.AABB
 */
package appeng.items.tools.powered;

import appeng.api.config.Actionable;
import appeng.core.AEConfig;
import appeng.core.AppEng;
import appeng.core.network.clientbound.LightningPacket;
import appeng.items.tools.powered.powersink.AEBasePoweredItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

public class ChargedStaffItem
extends AEBasePoweredItem {
    public ChargedStaffItem(Item.Properties props) {
        super(AEConfig.instance().getChargedStaffBattery(), props);
    }

    public boolean hurtEnemy(ItemStack item, LivingEntity target, LivingEntity hitter) {
        if (this.getAECurrentPower(item) > 300.0) {
            this.extractAEPower(item, 300.0, Actionable.MODULATE);
            if (!target.level().isClientSide()) {
                for (int x = 0; x < 2; ++x) {
                    AABB entityBoundingBox = target.getBoundingBox();
                    float dx = (float)((double)(target.level().getRandom().nextFloat() * target.getBbWidth()) + entityBoundingBox.minX);
                    float dy = (float)((double)(target.level().getRandom().nextFloat() * target.getBbHeight()) + entityBoundingBox.minY);
                    float dz = (float)((double)(target.level().getRandom().nextFloat() * target.getBbWidth()) + entityBoundingBox.minZ);
                    AppEng.instance().sendToAllNearExcept(null, dx, dy, dz, 32.0, target.level(), new LightningPacket(dx, dy, dz));
                }
            }
            target.hurt(target.level().damageSources().magic(), 6.0f);
            return true;
        }
        return false;
    }

    @Override
    public double getChargeRate(ItemStack stack) {
        return 32.0;
    }
}

