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
                    buf -> {
                        int ordinal = buf.readInt();
                        Mode[] modes = Mode.values();
                        if (ordinal < 0 || ordinal >= modes.length)
                            ordinal = 0;
                        return new TerminalCraftPacket(
                            modes[ordinal],
                            ItemStack.STREAM_CODEC.decode(buf),
                            buf.readInt(),
                            buf.readBlockPos(),
                            buf.readByte() & 0xFF
                        );
                    }
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

        // v1.7.1 — handle craft remainders (buckets, bottles) like vanilla ResultSlot.onTake.
        var grid = menu.getCraftingGrid();
        net.minecraft.world.item.crafting.CraftingInput craftInput = grid.asCraftInput();
        var recipeOpt = player.level().getRecipeManager()
                .getRecipeFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING, craftInput, player.level());
        net.minecraft.core.NonNullList<ItemStack> remainders = recipeOpt.isPresent()
                ? recipeOpt.get().value().getRemainingItems(craftInput)
                : net.minecraft.core.NonNullList.withSize(grid.getContainerSize(), ItemStack.EMPTY);
        for (int i = 0; i < grid.getContainerSize(); i++)
        {
            ItemStack s = grid.getItem(i);
            if (!s.isEmpty()) s.shrink(1);
            ItemStack rem = remainders.get(i);
            if (!rem.isEmpty())
            {
                if (grid.getItem(i).isEmpty())
                    grid.setItem(i, rem);
                else if (!player.getInventory().add(rem))
                    player.drop(rem, false);
            }
        }
        menu.getCraftResult().setItem(0, ItemStack.EMPTY);

        player.sendSystemMessage(Component.translatable("colonylink.term_pkt.crafted", result.getDisplayName()));
    }

    /**
     * Middle-click on a craftable item in the ME panel: opens AE2's native
     * CraftAmountMenu screen. [Fix 8]
     *
     * CraftAmountMenu.open() is the AE2 static method that:
     *   1. calls MenuOpener.open(TYPE, player, locator)
     *   2. casts player.containerMenu to CraftAmountMenu
     *   3. calls setWhatToCraft(aeKey, initialAmount) + broadcastChanges()
     *
     * Our Part implements ISubMenuHost via:
     *   WarehouseLinkTerminalPart → AbstractTerminalPart → ITerminalHost → ISubMenuHost
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

        // Build the locator from the Part directly (1 argument in this AE2 version)
        var locator = MenuLocators.forPart(part);

        // Open AE2's native CraftAmountMenu — the player sees the standard AE2 UI
        // with the amount field, CPU choice, etc.
        CraftAmountMenu.open(player, locator, aeKey, 1);
    }
}