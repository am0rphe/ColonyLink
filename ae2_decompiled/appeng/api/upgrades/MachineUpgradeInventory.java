/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.level.ItemLike
 *  org.jetbrains.annotations.Nullable
 */
package appeng.api.upgrades;

import appeng.api.upgrades.MachineUpgradesChanged;
import appeng.api.upgrades.UpgradeInventory;
import appeng.util.inv.AppEngInternalInventory;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

class MachineUpgradeInventory
extends UpgradeInventory {
    @Nullable
    private final MachineUpgradesChanged changeCallback;

    public MachineUpgradeInventory(ItemLike item, int slots, @Nullable MachineUpgradesChanged changeCallback) {
        super(item.asItem(), slots);
        this.changeCallback = changeCallback;
    }

    @Override
    public void onChangeInventory(AppEngInternalInventory inv, int slot) {
        super.onChangeInventory(inv, slot);
        if (this.changeCallback != null) {
            this.changeCallback.onUpgradesChanged();
        }
    }
}

