/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.LevelAccessor
 *  net.minecraft.world.level.block.entity.BlockEntity
 */
package appeng.api.util;

import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class DimensionalBlockPos {
    private final Level level;
    private final BlockPos pos;

    public DimensionalBlockPos(DimensionalBlockPos coordinate) {
        this(coordinate.getLevel(), coordinate.pos);
    }

    public DimensionalBlockPos(BlockEntity blockentity) {
        this(blockentity.getLevel(), blockentity.getBlockPos());
    }

    public DimensionalBlockPos(Level level, BlockPos pos) {
        this(level, pos.getX(), pos.getY(), pos.getZ());
    }

    public DimensionalBlockPos(Level level, int x, int y, int z) {
        this.level = Objects.requireNonNull(level, "level");
        this.pos = new BlockPos(x, y, z);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        DimensionalBlockPos that = (DimensionalBlockPos)o;
        return this.level.equals(that.level) && this.pos.equals((Object)that.pos);
    }

    public int hashCode() {
        return Objects.hash(this.level, this.pos);
    }

    public String toString() {
        return this.pos.getX() + "," + this.pos.getY() + "," + this.pos.getZ() + " in " + String.valueOf(this.getLevel().dimension().location());
    }

    public boolean isInWorld(LevelAccessor level) {
        return this.level == level;
    }

    public Level getLevel() {
        return this.level;
    }

    public BlockPos getPos() {
        return this.pos;
    }
}

