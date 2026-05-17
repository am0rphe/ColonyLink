/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.Container
 *  net.minecraft.world.SimpleContainer
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.Slot
 *  net.minecraft.world.item.ItemStack
 */
package appeng.client.gui.me.common;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ClientReadOnlySlot
extends Slot {
    private static final Container EMPTY_INVENTORY = new SimpleContainer(0);

    public ClientReadOnlySlot(int xPosition, int yPosition) {
        super(EMPTY_INVENTORY, 0, xPosition, yPosition);
    }

    public ClientReadOnlySlot() {
        this(0, 0);
    }

    public final boolean mayPlace(ItemStack stack) {
        return false;
    }

    public final void set(ItemStack stack) {
    }

    public final int getMaxStackSize() {
        return 0;
    }

    public final ItemStack remove(int amount) {
        return ItemStack.EMPTY;
    }

    public final boolean mayPickup(Player player) {
        return false;
    }
}

