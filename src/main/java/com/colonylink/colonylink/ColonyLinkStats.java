package com.colonylink.colonylink;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;

/**
 * v1.8.0 — cumulative ColonyLink activity counters, persisted in the world.
 *
 * Stored per {@link ServerLevel} via {@code getDataStorage()} (file
 * {@code colonylink_stats.dat}), so each dimension keeps its own file; inside it,
 * counters are keyed by colony id. A colony's counters therefore live in the
 * data storage of the colony's own dimension (see {@link #storageFor}).
 *
 * Counters are cumulative and never reset. Indices into the {@code long[]}:
 * 0 = items sent to Warehouse, 1 = AE2 crafts launched, 2 = Domum blocks crafted,
 * 3 = Skill Tomes used (3 is global per colony only, no per-builder breakdown).
 *
 * All mutation goes through the static {@code record*} helpers, which resolve the
 * colony's ServerLevel and mark the data dirty. Server-thread only.
 */
public class ColonyLinkStats extends SavedData
{
    public static final String DATA_NAME = "colonylink_stats";

    public static final int IDX_ITEMS = 0;
    public static final int IDX_AE2   = 1;
    public static final int IDX_DOMUM = 2;
    public static final int IDX_TOMES = 3;

    // colonyId -> global counters [items, ae2, domum, tomes]
    private final Map<Integer, long[]> global = new HashMap<>();
    // colonyId -> (builder BlockPos.asLong() -> [items, ae2, domum])
    private final Map<Integer, Map<Long, long[]>> perBuilder = new HashMap<>();

    public ColonyLinkStats() {}

    // ---- storage access -----------------------------------------------------

    private static final Factory<ColonyLinkStats> FACTORY =
            new Factory<>(ColonyLinkStats::new, ColonyLinkStats::load);

    /** The data storage for a colony's own dimension, or null if unavailable. */
    private static ServerLevel storageFor(IColony colony)
    {
        if (colony == null) return null;
        final Level world = colony.getWorld();
        return (world instanceof ServerLevel sl) ? sl : null;
    }

    public static ColonyLinkStats get(ServerLevel level)
    {
        return level.getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
    }

    // ---- mutation (static entry points) -------------------------------------

    public static void recordItemsSent(IColony colony, BlockPos builderPos, int count)
    {
        inc(colony, builderPos, IDX_ITEMS, count);
    }

    public static void recordAe2Crafts(IColony colony, BlockPos builderPos, int count)
    {
        inc(colony, builderPos, IDX_AE2, count);
    }

    public static void recordDomumCrafts(IColony colony, BlockPos builderPos, int count)
    {
        inc(colony, builderPos, IDX_DOMUM, count);
    }

    /** Tomes are tracked globally per colony (no builder attribution). */
    public static void recordTomesUsed(IColony colony, int count)
    {
        inc(colony, null, IDX_TOMES, count);
    }

    /**
     * Resolves the colony a wand is working with, from its linked builder entries
     * (dimension-guarded, same rule as CitizensScanHandler). Used by craft hooks
     * that only have the player + level, not a builder position.
     */
    public static IColony resolveColonyFromWand(ServerPlayer player, ServerLevel level)
    {
        final ItemStack wand = ColonyLinkServerTicker.findWandInInventory(player);
        if (wand == null) return null;
        for (final BuilderEntry e : ColonyLinkWandLinkableHandler.getBuilderEntries(wand))
        {
            if (e.dimension() != null && !e.dimension().equals(level.dimension())) continue;
            final IColony c = IColonyManager.getInstance().getClosestColony(level, e.builderPos());
            if (c != null) return c;
        }
        return null;
    }

    private static void inc(IColony colony, BlockPos builderPos, int idx, int count)
    {
        if (count <= 0 || colony == null) return;
        final ServerLevel level = storageFor(colony);
        if (level == null) return;

        final ColonyLinkStats data = get(level);
        final int colonyId = colony.getID();

        data.global.computeIfAbsent(colonyId, k -> new long[4])[idx] += count;

        // Per-builder breakdown for the three builder-scoped counters (not tomes).
        if (builderPos != null && idx != IDX_TOMES)
        {
            data.perBuilder
                    .computeIfAbsent(colonyId, k -> new HashMap<>())
                    .computeIfAbsent(builderPos.asLong(), k -> new long[3])[idx] += count;
        }

        data.setDirty();
    }

    // ---- read ---------------------------------------------------------------

    /** Global cumulative counters for a colony: [items, ae2, domum, tomes]. Never null. */
    public long[] snapshot(int colonyId)
    {
        final long[] g = global.get(colonyId);
        return g != null ? g.clone() : new long[4];
    }

    // ---- NBT ----------------------------------------------------------------

    public static ColonyLinkStats load(CompoundTag tag, HolderLookup.Provider provider)
    {
        final ColonyLinkStats data = new ColonyLinkStats();
        final ListTag colonies = tag.getList("colonies", Tag.TAG_COMPOUND);
        for (int i = 0; i < colonies.size(); i++)
        {
            final CompoundTag c = colonies.getCompound(i);
            final int id = c.getInt("id");
            data.global.put(id, new long[] {
                    c.getLong("items"), c.getLong("ae2"), c.getLong("domum"), c.getLong("tomes")
            });

            final ListTag builders = c.getList("builders", Tag.TAG_COMPOUND);
            if (!builders.isEmpty())
            {
                final Map<Long, long[]> map = new HashMap<>();
                for (int b = 0; b < builders.size(); b++)
                {
                    final CompoundTag bt = builders.getCompound(b);
                    map.put(bt.getLong("pos"), new long[] {
                            bt.getLong("items"), bt.getLong("ae2"), bt.getLong("domum")
                    });
                }
                data.perBuilder.put(id, map);
            }
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider)
    {
        final ListTag colonies = new ListTag();
        for (final Map.Entry<Integer, long[]> e : global.entrySet())
        {
            final int id = e.getKey();
            final long[] g = e.getValue();
            final CompoundTag c = new CompoundTag();
            c.putInt("id", id);
            c.putLong("items", g[IDX_ITEMS]);
            c.putLong("ae2",   g[IDX_AE2]);
            c.putLong("domum", g[IDX_DOMUM]);
            c.putLong("tomes", g[IDX_TOMES]);

            final Map<Long, long[]> builders = perBuilder.get(id);
            if (builders != null && !builders.isEmpty())
            {
                final ListTag bl = new ListTag();
                for (final Map.Entry<Long, long[]> be : builders.entrySet())
                {
                    final long[] bv = be.getValue();
                    final CompoundTag bt = new CompoundTag();
                    bt.putLong("pos",   be.getKey());
                    bt.putLong("items", bv[0]);
                    bt.putLong("ae2",   bv[1]);
                    bt.putLong("domum", bv[2]);
                    bl.add(bt);
                }
                c.put("builders", bl);
            }
            colonies.add(c);
        }
        tag.put("colonies", colonies);
        return tag;
    }
}
