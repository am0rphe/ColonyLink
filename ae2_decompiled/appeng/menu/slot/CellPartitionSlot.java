/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.item.ItemStack
 *  org.jetbrains.annotations.Nullable
 */
package appeng.menu.slot;

import appeng.api.inventories.InternalInventory;
import appeng.api.storage.StorageCells;
import appeng.api.storage.cells.StorageCell;
import appeng.client.Point;
import appeng.core.localization.GuiText;
import appeng.core.localization.Tooltips;
import appeng.menu.slot.FakeSlot;
import appeng.menu.slot.IOptionalSlot;
import appeng.menu.slot.IPartitionSlotHost;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class CellPartitionSlot
extends FakeSlot
implements IOptionalSlot {
    private final IPartitionSlotHost host;
    private final int slot;

    public CellPartitionSlot(InternalInventory inv, IPartitionSlotHost host, int invSlot) {
        super(inv, invSlot);
        this.host = host;
        this.slot = invSlot;
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
        return this.host.isPartitionSlotEnabled(this.slot);
    }

    @Override
    public void set(ItemStack is) {
        if (this.canFitInsideCell(is)) {
            super.set(is);
        }
    }

    @Override
    public boolean isRenderDisabled() {
        return true;
    }

    @Override
    public Point getBackgroundPos() {
        return new Point(this.x - 1, this.y - 1);
    }

    @Override
    @Nullable
    public List<Component> getCustomTooltip(ItemStack carriedItem) {
        if (!this.canFitInsideCell(carriedItem)) {
            return List.of(Tooltips.of(GuiText.CantFitInsideStorageCell, Tooltips.RED, new Object[0]));
        }
        return super.getCustomTooltip(carriedItem);
    }

    private boolean canFitInsideCell(ItemStack stack) {
        StorageCell cellInv = StorageCells.getCellInventory(stack, null);
        return cellInv == null || cellInv.canFitInsideCell();
    }
}

