package com.colonylink.colonylink;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
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
        String workerIdleReason,
        int availableCpus,
        String redirectorState,
        BuilderRequest builderRequest,
        boolean hasWarehouseCard,
        boolean warehousePriority,
        List<BuilderTabMeta> tabMetas,
        int activeTabIndex,
        long rfStored,
        long rfMax
) implements CustomPacketPayload
{
    public record BuilderTabMeta(
            BlockPos builderPos,
            String builderName,
            String buildingLabel,
            boolean hasRedirector
    ) {}

    public record ResourceEntry(
            ItemStack stack,
            ResourceStatus status,
            int realCount,
            boolean isDomum,
            BlockPos redirectorPos,
            List<Component> tooltipLines
    ) {}

    public record BuilderRequest(
            ItemStack stack,
            int count,
            ResourceStatus status,
            BlockPos redirectorPos,
            List<Component> tooltipLines
    )
    {
        public static BuilderRequest NONE = new BuilderRequest(
                ItemStack.EMPTY, 0, ResourceStatus.NO_PATTERN, BlockPos.ZERO, List.of());
    }

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(
            ColonyLink.MODID, "colony_link_packet");
    public static final CustomPacketPayload.Type<ColonyLinkPacket> TYPE =
            new CustomPacketPayload.Type<>(ID);

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
                    for (Component line : entry.tooltipLines()) ComponentSerialization.STREAM_CODEC.encode(buf, line);
                }
                buf.writeBlockPos(packet.builderPos());
                buf.writeUtf(packet.builderName());
                buf.writeUtf(packet.buildingName());
                buf.writeUtf(packet.workerStatus());
                buf.writeUtf(packet.workerIdleReason());
                buf.writeInt(packet.availableCpus());
                buf.writeUtf(packet.redirectorState());
                BuilderRequest req = packet.builderRequest() != null
                        ? packet.builderRequest() : BuilderRequest.NONE;
                boolean hasReq = !req.stack().isEmpty() && req.count() > 0;
                buf.writeBoolean(hasReq);
                if (hasReq)
                {
                    ItemStack.STREAM_CODEC.encode(buf, req.stack());
                    buf.writeInt(req.count());
                    buf.writeInt(req.status().ordinal());
                    buf.writeBlockPos(req.redirectorPos());
                    buf.writeInt(req.tooltipLines().size());
                    for (Component line : req.tooltipLines()) ComponentSerialization.STREAM_CODEC.encode(buf, line);
                }
                buf.writeBoolean(packet.hasWarehouseCard());
                buf.writeBoolean(packet.warehousePriority());
                List<BuilderTabMeta> metas = packet.tabMetas() != null ? packet.tabMetas() : List.of();
                buf.writeInt(metas.size());
                for (BuilderTabMeta meta : metas)
                {
                    buf.writeBlockPos(meta.builderPos());
                    buf.writeUtf(meta.builderName());
                    buf.writeUtf(meta.buildingLabel());
                    buf.writeBoolean(meta.hasRedirector());
                }
                buf.writeInt(packet.activeTabIndex());
                buf.writeLong(packet.rfStored());
                buf.writeLong(packet.rfMax());
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
                    List<Component> tooltipLines = new ArrayList<>();
                    for (int t = 0; t < tooltipCount; t++) tooltipLines.add(ComponentSerialization.STREAM_CODEC.decode(buf));
                    list.add(new ResourceEntry(stack, status, realCount, isDomum, redirectorPos, tooltipLines));
                }
                BlockPos pos        = buf.readBlockPos();
                String builderName  = buf.readUtf();
                String buildingName = buf.readUtf();
                String workerStatus = buf.readUtf();
                String workerIdleReason = buf.readUtf();
                int availableCpus   = buf.readInt();
                String redirectorState = buf.readUtf();
                BuilderRequest req;
                boolean hasReq = buf.readBoolean();
                if (hasReq)
                {
                    ItemStack reqStack   = ItemStack.STREAM_CODEC.decode(buf);
                    int reqCount         = buf.readInt();
                    ResourceStatus reqSt = ResourceStatus.values()[buf.readInt()];
                    BlockPos reqRPos     = buf.readBlockPos();
                    int reqTtCount       = buf.readInt();
                    List<Component> reqTt = new ArrayList<>();
                    for (int t = 0; t < reqTtCount; t++) reqTt.add(ComponentSerialization.STREAM_CODEC.decode(buf));
                    req = new BuilderRequest(reqStack, reqCount, reqSt, reqRPos, reqTt);
                }
                else req = BuilderRequest.NONE;

                boolean hasWCard = buf.readBoolean();
                boolean whPrio   = buf.readBoolean();
                int metaCount    = buf.readInt();
                List<BuilderTabMeta> metas = new ArrayList<>();
                for (int i = 0; i < metaCount; i++)
                {
                    BlockPos bPos  = buf.readBlockPos();
                    String bName   = buf.readUtf();
                    String bLabel  = buf.readUtf();
                    boolean hasR   = buf.readBoolean();
                    metas.add(new BuilderTabMeta(bPos, bName, bLabel, hasR));
                }
                int activeTabIndex = buf.readInt();
                long rfStored      = buf.readLong();
                long rfMax         = buf.readLong();

                return new ColonyLinkPacket(list, pos, builderName, buildingName,
                        workerStatus, workerIdleReason, availableCpus, redirectorState, req,
                        hasWCard, whPrio, metas, activeTabIndex, rfStored, rfMax);
            }
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }
}