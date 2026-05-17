/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 */
package appeng.api.storage;

import appeng.menu.ISubMenu;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface ISubMenuHost {
    public void returnToMainMenu(Player var1, ISubMenu var2);

    public ItemStack getMainMenuIcon();
}

