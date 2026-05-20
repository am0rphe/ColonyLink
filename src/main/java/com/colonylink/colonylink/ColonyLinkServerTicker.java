package com.colonylink.colonylink;

import appeng.api.implementations.blockentities.IWirelessAccessPoint;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.networking.storage.IStorageService;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.KeyCounter;
import com.ldtteam.domumornamentum.block.IMateriallyTexturedBlock;
import com.ldtteam.domumornamentum.block.IMateriallyTexturedBlockComponent;
import com.ldtteam.domumornamentum.client.model.data.MaterialTextureData;
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

    public static void registerTerminalPart(WarehouseLinkTerminalPart part)
    { activeTerminalParts.add(part); }

    public static void unregisterTerminalPart(WarehouseLinkTerminalPart part)
    { activeTerminalParts.remove(part); }
    private record ViewerState(BlockPos builderPos, int activeTabIndex, int colonyId) {}

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
            activeViewers.remove(sp.getUUID());
    }

    // ── Immediate update ──────────────────────────────────────────────────────

    public static void sendImmediateUpdate(ServerPlayer player, BlockPos builderPos, int activeTabIndex)
    {
        ItemStack wand = findWandInInventory(player);
        if (wand != null) sendFullUpdate(player, builderPos, activeTabIndex, wand);
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
            // Refresh auto de la tab Citizens si elle est active (Integer.MAX_VALUE)
            if (state.activeTabIndex() == Integer.MAX_VALUE)
                CitizensScanHandler.sendCitizensPacket(player);
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
            player.sendSystemMessage(Component.literal("§c[ColonyLink] Access revoked."));
            return;
        }

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
            int st = Math.min(activeTabIndex, allEntries.size() - 1);
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

                if (toolUpgrade && BuilderToolHelper.isTool(st2))
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
                        List<String> tooltip = buildToolSubstituteTooltip(
                                st2, sub.displayStack(), stat, miss, inSt, buildingLevel);
                        entries.add(new ColonyLinkPacket.ResourceEntry(
                                displayStack, stat, miss, false, safeR, tooltip));
                        continue;
                    }
                }

                ItemStack ms = st2.copy(); ms.setCount(Math.min(miss, 64));

                if (DomumCraftHandler.isDomumItem(st2))
                {
                    DomumCraftHandler.DomumStatus ds =
                            DomumCraftHandler.computeStatus(st2, grid, miss, rPos, level);
                    if (ds != null)
                    {
                        if (!showCrafting && ds.status() == ResourceStatus.CRAFTING) continue;
                        if (!showNoPattern && ds.status() == ResourceStatus.NO_PATTERN) continue;
                        entries.add(new ColonyLinkPacket.ResourceEntry(
                                ms, ds.status(), miss, true, safeR,
                                buildDomumTooltip(st2, ds, inv, cs, miss)));
                        continue;
                    }
                }

                AEItemKey k = AEItemKey.of(st2); long inSt = inv.get(k);
                ResourceStatus stat;
                if (inSt >= miss)            stat = ResourceStatus.AVAILABLE;
                else if (cs.isRequesting(k)) stat = ResourceStatus.CRAFTING;
                else if (cs.isCraftable(k))  stat = ResourceStatus.CRAFTABLE;
                else                         stat = ResourceStatus.NO_PATTERN;

                if (!showCrafting && stat == ResourceStatus.CRAFTING) continue;
                if (!showNoPattern && stat == ResourceStatus.NO_PATTERN) continue;

                entries.add(new ColonyLinkPacket.ResourceEntry(
                        ms, stat, miss, false, safeR, buildStandardTooltip(st2, stat, miss, inSt)));
            }
        }

        ColonyLinkPacket.BuilderRequest req = fetchBuilderRequest(bb, inv, cs, grid, safeR, buildingLevel, toolUpgrade, level);
        List<ColonyLinkPacket.BuilderTabMeta> tabMetas = ColonyLinkWand.buildTabMetas(allEntries);

        PacketDistributor.sendToPlayer(player, new ColonyLinkPacket(
                entries, builderPos, builderName, buildingName, workerStatus, workerIdleReason,
                cpus, rState, req, hasCard, whPrio,
                tabMetas, activeTabIndex, rfStored, rfMax));
    }

    // ── fetchBuilderRequest (AE2) ─────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private static ColonyLinkPacket.BuilderRequest fetchBuilderRequest(
            AbstractBuildingStructureBuilder bb, KeyCounter inv, ICraftingService cs,
            IGrid grid, BlockPos rPos, int buildingLevel, boolean toolUpgrade, ServerLevel level)
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
                            DomumCraftHandler.DomumStatus ds2 =
                                    DomumCraftHandler.computeStatus(rs, grid, cnt, rPos, level);
                            if (ds2 != null)
                                return new ColonyLinkPacket.BuilderRequest(
                                        disp, cnt, ds2.status(), rPos,
                                        buildDomumTooltip(rs, ds2, inv, cs, cnt));
                        }

                        if (toolUpgrade && BuilderToolHelper.isTool(rs))
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
                                        buildToolSubstituteTooltip(rs, sub.displayStack(), substSt, cnt, substInSt, buildingLevel));
                            }
                        }

                        AEItemKey k = AEItemKey.of(rs); long inSt = inv.get(k);
                        ResourceStatus st;
                        if (inSt >= cnt)             st = ResourceStatus.AVAILABLE;
                        else if (cs.isRequesting(k)) st = ResourceStatus.CRAFTING;
                        else if (cs.isCraftable(k))  st = ResourceStatus.CRAFTABLE;
                        else                         st = ResourceStatus.NO_PATTERN;

                        return new ColonyLinkPacket.BuilderRequest(disp, cnt, st, rPos,
                                buildStandardTooltip(rs, st, cnt, inSt));
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
                    DomumCraftHandler.DomumStatus ds2 =
                            DomumCraftHandler.computeStatus(st2, grid, miss, rPos, level);
                    if (ds2 != null)
                        return new ColonyLinkPacket.BuilderRequest(
                                disp, miss, ds2.status(), rPos,
                                buildDomumTooltip(st2, ds2, inv, cs, miss));
                }

                if (toolUpgrade && BuilderToolHelper.isTool(st2))
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
                                buildToolSubstituteTooltip(st2, sub.displayStack(), substSt, miss, substInSt, buildingLevel));
                    }
                }

                AEItemKey k = AEItemKey.of(st2); long inSt = inv.get(k);
                ResourceStatus st;
                if (inSt >= miss)            st = ResourceStatus.AVAILABLE;
                else if (cs.isRequesting(k)) st = ResourceStatus.CRAFTING;
                else if (cs.isCraftable(k))  st = ResourceStatus.CRAFTABLE;
                else                         st = ResourceStatus.NO_PATTERN;

                return new ColonyLinkPacket.BuilderRequest(disp, miss, st, rPos,
                        buildStandardTooltip(st2, st, miss, inSt));
            }
        }

        return ColonyLinkPacket.BuilderRequest.NONE;
    }

    // ── Worker status ─────────────────────────────────────────────────────────

    private static String[] computeWorkerStatus(ICitizenData citizen, AbstractBuildingStructureBuilder bb)
    {
        if (bb.getWorkOrder() == null) return new String[]{"Idle", ""};

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
                            "§e⚠ Needs: §f" + cnt + "x " + rs.getDisplayName().getString()};
                }
            }
        }
        catch (Exception e) { ColonyLink.LOGGER.debug("[ColonyLink] computeWorkerStatus requests: {}", e.getMessage()); }

        try
        {
            String jobState = citizen.getJobStatus().name().toLowerCase();
            if (jobState.contains("eat") || jobState.contains("food") || jobState.contains("hungry"))
                return new String[]{"Hungry", "§e🍖 Eating"};
            if (jobState.contains("sleep") || jobState.contains("rest"))
                return new String[]{"Sleeping", "§9💤 Sleeping"};
            if (jobState.contains("weather") || jobState.contains("rain"))
                return new String[]{"Bad weather", "§9🌧 Waiting for better weather"};
            if (jobState.contains("sick") || jobState.contains("disease"))
                return new String[]{"Sick", "§c🤒 Sick — needs treatment"};
            if (jobState.contains("mourn"))
                return new String[]{"Mourning", "§7😢 Mourning"};
            if (jobState.contains("raid"))
                return new String[]{"Raided!", "§c⚔ Colony under attack"};
        }
        catch (Exception e) { ColonyLink.LOGGER.debug("[ColonyLink] computeWorkerStatus jobState: {}", e.getMessage()); }

        try
        {
            var vs = citizen.getStatus();
            if (vs != null)
            {
                String key = vs.getTranslationKey().toLowerCase();
                if (key.contains("eat") || key.contains("food") || key.contains("hungry"))
                    return new String[]{"Hungry", "§e🍖 Eating"};
                if (key.contains("sleep") || key.contains("rest"))
                    return new String[]{"Sleeping", "§9💤 Sleeping"};
                if (key.contains("weather") || key.contains("rain"))
                    return new String[]{"Bad weather", "§9🌧 Waiting for better weather"};
                if (key.contains("sick") || key.contains("disease"))
                    return new String[]{"Sick", "§c🤒 Sick — needs treatment"};
                if (key.contains("mourn"))
                    return new String[]{"Mourning", "§7😢 Mourning"};
                if (key.contains("raid"))
                    return new String[]{"Raided!", "§c⚔ Colony under attack"};
                if (key.contains("house") || key.contains("home"))
                    return new String[]{"No home", "§e🏠 No home assigned"};
                if (key.contains("work") || key.contains("build") || key.contains("place"))
                    return new String[]{"Working", ""};
                String translated = Component.translatable(vs.getTranslationKey()).getString();
                if (!translated.equals(vs.getTranslationKey()) && !translated.isBlank())
                    return new String[]{translated, ""};
            }
        }
        catch (Exception e) { ColonyLink.LOGGER.debug("[ColonyLink] computeWorkerStatus status: {}", e.getMessage()); }

        try
        {
            if (citizen.getSaturation() < 3.0)
                return new String[]{"Hungry", "§e🍖 Eating"};
        }
        catch (Exception ignored) {}

        return new String[]{"Working", ""};
    }

    // ── Tooltips ──────────────────────────────────────────────────────────────

    private static List<String> buildToolSubstituteTooltip(ItemStack original, ItemStack substitute,
                                                           ResourceStatus stat, int missing, long inStorage, int buildingLevel)
    {
        List<String> lines = new ArrayList<>();
        lines.add("§6⚙ Tool Upgrade — Work Hut Level " + buildingLevel);
        lines.add("§7  Requested: §f" + original.getDisplayName().getString());
        lines.add("§7  Best available: §f" + substitute.getDisplayName().getString());
        switch (stat)
        {
            case AVAILABLE -> lines.add("§a  ✔ " + inStorage + "x in ME — ready to send");
            case CRAFTABLE -> lines.add("§a  ⚒ Craftable via AE2 pattern");
            case CRAFTING  -> lines.add("§6  ⟳ Craft in progress...");
            default        -> lines.add("§c  ✘ No pattern available");
        }
        lines.add("§8  Needed: §f" + missing + "x");
        return lines;
    }

    private static List<String> buildDomumTooltip(ItemStack stack, DomumCraftHandler.DomumStatus ds,
                                                  KeyCounter inv, ICraftingService cs, int missing)
    {
        List<String> lines = new ArrayList<>();
        if (!(stack.getItem() instanceof BlockItem bi)) return lines;
        Block block = bi.getBlock();
        if (!(block instanceof IMateriallyTexturedBlock tb)) return lines;
        MaterialTextureData td = MaterialTextureData.readFromItemStack(stack);
        lines.add("§b[Domum Ornamentum] §7Components:");
        for (IMateriallyTexturedBlockComponent comp : tb.getComponents())
        {
            Block mb = td.getTexturedComponents().get(comp.getId());
            if (mb == null) { lines.add("§c  - " + comp.getId().getPath() + ": §4MISSING"); continue; }
            ItemStack ms = new ItemStack(mb);
            AEItemKey k = AEItemKey.of(ms); long inSt = inv.get(k);
            String s;
            if (inSt >= missing)         s = "§a✔ " + inSt + " in ME";
            else if (cs.isRequesting(k)) s = "§6⟳ Crafting...";
            else if (cs.isCraftable(k))  s = "§e⚒ Craftable (" + inSt + "/" + missing + ")";
            else                         s = "§c✘ No pattern (" + inSt + "/" + missing + ")";
            lines.add("§7  - " + ms.getDisplayName().getString() + ": " + s);
        }
        return lines;
    }

    private static List<String> buildStandardTooltip(ItemStack stack, ResourceStatus status,
                                                     int missing, long inStorage)
    {
        List<String> lines = new ArrayList<>();
        String n = stack.getDisplayName().getString();
        switch (status)
        {
            case NO_PATTERN -> { lines.add("§c✘ No AE2 pattern for:"); lines.add("§7  " + n);
                lines.add("§8  Needed: §f" + missing + "x"); lines.add("§8  In ME: §f" + inStorage + "x"); }
            case CRAFTABLE  -> { lines.add("§a⚒ Craftable via AE2:"); lines.add("§7  " + n);
                lines.add("§8  Needed: §f" + missing + "x"); lines.add("§8  In ME: §f" + inStorage + "x");
                lines.add("§e  ⚠ Missing primary ingredient"); }
            case AVAILABLE  -> { lines.add("§a✔ Available in ME:"); lines.add("§7  " + n);
                lines.add("§8  Needed: §f" + missing + "x"); lines.add("§8  In ME: §f" + inStorage + "x"); }
            case CRAFTING   -> { lines.add("§6⟳ Crafting in progress:"); lines.add("§7  " + n);
                lines.add("§8  Needed: §f" + missing + "x"); }
            case MISSING    -> { lines.add("§e⚒ Missing raw materials:"); lines.add("§7  " + n);
                lines.add("§8  Needed: §f" + missing + "x"); }
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

    // ── Helpers ───────────────────────────────────────────────────────────────

    static ItemStack findWandInInventory(ServerPlayer player)
    {
        for (ItemStack stack : player.getInventory().items)
            if (stack.getItem() instanceof ColonyLinkWand) return stack;
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