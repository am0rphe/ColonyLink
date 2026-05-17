/*
 * Decompiled with CFR 0.152.
 */
package appeng.api.networking.crafting;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import java.util.Map;

public interface ICraftingPlan {
    public GenericStack finalOutput();

    public long bytes();

    public boolean simulation();

    public boolean multiplePaths();

    public KeyCounter usedItems();

    public KeyCounter emittedItems();

    public KeyCounter missingItems();

    public Map<IPatternDetails, Long> patternTimes();
}

