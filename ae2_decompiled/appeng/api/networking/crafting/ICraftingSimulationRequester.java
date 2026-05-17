/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package appeng.api.networking.crafting;

import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import org.jetbrains.annotations.Nullable;

public interface ICraftingSimulationRequester {
    @Nullable
    public IActionSource getActionSource();

    @Nullable
    default public IGridNode getGridNode() {
        IActionSource actionSource = this.getActionSource();
        if (actionSource != null) {
            return actionSource.machine().map(IActionHost::getActionableNode).orElse(null);
        }
        return null;
    }
}

