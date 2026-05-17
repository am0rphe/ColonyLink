/*
 * Decompiled with CFR 0.152.
 */
package appeng.util.prioritylist;

import appeng.api.stacks.AEKey;
import appeng.util.prioritylist.IPartitionList;
import java.util.Collections;

public class DefaultPriorityList
implements IPartitionList {
    public static final DefaultPriorityList INSTANCE = new DefaultPriorityList();

    @Override
    public boolean isListed(AEKey input) {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public Iterable<AEKey> getItems() {
        return Collections.emptyList();
    }
}

