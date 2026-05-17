/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Iterables
 *  it.unimi.dsi.fastutil.objects.Object2LongMap$Entry
 *  org.jetbrains.annotations.Nullable
 */
package appeng.crafting.inv;

import appeng.api.config.FuzzyMode;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageService;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.crafting.inv.CraftingSimulationState;
import com.google.common.collect.Iterables;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import java.util.Map;
import org.jetbrains.annotations.Nullable;

public class NetworkCraftingSimulationState
extends CraftingSimulationState {
    private final KeyCounter list;

    public NetworkCraftingSimulationState(IStorageService storage, @Nullable IActionSource src) {
        if (src != null && src.player().isPresent()) {
            this.list = storage.getInventory().getAvailableStacks();
        } else {
            this.list = new KeyCounter();
            for (Object2LongMap.Entry<AEKey> stack : storage.getCachedInventory()) {
                long networkAmount = stack.getLongValue();
                if (networkAmount <= 0L) continue;
                this.list.add((AEKey)stack.getKey(), networkAmount);
            }
        }
    }

    @Override
    protected long simulateExtractParent(AEKey what, long amount) {
        return Math.min(this.list.get(what), amount);
    }

    @Override
    protected Iterable<AEKey> findFuzzyParent(AEKey input) {
        return Iterables.transform(this.list.findFuzzy(input, FuzzyMode.IGNORE_ALL), Map.Entry::getKey);
    }
}

