/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 */
package appeng.menu;

import appeng.api.inventories.InternalInventory;
import appeng.items.contents.NetworkToolMenuHost;
import appeng.items.tools.NetworkToolItem;
import appeng.menu.AEBaseMenu;
import appeng.menu.SlotSemantics;
import appeng.menu.slot.RestrictedInputSlot;
import net.minecraft.network.chat.Component;

public class ToolboxMenu {
    private final AEBaseMenu menu;
    private final NetworkToolMenuHost<?> inv;

    public ToolboxMenu(AEBaseMenu menu) {
        this.menu = menu;
        this.inv = NetworkToolItem.findNetworkToolInv(menu.getPlayer());
        if (this.inv != null) {
            Integer playerSlot = this.inv.getPlayerInventorySlot();
            if (playerSlot != null) {
                menu.lockPlayerInventorySlot(playerSlot);
            }
            InternalInventory upgradeCardInv = this.inv.getInventory();
            for (int i = 0; i < upgradeCardInv.size(); ++i) {
                RestrictedInputSlot slot = new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.UPGRADES, upgradeCardInv, i);
                menu.addSlot(slot, SlotSemantics.TOOLBOX);
            }
        }
    }

    public boolean isPresent() {
        return this.inv != null;
    }

    public void tick() {
        if (this.inv != null) {
            if (!this.inv.isValid()) {
                this.menu.setValidMenu(false);
                return;
            }
            this.inv.tick();
        }
    }

    public Component getName() {
        return this.inv != null ? this.inv.getItemStack().getHoverName() : Component.empty();
    }
}

