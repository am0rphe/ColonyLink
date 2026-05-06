package com.colonylink.colonylink;

import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.CalculationStrategy;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.networking.crafting.ICraftingSimulationRequester;
import appeng.api.networking.security.IActionSource;
import appeng.api.implementations.blockentities.IWirelessAccessPoint;
import appeng.api.stacks.AEItemKey;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class CraftHandler
{
    public static void handleCraftRequest(ServerPlayer player, ItemStack stack, int realCount)
    {
        handleCraftRequests(player, List.of(stack), List.of(realCount));
    }

    public static void handleCraftRequests(ServerPlayer player, List<ItemStack> stacks, List<Integer> realCounts)
    {
        ItemStack wandStack = findWandInInventory(player);
        if (wandStack == null || !ColonyLinkWandLinkableHandler.isLinked(wandStack))
        {
            player.sendSystemMessage(Component.literal("§cWand not found or not linked!"));
            return;
        }

        ServerLevel level = player.serverLevel();

        IWirelessAccessPoint wap = getWap(wandStack, level);
        if (wap == null)
        {
            player.sendSystemMessage(Component.literal("§cCannot connect to AE2 network!"));
            return;
        }

        IGrid grid = wap.getGrid();
        if (grid == null)
        {
            player.sendSystemMessage(Component.literal("§cAE2 network is offline!"));
            return;
        }

        ICraftingService craftingService = grid.getCraftingService();
        IActionSource actionSource = IActionSource.ofPlayer(player, wap);
        ICraftingSimulationRequester simulationRequester = () -> actionSource;

        // Compte les CPUs libres au moment du clic
        int freeCpus = 0;
        for (var cpu : craftingService.getCpus())
            if (!cpu.isBusy()) freeCpus++;

        // Au minimum 1 thread pour que ça marche même sans CPU AE2 dédié
        int threadCount = Math.max(1, freeCpus);

        // Pool dynamique à la taille exacte du besoin courant
        ExecutorService craftExecutor = Executors.newFixedThreadPool(
                Math.min(threadCount, stacks.size()),
                r -> {
                    Thread t = new Thread(r, "ColonyLink-Craft");
                    t.setDaemon(true);
                    return t;
                });

        final int freeCpusFinal = freeCpus;

        craftExecutor.submit(() ->
        {
            int successCount = 0;
            int failCount = 0;

            for (int idx = 0; idx < stacks.size(); idx++)
            {
                ItemStack stack = stacks.get(idx);
                int realCount = realCounts.get(idx);
                AEItemKey aeKey = AEItemKey.of(stack);

                if (!craftingService.isCraftable(aeKey))
                {
                    failCount++;
                    continue;
                }

                try
                {
                    long batchSize = getBatchSize(craftingService, aeKey);
                    long batchesNeeded = batchSize > 0
                            ? (long) Math.ceil((double) realCount / batchSize)
                            : 1;
                    long totalToCraft = batchSize > 0 ? batchesNeeded * batchSize : realCount;

                    Future<ICraftingPlan> future = craftingService.beginCraftingCalculation(
                            level,
                            simulationRequester,
                            aeKey,
                            totalToCraft,
                            CalculationStrategy.CRAFT_LESS
                    );

                    ICraftingPlan plan = future.get(10, TimeUnit.SECONDS);

                    if (plan == null)
                    {
                        failCount++;
                        continue;
                    }

                    final boolean[] success = {false};
                    final Object lock = new Object();

                    level.getServer().execute(() ->
                    {
                        synchronized (lock)
                        {
                            var result = craftingService.submitJob(plan, null, null, false, actionSource);
                            if (result.successful())
                                success[0] = true;
                            lock.notifyAll();
                        }
                    });

                    synchronized (lock)
                    {
                        lock.wait(5000);
                    }

                    if (success[0]) successCount++;
                    else failCount++;
                }
                catch (Exception e)
                {
                    ColonyLink.LOGGER.error("Craft error", e);
                    failCount++;
                }
            }

            final int finalSuccess = successCount;
            final int finalFail = failCount;
            final int finalRealCount = realCounts.get(0);

            level.getServer().execute(() ->
            {
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

        craftExecutor.shutdown();
    }

    /**
     * Retourne le nombre de CPUs AE2 libres pour le réseau lié à la wand du joueur.
     * Retourne -1 si le réseau est inaccessible.
     */
    public static int getFreeCpus(ServerPlayer player)
    {
        ItemStack wandStack = findWandInInventory(player);
        if (wandStack == null || !ColonyLinkWandLinkableHandler.isLinked(wandStack))
            return -1;

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
        {
            if (output.what().equals(aeKey))
                return output.amount();
        }
        return 1;
    }

    private static ItemStack findWandInInventory(ServerPlayer player)
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