/*
 * Decompiled with CFR 0.152.
 */
package appeng.menu.slot;

import appeng.api.inventories.InternalInventory;
import appeng.menu.slot.IOptionalSlotHost;
import appeng.menu.slot.OptionalFakeSlot;

public class PatternOutputsSlot
extends OptionalFakeSlot {
    public PatternOutputsSlot(InternalInventory inv, IOptionalSlotHost containerBus, int invSlot, int groupNum) {
        super(inv, containerBus, invSlot, groupNum);
    }

    @Override
    public boolean isSlotEnabled() {
        return true;
    }
}

