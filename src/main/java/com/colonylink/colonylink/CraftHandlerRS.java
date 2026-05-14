package com.colonylink.colonylink;

import com.refinedmods.refinedstorage.api.autocrafting.calculation.CancellationToken;
import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.autocrafting.AutocraftingNetworkComponent;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Gère les demandes de craft vers le réseau Refined Storage 2.
 *
 * Différences vs CraftHandler (AE2) :
 * - Pas de calcul de plan asynchrone : RS2 gère ça en interne via ensureTask()
 * - ensureTask() prend : ResourceKey, amount, Actor, CancellationToken
 * - Résultat : EnsureResult.TASK_CREATED (succès), MISSING_RESOURCES, TASK_ALREADY_RUNNING
 * - Pas de notion de CPU libre — RS2 gère la file d'attente automatiquement
 *
 * Le coût RF est prélevé avant le craft, identique à CraftHandler AE2.
 */
public class CraftHandlerRS
{
    public static void handleCraftRequest(ServerPlayer player, ItemStack stack, int realCount)
    {
        handleCraftRequests(player, List.of(stack), List.of(realCount));
    }

    public static void handleCraftRequests(ServerPlayer player,
                                           List<ItemStack> stacks, List<Integer> realCounts)
    {
        // ── Coût RF ───────────────────────────────────────────────────────────
        long craftCost = ColonyLinkConfig.CRAFT_COST_RF.get();
        if (craftCost > 0 && !ColonyLinkServerTicker.tryConsumeRF(player, craftCost))
        {
            player.sendSystemMessage(Component.literal(
                    "§c[ColonyLink RS] Not enough power! Need " + craftCost + " RF to craft."));
            return;
        }

        ItemStack wandStack = findWandInInventory(player);
        if (wandStack == null || !ColonyLinkWandRSLinkableHandler.isLinked(wandStack))
        {
            player.sendSystemMessage(Component.literal("§c[ColonyLink RS] Wand not found or not linked!"));
            return;
        }

        ServerLevel level = player.serverLevel();
        Network network = ColonyLinkWandRSLinkableHandler.getNetwork(wandStack, level);
        if (network == null)
        {
            player.sendSystemMessage(Component.literal("§c[ColonyLink RS] Cannot connect to RS2 network!"));
            return;
        }

        AutocraftingNetworkComponent crafting = network.getComponent(AutocraftingNetworkComponent.class);
        if (crafting == null)
        {
            player.sendSystemMessage(Component.literal("§c[ColonyLink RS] RS2 network has no autocrafting!"));
            return;
        }

        int successCount = 0;
        int failCount    = 0;

        for (int idx = 0; idx < stacks.size(); idx++)
        {
            ItemStack stack   = stacks.get(idx);
            int realCount     = realCounts.get(idx);
            ItemResource rsKey = ItemResource.ofItemStack(stack);

            // Vérifie que l'item est craftable
            if (!crafting.getOutputs().contains(rsKey))
            {
                failCount++;
                continue;
            }

            // Lance le craft RS2 — ensureTask gère le calcul et la file en interne
            AutocraftingNetworkComponent.EnsureResult result = crafting.ensureTask(
                    rsKey,
                    realCount,
                    Actor.EMPTY,
                    CancellationToken.NONE);

            switch (result)
            {
                case TASK_CREATED ->
                {
                    successCount++;
                    ColonyLink.LOGGER.debug("[ColonyLink RS] Craft started: {}x {}",
                            realCount, stack.getDisplayName().getString());
                }
                case TASK_ALREADY_RUNNING ->
                {
                    // Craft déjà en cours — on compte comme succès (RS2 va compléter)
                    successCount++;
                    ColonyLink.LOGGER.debug("[ColonyLink RS] Craft already running: {}",
                            stack.getDisplayName().getString());
                }
                case MISSING_RESOURCES ->
                {
                    failCount++;
                    ColonyLink.LOGGER.debug("[ColonyLink RS] Missing resources for: {}",
                            stack.getDisplayName().getString());
                }
            }
        }

        // ── Feedback ──────────────────────────────────────────────────────────
        if (stacks.size() == 1)
        {
            if (successCount > 0)
                player.sendSystemMessage(Component.literal(
                        "§a[ColonyLink RS] Craft started: " + realCounts.get(0) + "x "
                                + stacks.get(0).getDisplayName().getString()));
            else
                player.sendSystemMessage(Component.literal(
                        "§c[ColonyLink RS] Missing primary ingredients for: "
                                + stacks.get(0).getDisplayName().getString()));
        }
        else
        {
            player.sendSystemMessage(Component.literal(
                    "§a[ColonyLink RS] Craft All: " + successCount + " started, "
                            + failCount + " failed."));
        }
    }

    static ItemStack findWandInInventory(ServerPlayer player)
    {
        for (ItemStack stack : player.getInventory().items)
            if (stack.getItem() instanceof ColonyLinkWandRS) return stack;
        return null;
    }
}