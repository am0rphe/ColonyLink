/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 */
package appeng.menu.slot;

import appeng.api.inventories.InternalInventory;
import appeng.menu.slot.AppEngSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class InaccessibleSlot
extends AppEngSlot {
    private ItemStack dspStack = ItemStack.EMPTY;

    public InaccessibleSlot(InternalInventory i, int invSlot) {
        super(i, invSlot);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }

    @Override
    public boolean mayPickup(Player player) {
        return false;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        this.dspStack = ItemStack.EMPTY;
    }

    @Override
    public ItemStack getDisplayStack() {
        ItemStack dsp;
        if (this.dspStack.isEmpty() && !(dsp = super.getDisplayStack()).isEmpty()) {
            this.dspStack = dsp.copy();
        }
        return this.dspStack;
    }
}

