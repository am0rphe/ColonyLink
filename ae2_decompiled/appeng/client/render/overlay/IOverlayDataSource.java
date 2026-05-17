/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.level.ChunkPos
 *  net.minecraft.world.level.block.entity.BlockEntity
 */
package appeng.client.render.overlay;

import appeng.api.util.DimensionalBlockPos;
import java.util.Set;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface IOverlayDataSource {
    public Set<ChunkPos> getOverlayChunks();

    public BlockEntity getOverlayBlockEntity();

    public DimensionalBlockPos getOverlaySourceLocation();

    public int getOverlayColor();
}

