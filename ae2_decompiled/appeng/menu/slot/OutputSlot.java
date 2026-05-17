/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.ItemStack
 */
package appeng.menu.slot;

import appeng.api.inventories.InternalInventory;
import appeng.client.gui.Icon;
import appeng.menu.slot.AppEngSlot;
import net.minecraft.world.item.ItemStack;

public class OutputSlot
extends AppEngSlot {
    public OutputSlot(InternalInventory inv, int invSlot, Icon icon) {
        super(inv, invSlot);
        this.setIcon(icon);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }
}

