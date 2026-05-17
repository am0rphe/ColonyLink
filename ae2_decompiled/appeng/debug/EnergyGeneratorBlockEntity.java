/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.math.IntMath
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockState
 *  net.neoforged.neoforge.capabilities.Capabilities$EnergyStorage
 *  net.neoforged.neoforge.energy.IEnergyStorage
 */
package appeng.debug;

import appeng.blockentity.AEBaseBlockEntity;
import appeng.blockentity.ServerTickingBlockEntity;
import com.google.common.math.IntMath;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

public class EnergyGeneratorBlockEntity
extends AEBaseBlockEntity
implements ServerTickingBlockEntity,
IEnergyStorage {
    private int generationRate = 8;

    public EnergyGeneratorBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
    }

    @Override
    public void serverTick() {
        Level level = this.getLevel();
        int tier = 1;
        for (Direction facing : Direction.values()) {
            BlockEntity te = level.getBlockEntity(this.getBlockPos().relative(facing));
            if (!(te instanceof EnergyGeneratorBlockEntity)) continue;
            ++tier;
        }
        int energyToInsert = IntMath.pow((int)this.generationRate, (int)tier);
        for (Direction facing : Direction.values()) {
            IEnergyStorage consumer = (IEnergyStorage)this.getLevel().getCapability(Capabilities.EnergyStorage.BLOCK, this.getBlockPos().relative(facing), (Object)facing.getOpposite());
            if (consumer == null || !consumer.canReceive()) continue;
            consumer.receiveEnergy(energyToInsert, false);
        }
    }

    public int getGenerationRate() {
        return this.generationRate;
    }

    public void setGenerationRate(int generationRate) {
        this.generationRate = generationRate;
    }

    @Override
    public void loadTag(CompoundTag data, HolderLookup.Provider registries) {
        super.loadTag(data, registries);
        if (data.contains("generationRate", 3)) {
            this.generationRate = data.getInt("generationRate");
        }
    }

    @Override
    public void saveAdditional(CompoundTag data, HolderLookup.Provider registries) {
        super.saveAdditional(data, registries);
        data.putInt("generationRate", this.generationRate);
    }

    public int receiveEnergy(int maxReceive, boolean simulate) {
        return 0;
    }

    public int extractEnergy(int maxExtract, boolean simulate) {
        return maxExtract;
    }

    public int getEnergyStored() {
        return Integer.MAX_VALUE;
    }

    public int getMaxEnergyStored() {
        return Integer.MAX_VALUE;
    }

    public boolean canExtract() {
        return true;
    }

    public boolean canReceive() {
        return false;
    }
}

