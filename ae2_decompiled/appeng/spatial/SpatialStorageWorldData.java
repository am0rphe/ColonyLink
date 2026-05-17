/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
 *  it.unimi.dsi.fastutil.ints.IntIterator
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.Tag
 */
package appeng.spatial;

import appeng.core.AELog;
import appeng.core.worlddata.AESavedData;
import appeng.spatial.SpatialStoragePlot;
import appeng.spatial.TransitionInfo;
import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntIterator;
import java.util.Collection;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

public class SpatialStorageWorldData
extends AESavedData {
    public static final String ID = "ae2_spatial_storage";
    private static final int CURRENT_FORMAT = 2;
    private static final String TAG_FORMAT = "format";
    private static final String TAG_PLOTS = "plots";
    private final Int2ObjectOpenHashMap<SpatialStoragePlot> plots = new Int2ObjectOpenHashMap();

    public SpatialStoragePlot getPlotById(int id) {
        return (SpatialStoragePlot)this.plots.get(id);
    }

    public List<SpatialStoragePlot> getPlots() {
        return ImmutableList.copyOf((Collection)this.plots.values());
    }

    public SpatialStoragePlot allocatePlot(BlockPos size, int owner) {
        int nextId = 1;
        IntIterator intIterator = this.plots.keySet().iterator();
        while (intIterator.hasNext()) {
            int id = (Integer)intIterator.next();
            if (id < nextId) continue;
            nextId = id + 1;
        }
        SpatialStoragePlot plot = new SpatialStoragePlot(nextId, size, owner);
        this.plots.put(nextId, (Object)plot);
        this.setDirty();
        return plot;
    }

    public void removePlot(int plotId) {
        this.plots.remove(plotId);
        this.setDirty();
    }

    public void setLastTransition(int plotId, TransitionInfo info) {
        SpatialStoragePlot plot = (SpatialStoragePlot)this.plots.get(plotId);
        if (plot != null) {
            plot.setLastTransition(info);
        }
        this.setDirty();
    }

    public static SpatialStorageWorldData load(CompoundTag tag, HolderLookup.Provider registries) {
        SpatialStorageWorldData result = new SpatialStorageWorldData();
        int version = tag.getInt(TAG_FORMAT);
        if (version != 2) {
            throw new IllegalStateException("Invalid AE2 spatial info version: " + version);
        }
        ListTag plotsTag = tag.getList(TAG_PLOTS, 10);
        for (Tag plotTag : plotsTag) {
            SpatialStoragePlot plot = SpatialStoragePlot.fromTag((CompoundTag)plotTag);
            if (result.plots.containsKey(plot.getId())) {
                AELog.warn("Overwriting duplicate plot id %s", plot.getId());
            }
            result.plots.put(plot.getId(), (Object)plot);
        }
        return result;
    }

    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt(TAG_FORMAT, 2);
        ListTag plotTags = new ListTag();
        for (SpatialStoragePlot plot : this.plots.values()) {
            plotTags.add((Object)plot.toTag());
        }
        tag.put(TAG_PLOTS, (Tag)plotTags);
        return tag;
    }
}

