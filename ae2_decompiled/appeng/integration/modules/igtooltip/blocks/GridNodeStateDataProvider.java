/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.block.entity.BlockEntity
 */
package appeng.integration.modules.igtooltip.blocks;

import appeng.api.implementations.IPowerChannelState;
import appeng.api.integrations.igtooltip.TooltipBuilder;
import appeng.api.integrations.igtooltip.TooltipContext;
import appeng.api.integrations.igtooltip.providers.BodyProvider;
import appeng.api.integrations.igtooltip.providers.ServerDataProvider;
import appeng.integration.modules.igtooltip.GridNodeState;
import appeng.me.helpers.IGridConnectedBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class GridNodeStateDataProvider
implements BodyProvider<BlockEntity>,
ServerDataProvider<BlockEntity> {
    private static final String TAG_STATE = "gridNodeState";

    @Override
    public void buildTooltip(BlockEntity object, TooltipContext context, TooltipBuilder tooltip) {
        CompoundTag tag = context.serverData();
        if (tag.contains(TAG_STATE, 1)) {
            GridNodeState state = GridNodeState.values()[tag.getByte(TAG_STATE)];
            tooltip.addLine((Component)state.textComponent().withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public void provideServerData(Player player, BlockEntity object, CompoundTag serverData) {
        IPowerChannelState powerChannelState;
        if (object instanceof IPowerChannelState && (powerChannelState = (IPowerChannelState)object).isActive()) {
            serverData.putByte(TAG_STATE, (byte)GridNodeState.ONLINE.ordinal());
            return;
        }
        if (object instanceof IGridConnectedBlockEntity) {
            IGridConnectedBlockEntity gridConnectedBlockEntity = (IGridConnectedBlockEntity)object;
            GridNodeState state = GridNodeState.fromNode(gridConnectedBlockEntity.getActionableNode());
            serverData.putByte(TAG_STATE, (byte)state.ordinal());
        }
    }
}

