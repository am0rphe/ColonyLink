package com.colonylink.colonylink;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record CraftRequestPacket(
        ItemStack stack,
        int realCount,
        boolean isDomum,
        BlockPos redirectorPos,
        ResourceStatus domumAction
) implements CustomPacketPayload
{
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(ColonyLink.MODID, "craft_request");
    public static final CustomPacketPayload.Type<CraftRequestPacket> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, CraftRequestPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
                ItemStack.STREAM_CODEC.encode(buf, packet.stack());
                buf.writeInt(packet.realCount());
                buf.writeBoolean(packet.isDomum());
                buf.writeBlockPos(packet.redirectorPos());
                buf.writeInt(packet.domumAction().ordinal());
            },
            buf -> {
                ItemStack stack        = ItemStack.STREAM_CODEC.decode(buf);
                int realCount          = buf.readInt();
                boolean isDomum        = buf.readBoolean();
                BlockPos redirectorPos = buf.readBlockPos();
                // v1.6.2 — validate the enum ordinal: an out-of-range value can only
                // come from a modified client and would throw AIOOBE. Reject the
                // malformed packet cleanly (connection closed) instead.
                int ord = buf.readInt();
                if (ord < 0 || ord >= ResourceStatus.values().length)
                    throw new io.netty.handler.codec.DecoderException(
                            "Invalid ResourceStatus ordinal: " + ord);
                return new CraftRequestPacket(stack, realCount, isDomum, redirectorPos,
                        ResourceStatus.values()[ord]);
            }
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(CraftRequestPacket packet, IPayloadContext context)
    {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) return;

            if (packet.isDomum())
            {
                if (packet.domumAction() == ResourceStatus.MISSING)
                    DomumCraftHandler.handleMissingCraft(
                            serverPlayer, packet.stack(), packet.realCount());
                else
                    // v1.4.x : un Domum craftable est produit via AE2 (le Redirector expose
                    // le DomumPattern). Vrai job AE2 sur le bloc DO — fini l'insertion virtuelle
                    // dans le buffer (qui provoquait « Redirector buffer is full »).
                    CraftHandler.handleCraftRequest(serverPlayer, packet.stack(), packet.realCount());
            }
            else
            {
                CraftHandler.handleCraftRequest(serverPlayer, packet.stack(), packet.realCount());
            }
        });
    }
}