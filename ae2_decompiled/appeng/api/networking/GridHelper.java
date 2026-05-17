/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  org.jetbrains.annotations.Nullable
 */
package appeng.api.networking;

import appeng.api.AECapabilities;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.IGridService;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.events.GridEvent;
import appeng.hooks.ticking.TickHandler;
import appeng.me.GridConnection;
import appeng.me.GridEventBus;
import appeng.me.InWorldGridNode;
import appeng.me.ManagedGridNode;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public final class GridHelper {
    private GridHelper() {
    }

    public static <T extends BlockEntity> void onFirstTick(T blockEntity, Consumer<? super T> callback) {
        TickHandler.instance().addInit(blockEntity, callback);
    }

    public static <T extends GridEvent> void addEventHandler(Class<T> eventClass, BiConsumer<IGrid, T> handler) {
        GridEventBus.subscribe(eventClass, handler);
    }

    public static <T extends GridEvent, C extends IGridService> void addGridServiceEventHandler(Class<T> eventClass, Class<C> cacheClass, BiConsumer<C, T> eventHandler) {
        GridHelper.addEventHandler(eventClass, (grid, event) -> eventHandler.accept(grid.getService(cacheClass), event));
    }

    public static <T extends GridEvent, C> void addNodeOwnerEventHandler(Class<T> eventClass, Class<C> nodeOwnerClass, BiConsumer<C, T> eventHandler) {
        GridHelper.addEventHandler(eventClass, (grid, event) -> {
            for (Object machine : grid.getMachines(nodeOwnerClass)) {
                eventHandler.accept(machine, event);
            }
        });
    }

    public static <T extends GridEvent, C> void addNodeOwnerEventHandler(Class<T> eventClass, Class<C> nodeOwnerClass, Consumer<C> eventHandler) {
        GridHelper.addEventHandler(eventClass, (grid, event) -> {
            for (Object machine : grid.getMachines(nodeOwnerClass)) {
                eventHandler.accept(machine);
            }
        });
    }

    @Nullable
    public static IInWorldGridNodeHost getNodeHost(Level level, BlockPos pos) {
        return (IInWorldGridNodeHost)level.getCapability(AECapabilities.IN_WORLD_GRID_NODE_HOST, pos, null);
    }

    @Nullable
    public static IGridNode getExposedNode(Level level, BlockPos pos, Direction side) {
        InWorldGridNode inWorldNode;
        IInWorldGridNodeHost host = GridHelper.getNodeHost(level, pos);
        if (host == null) {
            return null;
        }
        IGridNode node = host.getGridNode(side);
        if (node instanceof InWorldGridNode && (inWorldNode = (InWorldGridNode)node).isExposedOnSide(side)) {
            return node;
        }
        return null;
    }

    public static <T> IManagedGridNode createManagedNode(T owner, IGridNodeListener<T> listener) {
        return new ManagedGridNode(owner, listener);
    }

    public static IGridConnection createConnection(IGridNode a, IGridNode b) {
        return GridConnection.create(a, b, null);
    }
}

