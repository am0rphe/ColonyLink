/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Iterators
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Direction$Axis
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockState
 *  net.neoforged.neoforge.client.model.data.ModelData
 *  net.neoforged.neoforge.client.model.data.ModelProperty
 */
package appeng.blockentity.spatial;

import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridMultiblock;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.orientation.BlockOrientation;
import appeng.blockentity.grid.AENetworkedBlockEntity;
import appeng.me.cluster.IAEMultiBlock;
import appeng.me.cluster.implementations.SpatialPylonCalculator;
import appeng.me.cluster.implementations.SpatialPylonCluster;
import appeng.me.helpers.IGridConnectedBlockEntity;
import appeng.util.iterators.ChainedIterator;
import com.google.common.collect.Iterators;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;

public class SpatialPylonBlockEntity
extends AENetworkedBlockEntity
implements IAEMultiBlock<SpatialPylonCluster> {
    public static final ModelProperty<ClientState> STATE = new ModelProperty(Objects::nonNull);
    private final SpatialPylonCalculator calc = new SpatialPylonCalculator(this);
    private SpatialPylonCluster cluster;
    private ClientState clientState = ClientState.DEFAULT;

    public SpatialPylonBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
        this.getMainNode().setFlags(GridFlags.REQUIRE_CHANNEL, GridFlags.MULTIBLOCK).setIdlePowerUsage(0.5).addService(IGridMultiblock.class, this::getMultiblockNodes);
    }

    @Override
    public void onChunkUnloaded() {
        this.disconnect(false);
        super.onChunkUnloaded();
    }

    @Override
    public void onReady() {
        super.onReady();
        Level level = this.level;
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            this.calc.calculateMultiblock(serverLevel, this.worldPosition);
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        this.disconnect(false);
    }

    public void neighborChanged(BlockPos changedPos) {
        Level level = this.level;
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            this.calc.updateMultiblockAfterNeighborUpdate(serverLevel, this.worldPosition, changedPos);
        }
    }

    @Override
    public void disconnect(boolean b) {
        if (this.cluster != null) {
            this.cluster.destroy();
            this.updateStatus(null);
        }
    }

    @Override
    public SpatialPylonCluster getCluster() {
        return this.cluster;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    public void updateStatus(SpatialPylonCluster c) {
        if (this.isRemoved()) {
            return;
        }
        this.cluster = c;
        this.onGridConnectableSidesChanged();
        this.recalculateDisplay();
    }

    @Override
    public Set<Direction> getGridConnectableSides(BlockOrientation orientation) {
        return this.cluster == null ? EnumSet.noneOf(Direction.class) : EnumSet.allOf(Direction.class);
    }

    public void recalculateDisplay() {
        ClientState state;
        AxisPosition pos = AxisPosition.NONE;
        Direction.Axis axis = Direction.Axis.X;
        boolean powered = false;
        boolean online = false;
        if (this.cluster != null) {
            pos = this.cluster.getBoundsMin().equals((Object)this.worldPosition) ? AxisPosition.START : (this.cluster.getBoundsMax().equals((Object)this.worldPosition) ? AxisPosition.END : AxisPosition.MIDDLE);
            switch (this.cluster.getCurrentAxis()) {
                case X: {
                    Direction.Axis axis2 = Direction.Axis.X;
                    break;
                }
                case Y: {
                    Direction.Axis axis2 = Direction.Axis.Y;
                    break;
                }
                case Z: {
                    Direction.Axis axis2 = Direction.Axis.Z;
                    break;
                }
                default: {
                    Direction.Axis axis2 = axis = axis;
                }
            }
            if (this.getMainNode().isPowered()) {
                powered = true;
            }
            if (this.cluster.isValid() && this.getMainNode().isOnline()) {
                online = true;
            }
        }
        if (!this.clientState.equals(state = new ClientState(powered, online, pos, axis))) {
            this.clientState = state;
            this.markForUpdate();
        }
    }

    @Override
    protected boolean readFromStream(RegistryFriendlyByteBuf data) {
        boolean c = super.readFromStream(data);
        ClientState state = ClientState.readFromStream((FriendlyByteBuf)data);
        if (!this.clientState.equals(state)) {
            this.clientState = state;
            return true;
        }
        return c;
    }

    @Override
    protected void writeToStream(RegistryFriendlyByteBuf data) {
        super.writeToStream(data);
        this.clientState.writeToStream((FriendlyByteBuf)data);
    }

    @Override
    protected void saveVisualState(CompoundTag data) {
        super.saveVisualState(data);
        this.clientState.writeToNbt(data);
    }

    @Override
    protected void loadVisualState(CompoundTag data) {
        super.loadVisualState(data);
        this.clientState = ClientState.readFromNbt(data);
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        if (reason != IGridNodeListener.State.GRID_BOOT) {
            this.recalculateDisplay();
        }
    }

    public ClientState getClientState() {
        return this.clientState;
    }

    @Override
    public ModelData getModelData() {
        return ModelData.builder().with(STATE, (Object)this.getClientState()).build();
    }

    private Iterator<IGridNode> getMultiblockNodes() {
        if (this.getCluster() == null) {
            return new ChainedIterator<IGridNode>(new IGridNode[0]);
        }
        return Iterators.transform(this.getCluster().getBlockEntities(), IGridConnectedBlockEntity::getGridNode);
    }

    public record ClientState(boolean powered, boolean online, AxisPosition axisPosition, Direction.Axis axis) {
        public static final ClientState DEFAULT = new ClientState(false, false, AxisPosition.NONE, Direction.Axis.X);

        public void writeToStream(FriendlyByteBuf buf) {
            buf.writeBoolean(this.powered);
            buf.writeBoolean(this.online);
            buf.writeEnum((Enum)this.axisPosition);
            buf.writeEnum((Enum)this.axis);
        }

        public static ClientState readFromStream(FriendlyByteBuf buf) {
            return new ClientState(buf.readBoolean(), buf.readBoolean(), (AxisPosition)buf.readEnum(AxisPosition.class), (Direction.Axis)buf.readEnum(Direction.Axis.class));
        }

        public void writeToNbt(CompoundTag tag) {
            tag.putBoolean("powered", this.powered);
            tag.putBoolean("online", this.online);
            tag.putString("axisPosition", this.axisPosition.name());
            tag.putString("axis", this.axis.name());
        }

        public static ClientState readFromNbt(CompoundTag tag) {
            Direction.Axis axis;
            AxisPosition axisPosition;
            boolean powered = tag.getBoolean("powered");
            boolean online = tag.getBoolean("online");
            String axisPositionName = tag.getString("axisPosition");
            try {
                axisPosition = Enum.valueOf(AxisPosition.class, axisPositionName);
            }
            catch (IllegalArgumentException ignored) {
                axisPosition = ClientState.DEFAULT.axisPosition;
            }
            String axisName = tag.getString("axis");
            try {
                axis = Enum.valueOf(Direction.Axis.class, axisName);
            }
            catch (IllegalArgumentException ignored) {
                axis = ClientState.DEFAULT.axis;
            }
            return new ClientState(powered, online, axisPosition, axis);
        }
    }

    public static enum AxisPosition {
        NONE,
        START,
        MIDDLE,
        END;

    }
}

