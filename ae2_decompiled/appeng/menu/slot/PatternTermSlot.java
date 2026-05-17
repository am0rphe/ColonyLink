/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.ItemStack
 */
package appeng.menu.slot;

import appeng.client.gui.me.common.ClientReadOnlySlot;
import net.minecraft.world.item.ItemStack;

public class PatternTermSlot
extends ClientReadOnlySlot {
    private ItemStack resultItem = ItemStack.EMPTY;
    private boolean active = true;

    public ItemStack getItem() {
        return this.resultItem;
    }

    public void setResultItem(ItemStack resultItem) {
        this.resultItem = resultItem;
    }

    public boolean isActive() {
        return this.active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}

