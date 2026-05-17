/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Vec3i
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.decoration.ItemFrame
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.levelgen.structure.BoundingBox
 */
package appeng.server.testworld;

import appeng.server.testworld.BuildAction;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public record PlaceItemFrameAction(BlockPos pos, Direction facing, Consumer<ItemFrame> customizer) implements BuildAction
{
    @Override
    public BoundingBox getBoundingBox() {
        return new BoundingBox(this.pos);
    }

    @Override
    public void spawnEntities(ServerLevel level, BlockPos origin, List<Entity> entities) {
        BlockPos actualPos = this.pos.offset((Vec3i)origin);
        ItemFrame itemFrame = new ItemFrame(EntityType.ITEM_FRAME, (Level)level, actualPos, this.facing);
        if (!level.addFreshEntity((Entity)itemFrame)) {
            return;
        }
        this.customizer.accept(itemFrame);
        entities.add((Entity)itemFrame);
    }
}

