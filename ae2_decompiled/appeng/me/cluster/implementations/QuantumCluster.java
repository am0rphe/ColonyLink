/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.neoforge.common.NeoForge
 *  net.neoforged.neoforge.event.level.LevelEvent$Unload
 *  org.jetbrains.annotations.Nullable
 */
package appeng.me.cluster.implementations;

import appeng.api.features.Locatables;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.blockentity.qnb.QuantumBridgeBlockEntity;
import appeng.core.AELog;
import appeng.me.cluster.IAECluster;
import appeng.me.cluster.MBCalculator;
import appeng.me.service.helpers.ConnectionWrapper;
import appeng.util.iterators.ChainedIterator;
import java.util.Iterator;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.LevelEvent;
import org.jetbrains.annotations.Nullable;

public class QuantumCluster
implements IAECluster,
IActionHost {
    private final BlockPos boundsMin;
    private final BlockPos boundsMax;
    private boolean isDestroyed = false;
    private boolean updateStatus = true;
    private QuantumBridgeBlockEntity[] Ring;
    private boolean registered = false;
    private ConnectionWrapper connection;
    private long thisSide;
    private long otherSide;
    private QuantumBridgeBlockEntity center;

    public QuantumCluster(BlockPos min, BlockPos max) {
        this.boundsMin = min.immutable();
        this.boundsMax = max.immutable();
        this.setRing(new QuantumBridgeBlockEntity[8]);
    }

    @SubscribeEvent
    public void onUnload(LevelEvent.Unload e) {
        if (this.center != null && this.center.getLevel() == e.getLevel()) {
            this.setUpdateStatus(false);
            this.destroy();
        }
    }

    @Override
    public void updateStatus(boolean updateGrid) {
        long qe = this.center.getQEFrequency();
        if (this.thisSide != qe && this.thisSide != -qe) {
            if (qe != 0L) {
                if (this.thisSide != 0L) {
                    Locatables.quantumNetworkBridges().unregister(this.center.getLevel(), this.getLocatableKey());
                }
                if (this.canUseNode(-qe)) {
                    this.otherSide = qe;
                    this.thisSide = -qe;
                } else if (this.canUseNode(qe)) {
                    this.thisSide = qe;
                    this.otherSide = -qe;
                }
                Locatables.quantumNetworkBridges().register(this.center.getLevel(), this.getLocatableKey(), this);
            } else {
                Locatables.quantumNetworkBridges().unregister(this.center.getLevel(), this.getLocatableKey());
                this.otherSide = 0L;
                this.thisSide = 0L;
            }
        }
        IActionHost myOtherSide = this.otherSide == 0L ? null : Locatables.quantumNetworkBridges().get(this.center.getLevel(), this.otherSide);
        boolean shutdown = false;
        if (myOtherSide instanceof QuantumCluster) {
            QuantumCluster sideB = (QuantumCluster)myOtherSide;
            QuantumCluster sideA = this;
            if (sideA.isActive() && sideB.isActive()) {
                if (this.connection != null && this.connection.getConnection() != null) {
                    IGridNode a = this.connection.getConnection().a();
                    IGridNode b = this.connection.getConnection().b();
                    IGridNode sa = sideA.getNode();
                    IGridNode sb = sideB.getNode();
                    if (!(a != sa && b != sa || a != sb && b != sb)) {
                        return;
                    }
                }
                if (sideA.connection != null && sideA.connection.getConnection() != null) {
                    sideA.connection.getConnection().destroy();
                    sideA.connection = new ConnectionWrapper(null);
                }
                if (sideB.connection != null && sideB.connection.getConnection() != null) {
                    sideB.connection.getConnection().destroy();
                    sideB.connection = new ConnectionWrapper(null);
                }
                sideA.connection = sideB.connection = new ConnectionWrapper(GridHelper.createConnection(sideA.getNode(), sideB.getNode()));
            } else {
                shutdown = true;
            }
        } else {
            shutdown = true;
        }
        if (shutdown && this.connection != null && this.connection.getConnection() != null) {
            this.connection.getConnection().destroy();
            this.connection.setConnection(null);
            this.connection = new ConnectionWrapper(null);
        }
    }

    private boolean canUseNode(long qe) {
        IActionHost locatable = Locatables.quantumNetworkBridges().get(this.center.getLevel(), qe);
        if (locatable instanceof QuantumCluster) {
            QuantumCluster qc = (QuantumCluster)locatable;
            Level level = qc.center.getLevel();
            if (!qc.isDestroyed) {
                if (level.hasChunkAt(qc.center.getBlockPos())) {
                    ServerLevel cur = level.getServer().getLevel(level.dimension());
                    BlockEntity te = level.getBlockEntity(qc.center.getBlockPos());
                    return te != qc.center || level != cur;
                }
                AELog.warn("Found a registered QNB with serial %s whose chunk seems to be unloaded: %s", qe, qc);
            }
        }
        return true;
    }

    private boolean isActive() {
        return !this.isDestroyed && this.registered && this.hasQES() && this.getNode() != null;
    }

    private IGridNode getNode() {
        return this.center.getGridNode();
    }

    private boolean hasQES() {
        return this.thisSide != 0L;
    }

    @Override
    public BlockPos getBoundsMin() {
        return this.boundsMin;
    }

    @Override
    public BlockPos getBoundsMax() {
        return this.boundsMax;
    }

    @Override
    public boolean isDestroyed() {
        return this.isDestroyed;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void destroy() {
        if (this.isDestroyed) {
            return;
        }
        this.isDestroyed = true;
        MBCalculator.setModificationInProgress(this);
        try {
            if (this.registered) {
                NeoForge.EVENT_BUS.unregister((Object)this);
                this.registered = false;
            }
            if (this.thisSide != 0L) {
                this.updateStatus(true);
                Locatables.quantumNetworkBridges().unregister(this.center.getLevel(), this.getLocatableKey());
            }
            this.center.updateStatus(null, (byte)-1, this.isUpdateStatus());
            for (QuantumBridgeBlockEntity r : this.getRing()) {
                r.updateStatus(null, (byte)-1, this.isUpdateStatus());
            }
            this.center = null;
            this.setRing(new QuantumBridgeBlockEntity[8]);
        }
        finally {
            MBCalculator.setModificationInProgress(null);
        }
    }

    public Iterator<QuantumBridgeBlockEntity> getBlockEntities() {
        return new ChainedIterator<QuantumBridgeBlockEntity>(this.getRing()[0], this.getRing()[1], this.getRing()[2], this.getRing()[3], this.getRing()[4], this.getRing()[5], this.getRing()[6], this.getRing()[7], this.center);
    }

    public boolean isCorner(QuantumBridgeBlockEntity quantumBridge) {
        return this.getRing()[0] == quantumBridge || this.getRing()[2] == quantumBridge || this.getRing()[4] == quantumBridge || this.getRing()[6] == quantumBridge;
    }

    private long getLocatableKey() {
        return this.thisSide;
    }

    public QuantumBridgeBlockEntity getCenter() {
        return this.center;
    }

    void setCenter(QuantumBridgeBlockEntity c) {
        this.registered = true;
        NeoForge.EVENT_BUS.register((Object)this);
        this.center = c;
    }

    private boolean isUpdateStatus() {
        return this.updateStatus;
    }

    public void setUpdateStatus(boolean updateStatus) {
        this.updateStatus = updateStatus;
    }

    QuantumBridgeBlockEntity[] getRing() {
        return this.Ring;
    }

    private void setRing(QuantumBridgeBlockEntity[] ring) {
        this.Ring = ring;
    }

    public String toString() {
        if (this.center == null) {
            return "QuantumCluster{no-center}";
        }
        Level level = this.center.getLevel();
        BlockPos pos = this.center.getBlockPos();
        return "QuantumCluster{" + String.valueOf(level) + "," + String.valueOf(pos) + "}";
    }

    @Override
    @Nullable
    public IGridNode getActionableNode() {
        return this.center.getMainNode().getNode();
    }
}

