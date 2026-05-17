/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.level.block.entity.BlockEntity
 */
package appeng.me.cluster;

import java.util.Iterator;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface IAECluster {
    public BlockPos getBoundsMin();

    public BlockPos getBoundsMax();

    public void updateStatus(boolean var1);

    public void destroy();

    public boolean isDestroyed();

    public Iterator<? extends BlockEntity> getBlockEntities();
}

