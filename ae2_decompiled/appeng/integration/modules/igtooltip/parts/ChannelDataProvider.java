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
import appeng.api.networking.IGridNode;
import appeng.api.networking.pathing.ControllerState;
import appeng.core.localization.InGameTooltip;
import appeng.me.service.AdHocNetworkError;
import appeng.me.service.PathingService;
import appeng.parts.networking.IUsedChannelProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public final class ChannelDataProvider
implements BodyProvider<IUsedChannelProvider>,
ServerDataProvider<IUsedChannelProvider> {
    private static final String TAG_MAX_CHANNELS = "maxChannels";
    private static final String TAG_USED_CHANNELS = "usedChannels";
    private static final String TAG_ERROR = "channelError";

    @Override
    public void buildTooltip(IUsedChannelProvider object, TooltipContext context, TooltipBuilder tooltip) {
        CompoundTag serverData = context.serverData();
        if (serverData.contains(TAG_ERROR, 8)) {
            ChannelError error = ChannelError.valueOf(serverData.getString(TAG_ERROR));
            tooltip.addLine((Component)error.text.text().withStyle(ChatFormatting.RED));
            return;
        }
        if (serverData.contains(TAG_MAX_CHANNELS, 3)) {
            int usedChannels = serverData.getInt(TAG_USED_CHANNELS);
            int maxChannels = serverData.getInt(TAG_MAX_CHANNELS);
            if (maxChannels <= 0) {
                tooltip.addLine((Component)InGameTooltip.Channels.text(usedChannels));
            } else {
                tooltip.addLine((Component)InGameTooltip.ChannelsOf.text(usedChannels, maxChannels));
            }
        }
    }

    @Override
    public void provideServerData(Player player, IUsedChannelProvider object, CompoundTag serverData) {
        IGridNode gridNode = object.getGridNode();
        if (gridNode != null) {
            PathingService pathingService = (PathingService)gridNode.getGrid().getPathingService();
            if (pathingService.getControllerState() == ControllerState.NO_CONTROLLER) {
                AdHocNetworkError adHocError = pathingService.getAdHocNetworkError();
                if (adHocError != null) {
                    serverData.putString(TAG_ERROR, switch (adHocError) {
                        default -> throw new MatchException(null, null);
                        case AdHocNetworkError.NESTED_P2P_TUNNEL -> ChannelError.AD_HOC_NESTED_P2P_TUNNEL.name();
                        case AdHocNetworkError.TOO_MANY_CHANNELS -> ChannelError.AD_HOC_TOO_MANY_CHANNELS.name();
                    });
                    return;
                }
            } else if (pathingService.getControllerState() == ControllerState.CONTROLLER_CONFLICT) {
                serverData.putString(TAG_ERROR, ChannelError.CONTROLLER_CONFLICT.name());
            }
        }
        serverData.putInt(TAG_USED_CHANNELS, object.getUsedChannelsInfo());
        serverData.putInt(TAG_MAX_CHANNELS, object.getMaxChannelsInfo());
    }

    static enum ChannelError {
        AD_HOC_NESTED_P2P_TUNNEL(InGameTooltip.ErrorNestedP2PTunnel),
        AD_HOC_TOO_MANY_CHANNELS(InGameTooltip.ErrorTooManyChannels),
        CONTROLLER_CONFLICT(InGameTooltip.ErrorControllerConflict);

        final InGameTooltip text;

        private ChannelError(InGameTooltip text) {
            this.text = text;
        }
    }
}

