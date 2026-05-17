/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package appeng.api.storage;

import appeng.api.networking.IGridNode;
import appeng.api.storage.ILinkStatus;
import appeng.api.util.IConfigurableObject;
import org.jetbrains.annotations.Nullable;

public interface IPatternAccessTermMenuHost
extends IConfigurableObject {
    @Nullable
    public IGridNode getGridNode();

    public ILinkStatus getLinkStatus();
}

