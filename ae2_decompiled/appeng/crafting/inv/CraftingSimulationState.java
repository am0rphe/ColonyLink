/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Iterables
 *  it.unimi.dsi.fastutil.objects.Object2LongMap$Entry
 *  org.jetbrains.annotations.Nullable
 */
package appeng.crafting.inv;

import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.crafting.CraftingCalculation;
import appeng.crafting.CraftingPlan;
import appeng.crafting.inv.ICraftingSimulationState;
import com.google.common.collect.Iterables;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.Nullable;

public abstract class CraftingSimulationState
implements ICraftingSimulationState {
    private final KeyCounter unmodifiedCache;
    private final KeyCounter modifiableCache;
    private final KeyCounter emittedItems;
    private double bytes = 0.0;
    private final Map<IPatternDetails, Long> crafts = new HashMap<IPatternDetails, Long>();
    private final KeyCounter requiredExtract;

    protected CraftingSimulationState() {
        this.unmodifiedCache = new KeyCounter();
        this.modifiableCache = new KeyCounter();
        this.emittedItems = new KeyCounter();
        this.requiredExtract = new KeyCounter();
    }

    protected abstract long simulateExtractParent(AEKey var1, long var2);

    protected abstract Iterable<AEKey> findFuzzyParent(AEKey var1);

    private void cacheFuzzy(AEKey what) {
        if (this.unmodifiedCache.findFuzzy(what, FuzzyMode.IGNORE_ALL).isEmpty()) {
            boolean insertedAny = false;
            for (AEKey keyToCache : this.findFuzzyParent(what)) {
                long extracted = this.simulateExtractParent(keyToCache, Long.MAX_VALUE);
                if (extracted != 0L) {
                    insertedAny = true;
                }
                this.modifiableCache.add(keyToCache, extracted);
                this.unmodifiedCache.add(keyToCache, extracted);
            }
            if (!insertedAny) {
                this.unmodifiedCache.add(what, 0L);
            }
        }
    }

    @Override
    public void insert(AEKey what, long amount, Actionable mode) {
        this.cacheFuzzy(what);
        if (mode == Actionable.MODULATE) {
            this.modifiableCache.add(what, amount);
        }
    }

    private void updateRequiredExtract(AEKey key, long delta) {
        if (delta > 0L) {
            long max = Math.max(delta, this.requiredExtract.get(key));
            this.requiredExtract.set(key, max);
        }
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode) {
        this.cacheFuzzy(what);
        long cachedAmount = this.modifiableCache.get(what);
        if (cachedAmount == 0L) {
            return 0L;
        }
        long extracted = Math.min(cachedAmount, amount);
        if (mode == Actionable.MODULATE) {
            this.modifiableCache.remove(what, extracted);
        }
        this.updateRequiredExtract(what, this.unmodifiedCache.get(what) - this.modifiableCache.get(what));
        return extracted;
    }

    @Override
    @Nullable
    public Iterable<AEKey> findFuzzyTemplates(AEKey input) {
        if (input == null) {
            return Collections.emptyList();
        }
        this.cacheFuzzy(input);
        return Iterables.transform(this.modifiableCache.findFuzzy(input, FuzzyMode.IGNORE_ALL), Map.Entry::getKey);
    }

    @Override
    public void emitItems(AEKey what, long amount) {
        this.emittedItems.add(what, amount);
    }

    @Override
    public void addBytes(double bytes) {
        this.bytes += bytes;
    }

    @Override
    public void addCrafting(IPatternDetails details, long crafts) {
        this.crafts.merge(details, crafts, Long::sum);
    }

    public void ignore(AEKey stack) {
        this.cacheFuzzy(stack);
        this.unmodifiedCache.set(stack, 0L);
        this.modifiableCache.set(stack, 0L);
    }

    public void applyDiff(CraftingSimulationState parent) {
        for (Object2LongMap.Entry<AEKey> entry : this.requiredExtract) {
            AEKey key = (AEKey)entry.getKey();
            long delta = parent.unmodifiedCache.get(key) - parent.modifiableCache.get(key) + entry.getLongValue();
            parent.updateRequiredExtract(key, delta);
        }
        for (Object2LongMap.Entry<AEKey> entry : this.modifiableCache) {
            long unmodified = this.unmodifiedCache.get((AEKey)entry.getKey());
            long sizeDelta = entry.getLongValue() - unmodified;
            if (sizeDelta > 0L) {
                parent.insert((AEKey)entry.getKey(), sizeDelta, Actionable.MODULATE);
                continue;
            }
            if (sizeDelta >= 0L) continue;
            long newStackSize = -sizeDelta;
            long reallyExtracted = parent.extract((AEKey)entry.getKey(), newStackSize, Actionable.MODULATE);
            if (reallyExtracted == -sizeDelta) continue;
            throw new IllegalStateException("Failed to extract from parent. This is a bug!");
        }
        for (Object2LongMap.Entry<AEKey> entry : this.emittedItems) {
            parent.emitItems((AEKey)entry.getKey(), entry.getLongValue());
        }
        parent.addBytes(this.bytes);
        for (Map.Entry entry : this.crafts.entrySet()) {
            parent.addCrafting((IPatternDetails)entry.getKey(), (Long)entry.getValue());
        }
    }

    public static CraftingPlan buildCraftingPlan(CraftingSimulationState state, CraftingCalculation calculation, long calculatedAmount) {
        return new CraftingPlan(new GenericStack(calculation.getOutput(), calculatedAmount), (long)Math.ceil(state.bytes), calculation.isSimulation(), calculation.hasMultiplePaths(), state.requiredExtract, state.emittedItems, calculation.getMissingItems(), state.crafts);
    }
}

