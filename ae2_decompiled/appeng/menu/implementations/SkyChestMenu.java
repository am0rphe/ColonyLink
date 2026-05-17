/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.MenuType
 */
package appeng.menu.implementations;

import appeng.api.inventories.InternalInventory;
import appeng.blockentity.storage.SkyChestBlockEntity;
import appeng.menu.AEBaseMenu;
import appeng.menu.SlotSemantics;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.slot.AppEngSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;

public class SkyChestMenu
extends AEBaseMenu {
    public static final MenuType<SkyChestMenu> TYPE = MenuTypeBuilder.create(SkyChestMenu::new, SkyChestBlockEntity.class).build("skychest");
    private final SkyChestBlockEntity chest;

    public SkyChestMenu(int id, Inventory ip, SkyChestBlockEntity chest) {
        super(TYPE, id, ip, chest);
        this.chest = chest;
        chest.startOpen(ip.player);
        InternalInventory inv = chest.getInternalInventory();
        for (int i = 0; i < inv.size(); ++i) {
            this.addSlot(new AppEngSlot(inv, i), SlotSemantics.STORAGE);
        }
        this.createPlayerInventorySlots(ip);
    }

    public void removed(Player player) {
        super.removed(player);
        this.chest.stopOpen(player);
    }

    public SkyChestBlockEntity getChest() {
        return this.chest;
    }
}

