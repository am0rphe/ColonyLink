package com.colonylink.colonylink;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
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
 * S→C : sends the AE2 storage snapshot (items + craftable flags) to the client.
 * Sent together with WarehouseTerminalSyncPacket on every scan interval.
 */
public record TerminalMeSyncPacket(
        List<MeEntry> entries,
        BlockPos terminalPos
) implements CustomPacketPayload
{
    public record MeEntry(ItemStack stack, long count, boolean craftable) {}

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(
            ColonyLink.MODID, "terminal_me_sync");
    public static final CustomPacketPayload.Type<TerminalMeSyncPacket> TYPE =
            new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, TerminalMeSyncPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, p) -> {
                        buf.writeInt(p.entries().size());
                        for (var e : p.entries())
                        {
                            ItemStack.STREAM_CODEC.encode(buf, e.stack());
                            buf.writeLong(e.count());
                            buf.writeBoolean(e.craftable());
                        }
                        buf.writeBlockPos(p.terminalPos());
                    },
                    buf -> {
                        int size = buf.readInt();
                        List<MeEntry> entries = new ArrayList<>();
                        for (int i = 0; i < size; i++)
                        {
                            ItemStack stack   = ItemStack.STREAM_CODEC.decode(buf);
                            long count        = buf.readLong();
                            boolean craftable = buf.readBoolean();
                            entries.add(new MeEntry(stack, count, craftable));
                        }
                        BlockPos pos = buf.readBlockPos();
                        return new TerminalMeSyncPacket(entries, pos);
                    }
            );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(TerminalMeSyncPacket packet, IPayloadContext ctx)
    {
        ctx.enqueueWork(() -> {
            if (Minecraft.getInstance().screen instanceof WarehouseLinkTerminalScreen screen)
                screen.updateMeSnapshot(packet);
        });
    }

    /**
     * Builds a TerminalMeSyncPacket from the current AE2 network state.
     *
     * KeyCounter does not expose a forEach(BiConsumer<AEKey, Long>) that accepts
     * typed lambda parameters cleanly in all AE2 versions. We iterate via
     * its iterator instead, which yields Map.Entry<AEKey, Long>.
     */
    public static TerminalMeSyncPacket fromGrid(
            appeng.api.networking.IGrid grid, BlockPos terminalPos)
    {
        KeyCounter inventory       = grid.getStorageService().getCachedInventory();
        var        craftingService = grid.getCraftingService();

        List<MeEntry> entries = new ArrayList<>();

        // KeyCounter extends Iterable — iterate over its entries directly.
        for (var entry : inventory)
        {
            AEKey key = entry.getKey();
            if (!(key instanceof AEItemKey aeKey)) continue;
            long    count     = entry.getLongValue();
            boolean craftable = craftingService.isCraftable(aeKey);
            entries.add(new MeEntry(aeKey.toStack(1), count, craftable));
        }

        return new TerminalMeSyncPacket(entries, terminalPos);
    }
}