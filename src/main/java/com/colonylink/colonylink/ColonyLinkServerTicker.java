package com.colonylink.colonylink;

import appeng.api.implementations.blockentities.IWirelessAccessPoint;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.networking.storage.IStorageService;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.KeyCounter;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.request.RequestState;
import com.minecolonies.api.colony.requestsystem.requestable.IDeliverable;
import com.minecolonies.core.colony.buildings.AbstractBuildingStructureBuilder;
import com.minecolonies.core.colony.buildings.utils.BuildingBuilderResource;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ColonyLinkServerTicker
{
    // ── Registre des WarehouseLinkTerminalPart actives ────────────────────────
    // Les Parts s'enregistrent ici quand elles ont des viewers (GUI ouverts).
    // Le ticker appelle Part.serverTick() périodiquement pour les syncs.
    private static final java.util.Set<WarehouseLinkTerminalPart> activeTerminalParts =
            java.util.concurrent.ConcurrentHashMap.newKeySet();

    // Registre global — toutes les Parts placées dans le monde (avec ou sans viewer).
    // Utilisé par DomumQueuePacket pour trouver un terminal même si le GUI est fermé.
    private static final java.util.Set<WarehouseLinkTerminalPart> allTerminalParts =
            java.util.concurrent.ConcurrentHashMap.newKeySet();

    public static void registerTerminalPart(WarehouseLinkTerminalPart part)
    { activeTerminalParts.add(part); }

    public static void unregisterTerminalPart(WarehouseLinkTerminalPart part)
    { activeTerminalParts.remove(part); }

    public static void registerTerminalPartGlobal(WarehouseLinkTerminalPart part)
    { allTerminalParts.add(part); }

    public static void unregisterTerminalPartGlobal(WarehouseLinkTerminalPart part)
    { allTerminalParts.remove(part); }

    /**
     * v1.6.1 — Tous les WarehouseLinkTerminalPart VIVANTS d'une grille ME donnee.
     * Utilise par la queue Domum partagee (seed / persistance NBT / broadcast). Exclut les
     * instances fantomes (noeud null/inactif, laissees par un unload de chunk).
     */
    public static java.util.List<WarehouseLinkTerminalPart> findLiveTerminalsForGrid(
            net.minecraft.server.level.ServerLevel level, appeng.api.networking.IGrid grid)
    {
        java.util.List<WarehouseLinkTerminalPart> result = new java.util.ArrayList<>();
        if (grid == null) return result;
        java.util.Set<WarehouseLinkTerminalPart> seen = new java.util.HashSet<>();
        for (WarehouseLinkTerminalPart part : activeTerminalParts)
            if (isLiveTerminal(part, level) && isOnGrid(part, grid) && seen.add(part)) result.add(part);
        for (WarehouseLinkTerminalPart part : allTerminalParts)
            if (isLiveTerminal(part, level) && isOnGrid(part, grid) && seen.add(part)) result.add(part);
        return result;
    }

    private static boolean isLiveTerminal(WarehouseLinkTerminalPart part,
                                          net.minecraft.server.level.ServerLevel level)
    {
        if (part == null || part.getLevel() != level) return false;
        var node = part.getMainNode().getNode();
        return node != null && node.isActive();
    }

    private static boolean isOnGrid(WarehouseLinkTerminalPart part, appeng.api.networking.IGrid grid)
    {
        var node = part.getMainNode().getNode();
        return node != null && node.getGrid() == grid;
    }
    private record ViewerState(BlockPos builderPos, int activeTabIndex, int colonyId, long lastContentSig) {
        ViewerState(BlockPos builderPos, int activeTabIndex, int colonyId) {
            this(builderPos, activeTabIndex, colonyId, Long.MIN_VALUE);
        }
    }

    private static final Map<UUID, ViewerState> activeViewers = new ConcurrentHashMap<>();

    private static final java.util.concurrent.atomic.AtomicInteger tickCounter
            = new java.util.concurrent.atomic.AtomicInteger(0);

    private static int getTickerInterval()
    { return ColonyLinkConfig.TICKER_INTERVAL_TICKS.get(); }

    // ── AE2 viewer management ─────────────────────────────────────────────────

    public static void addViewer(UUID playerUUID, BlockPos builderPos, int activeTabIndex)
    { activeViewers.put(playerUUID, new ViewerState(builderPos, activeTabIndex, -1)); }

    public static void removeViewer(UUID playerUUID)
    { activeViewers.remove(playerUUID); }

    // ── Logout ────────────────────────────────────────────────────────────────

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event)
    {
        if (event.getEntity() instanceof ServerPlayer sp)
        {
            activeViewers.remove(sp.getUUID());
            CitizensScanHandler.forget(sp.getUUID()); // v1.6.2 — drop throttle state
        }
    }

    // ── Immediate update ──────────────────────────────────────────────────────

    public static void sendImmediateUpdate(ServerPlayer player, BlockPos builderPos, int activeTabIndex)
    {
        // Invalider la signature pour forcer le renvoi immédiat
        ViewerState vs = activeViewers.get(player.getUUID());
        if (vs != null)
            activeViewers.put(player.getUUID(),
                    new ViewerState(vs.builderPos(), vs.activeTabIndex(), vs.colonyId(), Long.MIN_VALUE));

        ItemStack wand = findWandInInventory(player);
        if (wand != null) sendFullUpdate(player, builderPos, activeTabIndex, wand);
    }

    // ── Signature invalidation (external callers) ─────────────────────────────
    // v1.6.6 — force an immediate GUI resend for a player whose wand GUI is open.
    // ColonyLink's content signature (computeWandSig) is NOT tied to MineColonies'
    // building.markDirty(), so an out-of-band state change (e.g. cancelling a
    // request) would otherwise be swallowed by the throttle until the next natural
    // content change. Resets the stored signature to Long.MIN_VALUE and resends now,
    // using the viewer's own stored builderPos/activeTabIndex (never a client value).
    // No-op if the player has no open GUI (nothing to refresh).
    public static void invalidateSignature(ServerPlayer player)
    {
        ViewerState vs = activeViewers.get(player.getUUID());
        if (vs == null) return;
        activeViewers.put(player.getUUID(),
                new ViewerState(vs.builderPos(), vs.activeTabIndex(), vs.colonyId(), Long.MIN_VALUE));
        ItemStack wand = findWandInInventory(player);
        if (wand != null) sendFullUpdate(player, vs.builderPos(), vs.activeTabIndex(), wand);
    }

    // ── Server tick ───────────────────────────────────────────────────────────

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Pre event)
    {
        if (tickCounter.incrementAndGet() < getTickerInterval()) return;
        tickCounter.set(0);

        // Ticker les Parts actives (sync warehouse/ME vers les viewers)
        activeTerminalParts.removeIf(part -> {
            if (part.getLevel() == null) return true; // Part déchargée
            part.serverTick();
            return false;
        });

        List<UUID> toRemove = new ArrayList<>();

        activeViewers.forEach((uuid, state) ->
        {
            ServerPlayer player = event.getServer().getPlayerList().getPlayer(uuid);
            if (player == null) { toRemove.add(uuid); return; }

            ItemStack wand = findWandInInventory(player);

            // Drain RF passif
            if (wand != null)
            {
                long drain = ColonyLinkConfig.PASSIVE_DRAIN_RF.get();
                if (drain > 0)
                {
                    long stored = WandEnergyStorage.getStoredRF(wand);
                    WandEnergyStorage.setStoredRF(wand, Math.max(0, stored - drain));
                }
            }

            sendFullUpdate(player, state.builderPos(), state.activeTabIndex(), wand);
            sendTabCounts(player, state.activeTabIndex(), wand);
            // Refresh auto de la tab Citizens si elle est active (Integer.MAX_VALUE).
            // v1.6.2 — force=false: le scan complet n'est diffusé que si le contenu a changé.
            if (state.activeTabIndex() == Integer.MAX_VALUE)
                CitizensScanHandler.sendCitizensPacket(player, false);
        });

        toRemove.forEach(activeViewers::remove);

        // Badge hotbar : scan joueurs GUI fermé
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers())
        {
            if (activeViewers.containsKey(player.getUUID())) continue;
            ItemStack wand = findWandInInventory(player);
            if (wand != null && ColonyLinkWandLinkableHandler.isLinked(wand))
                sendTabCounts(player, -1, wand);
        }
    }

    // ── RF helpers ────────────────────────────────────────────────────────────

    public static boolean tryConsumeRF(ServerPlayer player, long amount)
    {
        if (amount <= 0) return true;
        ItemStack wand = findWandInInventory(player);
        if (wand == null) return false;
        return WandEnergyStorage.tryConsume(wand, amount);
    }

    // ── sendFullUpdate AE2 ────────────────────────────────────────────────────

    private static void sendFullUpdate(ServerPlayer player, BlockPos builderPos,
                                       int activeTabIndex, ItemStack wandStack)
    {
        ServerLevel level = player.serverLevel();

        if (wandStack == null || !ColonyLinkWandLinkableHandler.isLinked(wandStack)) return;

        long rfStored = WandEnergyStorage.getStoredRF(wandStack);
        long rfMax    = ColonyLinkConfig.WAND_RF_CAPACITY.get();

        List<BuilderEntry> allEntries = ColonyLinkWandLinkableHandler.getBuilderEntries(wandStack);

        IWirelessAccessPoint wap = getWap(wandStack, level);
        if (wap == null) return;

        IGrid grid = wap.getGrid();
        if (grid == null) return;

        // Cache colonyId
        IColony colony = null;
        ViewerState currentState = activeViewers.get(player.getUUID());
        if (currentState != null && currentState.colonyId() >= 0)
            colony = IColonyManager.getInstance().getColonyByWorld(currentState.colonyId(), level);
        if (colony == null)
        {
            colony = IColonyManager.getInstance().getClosestColony(level, builderPos);
            if (colony != null && currentState != null)
                activeViewers.put(player.getUUID(),
                        new ViewerState(currentState.builderPos(), currentState.activeTabIndex(), colony.getID()));
        }

        if (colony == null)
        {
            List<ColonyLinkPacket.BuilderTabMeta> tabMetas = ColonyLinkWand.buildTabMetas(allEntries);
            PacketDistributor.sendToPlayer(player, new ColonyLinkPacket(
                    new ArrayList<>(), builderPos, "N/A", "No colony", "N/A", "", 0,
                    "NOT_LINKED", ColonyLinkPacket.BuilderRequest.NONE,
                    false, false, tabMetas, activeTabIndex, rfStored, rfMax));
            return;
        }

        if (!colony.getPermissions().hasPermission(player, Action.ACCESS_HUTS))
        {
            activeViewers.remove(player.getUUID());
            player.sendSystemMessage(Component.translatable("colonylink.ticker.access_revoked"));
            return;
        }

        // v1.6.0 — server-side reconciliation of the builder sent-keys ("b|"),
        // across ALL builders linked to this wand (not just the active tab).
        // Runs every ticker interval while the GUI is open, and immediately at
        // GUI open via sendImmediateUpdate — the state is reconciled before the
        // player can see anything stale.
        pruneBuilderSentKeys(player, wandStack);

        IBuilding building = IColonyManager.getInstance().getBuilding(level, builderPos);
        if (building == null)
            for (IBuilding b : colony.getServerBuildingManager().getBuildings().values())
                if (b.getPosition().equals(builderPos)) { building = b; break; }

        if (!(building instanceof AbstractBuildingStructureBuilder bb))
        {
            List<ColonyLinkPacket.BuilderTabMeta> tabMetas = ColonyLinkWand.buildTabMetas(allEntries);
            PacketDistributor.sendToPlayer(player, new ColonyLinkPacket(
                    new ArrayList<>(), builderPos, "N/A", "N/A", "N/A", "", 0,
                    "NOT_LINKED", ColonyLinkPacket.BuilderRequest.NONE,
                    false, false, tabMetas, activeTabIndex, rfStored, rfMax));
            return;
        }

        String builderName = "N/A", workerStatus = "Idle", workerIdleReason = "";
        if (!bb.getAllAssignedCitizen().isEmpty())
        {
            var citizen = bb.getAllAssignedCitizen().iterator().next();
            builderName = citizen.getName();
            String[] statusResult = computeWorkerStatus(citizen, bb);
            workerStatus     = statusResult[0];
            workerIdleReason = statusResult[1];

            int st = Math.min(activeTabIndex, allEntries.size() - 1);
            if (st >= 0)
            {
                BuilderEntry cur = allEntries.get(st);
                if (cur.builderName().equals("N/A") || cur.builderName().equals("Builder"))
                {
                    String lbl = cur.buildingLabel();
                    var wo2 = bb.getWorkOrder();
                    if (wo2 != null) lbl = wo2.getDisplayName().getString();
                    allEntries.set(st, cur.withLabels(builderName, lbl));
                    ColonyLinkWandLinkableHandler.setBuilderEntries(wandStack, allEntries);
                }
            }
        }

        String buildingName = "N/A";
        var wo = bb.getWorkOrder();
        if (wo != null)
        {
            buildingName = wo.getDisplayName().getString();
            if (wo.getStage() != null) buildingName += " [" + wo.getStage().name() + "]";
        }

        boolean showCrafting  = ColonyLinkConfig.SHOW_CRAFTING_STATUS.get();
        boolean showNoPattern = ColonyLinkConfig.SHOW_NO_PATTERN_ITEMS.get();
        int maxDisplayed      = ColonyLinkConfig.MAX_RESOURCES_DISPLAYED.get();
        boolean toolUpgrade   = ColonyLinkConfig.ENABLE_TOOL_UPGRADE.get();

        ICraftingService cs = grid.getCraftingService();
        int cpus = 0; for (var cpu : cs.getCpus()) if (!cpu.isBusy()) cpus++;

        String rState = "N/A"; boolean hasCard = false, whPrio = false;
        BlockPos rPos = null;
        if (!allEntries.isEmpty())
        {
            // v1.6.2 — last-resort clamp (low + high): activeTabIndex is client-sourced
            // and must never index allEntries out of bounds, even if it slipped past the
            // GuiStatePacket sanitisation. Integer.MAX_VALUE (Citizens) collapses to size-1.
            int st = Math.max(0, Math.min(activeTabIndex, allEntries.size() - 1));
            BuilderEntry ae = allEntries.get(st);
            if (ae.hasRedirector()) rPos = ae.redirectorPos();
        }

        if (rPos != null)
        {
            var rbe = level.getBlockEntity(rPos);
            if (rbe instanceof ColonyLinkRedirectorBlockEntity r)
            {
                r.updateState();
                var rn = r.getManagedGridNode().getNode();
                rState = rn != null ? switch (r.getState()) {
                    case STANDBY    -> "STANDBY";
                    case NOT_LINKED -> "NOT_LINKED";
                    default         -> "LINKED";
                } : "NOT_LINKED";
                hasCard = r.hasWarehouseCard();
                whPrio  = r.isWarehousePriority();
            }
            else rState = "NOT_LINKED";
        }

        IStorageService ss = grid.getStorageService();
        KeyCounter inv = ss.getCachedInventory();
        BlockPos safeR = rPos != null ? rPos : BlockPos.ZERO;
        int buildingLevel = bb.getBuildingLevel();

        // v1.6.0 — WAREHOUSE delivery mode: resources already sent to the
        // warehouse render as grey SENT_PENDING (Send disabled) until the
        // courier delivers and the key is pruned above. Read AFTER pruning so
        // the status reflects the reconciled state. In BUILDER mode (default)
        // sentKeys stays empty and SENT_PENDING can never appear.
        boolean warehouseMode =
                ColonyLinkConfig.SEND_TARGET.get() == ColonyLinkConfig.SendTarget.WAREHOUSE;
        java.util.Set<String> sentKeys = warehouseMode
                ? ColonyLinkWandLinkableHandler.getSentRequestKeys(wandStack)
                : java.util.Collections.emptySet();

        Map<String, BuildingBuilderResource> needed = bb.getNeededResources();
        List<ColonyLinkPacket.ResourceEntry> entries = new ArrayList<>();
        if (needed != null)
        {
            for (var res : needed.values())
            {
                if (entries.size() >= maxDisplayed) break;
                ItemStack st2 = res.getItemStack();
                int miss = res.getAmount() - res.getAvailable();
                if (miss <= 0) continue;

                // v1.6.0 — no tool substitution in WAREHOUSE mode: the courier
                // fulfills the request, so ColonyLink sends exactly what was
                // requested (a substituted tool may not match the request and
                // would strand in the warehouse). Rows show the original tool.
                // v1.6.4 — list substitution is now opt-in (tool_substitution_in_list,
                // default false): only the Priority Request line substitutes by default.
                if (!warehouseMode && toolUpgrade
                        && ColonyLinkConfig.TOOL_SUBSTITUTION_IN_LIST.get()
                        && BuilderToolHelper.isTool(st2))
                {
                    BuilderToolHelper.SubstituteResult sub =
                            BuilderToolHelper.findBestTool(st2, buildingLevel, inv, cs);
                    if (sub.action() != BuilderToolHelper.SubstituteAction.NONE)
                    {
                        ItemStack displayStack = sub.displayStack().copy();
                        displayStack.setCount(Math.min(miss, 64));
                        ResourceStatus stat;
                        AEItemKey subAEKey = AEItemKey.of(sub.displayStack());
                        long inSt = subAEKey != null ? inv.get(subAEKey) : 0L;
                        if (sub.action() == BuilderToolHelper.SubstituteAction.CRAFT)
                            stat = ResourceStatus.CRAFTABLE;
                        else if (inSt >= miss) stat = ResourceStatus.AVAILABLE;
                        else if (subAEKey != null && cs.isRequesting(subAEKey)) stat = ResourceStatus.CRAFTING;
                        else stat = ResourceStatus.AVAILABLE;
                        if (!showCrafting && stat == ResourceStatus.CRAFTING) continue;
                        if (!showNoPattern && stat == ResourceStatus.NO_PATTERN) continue;
                        List<Component> tooltip = buildToolSubstituteTooltip(
                                st2, sub.displayStack(), stat, miss, inSt, buildingLevel);
                        entries.add(new ColonyLinkPacket.ResourceEntry(
                                displayStack, stat, miss, false, safeR, tooltip));
                        continue;
                    }
                }

                ItemStack ms = st2.copy(); ms.setCount(Math.min(miss, 64));

                // v1.4.2 — Domum items: flow AE2 standard via ICraftingProvider
                // Le Redirector expose les DomumPatternDetails → cs.isCraftable() retourne true
                // si un pattern correspondant est dans un Redirector connecté.
                if (DomumCraftHandler.isDomumItem(st2))
                {
                    AEItemKey dk = AEItemKey.of(st2);
                    long dInSt = inv.get(dk);
                    ResourceStatus dStat;
                    if (dInSt >= miss)            dStat = ResourceStatus.AVAILABLE;
                    else if (cs.isRequesting(dk)) dStat = ResourceStatus.CRAFTING;
                    else if (cs.isCraftable(dk))  dStat = ResourceStatus.CRAFTABLE;
                    else                          dStat = ResourceStatus.NO_PATTERN;

                    // v1.6.0 — SENT_PENDING override, BEFORE the display filters:
                    // a pending line is essential state and must never be hidden.
                    // CRAFTING keeps priority (craft progress is not mode-related).
                    if (warehouseMode && dStat != ResourceStatus.CRAFTING
                            && ColonyLinkWandLinkableHandler.hasBuilderSentKey(sentKeys, builderPos, st2.getItem()))
                        dStat = ResourceStatus.SENT_PENDING;

                    if (!showCrafting && dStat == ResourceStatus.CRAFTING) continue;
                    if (!showNoPattern && dStat == ResourceStatus.NO_PATTERN) continue;
                    entries.add(new ColonyLinkPacket.ResourceEntry(
                            ms, dStat, miss, true, safeR,
                            buildDomumTooltip(st2, dStat, miss)));
                    continue;
                }

                AEItemKey k = AEItemKey.of(st2); long inSt = inv.get(k);
                ResourceStatus stat;
                if (inSt >= miss)            stat = ResourceStatus.AVAILABLE;
                else if (cs.isRequesting(k)) stat = ResourceStatus.CRAFTING;
                else if (cs.isCraftable(k))  stat = ResourceStatus.CRAFTABLE;
                else                         stat = ResourceStatus.NO_PATTERN;

                // v1.6.0 — SENT_PENDING override (see the Domum branch above).
                if (warehouseMode && stat != ResourceStatus.CRAFTING
                        && ColonyLinkWandLinkableHandler.hasBuilderSentKey(sentKeys, builderPos, st2.getItem()))
                    stat = ResourceStatus.SENT_PENDING;

                if (!showCrafting && stat == ResourceStatus.CRAFTING) continue;
                if (!showNoPattern && stat == ResourceStatus.NO_PATTERN) continue;

                entries.add(new ColonyLinkPacket.ResourceEntry(
                        ms, stat, miss, false, safeR, buildStandardTooltip(st2, stat, miss, inSt)));
            }
        }

        ColonyLinkPacket.BuilderRequest req = fetchBuilderRequest(bb, inv, cs, grid, safeR,
                buildingLevel, toolUpgrade, level, warehouseMode, sentKeys, builderPos);
        List<ColonyLinkPacket.BuilderTabMeta> tabMetas = ColonyLinkWand.buildTabMetas(allEntries);

        // Throttle : ne pas renvoyer si le contenu n'a pas changé
        // (RF exclu de la signature — change à chaque tick via drain passif)
        long newSig = computeWandSig(entries, builderName, buildingName, workerStatus,
                cpus, rState, hasCard, whPrio, activeTabIndex, req.cancellable());
        ViewerState vs = activeViewers.get(player.getUUID());
        if (vs != null && vs.lastContentSig() == newSig) return; // rien de changé
        if (vs != null)
            activeViewers.put(player.getUUID(),
                    new ViewerState(vs.builderPos(), vs.activeTabIndex(), vs.colonyId(), newSig));

        PacketDistributor.sendToPlayer(player, new ColonyLinkPacket(
                entries, builderPos, builderName, buildingName, workerStatus, workerIdleReason,
                cpus, rState, req, hasCard, whPrio,
                tabMetas, activeTabIndex, rfStored, rfMax));
    }

    // ── Signature de contenu wand (throttle sendFullUpdate) ───────────────────
    // FNV-1a fold sur le contenu visible. RF (rfStored/rfMax) volontairement exclu :
    // il change à chaque tick via le drain passif et ferait échouer le throttle.
    private static long computeWandSig(
            List<ColonyLinkPacket.ResourceEntry> entries,
            String builderName, String buildingName, String workerStatus,
            int cpus, String rState, boolean hasCard, boolean whPrio, int activeTabIndex,
            boolean reqCancellable)
    {
        long sig = 0xcbf29ce484222325L;
        final long P = 0x100000001b3L;
        for (var e : entries)
        {
            int h = Item.getId(e.stack().getItem());
            h = 31 * h + e.status().ordinal();
            h = 31 * h + e.realCount();
            sig = (sig ^ h) * P;
        }
        sig = (sig ^ builderName.hashCode()) * P;
        sig = (sig ^ buildingName.hashCode()) * P;
        sig = (sig ^ workerStatus.hashCode()) * P;
        sig = (sig ^ cpus) * P;
        sig = (sig ^ rState.hashCode()) * P;
        sig = (sig ^ (hasCard ? 1L : 0L)) * P;
        sig = (sig ^ (whPrio ? 1L : 0L)) * P;
        sig = (sig ^ activeTabIndex) * P;
        // v1.6.6 — fold the priority line's cancellable flag so a pass-1↔pass-2
        // transition with an otherwise identical displayed item/count/status still
        // forces a resend (else the button's clickability could stay stale).
        sig = (sig ^ (reqCancellable ? 1L : 0L)) * P;
        return sig;
    }

    // ── fetchBuilderRequest (AE2) ─────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private static ColonyLinkPacket.BuilderRequest fetchBuilderRequest(
            AbstractBuildingStructureBuilder bb, KeyCounter inv, ICraftingService cs,
            IGrid grid, BlockPos rPos, int buildingLevel, boolean toolUpgrade, ServerLevel level,
            boolean warehouseMode, java.util.Set<String> sentKeys, BlockPos builderPos)
    {
        // Passe 1 : getOpenRequests
        if (!bb.getAllAssignedCitizen().isEmpty())
        {
            var citizen = bb.getAllAssignedCitizen().iterator().next();
            try
            {
                var reqs = bb.getOpenRequests(citizen.getId());
                if (reqs != null)
                {
                    for (IRequest<?> req : reqs)
                    {
                        if (req.getState() == RequestState.CANCELLED
                                || req.getState() == RequestState.OVERRULED) continue;
                        if (!(req.getRequest() instanceof IDeliverable del)) continue;
                        ItemStack rs = ItemStack.EMPTY;
                        var ds = req.getDisplayStacks();
                        if (ds != null) for (ItemStack s : ds) if (!s.isEmpty()) { rs = s; break; }
                        if (rs.isEmpty()) continue;
                        int cnt = del.getCount(); if (cnt <= 0) cnt = 1;
                        ItemStack disp = rs.copy(); disp.setCount(Math.min(cnt, 64));

                        if (DomumCraftHandler.isDomumItem(rs))
                        {
                            AEItemKey dk = AEItemKey.of(rs);
                            long dInSt = inv.get(dk);
                            ResourceStatus dStat;
                            if (dInSt >= cnt)             dStat = ResourceStatus.AVAILABLE;
                            else if (cs.isRequesting(dk)) dStat = ResourceStatus.CRAFTING;
                            else if (cs.isCraftable(dk))  dStat = ResourceStatus.CRAFTABLE;
                            else                          dStat = ResourceStatus.NO_PATTERN;
                            // v1.6.2 — Priority Request line: BOTH modes. Do NOT re-gate on
                            // warehouseMode (a re-fetch silently reverted this once).
                            if (dStat != ResourceStatus.CRAFTING
                                    && ColonyLinkWandLinkableHandler.hasBuilderSentKey(sentKeys, builderPos, rs.getItem()))
                                dStat = ResourceStatus.SENT_PENDING;
                            return new ColonyLinkPacket.BuilderRequest(
                                    disp, cnt, dStat, rPos,
                                    buildDomumTooltip(rs, dStat, cnt), true); // pass 1 → cancellable
                        }

                        // v1.6.0 — no tool substitution in WAREHOUSE mode (courier fidelity).
                        if (!warehouseMode && toolUpgrade && BuilderToolHelper.isTool(rs))
                        {
                            BuilderToolHelper.SubstituteResult sub =
                                    BuilderToolHelper.findBestTool(rs, buildingLevel, inv, cs);
                            if (sub.action() != BuilderToolHelper.SubstituteAction.NONE)
                            {
                                ItemStack substDisp = sub.displayStack().copy();
                                substDisp.setCount(Math.min(cnt, 64));
                                AEItemKey substKey = AEItemKey.of(sub.displayStack());
                                long substInSt = substKey != null ? inv.get(substKey) : 0L;
                                ResourceStatus substSt;
                                if (sub.action() == BuilderToolHelper.SubstituteAction.CRAFT)
                                    substSt = ResourceStatus.CRAFTABLE;
                                else if (substInSt >= cnt) substSt = ResourceStatus.AVAILABLE;
                                else if (substKey != null && cs.isRequesting(substKey)) substSt = ResourceStatus.CRAFTING;
                                else substSt = ResourceStatus.AVAILABLE;
                                return new ColonyLinkPacket.BuilderRequest(substDisp, cnt, substSt, rPos,
                                        buildToolSubstituteTooltip(rs, sub.displayStack(), substSt, cnt, substInSt, buildingLevel), true); // pass 1 → cancellable
                            }
                        }

                        AEItemKey k = AEItemKey.of(rs); long inSt = inv.get(k);
                        ResourceStatus st;
                        if (inSt >= cnt)             st = ResourceStatus.AVAILABLE;
                        else if (cs.isRequesting(k)) st = ResourceStatus.CRAFTING;
                        else if (cs.isCraftable(k))  st = ResourceStatus.CRAFTABLE;
                        else                         st = ResourceStatus.NO_PATTERN;

                        // v1.6.2 — Priority Request line: BOTH modes. Do NOT re-gate on
                        // warehouseMode (a re-fetch silently reverted this once).
                        if (st != ResourceStatus.CRAFTING
                                && ColonyLinkWandLinkableHandler.hasBuilderSentKey(sentKeys, builderPos, rs.getItem()))
                            st = ResourceStatus.SENT_PENDING;

                        return new ColonyLinkPacket.BuilderRequest(disp, cnt, st, rPos,
                                buildStandardTooltip(rs, st, cnt, inSt), true); // pass 1 → cancellable
                    }
                }
            }
            catch (Exception e) { ColonyLink.LOGGER.debug("[ColonyLink] fetchBuilderRequest pass1: {}", e.getMessage()); }
        }

        // Passe 2 : neededResources trié (fallback)
        Map<String, BuildingBuilderResource> needed = bb.getNeededResources();
        if (needed != null)
        {
            java.util.List<BuildingBuilderResource> sorted = new java.util.ArrayList<>(needed.values());
            sorted.sort((a, b) -> {
                int missA = a.getAmount() - a.getAvailable();
                int missB = b.getAmount() - b.getAvailable();
                if (missA <= 0 && missB <= 0) return 0;
                if (missA <= 0) return 1;
                if (missB <= 0) return -1;
                boolean aZero = a.getAvailable() == 0;
                boolean bZero = b.getAvailable() == 0;
                if (aZero != bZero) return aZero ? -1 : 1;
                return Integer.compare(missB, missA);
            });
            for (var res : sorted)
            {
                ItemStack st2 = res.getItemStack();
                int miss = res.getAmount() - res.getAvailable();
                if (miss <= 0) continue;
                ItemStack disp = st2.copy(); disp.setCount(Math.min(miss, 64));

                if (DomumCraftHandler.isDomumItem(st2))
                {
                    AEItemKey dk = AEItemKey.of(st2);
                    long dInSt = inv.get(dk);
                    ResourceStatus dStat;
                    if (dInSt >= miss)            dStat = ResourceStatus.AVAILABLE;
                    else if (cs.isRequesting(dk)) dStat = ResourceStatus.CRAFTING;
                    else if (cs.isCraftable(dk))  dStat = ResourceStatus.CRAFTABLE;
                    else                          dStat = ResourceStatus.NO_PATTERN;
                    if (warehouseMode && dStat != ResourceStatus.CRAFTING
                            && ColonyLinkWandLinkableHandler.hasBuilderSentKey(sentKeys, builderPos, st2.getItem()))
                        dStat = ResourceStatus.SENT_PENDING;
                    return new ColonyLinkPacket.BuilderRequest(
                            disp, miss, dStat, rPos,
                            buildDomumTooltip(st2, dStat, miss), false); // pass 2 → not cancellable (no formal request)
                }

                // v1.6.0 — no tool substitution in WAREHOUSE mode (courier fidelity).
                if (!warehouseMode && toolUpgrade && BuilderToolHelper.isTool(st2))
                {
                    BuilderToolHelper.SubstituteResult sub =
                            BuilderToolHelper.findBestTool(st2, buildingLevel, inv, cs);
                    if (sub.action() != BuilderToolHelper.SubstituteAction.NONE)
                    {
                        ItemStack substDisp = sub.displayStack().copy();
                        substDisp.setCount(Math.min(miss, 64));
                        AEItemKey substKey = AEItemKey.of(sub.displayStack());
                        long substInSt = substKey != null ? inv.get(substKey) : 0L;
                        ResourceStatus substSt;
                        if (sub.action() == BuilderToolHelper.SubstituteAction.CRAFT)
                            substSt = ResourceStatus.CRAFTABLE;
                        else if (substInSt >= miss) substSt = ResourceStatus.AVAILABLE;
                        else if (substKey != null && cs.isRequesting(substKey)) substSt = ResourceStatus.CRAFTING;
                        else substSt = ResourceStatus.AVAILABLE;
                        return new ColonyLinkPacket.BuilderRequest(substDisp, miss, substSt, rPos,
                                buildToolSubstituteTooltip(st2, sub.displayStack(), substSt, miss, substInSt, buildingLevel), false); // pass 2 → not cancellable (no formal request)
                    }
                }

                AEItemKey k = AEItemKey.of(st2); long inSt = inv.get(k);
                ResourceStatus st;
                if (inSt >= miss)            st = ResourceStatus.AVAILABLE;
                else if (cs.isRequesting(k)) st = ResourceStatus.CRAFTING;
                else if (cs.isCraftable(k))  st = ResourceStatus.CRAFTABLE;
                else                         st = ResourceStatus.NO_PATTERN;

                if (warehouseMode && st != ResourceStatus.CRAFTING
                        && ColonyLinkWandLinkableHandler.hasBuilderSentKey(sentKeys, builderPos, st2.getItem()))
                    st = ResourceStatus.SENT_PENDING;

                return new ColonyLinkPacket.BuilderRequest(disp, miss, st, rPos,
                        buildStandardTooltip(st2, st, miss, inSt), false); // pass 2 → not cancellable (no formal request)
            }
        }

        return ColonyLinkPacket.BuilderRequest.NONE;
    }

    // ── Worker status ─────────────────────────────────────────────────────────

    private static String[] computeWorkerStatus(ICitizenData citizen, AbstractBuildingStructureBuilder bb)
    {
        if (bb.getWorkOrder() == null) return new String[]{"Idle", ""};

        // ── Priorité 1 : Hungry — vérifier AVANT les open requests ──────────────
        // Un builder hungry a toujours des open requests → sans ce check prioritaire,
        // le statut "Working" masque systématiquement le statut "Hungry".
        try
        {
            if (citizen.getSaturation() < 3.0)
                return new String[]{"Hungry", Component.translatable("colonylink.reason.needs_food").getString()};
        }
        catch (Exception ignored) {}

        try
        {
            String jobState = citizen.getJobStatus().name().toLowerCase();
            if (jobState.contains("eat") || jobState.contains("food") || jobState.contains("hungry"))
                return new String[]{"Hungry", Component.translatable("colonylink.reason.eating").getString()};
            if (jobState.contains("sleep") || jobState.contains("rest"))
                return new String[]{"Sleeping", Component.translatable("colonylink.reason.sleeping").getString()};
            if (jobState.contains("weather") || jobState.contains("rain"))
                return new String[]{"Bad weather", Component.translatable("colonylink.reason.bad_weather").getString()};
            if (jobState.contains("sick") || jobState.contains("disease"))
                return new String[]{"Sick", Component.translatable("colonylink.reason.sick").getString()};
            if (jobState.contains("mourn"))
                return new String[]{"Mourning", Component.translatable("colonylink.reason.mourning").getString()};
            if (jobState.contains("raid"))
                return new String[]{"Raided!", Component.translatable("colonylink.reason.raided").getString()};
        }
        catch (Exception e) { ColonyLink.LOGGER.debug("[ColonyLink] computeWorkerStatus jobState: {}", e.getMessage()); }

        try
        {
            var vs = citizen.getStatus();
            if (vs != null)
            {
                String key = vs.getTranslationKey().toLowerCase();
                if (key.contains("eat") || key.contains("food") || key.contains("hungry"))
                    return new String[]{"Hungry", Component.translatable("colonylink.reason.eating").getString()};
                if (key.contains("sleep") || key.contains("rest"))
                    return new String[]{"Sleeping", Component.translatable("colonylink.reason.sleeping").getString()};
                if (key.contains("weather") || key.contains("rain"))
                    return new String[]{"Bad weather", Component.translatable("colonylink.reason.bad_weather").getString()};
                if (key.contains("sick") || key.contains("disease"))
                    return new String[]{"Sick", Component.translatable("colonylink.reason.sick").getString()};
                if (key.contains("mourn"))
                    return new String[]{"Mourning", Component.translatable("colonylink.reason.mourning").getString()};
                if (key.contains("raid"))
                    return new String[]{"Raided!", Component.translatable("colonylink.reason.raided").getString()};
                if (key.contains("house") || key.contains("home"))
                    return new String[]{"No home", Component.translatable("colonylink.reason.no_home").getString()};
                if (key.contains("work") || key.contains("build") || key.contains("place"))
                    return new String[]{"Working", ""};
                String translated = Component.translatable(vs.getTranslationKey()).getString();
                if (!translated.equals(vs.getTranslationKey()) && !translated.isBlank())
                    return new String[]{translated, ""};
            }
        }
        catch (Exception e) { ColonyLink.LOGGER.debug("[ColonyLink] computeWorkerStatus status: {}", e.getMessage()); }

        // ── Priorité 2 : Open requests ───────────────────────────────────────────
        try
        {
            var reqs = bb.getOpenRequests(citizen.getId());
            if (reqs != null)
            {
                for (var r : reqs)
                {
                    if (r.getState() == RequestState.CANCELLED
                            || r.getState() == RequestState.OVERRULED) continue;
                    if (!(r.getRequest() instanceof IDeliverable del)) continue;
                    ItemStack rs = ItemStack.EMPTY;
                    var ds = r.getDisplayStacks();
                    if (ds != null) for (ItemStack s : ds) if (!s.isEmpty()) { rs = s; break; }
                    if (rs.isEmpty()) continue;
                    int cnt = del.getCount(); if (cnt <= 0) cnt = 1;
                    return new String[]{"Working",
                            Component.translatable("colonylink.reason.needs", cnt, rs.getDisplayName().getString()).getString()};
                }
            }
        }
        catch (Exception e) { ColonyLink.LOGGER.debug("[ColonyLink] computeWorkerStatus requests: {}", e.getMessage()); }

        return new String[]{"Working", ""};
    }

    // ── Tooltips ──────────────────────────────────────────────────────────────

    private static List<Component> buildToolSubstituteTooltip(ItemStack original, ItemStack substitute,
                                                              ResourceStatus stat, int missing, long inStorage, int buildingLevel)
    {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable("colonylink.avail.tool_upgrade", buildingLevel));
        lines.add(Component.translatable("colonylink.avail.tool_requested", original.getDisplayName()));
        lines.add(Component.translatable("colonylink.avail.tool_best", substitute.getDisplayName()));
        switch (stat)
        {
            case AVAILABLE    -> lines.add(Component.translatable("colonylink.avail.tool_ready", inStorage));
            case CRAFTABLE    -> lines.add(Component.translatable("colonylink.avail.craftable_ae2"));
            case CRAFTING     -> lines.add(Component.translatable("colonylink.avail.crafting"));
            case SENT_PENDING -> { lines.add(Component.translatable("colonylink.avail.sent_pending"));
                lines.add(Component.translatable("colonylink.avail.sent_pending_wait")); }
            default           -> lines.add(Component.translatable("colonylink.avail.no_pattern_short"));
        }
        lines.add(Component.translatable("colonylink.avail.needed", missing));
        return lines;
    }



    /**
     * Tooltip pour les lignes Domum dans le Clipboard.
     * Affiche le statut + les matériaux bruts nécessaires.
     */
    private static List<Component> buildDomumTooltip(ItemStack stack, ResourceStatus status, int missing)
    {
        List<Component> lines = new ArrayList<>();

        switch (status)
        {
            case NO_PATTERN -> { lines.add(Component.translatable("colonylink.avail.domum_no_pattern"));
                lines.add(Component.translatable("colonylink.avail.domum_click_send")); }
            case CRAFTABLE  -> lines.add(Component.translatable("colonylink.avail.domum_craftable"));
            case CRAFTING   -> lines.add(Component.translatable("colonylink.avail.domum_crafting"));
            case SENT_PENDING -> { lines.add(Component.translatable("colonylink.avail.sent_pending"));
                lines.add(Component.translatable("colonylink.avail.sent_pending_wait")); }
            default         -> {}
        }
        lines.add(Component.translatable("colonylink.avail.needed", missing));

        net.minecraft.world.item.component.BlockItemStateProperties blockState =
                stack.get(net.minecraft.core.component.DataComponents.BLOCK_STATE);
        if (blockState != null && !blockState.properties().isEmpty())
        {
            for (var entry : blockState.properties().entrySet())
                lines.add(Component.literal("§7  " + entry.getKey() + ": §f" + entry.getValue()));
        }

        if (stack.getItem() instanceof net.minecraft.world.item.BlockItem bi)
        {
            net.minecraft.world.level.block.Block block = bi.getBlock();
            if (block instanceof com.ldtteam.domumornamentum.block.IMateriallyTexturedBlock tb)
            {
                com.ldtteam.domumornamentum.client.model.data.MaterialTextureData td =
                        com.ldtteam.domumornamentum.client.model.data.MaterialTextureData
                                .readFromItemStack(stack);
                lines.add(Component.translatable("colonylink.redir.materials"));
                for (var comp : tb.getComponents())
                {
                    net.minecraft.world.level.block.Block mat =
                            td.getTexturedComponents().get(comp.getId());
                    if (mat != null)
                        lines.add(Component.translatable("colonylink.avail.domum_mat",
                                new net.minecraft.world.item.ItemStack(mat).getDisplayName()));
                    else if (!comp.isOptional())
                        lines.add(Component.translatable("colonylink.domum_item.missing", comp.getId().getPath()));
                }
            }
        }
        return lines;
    }

    private static List<Component> buildStandardTooltip(ItemStack stack, ResourceStatus status,
                                                        int missing, long inStorage)
    {
        List<Component> lines = new ArrayList<>();
        Component n = stack.getDisplayName();
        switch (status)
        {
            case NO_PATTERN -> { lines.add(Component.translatable("colonylink.avail.std_no_pattern")); lines.add(Component.translatable("colonylink.avail.std_name", n));
                lines.add(Component.translatable("colonylink.avail.needed", missing)); lines.add(Component.translatable("colonylink.avail.in_me", inStorage)); }
            case CRAFTABLE  -> { lines.add(Component.translatable("colonylink.avail.std_craftable")); lines.add(Component.translatable("colonylink.avail.std_name", n));
                lines.add(Component.translatable("colonylink.avail.needed", missing)); lines.add(Component.translatable("colonylink.avail.in_me", inStorage));
                lines.add(Component.translatable("colonylink.avail.std_missing_primary")); }
            case AVAILABLE  -> { lines.add(Component.translatable("colonylink.avail.std_available")); lines.add(Component.translatable("colonylink.avail.std_name", n));
                lines.add(Component.translatable("colonylink.avail.needed", missing)); lines.add(Component.translatable("colonylink.avail.in_me", inStorage)); }
            case CRAFTING   -> { lines.add(Component.translatable("colonylink.avail.std_crafting")); lines.add(Component.translatable("colonylink.avail.std_name", n));
                lines.add(Component.translatable("colonylink.avail.needed", missing)); }
            case MISSING    -> { lines.add(Component.translatable("colonylink.avail.std_missing_raw")); lines.add(Component.translatable("colonylink.avail.std_name", n));
                lines.add(Component.translatable("colonylink.avail.needed", missing)); }
            case SENT_PENDING -> { lines.add(Component.translatable("colonylink.avail.sent_pending")); lines.add(Component.translatable("colonylink.avail.std_name", n));
                lines.add(Component.translatable("colonylink.avail.sent_pending_wait")); lines.add(Component.translatable("colonylink.avail.needed", missing)); }
        }
        return lines;
    }

    // ── Tab counts ────────────────────────────────────────────────────────────

    private static void sendTabCounts(ServerPlayer player, int activeTabIndex, ItemStack wandStack)
    {
        if (wandStack == null) return;
        java.util.List<BuilderEntry> entries = ColonyLinkWandLinkableHandler.getBuilderEntries(wandStack);
        if (entries.size() <= 1) return;

        net.minecraft.server.level.ServerLevel level = player.serverLevel();
        java.util.List<Integer> counts = new java.util.ArrayList<>();

        for (int i = 0; i < entries.size(); i++)
        {
            if (i == activeTabIndex) { counts.add(-1); continue; }
            BuilderEntry entry = entries.get(i);
            int count = 0;
            try
            {
                var colony = com.minecolonies.api.colony.IColonyManager.getInstance()
                        .getClosestColony(level, entry.builderPos());
                if (colony != null)
                {
                    for (var b : colony.getServerBuildingManager().getBuildings().values())
                    {
                        if (b.getPosition().equals(entry.builderPos())
                                && b instanceof com.minecolonies.core.colony.buildings.AbstractBuildingStructureBuilder bb)
                        {
                            var needed = bb.getNeededResources();
                            if (needed != null)
                                for (var res : needed.values())
                                    if (res.getAmount() - res.getAvailable() > 0) count++;
                        }
                    }
                }
            }
            catch (Exception ignored) {}
            counts.add(count);
        }

        net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player,
                new TabCountsPacket(counts, activeTabIndex));
    }

    // ── Builder sent-key pruning (v1.6.0) ─────────────────────────────────────

    /**
     * Reconciles the builder sent-keys ("b|x,y,z|itemId|availableAtSend")
     * against the server truth, across ALL builders linked to the wand.
     *
     * A key is DROPPED when its builder resolves and:
     *   - the item is no longer in getNeededResources() AND matches no live
     *     open request of the assigned citizen              (request satisfied), or
     *   - missing <= 0                                      (request satisfied), or
     *   - it is a build material and 'available' differs from the stored
     *     baseline (a courier delivered something — the line re-arms so a
     *     partial send can be topped up), or
     *   - the builder is no longer linked to this wand, or the key is malformed.
     *
     * A key is KEPT (fail-safe) when its builder cannot be resolved: missing
     * dimension level, no colony, or a null needed map. An unloaded chunk does
     * NOT unresolve a MineColonies building (colony data is chunk-independent),
     * so "colony found but no builder building at that position" means the hut
     * is genuinely gone → dropped.
     *
     * Legacy unprefixed keys (pre-v1.6.0) are dropped by the prune call itself;
     * citizen keys ("c|") are never touched here (CitizensScanHandler owns them).
     */
    private static void pruneBuilderSentKeys(ServerPlayer player, ItemStack wandStack)
    {
        java.util.Set<String> stored = ColonyLinkWandLinkableHandler.getSentRequestKeys(wandStack);
        if (stored.isEmpty()) return;

        boolean hasBuilderKeys = false, hasLegacyKeys = false;
        for (String k : stored)
        {
            if (k.startsWith(ColonyLinkWandLinkableHandler.SENT_PREFIX_BUILDER)) hasBuilderKeys = true;
            else if (!k.startsWith(ColonyLinkWandLinkableHandler.SENT_PREFIX_CITIZEN)) hasLegacyKeys = true;
        }
        if (!hasBuilderKeys && !hasLegacyKeys) return;

        List<BuilderEntry> entries = ColonyLinkWandLinkableHandler.getBuilderEntries(wandStack);
        java.util.Set<String> keep = new java.util.HashSet<>();

        // Per-builder resolution caches.
        Map<BlockPos, AbstractBuildingStructureBuilder> resolvedBuilders = new java.util.HashMap<>();
        Map<BlockPos, java.util.Set<String>> openRequestIds = new java.util.HashMap<>();
        java.util.Set<BlockPos> unresolvable = new java.util.HashSet<>();
        java.util.Set<BlockPos> gone = new java.util.HashSet<>();

        for (String key : stored)
        {
            if (!key.startsWith(ColonyLinkWandLinkableHandler.SENT_PREFIX_BUILDER)) continue;

            BlockPos pos = ColonyLinkWandLinkableHandler.parseBuilderKeyPos(key);
            String itemId = ColonyLinkWandLinkableHandler.parseBuilderKeyItemId(key);
            int baseline = ColonyLinkWandLinkableHandler.parseBuilderKeyBaseline(key);
            if (pos == null || itemId == null || baseline < 0) continue; // malformed → drop

            BuilderEntry entry = null;
            for (BuilderEntry e : entries)
                if (e.builderPos().equals(pos)) { entry = e; break; }
            if (entry == null) continue; // builder unlinked from the wand → drop

            if (unresolvable.contains(pos)) { keep.add(key); continue; }
            if (gone.contains(pos)) continue;

            AbstractBuildingStructureBuilder bb = resolvedBuilders.get(pos);
            if (bb == null)
            {
                bb = resolveBuilder(player, entry);
                if (bb == null)
                {
                    // Could not resolve — distinguish "hut gone" from "can't tell".
                    if (builderHutGone(player, entry))
                    {
                        gone.add(pos);
                        continue; // drop this builder's keys
                    }
                    unresolvable.add(pos);
                    keep.add(key); // fail-safe: keep
                    continue;
                }
                resolvedBuilders.put(pos, bb);
            }

            Map<String, BuildingBuilderResource> needed = bb.getNeededResources();
            if (needed == null) { keep.add(key); continue; } // fail-safe: keep

            BuildingBuilderResource match = null;
            for (BuildingBuilderResource res : needed.values())
            {
                if (net.minecraft.core.registries.BuiltInRegistries.ITEM
                        .getKey(res.getItemStack().getItem()).toString().equals(itemId))
                {
                    match = res;
                    break;
                }
            }
            if (match == null)
            {
                // Not a build material — keys created for open requests
                // (tools/armor/food) survive while a matching request is still
                // alive, and drop once the courier resolved it.
                if (!openRequestIds.containsKey(pos))
                    openRequestIds.put(pos, openRequestItemIds(bb));
                java.util.Set<String> openIds = openRequestIds.get(pos);
                if (openIds == null || openIds.contains(itemId)) keep.add(key); // null = can't tell → keep
                continue;
            }
            if (match.getAmount() - match.getAvailable() <= 0) continue; // satisfied → drop
            if (match.getAvailable() != baseline) continue;           // courier delivered → re-arm
            keep.add(key);
        }

        ColonyLinkWandLinkableHandler.pruneSentRequestKeys(wandStack, keep,
                ColonyLinkWandLinkableHandler.SENT_PREFIX_BUILDER);
    }

    /**
     * Resolves a linked builder building, honouring the dimension frozen at
     * pairing time (v1.4.9). Returns null when anything cannot be resolved —
     * the caller decides keep-vs-drop via builderHutGone.
     */
    private static AbstractBuildingStructureBuilder resolveBuilder(
            ServerPlayer player, BuilderEntry entry)
    {
        ServerLevel lvl = entry.dimension() != null
                ? player.server.getLevel(entry.dimension())
                : player.serverLevel();
        if (lvl == null) return null;

        IColony colony = IColonyManager.getInstance().getClosestColony(lvl, entry.builderPos());
        if (colony == null) return null;

        for (IBuilding b : colony.getServerBuildingManager().getBuildings().values())
        {
            if (b.getPosition().equals(entry.builderPos())
                    && b instanceof AbstractBuildingStructureBuilder bb)
                return bb;
        }
        return null;
    }

    /**
     * Item ids (display stacks) of the builder's live open requests, or null
     * when the request system throws — callers keep keys on null (fail-safe).
     */
    private static java.util.Set<String> openRequestItemIds(AbstractBuildingStructureBuilder bb)
    {
        try
        {
            java.util.Set<String> ids = new java.util.HashSet<>();
            if (bb.getAllAssignedCitizen().isEmpty()) return ids;
            var citizen = bb.getAllAssignedCitizen().iterator().next();
            var reqs = bb.getOpenRequests(citizen.getId());
            if (reqs == null) return ids;
            for (IRequest<?> req : reqs)
            {
                if (req.getState() == RequestState.CANCELLED
                        || req.getState() == RequestState.OVERRULED) continue;
                if (!(req.getRequest() instanceof IDeliverable)) continue;
                var ds = req.getDisplayStacks();
                if (ds == null) continue;
                for (ItemStack s : ds)
                    if (!s.isEmpty())
                        ids.add(net.minecraft.core.registries.BuiltInRegistries.ITEM
                                .getKey(s.getItem()).toString());
            }
            return ids;
        }
        catch (Exception e)
        {
            ColonyLink.LOGGER.debug("[ColonyLink] open-request id scan failed: {}", e.getMessage());
            return null;
        }
    }

    /**
     * True only when we can positively tell the builder hut no longer exists:
     * the colony resolves but has no structure-builder building at that
     * position. MineColonies building data is colony data (chunk-independent),
     * so this is reliable even with unloaded chunks.
     */
    private static boolean builderHutGone(ServerPlayer player, BuilderEntry entry)
    {
        ServerLevel lvl = entry.dimension() != null
                ? player.server.getLevel(entry.dimension())
                : player.serverLevel();
        if (lvl == null) return false;
        IColony colony = IColonyManager.getInstance().getClosestColony(lvl, entry.builderPos());
        if (colony == null) return false;
        for (IBuilding b : colony.getServerBuildingManager().getBuildings().values())
            if (b.getPosition().equals(entry.builderPos()))
                return !(b instanceof AbstractBuildingStructureBuilder);
        return true; // colony known, no building at that position → hut gone
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Finds the ColonyLink Wand in the player's inventory or, if Curios is
     * installed, also in curio slots. Inventory takes priority (checked first).
     * Returns null if not found anywhere.
     */
    static ItemStack findWandInInventory(ServerPlayer player)
    {
        // 1. Standard inventory (hotbar + main)
        for (ItemStack stack : player.getInventory().items)
            if (stack.getItem() instanceof ColonyLinkWand) return stack;

        // 2. Curio slots (optional — no-op if Curios not installed)
        ItemStack curio = ColonyLinkCuriosCompat.findWandInCurioSlots(player);
        if (!curio.isEmpty()) return curio;

        return null;
    }

    private static IWirelessAccessPoint getWap(ItemStack wandStack, ServerLevel level)
    {
        net.minecraft.core.GlobalPos lp = ColonyLinkWandLinkableHandler.getLinkedPos(wandStack);
        if (lp == null) return null;
        ServerLevel tl = level.getServer().getLevel(lp.dimension());
        if (tl == null) return null;
        var be = tl.getBlockEntity(lp.pos());
        if (be instanceof IWirelessAccessPoint wap) return wap;
        return null;
    }
}