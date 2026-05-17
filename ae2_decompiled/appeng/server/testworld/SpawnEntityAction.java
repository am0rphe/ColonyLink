/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.MobSpawnType
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.levelgen.structure.BoundingBox
 */
package appeng.server.testworld;

import appeng.server.testworld.BuildAction;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public record SpawnEntityAction(BoundingBox bb, EntityType<?> type, Consumer<Entity> postProcessor) implements BuildAction
{
    @Override
    public BoundingBox getBoundingBox() {
        return this.bb;
    }

    @Override
    public void spawnEntities(ServerLevel level, BlockPos origin, List<Entity> entities) {
        BoundingBox actualBox = this.getBoundingBox().moved(origin.getX(), origin.getY(), origin.getZ());
        BlockPos.betweenClosedStream((BoundingBox)actualBox).forEach(pos -> {
            Entity entity = this.type.spawn(level, (ItemStack)null, null, pos, MobSpawnType.COMMAND, true, true);
            if (entity != null) {
                this.postProcessor.accept(entity);
                entities.add(entity);
            }
        });
    }
}

