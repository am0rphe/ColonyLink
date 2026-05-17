/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.ItemStack
 *  org.jetbrains.annotations.Nullable
 */
package appeng.api.storage.cells;

import appeng.api.storage.cells.ISaveProvider;
import appeng.api.storage.cells.StorageCell;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface ICellHandler {
    public boolean isCell(ItemStack var1);

    @Nullable
    public StorageCell getCellInventory(ItemStack var1, @Nullable ISaveProvider var2);
}

