package com.colonylink.colonylink;

import appeng.api.config.Actionable;
import appeng.api.implementations.blockentities.IWirelessAccessPoint;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.CalculationStrategy;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.networking.crafting.ICraftingSimulationRequester;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageService;
import appeng.api.stacks.AEItemKey;
import com.ldtteam.domumornamentum.block.IMateriallyTexturedBlock;
import com.ldtteam.domumornamentum.block.IMateriallyTexturedBlockComponent;
import com.ldtteam.domumornamentum.client.model.data.MaterialTextureData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingWareHouse;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Gère le craft depuis les ressources de la Warehouse MineColonies.
 *
 * Cas AE2 :
 *   1. Résout les composants nécessaires via les patterns AE2
 *   2. Extrait ces composants depuis les racks warehouse
 *   3. Les injecte dans le réseau ME
 *   4. Lance le craft AE2 normalement
 *
 * Cas Domum Ornamentum :
 *   1. Identifie les composants texture du bloc DO
 *   2. Extrait ces composants depuis les racks warehouse
 *   3. Insère dans le ME → AE2 autocraft via le DomumPattern (ICraftingProvider)
 *
 * v1.4.9 — Sécurité chunk-load & zéro voiding :
 *   - Toute extraction warehouse → ME passe par moveWarehouseToMe() qui applique
 *     le pattern simulate-first : on ne prélève des racks QUE ce que le ME acceptera,
 *     et tout reliquat (course impossible) est réinséré dans les racks, puis rendu
 *     au joueur en dernier recours. Plus aucun item détruit.
 *   - Le cas "bloc Domum fini déjà dans le warehouse" ne tente plus de l'insérer
 *     dans le buffer du Redirector (qui n'accepte que DomumPatternItem) : c'était une
 *     perte totale. Les blocs finis sont livrés Warehouse → Builder par le bouton Send.
 */
public class WarehouseCraftHandler
{
    public static void handleWarehouseCraft(
            ServerPlayer player,
            ItemStack stack,
            int realCount,
            boolean isDomum,
            BlockPos redirectorPos)
    {
        ServerLevel level = player.serverLevel();

        // ── Détection wand ────────────────────────────────────────────────────
        ItemStack wandStack = findWandInInventory(player);

        if (wandStack == null)
        {
            player.sendSystemMessage(Component.translatable("colonylink.whc.clipboard_not_found"));
            return;
        }

        // ── Trouve la Warehouse de la colonie ─────────────────────────────────
        IColony colony = IColonyManager.getInstance().getClosestColony(level, redirectorPos);
        if (colony == null)
        {
            player.sendSystemMessage(Component.translatable("colonylink.whc.no_colony"));
            return;
        }

        BuildingWareHouse warehouse = findWarehouse(colony);
        if (warehouse == null)
        {
            player.sendSystemMessage(Component.translatable("colonylink.whc.no_warehouse"));
            return;
        }

        // v1.4.9 — fail-off strict : le warehouse doit être ENTIÈREMENT chargé avant
        // toute extraction (sinon racks null en silence → transferts partiels).
        if (!ColonyLinkChunkUtil.warehouseFullyLoaded(level, warehouse))
        {
            player.sendSystemMessage(Component.translatable("colonylink.handler.warehouse_unloaded"));
            return;
        }

        // ── Route vers AE2 ──────────────────────────────────────────────────────
        {
            if (!ColonyLinkWandLinkableHandler.isLinked(wandStack))
            {
                player.sendSystemMessage(Component.translatable("colonylink.whc.clipboard_not_linked"));
                return;
            }
            IWirelessAccessPoint wap = getWap(wandStack, level);
            if (wap == null)
            {
                player.sendSystemMessage(Component.translatable("colonylink.handler.no_wap"));
                return;
            }
            IGrid grid = wap.getGrid();
            if (grid == null)
            {
                player.sendSystemMessage(Component.translatable("colonylink.handler.network_offline"));
                return;
            }
            IStorageService storageService = grid.getStorageService();
            IActionSource actionSource = IActionSource.ofPlayer(player,
                    (appeng.api.networking.security.IActionHost) wap);

            if (isDomum)
                handleDomumFromWarehouse(player, level, stack, realCount, redirectorPos,
                        grid, storageService, actionSource, warehouse, wandStack);
            else
                handleAe2FromWarehouse(player, level, stack, realCount,
                        grid, storageService, actionSource, warehouse);
        }
    }

    // ── Cas AE2 ──────────────────────────────────────────────────────────────

    private static void handleAe2FromWarehouse(
            ServerPlayer player,
            ServerLevel level,
            ItemStack stack,
            int realCount,
            IGrid grid,
            IStorageService storageService,
            IActionSource actionSource,
            BuildingWareHouse warehouse)
    {
        ICraftingService craftingService = grid.getCraftingService();
        AEItemKey aeKey = AEItemKey.of(stack);

        if (!craftingService.isCraftable(aeKey))
        {
            long inserted = moveWarehouseToMe(player, level, warehouse,
                    storageService, actionSource, stack, realCount);
            if (inserted > 0)
                player.sendSystemMessage(Component.translatable("colonylink.whc.transferred_no_pattern", inserted, stack.getDisplayName()));
            else
                player.sendSystemMessage(Component.translatable("colonylink.whc.no_pattern_not_found"));
            return;
        }

        long directInWarehouse = countItemInWarehouse(level, warehouse, stack);
        if (directInWarehouse >= realCount)
        {
            long inserted = moveWarehouseToMe(player, level, warehouse,
                    storageService, actionSource, stack, realCount);
            if (inserted > 0)
                player.sendSystemMessage(Component.translatable("colonylink.whc.transferred_ready", inserted, stack.getDisplayName()));
            else
                player.sendSystemMessage(Component.translatable("colonylink.whc.me_insertion_failed"));
            return;
        }

        long batchOutput = 1;
        var patterns = craftingService.getCraftingFor(aeKey);
        if (patterns != null && !patterns.isEmpty())
        {
            var pattern = patterns.iterator().next();
            for (var output : pattern.getOutputs())
            {
                if (output.what() instanceof AEItemKey outKey && outKey.getItem() == stack.getItem())
                {
                    batchOutput = output.amount();
                    break;
                }
            }
        }

        long batchesNeeded = (long) Math.ceil((double) realCount / batchOutput);
        long totalToCraft = batchesNeeded * batchOutput;

        List<ItemStack> injected = new ArrayList<>();

        if (patterns != null && !patterns.isEmpty())
        {
            var pattern = patterns.iterator().next();
            for (var input : pattern.getInputs())
            {
                if (input == null) continue;
                ItemStack inputStack = null;
                long inputNeeded = 0;
                for (var possible : input.getPossibleInputs())
                {
                    if (possible.what() instanceof AEItemKey inputKey)
                    {
                        inputStack = inputKey.toStack(1);
                        inputNeeded = possible.amount() * batchesNeeded;
                        break;
                    }
                }
                if (inputStack == null || inputStack.isEmpty()) continue;

                AEItemKey inputAeKey = AEItemKey.of(inputStack);
                long inMe = storageService.getInventory().extract(
                        inputAeKey, inputNeeded, Actionable.SIMULATE, actionSource);
                long stillNeeded = inputNeeded - inMe;
                if (stillNeeded <= 0) continue;

                // v1.4.9 — simulate-first + réinsertion racks : aucun composant voidé.
                long inserted = moveWarehouseToMe(player, level, warehouse,
                        storageService, actionSource, inputStack,
                        (int) Math.min(stillNeeded, Integer.MAX_VALUE));
                if (inserted > 0)
                {
                    injected.add(inputStack.copyWithCount((int) inserted));
                    player.sendSystemMessage(Component.translatable("colonylink.wh2me.transfer",
                            inserted, inputStack.getDisplayName()));
                }
            }
        }

        ICraftingSimulationRequester simulationRequester = () -> actionSource;
        final List<ItemStack> injectedFinal = injected;

        java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "ColonyLink-WarehouseCraft");
            t.setDaemon(true);
            return t;
        });

        executor.submit(() -> {
            try
            {
                Future<ICraftingPlan> future = craftingService.beginCraftingCalculation(
                        level, simulationRequester, aeKey, totalToCraft, CalculationStrategy.CRAFT_LESS);
                ICraftingPlan plan = future.get(10, TimeUnit.SECONDS);
                if (plan == null)
                {
                    level.getServer().execute(() -> {
                        player.sendSystemMessage(Component.translatable("colonylink.whc.craft_plan_failed"));
                        refundInjected(storageService, actionSource, injectedFinal);
                    });
                    return;
                }
                level.getServer().execute(() -> {
                    var result = craftingService.submitJob(plan, null, null, false, actionSource);
                    if (result.successful())
                        player.sendSystemMessage(Component.translatable("colonylink.whc.craft_started", realCount, stack.getDisplayName()));
                    else
                    {
                        player.sendSystemMessage(Component.translatable("colonylink.whc.craft_submission_failed"));
                        refundInjected(storageService, actionSource, injectedFinal);
                    }
                });
            }
            catch (Exception e)
            {
                ColonyLink.LOGGER.error("[ColonyLink] Warehouse craft error", e);
                level.getServer().execute(() -> {
                    player.sendSystemMessage(Component.translatable("colonylink.whc.craft_error", e.getMessage()));
                    refundInjected(storageService, actionSource, injectedFinal);
                });
            }
        });
        executor.shutdown();
    }

    // ── Cas Domum AE2 ────────────────────────────────────────────────────────

    private static void handleDomumFromWarehouse(
            ServerPlayer player,
            ServerLevel level,
            ItemStack domumStack,
            int realCount,
            BlockPos redirectorPos,
            IGrid grid,
            IStorageService storageService,
            IActionSource actionSource,
            BuildingWareHouse warehouse,
            ItemStack wandStack)
    {
        // v1.4.9 — Un bloc Domum FINI déjà présent dans le warehouse ne doit JAMAIS
        // être routé vers le buffer du Redirector (qui n'accepte que DomumPatternItem)
        // ni re-crafté via AE2 : l'ancien code l'insérait dans le buffer, où il était
        // systématiquement rejeté puis perdu (voiding 100 %). Les blocs finis sont
        // livrés Warehouse → Builder par le bouton Send (SendToBuilderHandler).
        // On ne prélève rien ici → rien ne peut être détruit.
        long directFound = countDomumInWarehouse(level, warehouse, domumStack);
        if (directFound > 0)
        {
            player.sendSystemMessage(Component.translatable("colonylink.whc.domum_already", directFound, domumStack.getDisplayName()));
            return;
        }

        // Pas de bloc fini en warehouse → on tente d'injecter les COMPOSANTS bruts
        // dans le ME pour qu'AE2 autocrafte via le DomumPattern (ICraftingProvider).
        Item item = domumStack.getItem();
        if (!(item instanceof BlockItem blockItem)) return;
        Block block = blockItem.getBlock();
        if (!(block instanceof IMateriallyTexturedBlock texturedBlock)) return;

        MaterialTextureData textureData = MaterialTextureData.readFromItemStack(domumStack);
        List<ItemStack> injected = new ArrayList<>();

        for (IMateriallyTexturedBlockComponent component : texturedBlock.getComponents())
        {
            Block materialBlock = textureData.getTexturedComponents().get(component.getId());
            if (materialBlock == null)
            {
                if (!component.isOptional())
                {
                    player.sendSystemMessage(Component.translatable("colonylink.whc.missing_material", component.getId()));
                    refundInjected(storageService, actionSource, injected);
                    return;
                }
                continue;
            }

            ItemStack materialStack = new ItemStack(materialBlock, realCount);
            AEItemKey aeKey = AEItemKey.of(materialStack);
            long inMe = storageService.getInventory().extract(
                    aeKey, realCount, Actionable.SIMULATE, actionSource);
            long stillNeeded = realCount - inMe;
            if (stillNeeded <= 0) continue;

            // v1.4.9 — simulate-first + réinsertion racks : aucun composant voidé.
            long inserted = moveWarehouseToMe(player, level, warehouse,
                    storageService, actionSource, materialStack,
                    (int) Math.min(stillNeeded, Integer.MAX_VALUE));
            if (inserted > 0)
            {
                injected.add(materialStack.copyWithCount((int) inserted));
                player.sendSystemMessage(Component.translatable("colonylink.wh2me.transfer",
                        inserted, materialStack.getDisplayName()));
            }
        }
        // v1.4.3 — handleDomumCraft() supprimé : les items Domum passent désormais
        // par ICraftingProvider (DomumPatternDetails). Les composants injectés dans le
        // ME ci-dessus seront consommés par AE2 lors de l'autocraft.
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * v1.4.9 — Déplace en toute sécurité jusqu'à {@code want} items {@code template}
     * des racks warehouse vers le réseau ME.
     *
     * Pattern « simulate-first » : on demande d'abord au ME combien il accepterait,
     * on ne prélève des racks QUE cette quantité, puis on insère. Un éventuel reliquat
     * (course impossible en un seul tick) est réinséré dans les racks ; en ultime
     * recours il est rendu au joueur. Aucun item ne peut donc être détruit.
     *
     * @return la quantité réellement placée dans le ME
     */
    private static long moveWarehouseToMe(
            ServerPlayer player,
            ServerLevel level,
            BuildingWareHouse warehouse,
            IStorageService storageService,
            IActionSource actionSource,
            ItemStack template,
            int want)
    {
        if (want <= 0) return 0;
        AEItemKey key = AEItemKey.of(template);
        if (key == null) return 0;

        // 1) Combien le ME accepterait-il ?
        long acceptable = storageService.getInventory().insert(
                key, want, Actionable.SIMULATE, actionSource);
        if (acceptable <= 0) return 0;

        // 2) On prélève EXACTEMENT cette quantité des racks.
        int toPull = (int) Math.min((long) want, acceptable);
        long extracted = extractFromWarehouseRacks(level, warehouse, template, toPull);
        if (extracted <= 0) return 0;

        // 3) Insertion réelle (garantie par le simulate ; filet de sécurité ci-dessous).
        long inserted = storageService.getInventory().insert(
                key, extracted, Actionable.MODULATE, actionSource);

        long leftover = extracted - inserted;
        if (leftover > 0)
        {
            // Cas théoriquement impossible : on remet le reliquat dans les racks,
            // puis dans l'inventaire du joueur en dernier recours. Jamais de void.
            int residue = returnToWarehouseRacks(level, warehouse, template, (int) leftover);
            if (residue > 0)
            {
                player.getInventory().placeItemBackInInventory(template.copyWithCount(residue));
                ColonyLink.LOGGER.warn("[ColonyLink] moveWarehouseToMe: returned {}x {} to player (ME+racks full)",
                        residue, template.getDisplayName().getString());
            }
        }
        return inserted;
    }

    /**
     * v1.4.9 — Réinsère jusqu'à {@code count} items {@code template} dans les racks
     * warehouse. Retourne la quantité qui n'a PAS pu être réinsérée (0 en pratique,
     * puisqu'on vient d'en extraire de ces mêmes racks au même tick).
     */
    private static int returnToWarehouseRacks(
            ServerLevel level, BuildingWareHouse warehouse, ItemStack template, int count)
    {
        if (count <= 0) return 0;
        ItemStack remainder = template.copyWithCount(count);
        try
        {
            var containerList = warehouse.getContainers();
            if (containerList == null) return remainder.getCount();
            for (BlockPos rackPos : containerList)
            {
                if (remainder.isEmpty()) break;
                IItemHandler rackHandler = level.getCapability(
                        Capabilities.ItemHandler.BLOCK, rackPos, null);
                if (rackHandler == null) continue;
                for (int slot = 0; slot < rackHandler.getSlots() && !remainder.isEmpty(); slot++)
                    remainder = rackHandler.insertItem(slot, remainder, false);
            }
        }
        catch (Exception e)
        { ColonyLink.LOGGER.debug("[ColonyLink] returnToWarehouseRacks error: {}", e.getMessage()); }
        return remainder.getCount();
    }

    private static long countItemInWarehouse(
            ServerLevel level, BuildingWareHouse warehouse, ItemStack stack)
    {
        long count = 0;
        try
        {
            var containerList = warehouse.getContainers();
            if (containerList == null) return 0;
            for (BlockPos rackPos : containerList)
            {
                IItemHandler rackHandler = level.getCapability(
                        Capabilities.ItemHandler.BLOCK, rackPos, null);
                if (rackHandler == null) continue;
                for (int slot = 0; slot < rackHandler.getSlots(); slot++)
                {
                    ItemStack inSlot = rackHandler.getStackInSlot(slot);
                    if (inSlot.isEmpty()) continue;
                    if (ItemStack.isSameItem(inSlot, stack))
                        count += inSlot.getCount();
                }
            }
        }
        catch (Exception e)
        { ColonyLink.LOGGER.debug("[ColonyLink] countItemInWarehouse error: {}", e.getMessage()); }
        return count;
    }

    private static long countDomumInWarehouse(
            ServerLevel level, BuildingWareHouse warehouse, ItemStack domumStack)
    {
        long count = 0;
        try
        {
            var containerList = warehouse.getContainers();
            if (containerList == null) return 0;
            for (BlockPos rackPos : containerList)
            {
                IItemHandler rackHandler = level.getCapability(
                        Capabilities.ItemHandler.BLOCK, rackPos, null);
                if (rackHandler == null) continue;
                for (int slot = 0; slot < rackHandler.getSlots(); slot++)
                {
                    ItemStack inSlot = rackHandler.getStackInSlot(slot);
                    if (inSlot.isEmpty()) continue;
                    if (ItemStack.isSameItemSameComponents(inSlot, domumStack))
                        count += inSlot.getCount();
                }
            }
        }
        catch (Exception e)
        { ColonyLink.LOGGER.debug("[ColonyLink] countDomumInWarehouse error: {}", e.getMessage()); }
        return count;
    }

    static long extractFromWarehouseRacks(
            ServerLevel level,
            BuildingWareHouse warehouse,
            ItemStack stack,
            int needed)
    {
        long totalExtracted = 0;
        int remaining = needed;
        try
        {
            var containerList = warehouse.getContainers();
            if (containerList == null) return 0;
            for (BlockPos rackPos : containerList)
            {
                if (remaining <= 0) break;
                IItemHandler rackHandler = level.getCapability(
                        Capabilities.ItemHandler.BLOCK, rackPos, null);
                if (rackHandler == null) continue;
                for (int slot = 0; slot < rackHandler.getSlots() && remaining > 0; slot++)
                {
                    ItemStack inSlot = rackHandler.getStackInSlot(slot);
                    if (inSlot.isEmpty() || !ItemStack.isSameItem(inSlot, stack)) continue;
                    int toExtract = Math.min(inSlot.getCount(), remaining);
                    ItemStack took = rackHandler.extractItem(slot, toExtract, false);
                    if (took.isEmpty()) continue;
                    totalExtracted += took.getCount();
                    remaining -= took.getCount();
                }
            }
        }
        catch (Exception e)
        { ColonyLink.LOGGER.debug("[ColonyLink] Warehouse rack extraction error: {}", e.getMessage()); }
        return totalExtracted;
    }

    private static void refundInjected(
            IStorageService storageService,
            IActionSource actionSource,
            List<ItemStack> injected)
    {
        for (ItemStack stack : injected)
        {
            AEItemKey key = AEItemKey.of(stack);
            storageService.getInventory().insert(key, stack.getCount(), Actionable.MODULATE, actionSource);
        }
    }

    private static BuildingWareHouse findWarehouse(IColony colony)
    {
        for (IBuilding building : colony.getServerBuildingManager().getBuildings().values())
            if (building instanceof BuildingWareHouse wh) return wh;
        return null;
    }

    private static ItemStack findWandInInventory(ServerPlayer player)
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
}