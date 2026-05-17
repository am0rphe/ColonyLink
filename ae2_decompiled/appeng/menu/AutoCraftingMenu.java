/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.item.ItemStack
 */
package appeng.menu;

import appeng.api.implementations.menus.IAutoCraftingMenu;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public class AutoCraftingMenu
extends AbstractContainerMenu
implements IAutoCraftingMenu {
    public AutoCraftingMenu() {
        super(null, 0);
    }

    public boolean stillValid(Player PlayerEntity) {
        return false;
    }

    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}

