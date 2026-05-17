/*
 * Decompiled with CFR 0.152.
 */
package appeng.crafting.inv;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEKey;
import appeng.crafting.inv.ICraftingInventory;

public interface ICraftingSimulationState
extends ICraftingInventory {
    public void emitItems(AEKey var1, long var2);

    public void addBytes(double var1);

    default public void addStackBytes(AEKey key, long amount, long multiplier) {
        this.addBytes((double)amount * (double)multiplier / (double)key.getType().getAmountPerByte() * 8.0);
    }

    public void addCrafting(IPatternDetails var1, long var2);
}

