/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.phys.BlockHitResult
 *  org.jetbrains.annotations.Nullable
 */
package appeng.menu.locator;

import appeng.menu.locator.ItemMenuHostLocator;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

record StackItemLocator(ItemStack stack) implements ItemMenuHostLocator
{
    @Override
    public ItemStack locateItem(Player player) {
        return this.stack;
    }

    @Override
    @Nullable
    public BlockHitResult hitResult() {
        return null;
    }
}

