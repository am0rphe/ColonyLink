package com.colonylink.colonylink;

import appeng.api.implementations.blockentities.IWirelessAccessPoint;
import appeng.api.networking.IGrid;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.storage.MEStorage;
import appeng.api.config.Actionable;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingWareHouse;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Packet C→S : extrait un item du ME et l'insère dans les racks de la warehouse.
 * Utilisé par la tab Citizens pour satisfaire les requêtes des citoyens non-builders.
 */
public record SendToWarehousePacket(ItemStack stack, BlockPos redirectorPos, int count)
        implements CustomPacketPayload
{
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(ColonyLink.MODID, "send_to_warehouse");
    public static final CustomPacketPayload.Type<SendToWarehousePacket> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, SendToWarehousePacket> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> {
                ItemStack.STREAM_CODEC.encode(buf, p.stack());
                buf.writeBlockPos(p.redirectorPos());
                buf.writeInt(p.count());
            },
            buf -> new SendToWarehousePacket(
                    ItemStack.STREAM_CODEC.decode(buf),
                    buf.readBlockPos(),
                    buf.readInt()
            )
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(SendToWarehousePacket packet, IPayloadContext context)
    {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer sp)
                handleSendToWarehouse(sp, packet.stack(), packet.redirectorPos(), packet.count());
        });
    }

    private static void handleSendToWarehouse(ServerPlayer player, ItemStack stack,
                                              BlockPos redirectorPos, int count)
    {
        ItemStack wandStack = findWandInInventory(player);
        if (wandStack == null) { player.sendSystemMessage(Component.literal("§cClipboard not found!")); return; }

        ServerLevel level = player.serverLevel();

        var be = level.getBlockEntity(redirectorPos);
        if (!(be instanceof ColonyLinkRedirectorBlockEntity redirector))
        {
            player.sendSystemMessage(Component.literal("§cRedirector not found!"));
            return;
        }
        if (!redirector.hasWarehouseCard())
        {
            player.sendSystemMessage(Component.literal("§cNo warehouse card in redirector!"));
            return;
        }

        GlobalPos linkedPos = ColonyLinkWandLinkableHandler.getLinkedPos(wandStack);
        if (linkedPos == null) { player.sendSystemMessage(Component.literal("§cClipboard not linked!")); return; }
        ServerLevel targetLevel = level.getServer().getLevel(linkedPos.dimension());
        if (targetLevel == null) return;
        var wapBe = targetLevel.getBlockEntity(linkedPos.pos());
        if (!(wapBe instanceof IWirelessAccessPoint wap)) { player.sendSystemMessage(Component.literal("§cAE2 WAP not found!")); return; }
        IGrid grid = wap.getGrid();
        if (grid == null) { player.sendSystemMessage(Component.literal("§cAE2 network offline!")); return; }

        IActionSource actionSource = IActionSource.ofPlayer(player, wap);
        MEStorage inventory = grid.getStorageService().getInventory();
        AEItemKey aeKey = AEItemKey.of(stack);
        if (aeKey == null) { player.sendSystemMessage(Component.literal("§cInvalid item!")); return; }

        long inStock = grid.getStorageService().getCachedInventory().get(aeKey);
        if (inStock <= 0)
        {
            player.sendSystemMessage(Component.literal(
                    "§c[ColonyLink] " + stack.getDisplayName().getString() + " not available in ME."));
            return;
        }

        IColony colony = IColonyManager.getInstance().getClosestColony(level, redirectorPos);
        if (colony == null) { player.sendSystemMessage(Component.literal("§cNo colony found!")); return; }

        int remaining = Math.min(count, (int) inStock);
        int totalInserted = 0;

        outer:
        for (IBuilding building : colony.getServerBuildingManager().getBuildings().values())
        {
            if (!(building instanceof BuildingWareHouse warehouse)) continue;
            var containers = warehouse.getContainers();
            if (containers == null) continue;

            for (BlockPos rackPos : containers)
            {
                if (remaining <= 0) break outer;
                IItemHandler rack = level.getCapability(Capabilities.ItemHandler.BLOCK, rackPos, null);
                if (rack == null) continue;

                while (remaining > 0)
                {
                    int batch = Math.min(remaining, 64);
                    long extracted = inventory.extract(aeKey, batch, Actionable.MODULATE, actionSource);
                    if (extracted <= 0) break outer;

                    ItemStack toInsert = aeKey.toStack((int) extracted);
                    ItemStack leftOver = insertIntoHandler(rack, toInsert);
                    long sent = extracted - leftOver.getCount();
                    totalInserted += (int) sent;
                    remaining -= (int) sent;

                    if (!leftOver.isEmpty())
                    {
                        inventory.insert(aeKey, leftOver.getCount(), Actionable.MODULATE, actionSource);
                        break;
                    }
                }
            }
        }

        String itemName = stack.getDisplayName().getString();
        if (itemName.startsWith("[") && itemName.endsWith("]"))
            itemName = itemName.substring(1, itemName.length() - 1);

        if (totalInserted > 0)
            player.sendSystemMessage(Component.literal(
                    "§a[ColonyLink] Sent §f" + totalInserted + "x " + itemName + "§a to warehouse."));
        else
            player.sendSystemMessage(Component.literal(
                    "§c[ColonyLink] Could not send " + itemName + " — warehouse full or not found."));
    }

    private static ItemStack insertIntoHandler(IItemHandler handler, ItemStack stack)
    {
        ItemStack remainder = stack.copy();
        for (int slot = 0; slot < handler.getSlots() && !remainder.isEmpty(); slot++)
            remainder = handler.insertItem(slot, remainder, false);
        return remainder;
    }

    private static ItemStack findWandInInventory(ServerPlayer player)
    {
        for (ItemStack s : player.getInventory().items)
            if (s.getItem() instanceof ColonyLinkWand)
                return s;
        return null;
    }
}
