/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.inventory.MenuType
 */
package appeng.menu.implementations;

import appeng.blockentity.storage.MEChestBlockEntity;
import appeng.menu.AEBaseMenu;
import appeng.menu.SlotSemantics;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.slot.RestrictedInputSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

public class MEChestMenu
extends AEBaseMenu {
    public static final MenuType<MEChestMenu> TYPE = MenuTypeBuilder.create(MEChestMenu::new, MEChestBlockEntity.class).build("me_chest");

    public MEChestMenu(int id, Inventory ip, MEChestBlockEntity chest) {
        super(TYPE, id, ip, chest);
        this.addSlot(new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.STORAGE_CELLS, chest.getInternalInventory(), 1), SlotSemantics.STORAGE_CELL);
        this.createPlayerInventorySlots(ip);
    }
}

