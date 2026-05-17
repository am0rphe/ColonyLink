/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.codec.StreamCodec
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload$Type
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.OnlyIn
 */
package appeng.core.network.clientbound;

import appeng.core.network.ClientboundPacket;
import appeng.core.network.CustomAppEngPayload;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public record MockExplosionPacket(double x, double y, double z) implements ClientboundPacket
{
    public static final StreamCodec<RegistryFriendlyByteBuf, MockExplosionPacket> STREAM_CODEC = StreamCodec.ofMember(MockExplosionPacket::write, MockExplosionPacket::decode);
    public static final CustomPacketPayload.Type<MockExplosionPacket> TYPE = CustomAppEngPayload.createType("mock_explosion");

    public CustomPacketPayload.Type<MockExplosionPacket> type() {
        return TYPE;
    }

    public static MockExplosionPacket decode(RegistryFriendlyByteBuf data) {
        double x = data.readDouble();
        double y = data.readDouble();
        double z = data.readDouble();
        return new MockExplosionPacket(x, y, z);
    }

    public void write(RegistryFriendlyByteBuf data) {
        data.writeDouble(this.x);
        data.writeDouble(this.y);
        data.writeDouble(this.z);
    }

    @Override
    @OnlyIn(value=Dist.CLIENT)
    public void handleOnClient(Player player) {
        Level level = player.getCommandSenderWorld();
        level.addParticle((ParticleOptions)ParticleTypes.EXPLOSION, this.x, this.y, this.z, 1.0, 0.0, 0.0);
    }
}

