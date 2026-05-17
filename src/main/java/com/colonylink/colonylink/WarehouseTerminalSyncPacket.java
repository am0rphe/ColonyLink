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

/**
 * S→C : sends the full warehouse snapshot to the client.
 */
public record WarehouseTerminalSyncPacket(
        List<WarehouseItemEntry> entries,
        boolean hasWarehouseCard,
        BlockPos terminalPos
) implements CustomPacketPayload
{
    public record WarehouseItemEntry(ItemStack stack, long count) {}

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(
            ColonyLink.MODID, "warehouse_terminal_sync");
    public static final CustomPacketPayload.Type<WarehouseTerminalSyncPacket> TYPE =
            new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, WarehouseTerminalSyncPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, p) -> {
                        buf.writeInt(p.entries().size());
                        for (var e : p.entries())
                        {
                            ItemStack.STREAM_CODEC.encode(buf, e.stack());
                            buf.writeLong(e.count());
                        }
                        buf.writeBoolean(p.hasWarehouseCard());
                        buf.writeBlockPos(p.terminalPos());
                    },
                    buf -> {
                        int size = buf.readInt();
                        List<WarehouseItemEntry> entries = new ArrayList<>();
                        for (int i = 0; i < size; i++)
                            entries.add(new WarehouseItemEntry(
                                    ItemStack.STREAM_CODEC.decode(buf), buf.readLong()));
                        boolean hasCard = buf.readBoolean();
                        BlockPos pos = buf.readBlockPos();
                        return new WarehouseTerminalSyncPacket(entries, hasCard, pos);
                    }
            );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(WarehouseTerminalSyncPacket packet, IPayloadContext ctx)
    {
        ctx.enqueueWork(() -> {
            if (Minecraft.getInstance().screen instanceof WarehouseLinkTerminalScreen screen)
                screen.updateWarehouseSnapshot(packet);
        });
    }
}