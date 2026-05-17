/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.phys.BlockHitResult
 *  org.jetbrains.annotations.Nullable
 *  org.jline.utils.Log
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package appeng.menu.locator;

import appeng.api.implementations.menuobjects.IMenuItem;
import appeng.api.implementations.menuobjects.ItemMenuHost;
import appeng.menu.locator.MenuHostLocator;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.jline.utils.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface ItemMenuHostLocator
extends MenuHostLocator {
    public static final Logger LOG = LoggerFactory.getLogger(ItemMenuHostLocator.class);

    @Override
    default public <T> T locate(Player player, Class<T> hostInterface) {
        Item item;
        ItemStack it = this.locateItem(player);
        if (!it.isEmpty() && (item = it.getItem()) instanceof IMenuItem) {
            IMenuItem menuItem = (IMenuItem)item;
            ItemMenuHost<?> menuHost = menuItem.getMenuHost(player, this, this.hitResult());
            if (hostInterface.isInstance(menuHost)) {
                return hostInterface.cast(menuHost);
            }
            if (menuHost != null) {
                Log.warn((Object[])new Object[]{"Item in {} of {} did not create a compatible menu of type {}: {}", this, player, hostInterface, menuHost});
            }
        } else {
            Log.warn((Object[])new Object[]{"Item in {} of {} is not an IMenuItem: {}", this, player, it});
        }
        return null;
    }

    @Nullable
    public BlockHitResult hitResult();

    public ItemStack locateItem(Player var1);

    @Nullable
    default public Integer getPlayerInventorySlot() {
        return null;
    }
}

