/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.resources.sounds.SimpleSoundInstance
 *  net.minecraft.client.resources.sounds.SoundInstance
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.codec.StreamCodec
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload$Type
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.tags.FluidTags
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.SoundType
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.material.Fluid
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.OnlyIn
 *  net.neoforged.neoforge.common.SoundActions
 *  net.neoforged.neoforge.registries.GameData
 */
package appeng.core.network.clientbound;

import appeng.client.render.effects.EnergyParticleData;
import appeng.core.AELog;
import appeng.core.AppEngClient;
import appeng.core.network.ClientboundPacket;
import appeng.core.network.CustomAppEngPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.registries.GameData;

public record BlockTransitionEffectPacket(BlockPos pos, BlockState blockState, Direction direction, SoundMode soundMode) implements ClientboundPacket
{
    public static final StreamCodec<RegistryFriendlyByteBuf, BlockTransitionEffectPacket> STREAM_CODEC = StreamCodec.ofMember(BlockTransitionEffectPacket::write, BlockTransitionEffectPacket::decode);
    public static final CustomPacketPayload.Type<BlockTransitionEffectPacket> TYPE = CustomAppEngPayload.createType("block_transition_effect");

    public CustomPacketPayload.Type<BlockTransitionEffectPacket> type() {
        return TYPE;
    }

    public void write(RegistryFriendlyByteBuf data) {
        data.writeBlockPos(this.pos);
        int blockStateId = GameData.getBlockStateIDMap().getId((Object)this.blockState);
        if (blockStateId == -1) {
            AELog.warn("Failed to find numeric id for block state %s", this.blockState);
        }
        data.writeInt(blockStateId);
        data.writeEnum((Enum)this.direction);
        data.writeEnum((Enum)this.soundMode);
    }

    public static BlockTransitionEffectPacket decode(RegistryFriendlyByteBuf data) {
        BlockPos pos = data.readBlockPos();
        int blockStateId = data.readInt();
        BlockState blockState = (BlockState)GameData.getBlockStateIDMap().byId(blockStateId);
        if (blockState == null) {
            AELog.warn("Received invalid blockstate id %d from server", blockStateId);
            blockState = Blocks.AIR.defaultBlockState();
        }
        Direction direction = (Direction)data.readEnum(Direction.class);
        SoundMode soundMode = (SoundMode)data.readEnum(SoundMode.class);
        return new BlockTransitionEffectPacket(pos, blockState, direction, soundMode);
    }

    @Override
    @OnlyIn(value=Dist.CLIENT)
    public void handleOnClient(Player player) {
        this.spawnParticles(player.level());
        this.playBreakOrPickupSound();
    }

    @OnlyIn(value=Dist.CLIENT)
    private void spawnParticles(Level level) {
        EnergyParticleData data = new EnergyParticleData(false, this.direction);
        for (int zz = 0; zz < 32; ++zz) {
            if (!AppEngClient.instance().shouldAddParticles(level.getRandom())) continue;
            double x = (float)this.pos.getX() + level.getRandom().nextFloat();
            double y = (float)this.pos.getY() + level.getRandom().nextFloat();
            double z = (float)this.pos.getZ() + level.getRandom().nextFloat();
            double speedX = 0.1f * (float)this.direction.getStepX();
            double speedY = 0.1f * (float)this.direction.getStepY();
            double speedZ = 0.1f * (float)this.direction.getStepZ();
            Minecraft.getInstance().particleEngine.createParticle((ParticleOptions)data, x, y, z, speedX, speedY, speedZ);
        }
    }

    @OnlyIn(value=Dist.CLIENT)
    private void playBreakOrPickupSound() {
        float pitch;
        float volume;
        SoundEvent soundEvent;
        if (this.soundMode == SoundMode.FLUID) {
            Fluid fluid = this.blockState.getFluidState().getType();
            soundEvent = fluid.getFluidType().getSound(SoundActions.BUCKET_FILL);
            if (soundEvent == null) {
                soundEvent = fluid.is(FluidTags.LAVA) ? SoundEvents.BUCKET_FILL_LAVA : SoundEvents.BUCKET_FILL;
            }
            volume = 1.0f;
            pitch = 1.0f;
        } else if (this.soundMode == SoundMode.BLOCK) {
            SoundType soundType = this.blockState.getSoundType();
            soundEvent = soundType.getBreakSound();
            volume = soundType.volume;
            pitch = soundType.pitch;
        } else {
            return;
        }
        SimpleSoundInstance sound = new SimpleSoundInstance(soundEvent, SoundSource.BLOCKS, (volume + 1.0f) / 2.0f, pitch * 0.8f, SoundInstance.createUnseededRandom(), this.pos);
        Minecraft.getInstance().getSoundManager().play((SoundInstance)sound);
    }

    public static enum SoundMode {
        BLOCK,
        FLUID,
        NONE;

    }
}

