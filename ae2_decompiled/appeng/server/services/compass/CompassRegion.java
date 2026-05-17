/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.level.ChunkPos
 *  net.minecraft.world.level.saveddata.SavedData$Factory
 */
package appeng.server.services.compass;

import appeng.core.AELog;
import appeng.core.worlddata.AESavedData;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;

final class CompassRegion
extends AESavedData {
    private static final SavedData.Factory<CompassRegion> FACTORY = new SavedData.Factory(CompassRegion::new, CompassRegion::load, null);
    private static final int CHUNKS_PER_REGION = 1024;
    private static final int BITMAP_LENGTH = 0x100000;
    private final Map<Integer, BitSet> sections = new HashMap<Integer, BitSet>();

    CompassRegion() {
    }

    private static String getRegionSaveName(int regionX, int regionZ) {
        return "ae2_compass_" + regionX + "_" + regionZ;
    }

    public static CompassRegion get(ServerLevel level, ChunkPos chunkPos) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(chunkPos, "chunkPos");
        int regionX = chunkPos.x / 1024;
        int regionZ = chunkPos.z / 1024;
        return (CompassRegion)level.getDataStorage().computeIfAbsent(FACTORY, CompassRegion.getRegionSaveName(regionX, regionZ));
    }

    public static CompassRegion load(CompoundTag nbt, HolderLookup.Provider registries) {
        CompassRegion result = new CompassRegion();
        for (String key : nbt.getAllKeys()) {
            if (key.startsWith("section")) {
                try {
                    int sectionIndex = Integer.parseInt(key.substring("section".length()));
                    result.sections.put(sectionIndex, BitSet.valueOf(nbt.getByteArray(key)));
                }
                catch (NumberFormatException e) {
                    AELog.warn("Compass region contains invalid NBT tag %s", key);
                }
                continue;
            }
            AELog.warn("Compass region contains unknown NBT tag %s", key);
        }
        return result;
    }

    public CompoundTag save(CompoundTag compound, HolderLookup.Provider registries) {
        for (Map.Entry<Integer, BitSet> entry : this.sections.entrySet()) {
            String key = "section" + String.valueOf(entry.getKey());
            if (entry.getValue().isEmpty()) continue;
            compound.putByteArray(key, entry.getValue().toByteArray());
        }
        return compound;
    }

    boolean hasCompassTarget(int cx, int cz) {
        int bitmapIndex = CompassRegion.getBitmapIndex(cx, cz);
        for (BitSet bitmap : this.sections.values()) {
            if (!bitmap.get(bitmapIndex)) continue;
            return true;
        }
        return false;
    }

    boolean hasCompassTarget(int cx, int cz, int sectionIndex) {
        int bitmapIndex = CompassRegion.getBitmapIndex(cx, cz);
        BitSet section = this.sections.get(sectionIndex);
        if (section != null) {
            return section.get(bitmapIndex);
        }
        return false;
    }

    void setHasCompassTarget(int cx, int cz, int sectionIndex, boolean hasTarget) {
        int bitmapIndex = CompassRegion.getBitmapIndex(cx, cz);
        BitSet section = this.sections.get(sectionIndex);
        if (section == null) {
            if (hasTarget) {
                section = new BitSet(0x100000);
                section.set(bitmapIndex);
                this.sections.put(sectionIndex, section);
                this.setDirty();
            }
        } else {
            if (section.get(bitmapIndex) != hasTarget) {
                this.setDirty();
            }
            if (!hasTarget) {
                section.clear(bitmapIndex);
                if (section.isEmpty()) {
                    this.sections.remove(sectionIndex);
                }
                this.setDirty();
            } else {
                section.set(bitmapIndex);
            }
        }
    }

    private static int getBitmapIndex(int cx, int cz) {
        return (cx &= 0x3FF) + (cz &= 0x3FF) * 1024;
    }
}

