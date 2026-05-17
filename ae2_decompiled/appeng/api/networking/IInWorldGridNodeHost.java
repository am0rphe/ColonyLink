/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Direction
 *  org.jetbrains.annotations.Nullable
 */
package appeng.api.networking;

import appeng.api.networking.IGridNode;
import appeng.api.util.AECableType;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

public interface IInWorldGridNodeHost {
    @Nullable
    public IGridNode getGridNode(Direction var1);

    default public AECableType getCableConnectionType(Direction dir) {
        return AECableType.GLASS;
    }
}

