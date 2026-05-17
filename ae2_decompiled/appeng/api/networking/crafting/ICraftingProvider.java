/*
 * Decompiled with CFR 0.152.
 */
package appeng.api.networking.crafting;

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeService;
import appeng.api.networking.IManagedGridNode;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import java.util.List;
import java.util.Set;

public interface ICraftingProvider
extends IGridNodeService {
    public List<IPatternDetails> getAvailablePatterns();

    default public int getPatternPriority() {
        return 0;
    }

    public boolean pushPattern(IPatternDetails var1, KeyCounter[] var2);

    public boolean isBusy();

    default public Set<AEKey> getEmitableItems() {
        return Set.of();
    }

    public static void requestUpdate(IManagedGridNode managedNode) {
        IGridNode node = managedNode.getNode();
        if (node != null) {
            node.getGrid().getCraftingService().refreshNodeCraftingProvider(node);
        }
    }
}

