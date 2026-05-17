/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package appeng.api.networking.security;

import appeng.api.networking.IGridNode;
import org.jetbrains.annotations.Nullable;

public interface IActionHost {
    @Nullable
    public IGridNode getActionableNode();
}

