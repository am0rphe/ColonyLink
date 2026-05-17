/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.MoverType
 *  net.minecraft.world.entity.item.ItemEntity
 *  net.minecraft.world.entity.item.PrimedTnt
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Explosion
 *  net.minecraft.world.level.Explosion$BlockInteraction
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.LevelAccessor
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.AABB
 *  net.neoforged.neoforge.entity.IEntityWithComplexSpawn
 *  net.neoforged.neoforge.event.EventHooks
 *  org.jetbrains.annotations.Nullable
 */
package appeng.entity;

import appeng.core.AEConfig;
import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEEntities;
import appeng.core.network.clientbound.MockExplosionPacket;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
import net.neoforged.neoforge.event.EventHooks;
import org.jetbrains.annotations.Nullable;

public final class TinyTNTPrimedEntity
extends PrimedTnt
implements IEntityWithComplexSpawn {
    private LivingEntity placedBy;

    public TinyTNTPrimedEntity(EntityType<? extends TinyTNTPrimedEntity> type, Level level) {
        super(type, level);
        this.blocksBuilding = true;
    }

    public TinyTNTPrimedEntity(Level level, double x, double y, double z, @Nullable LivingEntity igniter) {
        super((EntityType)AEEntities.TINY_TNT_PRIMED.get(), level);
        this.setPos(x, y, z);
        double d0 = level.random.nextDouble() * 6.2831854820251465;
        this.setDeltaMovement(-Math.sin(d0) * 0.02, 0.2f, -Math.cos(d0) * 0.02);
        this.setFuse(80);
        this.xo = x;
        this.yo = y;
        this.zo = z;
        this.placedBy = igniter;
    }

    @Nullable
    public LivingEntity getOwner() {
        return this.placedBy;
    }

    public void tick() {
        this.updateInWaterStateAndDoFluidPushing();
        this.xo = this.getX();
        this.yo = this.getY();
        this.zo = this.getZ();
        this.setDeltaMovement(this.getDeltaMovement().subtract(0.0, (double)0.04f, 0.0));
        this.move(MoverType.SELF, this.getDeltaMovement());
        this.setDeltaMovement(this.getDeltaMovement().multiply((double)0.98f, (double)0.98f, (double)0.98f));
        if (this.onGround()) {
            this.setDeltaMovement(this.getDeltaMovement().multiply((double)0.7f, (double)0.7f, -0.5));
        }
        if (this.isInWater() && !this.level().isClientSide()) {
            ItemStack tntStack = AEBlocks.TINY_TNT.stack();
            ItemEntity item = new ItemEntity(this.level(), this.getX(), this.getY(), this.getZ(), tntStack);
            item.setDeltaMovement(this.getDeltaMovement());
            item.xo = this.xo;
            item.yo = this.yo;
            item.zo = this.zo;
            this.level().addFreshEntity((Entity)item);
            this.discard();
        }
        if (this.getFuse() <= 0) {
            this.discard();
            if (!this.level().isClientSide) {
                this.explode();
            }
        } else {
            this.level().addParticle((ParticleOptions)ParticleTypes.SMOKE, this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
        }
        this.setFuse(this.getFuse() - 1);
    }

    protected void explode() {
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), (SoundEvent)SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 4.0f, (1.0f + (this.level().random.nextFloat() - this.level().random.nextFloat()) * 0.2f) * 32.9f);
        if (this.isInWater()) {
            return;
        }
        Explosion ex = new Explosion(this.level(), (Entity)this, this.getX(), this.getY(), this.getZ(), 0.2f, false, AEConfig.instance().isTinyTntBlockDamageEnabled() ? Explosion.BlockInteraction.DESTROY_WITH_DECAY : Explosion.BlockInteraction.KEEP);
        AABB area = new AABB(this.getX() - 1.5, this.getY() - 1.5, this.getZ() - 1.5, this.getX() + 1.5, this.getY() + 1.5, this.getZ() + 1.5);
        List list = this.level().getEntities((Entity)this, area);
        EventHooks.onExplosionDetonate((Level)this.level(), (Explosion)ex, (List)list, (double)0.4f);
        for (Entity e : list) {
            e.hurt(this.level().damageSources().explosion(ex), 6.0f);
        }
        if (AEConfig.instance().isTinyTntBlockDamageEnabled()) {
            this.setPos(this.getX(), this.getY() - 0.25, this.getZ());
            int x = (int)(this.getX() - 2.0);
            while ((double)x <= this.getX() + 2.0) {
                int y = (int)(this.getY() - 2.0);
                while ((double)y <= this.getY() + 2.0) {
                    int z = (int)(this.getZ() - 2.0);
                    while ((double)z <= this.getZ() + 2.0) {
                        BlockPos point = new BlockPos(x, y, z);
                        BlockState state = this.level().getBlockState(point);
                        Block block = state.getBlock();
                        if (!state.isAir()) {
                            float strength = (float)((double)2.3f - (((double)((float)x + 0.5f) - this.getX()) * ((double)((float)x + 0.5f) - this.getX()) + ((double)((float)y + 0.5f) - this.getY()) * ((double)((float)y + 0.5f) - this.getY()) + ((double)((float)z + 0.5f) - this.getZ()) * ((double)((float)z + 0.5f) - this.getZ())));
                            float fluidResistance = !state.getFluidState().isEmpty() ? state.getFluidState().getExplosionResistance() : 0.0f;
                            float resistance = Math.max(block.getExplosionResistance(state, (BlockGetter)this.level(), point, ex), fluidResistance);
                            if ((double)(strength -= (resistance + 0.3f) * 0.11f) > 0.01 && !state.isAir()) {
                                if (state.canDropFromExplosion((BlockGetter)this.level(), point, ex)) {
                                    Block.dropResources((BlockState)state, (LevelAccessor)this.level(), (BlockPos)point, (BlockEntity)this.level().getBlockEntity(point));
                                }
                                this.level().setBlock(point, Blocks.AIR.defaultBlockState(), 3);
                                state.onBlockExploded(this.level(), point, ex);
                            }
                        }
                        ++z;
                    }
                    ++y;
                }
                ++x;
            }
        }
        AppEng.instance().sendToAllNearExcept(null, this.getX(), this.getY(), this.getZ(), 64.0, this.level(), new MockExplosionPacket(this.getX(), this.getY(), this.getZ()));
    }

    public void writeSpawnData(RegistryFriendlyByteBuf buffer) {
        buffer.writeByte(this.getFuse());
    }

    public void readSpawnData(RegistryFriendlyByteBuf additionalData) {
        this.setFuse(additionalData.readByte());
    }
}

