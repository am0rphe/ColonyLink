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
 * Cas RS2 :
 *   1. Extrait directement l'item ou ses composants depuis les racks warehouse
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

        if (wandStack == null)
        {
            player.sendSystemMessage(Component.literal("§cClipboard not found!"));
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

        // ── Route vers AE2 ──────────────────────────────────────────────────────
        {
            if (!ColonyLinkWandLinkableHandler.isLinked(wandStack))
            {
                player.sendSystemMessage(Component.literal("§cClipboard not linked!"));
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
        // v1.4.3 — handleDomumCraft() supprimé : les items Domum passent désormais
        // par ICraftingProvider (DomumPatternDetails). L'appel ici était un vestige
        // de l'ancienne méthode de craft virtuel, obsolète depuis v1.4.3.
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
     */
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