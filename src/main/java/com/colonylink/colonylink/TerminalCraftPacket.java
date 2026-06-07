package com.colonylink.colonylink;

import appeng.api.networking.crafting.ICraftingService;
import appeng.api.stacks.AEItemKey;
import appeng.menu.locator.MenuLocators;
import appeng.menu.me.crafting.CraftAmountMenu;
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
 * C→S : crafting action in the Warehouse Link Terminal.
 *
 * mode = CRAFT_3x3  → complete a 3×3 recipe
 * mode = AUTOCRAFT  → open native AE2 CraftAmountMenu (fix 8)
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
                case AUTOCRAFT  -> handleAutocraft(player, part, packet.stack());
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

        player.sendSystemMessage(Component.translatable("colonylink.term_pkt.crafted", result.getDisplayName()));
    }

    /**
     * Middle-click sur un item craftable dans le panel ME :
     * ouvre le vrai écran CraftAmountMenu natif d'AE2. [Fix 8]
     *
     * CraftAmountMenu.open() est la méthode statique AE2 qui :
     *   1. Appelle MenuOpener.open(TYPE, player, locator)
     *   2. Cast player.containerMenu en CraftAmountMenu
     *   3. Appelle setWhatToCraft(aeKey, initialAmount) + broadcastChanges()
     *
     *   - Direction  = la face sur laquelle notre Part est monté (part.getSide())
     *
     * Notre Part implémente ISubMenuHost via :
     *   WarehouseLinkTerminalPart → AbstractTerminalPart → ITerminalHost → ISubMenuHost ✓
     */
    private static void handleAutocraft(ServerPlayer player,
                                        WarehouseLinkTerminalPart part,
                                        ItemStack stack)
    {
        if (!part.isAe2Active())
        {
            player.sendSystemMessage(Component.translatable("colonylink.term_pkt.ae2_offline"));
            return;
        }

        var node = part.getManagedGridNode().getNode();
        if (node == null) return;

        ICraftingService cs    = node.getGrid().getCraftingService();
        AEItemKey        aeKey = AEItemKey.of(stack);

        if (aeKey == null || !cs.isCraftable(aeKey))
        {
            player.sendSystemMessage(Component.translatable("colonylink.term_pkt.no_pattern", stack.getDisplayName()));
            return;
        }

        // Construire le locator depuis le Part directement (1 argument dans cette version AE2)
        var locator = MenuLocators.forPart(part);

        // Ouvre le CraftAmountMenu natif AE2 — l'utilisateur voit l'UI AE2
        // standard avec le champ de quantité, le choix du CPU, etc.
        CraftAmountMenu.open(player, locator, aeKey, 1);
    }
}