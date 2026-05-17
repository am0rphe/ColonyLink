/*
 * Decompiled with CFR 0.152.
 */
package appeng.parts.networking;

import appeng.api.networking.IGridNodeListener;
import appeng.api.util.AECableType;
import appeng.items.parts.ColoredPartItem;
import appeng.parts.networking.DenseCablePart;
import appeng.parts.networking.IUsedChannelProvider;

public class SmartDenseCablePart
extends DenseCablePart
implements IUsedChannelProvider {
    public SmartDenseCablePart(ColoredPartItem<?> partItem) {
        super(partItem);
    }

    @Override
    public AECableType getCableConnectionType() {
        return AECableType.DENSE_SMART;
    }

    @Override
    protected void onMainNodeStateChanged(IGridNodeListener.State reason) {
        if (reason != IGridNodeListener.State.GRID_BOOT) {
            this.getHost().markForUpdate();
        }
    }
}

