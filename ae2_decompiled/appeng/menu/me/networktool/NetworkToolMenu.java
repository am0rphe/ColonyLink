/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.inventory.MenuType
 */
package appeng.menu.me.networktool;

import appeng.items.contents.NetworkToolMenuHost;
import appeng.menu.AEBaseMenu;
import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.slot.RestrictedInputSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

public class NetworkToolMenu
extends AEBaseMenu {
    public static final MenuType<NetworkToolMenu> TYPE = MenuTypeBuilder.create(NetworkToolMenu::new, NetworkToolMenuHost.class).build("networktool");
    @GuiSync(value=1)
    public boolean facadeMode;

    public NetworkToolMenu(int id, Inventory ip, NetworkToolMenuHost host) {
        super(TYPE, id, ip, host);
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.UPGRADES, host.getInventory(), i), SlotSemantics.STORAGE);
        }
        this.createPlayerInventorySlots(ip);
    }
}

