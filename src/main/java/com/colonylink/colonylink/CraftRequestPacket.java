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
            buf -> new CraftRequestPacket(
                    ItemStack.STREAM_CODEC.decode(buf),
                    buf.readInt(),
                    buf.readBoolean(),
                    buf.readBlockPos(),
                    ResourceStatus.values()[buf.readInt()]
            )
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
                    DomumCraftHandler.handleDomumCraft(
                            serverPlayer, packet.stack(), packet.realCount(), packet.redirectorPos());
            }
            else
            {
                CraftHandler.handleCraftRequest(serverPlayer, packet.stack(), packet.realCount());
            }
        });
    }
}