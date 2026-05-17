/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  it.unimi.dsi.fastutil.objects.Object2LongMap$Entry
 *  net.minecraft.network.RegistryFriendlyByteBuf
 */
package appeng.menu.me.crafting;

import appeng.api.config.Actionable;
import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.MEStorage;
import appeng.menu.me.crafting.CraftingPlanSummaryEntry;
import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.network.RegistryFriendlyByteBuf;

public class CraftingPlanSummary {
    private final long usedBytes;
    private final boolean simulation;
    private final List<CraftingPlanSummaryEntry> entries;

    public CraftingPlanSummary(long usedBytes, boolean simulation, List<CraftingPlanSummaryEntry> entries) {
        this.usedBytes = usedBytes;
        this.simulation = simulation;
        this.entries = entries;
    }

    public long getUsedBytes() {
        return this.usedBytes;
    }

    public boolean isSimulation() {
        return this.simulation;
    }

    public List<CraftingPlanSummaryEntry> getEntries() {
        return this.entries;
    }

    public void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeVarLong(this.usedBytes);
        buffer.writeBoolean(this.simulation);
        buffer.writeVarInt(this.entries.size());
        for (CraftingPlanSummaryEntry entry : this.entries) {
            entry.write(buffer);
        }
    }

    public static CraftingPlanSummary read(RegistryFriendlyByteBuf buffer) {
        long bytesUsed = buffer.readVarLong();
        boolean simulation = buffer.readBoolean();
        int entryCount = buffer.readVarInt();
        ImmutableList.Builder entries = ImmutableList.builder();
        for (int i = 0; i < entryCount; ++i) {
            entries.add((Object)CraftingPlanSummaryEntry.read(buffer));
        }
        return new CraftingPlanSummary(bytesUsed, simulation, (List<CraftingPlanSummaryEntry>)entries.build());
    }

    public static CraftingPlanSummary fromJob(IGrid grid, IActionSource actionSource, ICraftingPlan job) {
        var plan = new HashMap<AEKey, KeyStats>(){

            private KeyStats mapping(AEKey key) {
                Objects.requireNonNull(key, "Key may not be null");
                return this.computeIfAbsent(key, k -> new KeyStats());
            }
        };
        for (Object2LongMap.Entry<AEKey> entry : job.usedItems()) {
            plan.mapping((AEKey)((AEKey)entry.getKey())).stored += entry.getLongValue();
        }
        for (Object2LongMap.Entry<AEKey> entry : job.missingItems()) {
            plan.mapping((AEKey)((AEKey)entry.getKey())).stored += entry.getLongValue();
        }
        for (Object2LongMap.Entry<AEKey> entry : job.emittedItems()) {
            KeyStats entry2 = plan.mapping((AEKey)entry.getKey());
            entry2.stored += entry.getLongValue();
            entry2.crafting += entry.getLongValue();
        }
        for (Map.Entry entry : job.patternTimes().entrySet()) {
            for (GenericStack out : ((IPatternDetails)entry.getKey()).getOutputs()) {
                plan.mapping((AEKey)out.what()).crafting += out.amount() * (Long)entry.getValue();
            }
        }
        ArrayList<CraftingPlanSummaryEntry> entries = new ArrayList<CraftingPlanSummaryEntry>();
        MEStorage mEStorage = grid.getStorageService().getInventory();
        ICraftingService crafting = grid.getCraftingService();
        for (Map.Entry out : plan.entrySet()) {
            long missingAmount;
            long storedAmount;
            if (job.simulation() && !crafting.canEmitFor((AEKey)out.getKey())) {
                storedAmount = mEStorage.extract((AEKey)out.getKey(), ((KeyStats)out.getValue()).stored, Actionable.SIMULATE, actionSource);
                missingAmount = ((KeyStats)out.getValue()).stored - storedAmount;
            } else {
                storedAmount = ((KeyStats)out.getValue()).stored;
                missingAmount = 0L;
            }
            long craftAmount = ((KeyStats)out.getValue()).crafting;
            entries.add(new CraftingPlanSummaryEntry((AEKey)out.getKey(), missingAmount, storedAmount, craftAmount));
        }
        Collections.sort(entries);
        return new CraftingPlanSummary(job.bytes(), job.simulation(), List.copyOf(entries));
    }

    private static class KeyStats {
        public long stored;
        public long crafting;

        private KeyStats() {
        }
    }
}

