/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 *  net.neoforged.neoforge.items.IItemHandler
 */
package appeng.hotkeys;

import appeng.api.features.HotkeyAction;
import appeng.hotkeys.InventoryHotkeyAction;
import appeng.integration.modules.curios.CuriosIntegration;
import appeng.menu.locator.MenuLocators;
import java.util.function.Predicate;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.items.IItemHandler;

public record CuriosHotkeyAction(Predicate<ItemStack> locatable, InventoryHotkeyAction.Opener opener) implements HotkeyAction
{
    public CuriosHotkeyAction(ItemLike item, InventoryHotkeyAction.Opener opener) {
        this((ItemStack stack) -> stack.is(item.asItem()), opener);
    }

    @Override
    public boolean run(Player player) {
        IItemHandler cap = (IItemHandler)player.getCapability(CuriosIntegration.ITEM_HANDLER);
        if (cap == null) {
            return false;
        }
        for (int i = 0; i < cap.getSlots(); ++i) {
            if (!this.locatable.test(cap.getStackInSlot(i)) || !this.opener.open(player, MenuLocators.forCurioSlot(i))) continue;
            return true;
        }
        return false;
    }
}

