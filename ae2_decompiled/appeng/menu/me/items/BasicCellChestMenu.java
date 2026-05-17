/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.inventory.MenuType
 */
package appeng.menu.me.items;

import appeng.api.storage.ITerminalHost;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.me.common.MEStorageMenu;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

public class BasicCellChestMenu
extends MEStorageMenu {
    public static final MenuType<MEStorageMenu> TYPE = MenuTypeBuilder.create(BasicCellChestMenu::new, ITerminalHost.class).build("basic_cell_chest");

    public BasicCellChestMenu(MenuType<?> menuType, int id, Inventory ip, ITerminalHost host) {
        super(menuType, id, ip, host);
    }

    @Override
    public boolean canConfigureTypeFilter() {
        return false;
    }
}

