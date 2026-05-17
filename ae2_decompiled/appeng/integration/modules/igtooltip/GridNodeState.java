/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.MutableComponent
 *  org.jetbrains.annotations.Nullable
 */
package appeng.integration.modules.igtooltip;

import appeng.api.networking.IGridNode;
import appeng.core.localization.InGameTooltip;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.Nullable;

public enum GridNodeState {
    OFFLINE(InGameTooltip.DeviceOffline),
    NETWORK_BOOTING(InGameTooltip.NetworkBooting),
    MISSING_CHANNEL(InGameTooltip.DeviceMissingChannel),
    ONLINE(InGameTooltip.DeviceOnline);

    private final InGameTooltip text;

    private GridNodeState(InGameTooltip text) {
        this.text = text;
    }

    public MutableComponent textComponent() {
        return this.text.text();
    }

    public static GridNodeState fromNode(@Nullable IGridNode gridNode) {
        GridNodeState state = OFFLINE;
        if (gridNode != null && gridNode.isPowered()) {
            state = !gridNode.hasGridBooted() ? NETWORK_BOOTING : (!gridNode.meetsChannelRequirements() ? MISSING_CHANNEL : ONLINE);
        }
        return state;
    }
}

