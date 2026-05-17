/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.level.block.entity.BlockEntity
 */
package appeng.me.cluster.implementations;

import appeng.blockentity.spatial.SpatialPylonBlockEntity;
import appeng.me.cluster.IAEMultiBlock;
import appeng.me.cluster.MBCalculator;
import appeng.me.cluster.implementations.SpatialPylonCluster;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;

public class SpatialPylonCalculator
extends MBCalculator<SpatialPylonBlockEntity, SpatialPylonCluster> {
    public SpatialPylonCalculator(SpatialPylonBlockEntity t) {
        super(t);
    }

    @Override
    public boolean checkMultiblockScale(BlockPos min, BlockPos max) {
        return min.getX() == max.getX() && min.getY() == max.getY() && min.getZ() != max.getZ() || min.getX() == max.getX() && min.getY() != max.getY() && min.getZ() == max.getZ() || min.getX() != max.getX() && min.getY() == max.getY() && min.getZ() == max.getZ();
    }

    @Override
    public SpatialPylonCluster createCluster(ServerLevel level, BlockPos min, BlockPos max) {
        return new SpatialPylonCluster(level, min, max);
    }

    @Override
    public boolean verifyInternalStructure(ServerLevel level, BlockPos min, BlockPos max) {
        for (BlockPos p : BlockPos.betweenClosed((BlockPos)min, (BlockPos)max)) {
            IAEMultiBlock te = (IAEMultiBlock)level.getBlockEntity(p);
            if (te != null && te.isValid()) continue;
            return false;
        }
        return true;
    }

    @Override
    public void updateBlockEntities(SpatialPylonCluster c, ServerLevel level, BlockPos min, BlockPos max) {
        for (BlockPos p : BlockPos.betweenClosed((BlockPos)min, (BlockPos)max)) {
            SpatialPylonBlockEntity te = (SpatialPylonBlockEntity)level.getBlockEntity(p);
            te.updateStatus(c);
            c.getLine().add(te);
        }
    }

    @Override
    public boolean isValidBlockEntity(BlockEntity te) {
        return te instanceof SpatialPylonBlockEntity;
    }
}

