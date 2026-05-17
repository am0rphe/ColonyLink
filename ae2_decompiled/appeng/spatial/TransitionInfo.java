/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.NbtUtils
 *  net.minecraft.resources.ResourceLocation
 */
package appeng.spatial;

import java.time.Instant;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;

public final class TransitionInfo {
    public static final String TAG_WORLD_ID = "world_id";
    public static final String TAG_MIN = "min";
    public static final String TAG_MAX = "max";
    public static final String TAG_TIMESTAMP = "timestamp";
    private final ResourceLocation worldId;
    private final BlockPos min;
    private final BlockPos max;
    private final Instant timestamp;

    public TransitionInfo(ResourceLocation worldId, BlockPos min, BlockPos max, Instant timestamp) {
        this.worldId = worldId;
        this.min = min.immutable();
        this.max = max.immutable();
        this.timestamp = timestamp;
    }

    public ResourceLocation getWorldId() {
        return this.worldId;
    }

    public BlockPos getMin() {
        return this.min;
    }

    public BlockPos getMax() {
        return this.max;
    }

    public Instant getTimestamp() {
        return this.timestamp;
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putString(TAG_WORLD_ID, this.worldId.toString());
        tag.put(TAG_MIN, NbtUtils.writeBlockPos((BlockPos)this.min));
        tag.put(TAG_MAX, NbtUtils.writeBlockPos((BlockPos)this.max));
        tag.putLong(TAG_TIMESTAMP, this.timestamp.toEpochMilli());
        return tag;
    }

    public static TransitionInfo fromTag(CompoundTag tag) {
        ResourceLocation worldId = ResourceLocation.parse((String)tag.getString(TAG_WORLD_ID));
        BlockPos min = (BlockPos)NbtUtils.readBlockPos((CompoundTag)tag, (String)TAG_MIN).orElseThrow();
        BlockPos max = (BlockPos)NbtUtils.readBlockPos((CompoundTag)tag, (String)TAG_MAX).orElseThrow();
        Instant timestamp = Instant.ofEpochMilli(tag.getLong(TAG_TIMESTAMP));
        return new TransitionInfo(worldId, min, max, timestamp);
    }
}

