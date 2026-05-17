/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Reference2LongMap
 *  it.unimi.dsi.fastutil.objects.Reference2LongMap$Entry
 *  it.unimi.dsi.fastutil.objects.Reference2LongOpenHashMap
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.util.Mth
 */
package appeng.crafting.execution;

import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.AEKeyTypes;
import it.unimi.dsi.fastutil.objects.Reference2LongMap;
import it.unimi.dsi.fastutil.objects.Reference2LongOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;

public class ElapsedTimeTracker {
    private static final String NBT_ELAPSED_TIME = "elapsedTime";
    private static final String NBT_STARTED_WORK = "startedWork";
    private static final String NBT_COMPLETED_WORK = "completedWork";
    private long lastTime = System.nanoTime();
    private long elapsedTime = 0L;
    private final Reference2LongMap<AEKeyType> startedWorkByType = new Reference2LongOpenHashMap(AEKeyTypes.getAll().size());
    private final Reference2LongMap<AEKeyType> completedWorkByType = new Reference2LongOpenHashMap(AEKeyTypes.getAll().size());

    public ElapsedTimeTracker() {
    }

    public ElapsedTimeTracker(CompoundTag data) {
        this.elapsedTime = data.getLong(NBT_ELAPSED_TIME);
        ElapsedTimeTracker.readLongByTypeMap(data.getCompound(NBT_STARTED_WORK), this.startedWorkByType);
        ElapsedTimeTracker.readLongByTypeMap(data.getCompound(NBT_COMPLETED_WORK), this.completedWorkByType);
    }

    public CompoundTag writeToNBT() {
        CompoundTag data = new CompoundTag();
        data.putLong(NBT_ELAPSED_TIME, this.elapsedTime);
        data.put(NBT_STARTED_WORK, (Tag)ElapsedTimeTracker.writeLongByTypeMap(this.startedWorkByType));
        data.put(NBT_COMPLETED_WORK, (Tag)ElapsedTimeTracker.writeLongByTypeMap(this.completedWorkByType));
        return data;
    }

    private static void readLongByTypeMap(CompoundTag tag, Reference2LongMap<AEKeyType> output) {
        for (AEKeyType keyType : AEKeyTypes.getAll()) {
            output.put((Object)keyType, tag.getLong(keyType.getId().toString()));
        }
    }

    private static CompoundTag writeLongByTypeMap(Reference2LongMap<AEKeyType> input) {
        CompoundTag result = new CompoundTag();
        for (Reference2LongMap.Entry entry : input.reference2LongEntrySet()) {
            result.putLong(((AEKeyType)entry.getKey()).getId().toString(), entry.getLongValue());
        }
        return result;
    }

    private void updateTime() {
        long currentTime = System.nanoTime();
        this.elapsedTime += currentTime - this.lastTime;
        this.lastTime = currentTime;
    }

    void decrementItems(long itemDiff, AEKeyType keyType) {
        this.updateTime();
        this.completedWorkByType.merge((Object)keyType, itemDiff, this::saturatedSum);
    }

    private long saturatedSum(long a, long b) {
        long result = a + b;
        return result < 0L ? Long.MAX_VALUE : result;
    }

    void addMaxItems(long itemDiff, AEKeyType keyType) {
        this.updateTime();
        this.startedWorkByType.merge((Object)keyType, itemDiff, this::saturatedSum);
    }

    public long getElapsedTime() {
        boolean allDone = true;
        for (AEKeyType keyType : AEKeyTypes.getAll()) {
            if (this.completedWorkByType.getLong((Object)keyType) >= this.startedWorkByType.getLong((Object)keyType)) continue;
            allDone = false;
            break;
        }
        if (!allDone) {
            return this.elapsedTime + (System.nanoTime() - this.lastTime);
        }
        return this.elapsedTime;
    }

    public float getProgress() {
        double startedUnits = 0.0;
        double completedUnits = 0.0;
        for (AEKeyType keyType : AEKeyTypes.getAll()) {
            long startedForType = this.startedWorkByType.getLong((Object)keyType);
            long completedForType = this.completedWorkByType.getLong((Object)keyType);
            startedUnits += (double)startedForType / (double)keyType.getAmountPerUnit();
            completedUnits += (double)completedForType / (double)keyType.getAmountPerUnit();
        }
        return Mth.clamp((float)((float)(completedUnits / startedUnits)), (float)0.0f, (float)1.0f);
    }

    @Deprecated(forRemoval=true)
    public long getRemainingItemCount() {
        return (int)(2.147483647E9 - (double)this.getProgress() * 2.147483647E9);
    }

    @Deprecated(forRemoval=true)
    public long getStartItemCount() {
        return Integer.MAX_VALUE;
    }
}

