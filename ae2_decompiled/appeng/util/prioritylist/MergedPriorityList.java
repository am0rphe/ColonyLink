/*
 * Decompiled with CFR 0.152.
 */
package appeng.util.prioritylist;

import appeng.api.stacks.AEKey;
import appeng.util.prioritylist.IPartitionList;
import java.util.ArrayList;
import java.util.Collection;

public final class MergedPriorityList
implements IPartitionList {
    private final Collection<IPartitionList> positive = new ArrayList<IPartitionList>();
    private final Collection<IPartitionList> negative = new ArrayList<IPartitionList>();

    public void addNewList(IPartitionList list, boolean isWhitelist) {
        if (isWhitelist) {
            this.positive.add(list);
        } else {
            this.negative.add(list);
        }
    }

    @Override
    public boolean isListed(AEKey input) {
        for (IPartitionList l : this.negative) {
            if (!l.isListed(input)) continue;
            return false;
        }
        if (!this.positive.isEmpty()) {
            for (IPartitionList l : this.positive) {
                if (!l.isListed(input)) continue;
                return true;
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean isEmpty() {
        return this.positive.isEmpty() && this.negative.isEmpty();
    }

    @Override
    public Iterable<AEKey> getItems() {
        throw new UnsupportedOperationException();
    }
}

