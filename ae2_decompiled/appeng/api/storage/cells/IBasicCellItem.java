/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Preconditions
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.inventory.tooltip.TooltipComponent
 *  net.minecraft.world.item.ItemStack
 */
package appeng.api.storage.cells;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.storage.cells.ICellWorkbenchItem;
import appeng.me.cells.BasicCellHandler;
import com.google.common.base.Preconditions;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

public interface IBasicCellItem
extends ICellWorkbenchItem {
    public AEKeyType getKeyType();

    public int getBytes(ItemStack var1);

    public int getBytesPerType(ItemStack var1);

    public int getTotalTypes(ItemStack var1);

    default public boolean isBlackListed(ItemStack cellItem, AEKey requestedAddition) {
        return false;
    }

    default public boolean storableInStorageCell() {
        return false;
    }

    default public boolean isStorageCell(ItemStack i) {
        return true;
    }

    public double getIdleDrain();

    default public void addCellInformationToTooltip(ItemStack is, List<Component> lines) {
        Preconditions.checkArgument((is.getItem() == this ? 1 : 0) != 0);
        BasicCellHandler.INSTANCE.addCellInformationToTooltip(is, lines);
    }

    default public Optional<TooltipComponent> getCellTooltipImage(ItemStack is) {
        Preconditions.checkArgument((is.getItem() == this ? 1 : 0) != 0);
        return BasicCellHandler.INSTANCE.getTooltipImage(is);
    }
}

