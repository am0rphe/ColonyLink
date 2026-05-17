/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.levelgen.structure.BoundingBox
 *  org.jetbrains.annotations.Nullable
 */
package appeng.server.testworld;

import appeng.api.parts.IPartHost;
import appeng.api.parts.PartHelper;
import appeng.core.definitions.AEItems;
import appeng.facade.FacadePart;
import appeng.server.testworld.BlockPlacingBuildAction;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.jetbrains.annotations.Nullable;

record PlaceFacade(BoundingBox bb, ItemStack visual, @Nullable Direction side) implements BlockPlacingBuildAction
{
    @Override
    public BoundingBox getBoundingBox() {
        return this.bb;
    }

    @Override
    public void placeBlock(ServerLevel level, Player player, BlockPos pos, BlockPos minPos, BlockPos maxPos) {
        Direction actualSide = Objects.requireNonNullElse(this.side, Direction.UP);
        IPartHost partHost = PartHelper.getPartHost((Level)level, pos);
        ItemStack facadeItem = AEItems.FACADE.get().createFacadeForItemUnchecked(this.visual);
        FacadePart facadePart = AEItems.FACADE.get().createPartFromItemStack(facadeItem, actualSide);
        partHost.getFacadeContainer().addFacade(facadePart);
    }
}

