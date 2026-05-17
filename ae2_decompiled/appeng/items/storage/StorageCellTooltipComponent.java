/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.inventory.tooltip.TooltipComponent
 *  net.minecraft.world.item.ItemStack
 */
package appeng.items.storage;

import appeng.api.stacks.GenericStack;
import java.util.List;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

public record StorageCellTooltipComponent(List<ItemStack> upgrades, List<GenericStack> content, boolean hasMoreContent, boolean showAmounts) implements TooltipComponent
{
}

