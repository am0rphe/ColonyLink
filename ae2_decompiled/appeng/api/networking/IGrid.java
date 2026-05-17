/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.stream.JsonWriter
 */
package appeng.api.networking;

import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridService;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.events.GridEvent;
import appeng.api.networking.pathing.IPathingService;
import appeng.api.networking.spatial.ISpatialService;
import appeng.api.networking.storage.IStorageService;
import appeng.api.networking.ticking.ITickManager;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.Set;

public interface IGrid {
    public <C extends IGridService> C getService(Class<C> var1);

    public <T extends GridEvent> T postEvent(T var1);

    public Iterable<Class<?>> getMachineClasses();

    public Iterable<IGridNode> getMachineNodes(Class<?> var1);

    public <T> Set<T> getMachines(Class<T> var1);

    public <T> Set<T> getActiveMachines(Class<T> var1);

    public Iterable<IGridNode> getNodes();

    public boolean isEmpty();

    public IGridNode getPivot();

    public int size();

    default public ITickManager getTickManager() {
        return this.getService(ITickManager.class);
    }

    default public IStorageService getStorageService() {
        return this.getService(IStorageService.class);
    }

    default public IEnergyService getEnergyService() {
        return this.getService(IEnergyService.class);
    }

    default public ICraftingService getCraftingService() {
        return this.getService(ICraftingService.class);
    }

    default public IPathingService getPathingService() {
        return this.getService(IPathingService.class);
    }

    default public ISpatialService getSpatialService() {
        return this.getService(ISpatialService.class);
    }

    public void export(JsonWriter var1) throws IOException;
}

