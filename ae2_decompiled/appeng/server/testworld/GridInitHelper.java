/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.level.LevelAccessor
 *  net.minecraft.world.level.block.entity.BlockEntity
 */
package appeng.server.testworld;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IManagedGridNode;
import appeng.api.parts.IPart;
import appeng.blockentity.networking.CableBusBlockEntity;
import appeng.hooks.ticking.TickHandler;
import appeng.me.helpers.IGridConnectedBlockEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;

final class GridInitHelper {
    GridInitHelper() {
    }

    static void doAfterGridInit(final ServerLevel level, final List<BlockPos> positions, final boolean waitForActive, final BiConsumer<IGrid, IGridNode> consumer) {
        Runnable delayedAction = new Runnable(){
            private int attempts = 120;

            /*
             * Enabled force condition propagation
             * Lifted jumps to return sites
             */
            @Override
            public void run() {
                ArrayList<IGridNode> gridNodes = new ArrayList<IGridNode>();
                for (BlockPos position : positions) {
                    BlockEntity be = level.getBlockEntity(position);
                    if (be instanceof IGridConnectedBlockEntity) {
                        IGridConnectedBlockEntity host = (IGridConnectedBlockEntity)be;
                        gridNodes.add(host.getMainNode().getNode());
                        continue;
                    }
                    if (!(be instanceof CableBusBlockEntity)) return;
                    CableBusBlockEntity cableBus = (CableBusBlockEntity)be;
                    IPart centerPart = cableBus.getCableBus().getPart(null);
                    if (centerPart == null) return;
                    gridNodes.add(centerPart.getGridNode());
                }
                if (gridNodes.stream().anyMatch(Objects::isNull) || waitForActive && !gridNodes.stream().allMatch(IGridNode::isActive)) {
                    if (--this.attempts <= 0) throw new IllegalStateException("Couldn't access grid nodes @ " + String.valueOf(positions));
                    TickHandler.instance().addCallable((LevelAccessor)level, this);
                    return;
                } else {
                    consumer.accept(((IGridNode)gridNodes.getFirst()).getGrid(), (IGridNode)gridNodes.getFirst());
                }
            }
        };
        TickHandler.instance().addCallable((LevelAccessor)level, delayedAction);
    }

    static void doAfterGridInit(final ServerLevel level, final List<BlockEntity> blockEntities, final boolean waitForActive, final Runnable callback) {
        Runnable delayedAction = new Runnable(){
            private int attempts = 120;

            /*
             * Enabled force condition propagation
             * Lifted jumps to return sites
             */
            @Override
            public void run() {
                ArrayList<BlockEntity> notInitialized = new ArrayList<BlockEntity>();
                ArrayList<BlockEntity> notActive = new ArrayList<BlockEntity>();
                for (BlockEntity be : blockEntities) {
                    CableBusBlockEntity cableBus;
                    IPart centerPart;
                    if (be.isRemoved()) {
                        return;
                    }
                    if (be instanceof IGridConnectedBlockEntity) {
                        IGridConnectedBlockEntity host = (IGridConnectedBlockEntity)be;
                        IManagedGridNode mainNode = host.getMainNode();
                        if (mainNode == null) {
                            notInitialized.add(be);
                            notActive.add(be);
                            break;
                        }
                        if (mainNode.isActive()) continue;
                        notActive.add(be);
                        continue;
                    }
                    if (!(be instanceof CableBusBlockEntity) || (centerPart = (cableBus = (CableBusBlockEntity)be).getCableBus().getPart(null)) == null) continue;
                    IGridNode mainNode = centerPart.getGridNode();
                    if (mainNode == null) {
                        notInitialized.add(be);
                        notActive.add(be);
                        break;
                    }
                    if (mainNode.isActive()) continue;
                    notActive.add(be);
                }
                if (!notInitialized.isEmpty() || waitForActive && !notActive.isEmpty()) {
                    if (--this.attempts <= 0) throw new IllegalStateException("Couldn't wait for grid to initialize. Not initialized: " + String.valueOf(notInitialized) + ". Not active: " + String.valueOf(notActive));
                    TickHandler.instance().addCallable((LevelAccessor)level, this);
                    return;
                } else {
                    callback.run();
                }
            }
        };
        TickHandler.instance().addCallable((LevelAccessor)level, delayedAction);
    }
}

