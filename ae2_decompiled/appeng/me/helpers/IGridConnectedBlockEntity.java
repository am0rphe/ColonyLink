/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Direction
 *  net.minecraft.world.entity.player.Player
 *  org.jetbrains.annotations.Nullable
 */
package appeng.me.helpers;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.api.orientation.BlockOrientation;
import appeng.block.IOwnerAwareBlockEntity;
import appeng.me.InWorldGridNode;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public interface IGridConnectedBlockEntity
extends IActionHost,
IOwnerAwareBlockEntity,
IInWorldGridNodeHost {
    public IManagedGridNode getMainNode();

    default public Set<Direction> getGridConnectableSides(BlockOrientation orientation) {
        return EnumSet.allOf(Direction.class);
    }

    @Nullable
    default public IGridNode getGridNode() {
        return this.getMainNode().getNode();
    }

    @Override
    default public IGridNode getGridNode(Direction dir) {
        InWorldGridNode inWorldGridNode;
        IGridNode node = this.getMainNode().getNode();
        if (node instanceof InWorldGridNode && (inWorldGridNode = (InWorldGridNode)node).isExposedOnSide(dir)) {
            return node;
        }
        return null;
    }

    default public boolean ifGridPresent(Consumer<IGrid> action) {
        return this.getMainNode().ifPresent(action);
    }

    public void saveChanges();

    default public void onMainNodeStateChanged(IGridNodeListener.State reason) {
    }

    @Override
    default public IGridNode getActionableNode() {
        return this.getMainNode().getNode();
    }

    @Override
    default public void setOwner(Player owner) {
        this.getMainNode().setOwningPlayer(owner);
    }
}

