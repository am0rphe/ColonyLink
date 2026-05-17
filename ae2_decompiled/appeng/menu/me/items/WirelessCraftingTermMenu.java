/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.inventory.MenuType
 */
package appeng.menu.me.items;

import appeng.api.networking.IGridNode;
import appeng.helpers.WirelessCraftingTerminalMenuHost;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.me.items.CraftingTermMenu;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

public class WirelessCraftingTermMenu
extends CraftingTermMenu {
    public static final MenuType<WirelessCraftingTermMenu> TYPE = MenuTypeBuilder.create(WirelessCraftingTermMenu::new, WirelessCraftingTerminalMenuHost.class).build("wirelesscraftingterm");
    private final WirelessCraftingTerminalMenuHost<?> menuHost;

    public WirelessCraftingTermMenu(int id, Inventory ip, WirelessCraftingTerminalMenuHost monitorable) {
        super(TYPE, id, ip, monitorable, false);
        this.createPlayerInventorySlots(ip);
        this.menuHost = monitorable;
    }

    @Override
    public IGridNode getGridNode() {
        return this.menuHost.getActionableNode();
    }
}

