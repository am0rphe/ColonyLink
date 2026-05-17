/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.level.block.entity.BlockEntity
 */
package appeng.me.cluster.implementations;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.events.GridCraftingCpuChange;
import appeng.blockentity.crafting.CraftingBlockEntity;
import appeng.me.cluster.MBCalculator;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import java.util.Iterator;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;

public class CraftingCPUCalculator
extends MBCalculator<CraftingBlockEntity, CraftingCPUCluster> {
    public CraftingCPUCalculator(CraftingBlockEntity t) {
        super(t);
    }

    @Override
    public boolean checkMultiblockScale(BlockPos min, BlockPos max) {
        if (max.getX() - min.getX() > 16) {
            return false;
        }
        if (max.getY() - min.getY() > 16) {
            return false;
        }
        return max.getZ() - min.getZ() <= 16;
    }

    @Override
    public CraftingCPUCluster createCluster(ServerLevel level, BlockPos min, BlockPos max) {
        return new CraftingCPUCluster(min, max);
    }

    @Override
    public boolean verifyInternalStructure(ServerLevel level, BlockPos min, BlockPos max) {
        boolean storage = false;
        for (BlockPos blockPos : BlockPos.betweenClosed((BlockPos)min, (BlockPos)max)) {
            BlockEntity blockEntity = level.getBlockEntity(blockPos);
            if (!(blockEntity instanceof CraftingBlockEntity)) {
                return false;
            }
            CraftingBlockEntity craftingBlockEntity = (CraftingBlockEntity)blockEntity;
            storage |= craftingBlockEntity.getStorageBytes() > 0L;
        }
        return storage;
    }

    @Override
    public void updateBlockEntities(CraftingCPUCluster c, ServerLevel level, BlockPos min, BlockPos max) {
        for (BlockPos blockPos : BlockPos.betweenClosed((BlockPos)min, (BlockPos)max)) {
            CraftingBlockEntity te = (CraftingBlockEntity)level.getBlockEntity(blockPos);
            te.updateStatus(c);
            c.addBlockEntity(te);
        }
        c.done();
        Iterator<CraftingBlockEntity> i = c.getBlockEntities();
        while (i.hasNext()) {
            CraftingBlockEntity gh = i.next();
            IGridNode n = gh.getGridNode();
            if (n == null) continue;
            IGrid g = n.getGrid();
            g.postEvent(new GridCraftingCpuChange(n));
            return;
        }
    }

    @Override
    public boolean isValidBlockEntity(BlockEntity te) {
        return te instanceof CraftingBlockEntity;
    }
}

