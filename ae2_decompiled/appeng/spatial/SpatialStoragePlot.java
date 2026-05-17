/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.NbtUtils
 *  net.minecraft.nbt.Tag
 *  net.minecraft.world.level.ChunkPos
 *  org.jetbrains.annotations.Nullable
 */
package appeng.spatial;

import appeng.spatial.TransitionInfo;
import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.Nullable;

public class SpatialStoragePlot {
    private static final String TAG_ID = "id";
    private static final String TAG_SIZE = "size";
    private static final String TAG_OWNER = "owner";
    private static final String TAG_LAST_TRANSITION = "last_transition";
    private static final int REGION_SIZE = 512;
    public static final int MAX_SIZE = 128;
    private final int id;
    private final BlockPos size;
    private final int owner;
    @Nullable
    private TransitionInfo lastTransition;

    public SpatialStoragePlot(int id, BlockPos size, int owner) {
        this.id = id;
        this.size = size;
        this.owner = owner;
        if (size.getX() < 1 || size.getY() < 1 || size.getZ() < 1) {
            throw new IllegalArgumentException("Plot size " + String.valueOf(size) + " is smaller than minimum size.");
        }
        if (size.getX() > 128 || size.getY() > 128 || size.getZ() > 128) {
            throw new IllegalArgumentException("Plot size " + String.valueOf(size) + " exceeds maximum size of 128");
        }
    }

    public int getId() {
        return this.id;
    }

    public BlockPos getSize() {
        return this.size;
    }

    public int getOwner() {
        return this.owner;
    }

    @Nullable
    public TransitionInfo getLastTransition() {
        return this.lastTransition;
    }

    void setLastTransition(TransitionInfo info) {
        this.lastTransition = info;
    }

    public BlockPos getOrigin() {
        int signBits = this.id & 3;
        int offsetBits = this.id >> 2;
        int offsetScale = 1;
        int posx = 256;
        int posz = 256;
        while (offsetBits != 0) {
            posx += 512 * offsetScale * (offsetBits & 1);
            posz += 512 * offsetScale * (offsetBits >> 1 & 1);
            offsetBits >>= 2;
            offsetScale <<= 1;
        }
        if ((signBits & 1) != 0) {
            posz *= -1;
        }
        if ((signBits & 2) != 0) {
            posx *= -1;
        }
        return new BlockPos(posx -= 64, 64, posz -= 64);
    }

    public String getRegionFilename() {
        BlockPos origin = this.getOrigin();
        ChunkPos originChunk = new ChunkPos(origin);
        return String.format(Locale.ROOT, "r.%d.%d.mca", originChunk.getRegionX(), originChunk.getRegionZ());
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putInt(TAG_ID, this.id);
        tag.put(TAG_SIZE, NbtUtils.writeBlockPos((BlockPos)this.size));
        tag.putInt(TAG_OWNER, this.owner);
        if (this.lastTransition != null) {
            tag.put(TAG_LAST_TRANSITION, (Tag)this.lastTransition.toTag());
        }
        return tag;
    }

    public static SpatialStoragePlot fromTag(CompoundTag tag) {
        int id = tag.getInt(TAG_ID);
        BlockPos size = (BlockPos)NbtUtils.readBlockPos((CompoundTag)tag, (String)TAG_SIZE).orElseThrow();
        int ownerId = tag.getInt(TAG_OWNER);
        SpatialStoragePlot plot = new SpatialStoragePlot(id, size, ownerId);
        if (tag.contains(TAG_LAST_TRANSITION, 10)) {
            plot.lastTransition = TransitionInfo.fromTag(tag.getCompound(TAG_LAST_TRANSITION));
        }
        return plot;
    }
}

