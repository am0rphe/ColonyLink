/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 */
package appeng.items.storage;

import appeng.api.config.FuzzyMode;
import appeng.api.ids.AEComponents;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.AEKeyFilter;
import appeng.api.storage.cells.ICellWorkbenchItem;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.UpgradeInventories;
import appeng.core.definitions.AEItems;
import appeng.items.AEBaseItem;
import appeng.items.contents.CellConfig;
import appeng.util.ConfigInventory;
import appeng.util.prioritylist.FuzzyPriorityList;
import appeng.util.prioritylist.IPartitionList;
import appeng.util.prioritylist.MergedPriorityList;
import appeng.util.prioritylist.PrecisePriorityList;
import java.util.Collection;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ViewCellItem
extends AEBaseItem
implements ICellWorkbenchItem {
    public ViewCellItem(Item.Properties properties) {
        super(properties);
    }

    public static IPartitionList createItemFilter(Collection<ItemStack> list) {
        return ViewCellItem.createFilter(AEItemKey.filter(), list);
    }

    public static IPartitionList createFilter(AEKeyFilter filter, Collection<ItemStack> list) {
        MergedPriorityList myPartitionList = null;
        MergedPriorityList myMergedList = new MergedPriorityList();
        for (ItemStack currentViewCell : list) {
            if (currentViewCell == null || !(currentViewCell.getItem() instanceof ViewCellItem)) continue;
            KeyCounter priorityList = new KeyCounter();
            ICellWorkbenchItem vc = (ICellWorkbenchItem)currentViewCell.getItem();
            ConfigInventory config = vc.getConfigInventory(currentViewCell);
            FuzzyMode fzMode = vc.getFuzzyMode(currentViewCell);
            for (int i = 0; i < config.size(); ++i) {
                AEKey what = config.getKey(i);
                if (what == null || !filter.matches(what)) continue;
                priorityList.add(what, 1L);
            }
            if (priorityList.isEmpty()) continue;
            IUpgradeInventory upgrades = vc.getUpgrades(currentViewCell);
            boolean hasInverter = upgrades.isInstalled(AEItems.INVERTER_CARD);
            if (upgrades.isInstalled(AEItems.FUZZY_CARD)) {
                myMergedList.addNewList(new FuzzyPriorityList(priorityList, fzMode), !hasInverter);
            } else {
                myMergedList.addNewList(new PrecisePriorityList(priorityList), !hasInverter);
            }
            myPartitionList = myMergedList;
        }
        return myPartitionList;
    }

    @Override
    public IUpgradeInventory getUpgrades(ItemStack is) {
        return UpgradeInventories.forItem(is, 2);
    }

    @Override
    public ConfigInventory getConfigInventory(ItemStack is) {
        return CellConfig.create(is);
    }

    @Override
    public FuzzyMode getFuzzyMode(ItemStack is) {
        return (FuzzyMode)((Object)is.getOrDefault(AEComponents.STORAGE_CELL_FUZZY_MODE, (Object)FuzzyMode.IGNORE_ALL));
    }

    @Override
    public void setFuzzyMode(ItemStack is, FuzzyMode fzMode) {
        is.set(AEComponents.STORAGE_CELL_FUZZY_MODE, (Object)fzMode);
    }
}

