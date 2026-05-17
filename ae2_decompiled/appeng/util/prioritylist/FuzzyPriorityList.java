/*
 * Decompiled with CFR 0.152.
 */
package appeng.util.prioritylist;

import appeng.api.config.FuzzyMode;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.util.prioritylist.IPartitionList;

public class FuzzyPriorityList
implements IPartitionList {
    private final KeyCounter list;
    private final FuzzyMode mode;

    public FuzzyPriorityList(KeyCounter in, FuzzyMode mode) {
        this.list = in;
        this.mode = mode;
    }

    @Override
    public boolean isListed(AEKey input) {
        return !this.list.findFuzzy(input, this.mode).isEmpty();
    }

    @Override
    public boolean isEmpty() {
        return this.list.isEmpty();
    }

    @Override
    public Iterable<AEKey> getItems() {
        return this.list.keySet();
    }
}

