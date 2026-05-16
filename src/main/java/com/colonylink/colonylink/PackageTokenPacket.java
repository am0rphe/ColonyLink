package com.colonylink.colonylink;

import appeng.api.implementations.blockentities.IWirelessAccessPoint;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingService;
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
 * Packet C→S : action Citizens (Send ou Craft) avec vérification et consommation
 * d'un ColonyLink Package côté serveur.
 *
 * action = SEND  → extrait du ME et insère dans le warehouse (comme SendToWarehousePacket)
 * action = CRAFT → lance un craft AE2 (comme CraftRequestPacket isDomum=false)
 *
 * Pré-conditions vérifiées côté serveur :
 *   1. Clipboard présent dans l'inventaire
 *   2. Au moins 1 Package stocké dans la wand (NBT "citizen_packages")
 *   3. Warehouse card dans le redirector lié
 *   Si l'une échoue → message d'erreur, aucune consommation.
 */
public record PackageTokenPacket(
        ItemStack stack,
        int count,
        BlockPos redirectorPos,
        boolean isCraft   // false = Send, true = Craft AE2
) implements CustomPacketPayload
{
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(
            ColonyLink.MODID, "package_token");
    public static final CustomPacketPayload.Type<PackageTokenPacket> TYPE =
            new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, PackageTokenPacket> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> {
                ItemStack.STREAM_CODEC.encode(buf, p.stack());
                buf.writeInt(p.count());
                buf.writeBlockPos(p.redirectorPos());
                buf.writeBoolean(p.isCraft());
            },
            buf -> new PackageTokenPacket(
                    ItemStack.STREAM_CODEC.decode(buf),
                    buf.readInt(),
                    buf.readBlockPos(),
                    buf.readBoolean()
            )
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(PackageTokenPacket packet, IPayloadContext context)
    {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer sp)) return;
            handlePackageAction(sp, packet.stack(), packet.count(), packet.redirectorPos(), packet.isCraft());
        });
    }

    // ── Logique principale ────────────────────────────────────────────────────

    private static void handlePackageAction(ServerPlayer player, ItemStack stack,
                                            int count, BlockPos redirectorPos, boolean isCraft)
    {
        // 1. Trouver la wand
        ItemStack wand = findWandInInventory(player);
        if (wand == null)
        {
            player.sendSystemMessage(Component.literal("§c[ColonyLink] Clipboard not found!"));
            return;
        }

        // 2. Vérifier le stock de packages
        int stored = ColonyLinkWandLinkableHandler.getCitizenPackages(wand);
        if (stored <= 0)
        {
            player.sendSystemMessage(Component.literal(
                    "§c[ColonyLink] No Packages left! Craft more ColonyLink Packages and load them."));
            return;
        }

        // 3. Vérifier le redirector et la warehouse card
        ServerLevel level = player.serverLevel();
        var be = level.getBlockEntity(redirectorPos);
        if (!(be instanceof ColonyLinkRedirectorBlockEntity redirector))
        {
            player.sendSystemMessage(Component.literal("§c[ColonyLink] Redirector not found!"));
            return;
        }
        if (!redirector.hasWarehouseCard())
        {
            player.sendSystemMessage(Component.literal(
                    "§c[ColonyLink] No Warehouse Link Card in Redirector!"));
            return;
        }

        // 4. Connexion AE2
        GlobalPos linkedPos = ColonyLinkWandLinkableHandler.getLinkedPos(wand);
        if (linkedPos == null)
        {
            player.sendSystemMessage(Component.literal("§c[ColonyLink] Clipboard not linked to AE2!"));
            return;
        }
        ServerLevel targetLevel = level.getServer().getLevel(linkedPos.dimension());
        if (targetLevel == null) return;
        var wapBe = targetLevel.getBlockEntity(linkedPos.pos());
        if (!(wapBe instanceof IWirelessAccessPoint wap))
        {
            player.sendSystemMessage(Component.literal("§c[ColonyLink] AE2 WAP not found!"));
            return;
        }
        IGrid grid = wap.getGrid();
        if (grid == null)
        {
            player.sendSystemMessage(Component.literal("§c[ColonyLink] AE2 network offline!"));
            return;
        }

        // 5. Consommer 1 package AVANT l'action (point de non-retour)
        ColonyLinkWandLinkableHandler.consumeCitizenPackage(wand);

        // 6. Exécuter l'action
        boolean success;
        if (isCraft)
            success = doCraft(player, stack, count, grid, wap);
        else
            success = doSend(player, stack, count, redirectorPos, grid, wap, level, redirector);

        // 7. Feedback
        String itemName = stripBrackets(stack.getDisplayName().getString());
        if (success)
        {
            String action = isCraft ? "Craft queued" : "Sent";
            player.sendSystemMessage(Component.literal(
                    "§a[ColonyLink] " + action + ": §f" + count + "x " + itemName
                            + " §8(§7" + (stored - 1) + " package" + (stored - 1 != 1 ? "s" : "") + " remaining§8)"));
        }
        else
        {
            // Rembourser le package si l'action a échoué
            ColonyLinkWandLinkableHandler.addCitizenPackages(wand, 1);
            player.sendSystemMessage(Component.literal(
                    "§c[ColonyLink] Action failed — package refunded."));
        }

        // 8. Sync du nouveau count au client
        net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player,
                new PackageTokenSyncPacket(ColonyLinkWandLinkableHandler.getCitizenPackages(wand)));
    }

    // ── Send : ME → Warehouse ─────────────────────────────────────────────────

    private static boolean doSend(ServerPlayer player, ItemStack stack, int count,
                                  BlockPos redirectorPos, IGrid grid, IWirelessAccessPoint wap,
                                  ServerLevel level, ColonyLinkRedirectorBlockEntity redirector)
    {
        AEItemKey aeKey = AEItemKey.of(stack);
        if (aeKey == null) return false;

        IActionSource actionSource = IActionSource.ofPlayer(player, wap);
        MEStorage inventory = grid.getStorageService().getInventory();

        long inStock = grid.getStorageService().getCachedInventory().get(aeKey);
        if (inStock <= 0)
        {
            player.sendSystemMessage(Component.literal(
                    "§c[ColonyLink] " + stripBrackets(stack.getDisplayName().getString())
                            + " not available in ME."));
            return false;
        }

        IColony colony = IColonyManager.getInstance().getClosestColony(level, redirectorPos);
        if (colony == null) return false;

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
        return totalInserted > 0;
    }

    // ── Craft : AE2 ──────────────────────────────────────────────────────────

    private static boolean doCraft(ServerPlayer player, ItemStack stack, int count,
                                   IGrid grid, IWirelessAccessPoint wap)
    {
        IActionSource actionSource = IActionSource.ofPlayer(player, wap);
        ICraftingService cs = grid.getCraftingService();
        AEItemKey aeKey = AEItemKey.of(stack);
        if (aeKey == null || !cs.isCraftable(aeKey)) return false;

        // Délégue à CraftHandler qui gère le calcul du plan et le submit
        CraftHandler.handleCraftRequest(player, stack, count);
        return true;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

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
            if (s.getItem() instanceof ColonyLinkWand) return s;
        return null;
    }

    private static String stripBrackets(String name)
    {
        return (name.startsWith("[") && name.endsWith("]"))
                ? name.substring(1, name.length() - 1) : name;
    }
}