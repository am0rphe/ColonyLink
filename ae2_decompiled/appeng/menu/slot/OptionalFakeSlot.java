/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.ItemStack
 */
package appeng.menu.slot;

import appeng.api.inventories.InternalInventory;
import appeng.client.Point;
import appeng.menu.slot.FakeSlot;
import appeng.menu.slot.IOptionalSlot;
import appeng.menu.slot.IOptionalSlotHost;
import net.minecraft.world.item.ItemStack;

public class OptionalFakeSlot
extends FakeSlot
implements IOptionalSlot {
    private final int groupNum;
    private final IOptionalSlotHost host;
    private boolean renderDisabled = true;

    public OptionalFakeSlot(InternalInventory inv, IOptionalSlotHost containerBus, int invSlot, int groupNum) {
        super(inv, invSlot);
        this.groupNum = groupNum;
        this.host = containerBus;
    }

    @Override
    public ItemStack getItem() {
        if (!this.isSlotEnabled()) {
            this.clearStack();
        }
        return super.getItem();
    }

    @Override
    public boolean isSlotEnabled() {
        if (this.host == null) {
            return false;
        }
        return this.host.isSlotEnabled(this.groupNum);
    }

    @Override
    public boolean isRenderDisabled() {
        return this.renderDisabled;
    }

    public void setRenderDisabled(boolean renderDisabled) {
        this.renderDisabled = renderDisabled;
    }

    @Override
    public Point getBackgroundPos() {
        return new Point(this.x - 1, this.y - 1);
    }
}

