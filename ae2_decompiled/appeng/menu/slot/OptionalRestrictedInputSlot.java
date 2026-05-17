/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.player.Inventory
 */
package appeng.menu.slot;

import appeng.api.inventories.InternalInventory;
import appeng.menu.slot.IOptionalSlotHost;
import appeng.menu.slot.RestrictedInputSlot;
import net.minecraft.world.entity.player.Inventory;

public class OptionalRestrictedInputSlot
extends RestrictedInputSlot {
    private final int groupNum;
    private final IOptionalSlotHost host;

    public OptionalRestrictedInputSlot(RestrictedInputSlot.PlacableItemType valid, InternalInventory inv, IOptionalSlotHost host, int invSlot, int grpNum, Inventory invPlayer) {
        super(valid, inv, invSlot);
        this.groupNum = grpNum;
        this.host = host;
    }

    @Override
    public boolean isSlotEnabled() {
        if (this.host == null) {
            return false;
        }
        return this.host.isSlotEnabled(this.groupNum);
    }
}

