/*
 * Decompiled with CFR 0.152.
 */
package appeng.crafting.inv;

import appeng.api.config.Actionable;
import appeng.api.stacks.AEKey;

public interface ICraftingInventory {
    public void insert(AEKey var1, long var2, Actionable var4);

    public long extract(AEKey var1, long var2, Actionable var4);

    public Iterable<AEKey> findFuzzyTemplates(AEKey var1);
}

