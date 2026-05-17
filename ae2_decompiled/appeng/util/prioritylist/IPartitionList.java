/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package appeng.util.prioritylist;

import appeng.api.config.FuzzyMode;
import appeng.api.config.IncludeExclude;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.util.prioritylist.DefaultPriorityList;
import appeng.util.prioritylist.FuzzyPriorityList;
import appeng.util.prioritylist.PrecisePriorityList;
import org.jetbrains.annotations.Nullable;

public interface IPartitionList {
    public boolean isListed(AEKey var1);

    public boolean isEmpty();

    public Iterable<AEKey> getItems();

    default public boolean matchesFilter(AEKey key, IncludeExclude mode) {
        if (!this.isEmpty()) {
            switch (mode) {
                case WHITELIST: {
                    if (this.isListed(key)) break;
                    return false;
                }
                case BLACKLIST: {
                    if (!this.isListed(key)) break;
                    return false;
                }
            }
        }
        return true;
    }

    public static Builder builder() {
        return new Builder(null);
    }

    public static class Builder {
        private final KeyCounter keys = new KeyCounter();
        @Nullable
        private FuzzyMode fuzzyMode;

        private Builder(@Nullable FuzzyMode fuzzyMode) {
            this.fuzzyMode = fuzzyMode;
        }

        public void add(@Nullable AEKey key) {
            if (key != null) {
                this.keys.add(key, 1L);
            }
        }

        public void addAll(Iterable<AEKey> keys) {
            for (AEKey key : keys) {
                this.keys.add(key, 1L);
            }
        }

        public void fuzzyMode(FuzzyMode mode) {
            this.fuzzyMode = mode;
        }

        public IPartitionList build() {
            if (this.keys.isEmpty()) {
                return DefaultPriorityList.INSTANCE;
            }
            if (this.fuzzyMode != null) {
                return new FuzzyPriorityList(this.keys, this.fuzzyMode);
            }
            return new PrecisePriorityList(this.keys);
        }
    }
}

