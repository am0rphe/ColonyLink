/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Holder
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.tags.FluidTags
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.LevelAccessor
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.LiquidBlockContainer
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.gameevent.GameEvent
 *  net.minecraft.world.level.gameevent.GameEvent$Context
 *  net.minecraft.world.level.material.FlowingFluid
 *  net.minecraft.world.level.material.Fluid
 *  net.minecraft.world.level.material.Fluids
 *  org.jetbrains.annotations.Nullable
 */
package appeng.parts.automation;

import appeng.api.behaviors.PlacementStrategy;
import appeng.api.config.Actionable;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEKey;
import appeng.util.Platform;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

public class FluidPlacementStrategy
implements PlacementStrategy {
    private final ServerLevel level;
    private final BlockPos pos;
    private final Direction side;
    @Nullable
    private final UUID owningPlayerId;
    private final Set<Fluid> blocked = new HashSet<Fluid>();
    private long lastEffect;

    public FluidPlacementStrategy(ServerLevel level, BlockPos pos, Direction side, BlockEntity host, @Nullable UUID owningPlayerId) {
        this.level = level;
        this.pos = pos;
        this.side = side;
        this.owningPlayerId = owningPlayerId;
    }

    @Override
    public void clearBlocked() {
        this.blocked.clear();
    }

    /*
     * Enabled aggressive block sorting
     */
    @Override
    public long placeInWorld(AEKey f, long amount, Actionable type, boolean placeAsEntity) {
        if (placeAsEntity) return 0L;
        if (!(f instanceof AEFluidKey)) return 0L;
        AEFluidKey fluidKey = (AEFluidKey)f;
        if (amount < 1000L) {
            return 0L;
        }
        Fluid fluid = fluidKey.getFluid();
        if (this.blocked.contains(fluid)) {
            return 0L;
        }
        if (!fluidKey.toStack(1).getComponentsPatch().isEmpty()) {
            return 0L;
        }
        BlockState state = this.level.getBlockState(this.pos);
        if (!this.canPlace(this.level, state, this.pos, fluid)) {
            this.blocked.add(fluid);
            return 0L;
        }
        if (type != Actionable.MODULATE) return 1000L;
        if (this.level.dimensionType().ultraWarm() && fluid.is(FluidTags.WATER)) {
            this.playEvaporationEffect((Level)this.level, this.pos);
            return 1000L;
        }
        Block block = state.getBlock();
        if (block instanceof LiquidBlockContainer) {
            LiquidBlockContainer liquidBlockContainer = (LiquidBlockContainer)block;
            if (fluid == Fluids.WATER) {
                liquidBlockContainer.placeLiquid((LevelAccessor)this.level, this.pos, state, ((FlowingFluid)fluid).getSource(false));
                this.playEmptySound((Level)this.level, this.pos, fluid);
                return 1000L;
            }
        }
        if (state.canBeReplaced(fluid) && !state.liquid()) {
            this.level.destroyBlock(this.pos, true);
        }
        if (!this.level.setBlock(this.pos, fluid.defaultFluidState().createLegacyBlock(), 11) && !state.getFluidState().isSource()) {
            return 0L;
        }
        this.playEmptySound((Level)this.level, this.pos, fluid);
        return 1000L;
    }

    private void playEmptySound(Level level, BlockPos pos, Fluid fluid) {
        if (this.throttleEffect()) {
            return;
        }
        SoundEvent soundEvent = fluid.is(FluidTags.LAVA) ? SoundEvents.BUCKET_EMPTY_LAVA : SoundEvents.BUCKET_EMPTY;
        level.playSound(null, pos, soundEvent, SoundSource.BLOCKS, 1.0f, 1.0f);
        level.gameEvent((Holder)GameEvent.FLUID_PLACE, pos, GameEvent.Context.of(null, null));
    }

    private void playEvaporationEffect(Level level, BlockPos pos) {
        if (this.throttleEffect()) {
            return;
        }
        level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5f, 2.6f + (level.random.nextFloat() - level.random.nextFloat()) * 0.8f);
        for (int l = 0; l < 8; ++l) {
            level.addParticle((ParticleOptions)ParticleTypes.LARGE_SMOKE, (double)pos.getX() + Math.random(), (double)pos.getY() + Math.random(), (double)pos.getZ() + Math.random(), 0.0, 0.0, 0.0);
        }
    }

    private boolean canPlace(ServerLevel level, BlockState state, BlockPos pos, Fluid fluid) {
        if (!(fluid instanceof FlowingFluid)) {
            return false;
        }
        if (state == fluid.defaultFluidState().createLegacyBlock()) {
            return false;
        }
        if (state.isAir()) {
            return true;
        }
        if (state.canBeReplaced(fluid)) {
            return true;
        }
        Block block = state.getBlock();
        if (block instanceof LiquidBlockContainer) {
            LiquidBlockContainer liquidBlockContainer = (LiquidBlockContainer)block;
            Player fakePlayer = Platform.getFakePlayer(level, this.owningPlayerId);
            return liquidBlockContainer.canPlaceLiquid(fakePlayer, (BlockGetter)level, pos, state, fluid);
        }
        return false;
    }

    protected final boolean throttleEffect() {
        long now = System.currentTimeMillis();
        if (now < this.lastEffect + 250L) {
            return true;
        }
        this.lastEffect = now;
        return false;
    }
}

