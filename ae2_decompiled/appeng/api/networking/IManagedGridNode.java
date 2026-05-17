/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  org.jetbrains.annotations.Nullable
 */
package appeng.api.networking;

import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeService;
import appeng.api.stacks.AEItemKey;
import appeng.api.util.AEColor;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public interface IManagedGridNode {
    public void destroy();

    public void create(Level var1, @Nullable BlockPos var2);

    public void loadFromNBT(CompoundTag var1);

    public void saveToNBT(CompoundTag var1);

    default public boolean ifPresent(Consumer<IGrid> action) {
        IGridNode node = this.getNode();
        if (node == null) {
            return false;
        }
        action.accept(node.getGrid());
        return true;
    }

    default public boolean ifPresent(BiConsumer<IGrid, IGridNode> action) {
        IGridNode node = this.getNode();
        if (node == null) {
            return false;
        }
        action.accept(node.getGrid(), node);
        return true;
    }

    @Nullable
    default public IGrid getGrid() {
        IGridNode node = this.getNode();
        if (node == null) {
            return null;
        }
        return node.getGrid();
    }

    public IManagedGridNode setFlags(GridFlags ... var1);

    public IManagedGridNode setExposedOnSides(Set<Direction> var1);

    public IManagedGridNode setIdlePowerUsage(double var1);

    public IManagedGridNode setVisualRepresentation(@Nullable AEItemKey var1);

    default public IManagedGridNode setVisualRepresentation(ItemStack visualRepresentation) {
        return this.setVisualRepresentation(AEItemKey.of(visualRepresentation));
    }

    default public IManagedGridNode setVisualRepresentation(ItemLike visualRepresentation) {
        return this.setVisualRepresentation(AEItemKey.of(visualRepresentation));
    }

    public IManagedGridNode setInWorldNode(boolean var1);

    public IManagedGridNode setTagName(String var1);

    public IManagedGridNode setGridColor(AEColor var1);

    public <T extends IGridNodeService> IManagedGridNode addService(Class<T> var1, T var2);

    public boolean isReady();

    public boolean isActive();

    public boolean isOnline();

    public boolean isPowered();

    public boolean hasGridBooted();

    public void setOwningPlayerId(int var1);

    public void setOwningPlayer(Player var1);

    @Nullable
    public IGridNode getNode();
}

