/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.NonNullList
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 */
package appeng.hotkeys;

import appeng.api.features.HotkeyAction;
import appeng.menu.locator.ItemMenuHostLocator;
import appeng.menu.locator.MenuLocators;
import java.util.function.Predicate;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public record InventoryHotkeyAction(Predicate<ItemStack> locatable, Opener opener) implements HotkeyAction
{
    public InventoryHotkeyAction(ItemLike item, Opener opener) {
        this((ItemStack stack) -> stack.is(item.asItem()), opener);
    }

    @Override
    public boolean run(Player player) {
        NonNullList items = player.getInventory().items;
        for (int i = 0; i < items.size(); ++i) {
            if (!this.locatable.test((ItemStack)items.get(i)) || !this.opener.open(player, MenuLocators.forInventorySlot(i))) continue;
            return true;
        }
        return false;
    }

    @FunctionalInterface
    public static interface Opener {
        public boolean open(Player var1, ItemMenuHostLocator var2);
    }
}

