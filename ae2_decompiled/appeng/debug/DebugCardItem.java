/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Iterables
 *  com.google.common.math.StatsAccumulator
 *  net.minecraft.ChatFormatting
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.InteractionResultHolder
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.context.UseOnContext
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.entity.BlockEntity
 */
package appeng.debug;

import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.networking.energy.IAEPowerStorage;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.pathing.ControllerState;
import appeng.api.networking.pathing.IPathingService;
import appeng.api.parts.IPartHost;
import appeng.blockentity.AEBaseBlockEntity;
import appeng.blockentity.networking.ControllerBlockEntity;
import appeng.hooks.ticking.TickHandler;
import appeng.items.AEBaseItem;
import appeng.me.Grid;
import appeng.me.GridNode;
import appeng.me.helpers.IGridConnectedBlockEntity;
import appeng.me.service.TickManagerService;
import appeng.parts.networking.CablePart;
import appeng.parts.p2p.P2PTunnelPart;
import appeng.util.InteractionUtil;
import appeng.util.Platform;
import com.google.common.collect.Iterables;
import com.google.common.math.StatsAccumulator;
import java.util.HashSet;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class DebugCardItem
extends AEBaseItem {
    public DebugCardItem(Item.Properties properties) {
        super(properties);
    }

    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        if (InteractionUtil.isInAlternateUseMode(player) && !level.isClientSide) {
            int grids = 0;
            StatsAccumulator stats = new StatsAccumulator();
            for (Grid g : TickHandler.instance().getGridList()) {
                ++grids;
                stats.add((double)g.size());
            }
            this.divider(player);
            this.outputMessage((Entity)player, "Grids", ChatFormatting.BOLD);
            this.outputSecondaryMessage((Entity)player, "Grids", Integer.toString(grids));
            if (stats.count() > 0L) {
                this.outputSecondaryMessage((Entity)player, "Total Nodes", "" + (long)stats.sum());
                this.outputSecondaryMessage((Entity)player, "Mean Nodes", "" + (long)stats.mean());
                this.outputSecondaryMessage((Entity)player, "Max Nodes", "" + (long)stats.max());
            }
            this.divider(player);
            this.outputMessage((Entity)player, "Ticking", ChatFormatting.BOLD);
            this.outputSecondaryMessage((Entity)player, "Current Tick: ", Long.toString(TickHandler.instance().getCurrentTick()));
            for (Component line : TickHandler.instance().getBlockEntityReport()) {
                player.sendSystemMessage(line);
            }
        }
        return InteractionResultHolder.sidedSuccess((Object)player.getItemInHand(usedHand), (boolean)level.isClientSide);
    }

    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Object center;
        if (context.getLevel().isClientSide()) {
            return InteractionResult.PASS;
        }
        Player player = context.getPlayer();
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction side = context.getClickedFace();
        if (player == null || InteractionUtil.isInAlternateUseMode(player)) {
            return InteractionResult.PASS;
        }
        IInWorldGridNodeHost gh = GridHelper.getNodeHost(level, pos);
        if (gh != null) {
            this.divider(player);
            GridNode node = (GridNode)gh.getGridNode(side);
            if (node == null && gh instanceof IGridConnectedBlockEntity) {
                IGridConnectedBlockEntity gridConnectedBlockEntity = (IGridConnectedBlockEntity)gh;
                node = (GridNode)gridConnectedBlockEntity.getMainNode().getNode();
                this.outputMessage((Entity)player, "Main node of IGridConnectedBlockEntity");
            }
            if (node != null) {
                Object object;
                this.outputMessage((Entity)player, "-- Grid Details");
                Grid g = node.getInternalGrid();
                center = g.getPivot();
                this.outputPrimaryMessage((Entity)player, "Grid Powered", String.valueOf(g.getEnergyService().isNetworkPowered()));
                this.outputPrimaryMessage((Entity)player, "Grid Booted", String.valueOf(!g.getPathingService().isNetworkBooting()));
                this.outputPrimaryMessage((Entity)player, "Nodes in grid", String.valueOf(Iterables.size((Iterable)g.getNodes())));
                this.outputSecondaryMessage((Entity)player, "Grid Pivot Node", String.valueOf(center));
                TickManagerService tmc = (TickManagerService)g.getTickManager();
                for (Class<?> c : g.getMachineClasses()) {
                    int o = 0;
                    long totalAverageTime = 0L;
                    long singleMaximumTime = 0L;
                    for (IGridNode oj : g.getMachineNodes(c)) {
                        ++o;
                        totalAverageTime += tmc.getAverageTime(oj);
                        singleMaximumTime = Math.max(singleMaximumTime, tmc.getMaximumTime(oj));
                    }
                    String message = "#: " + o;
                    if (totalAverageTime > 0L) {
                        message = message + "; average: " + Platform.formatTimeMeasurement(totalAverageTime);
                    }
                    if (singleMaximumTime > 0L) {
                        message = message + "; max: " + Platform.formatTimeMeasurement(singleMaximumTime);
                    }
                    this.outputSecondaryMessage((Entity)player, c.getSimpleName(), message);
                }
                this.outputMessage((Entity)player, "-- Node Details");
                this.outputPrimaryMessage((Entity)player, "This Node", String.valueOf(node));
                this.outputPrimaryMessage((Entity)player, "This Node Active", String.valueOf(node.isActive()));
                this.outputSecondaryMessage((Entity)player, "Node exposed on side", side.getName());
                IPathingService pg = g.getPathingService();
                if (pg.getControllerState() == ControllerState.CONTROLLER_ONLINE) {
                    HashSet<IGridNode> next = new HashSet<IGridNode>();
                    next.add(node);
                    int maxLength = 10000;
                    int length = 0;
                    block2: while (!next.isEmpty()) {
                        HashSet<IGridNode> current = next;
                        next = new HashSet();
                        for (IGridNode n : current) {
                            if (n.getOwner() instanceof ControllerBlockEntity) break block2;
                            for (IGridConnection c : n.getConnections()) {
                                next.add(c.getOtherSide(n));
                            }
                        }
                        if (++length <= 10000) continue;
                        break;
                    }
                    this.outputSecondaryMessage((Entity)player, "Cable Distance", Integer.toString(length));
                }
                if ((object = center.getOwner()) instanceof P2PTunnelPart) {
                    P2PTunnelPart tunnelPart = (P2PTunnelPart)object;
                    this.outputSecondaryMessage((Entity)player, "Freq", Integer.toString(tunnelPart.getFrequency()));
                }
            } else {
                this.outputMessage((Entity)player, "No Node Available.");
            }
        } else {
            this.outputMessage((Entity)player, "Not Networked Block");
        }
        BlockEntity te = level.getBlockEntity(pos);
        if (te instanceof IPartHost) {
            IPartHost partHost = (IPartHost)te;
            this.outputMessage((Entity)player, "-- CableBus Details");
            this.outputSecondaryMessage((Entity)player, "In World", Boolean.toString(partHost.isInWorld()));
            this.outputSecondaryMessage((Entity)player, "Has Redstone", Boolean.toString(partHost.hasRedstone()));
            center = partHost.getPart(null);
            partHost.markForUpdate();
            if (center != null) {
                GridNode n = (GridNode)center.getGridNode();
                this.outputSecondaryMessage((Entity)player, "Node Channels", Integer.toString(n.getUsedChannels()));
                for (Map.Entry<Direction, IGridConnection> entry : n.getInWorldConnections().entrySet()) {
                    this.outputSecondaryMessage((Entity)player, "Channels " + entry.getKey().getName(), Integer.toString(entry.getValue().getUsedChannels()));
                }
            }
            if (center instanceof CablePart) {
                CablePart cablePart = (CablePart)center;
                MutableComponent msg = Component.literal((String)"");
                for (Direction v : Direction.values()) {
                    msg.append((Component)Component.literal((String)v.name().substring(0, 1)).withStyle(cablePart.isConnected(v) ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY));
                }
                player.sendSystemMessage((Component)Component.literal((String)"Connected Sides: ").withStyle(ChatFormatting.GRAY).append((Component)msg));
            }
        }
        if (te instanceof IAEPowerStorage) {
            IGridNode node;
            IAEPowerStorage ps = (IAEPowerStorage)te;
            this.outputMessage((Entity)player, "-- EnergyStorage Details");
            this.outputSecondaryMessage((Entity)player, "Energy", ps.getAECurrentPower() + " / " + ps.getAEMaxPower());
            if (gh != null && (node = gh.getGridNode(side)) != null) {
                IEnergyService eg = node.getGrid().getEnergyService();
                this.outputSecondaryMessage((Entity)player, "GridEnergy", eg.getStoredPower() + " : " + eg.getEnergyDemand(Double.MAX_VALUE));
            }
        }
        if (te instanceof AEBaseBlockEntity) {
            AEBaseBlockEntity be = (AEBaseBlockEntity)te;
            this.outputMessage((Entity)player, "-- Delayed Init Details");
            this.outputSecondaryMessage((Entity)player, "QueuedForReady", "" + be.getQueuedForReady());
            this.outputSecondaryMessage((Entity)player, "ReadyInvoked", "" + be.getReadyInvoked());
        }
        return InteractionResult.sidedSuccess((boolean)level.isClientSide());
    }

    private void divider(Player player) {
        this.outputMessage((Entity)player, "---------------------------------------------", ChatFormatting.BOLD, ChatFormatting.DARK_PURPLE);
    }

    private void outputMessage(Entity player, String string, ChatFormatting ... chatFormattings) {
        player.sendSystemMessage((Component)Component.literal((String)string).withStyle(chatFormattings));
    }

    private void outputMessage(Entity player, String string) {
        player.sendSystemMessage((Component)Component.literal((String)string));
    }

    private void outputPrimaryMessage(Entity player, String label, String value) {
        this.outputLabeledMessage(player, label, value, ChatFormatting.BOLD, ChatFormatting.LIGHT_PURPLE);
    }

    private void outputSecondaryMessage(Entity player, String label, String value) {
        this.outputLabeledMessage(player, label, value, ChatFormatting.GRAY);
    }

    private void outputLabeledMessage(Entity player, String label, String value, ChatFormatting ... chatFormattings) {
        player.sendSystemMessage((Component)Component.literal((String)"").append((Component)Component.literal((String)(label + ": ")).withStyle(chatFormattings)).append(value));
    }
}

