/*
 * Decompiled with CFR 0.152.
 */
package appeng.util.prioritylist;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.util.prioritylist.IPartitionList;

public class PrecisePriorityList
implements IPartitionList {
    private final KeyCounter list;

    public PrecisePriorityList(KeyCounter in) {
        this.list = in;
    }

    @Override
    public boolean isListed(AEKey input) {
        return this.list.get(input) > 0L;
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

