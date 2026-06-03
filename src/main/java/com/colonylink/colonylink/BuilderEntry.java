package com.colonylink.colonylink;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

/**
 * Représente un couple Builder's Hut + Redirector lié à la wand.
 * Stocké en NBT dans la liste "builder_entries" de la wand.
 *
 * builderPos     : position du Builder's Hut (snapshot à l'appairage)
 * redirectorPos  : position du Redirector lié (BlockPos.ZERO si pas encore lié)
 * builderName    : nom du builder PNJ (snapshot à l'appairage, "N/A" si vide)
 * buildingLabel  : nom du bâtiment MineColonies (snapshot à l'appairage)
 */
public record BuilderEntry(
        BlockPos builderPos,
        BlockPos redirectorPos,
        String builderName,
        String buildingLabel,
        ResourceKey<Level> dimension
)
{
    public static final BuilderEntry EMPTY = new BuilderEntry(
            BlockPos.ZERO, BlockPos.ZERO, "N/A", "N/A", null);

    // ── Sérialisation NBT ────────────────────────────────────────────────

    public CompoundTag toNbt()
    {
        CompoundTag tag = new CompoundTag();
        tag.putInt("bx", builderPos.getX());
        tag.putInt("by", builderPos.getY());
        tag.putInt("bz", builderPos.getZ());
        tag.putInt("rx", redirectorPos.getX());
        tag.putInt("ry", redirectorPos.getY());
        tag.putInt("rz", redirectorPos.getZ());
        tag.putString("builder_name", builderName);
        tag.putString("building_label", buildingLabel);
        // v1.4.9 — dimension du builder (figée à l'appairage). Absent = legacy/inconnu.
        if (dimension != null) tag.putString("dim", dimension.location().toString());
        return tag;
    }

    public static BuilderEntry fromNbt(CompoundTag tag)
    {
        BlockPos bPos = new BlockPos(tag.getInt("bx"), tag.getInt("by"), tag.getInt("bz"));
        BlockPos rPos = new BlockPos(tag.getInt("rx"), tag.getInt("ry"), tag.getInt("rz"));
        String bName = tag.contains("builder_name") ? tag.getString("builder_name") : "N/A";
        String label = tag.contains("building_label") ? tag.getString("building_label") : "N/A";
        ResourceKey<Level> dim = null;
        if (tag.contains("dim"))
        {
            ResourceLocation rl = ResourceLocation.tryParse(tag.getString("dim"));
            if (rl != null) dim = ResourceKey.create(Registries.DIMENSION, rl);
        }
        return new BuilderEntry(bPos, rPos, bName, label, dim);
    }

    /** Retourne une copie avec le redirectorPos mis à jour. */
    public BuilderEntry withRedirector(BlockPos newRedirectorPos)
    {
        return new BuilderEntry(builderPos, newRedirectorPos, builderName, buildingLabel, dimension);
    }

    /** Retourne une copie avec les labels mis à jour (snapshot à l'appairage). */
    public BuilderEntry withLabels(String newBuilderName, String newBuildingLabel)
    {
        return new BuilderEntry(builderPos, redirectorPos, newBuilderName, newBuildingLabel, dimension);
    }

    /** true si cet entry a un redirector lié (pas ZERO). */
    public boolean hasRedirector()
    {
        return !redirectorPos.equals(BlockPos.ZERO);
    }

    // ── Sérialisation liste en NBT ───────────────────────────────────────

    public static ListTag listToNbt(List<BuilderEntry> entries)
    {
        ListTag list = new ListTag();
        for (BuilderEntry e : entries)
            list.add(e.toNbt());
        return list;
    }

    public static List<BuilderEntry> listFromNbt(ListTag list)
    {
        List<BuilderEntry> entries = new ArrayList<>();
        for (Tag t : list)
        {
            if (t instanceof CompoundTag ct)
                entries.add(fromNbt(ct));
        }
        return entries;
    }
}