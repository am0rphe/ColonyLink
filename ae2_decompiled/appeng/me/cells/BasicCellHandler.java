/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Object2LongMap$Entry
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.world.inventory.tooltip.TooltipComponent
 *  net.minecraft.world.item.ItemStack
 */
package appeng.me.cells;

import appeng.api.config.IncludeExclude;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.cells.ICellHandler;
import appeng.api.storage.cells.ISaveProvider;
import appeng.core.AEConfig;
import appeng.core.localization.GuiText;
import appeng.core.localization.Tooltips;
import appeng.items.storage.StorageCellTooltipComponent;
import appeng.me.cells.BasicCellInventory;
import appeng.util.ConfigInventory;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

public class BasicCellHandler
implements ICellHandler {
    public static final BasicCellHandler INSTANCE = new BasicCellHandler();

    @Override
    public boolean isCell(ItemStack is) {
        return BasicCellInventory.isCell(is);
    }

    @Override
    public BasicCellInventory getCellInventory(ItemStack is, ISaveProvider container) {
        return BasicCellInventory.createInventory(is, container);
    }

    public void addCellInformationToTooltip(ItemStack is, List<Component> lines) {
        BasicCellInventory handler = this.getCellInventory(is, null);
        if (handler == null) {
            return;
        }
        lines.add(Tooltips.bytesUsed(handler.getUsedBytes(), handler.getTotalBytes()));
        lines.add(Tooltips.typesUsed(handler.getStoredItemTypes(), handler.getTotalItemTypes()));
        if (handler.isPreformatted()) {
            MutableComponent list = (handler.getPartitionListMode() == IncludeExclude.WHITELIST ? GuiText.Included : GuiText.Excluded).text();
            if (handler.isFuzzy()) {
                lines.add((Component)GuiText.Partitioned.withSuffix(" - ").append((Component)list).append(" ").append((Component)GuiText.Fuzzy.text()));
            } else {
                lines.add((Component)GuiText.Partitioned.withSuffix(" - ").append((Component)list).append(" ").append((Component)GuiText.Precise.text()));
            }
        }
    }

    public Optional<TooltipComponent> getTooltipImage(ItemStack is) {
        boolean hasMoreContent;
        ArrayList<GenericStack> content;
        BasicCellInventory handler = this.getCellInventory(is, null);
        if (handler == null) {
            return Optional.empty();
        }
        ArrayList<ItemStack> upgradeStacks = new ArrayList<ItemStack>();
        if (AEConfig.instance().isTooltipShowCellUpgrades()) {
            for (ItemStack upgrade : handler.getUpgradesInventory()) {
                upgradeStacks.add(upgrade);
            }
        }
        if (AEConfig.instance().isTooltipShowCellContent()) {
            content = new ArrayList();
            int maxCountShown = AEConfig.instance().getTooltipMaxCellContentShown();
            KeyCounter availableStacks = handler.getAvailableStacks();
            for (Object2LongMap.Entry<AEKey> entry : availableStacks) {
                content.add(new GenericStack((AEKey)entry.getKey(), entry.getLongValue()));
            }
            if (content.size() < maxCountShown && handler.getPartitionListMode() == IncludeExclude.WHITELIST) {
                ConfigInventory config = handler.getConfigInventory();
                for (int i = 0; i < config.size(); ++i) {
                    AEKey what = config.getKey(i);
                    if (what != null && availableStacks.get(what) <= 0L) {
                        content.add(new GenericStack(what, 0L));
                    }
                    if (content.size() > maxCountShown) break;
                }
            }
            content.sort(Comparator.comparingLong(GenericStack::amount).reversed());
            boolean bl = hasMoreContent = content.size() > maxCountShown;
            if (content.size() > maxCountShown) {
                content.subList(maxCountShown, content.size()).clear();
            }
        } else {
            hasMoreContent = false;
            content = Collections.emptyList();
        }
        return Optional.of(new StorageCellTooltipComponent(upgradeStacks, content, hasMoreContent, true));
    }
}

