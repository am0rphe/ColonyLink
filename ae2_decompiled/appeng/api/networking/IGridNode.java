/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.CrashReportCategory
 *  net.minecraft.core.Direction
 *  net.minecraft.server.level.ServerLevel
 *  org.jetbrains.annotations.Nullable
 */
package appeng.api.networking;

import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridNodeService;
import appeng.api.networking.IGridVisitor;
import appeng.api.stacks.AEItemKey;
import appeng.api.util.AEColor;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.CrashReportCategory;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

public interface IGridNode {
    @Nullable
    public <T extends IGridNodeService> T getService(Class<T> var1);

    public Object getOwner();

    public void beginVisit(IGridVisitor var1);

    public IGrid getGrid();

    public ServerLevel getLevel();

    public Set<Direction> getConnectedSides();

    public Map<Direction, IGridConnection> getInWorldConnections();

    public List<IGridConnection> getConnections();

    default public boolean isActive() {
        return this.isPowered() && this.hasGridBooted() && this.meetsChannelRequirements();
    }

    default public boolean isOnline() {
        return this.isPowered() && this.meetsChannelRequirements();
    }

    public boolean hasGridBooted();

    public boolean isPowered();

    public boolean meetsChannelRequirements();

    public boolean hasFlag(GridFlags var1);

    public int getOwningPlayerId();

    @Nullable
    public UUID getOwningPlayerProfileId();

    public double getIdlePowerUsage();

    @Nullable
    public AEItemKey getVisualRepresentation();

    public AEColor getGridColor();

    public void fillCrashReportCategory(CrashReportCategory var1);

    public int getMaxChannels();

    public int getUsedChannels();
}

