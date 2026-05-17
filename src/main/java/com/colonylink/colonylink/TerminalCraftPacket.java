package com.colonylink.colonylink;

import appeng.api.networking.crafting.CalculationStrategy;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.networking.crafting.ICraftingSimulationRequester;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * C→S : crafting action in the Warehouse Link Terminal.
 *
 * mode = CRAFT_3x3  → complete a 3×3 recipe
 * mode = AUTOCRAFT  → submit an AE2 autocraft job
 */
public record TerminalCraftPacket(
        Mode mode,
        ItemStack stack,
        int count,
        BlockPos hostPos,
        int sideByte
) implements CustomPacketPayload
{
    public enum Mode { CRAFT_3x3, AUTOCRAFT }

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(
            ColonyLink.MODID, "terminal_craft");
    public static final CustomPacketPayload.Type<TerminalCraftPacket> TYPE =
            new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, TerminalCraftPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, p) -> {
                        buf.writeInt(p.mode().ordinal());
                        ItemStack.STREAM_CODEC.encode(buf, p.stack());
                        buf.writeInt(p.count());
                        buf.writeBlockPos(p.hostPos());
                        buf.writeByte(p.sideByte());
                    },
                    buf -> new TerminalCraftPacket(
                            Mode.values()[buf.readInt()],
                            ItemStack.STREAM_CODEC.decode(buf),
                            buf.readInt(),
                            buf.readBlockPos(),
                            buf.readByte() & 0xFF
                    )
            );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(TerminalCraftPacket packet, IPayloadContext ctx)
    {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;

            WarehouseLinkTerminalPart part =
                    TerminalGuiStatePacket.resolvePart(player, packet.hostPos(), packet.sideByte());
            if (part == null) return;

            switch (packet.mode())
            {
                case CRAFT_3x3  -> handleCraft3x3(player);
                case AUTOCRAFT  -> handleAutocraft(player, part, packet.stack(), packet.count());
            }
        });
    }

    private static void handleCraft3x3(ServerPlayer player)
    {
        if (!(player.containerMenu instanceof WarehouseLinkTerminalMenu menu)) return;

        ItemStack result = menu.getCraftResult().getItem(0);
        if (result.isEmpty()) return;

        if (!player.getInventory().add(result.copy()))
            player.drop(result.copy(), false);

        var grid = menu.getCraftingGrid();
        for (int i = 0; i < grid.getContainerSize(); i++)
        {
            ItemStack s = grid.getItem(i);
            if (!s.isEmpty()) s.shrink(1);
        }
        menu.getCraftResult().setItem(0, ItemStack.EMPTY);

        player.sendSystemMessage(Component.literal(
                "§a[Terminal] Crafted §f" + result.getDisplayName().getString()));
    }

    private static void handleAutocraft(ServerPlayer player, WarehouseLinkTerminalPart part,
                                        ItemStack stack, int count)
    {
        if (!part.isAe2Active())
        {
            player.sendSystemMessage(Component.literal("§c[Terminal] AE2 offline!"));
            return;
        }

        var node = part.getManagedGridNode().getNode();
        if (node == null) return;
        var grid = node.getGrid();

        ICraftingService cs        = grid.getCraftingService();
        IActionSource actionSrc    = IActionSource.ofMachine(part);
        ICraftingSimulationRequester simReq = () -> actionSrc;

        AEItemKey aeKey = AEItemKey.of(stack);
        if (aeKey == null || !cs.isCraftable(aeKey))
        {
            player.sendSystemMessage(Component.literal(
                    "§c[Terminal] No pattern for §f" + stack.getDisplayName().getString()));
            return;
        }

        CraftHandler.submitCraftJob(player, grid, cs, actionSrc, simReq, aeKey, count);
    }
}