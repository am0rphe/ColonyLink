/*
 * Decompiled with CFR 0.152.
 */
package appeng.api.networking.pathing;

import appeng.api.networking.IGridService;
import appeng.api.networking.pathing.ChannelMode;
import appeng.api.networking.pathing.ControllerState;

public interface IPathingService
extends IGridService {
    public boolean isNetworkBooting();

    public ControllerState getControllerState();

    public void repath();

    public ChannelMode getChannelMode();

    public int getUsedChannels();
}

