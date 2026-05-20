package com.colonylink.colonylink;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * C→S : interactions avec le Warehouse Link Terminal.
 *
 * Directions :
 *   PICKUP_FROM_WH   Clic sur item WH → extrait du WH, met dans carried vanilla
 *   PICKUP_FROM_ME   Clic sur item ME → extrait du ME, met dans carried vanilla
 *   PUT_INTO_WH      Drop carried → WH (carried non-vide, clic sur panel WH)
 *   PUT_INTO_ME      Drop carried → ME (carried non-vide, clic sur panel ME)
 *   WH_TO_ME         Sélection WH → ME (flèche centrale)
 *   WH_TO_PLAYER     Sélection WH → Inventaire joueur
 *   ME_TO_WH         Sélection ME → WH
 *   ME_TO_PLAYER     Sélection ME → Inventaire joueur
 *   CRAFT_TO_WH      Vide grille craft → WH
 *   CRAFT_TO_PLAYER  Vide grille craft → Inventaire joueur
 *   CRAFT_TO_ME      Vide grille craft → ME
 *   RESULT_TO_WH     Craft + résultat → WH
 *   RESULT_TO_ME     Craft + résultat → ME
 *   INV_TO_WH        Shift+clic inventaire → WH
 *   INV_TO_ME        Shift+clic inventaire → ME
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
        PICKUP_FROM_WH,  // clic panel WH → extrait du WH → carried
        PICKUP_FROM_ME,  // clic panel ME → extrait du ME → carried
        PUT_INTO_WH,     // drop carried sur panel WH → insère dans WH
        PUT_INTO_ME,     // drop carried sur panel ME → insère dans ME
        WH_TO_ME,
        WH_TO_PLAYER,
        ME_TO_WH,
        ME_TO_PLAYER,
        CRAFT_TO_WH,
        CRAFT_TO_PLAYER,
        CRAFT_TO_ME,
        RESULT_TO_WH,
        RESULT_TO_ME,
        INV_TO_WH,
        INV_TO_ME
    }

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(
            ColonyLink.MODID, "terminal_transfer");
    public static final CustomPacketPayload.Type<TerminalTransferPacket> TYPE =
            new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, TerminalTransferPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, p) -> {
                        ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, p.stack());
                        buf.writeInt(p.count());
                        buf.writeInt(p.direction().ordinal());
                        buf.writeBlockPos(p.hostPos());
                        buf.writeByte(p.sideByte());
                    },
                    buf -> new TerminalTransferPacket(
                            ItemStack.OPTIONAL_STREAM_CODEC.decode(buf),
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

            switch (packet.direction())
            {
                // ── PICKUP : extrait de WH/ME → carried vanilla ──────────────
                case PICKUP_FROM_WH -> part.pickupFromWarehouse(player, packet.stack(), packet.count());
                case PICKUP_FROM_ME -> part.pickupFromMe(player, packet.stack(), packet.count());

                // ── PUT : dépose carried dans WH/ME ──────────────────────────
                case PUT_INTO_WH    -> part.putCarriedIntoWarehouse(player);
                case PUT_INTO_ME    -> part.putCarriedIntoMe(player);

                // ── Transferts sélection ──────────────────────────────────────
                case WH_TO_ME      -> part.transferWarehouseToMe(player, packet.stack(), packet.count());
                case WH_TO_PLAYER  -> part.transferWarehouseToInventory(player, packet.stack(), packet.count());
                case ME_TO_WH      -> part.transferMeToWarehouse(player, packet.stack(), packet.count());
                case ME_TO_PLAYER  -> part.transferMeToInventory(player, packet.stack(), packet.count());

                // ── Grille craft ──────────────────────────────────────────────
                case CRAFT_TO_WH, CRAFT_TO_PLAYER, CRAFT_TO_ME -> {
                    if (player.containerMenu instanceof WarehouseLinkTerminalMenu menu) {
                        int dest = switch (packet.direction()) {
                            case CRAFT_TO_WH     -> 0;
                            case CRAFT_TO_PLAYER -> 1;
                            case CRAFT_TO_ME     -> 2;
                            default              -> 1;
                        };
                        part.transferCraftGridContents(player, menu.getCraftingGrid(), dest);
                    }
                }
                case RESULT_TO_WH -> {
                    if (player.containerMenu instanceof WarehouseLinkTerminalMenu menu) {
                        ItemStack result = menu.getCraftResult().getItem(0);
                        if (!result.isEmpty()) {
                            int times = packet.count() > 1 ? result.getMaxStackSize() : 1;
                            for (int t = 0; t < times; t++) {
                                if (menu.getCraftResult().getItem(0).isEmpty()) break;
                                ItemStack crafted = doCraft(menu, player);
                                if (crafted.isEmpty()) break;
                                part.transferPlayerToWarehouse(player, crafted);
                            }
                        }
                    }
                }
                case RESULT_TO_ME -> {
                    if (player.containerMenu instanceof WarehouseLinkTerminalMenu menu) {
                        ItemStack result = menu.getCraftResult().getItem(0);
                        if (!result.isEmpty()) {
                            int times = packet.count() > 1 ? result.getMaxStackSize() : 1;
                            for (int t = 0; t < times; t++) {
                                if (menu.getCraftResult().getItem(0).isEmpty()) break;
                                ItemStack crafted = doCraft(menu, player);
                                if (crafted.isEmpty()) break;
                                part.transferPlayerToMe(player, crafted);
                            }
                        }
                    }
                }
                case INV_TO_WH -> {
                    ItemStack from = removeFromPlayerInventory(player, packet.stack(), packet.count());
                    if (!from.isEmpty()) part.transferPlayerToWarehouse(player, from);
                }
                case INV_TO_ME -> {
                    ItemStack from = removeFromPlayerInventory(player, packet.stack(), packet.count());
                    if (!from.isEmpty()) part.transferPlayerToMe(player, from);
                }
            }
        });
    }

    private static ItemStack removeFromPlayerInventory(ServerPlayer player, ItemStack template, int count)
    {
        var inv = player.getInventory();
        int remaining = count;
        java.util.List<ItemStack> collected = new java.util.ArrayList<>();
        for (int i = 0; i < inv.getContainerSize() && remaining > 0; i++) {
            ItemStack s = inv.getItem(i);
            if (s.isEmpty() || !ItemStack.isSameItemSameComponents(s, template)) continue;
            int take = Math.min(remaining, s.getCount());
            collected.add(s.copyWithCount(take));
            s.shrink(take);
            remaining -= take;
        }
        if (collected.isEmpty()) return ItemStack.EMPTY;
        ItemStack result = collected.get(0).copy();
        for (int i = 1; i < collected.size(); i++) result.grow(collected.get(i).getCount());
        return result;
    }

    private static ItemStack doCraft(WarehouseLinkTerminalMenu menu, ServerPlayer player)
    {
        ItemStack result = menu.getCraftResult().getItem(0);
        if (result.isEmpty()) return ItemStack.EMPTY;
        ItemStack crafted = result.copy();
        var grid = menu.getCraftingGrid();
        for (int i = 0; i < grid.getContainerSize(); i++) {
            ItemStack slot = grid.getItem(i);
            if (!slot.isEmpty()) slot.shrink(1);
        }
        menu.getCraftResult().setItem(0, ItemStack.EMPTY);
        menu.slotsChanged(grid);
        return crafted;
    }
}