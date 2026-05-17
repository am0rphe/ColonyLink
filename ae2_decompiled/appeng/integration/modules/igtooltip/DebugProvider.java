/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.core.Direction
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.state.BlockState
 *  org.jetbrains.annotations.Nullable
 */
package appeng.integration.modules.igtooltip;

import appeng.api.integrations.igtooltip.TooltipBuilder;
import appeng.api.integrations.igtooltip.TooltipContext;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.orientation.IOrientationStrategy;
import appeng.core.definitions.AEItems;
import appeng.me.InWorldGridNode;
import appeng.me.helpers.IGridConnectedBlockEntity;
import appeng.me.service.TickManagerService;
import appeng.parts.AEBasePart;
import appeng.util.Platform;
import java.util.ArrayList;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class DebugProvider {
    private static final String TAG_NODES = "debugNodes";
    private static final String TAG_NODE_NAME = "nodeName";
    private static final String TAG_TICK_TIME = "tickTime";
    private static final String TAG_TICK_SLEEPING = "tickSleeping";
    private static final String TAG_TICK_ALERTABLE = "tickAlertable";
    private static final String TAG_TICK_AWAKE = "tickAwake";
    private static final String TAG_TICK_QUEUED = "tickQueued";
    private static final String TAG_TICK_CURRENT_RATE = "tickCurrentRate";
    private static final String TAG_TICK_LAST_TICK = "tickLastTick";
    private static final String TAG_NODE_EXPOSED = "exposedSides";

    private DebugProvider() {
    }

    public static void provideBlockEntityBody(BlockEntity object, TooltipContext context, TooltipBuilder tooltip) {
        Player player = context.player();
        if (!DebugProvider.isVisible(player)) {
            return;
        }
        DebugProvider.addBlockEntityRotation(object, tooltip);
        DebugProvider.addToTooltip(context.serverData(), tooltip);
    }

    public static void provideBlockEntityData(Player player, BlockEntity object, CompoundTag serverData) {
        if (object instanceof IGridConnectedBlockEntity) {
            IGridConnectedBlockEntity gridConnected = (IGridConnectedBlockEntity)object;
            if (DebugProvider.isVisible(player)) {
                DebugProvider.addServerDataMainNode(serverData, gridConnected.getMainNode());
            }
        }
    }

    public static void providePartBody(AEBasePart object, TooltipContext context, TooltipBuilder tooltip) {
        DebugProvider.addToTooltip(context.serverData(), tooltip);
    }

    public static void providePartData(Player player, AEBasePart part, CompoundTag serverData) {
        if (DebugProvider.isVisible(player)) {
            DebugProvider.addServerDataMainNode(serverData, part.getMainNode());
            DebugProvider.addServerDataNode(serverData, "External Node", part.getExternalFacingNode());
        }
    }

    private static void addBlockEntityRotation(BlockEntity blockEntity, TooltipBuilder tooltip) {
        BlockState blockState = blockEntity.getBlockState();
        IOrientationStrategy strategy = IOrientationStrategy.get(blockState);
        if (!strategy.getProperties().isEmpty()) {
            Direction forward = strategy.getFacing(blockState);
            int spin = strategy.getSpin(blockState);
            tooltip.addLine((Component)Component.literal((String)"").append((Component)Component.literal((String)" Forward: ").withStyle(ChatFormatting.WHITE)).append((Component)Component.literal((String)forward.name())).append((Component)Component.literal((String)" Spin: ").withStyle(ChatFormatting.WHITE)).append((Component)Component.literal((String)String.valueOf(spin))));
        }
    }

    private static void addToTooltip(CompoundTag serverData, TooltipBuilder tooltip) {
        ListTag nodes = serverData.getList(TAG_NODES, 10);
        for (Tag node : nodes) {
            CompoundTag nodeCompound = (CompoundTag)node;
            if (nodes.size() > 1) {
                String nodeName = ((CompoundTag)node).getString(TAG_NODE_NAME);
                tooltip.addLine((Component)Component.literal((String)nodeName).withStyle(ChatFormatting.ITALIC));
            }
            DebugProvider.addNodeToTooltip(nodeCompound, tooltip);
        }
    }

    private static void addNodeToTooltip(CompoundTag tag, TooltipBuilder tooltip) {
        long[] tickTimes;
        if (tag.contains(TAG_TICK_TIME, 12) && (tickTimes = tag.getLongArray(TAG_TICK_TIME)).length == 3) {
            long avg = tickTimes[0];
            long max = tickTimes[1];
            long sum = tickTimes[2];
            tooltip.addLine((Component)Component.literal((String)"").append((Component)Component.literal((String)"Tick Time: ").withStyle(ChatFormatting.WHITE)).append((Component)Component.literal((String)"Avg: ").withStyle(ChatFormatting.ITALIC)).append((Component)Component.literal((String)Platform.formatTimeMeasurement(avg)).withStyle(ChatFormatting.WHITE)).append((Component)Component.literal((String)" Max: ").withStyle(ChatFormatting.ITALIC)).append((Component)Component.literal((String)Platform.formatTimeMeasurement(max)).withStyle(ChatFormatting.WHITE)).append((Component)Component.literal((String)" Sum: ").withStyle(ChatFormatting.ITALIC)).append((Component)Component.literal((String)Platform.formatTimeMeasurement(sum)).withStyle(ChatFormatting.WHITE)));
        }
        if (tag.contains(TAG_TICK_QUEUED)) {
            ArrayList<String> status = new ArrayList<String>();
            if (tag.getBoolean(TAG_TICK_SLEEPING)) {
                status.add("Sleeping");
            }
            if (tag.getBoolean(TAG_TICK_ALERTABLE)) {
                status.add("Alertable");
            }
            if (tag.getBoolean(TAG_TICK_AWAKE)) {
                status.add("Awake");
            }
            if (tag.getBoolean(TAG_TICK_QUEUED)) {
                status.add("Queued");
            }
            tooltip.addLine((Component)Component.literal((String)"").append((Component)Component.literal((String)"Tick Status: ").withStyle(ChatFormatting.WHITE)).append(String.join((CharSequence)", ", status)));
            tooltip.addLine((Component)Component.literal((String)"").append((Component)Component.literal((String)"Tick Rate: ").withStyle(ChatFormatting.WHITE)).append(String.valueOf(tag.getInt(TAG_TICK_CURRENT_RATE))).append((Component)Component.literal((String)" Last: ").withStyle(ChatFormatting.WHITE)).append(tag.getInt(TAG_TICK_LAST_TICK) + " ticks ago"));
        }
        if (tag.contains(TAG_NODE_EXPOSED, 3)) {
            int exposedSides = tag.getInt(TAG_NODE_EXPOSED);
            MutableComponent line = Component.literal((String)"Node Exposed: ").withStyle(ChatFormatting.WHITE);
            for (Direction value : Direction.values()) {
                MutableComponent sideText = Component.literal((String)value.name().substring(0, 1));
                if ((exposedSides & 1 << value.ordinal()) == 0) {
                    sideText.withStyle(ChatFormatting.GRAY);
                } else {
                    sideText.withStyle(ChatFormatting.GREEN);
                }
                line.append((Component)sideText);
            }
            tooltip.addLine((Component)line);
        }
    }

    private static void addServerDataMainNode(CompoundTag tag, IManagedGridNode managedGridNode) {
        DebugProvider.addServerDataNode(tag, "Main Node", managedGridNode.getNode());
    }

    private static void addServerDataNode(CompoundTag tag, String name, @Nullable IGridNode node) {
        CompoundTag nodeTag = DebugProvider.toServerData(node, name);
        if (nodeTag != null) {
            ListTag nodes = (ListTag)tag.get(TAG_NODES);
            if (nodes == null) {
                nodes = new ListTag();
                tag.put(TAG_NODES, (Tag)nodes);
            }
            nodes.add((Object)nodeTag);
        }
    }

    private static CompoundTag toServerData(IGridNode node, String name) {
        if (node == null) {
            return null;
        }
        CompoundTag tag = new CompoundTag();
        tag.putString(TAG_NODE_NAME, name);
        if (node.getService(IGridTickable.class) != null) {
            TickManagerService tickManager = (TickManagerService)node.getGrid().getTickManager();
            long avg = tickManager.getAverageTime(node);
            long max = tickManager.getMaximumTime(node);
            long sum = tickManager.getOverallTime(node);
            tag.putLongArray(TAG_TICK_TIME, new long[]{avg, max, sum});
            TickManagerService.NodeStatus status = tickManager.getStatus(node);
            tag.putBoolean(TAG_TICK_SLEEPING, status.sleeping());
            tag.putBoolean(TAG_TICK_ALERTABLE, status.alertable());
            tag.putBoolean(TAG_TICK_AWAKE, status.awake());
            tag.putBoolean(TAG_TICK_QUEUED, status.queued());
            tag.putInt(TAG_TICK_CURRENT_RATE, status.currentRate());
            tag.putLong(TAG_TICK_LAST_TICK, status.lastTick());
        }
        if (node instanceof InWorldGridNode) {
            InWorldGridNode inWorldNode = (InWorldGridNode)node;
            int exposedSides = 0;
            for (Direction value : Direction.values()) {
                if (!inWorldNode.isExposedOnSide(value)) continue;
                exposedSides |= 1 << value.ordinal();
            }
            tag.putInt(TAG_NODE_EXPOSED, exposedSides);
        }
        return tag;
    }

    private static boolean isVisible(Player player) {
        return AEItems.DEBUG_CARD.is(player.getItemInHand(InteractionHand.OFF_HAND)) || AEItems.DEBUG_CARD.is(player.getItemInHand(InteractionHand.MAIN_HAND));
    }
}

