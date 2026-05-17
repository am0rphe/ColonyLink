/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.inventory.tooltip.TooltipComponent
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.Item$TooltipContext
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.material.Fluid
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.OnlyIn
 */
package appeng.items.storage;

import appeng.api.client.AEKeyRendering;
import appeng.api.config.FuzzyMode;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.StorageCells;
import appeng.api.storage.cells.ICellWorkbenchItem;
import appeng.api.storage.cells.StorageCell;
import appeng.core.definitions.AEItems;
import appeng.core.localization.GuiText;
import appeng.core.localization.Tooltips;
import appeng.items.AEBaseItem;
import appeng.items.contents.CellConfig;
import appeng.me.cells.CreativeCellHandler;
import appeng.util.ConfigInventory;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class CreativeCellItem
extends AEBaseItem
implements ICellWorkbenchItem {
    public CreativeCellItem(Item.Properties props) {
        super(props);
    }

    @Override
    public ConfigInventory getConfigInventory(ItemStack is) {
        return CellConfig.create(is);
    }

    @Override
    public FuzzyMode getFuzzyMode(ItemStack is) {
        return FuzzyMode.IGNORE_ALL;
    }

    @Override
    public void setFuzzyMode(ItemStack is, FuzzyMode fzMode) {
    }

    @OnlyIn(value=Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> lines, TooltipFlag advancedTooltips) {
        ConfigInventory cc;
        StorageCell inventory = StorageCells.getCellInventory(stack, null);
        if (inventory != null && !(cc = this.getConfigInventory(stack)).isEmpty()) {
            if (Screen.hasShiftDown()) {
                for (AEKey key : cc.keySet()) {
                    lines.add((Component)Tooltips.of(AEKeyRendering.getDisplayName(key)));
                }
            } else {
                lines.add((Component)Tooltips.of(GuiText.PressShiftForFullList, new Object[0]));
            }
        }
    }

    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        return CreativeCellHandler.INSTANCE.getTooltipImage(stack);
    }

    public static ItemStack ofItems(ItemLike ... items) {
        ItemStack cell = AEItems.CREATIVE_CELL.stack();
        ConfigInventory configInv = AEItems.CREATIVE_CELL.get().getConfigInventory(cell);
        for (int i = 0; i < items.length; ++i) {
            configInv.setStack(i, GenericStack.fromItemStack(new ItemStack(items[i])));
        }
        return cell;
    }

    public static ItemStack ofFluids(Fluid ... fluids) {
        ItemStack cell = AEItems.CREATIVE_CELL.stack();
        ConfigInventory configInv = AEItems.CREATIVE_CELL.get().getConfigInventory(cell);
        for (int i = 0; i < fluids.length; ++i) {
            configInv.setStack(i, new GenericStack(AEFluidKey.of(fluids[i]), 1L));
        }
        return cell;
    }
}

