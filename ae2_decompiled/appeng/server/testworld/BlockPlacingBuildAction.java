/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.levelgen.structure.BoundingBox
 */
package appeng.server.testworld;

import appeng.server.testworld.BuildAction;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public interface BlockPlacingBuildAction
extends BuildAction {
    @Override
    default public void build(ServerLevel level, Player player, BlockPos origin) {
        BoundingBox actualBox = this.getBoundingBox().moved(origin.getX(), origin.getY(), origin.getZ());
        BlockPos minPos = new BlockPos(actualBox.minX(), actualBox.minY(), actualBox.minZ());
        BlockPos maxPos = new BlockPos(actualBox.maxX(), actualBox.maxY(), actualBox.maxZ());
        BlockPos.betweenClosedStream((BoundingBox)actualBox).forEach(pos -> this.placeBlock(level, player, (BlockPos)pos, minPos, maxPos));
    }

    public void placeBlock(ServerLevel var1, Player var2, BlockPos var3, BlockPos var4, BlockPos var5);
}

