/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.advancements.critereon.PlayerTrigger
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.server.level.ServerPlayer
 *  org.jetbrains.annotations.Nullable
 */
package appeng.me.service;

import appeng.api.features.IPlayerRegistry;
import appeng.api.networking.GridFlags;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridMultiblock;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.IGridServiceProvider;
import appeng.api.networking.events.GridBootingStatusChange;
import appeng.api.networking.events.GridChannelRequirementChanged;
import appeng.api.networking.events.GridControllerChange;
import appeng.api.networking.pathing.ChannelMode;
import appeng.api.networking.pathing.ControllerState;
import appeng.api.networking.pathing.IPathingService;
import appeng.blockentity.networking.ControllerBlockEntity;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.stats.AdvancementTriggers;
import appeng.me.Grid;
import appeng.me.pathfinding.AdHocChannelUpdater;
import appeng.me.pathfinding.ChannelFinalizer;
import appeng.me.pathfinding.ControllerValidator;
import appeng.me.pathfinding.PathingCalculation;
import appeng.me.service.AdHocNetworkError;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public class PathingService
implements IPathingService,
IGridServiceProvider {
    private static final String TAG_CHANNEL_MODE = "cm";
    private final Set<ControllerBlockEntity> controllers = new HashSet<ControllerBlockEntity>();
    private final Set<IGridNode> nodesNeedingChannels = new HashSet<IGridNode>();
    private final Set<IGridNode> cannotCarryCompressedNodes = new HashSet<IGridNode>();
    private final Grid grid;
    private int channelsInUse = 0;
    private int channelsByBlocks = 0;
    private double channelPowerUsage = 0.0;
    private boolean recalculateControllerNextTick = true;
    private boolean reboot = true;
    private boolean booting = false;
    @Nullable
    private AdHocNetworkError adHocNetworkError;
    private ControllerState controllerState = ControllerState.NO_CONTROLLER;
    private int lastChannels = 0;
    private boolean channelModeLocked;
    private ChannelMode channelMode = AEConfig.instance().getChannelMode();

    public PathingService(IGrid g) {
        this.grid = (Grid)g;
    }

    @Override
    public void onServerEndTick() {
        if (this.recalculateControllerNextTick) {
            this.updateControllerState();
        }
        if (this.reboot) {
            this.reboot = false;
            this.booting = true;
            this.postBootingStatusChange();
            this.channelsInUse = 0;
            this.adHocNetworkError = null;
            if (this.grid.isEmpty()) {
                return;
            }
            if (this.controllerState == ControllerState.NO_CONTROLLER) {
                this.channelsInUse = this.calculateAdHocChannels();
                int nodes = this.grid.size();
                this.channelsByBlocks = nodes * this.channelsInUse;
                this.setChannelPowerUsage((double)this.channelsByBlocks / 128.0);
                this.grid.getPivot().beginVisit(new AdHocChannelUpdater(this.channelsInUse));
            } else if (this.controllerState == ControllerState.CONTROLLER_CONFLICT) {
                this.grid.getPivot().beginVisit(new AdHocChannelUpdater(0));
                this.channelsInUse = 0;
                this.channelsByBlocks = 0;
            } else {
                PathingCalculation calculation = new PathingCalculation(this.grid);
                calculation.compute();
                this.channelsInUse = calculation.getChannelsInUse();
                this.channelsByBlocks = calculation.getChannelsByBlocks();
            }
            this.achievementPost();
            this.booting = false;
            this.setChannelPowerUsage((double)this.channelsByBlocks / 128.0);
            this.grid.getPivot().beginVisit(new ChannelFinalizer());
            this.postBootingStatusChange();
        }
    }

    private void postBootingStatusChange() {
        this.grid.postEvent(new GridBootingStatusChange(this.booting));
        this.grid.notifyAllNodes(IGridNodeListener.State.GRID_BOOT);
    }

    @Override
    public void removeNode(IGridNode gridNode) {
        Object object = gridNode.getOwner();
        if (object instanceof ControllerBlockEntity) {
            ControllerBlockEntity controller = (ControllerBlockEntity)object;
            this.controllers.remove(controller);
            this.recalculateControllerNextTick = true;
        }
        if (gridNode.hasFlag(GridFlags.REQUIRE_CHANNEL)) {
            this.nodesNeedingChannels.remove(gridNode);
        }
        if (gridNode.hasFlag(GridFlags.CANNOT_CARRY_COMPRESSED)) {
            this.cannotCarryCompressedNodes.remove(gridNode);
        }
        this.repath();
    }

    @Override
    public void addNode(IGridNode gridNode, @Nullable CompoundTag savedData) {
        Object object;
        if (savedData != null) {
            this.restoreChannelMode(savedData);
        }
        if ((object = gridNode.getOwner()) instanceof ControllerBlockEntity) {
            ControllerBlockEntity controller = (ControllerBlockEntity)object;
            this.controllers.add(controller);
            this.recalculateControllerNextTick = true;
        }
        if (gridNode.hasFlag(GridFlags.REQUIRE_CHANNEL)) {
            this.nodesNeedingChannels.add(gridNode);
        }
        if (gridNode.hasFlag(GridFlags.CANNOT_CARRY_COMPRESSED)) {
            this.cannotCarryCompressedNodes.add(gridNode);
        }
        this.repath();
    }

    private void restoreChannelMode(CompoundTag savedData) {
        if (savedData.contains(TAG_CHANNEL_MODE, 8)) {
            String channelModeName = savedData.getString(TAG_CHANNEL_MODE);
            try {
                ChannelMode nodeChannelMode = ChannelMode.valueOf(channelModeName);
                if (!this.channelModeLocked || nodeChannelMode.getAdHocNetworkChannels() > this.channelMode.getAdHocNetworkChannels()) {
                    this.channelModeLocked = true;
                    this.channelMode = nodeChannelMode;
                }
            }
            catch (IllegalArgumentException e) {
                AELog.warn("Invalid channel mode stored on grid node: %s", channelModeName);
            }
        }
    }

    private void updateControllerState() {
        this.recalculateControllerNextTick = false;
        ControllerState old = this.controllerState;
        this.controllerState = ControllerValidator.calculateState(this.controllers);
        if (old != this.controllerState) {
            this.grid.postEvent(new GridControllerChange());
        }
    }

    @Nullable
    public AdHocNetworkError getAdHocNetworkError() {
        return this.adHocNetworkError;
    }

    private int calculateAdHocChannels() {
        HashSet<IGridNode> ignore = new HashSet<IGridNode>();
        this.adHocNetworkError = null;
        int channels = 0;
        for (IGridNode node : this.nodesNeedingChannels) {
            IGridMultiblock multiblock;
            if (ignore.contains(node)) continue;
            if (node.hasFlag(GridFlags.COMPRESSED_CHANNEL) && !this.cannotCarryCompressedNodes.isEmpty()) {
                this.adHocNetworkError = AdHocNetworkError.NESTED_P2P_TUNNEL;
                return 0;
            }
            ++channels;
            if (!node.hasFlag(GridFlags.MULTIBLOCK) || (multiblock = node.getService(IGridMultiblock.class)) == null) continue;
            Iterator<IGridNode> it = multiblock.getMultiblockNodes();
            while (it.hasNext()) {
                ignore.add(it.next());
            }
        }
        if (channels > this.channelMode.getAdHocNetworkChannels()) {
            this.adHocNetworkError = AdHocNetworkError.TOO_MANY_CHANNELS;
            return 0;
        }
        return channels;
    }

    private void achievementPost() {
        PlayerTrigger lastBracket;
        PlayerTrigger currentBracket;
        MinecraftServer server = this.grid.getPivot().getLevel().getServer();
        if (this.lastChannels != this.channelsInUse && (currentBracket = this.getAchievementBracket(this.channelsInUse)) != (lastBracket = this.getAchievementBracket(this.lastChannels)) && currentBracket != null) {
            for (IGridNode n : this.nodesNeedingChannels) {
                ServerPlayer player = IPlayerRegistry.getConnected(server, n.getOwningPlayerId());
                if (player == null) continue;
                currentBracket.trigger(player);
            }
        }
        this.lastChannels = this.channelsInUse;
    }

    private PlayerTrigger getAchievementBracket(int ch) {
        if (ch < 8) {
            return null;
        }
        if (ch < 128) {
            return AdvancementTriggers.NETWORK_APPRENTICE;
        }
        if (ch < 2048) {
            return AdvancementTriggers.NETWORK_ENGINEER;
        }
        return AdvancementTriggers.NETWORK_ADMIN;
    }

    private void updateNodReq(GridChannelRequirementChanged ev) {
        IGridNode gridNode = ev.node;
        if (gridNode.hasFlag(GridFlags.REQUIRE_CHANNEL)) {
            this.nodesNeedingChannels.add(gridNode);
        } else {
            this.nodesNeedingChannels.remove(gridNode);
        }
        this.repath();
    }

    @Override
    public boolean isNetworkBooting() {
        return this.booting;
    }

    @Override
    public ControllerState getControllerState() {
        return this.controllerState;
    }

    @Override
    public void repath() {
        if (!this.channelModeLocked) {
            this.channelMode = AEConfig.instance().getChannelMode();
        }
        this.channelsByBlocks = 0;
        this.reboot = true;
    }

    double getChannelPowerUsage() {
        return this.channelPowerUsage;
    }

    private void setChannelPowerUsage(double channelPowerUsage) {
        this.channelPowerUsage = channelPowerUsage;
    }

    @Override
    public ChannelMode getChannelMode() {
        return this.channelMode;
    }

    public void setForcedChannelMode(@Nullable ChannelMode forcedChannelMode) {
        if (forcedChannelMode == null) {
            if (this.channelModeLocked) {
                this.channelModeLocked = false;
                this.repath();
            }
        } else {
            this.channelModeLocked = true;
            if (this.channelMode != forcedChannelMode) {
                this.channelMode = forcedChannelMode;
                this.repath();
            }
        }
    }

    @Override
    public int getUsedChannels() {
        return this.channelsInUse;
    }

    @Override
    public void saveNodeData(IGridNode gridNode, CompoundTag savedData) {
        if (this.channelModeLocked) {
            savedData.putString(TAG_CHANNEL_MODE, this.channelMode.name());
        }
    }

    static {
        GridHelper.addGridServiceEventHandler(GridChannelRequirementChanged.class, IPathingService.class, (service, event) -> ((PathingService)service).updateNodReq((GridChannelRequirementChanged)event));
    }
}

