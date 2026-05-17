/*
 * Decompiled with CFR 0.152.
 */
package appeng.crafting;

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import java.util.Map;

public record CraftingPlan(GenericStack finalOutput, long bytes, boolean simulation, boolean multiplePaths, KeyCounter usedItems, KeyCounter emittedItems, KeyCounter missingItems, Map<IPatternDetails, Long> patternTimes) implements ICraftingPlan
{
}

