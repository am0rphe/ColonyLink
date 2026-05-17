/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Preconditions
 *  com.google.common.collect.ClassToInstanceMap
 *  com.google.common.collect.ImmutableSet
 *  com.google.common.collect.MutableClassToInstanceMap
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 *  org.jetbrains.annotations.Nullable
 */
package appeng.me;

import appeng.api.features.IPlayerRegistry;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.IGridNodeService;
import appeng.api.networking.IManagedGridNode;
import appeng.api.stacks.AEItemKey;
import appeng.api.util.AEColor;
import appeng.core.AELog;
import appeng.me.GridNode;
import appeng.me.InWorldGridNode;
import com.google.common.base.Preconditions;
import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.MutableClassToInstanceMap;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class ManagedGridNode
implements IManagedGridNode {
    @Nullable
    private InitData<?> initData;
    private String tagName = "gn";
    @Nullable
    private GridNode node = null;

    public <T> ManagedGridNode(T nodeOwner, IGridNodeListener<? super T> listener) {
        this.initData = new InitData<T>(nodeOwner, listener);
    }

    @Override
    public ManagedGridNode setInWorldNode(boolean accessible) {
        this.getInitData().inWorldNode = accessible;
        return this;
    }

    @Override
    public ManagedGridNode setTagName(String tagName) {
        if (this.getInitData().data != null) {
            throw new IllegalStateException("Cannot change tag name after NBT has already been read.");
        }
        this.tagName = Objects.requireNonNull(tagName);
        return this;
    }

    @Override
    public void destroy() {
        if (this.node != null) {
            this.node.destroy();
            this.node = null;
        }
    }

    @Override
    public void create(Level level, @Nullable BlockPos blockPos) {
        InitData<?> initData = this.getInitData();
        initData.level = level;
        initData.pos = blockPos;
        this.initData = null;
        if (this.node == null && !initData.level.isClientSide()) {
            this.createNode(initData);
        }
    }

    private void createNode(InitData<?> initData) {
        Preconditions.checkState((this.node == null ? 1 : 0) != 0);
        GridNode node = initData.createNode();
        if (initData.data != null) {
            node.loadFromNBT(this.tagName, initData.data);
        }
        this.node = node;
        this.node.markReady();
    }

    @Override
    public IGridNode getNode() {
        return this.node;
    }

    @Override
    public void loadFromNBT(CompoundTag tag) {
        if (this.node == null) {
            this.getInitData().data = tag;
        } else {
            this.node.loadFromNBT(this.tagName, tag);
        }
    }

    @Override
    public void saveToNBT(CompoundTag tag) {
        if (this.node != null) {
            this.node.saveToNBT(this.tagName, tag);
        }
    }

    @Override
    public boolean isReady() {
        return this.initData == null && this.node != null;
    }

    @Override
    public boolean isActive() {
        if (this.node == null) {
            return false;
        }
        return this.node.isActive();
    }

    @Override
    public boolean isOnline() {
        if (this.node == null) {
            return false;
        }
        return this.node.isOnline();
    }

    @Override
    public boolean isPowered() {
        IGrid grid = this.getGrid();
        return grid != null && grid.getEnergyService().isNetworkPowered();
    }

    @Override
    public boolean hasGridBooted() {
        if (this.node == null) {
            return false;
        }
        return this.node.hasGridBooted();
    }

    @Override
    public void setOwningPlayerId(int ownerPlayerId) {
        if (this.initData != null) {
            this.getInitData().owner = ownerPlayerId;
        } else if (this.node != null) {
            this.node.setOwningPlayerId(ownerPlayerId);
        }
    }

    @Override
    public void setOwningPlayer(Player player) {
        if (player instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)player;
            this.setOwningPlayerId(IPlayerRegistry.getPlayerId(serverPlayer));
        }
    }

    @Override
    public ManagedGridNode setFlags(GridFlags ... flags) {
        EnumSet<GridFlags> flagSet = EnumSet.noneOf(GridFlags.class);
        Collections.addAll(flagSet, flags);
        this.getInitData().flags = flagSet;
        return this;
    }

    @Override
    public ManagedGridNode setExposedOnSides(Set<Direction> directions) {
        if (this.node == null) {
            this.getInitData().exposedOnSides = ImmutableSet.copyOf(directions);
        } else {
            GridNode gridNode = this.node;
            if (gridNode instanceof InWorldGridNode) {
                InWorldGridNode inWorldNode = (InWorldGridNode)gridNode;
                inWorldNode.setExposedOnSides(directions);
            }
        }
        return this;
    }

    @Override
    public ManagedGridNode setIdlePowerUsage(double usagePerTick) {
        Preconditions.checkArgument((usagePerTick >= 0.0 ? 1 : 0) != 0, (Object)"usagePerTick must be >= 0");
        if (this.node == null) {
            this.getInitData().idlePowerUsage = usagePerTick;
        } else {
            this.node.setIdlePowerUsage(usagePerTick);
        }
        return this;
    }

    @Override
    public ManagedGridNode setVisualRepresentation(@Nullable AEItemKey visualRepresentation) {
        if (this.node == null) {
            this.getInitData().visualRepresentation = visualRepresentation;
        } else {
            this.node.setVisualRepresentation(visualRepresentation);
        }
        return this;
    }

    @Override
    public ManagedGridNode setGridColor(AEColor gridColor) {
        if (this.node == null) {
            this.getInitData().gridColor = gridColor;
        } else {
            this.node.setGridColor(gridColor);
        }
        return this;
    }

    public double getIdlePowerUsage() {
        return this.node != null ? this.node.getIdlePowerUsage() : this.getInitData().idlePowerUsage;
    }

    private InitData<?> getInitData() {
        if (this.initData == null) {
            throw new IllegalStateException("The node has already been initialized. Initialization data cannot be changed anymore.");
        }
        return this.initData;
    }

    @Override
    public <T extends IGridNodeService> ManagedGridNode addService(Class<T> serviceClass, T service) {
        InitData<?> initData = this.getInitData();
        if (initData.services == null) {
            initData.services = MutableClassToInstanceMap.create();
        }
        initData.services.putInstance(serviceClass, service);
        return this;
    }

    public AEColor getGridColor() {
        if (this.node == null) {
            return this.getInitData().gridColor;
        }
        return this.node.getGridColor();
    }

    private static class InitData<T> {
        private final T logicalHost;
        private final IGridNodeListener<T> listener;
        public ClassToInstanceMap<IGridNodeService> services;
        private CompoundTag data = null;
        private AEColor gridColor = AEColor.TRANSPARENT;
        private Set<Direction> exposedOnSides = EnumSet.allOf(Direction.class);
        private AEItemKey visualRepresentation = null;
        private EnumSet<GridFlags> flags = EnumSet.noneOf(GridFlags.class);
        private double idlePowerUsage = 1.0;
        private int owner = -1;
        private Level level;
        private BlockPos pos;
        private boolean inWorldNode;

        public InitData(T logicalHost, IGridNodeListener<T> listener) {
            this.logicalHost = Objects.requireNonNull(logicalHost);
            this.listener = Objects.requireNonNull(listener);
        }

        public GridNode createNode() {
            GridNode node;
            if (this.inWorldNode) {
                Preconditions.checkState((this.pos != null ? 1 : 0) != 0, (Object)"No position was set for an in-world node");
                InWorldGridNode inWorldNode = new InWorldGridNode((ServerLevel)this.level, this.pos, this.logicalHost, this.listener, this.flags);
                inWorldNode.setExposedOnSides(this.exposedOnSides);
                node = inWorldNode;
            } else {
                node = new GridNode((ServerLevel)this.level, this.logicalHost, this.listener, this.flags);
            }
            node.setGridColor(this.gridColor);
            node.setOwningPlayerId(this.owner);
            node.setIdlePowerUsage(this.idlePowerUsage);
            node.setVisualRepresentation(this.visualRepresentation);
            if (this.services != null) {
                for (Class serviceClass : this.services.keySet()) {
                    this.addService(node, serviceClass);
                }
            }
            AELog.grid("Created node %s", node);
            return node;
        }

        private <SC extends IGridNodeService> void addService(GridNode node, Class<SC> serviceClass) {
            node.addService(serviceClass, (IGridNodeService)this.services.getInstance(serviceClass));
        }
    }
}

