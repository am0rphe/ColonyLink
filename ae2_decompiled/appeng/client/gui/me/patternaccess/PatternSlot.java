/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 */
package appeng.client.gui.me.patternaccess;

import appeng.client.gui.me.patternaccess.PatternContainerRecord;
import appeng.crafting.pattern.EncodedPatternItem;
import appeng.menu.slot.AppEngSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class PatternSlot
extends AppEngSlot {
    private final PatternContainerRecord machineInv;

    public PatternSlot(PatternContainerRecord machineInv, int machineInvSlot, int x, int y) {
        super(machineInv.getInventory(), machineInvSlot);
        this.machineInv = machineInv;
        this.x = x;
        this.y = y;
    }

    @Override
    public ItemStack getDisplayStack() {
        EncodedPatternItem iep;
        ItemStack out;
        Item item;
        ItemStack is;
        if (this.isRemote() && !(is = super.getDisplayStack()).isEmpty() && (item = is.getItem()) instanceof EncodedPatternItem && !(out = (iep = (EncodedPatternItem)item).getOutput(is)).isEmpty()) {
            return out;
        }
        return super.getDisplayStack();
    }

    public boolean hasItem() {
        return !this.getItem().isEmpty();
    }

    public PatternContainerRecord getMachineInv() {
        return this.machineInv;
    }

    @Override
    public final boolean mayPlace(ItemStack stack) {
        return false;
    }

    @Override
    public final void set(ItemStack stack) {
    }

    @Override
    public void initialize(ItemStack stack) {
    }

    @Override
    public final int getMaxStackSize() {
        return 0;
    }

    @Override
    public final ItemStack remove(int amount) {
        return ItemStack.EMPTY;
    }

    @Override
    public final boolean mayPickup(Player player) {
        return false;
    }
}

