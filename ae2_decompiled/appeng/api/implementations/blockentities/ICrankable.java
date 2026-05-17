/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.world.level.Level
 *  org.jetbrains.annotations.Nullable
 */
package appeng.api.implementations.blockentities;

import appeng.api.AECapabilities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public interface ICrankable {
    public boolean canTurn();

    public void applyTurn();

    @Nullable
    public static ICrankable get(Level level, BlockPos pos, Direction side) {
        return (ICrankable)level.getCapability(AECapabilities.CRANKABLE, pos, (Object)side);
    }
}

