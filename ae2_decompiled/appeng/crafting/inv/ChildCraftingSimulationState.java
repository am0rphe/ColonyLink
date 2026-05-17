/*
 * Decompiled with CFR 0.152.
 */
package appeng.crafting.inv;

import appeng.api.config.Actionable;
import appeng.api.stacks.AEKey;
import appeng.crafting.inv.CraftingSimulationState;
import appeng.crafting.inv.ICraftingInventory;

public class ChildCraftingSimulationState
extends CraftingSimulationState {
    private final ICraftingInventory parent;

    public ChildCraftingSimulationState(ICraftingInventory parent) {
        this.parent = parent;
    }

    @Override
    protected long simulateExtractParent(AEKey what, long amount) {
        return this.parent.extract(what, amount, Actionable.SIMULATE);
    }

    @Override
    protected Iterable<AEKey> findFuzzyParent(AEKey input) {
        return this.parent.findFuzzyTemplates(input);
    }
}

