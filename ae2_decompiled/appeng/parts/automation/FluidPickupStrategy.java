/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.enchantment.ItemEnchantments
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.LevelAccessor
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.BucketPickup
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.material.Fluid
 *  net.minecraft.world.level.material.FluidState
 *  net.minecraft.world.level.material.Fluids
 *  org.jetbrains.annotations.Nullable
 */
package appeng.parts.automation;

import appeng.api.behaviors.PickupSink;
import appeng.api.behaviors.PickupStrategy;
import appeng.api.config.Actionable;
import appeng.api.ids.AETags;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.core.AppEng;
import appeng.core.network.clientbound.BlockTransitionEffectPacket;
import appeng.util.GenericContainerHelper;
import appeng.util.Platform;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

public class FluidPickupStrategy
implements PickupStrategy {
    private final ServerLevel level;
    private final BlockPos pos;
    private final Direction side;
    @Nullable
    private final UUID owningPlayerId;
    private long lastEffect;

    public FluidPickupStrategy(ServerLevel level, BlockPos pos, Direction side, BlockEntity host, ItemEnchantments enchantments, @Nullable UUID owningPlayerId) {
        this.level = level;
        this.pos = pos;
        this.side = side;
        this.owningPlayerId = owningPlayerId;
    }

    @Override
    public void reset() {
    }

    @Override
    public boolean canPickUpEntity(Entity entity) {
        return false;
    }

    @Override
    public boolean pickUpEntity(IEnergySource energySource, PickupSink sink, Entity entity) {
        return false;
    }

    @Override
    public PickupStrategy.Result tryPickup(IEnergySource energySource, PickupSink sink) {
        BlockState blockstate = this.level.getBlockState(this.pos);
        Block block = blockstate.getBlock();
        if (block instanceof BucketPickup) {
            BucketPickup bucketPickup = (BucketPickup)block;
            FluidState fluidState = blockstate.getFluidState();
            Fluid fluid = fluidState.getType();
            if (this.isFluidBlacklisted(fluid)) {
                return PickupStrategy.Result.CANT_PICKUP;
            }
            if (fluid != Fluids.EMPTY && fluidState.isSource()) {
                AEFluidKey what = AEFluidKey.of(fluid);
                if (this.storeFluid(sink, what, 1000L, false)) {
                    AEKey aEKey;
                    Player fakePlayer = Platform.getFakePlayer(this.level, this.owningPlayerId);
                    ItemStack fluidContainer = bucketPickup.pickupBlock(fakePlayer, (LevelAccessor)this.level, this.pos, blockstate);
                    GenericStack pickedUpStack = GenericContainerHelper.getContainedFluidStack(fluidContainer);
                    if (pickedUpStack != null && (aEKey = pickedUpStack.what()) instanceof AEFluidKey) {
                        AEFluidKey fluidKey = (AEFluidKey)aEKey;
                        this.storeFluid(sink, fluidKey, pickedUpStack.amount(), true);
                    }
                    if (!this.throttleEffect()) {
                        AppEng.instance().sendToAllNearExcept(null, this.pos.getX(), this.pos.getY(), this.pos.getZ(), 64.0, (Level)this.level, new BlockTransitionEffectPacket(this.pos, blockstate, this.side, BlockTransitionEffectPacket.SoundMode.FLUID));
                    }
                    return PickupStrategy.Result.PICKED_UP;
                }
                return PickupStrategy.Result.CANT_STORE;
            }
        }
        return PickupStrategy.Result.CANT_PICKUP;
    }

    private boolean storeFluid(PickupSink sink, AEFluidKey what, long amount, boolean modulate) {
        return sink.insert(what, amount, modulate ? Actionable.MODULATE : Actionable.SIMULATE) >= amount;
    }

    private boolean isFluidBlacklisted(Fluid fluid) {
        return fluid.builtInRegistryHolder().is(AETags.ANNIHILATION_PLANE_FLUID_BLACKLIST);
    }

    private boolean throttleEffect() {
        long now = System.currentTimeMillis();
        if (now < this.lastEffect + 250L) {
            return true;
        }
        this.lastEffect = now;
        return false;
    }
}

