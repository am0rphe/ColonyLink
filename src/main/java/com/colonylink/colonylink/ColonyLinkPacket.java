package com.colonylink.colonylink;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public record ColonyLinkPacket(
        List<ResourceEntry> entries,
        BlockPos builderPos,
        String builderName,
        String buildingName,
        String workerStatus,
        int availableCpus,
        String redirectorState
) implements CustomPacketPayload
{
    public record ResourceEntry(
            ItemStack stack,
            ResourceStatus status,
            int realCount,
            boolean isDomum,
            BlockPos redirectorPos,
            List<String> tooltipLines
    ) {}

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(ColonyLink.MODID, "colony_link_packet");
    public static final CustomPacketPayload.Type<ColonyLinkPacket> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, ColonyLinkPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeInt(packet.entries().size());
                for (ResourceEntry entry : packet.entries())
                {
                    ItemStack.STREAM_CODEC.encode(buf, entry.stack());
                    buf.writeInt(entry.status().ordinal());
                    buf.writeInt(entry.realCount());
                    buf.writeBoolean(entry.isDomum());
                    buf.writeBlockPos(entry.redirectorPos());
                    buf.writeInt(entry.tooltipLines().size());
                    for (String line : entry.tooltipLines())
                        buf.writeUtf(line);
                }
                buf.writeBlockPos(packet.builderPos());
                buf.writeUtf(packet.builderName());
                buf.writeUtf(packet.buildingName());
                buf.writeUtf(packet.workerStatus());
                buf.writeInt(packet.availableCpus());
                buf.writeUtf(packet.redirectorState());
            },
            buf -> {
                int size = buf.readInt();
                List<ResourceEntry> list = new ArrayList<>();
                for (int i = 0; i < size; i++)
                {
                    ItemStack stack = ItemStack.STREAM_CODEC.decode(buf);
                    ResourceStatus status = ResourceStatus.values()[buf.readInt()];
                    int realCount = buf.readInt();
                    boolean isDomum = buf.readBoolean();
                    BlockPos redirectorPos = buf.readBlockPos();
                    int tooltipCount = buf.readInt();
                    List<String> tooltipLines = new ArrayList<>();
                    for (int t = 0; t < tooltipCount; t++)
                        tooltipLines.add(buf.readUtf());
                    list.add(new ResourceEntry(stack, status, realCount, isDomum, redirectorPos, tooltipLines));
                }
                BlockPos pos = buf.readBlockPos();
                String builderName = buf.readUtf();
                String buildingName = buf.readUtf();
                String workerStatus = buf.readUtf();
                int availableCpus = buf.readInt();
                String redirectorState = buf.readUtf();
                return new ColonyLinkPacket(list, pos, builderName, buildingName, workerStatus, availableCpus, redirectorState);
            }
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(ColonyLinkPacket packet, IPayloadContext context)
    {
        context.enqueueWork(() -> {
            if (Minecraft.getInstance().screen instanceof ColonyLinkScreen screen)
                screen.updateEntries(packet.entries(), packet.builderName(), packet.buildingName(),
                        packet.workerStatus(), packet.availableCpus(), packet.redirectorState());
            else
                Minecraft.getInstance().setScreen(new ColonyLinkScreen(packet));
        });
    }
}