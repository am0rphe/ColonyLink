/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.inventory.tooltip.TooltipComponent
 *  net.minecraft.world.item.ItemStack
 */
package appeng.me.cells;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.cells.ICellHandler;
import appeng.api.storage.cells.ISaveProvider;
import appeng.api.storage.cells.StorageCell;
import appeng.core.AEConfig;
import appeng.items.contents.CellConfig;
import appeng.items.storage.CreativeCellItem;
import appeng.items.storage.StorageCellTooltipComponent;
import appeng.me.cells.CreativeCellInventory;
import appeng.util.ConfigInventory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

public class CreativeCellHandler
implements ICellHandler {
    public static final CreativeCellHandler INSTANCE = new CreativeCellHandler();

    @Override
    public boolean isCell(ItemStack is) {
        return !is.isEmpty() && is.getItem() instanceof CreativeCellItem;
    }

    @Override
    public StorageCell getCellInventory(ItemStack is, ISaveProvider container) {
        if (!is.isEmpty() && is.getItem() instanceof CreativeCellItem) {
            return new CreativeCellInventory(is);
        }
        return null;
    }

    public Optional<TooltipComponent> getTooltipImage(ItemStack is) {
        boolean hasMoreContent;
        ArrayList<GenericStack> content;
        StorageCell handler = this.getCellInventory(is, null);
        if (handler == null) {
            return Optional.empty();
        }
        ConfigInventory cc = CellConfig.create(is);
        if (AEConfig.instance().isTooltipShowCellContent()) {
            content = new ArrayList();
            int maxCountShown = AEConfig.instance().getTooltipMaxCellContentShown();
            for (AEKey key : cc.keySet()) {
                content.add(new GenericStack(key, 1L));
            }
            boolean bl = hasMoreContent = content.size() > maxCountShown;
            if (content.size() > maxCountShown) {
                content.subList(maxCountShown, content.size()).clear();
            }
        } else {
            hasMoreContent = false;
            content = Collections.emptyList();
        }
        return Optional.of(new StorageCellTooltipComponent(List.of(), content, hasMoreContent, false));
    }
}

