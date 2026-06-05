package com.colonylink.colonylink;

import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.CalculationStrategy;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.networking.crafting.ICraftingSimulationRequester;
import appeng.api.networking.security.IActionSource;
import appeng.api.implementations.blockentities.IWirelessAccessPoint;
import appeng.api.stacks.AEItemKey;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class CraftHandler
{
    static final ExecutorService CRAFT_EXECUTOR = Executors.newCachedThreadPool(r ->
    {
        Thread t = new Thread(r, "ColonyLink-Craft");
        t.setDaemon(true);
        return t;
    });

    public static void handleCraftRequest(ServerPlayer player, ItemStack stack, int realCount)
    {
        handleCraftRequests(player, List.of(stack), List.of(realCount));
    }

    public static void handleCraftRequests(ServerPlayer player, List<ItemStack> stacks, List<Integer> realCounts)
    {
        // ── v1.1.3 : Vérification RF avant de lancer quoi que ce soit ─────────
        long craftCost = ColonyLinkConfig.CRAFT_COST_RF.get();
        if (craftCost > 0 && !ColonyLinkServerTicker.tryConsumeRF(player, craftCost))
        {
            player.sendSystemMessage(Component.literal(
                    "§c[ColonyLink] Not enough power! Need " + craftCost + " RF to craft."));
            return;
        }

        ItemStack wandStack = findWandInInventory(player);
        if (wandStack == null || !ColonyLinkWandLinkableHandler.isLinked(wandStack))
        {
            player.sendSystemMessage(Component.literal("§cClipboard not found or not linked!"));
            return;
        }

        ServerLevel level = player.serverLevel();
        IWirelessAccessPoint wap = getWap(wandStack, level);
        if (wap == null) { player.sendSystemMessage(Component.literal("§cCannot connect to AE2 network!")); return; }

        IGrid grid = wap.getGrid();
        if (grid == null) { player.sendSystemMessage(Component.literal("§cAE2 network is offline!")); return; }

        ICraftingService craftingService = grid.getCraftingService();
        IActionSource actionSource = IActionSource.ofPlayer(player, wap);
        ICraftingSimulationRequester simulationRequester = () -> actionSource;

        int freeCpus = 0;
        for (var cpu : craftingService.getCpus())
            if (!cpu.isBusy()) freeCpus++;
        final int freeCpusFinal = freeCpus;

        boolean advancedAeLoaded = ModList.get().isLoaded("advanced_ae");
        boolean quantumComputerOnNetwork = advancedAeLoaded
                && hasAdvancedAeQuantumComputerOnCurrentGrid(grid);
        int configuredAdvancedAeLimit = ColonyLinkConfig.ADVANCED_AE_CRAFT_SUBMISSION_LIMIT.get();
        boolean useAdvancedAeLimit = quantumComputerOnNetwork
                && ColonyLinkConfig.ENABLE_ADVANCED_AE_COMPAT.get()
                && configuredAdvancedAeLimit > 0;

        // Limit ColonyLink submissions only; AE2/AdvancedAE still chooses where jobs run.
        final int maxCrafts = useAdvancedAeLimit
                ? configuredAdvancedAeLimit
                : (freeCpusFinal > 0 ? freeCpusFinal : 1);
        final boolean advancedAeCompatActive = useAdvancedAeLimit;
        final int totalRequested = stacks.size();

        ColonyLink.LOGGER.debug(
                "Craft request: AdvancedAE installed={}, quantum computer on network={}, free CPUs seen={}, craft submission limit={}",
                advancedAeLoaded, quantumComputerOnNetwork, freeCpusFinal, maxCrafts);

        CRAFT_EXECUTOR.submit(() ->
        {
            int successCount = 0;
            int failCount    = 0;
            int skipped      = 0;
            int cpuSlot      = 0; // nb de CPUs utilisés par ce batch

            for (int idx = 0; idx < stacks.size(); idx++)
            {
                ItemStack stack    = stacks.get(idx);
                int realCount      = realCounts.get(idx);
                AEItemKey aeKey    = AEItemKey.of(stack);

                if (!craftingService.isCraftable(aeKey)) { failCount++; continue; }

                // #9 : on s'arrête quand on a rempli tous les CPUs libres
                if (cpuSlot >= maxCrafts) { skipped += (stacks.size() - idx); break; }

                try
                {
                    long batchSize     = getBatchSize(craftingService, aeKey);
                    long batchesNeeded = batchSize > 0 ? (long) Math.ceil((double) realCount / batchSize) : 1;
                    long totalToCraft  = batchSize > 0 ? batchesNeeded * batchSize : realCount;

                    Future<ICraftingPlan> future = craftingService.beginCraftingCalculation(
                            level, simulationRequester, aeKey, totalToCraft, CalculationStrategy.CRAFT_LESS);
                    ICraftingPlan plan = future.get(10, TimeUnit.SECONDS);
                    if (plan == null) { failCount++; continue; }

                    CompletableFuture<Boolean> submitFuture = new CompletableFuture<>();
                    level.getServer().execute(() -> {
                        var result = craftingService.submitJob(plan, null, null, false, actionSource);
                        submitFuture.complete(result.successful());
                    });

                    boolean success = submitFuture.get(5, TimeUnit.SECONDS);
                    if (success) { successCount++; cpuSlot++; }
                    else failCount++;
                }
                catch (Exception e) { ColonyLink.LOGGER.error("Craft error", e); failCount++; }
            }

            final int finalSuccess   = successCount;
            final int finalFail      = failCount;
            final int finalSkipped   = skipped;
            final int finalRealCount = realCounts.get(0);

            level.getServer().execute(() -> {
                if (totalRequested == 1)
                {
                    if (finalSuccess > 0)
                        player.sendSystemMessage(Component.literal(
                                "§aCraft started: " + finalRealCount + "x "
                                        + stacks.get(0).getDisplayName().getString()));
                    else
                        player.sendSystemMessage(Component.literal(
                                "§cMissing primary ingredients for: " + stacks.get(0).getDisplayName().getString()));
                }
                else
                {
                    StringBuilder msg = new StringBuilder("§aCraft All: §f" + finalSuccess + " §astarted");
                    if (finalFail > 0)
                        msg.append("§c, ").append(finalFail).append(" failed");
                    if (finalSkipped > 0)
                        msg.append("§e, ").append(finalSkipped).append(" queued (no free CPU)");
                    msg.append("§7 (free CPUs seen: ").append(freeCpusFinal)
                            .append(", craft submission limit: ").append(maxCrafts)
                            .append(", AdvancedAE installed: ").append(advancedAeLoaded)
                            .append(", quantum computer on network: ").append(quantumComputerOnNetwork)
                            .append(", AdvancedAE compat: ").append(advancedAeCompatActive ? "active" : "inactive")
                            .append(")");
                    player.sendSystemMessage(Component.literal(msg.toString()));
                }
            });
        });
    }

    private static boolean hasAdvancedAeQuantumComputerOnCurrentGrid(IGrid grid)
    {
        if (grid == null) return false;

        try
        {
            ICraftingService craftingService = grid.getCraftingService();
            if (craftingService != null)
            {
                for (var cpu : craftingService.getCpus())
                    if (isAdvancedAeQuantumComputerClass(cpu))
                        return true;
            }

            for (var node : grid.getNodes())
            {
                if (node == null) continue;

                Object owner = node.getOwner();
                if (isAdvancedAeQuantumComputerClass(owner))
                    return true;

                if (hasAdvancedAeQuantumComputerCluster(owner))
                    return true;
            }
        }
        catch (Throwable t)
        {
            ColonyLink.LOGGER.debug(
                    "AdvancedAE quantum computer network detection failed; using normal AE2 CPU behaviour.", t);
        }

        return false;
    }

    private static boolean hasAdvancedAeQuantumComputerCluster(Object owner)
    {
        if (owner == null) return false;

        try
        {
            var method = owner.getClass().getMethod("getCluster");
            Object cluster = method.invoke(owner);
            return isAdvancedAeQuantumComputerClass(cluster);
        }
        catch (ReflectiveOperationException | SecurityException ignored)
        {
            return false;
        }
        catch (Throwable t)
        {
            ColonyLink.LOGGER.debug("AdvancedAE quantum computer cluster inspection failed.", t);
            return false;
        }
    }

    private static boolean isAdvancedAeQuantumComputerClass(Object object)
    {
        if (object == null) return false;
        String className = object.getClass().getName();
        return className.equals("net.pedroksl.advanced_ae.common.cluster.AdvCraftingCPU")
                || className.equals("net.pedroksl.advanced_ae.common.cluster.AdvCraftingCPUCluster")
                || className.equals("net.pedroksl.advanced_ae.common.entities.AdvCraftingBlockEntity");
    }

    public static int getFreeCpus(ServerPlayer player)
    {
        ItemStack wandStack = findWandInInventory(player);
        if (wandStack == null || !ColonyLinkWandLinkableHandler.isLinked(wandStack)) return -1;
        ServerLevel level = player.serverLevel();
        IWirelessAccessPoint wap = getWap(wandStack, level);
        if (wap == null) return -1;
        IGrid grid = wap.getGrid();
        if (grid == null) return -1;
        int free = 0;
        for (var cpu : grid.getCraftingService().getCpus())
            if (!cpu.isBusy()) free++;
        return free;
    }

    private static long getBatchSize(ICraftingService craftingService, AEItemKey aeKey)
    {
        var patterns = craftingService.getCraftingFor(aeKey);
        if (patterns == null || patterns.isEmpty()) return 1;
        var pattern = patterns.iterator().next();
        for (var output : pattern.getOutputs())
            if (output.what().equals(aeKey)) return output.amount();
        return 1;
    }

    static ItemStack findWandInInventory(ServerPlayer player)
    {
        // Delegate to the shared implementation that also checks Curios slots.
        return ColonyLinkServerTicker.findWandInInventory(player);
    }

    private static IWirelessAccessPoint getWap(ItemStack wandStack, ServerLevel level)
    {
        net.minecraft.core.GlobalPos linkedPos = ColonyLinkWandLinkableHandler.getLinkedPos(wandStack);
        if (linkedPos == null) return null;
        ServerLevel targetLevel = level.getServer().getLevel(linkedPos.dimension());
        if (targetLevel == null) return null;
        var be = targetLevel.getBlockEntity(linkedPos.pos());
        if (be instanceof IWirelessAccessPoint wap) return wap;
        return null;
    }

    /**
     * Submits a single AE2 craft job — used by TerminalCraftPacket (Warehouse Link Terminal).
     * Package-visible so TerminalCraftPacket can call it without accessing CRAFT_EXECUTOR directly.
     */
    static void submitCraftJob(ServerPlayer player,
                               IGrid grid,
                               ICraftingService cs,
                               IActionSource actionSource,
                               ICraftingSimulationRequester simReq,
                               AEItemKey aeKey,
                               int count)
    {
        CRAFT_EXECUTOR.submit(() -> {
            try
            {
                Future<ICraftingPlan> future = cs.beginCraftingCalculation(
                        player.serverLevel(), simReq, aeKey, count, CalculationStrategy.CRAFT_LESS);
                ICraftingPlan plan = future.get(10, TimeUnit.SECONDS);
                if (plan == null)
                {
                    player.serverLevel().getServer().execute(() ->
                            player.sendSystemMessage(Component.literal(
                                    "§c[Terminal] Craft plan failed for §f"
                                            + aeKey.toStack(1).getDisplayName().getString())));
                    return;
                }

                CompletableFuture<Boolean> done = new CompletableFuture<>();
                player.serverLevel().getServer().execute(() ->
                        done.complete(cs.submitJob(plan, null, null, false, actionSource).successful()));

                boolean ok = done.get(5, TimeUnit.SECONDS);
                player.serverLevel().getServer().execute(() ->
                        player.sendSystemMessage(Component.literal(ok
                                ? "§a[Terminal] Autocraft started: §f" + count + "x "
                                  + aeKey.toStack(1).getDisplayName().getString()
                                : "§c[Terminal] Autocraft failed — missing ingredients.")));
            }
            catch (Exception e)
            {
                ColonyLink.LOGGER.error("[Terminal] Autocraft error", e);
                player.serverLevel().getServer().execute(() ->
                        player.sendSystemMessage(Component.literal(
                                "§c[Terminal] Autocraft error: " + e.getMessage())));
            }
        });
    }
}
