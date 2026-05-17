/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.inventory.MenuType
 */
package appeng.menu.implementations;

import appeng.blockentity.qnb.QuantumBridgeBlockEntity;
import appeng.menu.AEBaseMenu;
import appeng.menu.SlotSemantics;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.slot.RestrictedInputSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

public class QNBMenu
extends AEBaseMenu {
    public static final MenuType<QNBMenu> TYPE = MenuTypeBuilder.create(QNBMenu::new, QuantumBridgeBlockEntity.class).build("qnb");

    public QNBMenu(int id, Inventory ip, QuantumBridgeBlockEntity quantumBridge) {
        super(TYPE, id, ip, quantumBridge);
        this.addSlot(new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.QE_SINGULARITY, quantumBridge.getInternalInventory(), 0).setStackLimit(1), SlotSemantics.STORAGE);
        this.createPlayerInventorySlots(ip);
    }
}

