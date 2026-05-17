/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Vec3i
 */
package appeng.server.testplots;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;

public record PosAndSide(BlockPos pos, Direction side) {
    public static PosAndSide up(BlockPos pos) {
        return new PosAndSide(pos, Direction.UP);
    }

    public static PosAndSide down(BlockPos pos) {
        return new PosAndSide(pos, Direction.DOWN);
    }

    public static PosAndSide north(BlockPos pos) {
        return new PosAndSide(pos, Direction.NORTH);
    }

    public static PosAndSide south(BlockPos pos) {
        return new PosAndSide(pos, Direction.SOUTH);
    }

    public static PosAndSide east(BlockPos pos) {
        return new PosAndSide(pos, Direction.EAST);
    }

    public static PosAndSide west(BlockPos pos) {
        return new PosAndSide(pos, Direction.WEST);
    }

    public PosAndSide offset(Vec3i offset) {
        return new PosAndSide(this.pos.offset(offset), this.side);
    }
}

