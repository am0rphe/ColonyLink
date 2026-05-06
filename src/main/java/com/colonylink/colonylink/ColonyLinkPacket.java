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
        String redirectorState,
        BuilderRequest builderRequest,
        boolean hasWarehouseCard,         // présence de la WarehouseLinkCard
        boolean warehousePriority         // état du switch priorité Warehouse/AE2
) implements CustomPacketPayload
{
    /**
     * Représente une entrée de ressource dans la liste.
     */
    public record ResourceEntry(
            ItemStack stack,
            ResourceStatus status,
            int realCount,
            boolean isDomum,
            BlockPos redirectorPos,
            List<String> tooltipLines
    ) {}

    /**
     * Feature 1 — Requête prioritaire du builder PNJ.
     */
    public record BuilderRequest(
            ItemStack stack,
            int count,
            ResourceStatus status,
            BlockPos redirectorPos,
            List<String> tooltipLines
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
                // entries
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
                // header
                buf.writeBlockPos(packet.builderPos());
                buf.writeUtf(packet.builderName());
                buf.writeUtf(packet.buildingName());
                buf.writeUtf(packet.workerStatus());
                buf.writeInt(packet.availableCpus());
                buf.writeUtf(packet.redirectorState());
                // builder request
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
                    for (String line : req.tooltipLines())
                        buf.writeUtf(line);
                }
                // warehouse card + priority
                buf.writeBoolean(packet.hasWarehouseCard());
                buf.writeBoolean(packet.warehousePriority());
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
                // builder request
                BuilderRequest req;
                boolean hasReq = buf.readBoolean();
                if (hasReq)
                {
                    ItemStack reqStack = ItemStack.STREAM_CODEC.decode(buf);
                    int reqCount = buf.readInt();
                    ResourceStatus reqStatus = ResourceStatus.values()[buf.readInt()];
                    BlockPos reqRedirectorPos = buf.readBlockPos();
                    int reqTooltipCount = buf.readInt();
                    List<String> reqTooltipLines = new ArrayList<>();
                    for (int t = 0; t < reqTooltipCount; t++)
                        reqTooltipLines.add(buf.readUtf());
                    req = new BuilderRequest(reqStack, reqCount, reqStatus, reqRedirectorPos, reqTooltipLines);
                }
                else
                {
                    req = BuilderRequest.NONE;
                }
                // warehouse card + priority
                boolean hasWarehouseCard = buf.readBoolean();
                boolean warehousePriority = buf.readBoolean();

                return new ColonyLinkPacket(list, pos, builderName, buildingName,
                        workerStatus, availableCpus, redirectorState, req, hasWarehouseCard, warehousePriority);
            }
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(ColonyLinkPacket packet, IPayloadContext context)
    {
        context.enqueueWork(() -> {
            if (Minecraft.getInstance().screen instanceof ColonyLinkScreen screen)
                screen.updateEntries(packet.entries(), packet.builderName(), packet.buildingName(),
                        packet.workerStatus(), packet.availableCpus(), packet.redirectorState(),
                        packet.builderRequest(), packet.hasWarehouseCard(), packet.warehousePriority());
            else
                Minecraft.getInstance().setScreen(new ColonyLinkScreen(packet));
        });
    }
}