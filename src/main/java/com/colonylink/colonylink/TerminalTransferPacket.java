package com.colonylink.colonylink;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * C→S : directional item transfer in the Warehouse Link Terminal.
 *
 * Directions:
 *   WH_TO_ME      — Warehouse → ME
 *   WH_TO_PLAYER  — Warehouse → Player
 *   ME_TO_WH      — ME → Warehouse
 *   ME_TO_PLAYER  — ME → Player
 */
public record TerminalTransferPacket(
        ItemStack stack,
        int count,
        Direction direction,
        BlockPos hostPos,
        int sideByte
) implements CustomPacketPayload
{
    public enum Direction
    {
        WH_TO_ME,
        WH_TO_PLAYER,
        ME_TO_WH,
        ME_TO_PLAYER,
        ME_TO_ME
    }

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(
            ColonyLink.MODID, "terminal_transfer");
    public static final CustomPacketPayload.Type<TerminalTransferPacket> TYPE =
            new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, TerminalTransferPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, p) -> {
                        ItemStack.STREAM_CODEC.encode(buf, p.stack());
                        buf.writeInt(p.count());
                        buf.writeInt(p.direction().ordinal());
                        buf.writeBlockPos(p.hostPos());
                        buf.writeByte(p.sideByte());
                    },
                    buf -> new TerminalTransferPacket(
                            ItemStack.STREAM_CODEC.decode(buf),
                            buf.readInt(),
                            Direction.values()[buf.readInt()],
                            buf.readBlockPos(),
                            buf.readByte() & 0xFF
                    )
            );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(TerminalTransferPacket packet, IPayloadContext ctx)
    {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;

            WarehouseLinkTerminalPart part =
                    TerminalGuiStatePacket.resolvePart(player, packet.hostPos(), packet.sideByte());
            if (part == null) return;

            String itemName = stripBrackets(packet.stack().getDisplayName().getString());

            int transferred = switch (packet.direction())
            {
                case WH_TO_ME     -> part.transferWarehouseToMe(player, packet.stack(), packet.count());
                case WH_TO_PLAYER -> part.transferWarehouseToPlayer(player, packet.stack(), packet.count());
                case ME_TO_WH     -> part.transferMeToWarehouse(player, packet.stack(), packet.count());
                case ME_TO_PLAYER -> part.transferMeToPlayer(player, packet.stack(), packet.count());
                default           -> 0;
            };

            if (transferred > 0)
            {
                String label = switch (packet.direction())
                {
                    case WH_TO_ME     -> "WH → ME";
                    case WH_TO_PLAYER -> "WH → Inventory";
                    case ME_TO_WH     -> "ME → WH";
                    case ME_TO_PLAYER -> "ME → Inventory";
                    default           -> "Transferred";
                };
                player.sendSystemMessage(Component.literal(
                        "§a[Terminal] §7" + label + ": §f" + transferred + "x " + itemName));
            }
            else
            {
                player.sendSystemMessage(Component.literal(
                        "§c[Terminal] Transfer failed."));
            }
        });
    }

    private static String stripBrackets(String s)
    {
        return s.startsWith("[") && s.endsWith("]") ? s.substring(1, s.length() - 1) : s;
    }
}