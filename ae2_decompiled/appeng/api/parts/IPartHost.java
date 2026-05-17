/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Direction
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.phys.Vec3
 *  net.minecraft.world.phys.shapes.CollisionContext
 *  net.minecraft.world.phys.shapes.VoxelShape
 *  org.jetbrains.annotations.Nullable
 */
package appeng.api.parts;

import appeng.api.parts.ICustomCableConnection;
import appeng.api.parts.IFacadeContainer;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartItem;
import appeng.api.parts.SelectedPart;
import appeng.api.util.AEColor;
import appeng.api.util.DimensionalBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public interface IPartHost
extends ICustomCableConnection {
    public IFacadeContainer getFacadeContainer();

    @Nullable
    public IPart getPart(@Nullable Direction var1);

    public boolean canAddPart(ItemStack var1, @Nullable Direction var2);

    @Nullable
    public <T extends IPart> T addPart(IPartItem<T> var1, @Nullable Direction var2, @Nullable Player var3);

    @Nullable
    public <T extends IPart> T replacePart(IPartItem<T> var1, @Nullable Direction var2, @Nullable Player var3, @Nullable InteractionHand var4);

    public void removePartFromSide(@Nullable Direction var1);

    public void markForUpdate();

    public DimensionalBlockPos getLocation();

    public BlockEntity getBlockEntity();

    public AEColor getColor();

    public void clearContainer();

    public boolean isBlocked(Direction var1);

    public SelectedPart selectPartLocal(Vec3 var1);

    public VoxelShape getCollisionShape(CollisionContext var1);

    public boolean removePart(IPart var1);

    default public SelectedPart selectPartWorld(Vec3 pos) {
        DimensionalBlockPos worldPos = this.getLocation();
        return this.selectPartLocal(pos.subtract((double)worldPos.getPos().getX(), (double)worldPos.getPos().getY(), (double)worldPos.getPos().getZ()));
    }

    public void markForSave();

    public void partChanged();

    public boolean hasRedstone();

    public boolean isEmpty();

    public void cleanup();

    public void notifyNeighbors();

    public void notifyNeighborNow(Direction var1);

    public boolean isInWorld();
}

