/*
 * Decompiled with CFR 0.152.
 */
package appeng.parts.networking;

import appeng.api.util.AECableType;
import appeng.items.parts.ColoredPartItem;
import appeng.parts.networking.DenseCablePart;

public class CoveredDenseCablePart
extends DenseCablePart {
    public CoveredDenseCablePart(ColoredPartItem<?> partItem) {
        super(partItem);
    }

    @Override
    public AECableType getCableConnectionType() {
        return AECableType.DENSE_COVERED;
    }
}

