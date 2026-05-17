/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
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
import appeng.core.network.ClientboundPacket;
import appeng.core.network.CustomAppEngPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public record MatterCannonPacket(double x, double y, double z, double dx, double dy, double dz, byte len) implements ClientboundPacket
{
    public static final StreamCodec<RegistryFriendlyByteBuf, MatterCannonPacket> STREAM_CODEC = StreamCodec.ofMember(MatterCannonPacket::write, MatterCannonPacket::decode);
    public static final CustomPacketPayload.Type<MatterCannonPacket> TYPE = CustomAppEngPayload.createType("matter_cannon");

    public MatterCannonPacket(double x, double y, double z, double dx, double dy, double dz, byte len) {
        double dl = dx * dx + dy * dy + dz * dz;
        float dlz = (float)Math.sqrt(dl);
        this.x = x;
        this.y = y;
        this.z = z;
        this.dx = dx / (double)dlz;
        this.dy = dy / (double)dlz;
        this.dz = dz / (double)dlz;
        this.len = len;
    }

    public CustomPacketPayload.Type<MatterCannonPacket> type() {
        return TYPE;
    }

    public static MatterCannonPacket decode(RegistryFriendlyByteBuf stream) {
        float x = stream.readFloat();
        float y = stream.readFloat();
        float z = stream.readFloat();
        float dx = stream.readFloat();
        float dy = stream.readFloat();
        float dz = stream.readFloat();
        byte len = stream.readByte();
        return new MatterCannonPacket(x, y, z, dx, dy, dz, len);
    }

    public void write(RegistryFriendlyByteBuf data) {
        data.writeFloat((float)this.x);
        data.writeFloat((float)this.y);
        data.writeFloat((float)this.z);
        data.writeFloat((float)this.dx);
        data.writeFloat((float)this.dy);
        data.writeFloat((float)this.dz);
        data.writeByte(this.len);
    }

    @Override
    @OnlyIn(value=Dist.CLIENT)
    public void handleOnClient(Player player) {
        try {
            for (int a = 1; a < this.len; ++a) {
                Minecraft.getInstance().particleEngine.createParticle((ParticleOptions)ParticleTypes.MATTER_CANNON, this.x + this.dx * (double)a, this.y + this.dy * (double)a, this.z + this.dz * (double)a, 0.0, 0.0, 0.0);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }
}

