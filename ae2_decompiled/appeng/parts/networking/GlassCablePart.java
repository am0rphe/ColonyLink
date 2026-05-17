/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Direction
 *  org.jetbrains.annotations.Nullable
 */
package appeng.parts.networking;

import appeng.api.parts.IPartCollisionHelper;
import appeng.api.util.AECableType;
import appeng.items.parts.ColoredPartItem;
import appeng.parts.networking.CablePart;
import java.util.function.Predicate;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

public class GlassCablePart
extends CablePart {
    public GlassCablePart(ColoredPartItem<?> partItem) {
        super(partItem);
    }

    @Override
    public AECableType getCableConnectionType() {
        return AECableType.GLASS;
    }

    @Override
    public void getBoxes(IPartCollisionHelper bch, Predicate<@Nullable Direction> filterConnections) {
        this.updateConnections();
        this.addNonDenseBoxes(bch, filterConnections, 6.0, 10.0);
    }
}

