/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.item.ItemStack
 */
package appeng.util.inv;

import appeng.api.inventories.InternalInventory;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class PlayerInternalInventory
implements InternalInventory {
    private final Inventory inventory;

    public PlayerInternalInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public int size() {
        return 36;
    }

    @Override
    public ItemStack getStackInSlot(int slotIndex) {
        return this.inventory.getItem(slotIndex);
    }

    @Override
    public void setItemDirect(int slotIndex, ItemStack stack) {
        this.inventory.setItem(slotIndex, stack);
        if (!stack.isEmpty()) {
            this.inventory.getItem(slotIndex).setPopTime(5);
        }
    }
}

