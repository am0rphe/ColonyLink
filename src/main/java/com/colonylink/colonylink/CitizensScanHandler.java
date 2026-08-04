package com.colonylink.colonylink;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
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
 * Scanne tous les citoyens de la colonie du joueur (hors builders)
 * et envoie un CitizensPacket au client.
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

                    ItemStack finalDisplay = display;
                    BuilderToolHelper.SubstituteAction finalAction = BuilderToolHelper.SubstituteAction.NONE;

                    // Niveau réel du bâtiment du citoyen (0 si pas de bâtiment)
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

                    result.add(new CitizensPacket.CitizenRequestEntry(
                            finalDisplay, citizen.getName(), jobName, count, availableInME, craftableInME));
                }
            }
            catch (Exception e)
            {
                ColonyLink.LOGGER.debug("[CitizensScan] Error scanning citizen {}: {}", citizen.getName(), e.getMessage());
            }
        }

        // v1.6.0 — server-side reconciliation of the citizen sent-keys ("c|").
        // Any stored citizen key whose request is no longer active is pruned, so
        // the grey "Sent ↺" button re-arms when the request resolves. Scoped to
        // the citizen family: builder keys ("b|") are reconciled by the ticker.
        // The early returns above skip this on failure — a failed scan must
        // never wipe the memory.
        // v1.6.2 — throttle the ticker path: identical content means nothing to send
        // and nothing to reconcile. An explicit click (force) always proceeds.
        long sig = computeCitizensSig(result);
        if (!force)
        {
            Long last = LAST_SIG.get(player.getUUID());
            if (last != null && last == sig) return;
        }
        LAST_SIG.put(player.getUUID(), sig);

        java.util.Set<String> activeCitizenKeys = new java.util.HashSet<>();
        for (CitizensPacket.CitizenRequestEntry entry : result)
            activeCitizenKeys.add(ColonyLinkWandLinkableHandler.citizenSentKey(
                    entry.citizenName(), entry.stack().getItem()));
        ColonyLinkWandLinkableHandler.pruneSentRequestKeys(wandStack, activeCitizenKeys,
                ColonyLinkWandLinkableHandler.SENT_PREFIX_CITIZEN);

        PacketDistributor.sendToPlayer(player, new CitizensPacket(result));
    }

    // ── v1.6.2 — throttle state for the ticker Citizens tab ────────────────────
    private static final java.util.Map<java.util.UUID, Long> LAST_SIG =
            new java.util.concurrent.ConcurrentHashMap<>();

    // ── A2 — per-player cooldown for MANUAL requests only ──────────────────────
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
            sig = (sig ^ h) * P;
        }
        return sig;
    }

    private static ItemStack findWandInInventory(ServerPlayer player)
    {
        // Delegate to the shared implementation that also checks Curios slots.
        return ColonyLinkServerTicker.findWandInInventory(player);
    }
}