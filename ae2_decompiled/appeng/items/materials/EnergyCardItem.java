/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Preconditions
 *  net.minecraft.world.item.Item$Properties
 */
package appeng.items.materials;

import appeng.items.materials.UpgradeCardItem;
import com.google.common.base.Preconditions;
import net.minecraft.world.item.Item;

public class EnergyCardItem
extends UpgradeCardItem {
    private final int energyMultiplier;

    public EnergyCardItem(Item.Properties properties, int energyMultiplier) {
        super(properties);
        Preconditions.checkArgument((energyMultiplier > 0 ? 1 : 0) != 0, (Object)"energyMultiplier must be > 0");
        this.energyMultiplier = energyMultiplier;
    }

    public int getEnergyMultiplier() {
        return this.energyMultiplier;
    }
}

