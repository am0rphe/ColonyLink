/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.inventory.MenuType
 */
package appeng.menu.implementations;

import appeng.blockentity.storage.DriveBlockEntity;
import appeng.menu.AEBaseMenu;
import appeng.menu.SlotSemantics;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.slot.RestrictedInputSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

public class DriveMenu
extends AEBaseMenu {
    public static final MenuType<DriveMenu> TYPE = MenuTypeBuilder.create(DriveMenu::new, DriveBlockEntity.class).build("drive");

    public DriveMenu(int id, Inventory ip, DriveBlockEntity drive) {
        super(TYPE, id, ip, drive);
        for (int i = 0; i < 10; ++i) {
            this.addSlot(new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.STORAGE_CELLS, drive.getInternalInventory(), i), SlotSemantics.STORAGE_CELL);
        }
        this.createPlayerInventorySlots(ip);
    }
}

