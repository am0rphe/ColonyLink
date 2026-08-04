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
import com.minecolonies.api.colony.permissions.Action;
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
 *
 * v1.6.0 — the payload gained {@code citizenName} so the SERVER can record the
 * sent-request key ("c|name|itemId") in the wand NBT on success. Before this,
 * the key was written client-side on the client's stack copy and was lost on
 * every inventory resync/relog. The Package token remains the real consumable
 * guard on this path; the key only drives the grey "Sent ↺" button state.
 */
public record PackageTokenPacket(
        ItemStack stack,
        int count,
        BlockPos redirectorPos,
        boolean isCraft,     // false = Send, true = Craft AE2
        String citizenName   // v1.6.0 — for the server-side sent-key
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
                buf.writeUtf(p.citizenName());
            },
            buf -> new PackageTokenPacket(
                    ItemStack.STREAM_CODEC.decode(buf),
                    buf.readInt(),
                    buf.readBlockPos(),
                    buf.readBoolean(),
                    buf.readUtf()
            )
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(PackageTokenPacket packet, IPayloadContext context)
    {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer sp)) return;
            handlePackageAction(sp, packet.stack(), packet.count(), packet.redirectorPos(),
                    packet.isCraft(), packet.citizenName());
        });
    }

    // ── Logique principale ────────────────────────────────────────────────────

    private static void handlePackageAction(ServerPlayer player, ItemStack stack,
                                            int count, BlockPos redirectorPos, boolean isCraft,
                                            String citizenName)
    {
        // v1.6.0 — input hardening: a non-positive count or empty stack can only
        // come from a modified client. Reject before consuming anything.
        if (count <= 0 || stack.isEmpty())
        {
            player.sendSystemMessage(Component.translatable("colonylink.stw.invalid_request"));
            return;
        }

        // 1. Trouver la wand
        ItemStack wand = findWandInInventory(player);
        if (wand == null)
        {
            player.sendSystemMessage(Component.translatable("colonylink.pkg_token.clip_not_found"));
            return;
        }

        // A4 — redirector↔wand validation (client is NOT authoritative on redirectorPos).
        // The client sends a raw BlockPos; a modified client could point at ANY colony's
        // redirector. Only accept a redirectorPos actually linked to THIS player's wand.
        // Same predicate/dimension guard as CancelRequestPacket. Done at the earliest
        // point the wand is available, before anything else is checked or consumed.
        boolean redirectorLinked = false;
        for (BuilderEntry e : ColonyLinkWandLinkableHandler.getBuilderEntries(wand))
        {
            if (e.hasRedirector() && e.redirectorPos().equals(redirectorPos))
            {
                // Dimension guard: refuse a cross-dimension redirector. Legacy entries
                // (dimension == null) are not filtered — historical behaviour preserved.
                if (e.dimension() != null && !e.dimension().equals(player.serverLevel().dimension()))
                    break;
                redirectorLinked = true;
                break;
            }
        }
        if (!redirectorLinked)
        {
            resyncPackages(player, wand);
            player.sendSystemMessage(Component.translatable("colonylink.stw.invalid_request"));
            return;
        }

        // 2. Vérifier le stock de packages
        int stored = ColonyLinkWandLinkableHandler.getCitizenPackages(wand);
        if (stored <= 0)
        {
            resyncPackages(player, wand);
            player.sendSystemMessage(Component.translatable("colonylink.pkg_token.no_packages"));
            return;
        }

        // 3. Vérifier le redirector et la warehouse card
        ServerLevel level = player.serverLevel();
        var be = level.getBlockEntity(redirectorPos);
        if (!(be instanceof ColonyLinkRedirectorBlockEntity redirector))
        {
            resyncPackages(player, wand);
            player.sendSystemMessage(Component.translatable("colonylink.pkg_token.redir_not_found"));
            return;
        }
        if (!redirector.hasWarehouseCard())
        {
            resyncPackages(player, wand);
            player.sendSystemMessage(Component.translatable("colonylink.pkg_token.no_card"));
            return;
        }

        // 4. Connexion AE2
        GlobalPos linkedPos = ColonyLinkWandLinkableHandler.getLinkedPos(wand);
        if (linkedPos == null)
        {
            resyncPackages(player, wand);
            player.sendSystemMessage(Component.translatable("colonylink.pkg_token.not_linked"));
            return;
        }
        ServerLevel targetLevel = level.getServer().getLevel(linkedPos.dimension());
        if (targetLevel == null)
        {
            resyncPackages(player, wand);
            return;
        }
        var wapBe = targetLevel.getBlockEntity(linkedPos.pos());
        if (!(wapBe instanceof IWirelessAccessPoint wap))
        {
            resyncPackages(player, wand);
            player.sendSystemMessage(Component.translatable("colonylink.pkg_token.no_wap"));
            return;
        }
        IGrid grid = wap.getGrid();
        if (grid == null)
        {
            resyncPackages(player, wand);
            player.sendSystemMessage(Component.translatable("colonylink.pkg_token.network_offline"));
            return;
        }

        // A4 — colony permission (ACCESS_HUTS), non-craft path only. doCraft touches no
        // colony (it launches a craft on the player's own grid), so permission is moot
        // there. This resolves the colony a second time (doSend resolves it again from
        // the same redirectorPos): intentional, to avoid changing doSend's signature or
        // body — the cost is negligible. Do NOT "deduplicate" this getClosestColony call.
        // colony == null stays permissive: doSend already returns false in that case,
        // which triggers the refund path.
        if (!isCraft)
        {
            IColony colony = IColonyManager.getInstance().getClosestColony(level, redirectorPos);
            if (colony != null && !colony.getPermissions().hasPermission(player, Action.ACCESS_HUTS))
            {
                resyncPackages(player, wand);
                player.sendSystemMessage(Component.translatable("colonylink.wand.msg.no_permission"));
                return;
            }
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
            // v1.6.0 — record the sent-request key SERVER-side (authoritative
            // stack, synced to the client, survives relogs). Only on success:
            // the refund path below must not leave a key behind. addSentRequestKey
            // dedupes, so the "Sent ↺" re-click (craft+send pair) stays idempotent.
            if (citizenName != null && !citizenName.isEmpty())
                ColonyLinkWandLinkableHandler.addSentRequestKey(wand,
                        ColonyLinkWandLinkableHandler.citizenSentKey(citizenName, stack.getItem()));

            String action = (isCraft ? Component.translatable("colonylink.pkg_token.action_craft").getString() : Component.translatable("colonylink.pkg_token.action_sent").getString());
            player.sendSystemMessage(Component.translatable("colonylink.pkg_token.success", action, count, itemName, (stored - 1)));
        }
        else
        {
            // Rembourser le package si l'action a échoué
            ColonyLinkWandLinkableHandler.addCitizenPackages(wand, 1);
            player.sendSystemMessage(Component.translatable("colonylink.pkg_token.refunded"));
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
            player.sendSystemMessage(Component.translatable("colonylink.wh_pkt.not_available", stripBrackets(stack.getDisplayName().getString())));
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
                        // v1.6.0 — zero voiding: VERIFY the ME re-insert (a full
                        // network or an overflow/void cell can silently discard);
                        // the player inventory is the last-resort sink.
                        long reinserted = inventory.insert(aeKey, leftOver.getCount(),
                                Actionable.MODULATE, actionSource);
                        int lost = leftOver.getCount() - (int) reinserted;
                        if (lost > 0)
                            player.getInventory().placeItemBackInInventory(aeKey.toStack(lost));
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

    /**
     * Pushes the authoritative package count back to the client. The client decrements
     * its counter optimistically before the server replies, so any early return that
     * skips step 8 would leave the displayed count wrong until the next tab resync.
     */
    private static void resyncPackages(ServerPlayer player, ItemStack wand)
    {
        net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player,
                new PackageTokenSyncPacket(ColonyLinkWandLinkableHandler.getCitizenPackages(wand)));
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
        // Delegate to the shared implementation that also checks Curios slots.
        return ColonyLinkServerTicker.findWandInInventory(player);
    }

    private static String stripBrackets(String name)
    {
        return (name.startsWith("[") && name.endsWith("]"))
                ? name.substring(1, name.length() - 1) : name;
    }
}