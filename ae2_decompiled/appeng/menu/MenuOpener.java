/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Preconditions
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.MenuType
 */
package appeng.menu;

import appeng.core.AELog;
import appeng.menu.AEBaseMenu;
import appeng.menu.locator.MenuHostLocator;
import com.google.common.base.Preconditions;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;

public final class MenuOpener {
    private static final Map<MenuType<? extends AEBaseMenu>, Opener> registry = new HashMap<MenuType<? extends AEBaseMenu>, Opener>();

    private MenuOpener() {
    }

    public static <T extends AEBaseMenu> void addOpener(MenuType<T> type, Opener opener) {
        registry.put(type, opener);
    }

    public static boolean returnTo(MenuType<?> type, Player player, MenuHostLocator locator) {
        return MenuOpener.open(type, player, locator, true);
    }

    public static boolean open(MenuType<?> type, Player player, MenuHostLocator locator) {
        return MenuOpener.open(type, player, locator, false);
    }

    public static boolean open(MenuType<?> type, Player player, MenuHostLocator locator, boolean fromSubMenu) {
        Preconditions.checkArgument((!player.level().isClientSide() ? 1 : 0) != 0, (Object)"Menus must be opened on the server.");
        Opener opener = registry.get(type);
        if (opener == null) {
            AELog.warn("Trying to open menu for unknown menu type {}", type);
            return false;
        }
        return opener.open(player, locator, fromSubMenu);
    }

    @FunctionalInterface
    public static interface Opener {
        public boolean open(Player var1, MenuHostLocator var2, boolean var3);
    }
}

