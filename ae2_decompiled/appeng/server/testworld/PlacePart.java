/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.levelgen.structure.BoundingBox
 *  org.jetbrains.annotations.Nullable
 */
package appeng.server.testworld;

import appeng.api.parts.IPartItem;
import appeng.api.parts.PartHelper;
import appeng.server.testworld.BlockPlacingBuildAction;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.jetbrains.annotations.Nullable;

record PlacePart(BoundingBox bb, IPartItem<?> what, @Nullable Direction side) implements BlockPlacingBuildAction
{
    @Override
    public BoundingBox getBoundingBox() {
        return this.bb;
    }

    @Override
    public void placeBlock(ServerLevel level, Player player, BlockPos pos, BlockPos minPos, BlockPos maxPos) {
        Direction actualSide = Objects.requireNonNullElse(this.side, Direction.UP);
        PartHelper.setPart(level, pos, actualSide, player, this.what);
    }
}

