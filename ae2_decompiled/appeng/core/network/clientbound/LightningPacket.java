/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.codec.StreamCodec
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload$Type
 *  net.minecraft.world.entity.player.Player
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.OnlyIn
 */
package appeng.core.network.clientbound;

import appeng.client.render.effects.ParticleTypes;
import appeng.core.AEConfig;
import appeng.core.network.ClientboundPacket;
import appeng.core.network.CustomAppEngPayload;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public record LightningPacket(double x, double y, double z) implements ClientboundPacket
{
    public static final StreamCodec<RegistryFriendlyByteBuf, LightningPacket> STREAM_CODEC = StreamCodec.ofMember(LightningPacket::write, LightningPacket::decode);
    public static final CustomPacketPayload.Type<LightningPacket> TYPE = CustomAppEngPayload.createType("lightning");

    public CustomPacketPayload.Type<LightningPacket> type() {
        return TYPE;
    }

    public static LightningPacket decode(RegistryFriendlyByteBuf stream) {
        float x = stream.readFloat();
        float y = stream.readFloat();
        float z = stream.readFloat();
        return new LightningPacket(x, y, z);
    }

    public void write(RegistryFriendlyByteBuf data) {
        data.writeFloat((float)this.x);
        data.writeFloat((float)this.y);
        data.writeFloat((float)this.z);
    }

    @Override
    @OnlyIn(value=Dist.CLIENT)
    public void handleOnClient(Player player) {
        try {
            if (AEConfig.instance().isEnableEffects()) {
                player.getCommandSenderWorld().addParticle((ParticleOptions)ParticleTypes.LIGHTNING, this.x, this.y, this.z, 0.0, 0.0, 0.0);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }
}

