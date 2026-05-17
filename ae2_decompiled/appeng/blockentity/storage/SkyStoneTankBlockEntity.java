/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockState
 *  net.neoforged.neoforge.fluids.FluidUtil
 *  net.neoforged.neoforge.fluids.IFluidTank
 *  net.neoforged.neoforge.fluids.capability.IFluidHandler
 *  net.neoforged.neoforge.fluids.capability.templates.FluidTank
 */
package appeng.blockentity.storage;

import appeng.blockentity.AEBaseBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

public class SkyStoneTankBlockEntity
extends AEBaseBlockEntity {
    public static final int BUCKET_CAPACITY = 16;
    protected FluidTank tank = new FluidTank(16000){

        protected void onContentsChanged() {
            SkyStoneTankBlockEntity.this.markForUpdate();
            SkyStoneTankBlockEntity.this.setChanged();
        }
    };

    public SkyStoneTankBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
    }

    @Override
    public void saveAdditional(CompoundTag data, HolderLookup.Provider registries) {
        super.saveAdditional(data, registries);
        CompoundTag tankNbt = new CompoundTag();
        this.tank.writeToNBT(registries, tankNbt);
        if (!tankNbt.isEmpty()) {
            data.put("tank", (Tag)tankNbt);
        }
    }

    @Override
    public void loadTag(CompoundTag data, HolderLookup.Provider registries) {
        super.loadTag(data, registries);
        this.tank.readFromNBT(registries, data.getCompound("tank"));
    }

    public boolean onPlayerUse(Player player, InteractionHand hand) {
        return FluidUtil.interactWithFluidHandler((Player)player, (InteractionHand)hand, (IFluidHandler)this.tank);
    }

    public IFluidTank getTank() {
        return this.tank;
    }

    public IFluidHandler getFluidHandler() {
        return this.tank;
    }

    @Override
    protected boolean readFromStream(RegistryFriendlyByteBuf data) {
        boolean ret = super.readFromStream(data);
        this.tank.readFromNBT((HolderLookup.Provider)data.registryAccess(), data.readNbt());
        return ret;
    }

    @Override
    protected void writeToStream(RegistryFriendlyByteBuf data) {
        super.writeToStream(data);
        CompoundTag tag = new CompoundTag();
        this.tank.writeToNBT((HolderLookup.Provider)data.registryAccess(), tag);
        data.writeNbt((Tag)tag);
    }
}

