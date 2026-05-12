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

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class CraftHandler
{
    private static final ExecutorService CRAFT_EXECUTOR = Executors.newCachedThreadPool(r ->
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
            player.sendSystemMessage(Component.literal("§cWand not found or not linked!"));
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

        CRAFT_EXECUTOR.submit(() ->
        {
            int successCount = 0;
            int failCount = 0;

            for (int idx = 0; idx < stacks.size(); idx++)
            {
                ItemStack stack = stacks.get(idx);
                int realCount = realCounts.get(idx);
                AEItemKey aeKey = AEItemKey.of(stack);

                if (!craftingService.isCraftable(aeKey)) { failCount++; continue; }

                try
                {
                    long batchSize = getBatchSize(craftingService, aeKey);
                    long batchesNeeded = batchSize > 0 ? (long) Math.ceil((double) realCount / batchSize) : 1;
                    long totalToCraft = batchSize > 0 ? batchesNeeded * batchSize : realCount;

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
                    if (success) successCount++;
                    else failCount++;
                }
                catch (Exception e) { ColonyLink.LOGGER.error("Craft error", e); failCount++; }
            }

            final int finalSuccess = successCount;
            final int finalFail = failCount;
            final int finalRealCount = realCounts.get(0);

            level.getServer().execute(() -> {
                if (stacks.size() == 1)
                {
                    if (finalSuccess > 0)
                        player.sendSystemMessage(Component.literal(
                                "§aCraft started: " + finalRealCount + "x "
                                        + stacks.get(0).getDisplayName().getString()));
                    else
                        player.sendSystemMessage(Component.literal(
                                "§cMissing primary ingredients: " + stacks.get(0).getDisplayName().getString()));
                }
                else
                {
                    player.sendSystemMessage(Component.literal(
                            "§aCraft All: " + finalSuccess + " started, " + finalFail + " failed."
                                    + " (" + freeCpusFinal + " CPUs were available)"));
                }
            });
        });
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
        for (ItemStack stack : player.getInventory().items)
            if (stack.getItem() instanceof ColonyLinkWand) return stack;
        return null;
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
}