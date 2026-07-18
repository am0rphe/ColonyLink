package com.colonylink.colonylink;

import appeng.api.config.Actionable;
import appeng.api.implementations.blockentities.IWirelessAccessPoint;
import appeng.api.networking.IGrid;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.storage.MEStorage;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.core.colony.buildings.AbstractBuildingStructureBuilder;
import com.minecolonies.core.colony.buildings.utils.BuildingBuilderResource;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingWareHouse;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * C→S packet: the WAREHOUSE delivery mode ("send_target = WAREHOUSE").
 * Extracts a builder-needed item from the ME network and inserts it into the
 * colony Warehouse racks; a MineColonies courier then delivers it normally.
 *
 * v1.6.0 — complete rewrite. This packet was registered but never sent before;
 * the payload changed from (stack, redirectorPos, count) to
 * (stack, builderPos, count): the server resolves everything else from the
 * player's wand, so a modified client cannot point it at arbitrary positions.
 *
 * Server-side validation chain (the client is never authoritative):
 *   1. send_target must be WAREHOUSE (server config)
 *   2. wand present and linked to a Wireless Access Point
 *   3. builderPos must be one of the wand's linked builders (same dimension)
 *   4. colony permission ACCESS_HUTS
 *   5. the item must be a currently-missing resource of that builder
 *   6. count clamped to [1, missing]
 *   7. duplicate-send guard (sent-key already stored → reject)
 *   8. warehouse chunks fully live (fail-off, v1.4.9 policy)
 *   9. ME network reachable and item in stock
 *  10. RF cost (same SEND_COST_RF as the builder path), only after validation
 *
 * Zero voiding: any leftover the racks cannot take is re-inserted into the ME
 * network with a VERIFIED return value; whatever the ME cannot re-accept goes
 * to the player inventory (dropped at their feet if full).
 *
 * On success the sent-key "b|x,y,z|itemId|availableAtSend" is stored in the
 * wand NBT (server side, persists). NEVER calls creditDeliveredResource():
 * the line must stay visible as SENT_PENDING until the courier delivers.
 */
public record SendToWarehousePacket(ItemStack stack, BlockPos builderPos, int count)
        implements CustomPacketPayload
{
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(ColonyLink.MODID, "send_to_warehouse");
    public static final CustomPacketPayload.Type<SendToWarehousePacket> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, SendToWarehousePacket> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> {
                ItemStack.STREAM_CODEC.encode(buf, p.stack());
                buf.writeBlockPos(p.builderPos());
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
                handleSendToWarehouse(sp, packet.stack(), packet.builderPos(), packet.count());
        });
    }

    private static void handleSendToWarehouse(ServerPlayer player, ItemStack stack,
                                              BlockPos builderPos, int count)
    {
        // ── 1. Mode guard — server config is the only authority ─────────────
        if (ColonyLinkConfig.SEND_TARGET.get() != ColonyLinkConfig.SendTarget.WAREHOUSE)
        {
            player.sendSystemMessage(Component.translatable("colonylink.delivery.blocked_warehouse"));
            return;
        }

        if (stack.isEmpty())
        {
            player.sendSystemMessage(Component.translatable("colonylink.wh_pkt.invalid_item"));
            return;
        }
        // A non-positive count can only come from a modified client — reject,
        // don't clamp up (clamping -1 to 1 would still extract an item).
        if (count <= 0)
        {
            player.sendSystemMessage(Component.translatable("colonylink.stw.invalid_request"));
            return;
        }

        // ── 2. Wand ──────────────────────────────────────────────────────────
        ItemStack wandStack = findWandInInventory(player);
        if (wandStack == null)
        {
            player.sendSystemMessage(Component.translatable("colonylink.whc.clipboard_not_found"));
            return;
        }
        if (!ColonyLinkWandLinkableHandler.isLinked(wandStack))
        {
            player.sendSystemMessage(Component.translatable("colonylink.whc.clipboard_not_linked"));
            return;
        }

        // ── 3. The builder must be linked to THIS wand ───────────────────────
        boolean builderLinked = false;
        for (BuilderEntry e : ColonyLinkWandLinkableHandler.getBuilderEntries(wandStack))
        {
            if (e.builderPos().equals(builderPos)) { builderLinked = true; break; }
        }
        if (!builderLinked)
        {
            player.sendSystemMessage(Component.translatable("colonylink.stw.invalid_request"));
            return;
        }

        // Cross-dimension: same v1.4.9 policy as the builder path — refuse cleanly.
        ResourceKey<Level> builderDim =
                ColonyLinkWandLinkableHandler.getBuilderDimension(wandStack, builderPos);
        if (builderDim != null && !player.serverLevel().dimension().equals(builderDim))
        {
            player.sendSystemMessage(Component.translatable("colonylink.handler.cross_dimension", builderDim.location().getPath()));
            return;
        }

        ServerLevel level = player.serverLevel();

        // ── 4. Colony, building, permission ──────────────────────────────────
        IColony colony = IColonyManager.getInstance().getClosestColony(level, builderPos);
        if (colony == null)
        {
            player.sendSystemMessage(Component.translatable("colonylink.wh_pkt.no_colony"));
            return;
        }
        if (!colony.getPermissions().hasPermission(player, Action.ACCESS_HUTS))
        {
            player.sendSystemMessage(Component.translatable("colonylink.wand.msg.no_permission"));
            return;
        }

        AbstractBuildingStructureBuilder builderBuilding = null;
        for (IBuilding b : colony.getServerBuildingManager().getBuildings().values())
        {
            if (b.getPosition().equals(builderPos) && b instanceof AbstractBuildingStructureBuilder bb)
            {
                builderBuilding = bb;
                break;
            }
        }
        if (builderBuilding == null)
        {
            player.sendSystemMessage(Component.translatable("colonylink.handler.hut_not_found"));
            return;
        }

        // ── 5. The item must actually be requested by that builder ────────────
        // Two legitimate sources: a missing build material (getNeededResources),
        // or an open request of the assigned citizen (tools/armor/food are
        // requests, not materials — the courier fulfills those from the
        // warehouse too, so WAREHOUSE mode must accept them).
        var needed = builderBuilding.getNeededResources();
        BuildingBuilderResource matched = null;
        if (needed != null)
        {
            for (BuildingBuilderResource res : needed.values())
            {
                if (ItemStack.isSameItemSameComponents(res.getItemStack(), stack))
                {
                    matched = res;
                    break;
                }
            }
        }

        int missing;
        int baseline;
        if (matched != null)
        {
            missing = matched.getAmount() - matched.getAvailable();
            baseline = matched.getAvailable();
        }
        else
        {
            missing = findOpenRequestCount(builderBuilding, stack);
            // Open requests carry no 'available' counter: reconciliation for
            // these keys is request disappearance (see pruneBuilderSentKeys).
            baseline = 0;
        }
        if (missing <= 0)
        {
            player.sendSystemMessage(Component.translatable("colonylink.stw.not_needed", stack.getDisplayName()));
            return;
        }

        // ── 6. Clamp the client-supplied count (MAX_VALUE safe; <=0 was rejected) ─
        int toSend = Mth.clamp(count, 1, missing);

        // ── 7. Duplicate-send guard — the sent-key memory IS the anti-cascade ─
        Set<String> sentKeys = ColonyLinkWandLinkableHandler.getSentRequestKeys(wandStack);
        if (ColonyLinkWandLinkableHandler.hasBuilderSentKey(sentKeys, builderPos, stack.getItem()))
        {
            player.sendSystemMessage(Component.translatable("colonylink.stw.already_sent", stack.getDisplayName()));
            return;
        }

        // ── 8. Warehouse racks must exist and be fully live (fail-off) ────────
        if (!ColonyLinkChunkUtil.colonyWarehousesFullyLoaded(level, colony))
        {
            player.sendSystemMessage(Component.translatable("colonylink.handler.warehouse_unloaded"));
            return;
        }

        List<IItemHandler> rackHandlers = new ArrayList<>();
        for (IBuilding building : colony.getServerBuildingManager().getBuildings().values())
        {
            if (!(building instanceof BuildingWareHouse warehouse)) continue;
            var containers = warehouse.getContainers();
            if (containers == null) continue;
            for (BlockPos rackPos : containers)
            {
                IItemHandler rack = level.getCapability(Capabilities.ItemHandler.BLOCK, rackPos, null);
                if (rack != null) rackHandlers.add(rack);
            }
        }
        if (rackHandlers.isEmpty())
        {
            player.sendSystemMessage(Component.translatable("colonylink.wh_pkt.could_not_send", displayName(stack)));
            return;
        }

        // ── 9. ME network and stock ───────────────────────────────────────────
        GlobalPos linkedPos = ColonyLinkWandLinkableHandler.getLinkedPos(wandStack);
        if (linkedPos == null)
        {
            player.sendSystemMessage(Component.translatable("colonylink.whc.clipboard_not_linked"));
            return;
        }
        ServerLevel wapLevel = level.getServer().getLevel(linkedPos.dimension());
        if (wapLevel == null) return;
        var wapBe = wapLevel.getBlockEntity(linkedPos.pos());
        if (!(wapBe instanceof IWirelessAccessPoint wap))
        {
            player.sendSystemMessage(Component.translatable("colonylink.wh_pkt.no_wap"));
            return;
        }
        IGrid grid = wap.getGrid();
        if (grid == null)
        {
            player.sendSystemMessage(Component.translatable("colonylink.wh_pkt.network_offline"));
            return;
        }

        IActionSource actionSource = IActionSource.ofPlayer(player, wap);
        MEStorage inventory = grid.getStorageService().getInventory();
        AEItemKey aeKey = AEItemKey.of(stack);
        if (aeKey == null)
        {
            player.sendSystemMessage(Component.translatable("colonylink.wh_pkt.invalid_item"));
            return;
        }

        long inStock = grid.getStorageService().getCachedInventory().get(aeKey);
        if (inStock <= 0)
        {
            player.sendSystemMessage(Component.translatable("colonylink.wh_pkt.not_available", stack.getDisplayName()));
            return;
        }
        toSend = (int) Math.min(toSend, inStock);

        // ── 10. RF cost — charged only after every validation passed ──────────
        long sendCost = ColonyLinkConfig.SEND_COST_RF.get();
        if (sendCost > 0 && !ColonyLinkServerTicker.tryConsumeRF(player, sendCost))
        {
            player.sendSystemMessage(Component.translatable("colonylink.stb.not_enough_power", sendCost));
            return;
        }

        // ── Extract from ME, insert into racks (zero voiding) ────────────────
        int remaining = toSend;
        int totalInserted = 0;

        outer:
        for (IItemHandler rack : rackHandlers)
        {
            while (remaining > 0)
            {
                int batch = Math.min(remaining, 64);
                long extracted = inventory.extract(aeKey, batch, Actionable.MODULATE, actionSource);
                if (extracted <= 0) break outer;

                ItemStack toInsert = aeKey.toStack((int) extracted);
                ItemStack leftOver = insertIntoHandler(rack, toInsert);
                int sent = (int) extracted - leftOver.getCount();
                totalInserted += sent;
                remaining -= sent;

                if (!leftOver.isEmpty())
                {
                    // This rack is full — return the leftover to the ME network,
                    // VERIFYING the insert (an overflow/void cell or a full network
                    // could silently discard it); the player inventory is the
                    // last-resort sink so nothing is ever voided.
                    long reinserted = inventory.insert(aeKey, leftOver.getCount(),
                            Actionable.MODULATE, actionSource);
                    int lost = leftOver.getCount() - (int) reinserted;
                    if (lost > 0)
                        player.getInventory().placeItemBackInInventory(aeKey.toStack(lost));
                    break; // try the next rack
                }
            }
            if (remaining <= 0) break;
        }

        // ── Feedback + sent-key memory ────────────────────────────────────────
        if (totalInserted > 0)
        {
            // Record the send server-side. Baseline = the builder's 'available'
            // at send time: the ticker prunes this key when 'available' changes
            // (courier delivered) or the item leaves getNeededResources() and
            // has no matching open request anymore.
            // NEVER creditDeliveredResource() here — the request must stay open
            // (grey SENT_PENDING) until the courier actually delivers.
            String key = ColonyLinkWandLinkableHandler.builderSentKey(
                    builderPos, stack.getItem(), baseline);
            ColonyLinkWandLinkableHandler.addSentRequestKey(wandStack, key);

            player.sendSystemMessage(Component.translatable("colonylink.wh_pkt.sent",
                    totalInserted, displayName(stack)));
        }
        else
        {
            player.sendSystemMessage(Component.translatable("colonylink.wh_pkt.could_not_send",
                    displayName(stack)));
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * v1.6.0 — count requested via the builder's open requests (tools, armor,
     * food...), 0 if the stack matches none. Mirrors the ticker's
     * fetchBuilderRequest pass 1; IDeliverable.matches() is the canonical test.
     */
    private static int findOpenRequestCount(AbstractBuildingStructureBuilder bb, ItemStack stack)
    {
        try
        {
            if (bb.getAllAssignedCitizen().isEmpty()) return 0;
            var citizen = bb.getAllAssignedCitizen().iterator().next();
            var reqs = bb.getOpenRequests(citizen.getId());
            if (reqs == null) return 0;
            for (com.minecolonies.api.colony.requestsystem.request.IRequest<?> req : reqs)
            {
                if (req.getState() == com.minecolonies.api.colony.requestsystem.request.RequestState.CANCELLED
                        || req.getState() == com.minecolonies.api.colony.requestsystem.request.RequestState.OVERRULED)
                    continue;
                if (!(req.getRequest() instanceof com.minecolonies.api.colony.requestsystem.requestable.IDeliverable del))
                    continue;
                if (!del.matches(stack)) continue;
                return Math.max(1, del.getCount());
            }
        }
        catch (Exception e)
        {
            ColonyLink.LOGGER.debug("[ColonyLink] open-request match failed: {}", e.getMessage());
        }
        return 0;
    }

    private static String displayName(ItemStack stack)
    {
        String itemName = stack.getDisplayName().getString();
        if (itemName.startsWith("[") && itemName.endsWith("]"))
            itemName = itemName.substring(1, itemName.length() - 1);
        return itemName;
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
}
