/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Vec3i
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.levelgen.structure.BoundingBox
 */
package appeng.server.testworld;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.server.testworld.BuildAction;
import appeng.server.testworld.GridInitHelper;
import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public record PostGridInitAction(List<BlockPos> positions, BiConsumer<IGrid, IGridNode> consumer, boolean waitForActive) implements BuildAction
{
    @Override
    public void build(ServerLevel level, Player player, BlockPos origin) {
        List<BlockPos> absolutePositions = this.positions.stream().map(p -> p.offset((Vec3i)origin)).toList();
        GridInitHelper.doAfterGridInit(level, absolutePositions, this.waitForActive, this.consumer);
    }

    @Override
    public BoundingBox getBoundingBox() {
        return new BoundingBox(this.positions.getFirst());
    }
}

