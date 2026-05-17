/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Iterators
 *  it.unimi.dsi.fastutil.objects.Object2LongMap$Entry
 *  org.jetbrains.annotations.Nullable
 */
package appeng.me.service.helpers;

import appeng.api.config.FuzzyMode;
import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.AEKeyFilter;
import appeng.hooks.ticking.TickHandler;
import com.google.common.collect.Iterators;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.jetbrains.annotations.Nullable;

public class NetworkCraftingProviders {
    private final Map<IGridNode, ProviderState> craftingProviders = new HashMap<IGridNode, ProviderState>();
    private final List<ProviderState> globalProviders = new ArrayList<ProviderState>();
    private final Map<IPatternDetails, CraftingProviderList> craftingMethods = new HashMap<IPatternDetails, CraftingProviderList>();
    private final Map<AEKey, PatternsForKey> craftableItems = new HashMap<AEKey, PatternsForKey>();
    private final KeyCounter craftableItemsList = new KeyCounter();
    private final Map<AEKey, Integer> emitableItems = new HashMap<AEKey, Integer>();
    private final Set<AEKey> craftableKeys = Collections.unmodifiableSet(this.craftableItems.keySet());
    private final Set<AEKey> emittableKeys = Collections.unmodifiableSet(this.emitableItems.keySet());
    private long lastModifiedOnTick = TickHandler.instance().getCurrentTick();

    public void addProvider(IGridNode node) {
        ICraftingProvider provider = node.getService(ICraftingProvider.class);
        if (provider != null) {
            if (this.craftingProviders.containsKey(node)) {
                throw new IllegalArgumentException("Duplicate crafting provider registration for node " + String.valueOf(node));
            }
            ProviderState state = new ProviderState(provider);
            state.mount(this);
            this.craftingProviders.put(node, state);
            this.setLastModifiedOnTick();
        }
    }

    public void addProvider(ICraftingProvider provider) {
        for (ProviderState state : this.globalProviders) {
            if (state.provider != provider) continue;
            throw new IllegalArgumentException("Duplicate crafting provider registration for " + String.valueOf(provider));
        }
        ProviderState state = new ProviderState(provider);
        state.mount(this);
        this.globalProviders.add(state);
        this.setLastModifiedOnTick();
    }

    public void removeProvider(IGridNode node) {
        ProviderState state;
        ICraftingProvider provider = node.getService(ICraftingProvider.class);
        if (provider != null && (state = this.craftingProviders.remove(node)) != null) {
            state.unmount(this);
            this.setLastModifiedOnTick();
        }
    }

    public void removeProvider(ICraftingProvider provider) {
        Iterator<ProviderState> it = this.globalProviders.iterator();
        while (it.hasNext()) {
            ProviderState state = it.next();
            if (state.provider != provider) continue;
            it.remove();
            state.unmount(this);
            this.setLastModifiedOnTick();
        }
    }

    public Set<AEKey> getCraftables(AEKeyFilter filter) {
        HashSet<AEKey> result = new HashSet<AEKey>();
        for (AEKey stack : this.craftableItems.keySet()) {
            if (!filter.matches(stack)) continue;
            result.add(stack);
        }
        for (AEKey stack : this.emitableItems.keySet()) {
            if (!filter.matches(stack)) continue;
            result.add(stack);
        }
        return result;
    }

    public Set<AEKey> getCraftableKeys() {
        return this.craftableKeys;
    }

    public Set<AEKey> getEmittableKeys() {
        return this.emittableKeys;
    }

    public Collection<IPatternDetails> getCraftingFor(AEKey whatToCraft) {
        PatternsForKey patterns = this.craftableItems.get(whatToCraft);
        if (patterns != null) {
            return patterns.getSortedPatterns();
        }
        return Collections.emptyList();
    }

    @Nullable
    public AEKey getFuzzyCraftable(AEKey whatToCraft, AEKeyFilter filter) {
        for (Object2LongMap.Entry<AEKey> fuzzy : this.craftableItemsList.findFuzzy(whatToCraft, FuzzyMode.IGNORE_ALL)) {
            if (!filter.matches((AEKey)fuzzy.getKey())) continue;
            return (AEKey)fuzzy.getKey();
        }
        return null;
    }

    public boolean canEmitFor(AEKey someItem) {
        return this.emitableItems.containsKey(someItem);
    }

    public Iterable<ICraftingProvider> getMediums(IPatternDetails key) {
        CraftingProviderList mediumList = this.craftingMethods.get(key);
        return Objects.requireNonNullElse(mediumList, Collections.emptyList());
    }

    private void setLastModifiedOnTick() {
        this.lastModifiedOnTick = TickHandler.instance().getCurrentTick();
    }

    public long getLastModifiedOnTick() {
        return this.lastModifiedOnTick;
    }

    private static class ProviderState {
        private final ICraftingProvider provider;
        private final Set<AEKey> emitableItems;
        private final List<IPatternDetails> patterns;
        private final int priority;

        private ProviderState(ICraftingProvider provider) {
            this.provider = provider;
            this.emitableItems = new HashSet<AEKey>(provider.getEmitableItems());
            this.patterns = new ArrayList<IPatternDetails>(provider.getAvailablePatterns());
            this.priority = provider.getPatternPriority();
        }

        private void mount(NetworkCraftingProviders methods) {
            for (AEKey emitable : this.emitableItems) {
                methods.emitableItems.merge(emitable, 1, Integer::sum);
            }
            for (IPatternDetails pattern : this.patterns) {
                GenericStack primaryOutput = pattern.getPrimaryOutput();
                methods.craftableItemsList.add(primaryOutput.what(), 1L);
                PatternsForKey patternsForKey = methods.craftableItems.computeIfAbsent(primaryOutput.what(), k -> new PatternsForKey());
                patternsForKey.patterns.add(new PatternInfo(pattern, this));
                patternsForKey.needsSorting = true;
                methods.craftingMethods.computeIfAbsent(pattern, d -> new CraftingProviderList()).add(this.provider);
            }
        }

        private void unmount(NetworkCraftingProviders methods) {
            for (AEKey emitable : this.emitableItems) {
                methods.emitableItems.compute(emitable, (key, cnt) -> cnt == 1 ? null : Integer.valueOf(cnt - 1));
            }
            for (IPatternDetails pattern : this.patterns) {
                GenericStack primaryOutput = pattern.getPrimaryOutput();
                methods.craftableItemsList.remove(primaryOutput.what(), 1L);
                methods.craftableItems.computeIfPresent(primaryOutput.what(), (key, patternsForKey) -> {
                    patternsForKey.patterns.remove(new PatternInfo(pattern, this));
                    patternsForKey.needsSorting = true;
                    return patternsForKey.patterns.isEmpty() ? null : patternsForKey;
                });
                methods.craftingMethods.computeIfPresent(pattern, (pat, list) -> {
                    list.remove(this.provider);
                    return list.providers.isEmpty() ? null : list;
                });
            }
        }
    }

    private static class PatternsForKey {
        private final Set<PatternInfo> patterns = new HashSet<PatternInfo>();
        private List<IPatternDetails> sortedPatterns = Collections.emptyList();
        private boolean needsSorting = false;

        private PatternsForKey() {
        }

        private void sortPatterns() {
            this.sortedPatterns = this.patterns.stream().sorted(Comparator.comparingInt(pi -> pi.state.priority).reversed()).map(PatternInfo::pattern).distinct().toList();
        }

        private List<IPatternDetails> getSortedPatterns() {
            if (this.needsSorting) {
                this.sortPatterns();
            }
            return this.sortedPatterns;
        }
    }

    private static class CraftingProviderList
    implements Iterable<ICraftingProvider> {
        private final List<ICraftingProvider> providers = new ArrayList<ICraftingProvider>();
        private Iterator<ICraftingProvider> cycleIterator = Iterators.cycle(this.providers);

        private CraftingProviderList() {
        }

        private void add(ICraftingProvider provider) {
            this.providers.add(provider);
            this.cycleIterator = Iterators.cycle(this.providers);
        }

        private void remove(ICraftingProvider provider) {
            this.providers.remove(provider);
            this.cycleIterator = Iterators.cycle(this.providers);
        }

        @Override
        public Iterator<ICraftingProvider> iterator() {
            return Iterators.limit(this.cycleIterator, (int)this.providers.size());
        }
    }

    private record PatternInfo(IPatternDetails pattern, ProviderState state) {
    }
}

