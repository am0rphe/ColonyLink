/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  org.jetbrains.annotations.Nullable
 */
package appeng.api.implementations.blockentities;

import appeng.api.AECapabilities;
import appeng.api.crafting.IPatternDetails;
import appeng.api.implementations.blockentities.PatternContainerGroup;
import appeng.api.stacks.KeyCounter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public interface ICraftingMachine {
    @Nullable
    public static ICraftingMachine of(@Nullable BlockEntity blockEntity, Direction side) {
        if (blockEntity == null || blockEntity.getLevel() == null) {
            return null;
        }
        return (ICraftingMachine)blockEntity.getLevel().getCapability(AECapabilities.CRAFTING_MACHINE, blockEntity.getBlockPos(), blockEntity.getBlockState(), blockEntity, (Object)side);
    }

    @Nullable
    public static ICraftingMachine of(Level level, BlockPos pos, Direction side) {
        return (ICraftingMachine)level.getCapability(AECapabilities.CRAFTING_MACHINE, pos, (Object)side);
    }

    public PatternContainerGroup getCraftingMachineInfo();

    public boolean pushPattern(IPatternDetails var1, KeyCounter[] var2, Direction var3);

    public boolean acceptsPlans();
}

