/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.codec.StreamCodec
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload$Type
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.level.ChunkPos
 *  net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package appeng.core.network.serverbound;

import appeng.core.network.CustomAppEngPayload;
import appeng.core.network.ServerboundPacket;
import appeng.core.network.clientbound.CompassResponsePacket;
import appeng.server.services.compass.ServerCompassService;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record RequestClosestMeteoritePacket(ChunkPos pos) implements ServerboundPacket
{
    private static final Logger LOG = LoggerFactory.getLogger(RequestClosestMeteoritePacket.class);
    public static final StreamCodec<RegistryFriendlyByteBuf, RequestClosestMeteoritePacket> STREAM_CODEC = StreamCodec.composite((StreamCodec)NeoForgeStreamCodecs.CHUNK_POS, RequestClosestMeteoritePacket::pos, RequestClosestMeteoritePacket::new);
    public static final CustomPacketPayload.Type<RequestClosestMeteoritePacket> TYPE = CustomAppEngPayload.createType("compass_request");

    public CustomPacketPayload.Type<RequestClosestMeteoritePacket> type() {
        return TYPE;
    }

    @Override
    public void handleOnServer(ServerPlayer player) {
        Optional<BlockPos> result = ServerCompassService.getClosestMeteorite(player.serverLevel(), this.pos);
        LOG.trace("{} requested closest meteorite for {} in {} -> {}", new Object[]{player, this.pos, player.serverLevel(), result});
        player.connection.send((CustomPacketPayload)new CompassResponsePacket(this.pos, result));
    }
}

