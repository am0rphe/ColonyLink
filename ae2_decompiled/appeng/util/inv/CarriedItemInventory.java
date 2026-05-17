/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Preconditions
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.item.ItemStack
 */
package appeng.util.inv;

import appeng.api.inventories.InternalInventory;
import com.google.common.base.Preconditions;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public class CarriedItemInventory
implements InternalInventory {
    private final AbstractContainerMenu menu;

    public CarriedItemInventory(AbstractContainerMenu menu) {
        this.menu = menu;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public ItemStack getStackInSlot(int slotIndex) {
        Preconditions.checkArgument((slotIndex == 0 ? 1 : 0) != 0);
        return this.menu.getCarried();
    }

    @Override
    public void setItemDirect(int slotIndex, ItemStack stack) {
        Preconditions.checkArgument((slotIndex == 0 ? 1 : 0) != 0);
        this.menu.setCarried(stack);
    }
}

