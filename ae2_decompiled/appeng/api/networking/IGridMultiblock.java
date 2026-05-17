/*
 * Decompiled with CFR 0.152.
 */
package appeng.api.networking;

import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeService;
import java.util.Iterator;

public interface IGridMultiblock
extends IGridNodeService {
    public Iterator<IGridNode> getMultiblockNodes();
}

