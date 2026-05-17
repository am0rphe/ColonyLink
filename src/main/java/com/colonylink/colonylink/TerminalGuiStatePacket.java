package com.colonylink.colonylink;

import appeng.api.parts.IPartHost;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.Nullable;

/**
 * C→S : player opened or closed the Warehouse Link Terminal GUI.
 *
 * Contains the host BlockPos and the Direction of the Part face,
 * so the server can locate the exact Part instance via IPartHost.getPart(side).
 */
public record TerminalGuiStatePacket(boolean open, BlockPos hostPos, int sideByte)
        implements CustomPacketPayload
{
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(
            ColonyLink.MODID, "terminal_gui_state");
    public static final CustomPacketPayload.Type<TerminalGuiStatePacket> TYPE =
            new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, TerminalGuiStatePacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, p) -> {
                        buf.writeBoolean(p.open());
                        buf.writeBlockPos(p.hostPos());
                        buf.writeByte(p.sideByte());
                    },
                    buf -> new TerminalGuiStatePacket(
                            buf.readBoolean(), buf.readBlockPos(), buf.readByte() & 0xFF)
            );

    /** Constructor used by the screen on open. Reads side from the open menu's part. */
    public TerminalGuiStatePacket(boolean open, BlockPos hostPos, Direction side)
    {
        this(open, hostPos, side != null ? side.ordinal() : 0);
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(TerminalGuiStatePacket packet, IPayloadContext ctx)
    {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;

            WarehouseLinkTerminalPart part = resolvePart(player, packet.hostPos(), packet.sideByte());
            if (part == null) return;

            if (packet.open())
                part.requestImmediateSync();
            else
                part.onGuiClosed();
        });
    }

    @Nullable
    static WarehouseLinkTerminalPart resolvePart(ServerPlayer player, BlockPos pos, int sideOrd)
    {
        var be = player.serverLevel().getBlockEntity(pos);
        if (!(be instanceof IPartHost host)) return null;
        Direction side = sideOrd < Direction.values().length
                ? Direction.values()[sideOrd] : Direction.NORTH;
        var raw = host.getPart(side);
        return raw instanceof WarehouseLinkTerminalPart p ? p : null;
    }
}