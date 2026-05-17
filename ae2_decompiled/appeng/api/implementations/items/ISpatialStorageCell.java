/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.item.ItemStack
 */
package appeng.api.implementations.items;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

public interface ISpatialStorageCell {
    public boolean isSpatialStorage(ItemStack var1);

    public int getMaxStoredDim(ItemStack var1);

    public int getAllocatedPlotId(ItemStack var1);

    public boolean doSpatialTransition(ItemStack var1, ServerLevel var2, BlockPos var3, BlockPos var4, int var5);
}

