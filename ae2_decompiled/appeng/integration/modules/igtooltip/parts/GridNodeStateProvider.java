/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.entity.player.Player
 */
package appeng.integration.modules.igtooltip.parts;

import appeng.api.integrations.igtooltip.TooltipBuilder;
import appeng.api.integrations.igtooltip.TooltipContext;
import appeng.api.integrations.igtooltip.providers.BodyProvider;
import appeng.api.integrations.igtooltip.providers.ServerDataProvider;
import appeng.api.parts.IPart;
import appeng.integration.modules.igtooltip.GridNodeState;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public final class GridNodeStateProvider
implements BodyProvider<IPart>,
ServerDataProvider<IPart> {
    private static final String TAG_STATE = "gridNodeState";

    @Override
    public void buildTooltip(IPart object, TooltipContext context, TooltipBuilder tooltip) {
        CompoundTag serverData = context.serverData();
        if (serverData.contains(TAG_STATE, 1)) {
            GridNodeState state = GridNodeState.values()[serverData.getByte(TAG_STATE)];
            tooltip.addLine((Component)state.textComponent().withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public void provideServerData(Player player, IPart part, CompoundTag serverData) {
        GridNodeState state = GridNodeState.fromNode(part.getGridNode());
        serverData.putByte(TAG_STATE, (byte)state.ordinal());
    }
}

