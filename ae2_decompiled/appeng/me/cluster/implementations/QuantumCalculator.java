/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.block.entity.BlockEntity
 */
package appeng.me.cluster.implementations;

import appeng.blockentity.qnb.QuantumBridgeBlockEntity;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.BlockDefinition;
import appeng.me.cluster.IAEMultiBlock;
import appeng.me.cluster.MBCalculator;
import appeng.me.cluster.implementations.QuantumCluster;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;

public class QuantumCalculator
extends MBCalculator<QuantumBridgeBlockEntity, QuantumCluster> {
    public QuantumCalculator(QuantumBridgeBlockEntity t) {
        super(t);
    }

    @Override
    public boolean checkMultiblockScale(BlockPos min, BlockPos max) {
        if ((max.getX() - min.getX() + 1) * (max.getY() - min.getY() + 1) * (max.getZ() - min.getZ() + 1) == 9) {
            int ones = (max.getX() - min.getX() == 0 ? 1 : 0) + (max.getY() - min.getY() == 0 ? 1 : 0) + (max.getZ() - min.getZ() == 0 ? 1 : 0);
            int threes = (max.getX() - min.getX() == 2 ? 1 : 0) + (max.getY() - min.getY() == 2 ? 1 : 0) + (max.getZ() - min.getZ() == 2 ? 1 : 0);
            return ones == 1 && threes == 2;
        }
        return false;
    }

    @Override
    public QuantumCluster createCluster(ServerLevel level, BlockPos min, BlockPos max) {
        return new QuantumCluster(min, max);
    }

    @Override
    public boolean verifyInternalStructure(ServerLevel level, BlockPos min, BlockPos max) {
        int num = 0;
        for (BlockPos p : BlockPos.betweenClosed((BlockPos)min, (BlockPos)max)) {
            IAEMultiBlock te = (IAEMultiBlock)level.getBlockEntity(p);
            if (te == null || !te.isValid()) {
                return false;
            }
            if (!((num = (int)((byte)(num + 1))) == 5 ? !this.isBlockAtLocation((BlockGetter)level, p, AEBlocks.QUANTUM_LINK) : !this.isBlockAtLocation((BlockGetter)level, p, AEBlocks.QUANTUM_RING))) continue;
            return false;
        }
        return true;
    }

    @Override
    public void updateBlockEntities(QuantumCluster c, ServerLevel level, BlockPos min, BlockPos max) {
        byte num = 0;
        int ringNum = 0;
        for (BlockPos p : BlockPos.betweenClosed((BlockPos)min, (BlockPos)max)) {
            byte flags;
            QuantumBridgeBlockEntity te = (QuantumBridgeBlockEntity)level.getBlockEntity(p);
            if ((num = (byte)((byte)(num + 1))) == 5) {
                flags = num;
                c.setCenter(te);
            } else {
                flags = num == 1 || num == 3 || num == 7 || num == 9 ? (byte)(((QuantumBridgeBlockEntity)this.target).getCorner() | num) : num;
                c.getRing()[ringNum] = te;
                ringNum = (byte)(ringNum + 1);
            }
            te.updateStatus(c, flags, true);
        }
    }

    @Override
    public boolean isValidBlockEntity(BlockEntity te) {
        return te instanceof QuantumBridgeBlockEntity;
    }

    private boolean isBlockAtLocation(BlockGetter level, BlockPos pos, BlockDefinition<?> def) {
        return def.block() == level.getBlockState(pos).getBlock();
    }
}

