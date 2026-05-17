/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.Item
 *  org.jetbrains.annotations.Nullable
 */
package appeng.api.implementations.blockentities;

import appeng.api.networking.security.IActionHost;
import appeng.api.storage.MEStorage;
import appeng.api.storage.cells.CellState;
import appeng.api.storage.cells.StorageCell;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

public interface IChestOrDrive
extends IActionHost {
    public int getCellCount();

    public CellState getCellStatus(int var1);

    public boolean isPowered();

    public boolean isCellBlinking(int var1);

    @Nullable
    public Item getCellItem(int var1);

    @Nullable
    public MEStorage getCellInventory(int var1);

    @Nullable
    public StorageCell getOriginalCellInventory(int var1);
}

