/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.component.ItemContainerContents
 *  org.jetbrains.annotations.Nullable
 */
package appeng.api.upgrades;

import appeng.api.ids.AEComponents;
import appeng.api.upgrades.ItemUpgradesChanged;
import appeng.api.upgrades.UpgradeInventory;
import appeng.util.inv.AppEngInternalInventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import org.jetbrains.annotations.Nullable;

final class ItemUpgradeInventory
extends UpgradeInventory {
    private final ItemStack stack;
    @Nullable
    private final ItemUpgradesChanged changeCallback;

    public ItemUpgradeInventory(ItemStack stack, int upgrades, @Nullable ItemUpgradesChanged changeCallback) {
        super(stack.getItem(), upgrades);
        this.stack = stack;
        this.changeCallback = changeCallback;
        this.fromItemContainerContents((ItemContainerContents)stack.getOrDefault(AEComponents.UPGRADES, (Object)ItemContainerContents.EMPTY));
    }

    @Override
    public void saveChangedInventory(AppEngInternalInventory inv) {
        this.stack.set(AEComponents.UPGRADES, (Object)this.toItemContainerContents());
        super.saveChangedInventory(inv);
    }

    @Override
    public void onChangeInventory(AppEngInternalInventory inv, int slot) {
        super.onChangeInventory(inv, slot);
        if (this.changeCallback != null) {
            this.changeCallback.onUpgradesChanged(this.stack, this);
        }
    }
}

