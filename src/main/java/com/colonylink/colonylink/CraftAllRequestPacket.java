package com.colonylink.colonylink;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public record CraftAllRequestPacket(List<ItemStack> stacks, List<Integer> realCounts) implements CustomPacketPayload
{
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(ColonyLink.MODID, "craft_all_request");
    public static final CustomPacketPayload.Type<CraftAllRequestPacket> TYPE = new CustomPacketPayload.Type<>(ID);

    /**
     * v1.6.2 — hard cap on the entry count decoded from the wire. The GUI list is
     * itself bounded by max_resources_displayed (max 500); anything above this can
     * only be a malformed/malicious packet trying to force a huge allocation.
     */
    private static final int MAX_ENTRIES = 512;

    public static final StreamCodec<RegistryFriendlyByteBuf, CraftAllRequestPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeInt(packet.stacks().size());
                for (int i = 0; i < packet.stacks().size(); i++)
                {
                    ItemStack.STREAM_CODEC.encode(buf, packet.stacks().get(i));
                    buf.writeInt(packet.realCounts().get(i));
                }
            },
            buf -> {
                int size = buf.readInt();
                // v1.6.2 — reject an out-of-range count instead of allocating a huge list.
                if (size < 0 || size > MAX_ENTRIES)
                    throw new io.netty.handler.codec.DecoderException(
                            "CraftAllRequestPacket size out of range: " + size);
                List<ItemStack> stacks = new ArrayList<>();
                List<Integer> counts = new ArrayList<>();
                for (int i = 0; i < size; i++)
                {
                    stacks.add(ItemStack.STREAM_CODEC.decode(buf));
                    counts.add(buf.readInt());
                }
                return new CraftAllRequestPacket(stacks, counts);
            }
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }

    public static void handle(CraftAllRequestPacket packet, IPayloadContext context)
    {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer)
                CraftHandler.handleCraftRequests(serverPlayer, packet.stacks(), packet.realCounts());
        });
    }
}