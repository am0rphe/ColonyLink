/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Direction
 *  org.jetbrains.annotations.Nullable
 */
package appeng.api.networking;

import appeng.api.networking.IGridNode;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

public interface IGridConnection {
    public IGridNode getOtherSide(IGridNode var1);

    public boolean isInWorld();

    @Nullable
    public Direction getDirection(IGridNode var1);

    public void destroy();

    public IGridNode a();

    public IGridNode b();

    public int getUsedChannels();
}

