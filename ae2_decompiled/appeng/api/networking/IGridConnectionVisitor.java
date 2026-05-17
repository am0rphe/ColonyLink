/*
 * Decompiled with CFR 0.152.
 */
package appeng.api.networking;

import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridVisitor;

public interface IGridConnectionVisitor
extends IGridVisitor {
    public void visitConnection(IGridConnection var1);
}

