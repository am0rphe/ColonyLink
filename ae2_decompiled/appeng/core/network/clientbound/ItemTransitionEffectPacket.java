/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.core.Direction
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.codec.StreamCodec
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload$Type
 *  net.minecraft.world.entity.player.Player
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.OnlyIn
 */
package appeng.core.network.clientbound;

import appeng.client.render.effects.EnergyParticleData;
import appeng.core.AppEngClient;
import appeng.core.network.ClientboundPacket;
import appeng.core.network.CustomAppEngPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public record ItemTransitionEffectPacket(double x, double y, double z, Direction d) implements ClientboundPacket
{
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemTransitionEffectPacket> STREAM_CODEC = StreamCodec.ofMember(ItemTransitionEffectPacket::write, ItemTransitionEffectPacket::decode);
    public static final CustomPacketPayload.Type<ItemTransitionEffectPacket> TYPE = CustomAppEngPayload.createType("item_transition_effect");

    public CustomPacketPayload.Type<ItemTransitionEffectPacket> type() {
        return TYPE;
    }

    public static ItemTransitionEffectPacket decode(RegistryFriendlyByteBuf stream) {
        float x = stream.readFloat();
        float y = stream.readFloat();
        float z = stream.readFloat();
        Direction d = (Direction)stream.readEnum(Direction.class);
        return new ItemTransitionEffectPacket(x, y, z, d);
    }

    public void write(RegistryFriendlyByteBuf data) {
        data.writeFloat((float)this.x);
        data.writeFloat((float)this.y);
        data.writeFloat((float)this.z);
        data.writeEnum((Enum)this.d);
    }

    @Override
    @OnlyIn(value=Dist.CLIENT)
    public void handleOnClient(Player player) {
        EnergyParticleData data = new EnergyParticleData(true, this.d);
        for (int zz = 0; zz < 8; ++zz) {
            if (!AppEngClient.instance().shouldAddParticles(player.level().getRandom())) continue;
            double x = this.x + (double)player.level().getRandom().nextFloat() * 0.5 - 0.25;
            double y = this.y + (double)player.level().getRandom().nextFloat() * 0.5 - 0.25;
            double z = this.z + (double)player.level().getRandom().nextFloat() * 0.5 - 0.25;
            double speedX = 0.1f * (float)this.d.getStepX();
            double speedY = 0.1f * (float)this.d.getStepY();
            double speedZ = 0.1f * (float)this.d.getStepZ();
            Minecraft.getInstance().particleEngine.createParticle((ParticleOptions)data, x, y, z, speedX, speedY, speedZ);
        }
    }
}

