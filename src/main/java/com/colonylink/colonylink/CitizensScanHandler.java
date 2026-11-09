package com.colonylink.colonylink;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.request.RequestState;
import com.minecolonies.api.colony.requestsystem.requestable.IDeliverable;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.core.colony.buildings.AbstractBuildingStructureBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.stacks.KeyCounter;
import appeng.api.implementations.blockentities.IWirelessAccessPoint;
import net.minecraft.core.GlobalPos;

import java.util.ArrayList;
import java.util.List;

/**
 * Scans every citizen of the player's colony (builders excluded)
 * and sends a CitizensPacket to the client.
 */
public class CitizensScanHandler
{
    public static void sendCitizensPacket(ServerPlayer player)
    {
        sendCitizensPacket(player, true);
    }

    /**
     * v1.6.2 — {@code force=false} (ticker path) skips the broadcast when the content
     * is identical to the last one sent to this player, avoiding a full colony rescan
     * push every ticker interval. {@code force=true} (explicit click) always sends.
     */
    public static void sendCitizensPacket(ServerPlayer player, boolean force)
    {
        ItemStack wandStack = findWandInInventory(player);
        if (wandStack == null) return;

        BuilderToolHelper.ToolInventoryView inventoryView = stack -> 0L;
        BuilderToolHelper.ToolCraftingView   craftingView  = stack -> false;

        try
        {
            GlobalPos linkedPos = ColonyLinkWandLinkableHandler.getLinkedPos(wandStack);
            if (linkedPos != null)
            {
                ServerLevel targetLevel = player.server.getLevel(linkedPos.dimension());
                if (targetLevel != null)
                {
                    var be = targetLevel.getBlockEntity(linkedPos.pos());
                    if (be instanceof IWirelessAccessPoint wap)
                    {
                        IGrid grid = wap.getGrid();
                        if (grid != null)
                        {
                            KeyCounter inv = grid.getStorageService().getCachedInventory();
                            ICraftingService cs = grid.getCraftingService();
                            inventoryView = BuilderToolHelper.fromAE2Inventory(inv);
                            craftingView  = BuilderToolHelper.fromAE2CraftingService(cs);
                        }
                    }
                }
            }
        }
        catch (Exception ignored) {}

        final BuilderToolHelper.ToolInventoryView finalInv = inventoryView;
        final BuilderToolHelper.ToolCraftingView   finalCs  = craftingView;

        List<BuilderEntry> entries = ColonyLinkWandLinkableHandler.getBuilderEntries(wandStack);
        if (entries.isEmpty()) return;

        ServerLevel level = player.serverLevel();

        IColony colony = null;
        for (BuilderEntry e : entries)
        {
            // A2 dimension guard: never resolve a colony from a builder position stored
            // for another dimension — getClosestColony would return null or the wrong
            // colony, and the permission check below would then apply to the wrong
            // colony. Legacy entries (dimension == null) keep the prior behaviour.
            // Same semantics as CancelRequestPacket's cross-dimension guard.
            if (e.dimension() != null && !e.dimension().equals(level.dimension())) continue;
            colony = IColonyManager.getInstance().getClosestColony(level, e.builderPos());
            if (colony != null) break;
        }
        if (colony == null) return;

        // A2 — deny reading citizen requests without colony access. Covers BOTH call
        // paths (manual packet + periodic ticker), and placed before the citizen loop
        // it doubles as a free CPU skip on the periodic path. Removing the viewer stops
        // the ticker from re-running this, so the refusal message stays a single
        // occurrence per GUI session instead of ~2/second.
        if (!colony.getPermissions().hasPermission(player, Action.ACCESS_HUTS))
        {
            ColonyLinkServerTicker.removeViewer(player.getUUID());
            player.sendSystemMessage(
                    Component.translatable("colonylink.wand.msg.no_permission"));
            return;
        }

        java.util.Set<net.minecraft.core.BlockPos> builderPositions = new java.util.HashSet<>();
        for (BuilderEntry e : entries)
            builderPositions.add(e.builderPos());

        List<CitizensPacket.CitizenRequestEntry> result = new ArrayList<>();

        // 1.7.0-beta.3 — shared per-colony memory, keyed by request UUID. Cleaned once
        // per scan, only from MineColonies' own state (never from "the row is missing").
        IRequestManager requestManager = colony.getRequestManager();
        CitizenRequestMemory.ColonyKey colonyKey = CitizenRequestMemory.ColonyKey.of(colony);
        CitizenRequestMemory.cleanup(colonyKey, requestManager, player.server);
        WarehouseStock warehouseStock = new WarehouseStock(colony, level);

        for (ICitizenData citizen : colony.getCitizenManager().getCitizens())
        {
            IBuilding workBuilding = citizen.getWorkBuilding();

            if (workBuilding instanceof AbstractBuildingStructureBuilder) continue;
            if (workBuilding != null && builderPositions.contains(workBuilding.getPosition())) continue;

            if (citizen.getJob() != null)
            {
                String jobClass = citizen.getJob().getClass().getSimpleName().toLowerCase();
                if (jobClass.contains("builder")) continue;
            }

            String jobName = "Citizen";
            if (citizen.getJob() != null)
            {
                try
                {
                    String key = citizen.getJob().getJobRegistryEntry().getTranslationKey();
                    int lastDot = key.lastIndexOf('.');
                    String raw = lastDot >= 0 ? key.substring(lastDot + 1) : key;
                    jobName = raw.substring(0, 1).toUpperCase() + raw.substring(1);
                }
                catch (Exception ignored)
                {
                    String cls = citizen.getJob().getClass().getSimpleName();
                    jobName = cls.endsWith("Job") ? cls.substring(0, cls.length() - 3) : cls;
                }
            }

            try
            {
                var reqs = citizen.getJob() != null
                        ? workBuilding != null
                          ? workBuilding.getOpenRequests(citizen.getId())
                          : null
                        : null;
                if (reqs == null) continue;

                for (IRequest<?> req : reqs)
                {
                    if (req.getState() == RequestState.CANCELLED
                            || req.getState() == RequestState.OVERRULED) continue;
                    if (!(req.getRequest() instanceof IDeliverable del)) continue;

                    ItemStack display = ItemStack.EMPTY;
                    var displayStacks = req.getDisplayStacks();
                    if (displayStacks != null)
                        for (ItemStack s : displayStacks)
                            if (!s.isEmpty()) { display = s.copy(); break; }
                    if (display.isEmpty()) continue;

                    int count = del.getCount();
                    if (count <= 0) count = 1;
                    display.setCount(Math.min(count, 64));

                    // 1.7.0-beta.3 — diagnostic for "Sent reverts to Send": same uuid
                    // across scans = our memory was wiped; new uuid = MineColonies
                    // created a new request.
                    if (ColonyLink.LOGGER.isDebugEnabled())
                        ColonyLink.LOGGER.debug("[ColonyLink] Citizens scan: uuid={} state={} resolver={} citizen={} item={} count={}",
                                CitizenRequestMemory.requestUuid(req), req.getState(),
                                CitizenRequestMemory.resolverName(colony.getRequestManager(), req),
                                citizen.getName(),
                                net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(display.getItem()),
                                count);

                    ItemStack finalDisplay = display;
                    BuilderToolHelper.SubstituteAction finalAction = BuilderToolHelper.SubstituteAction.NONE;

                    // Citizen's actual building level (0 if no building)
                    int buildingLevel = workBuilding != null ? workBuilding.getBuildingLevel() : 0;

                    if (BuilderToolHelper.isArmor(display))
                    {
                        BuilderToolHelper.SubstituteResult sub =
                                BuilderToolHelper.findBestArmor(display, buildingLevel, finalInv, finalCs);
                        if (sub.action() != BuilderToolHelper.SubstituteAction.NONE)
                        {
                            finalDisplay = sub.displayStack();
                            finalAction  = sub.action();
                        }
                    }
                    else if (BuilderToolHelper.isTool(display))
                    {
                        BuilderToolHelper.SubstituteResult sub =
                                BuilderToolHelper.findBestTool(display, buildingLevel, finalInv, finalCs);
                        if (sub.action() != BuilderToolHelper.SubstituteAction.NONE)
                        {
                            finalDisplay = sub.displayStack();
                            finalAction  = sub.action();
                        }
                    }

                    boolean availableInME;
                    boolean craftableInME;
                    if (finalAction == BuilderToolHelper.SubstituteAction.SEND)
                    {
                        availableInME = true;
                        craftableInME = false;
                    }
                    else if (finalAction == BuilderToolHelper.SubstituteAction.CRAFT)
                    {
                        availableInME = false;
                        craftableInME = true;
                    }
                    else
                    {
                        availableInME = finalInv.get(finalDisplay) > 0;
                        craftableInME = !availableInME && finalCs.isCraftable(finalDisplay);
                    }

                    // 1.7.0-beta.3 — request identity, who handles it, Warehouse stock
                    // (only when it is still the player's to provide) and our mark.
                    java.util.UUID requestId = CitizenRequestMemory.requestUuid(req);
                    if (requestId == null) continue; // not a StandardToken: cannot be tracked
                    byte colonyState = CitizenRequestMemory.classifyRequest(requestManager, req);
                    int whStock = -1;
                    if (colonyState == CitizensPacket.STATE_PLAYER
                            || colonyState == CitizensPacket.STATE_RETRYING)
                        whStock = Math.min(count, warehouseStock.count(del, display));
                    CitizenRequestMemory.Entry markEntry = CitizenRequestMemory.get(colonyKey, requestId);
                    byte mark = markEntry != null ? markEntry.mark().code() : CitizensPacket.MARK_NONE;
                    String markedBy = markEntry != null ? markEntry.playerName() : "";

                    result.add(new CitizensPacket.CitizenRequestEntry(
                            finalDisplay, citizen.getName(), jobName, count, availableInME, craftableInME,
                            requestId, colonyState, whStock, mark, markedBy));
                }
            }
            catch (Exception e)
            {
                ColonyLink.LOGGER.debug("[CitizensScan] Error scanning citizen {}: {}", citizen.getName(), e.getMessage());
            }
        }

        // 1.7.0-beta.3 — the citizen "c|name|item" keys in the Clipboard are obsolete
        // (marks live in CitizenRequestMemory): purge them all. pruneSentRequestKeys
        // leaves the component untouched when nothing is removed, so after the first
        // purge this costs one read. Builder keys ("b|") are not touched.
        ColonyLinkWandLinkableHandler.pruneSentRequestKeys(wandStack, java.util.Collections.emptySet(),
                ColonyLinkWandLinkableHandler.SENT_PREFIX_CITIZEN);

        // v1.6.2 — throttle the ticker path: identical content means nothing to send.
        // An explicit click (force) always proceeds.
        long sig = computeCitizensSig(result);
        if (!force)
        {
            Long last = LAST_SIG.get(player.getUUID());
            if (last != null && last == sig) return;
        }
        LAST_SIG.put(player.getUUID(), sig);

        PacketDistributor.sendToPlayer(player, new CitizensPacket(result));
    }

    /**
     * 1.7.0-beta.3 — forced rescan for {@code actor} and every other player whose
     * Clipboard is open on the Citizens tab in the same dimension (the Sent memory is
     * shared per colony). Each rescan resolves its own colony and permission, so a
     * viewer of another colony just gets an unchanged list.
     */
    public static void rescanCitizensViewers(ServerPlayer actor)
    {
        sendCitizensPacket(actor, true);
        var server = actor.getServer();
        if (server == null) return;
        for (java.util.UUID uuid : ColonyLinkServerTicker.citizensTabViewers())
        {
            if (uuid.equals(actor.getUUID())) continue;
            ServerPlayer other = server.getPlayerList().getPlayer(uuid);
            if (other != null && other.level() == actor.level())
                sendCitizensPacket(other, true);
        }
    }

    // v1.6.2 — throttle state for the ticker Citizens tab
    private static final java.util.Map<java.util.UUID, Long> LAST_SIG =
            new java.util.concurrent.ConcurrentHashMap<>();

    // A2 — per-player cooldown for MANUAL requests only
    // Guards CitizensRequestPacket (the client entering the Citizens tab). It must
    // NOT cover the periodic ticker refresh, which calls sendCitizensPacket(..., false)
    // directly and has to keep pushing live updates every ticker interval.
    private static final long REQUEST_COOLDOWN_MS = 500L;
    private static final java.util.Map<java.util.UUID, Long> LAST_REQUEST_MS =
            new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * Gate for MANUAL Citizens requests (the packet path). Returns {@code false} when
     * this player fired one less than {@link #REQUEST_COOLDOWN_MS} ago, {@code true}
     * otherwise (arming the window). Silent on rejection — no chat message is sent.
     */
    public static boolean allowManualRequest(ServerPlayer player)
    {
        long now = System.currentTimeMillis();
        java.util.UUID uuid = player.getUUID();
        Long last = LAST_REQUEST_MS.get(uuid);
        if (last != null && now - last < REQUEST_COOLDOWN_MS)
        {
            // Rejected: do NOT refresh the timestamp — otherwise a spammer would keep
            // pushing their own window forward and could never clear the cooldown.
            //
            // Invalidate this player's content signature so the next ticker pass
            // (<= 0.5s) is forced to rescan and resend. On entering the tab the client
            // cleared its list and shows a spinner; the handler sends nothing on a
            // rejected request, so the ticker is what unblocks the GUI.
            invalidateSignature(uuid);
            return false;
        }
        LAST_REQUEST_MS.put(uuid, now);
        return true;
    }

    /**
     * Drops only the throttle signature (LAST_SIG), forcing the next ticker pass to
     * resend. Must NOT touch LAST_REQUEST_MS — clearing the cooldown on a rejection
     * would defeat the cooldown entirely.
     */
    public static void invalidateSignature(java.util.UUID uuid)
    {
        LAST_SIG.remove(uuid);
    }

    /** Drops a player's throttle + cooldown state (called on logout by the server ticker). */
    public static void forget(java.util.UUID uuid)
    {
        LAST_SIG.remove(uuid);
        LAST_REQUEST_MS.remove(uuid);
    }

    /** v1.7.1 — bulk clear on server stop (called from ColonyLinkServerTicker.onServerStopping). */
    public static void clearAll()
    {
        LAST_SIG.clear();
        LAST_REQUEST_MS.clear();
    }

    /** FNV-1a fold over the visible Citizens content — same idea as computeWandSig. */
    private static long computeCitizensSig(java.util.List<CitizensPacket.CitizenRequestEntry> entries)
    {
        long sig = 0xcbf29ce484222325L;
        final long P = 0x100000001b3L;
        for (CitizensPacket.CitizenRequestEntry e : entries)
        {
            int h = net.minecraft.world.item.Item.getId(e.stack().getItem());
            h = 31 * h + e.citizenName().hashCode();
            h = 31 * h + e.jobName().hashCode();
            h = 31 * h + e.count();
            h = 31 * h + (e.availableInME()  ? 1 : 0);
            h = 31 * h + (e.craftableInME() ? 1 : 0);
            // 1.7.0-beta.3 — state changes must be pushed too
            h = 31 * h + e.requestId().hashCode();
            h = 31 * h + e.colonyState();
            h = 31 * h + e.warehouseStock();
            h = 31 * h + e.mark();
            h = 31 * h + e.markedBy().hashCode();
            sig = (sig ^ h) * P;
        }
        return sig;
    }

    /**
     * 1.7.0-beta.3 — matching items across ALL the colony's Warehouses (MineColonies
     * sums every Warehouse when deciding whether it can resolve a request), counted
     * with the request's own matcher (tags, fuel lists, NBT…). Read the way the
     * Warehouse Link Terminal reads a Warehouse: the aggregated meta-handler MineColonies
     * exposes at the hut position, else each rack's local half (27 slots, double racks
     * mirror the other half). Racks in unloaded chunks are skipped (getCapability on an
     * unloaded position returns null). Walked once per (requestable type, displayed
     * item) per scan.
     */
    private static final class WarehouseStock
    {
        private static final int RACK_HALF_SLOTS = 27;

        private final ServerLevel level;
        private final List<com.minecolonies.core.colony.buildings.workerbuildings.BuildingWareHouse> warehouses = new ArrayList<>();
        private final java.util.Map<String, Integer> cache = new java.util.HashMap<>();

        WarehouseStock(IColony colony, ServerLevel level)
        {
            this.level = level;
            for (IBuilding b : colony.getServerBuildingManager().getBuildings().values())
                if (b instanceof com.minecolonies.core.colony.buildings.workerbuildings.BuildingWareHouse wh)
                    warehouses.add(wh);
        }

        int count(IDeliverable request, ItemStack display)
        {
            String key = request.getClass().getName() + "|"
                    + net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(display.getItem())
                    + "|" + display.getComponents().hashCode();
            return cache.computeIfAbsent(key, k -> {
                long total = 0;
                for (var wh : warehouses)
                {
                    try { total += countIn(wh, request); }
                    catch (Exception ignored) {}
                }
                return (int) Math.min(Integer.MAX_VALUE, total);
            });
        }

        private long countIn(com.minecolonies.core.colony.buildings.workerbuildings.BuildingWareHouse wh,
                             IDeliverable request)
        {
            var cap = net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK;
            net.neoforged.neoforge.items.IItemHandler meta = level.getCapability(cap, wh.getPosition(), null);
            if (meta != null && meta.getSlots() > RACK_HALF_SLOTS)
                return countSlots(meta, meta.getSlots(), request);
            long total = 0;
            java.util.Set<net.minecraft.core.BlockPos> seen = new java.util.HashSet<>();
            for (net.minecraft.core.BlockPos rackPos : wh.getContainers())
            {
                if (!seen.add(rackPos)) continue;
                net.neoforged.neoforge.items.IItemHandler rack = level.getCapability(cap, rackPos, null);
                if (rack != null) total += countSlots(rack, Math.min(rack.getSlots(), RACK_HALF_SLOTS), request);
            }
            return total;
        }

        private static long countSlots(net.neoforged.neoforge.items.IItemHandler h, int slots, IDeliverable request)
        {
            long total = 0;
            for (int s = 0; s < slots; s++)
            {
                ItemStack st = h.getStackInSlot(s);
                if (!st.isEmpty() && request.matches(st)) total += st.getCount();
            }
            return total;
        }
    }

    private static ItemStack findWandInInventory(ServerPlayer player)
    {
        // Delegate to the shared implementation that also checks Curios slots.
        return ColonyLinkServerTicker.findWandInInventory(player);
    }
}