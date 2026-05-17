/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.levelgen.structure.BoundingBox
 */
package appeng.server.testworld;

import appeng.api.parts.IPart;
import appeng.api.parts.PartHelper;
import appeng.core.definitions.ItemDefinition;
import appeng.items.parts.PartItem;
import appeng.server.testworld.BlockPlacingBuildAction;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public record PartCustomizer<T extends IPart>(BoundingBox bb, Direction side, ItemDefinition<? extends PartItem<T>> part, Consumer<T> partCustomizer) implements BlockPlacingBuildAction
{
    @Override
    public void placeBlock(ServerLevel level, Player player, BlockPos pos, BlockPos minPos, BlockPos maxPos) {
        Object placedPart = PartHelper.getPart(this.part.get(), (BlockGetter)level, pos, this.side);
        if (placedPart != null) {
            this.partCustomizer.accept(placedPart);
        }
    }

    @Override
    public BoundingBox getBoundingBox() {
        return this.bb;
    }
}

