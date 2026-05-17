/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Direction
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.LevelAccessor
 *  net.minecraft.world.level.block.entity.BlockEntity
 */
package appeng.parts.p2p;

import appeng.api.networking.GridFlags;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.api.util.AECableType;
import appeng.core.AppEng;
import appeng.core.settings.TickRates;
import appeng.hooks.ticking.TickHandler;
import appeng.items.parts.PartModels;
import appeng.parts.AEBasePart;
import appeng.parts.p2p.P2PModels;
import appeng.parts.p2p.P2PTunnelPart;
import java.util.EnumSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;

public class MEP2PTunnelPart
extends P2PTunnelPart<MEP2PTunnelPart>
implements IGridTickable {
    private static final P2PModels MODELS = new P2PModels(AppEng.makeId("part/p2p/p2p_tunnel_me"));
    private ConnectionUpdate pendingUpdate = ConnectionUpdate.NONE;
    private final Map<MEP2PTunnelPart, IGridConnection> connections = new IdentityHashMap<MEP2PTunnelPart, IGridConnection>();
    private final IManagedGridNode outerNode = GridHelper.createManagedNode(this, AEBasePart.NodeListener.INSTANCE).setTagName("outer").setInWorldNode(true).setFlags(GridFlags.DENSE_CAPACITY, GridFlags.CANNOT_CARRY_COMPRESSED);

    @PartModels
    public static List<IPartModel> getModels() {
        return MODELS.getModels();
    }

    public MEP2PTunnelPart(IPartItem<?> partItem) {
        super(partItem);
        this.getMainNode().setFlags(GridFlags.REQUIRE_CHANNEL, GridFlags.COMPRESSED_CHANNEL).addService(IGridTickable.class, this);
    }

    @Override
    protected float getPowerDrainPerTick() {
        return 2.0f;
    }

    @Override
    public void readFromNBT(CompoundTag extra, HolderLookup.Provider registries) {
        super.readFromNBT(extra, registries);
        this.outerNode.loadFromNBT(extra);
    }

    @Override
    public void writeToNBT(CompoundTag extra, HolderLookup.Provider registries) {
        super.writeToNBT(extra, registries);
        this.outerNode.saveToNBT(extra);
    }

    @Override
    public void onTunnelNetworkChange() {
        super.onTunnelNetworkChange();
        if (!this.isOutput() || !this.connections.isEmpty()) {
            this.getMainNode().ifPresent((grid, node) -> grid.getTickManager().wakeDevice((IGridNode)node));
        }
    }

    @Override
    public AECableType getExternalCableConnectionType() {
        return AECableType.DENSE_SMART;
    }

    @Override
    public void removeFromWorld() {
        super.removeFromWorld();
        this.outerNode.destroy();
    }

    @Override
    public void addToWorld() {
        super.addToWorld();
        this.outerNode.create(this.getLevel(), this.getBlockEntity().getBlockPos());
    }

    @Override
    public void setPartHostInfo(Direction side, IPartHost host, BlockEntity blockEntity) {
        super.setPartHostInfo(side, host, blockEntity);
        this.outerNode.setExposedOnSides(EnumSet.of(side));
    }

    @Override
    public IGridNode getExternalFacingNode() {
        return this.outerNode.getNode();
    }

    @Override
    public void onPlacement(Player player) {
        super.onPlacement(player);
        this.outerNode.setOwningPlayer(player);
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(TickRates.METunnel, true);
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        this.pendingUpdate = !node.isOnline() ? ConnectionUpdate.DISCONNECT : ConnectionUpdate.CONNECT;
        TickHandler.instance().addCallable((LevelAccessor)this.getLevel(), this::updateConnections);
        return TickRateModulation.SLEEP;
    }

    private void updateConnections() {
        ConnectionUpdate operation = this.pendingUpdate;
        this.pendingUpdate = ConnectionUpdate.NONE;
        IGrid mainGrid = this.getMainNode().getGrid();
        if (this.isOutput()) {
            operation = ConnectionUpdate.DISCONNECT;
        } else if (mainGrid == null) {
            operation = ConnectionUpdate.DISCONNECT;
        }
        if (operation == ConnectionUpdate.DISCONNECT) {
            for (IGridConnection cw : this.connections.values()) {
                cw.destroy();
            }
            this.connections.clear();
        } else if (operation == ConnectionUpdate.CONNECT) {
            IGridConnection connection;
            List outputs = this.getOutputs();
            Iterator<Map.Entry<MEP2PTunnelPart, IGridConnection>> it = this.connections.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<MEP2PTunnelPart, IGridConnection> entry = it.next();
                MEP2PTunnelPart output = entry.getKey();
                connection = entry.getValue();
                if (output.getMainNode().getGrid() == mainGrid && output.getMainNode().isOnline() && outputs.contains(output)) continue;
                connection.destroy();
                it.remove();
            }
            for (MEP2PTunnelPart output : outputs) {
                if (!output.getMainNode().isOnline() || this.connections.containsKey(output)) continue;
                connection = GridHelper.createConnection(this.getExternalFacingNode(), output.getExternalFacingNode());
                this.connections.put(output, connection);
            }
        }
    }

    @Override
    public IPartModel getStaticModels() {
        return MODELS.getModel(this.isPowered(), this.isActive());
    }

    private static enum ConnectionUpdate {
        NONE,
        DISCONNECT,
        CONNECT;

    }
}

