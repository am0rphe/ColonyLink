/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.levelgen.structure.BoundingBox
 */
package appeng.server.testworld;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public interface BuildAction {
    public BoundingBox getBoundingBox();

    default public void build(ServerLevel level, Player player, BlockPos origin) {
    }

    default public void spawnEntities(ServerLevel level, BlockPos origin, List<Entity> entities) {
    }
}

