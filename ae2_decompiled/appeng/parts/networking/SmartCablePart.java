/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Direction
 *  org.jetbrains.annotations.Nullable
 */
package appeng.parts.networking;

import appeng.api.networking.IGridNodeListener;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.util.AECableType;
import appeng.items.parts.ColoredPartItem;
import appeng.parts.networking.CablePart;
import appeng.parts.networking.IUsedChannelProvider;
import java.util.function.Predicate;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

public class SmartCablePart
extends CablePart
implements IUsedChannelProvider {
    public SmartCablePart(ColoredPartItem<?> partItem) {
        super(partItem);
    }

    @Override
    protected void onMainNodeStateChanged(IGridNodeListener.State reason) {
        if (reason != IGridNodeListener.State.GRID_BOOT) {
            this.getHost().markForUpdate();
        }
    }

    @Override
    public AECableType getCableConnectionType() {
        return AECableType.SMART;
    }

    @Override
    public void getBoxes(IPartCollisionHelper bch, Predicate<@Nullable Direction> filterConnections) {
        this.updateConnections();
        this.addNonDenseBoxes(bch, filterConnections, 5.0, 11.0);
    }
}

