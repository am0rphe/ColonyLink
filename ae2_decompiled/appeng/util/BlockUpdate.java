/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Blocks
 */
package appeng.util;

import appeng.util.ILevelRunnable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class BlockUpdate
implements ILevelRunnable {
    private final BlockPos pos;

    BlockUpdate(BlockPos pos) {
        this.pos = pos;
    }

    @Override
    public void call(Level level) throws Exception {
        if (level.hasChunkAt(this.pos)) {
            level.updateNeighborsAt(this.pos, Blocks.AIR);
        }
    }
}

