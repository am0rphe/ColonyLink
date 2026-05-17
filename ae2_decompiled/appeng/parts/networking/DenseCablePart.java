/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  org.jetbrains.annotations.Nullable
 */
package appeng.parts.networking;

import appeng.api.networking.GridFlags;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.parts.BusSupport;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.util.AECableType;
import appeng.items.parts.ColoredPartItem;
import appeng.parts.networking.CablePart;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

public abstract class DenseCablePart
extends CablePart {
    public DenseCablePart(ColoredPartItem<?> partItem) {
        super(partItem);
        this.getMainNode().setFlags(GridFlags.DENSE_CAPACITY);
    }

    @Override
    public BusSupport supportsBuses() {
        return BusSupport.DENSE_CABLE;
    }

    @Override
    public void getBoxes(IPartCollisionHelper bch, Predicate<@Nullable Direction> filterConnections) {
        double max;
        this.updateConnections();
        boolean noLadder = !bch.isBBCollision();
        double min = noLadder ? 3.0 : 4.9;
        double d = max = noLadder ? 13.0 : 11.1;
        if (filterConnections.test(null)) {
            bch.addBox(min, min, min, max, max, max);
        }
        for (Direction of : this.getConnections()) {
            if (!filterConnections.test(of)) continue;
            if (this.isDense(of)) {
                DenseCablePart.addConnectionBox(bch, of, min, max, 0.0);
                continue;
            }
            DenseCablePart.addConnectionBox(bch, of, 5.0, 11.0, 0.0);
        }
    }

    private boolean isDense(Direction of) {
        BlockPos adjacentPos = this.getBlockEntity().getBlockPos().relative(of);
        if (!this.getLevel().hasChunkAt(adjacentPos)) {
            return false;
        }
        IInWorldGridNodeHost adjacentHost = GridHelper.getNodeHost(this.getBlockEntity().getLevel(), adjacentPos);
        if (adjacentHost != null) {
            AECableType t = adjacentHost.getCableConnectionType(of.getOpposite());
            return t.isDense();
        }
        return false;
    }
}

