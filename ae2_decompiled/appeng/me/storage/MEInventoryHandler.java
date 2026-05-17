/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Object2LongMap$Entry
 */
package appeng.me.storage;

import appeng.api.config.Actionable;
import appeng.api.config.IncludeExclude;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import appeng.me.storage.DelegatingMEInventory;
import appeng.util.prioritylist.DefaultPriorityList;
import appeng.util.prioritylist.IPartitionList;
import it.unimi.dsi.fastutil.objects.Object2LongMap;

public class MEInventoryHandler
extends DelegatingMEInventory {
    private IPartitionList partitionList = DefaultPriorityList.INSTANCE;
    private IncludeExclude partitionListMode = IncludeExclude.WHITELIST;
    private boolean filterOnExtraction;
    private boolean filterAvailableContents;
    private boolean allowExtraction = true;
    private boolean allowInsertion = true;
    private boolean voidOverflow;
    private boolean gettingAvailableContent = false;

    public MEInventoryHandler(MEStorage inventory) {
        super(inventory);
    }

    public void setAllowExtraction(boolean allowExtraction) {
        this.allowExtraction = allowExtraction;
    }

    public void setAllowInsertion(boolean allowInsertion) {
        this.allowInsertion = allowInsertion;
    }

    protected IncludeExclude getWhitelist() {
        return this.partitionListMode;
    }

    public void setWhitelist(IncludeExclude myWhitelist) {
        this.partitionListMode = myWhitelist;
    }

    protected IPartitionList getPartitionList() {
        return this.partitionList;
    }

    public void setPartitionList(IPartitionList myPartitionList) {
        this.partitionList = myPartitionList;
    }

    public void setExtractFiltering(boolean filterOnExtraction, boolean filterAvailableContents) {
        this.filterOnExtraction = filterOnExtraction;
        this.filterAvailableContents = filterAvailableContents;
    }

    public void setVoidOverflow(boolean voidOverflow) {
        this.voidOverflow = voidOverflow;
    }

    @Override
    public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
        if (!this.allowInsertion || !this.passesBlackOrWhitelist(what)) {
            return 0L;
        }
        long inserted = super.insert(what, amount, mode, source);
        return this.voidOverflow ? amount : inserted;
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        if (this.filterOnExtraction && !this.canExtract(what)) {
            return 0L;
        }
        return super.extract(what, amount, mode, source);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void getAvailableStacks(KeyCounter out) {
        if (this.gettingAvailableContent) {
            return;
        }
        this.gettingAvailableContent = true;
        try {
            if (!this.filterAvailableContents) {
                super.getAvailableStacks(out);
            } else {
                if (!this.allowExtraction) {
                    return;
                }
                for (Object2LongMap.Entry<AEKey> entry : this.getDelegate().getAvailableStacks()) {
                    if (!this.canExtract((AEKey)entry.getKey())) continue;
                    out.add((AEKey)entry.getKey(), entry.getLongValue());
                }
            }
        }
        finally {
            this.gettingAvailableContent = false;
        }
    }

    @Override
    public boolean isPreferredStorageFor(AEKey input, IActionSource source) {
        if (this.partitionListMode == IncludeExclude.WHITELIST && this.partitionList.isListed(input)) {
            return true;
        }
        if (super.extract(input, 1L, Actionable.SIMULATE, source) > 0L) {
            return true;
        }
        return super.isPreferredStorageFor(input, source);
    }

    protected boolean canExtract(AEKey request) {
        return this.allowExtraction && this.passesBlackOrWhitelist(request);
    }

    private boolean passesBlackOrWhitelist(AEKey input) {
        return this.partitionList.matchesFilter(input, this.partitionListMode);
    }
}

