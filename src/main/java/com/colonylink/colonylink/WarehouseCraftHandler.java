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
import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.storage.StorageNetworkComponent;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;
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
 * Cas RS2 :
 *   1. Extrait directement l'item ou ses composants depuis les racks warehouse
 *   2. Les injecte dans le réseau RS2 via StorageNetworkComponent
 *   3. Pour les items standards : lance le craft RS2 via AutocraftingNetworkComponent
 *   4. Pour les items Domum : insère les composants extraits dans le buffer redirector
 *
 * Cas Domum Ornamentum (AE2 + RS2) :
 *   1. Identifie les composants texture du bloc DO
 *   2. Extrait ces composants depuis les racks warehouse
 *   3. Insère dans ME ou RS2 puis craft virtuel DO
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

        // ── Détection wand AE2 ou RS2 ─────────────────────────────────────────
        ItemStack wandStack = findWandInInventory(player);
        boolean isRS = wandStack != null && wandStack.getItem() instanceof ColonyLinkWandRS;

        if (wandStack == null)
        {
            player.sendSystemMessage(Component.literal("§cWand not found!"));
            return;
        }

        // ── Trouve la Warehouse de la colonie ─────────────────────────────────
        IColony colony = IColonyManager.getInstance().getClosestColony(level, redirectorPos);
        if (colony == null)
        {
            player.sendSystemMessage(Component.literal("§c[ColonyLink] No colony found near redirector!"));
            return;
        }

        BuildingWareHouse warehouse = findWarehouse(colony);
        if (warehouse == null)
        {
            player.sendSystemMessage(Component.literal("§c[ColonyLink] No Warehouse found in colony!"));
            return;
        }

        // ── Route vers le handler approprié ──────────────────────────────────
        if (isRS)
        {
            if (!ColonyLinkWandRSLinkableHandler.isLinked(wandStack))
            {
                player.sendSystemMessage(Component.literal("§cRS2 Wand not linked!"));
                return;
            }
            Network network = ColonyLinkWandRSLinkableHandler.getNetwork(wandStack, level);
            if (network == null)
            {
                player.sendSystemMessage(Component.literal("§cCannot connect to RS2 network!"));
                return;
            }
            StorageNetworkComponent storage = network.getComponent(StorageNetworkComponent.class);
            if (storage == null)
            {
                player.sendSystemMessage(Component.literal("§cRS2 network has no storage!"));
                return;
            }

            if (isDomum)
                handleDomumFromWarehouseRS(player, level, stack, realCount, redirectorPos,
                        network, storage, warehouse);
            else
                handleRS2FromWarehouse(player, level, stack, realCount,
                        network, storage, warehouse);
        }
        else
        {
            if (!ColonyLinkWandLinkableHandler.isLinked(wandStack))
            {
                player.sendSystemMessage(Component.literal("§cWand not linked!"));
                return;
            }
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

    // ── Cas RS2 standard ──────────────────────────────────────────────────────

    private static void handleRS2FromWarehouse(
            ServerPlayer player,
            ServerLevel level,
            ItemStack stack,
            int realCount,
            Network network,
            StorageNetworkComponent storage,
            BuildingWareHouse warehouse)
    {
        ItemResource rsKey = ItemResource.ofItemStack(stack);

        // Vérifie d'abord si l'item est directement en warehouse
        long directInWarehouse = countItemInWarehouse(level, warehouse, stack);
        if (directInWarehouse > 0)
        {
            int toExtract = (int) Math.min(directInWarehouse, realCount);
            long extracted = extractFromWarehouseRacks(level, warehouse, stack, toExtract);
            if (extracted > 0)
            {
                // Insère dans RS2
                long remaining = extracted;
                long inserted = storage.insert(rsKey, remaining, com.refinedmods.refinedstorage.api.core.Action.EXECUTE, Actor.EMPTY);
                if (inserted > 0)
                    player.sendSystemMessage(Component.literal(
                            "§a[ColonyLink RS] Transferred " + inserted + "x "
                                    + stack.getDisplayName().getString()
                                    + " from Warehouse → RS2 (ready to Send)"));
                else
                    player.sendSystemMessage(Component.literal("§c[ColonyLink RS] RS2 insertion failed!"));
            }
            return;
        }

        // Tente craft RS2 : extrait composants WH → RS2 → craft
        var autocrafting = network.getComponent(
                com.refinedmods.refinedstorage.api.network.autocrafting.AutocraftingNetworkComponent.class);
        if (autocrafting == null || !autocrafting.getOutputs().contains(rsKey))
        {
            player.sendSystemMessage(Component.literal(
                    "§c[ColonyLink RS] No RS2 pattern and item not found in Warehouse!"));
            return;
        }

        // Pour RS2, ensureTask gère lui-même les composants — on insère juste ce qu'on a en WH
        long extracted = extractFromWarehouseRacks(level, warehouse, stack, realCount);
        if (extracted > 0)
        {
            storage.insert(rsKey, extracted, com.refinedmods.refinedstorage.api.core.Action.EXECUTE, Actor.EMPTY);
            player.sendSystemMessage(Component.literal(
                    "§7[WH→RS2] " + extracted + "x " + stack.getDisplayName().getString()));
        }

        var result = autocrafting.ensureTask(rsKey, realCount, Actor.EMPTY,
                com.refinedmods.refinedstorage.api.autocrafting.calculation.CancellationToken.NONE);
        switch (result)
        {
            case TASK_CREATED, TASK_ALREADY_RUNNING ->
                    player.sendSystemMessage(Component.literal(
                            "§a[ColonyLink RS] Craft started: " + realCount + "x "
                                    + stack.getDisplayName().getString()
                                    + " (components from Warehouse)"));
            case MISSING_RESOURCES ->
                    player.sendSystemMessage(Component.literal(
                            "§c[ColonyLink RS] Craft failed — missing resources even after WH injection!"));
        }
    }

    // ── Cas Domum RS2 ─────────────────────────────────────────────────────────

    private static void handleDomumFromWarehouseRS(
            ServerPlayer player,
            ServerLevel level,
            ItemStack domumStack,
            int realCount,
            BlockPos redirectorPos,
            Network network,
            StorageNetworkComponent storage,
            BuildingWareHouse warehouse)
    {
        // Cas 1 : l'item DO final est directement en warehouse → buffer redirector
        long directFound = countDomumInWarehouse(level, warehouse, domumStack);
        if (directFound > 0)
        {
            int toExtract = (int) Math.min(directFound, realCount);
            long extracted = extractDomumFromWarehouseRacks(level, warehouse, domumStack, toExtract);
            if (extracted > 0)
            {
                var redirectorBe = level.getBlockEntity(redirectorPos);
                IItemHandler buffer = null;
                if (redirectorBe instanceof ColonyLinkRedirectorBlockEntityRS redirectorRS)
                    buffer = redirectorRS.buffer;
                else if (redirectorBe instanceof ColonyLinkRedirectorBlockEntity redirectorAE2)
                    buffer = redirectorAE2.buffer;

                if (buffer == null)
                {
                    player.sendSystemMessage(Component.literal("§c[ColonyLink RS] Redirector not found!"));
                    return;
                }

                ItemStack toInsert = domumStack.copyWithCount((int) extracted);
                for (int slot = 0; slot < buffer.getSlots() && !toInsert.isEmpty(); slot++)
                    toInsert = buffer.insertItem(slot, toInsert, false);

                if (toInsert.isEmpty())
                    player.sendSystemMessage(Component.literal(
                            "§a[ColonyLink RS] Sent " + extracted + "x "
                                    + domumStack.getDisplayName().getString()
                                    + " from Warehouse → Redirector buffer!"));
                else
                    player.sendSystemMessage(Component.literal(
                            "§6[ColonyLink RS] Buffer partially full — " + toInsert.getCount() + "x not inserted."));
                return;
            }
        }

        // Cas 2 : item DO pas en warehouse → extrait composants WH → buffer redirector
        Item item = domumStack.getItem();
        if (!(item instanceof BlockItem blockItem)) return;
        Block block = blockItem.getBlock();
        if (!(block instanceof IMateriallyTexturedBlock texturedBlock)) return;

        MaterialTextureData textureData = MaterialTextureData.readFromItemStack(domumStack);

        // Collecte les composants extraits
        List<ItemStack> extractedComponents = new ArrayList<>();
        boolean allOk = true;

        for (IMateriallyTexturedBlockComponent component : texturedBlock.getComponents())
        {
            Block materialBlock = textureData.getTexturedComponents().get(component.getId());
            if (materialBlock == null)
            {
                if (!component.isOptional())
                {
                    player.sendSystemMessage(Component.literal(
                            "§c[ColonyLink RS] Missing material for component: " + component.getId()));
                    allOk = false;
                    break;
                }
                continue;
            }

            ItemStack materialStack = new ItemStack(materialBlock, realCount);

            // Vérifie ce qui est déjà dans RS2
            ItemResource rsMatKey = ItemResource.ofItemStack(materialStack);
            long inRS2 = storage.get(rsMatKey);
            long stillNeeded = Math.max(0, realCount - inRS2);

            if (stillNeeded > 0)
            {
                long extracted = extractFromWarehouseRacks(
                        level, warehouse, materialStack, (int) Math.min(stillNeeded, Integer.MAX_VALUE));
                if (extracted > 0)
                {
                    // Insère dans RS2 pour que le craft DO puisse l'utiliser
                    storage.insert(rsMatKey, extracted, com.refinedmods.refinedstorage.api.core.Action.EXECUTE, Actor.EMPTY);
                    extractedComponents.add(materialStack.copyWithCount((int) extracted));
                    player.sendSystemMessage(Component.literal(
                            "§7[WH→RS2] " + extracted + "x " + materialStack.getDisplayName().getString()));
                }
            }
        }

        if (!allOk) return;

        // Insère le résultat DO dans le buffer redirector via extraction RS2
        // (même logique que DomumCraftHandler mais côté RS2)
        var redirectorBe = level.getBlockEntity(redirectorPos);
        IItemHandler buffer = null;
        if (redirectorBe instanceof ColonyLinkRedirectorBlockEntityRS redirectorRS)
            buffer = redirectorRS.buffer;
        else if (redirectorBe instanceof ColonyLinkRedirectorBlockEntity redirectorAE2)
            buffer = redirectorAE2.buffer;

        if (buffer == null)
        {
            player.sendSystemMessage(Component.literal("§c[ColonyLink RS] Redirector not found!"));
            return;
        }

        // Construit le MaterialTextureData et insère dans le buffer
        com.ldtteam.domumornamentum.client.model.data.MaterialTextureData.Builder builder =
                com.ldtteam.domumornamentum.client.model.data.MaterialTextureData.builder();
        for (IMateriallyTexturedBlockComponent component : texturedBlock.getComponents())
        {
            Block materialBlock = textureData.getTexturedComponents().get(component.getId());
            if (materialBlock != null)
                builder.setComponent(component.getId(), materialBlock);
        }

        ItemStack result = domumStack.copy();
        result.setCount(realCount);
        builder.writeToItemStack(result);

        ItemStack remainder = result.copy();
        for (int slot = 0; slot < buffer.getSlots() && !remainder.isEmpty(); slot++)
            remainder = buffer.insertItem(slot, remainder, false);

        if (remainder.isEmpty())
            player.sendSystemMessage(Component.literal(
                    "§a[ColonyLink RS] Crafted " + realCount + "x "
                            + domumStack.getDisplayName().getString()
                            + " from Warehouse components → buffer!"));
        else
        {
            // Remboursement RS2
            for (ItemStack comp : extractedComponents)
                storage.insert(ItemResource.ofItemStack(comp), comp.getCount(),
                        com.refinedmods.refinedstorage.api.core.Action.EXECUTE, Actor.EMPTY);
            player.sendSystemMessage(Component.literal("§c[ColonyLink RS] Buffer full — DO craft cancelled."));
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
            long extracted = extractFromWarehouseRacks(level, warehouse, stack, realCount);
            if (extracted > 0)
            {
                long inserted = storageService.getInventory().insert(
                        aeKey, extracted, Actionable.MODULATE, actionSource);
                if (inserted > 0)
                    player.sendSystemMessage(Component.literal(
                            "§a[ColonyLink] Transferred " + inserted + "x "
                                    + stack.getDisplayName().getString()
                                    + " from Warehouse → ME (no AE2 pattern)"));
                else
                    player.sendSystemMessage(Component.literal("§c[ColonyLink] ME insertion failed!"));
            }
            else
                player.sendSystemMessage(Component.literal(
                        "§c[ColonyLink] No AE2 pattern and item not found in Warehouse!"));
            return;
        }

        long directInWarehouse = countItemInWarehouse(level, warehouse, stack);
        if (directInWarehouse >= realCount)
        {
            long extracted = extractFromWarehouseRacks(level, warehouse, stack, realCount);
            if (extracted > 0)
            {
                long inserted = storageService.getInventory().insert(
                        aeKey, extracted, Actionable.MODULATE, actionSource);
                if (inserted > 0)
                    player.sendSystemMessage(Component.literal(
                            "§a[ColonyLink] Transferred " + inserted + "x "
                                    + stack.getDisplayName().getString()
                                    + " from Warehouse → ME (ready to Send)"));
                else
                    player.sendSystemMessage(Component.literal("§c[ColonyLink] ME insertion failed!"));
            }
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

                long extracted = extractFromWarehouseRacks(
                        level, warehouse, inputStack, (int) Math.min(stillNeeded, Integer.MAX_VALUE));
                if (extracted > 0)
                {
                    long inserted = storageService.getInventory().insert(
                            inputAeKey, extracted, Actionable.MODULATE, actionSource);
                    if (inserted > 0)
                    {
                        injected.add(inputStack.copyWithCount((int) inserted));
                        player.sendSystemMessage(Component.literal(
                                "§7[WH→ME] " + inserted + "x " + inputStack.getDisplayName().getString()));
                    }
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
                        player.sendSystemMessage(Component.literal("§c[ColonyLink] Craft plan failed!"));
                        refundInjected(storageService, actionSource, injectedFinal);
                    });
                    return;
                }
                level.getServer().execute(() -> {
                    var result = craftingService.submitJob(plan, null, null, false, actionSource);
                    if (result.successful())
                        player.sendSystemMessage(Component.literal(
                                "§a[ColonyLink] Craft started: " + realCount + "x "
                                        + stack.getDisplayName().getString()
                                        + " (components injected from Warehouse)"));
                    else
                    {
                        player.sendSystemMessage(Component.literal("§c[ColonyLink] Craft submission failed!"));
                        refundInjected(storageService, actionSource, injectedFinal);
                    }
                });
            }
            catch (Exception e)
            {
                ColonyLink.LOGGER.error("[ColonyLink] Warehouse craft error", e);
                level.getServer().execute(() -> {
                    player.sendSystemMessage(Component.literal("§c[ColonyLink] Craft error: " + e.getMessage()));
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
        long directFound = countDomumInWarehouse(level, warehouse, domumStack);
        if (directFound > 0)
        {
            int toExtract = (int) Math.min(directFound, realCount);
            long extracted = extractDomumFromWarehouseRacks(level, warehouse, domumStack, toExtract);
            if (extracted > 0)
            {
                var redirectorBe = level.getBlockEntity(redirectorPos);
                if (!(redirectorBe instanceof ColonyLinkRedirectorBlockEntity redirector))
                {
                    player.sendSystemMessage(Component.literal("§c[ColonyLink] Redirector not found!"));
                    return;
                }
                ItemStack toInsert = domumStack.copyWithCount((int) extracted);
                net.neoforged.neoforge.items.IItemHandler buffer = redirector.buffer;
                for (int slot = 0; slot < buffer.getSlots() && !toInsert.isEmpty(); slot++)
                    toInsert = buffer.insertItem(slot, toInsert, false);
                if (toInsert.isEmpty())
                    player.sendSystemMessage(Component.literal(
                            "§a[ColonyLink] Sent " + extracted + "x "
                                    + domumStack.getDisplayName().getString()
                                    + " from Warehouse → Redirector buffer!"));
                else
                    player.sendSystemMessage(Component.literal(
                            "§6[ColonyLink] Buffer partially full — " + toInsert.getCount() + "x not inserted."));
                return;
            }
        }

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
                    player.sendSystemMessage(Component.literal(
                            "§c[ColonyLink] Missing material for component: " + component.getId()));
                    refundInjected(storageService, actionSource, injected);
                    return;
                }
                continue;
            }

            ItemStack materialStack = new ItemStack(materialBlock, realCount);
            AEItemKey aeKey = AEItemKey.of(materialStack);
            long inMe = storageService.getInventory().extract(
                    aeKey, realCount, appeng.api.config.Actionable.SIMULATE, actionSource);
            long stillNeeded = realCount - inMe;
            if (stillNeeded <= 0) continue;

            long extracted = extractFromWarehouseRacks(
                    level, warehouse, materialStack, (int) Math.min(stillNeeded, Integer.MAX_VALUE));
            if (extracted > 0)
            {
                long inserted = storageService.getInventory().insert(
                        aeKey, extracted, Actionable.MODULATE, actionSource);
                if (inserted > 0)
                {
                    injected.add(materialStack.copyWithCount((int) inserted));
                    player.sendSystemMessage(Component.literal(
                            "§7[WH→ME] " + inserted + "x " + materialStack.getDisplayName().getString()));
                }
            }
        }

        DomumCraftHandler.handleDomumCraft(player, domumStack, realCount, redirectorPos);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

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

    private static long extractDomumFromWarehouseRacks(
            ServerLevel level, BuildingWareHouse warehouse, ItemStack domumStack, int needed)
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
                    if (inSlot.isEmpty() || !ItemStack.isSameItemSameComponents(inSlot, domumStack)) continue;
                    int toExtract = Math.min(inSlot.getCount(), remaining);
                    ItemStack took = rackHandler.extractItem(slot, toExtract, false);
                    if (took.isEmpty()) continue;
                    totalExtracted += took.getCount();
                    remaining -= took.getCount();
                }
            }
        }
        catch (Exception e)
        { ColonyLink.LOGGER.debug("[ColonyLink] extractDomumFromWarehouseRacks error: {}", e.getMessage()); }
        return totalExtracted;
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

    /**
     * Cherche d'abord la wand RS2, puis la wand AE2.
     * La wand RS2 est prioritaire car l'utilisateur est en mode RS2.
     */
    private static ItemStack findWandInInventory(ServerPlayer player)
    {
        for (ItemStack stack : player.getInventory().items)
            if (stack.getItem() instanceof ColonyLinkWandRS) return stack;
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