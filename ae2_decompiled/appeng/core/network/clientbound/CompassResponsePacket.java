/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.codec.ByteBufCodecs
 *  net.minecraft.network.codec.StreamCodec
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload$Type
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.ChunkPos
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.OnlyIn
 *  net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs
 */
package appeng.core.network.clientbound;

import appeng.core.network.ClientboundPacket;
import appeng.core.network.CustomAppEngPayload;
import appeng.hooks.CompassManager;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

public record CompassResponsePacket(ChunkPos requestedPos, Optional<BlockPos> closestMeteorite) implements ClientboundPacket
{
    public static final StreamCodec<RegistryFriendlyByteBuf, CompassResponsePacket> STREAM_CODEC = StreamCodec.composite((StreamCodec)NeoForgeStreamCodecs.CHUNK_POS, CompassResponsePacket::requestedPos, (StreamCodec)ByteBufCodecs.optional((StreamCodec)BlockPos.STREAM_CODEC), CompassResponsePacket::closestMeteorite, CompassResponsePacket::new);
    public static final CustomPacketPayload.Type<CompassResponsePacket> TYPE = CustomAppEngPayload.createType("compass_response");

    public CustomPacketPayload.Type<CompassResponsePacket> type() {
        return TYPE;
    }

    @Override
    @OnlyIn(value=Dist.CLIENT)
    public void handleOnClient(Player player) {
        CompassManager.INSTANCE.postResult(this.requestedPos, this.closestMeteorite.orElse(null));
    }
}

