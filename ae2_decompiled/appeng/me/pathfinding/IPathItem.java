/*
 * Decompiled with CFR 0.152.
 */
package appeng.me.pathfinding;

import appeng.api.networking.GridFlags;

public interface IPathItem {
    public void setAdHocChannels(int var1);

    public IPathItem getControllerRoute();

    public void setControllerRoute(IPathItem var1);

    public int getMaxChannels();

    public Iterable<IPathItem> getPossibleOptions();

    public boolean hasFlag(GridFlags var1);

    public void finalizeChannels();
}

