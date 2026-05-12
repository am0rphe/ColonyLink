package com.colonylink.colonylink;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.autocrafting.AutocraftingNetworkComponent;
import com.refinedmods.refinedstorage.api.network.storage.StorageNetworkComponent;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;
import com.ldtteam.domumornamentum.client.model.data.MaterialTextureData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingWareHouse;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

/**
 * Gère l'envoi d'items depuis le réseau RS2 vers l'inventaire du builder.
 *
 * v1.1.4 : Zero import AE2.
 * La substitution d'outils utilise BuilderToolHelper.ToolInventoryView
 * et ToolCraftingView construites via fromRS2Storage() / fromRS2Crafting().
 */
public class SendToBuilderHandlerRS
{
    public static void handleSendToBuilder(ServerPlayer player, ItemStack stack,
                                           BlockPos builderPos, int realCount)
    {
        // ── Coût RF ───────────────────────────────────────────────────────────
        long sendCost = ColonyLinkConfig.SEND_COST_RF.get();
        if (sendCost > 0 && !ColonyLinkServerTicker.tryConsumeRF(player, sendCost))
        {
            player.sendSystemMessage(Component.literal(
                    "§c[ColonyLink RS] Not enough power! Need " + sendCost + " RF to send."));
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

        StorageNetworkComponent storage = network.getComponent(StorageNetworkComponent.class);
        if (storage == null)
        {
            player.sendSystemMessage(Component.literal("§c[ColonyLink RS] RS2 network has no storage!"));
            return;
        }

        AutocraftingNetworkComponent crafting = network.getComponent(AutocraftingNetworkComponent.class);

        // ── Substitution d'outils — interfaces neutres, zéro AE2 ─────────────
        if (BuilderToolHelper.isTool(stack))
        {
            BuilderToolHelper.ToolInventoryView invView   = BuilderToolHelper.fromRS2Storage(storage);
            BuilderToolHelper.ToolCraftingView  craftView = BuilderToolHelper.fromRS2Crafting(crafting);

            int buildingLevel = 0;
            IColony colony = IColonyManager.getInstance().getClosestColony(level, builderPos);
            if (colony != null)
                for (IBuilding b : colony.getServerBuildingManager().getBuildings().values())
                    if (b.getPosition().equals(builderPos)) { buildingLevel = b.getBuildingLevel(); break; }

            BuilderToolHelper.SubstituteResult sub =
                    BuilderToolHelper.findBestTool(stack, buildingLevel, invView, craftView);

            if (sub.action() == BuilderToolHelper.SubstituteAction.SEND)
            {
                stack = sub.displayStack().copyWithCount(stack.getCount());
                player.sendSystemMessage(Component.literal(
                        "§6[ColonyLink RS] Tool upgraded: §f" + stack.getDisplayName().getString()));
            }
            else if (sub.action() == BuilderToolHelper.SubstituteAction.CRAFT)
            {
                CraftHandlerRS.handleCraftRequest(player, sub.displayStack(), 1);
                player.sendSystemMessage(Component.literal(
                        "§a[ColonyLink RS] Crafting best tool: §f"
                                + sub.displayStack().getDisplayName().getString()));
                return;
            }
        }

        // ── Redirector RS2 ────────────────────────────────────────────────────
        BlockPos redirectorPos = ColonyLinkWandRSLinkableHandler.getActiveRedirectorPos(wandStack);
        if (redirectorPos == null)
        {
            player.sendSystemMessage(Component.literal("§c[ColonyLink RS] No Redirector linked to this wand!"));
            player.sendSystemMessage(Component.literal("§7Sneak + Right-click a Colony Link Redirector RS with the wand."));
            return;
        }

        var be = level.getBlockEntity(redirectorPos);
        if (!(be instanceof ColonyLinkRedirectorBlockEntityRS redirector))
        {
            player.sendSystemMessage(Component.literal("§c[ColonyLink RS] Redirector RS2 not found!"));
            return;
        }

        if (!redirector.isRs2Active())
        {
            player.sendSystemMessage(Component.literal("§c[ColonyLink RS] Redirector is not connected to RS2!"));
            return;
        }

        BlockPos targetPos = redirector.getTargetInventoryPos();
        if (targetPos == null)
        {
            player.sendSystemMessage(Component.literal("§c[ColonyLink RS] Redirector has no target linked!"));
            return;
        }

        IItemHandler targetHandler = level.getCapability(Capabilities.ItemHandler.BLOCK, targetPos, null);
        if (targetHandler == null)
        {
            player.sendSystemMessage(Component.literal(
                    "§c[ColonyLink RS] Target inventory not found at " + targetPos.toShortString()));
            return;
        }

        if (redirector.getState() == ColonyLinkRedirectorBlockEntityRS.RedirectorState.STANDBY)
        {
            player.sendSystemMessage(Component.literal(
                    "§6[ColonyLink RS] Redirector STANDBY — target inventory is full!"));
            return;
        }

        // ── Extraction ────────────────────────────────────────────────────────
        boolean isDomum = DomumCraftHandler.isDomumItem(stack);
        ItemResource rsKey = ItemResource.ofItemStack(stack);

        long totalInserted = 0;
        int remaining = realCount;

        // Étape 1 : buffer DO
        if (isDomum)
        {
            MaterialTextureData targetData = MaterialTextureData.readFromItemStack(stack);
            IItemHandler buffer = redirector.buffer;
            for (int slot = 0; slot < buffer.getSlots() && remaining > 0; slot++)
            {
                ItemStack inSlot = buffer.getStackInSlot(slot);
                if (inSlot.isEmpty() || inSlot.getItem() != stack.getItem()) continue;
                MaterialTextureData slotData = MaterialTextureData.readFromItemStack(inSlot);
                net.minecraft.world.item.component.BlockItemStateProperties slotBsp =
                        inSlot.get(net.minecraft.core.component.DataComponents.BLOCK_STATE);
                net.minecraft.world.item.component.BlockItemStateProperties targetBsp =
                        stack.get(net.minecraft.core.component.DataComponents.BLOCK_STATE);
                if (!slotData.equals(targetData) || !java.util.Objects.equals(slotBsp, targetBsp)) continue;

                int toExtract = Math.min(inSlot.getCount(), remaining);
                ItemStack took = buffer.extractItem(slot, toExtract, false);
                if (took.isEmpty()) continue;

                ItemStack leftOver = insertIntoHandler(targetHandler, took);
                long sent = took.getCount() - leftOver.getCount();
                totalInserted += sent;
                remaining -= (int) sent;

                if (!leftOver.isEmpty())
                {
                    for (int s2 = 0; s2 < buffer.getSlots() && !leftOver.isEmpty(); s2++)
                        leftOver = buffer.insertItem(s2, leftOver, false);
                    break;
                }
            }
        }

        // Étape 2 : extraction selon priorité
        boolean warehousePriority = redirector.hasWarehouseCard() && redirector.isWarehousePriority();

        if (warehousePriority && !isDomum)
        {
            remaining = extractFromWarehouseThenRS2(level, stack, rsKey, remaining,
                    storage, redirector, targetPos);
            totalInserted = realCount - remaining;
        }
        else if (!isDomum)
        {
            while (remaining > 0)
            {
                int batchSize = Math.min(remaining, 64);
                long extracted = storage.extract(rsKey, batchSize, Action.EXECUTE, Actor.EMPTY);
                if (extracted <= 0) break;

                ItemStack toInsert = rsKey.toItemStack((int) extracted);
                ItemStack leftOver = insertIntoHandler(targetHandler, toInsert);
                long sent = extracted - leftOver.getCount();
                totalInserted += sent;
                remaining -= (int) sent;

                if (!leftOver.isEmpty())
                {
                    storage.insert(rsKey, leftOver.getCount(), Action.EXECUTE, Actor.EMPTY);
                    redirector.setState(ColonyLinkRedirectorBlockEntityRS.RedirectorState.STANDBY);
                    break;
                }
            }
        }

        // ── Feedback ──────────────────────────────────────────────────────────
        if (totalInserted > 0)
        {
            player.sendSystemMessage(Component.literal(
                    "§a[ColonyLink RS] Sent " + totalInserted + "x "
                            + stack.getDisplayName().getString() + " to builder!"));
            if (remaining > 0)
                player.sendSystemMessage(Component.literal(
                        "§6[ColonyLink RS] Target inventory full — " + remaining + "x not sent."));
        }
        else
        {
            player.sendSystemMessage(Component.literal(
                    "§c[ColonyLink RS] Could not send " + stack.getDisplayName().getString()
                            + " — not enough in RS2 or inventory full!"));
        }
    }

    private static int extractFromWarehouseThenRS2(
            ServerLevel level, ItemStack stack, ItemResource rsKey, int remaining,
            StorageNetworkComponent storage,
            ColonyLinkRedirectorBlockEntityRS redirector, BlockPos targetPos)
    {
        IColony colony = IColonyManager.getInstance().getClosestColony(level, redirector.getBlockPos());
        if (colony != null)
        {
            for (IBuilding building : colony.getServerBuildingManager().getBuildings().values())
            {
                if (!(building instanceof BuildingWareHouse warehouse)) continue;
                try
                {
                    var containerList = warehouse.getContainers();
                    if (containerList == null || containerList.isEmpty()) continue;
                    for (BlockPos rackPos : containerList)
                    {
                        if (remaining <= 0) break;
                        IItemHandler rackHandler = level.getCapability(
                                Capabilities.ItemHandler.BLOCK, rackPos, null);
                        if (rackHandler == null) continue;
                        for (int slot = 0; slot < rackHandler.getSlots() && remaining > 0; slot++)
                        {
                            ItemStack inSlot = rackHandler.getStackInSlot(slot);
                            if (inSlot.isEmpty() || !ItemStack.isSameItemSameComponents(inSlot, stack)) continue;
                            int toExtract = Math.min(inSlot.getCount(), remaining);
                            ItemStack took = rackHandler.extractItem(slot, toExtract, false);
                            if (took.isEmpty()) continue;
                            IItemHandler targetHandler = level.getCapability(
                                    Capabilities.ItemHandler.BLOCK, targetPos, null);
                            if (targetHandler == null) return remaining;
                            ItemStack leftOver = insertIntoHandler(targetHandler, took);
                            int sent = took.getCount() - leftOver.getCount();
                            remaining -= sent;
                            if (!leftOver.isEmpty())
                            {
                                for (int s2 = 0; s2 < rackHandler.getSlots() && !leftOver.isEmpty(); s2++)
                                    leftOver = rackHandler.insertItem(s2, leftOver, false);
                                redirector.setState(ColonyLinkRedirectorBlockEntityRS.RedirectorState.STANDBY);
                                return remaining;
                            }
                        }
                    }
                }
                catch (Exception e)
                { ColonyLink.LOGGER.debug("[ColonyLink RS] Warehouse extraction error: {}", e.getMessage()); }
                break;
            }
        }

        while (remaining > 0)
        {
            int batchSize = Math.min(remaining, 64);
            long extracted = storage.extract(rsKey, batchSize, Action.EXECUTE, Actor.EMPTY);
            if (extracted <= 0) break;
            IItemHandler targetHandler = level.getCapability(
                    Capabilities.ItemHandler.BLOCK, targetPos, null);
            if (targetHandler == null) break;
            ItemStack toInsert = rsKey.toItemStack((int) extracted);
            ItemStack leftOver = insertIntoHandler(targetHandler, toInsert);
            int sent = (int) extracted - leftOver.getCount();
            remaining -= sent;
            if (!leftOver.isEmpty())
            {
                storage.insert(rsKey, leftOver.getCount(), Action.EXECUTE, Actor.EMPTY);
                redirector.setState(ColonyLinkRedirectorBlockEntityRS.RedirectorState.STANDBY);
                break;
            }
        }
        return remaining;
    }

    private static ItemStack insertIntoHandler(IItemHandler handler, ItemStack stack)
    {
        ItemStack remainder = stack.copy();
        for (int slot = 0; slot < handler.getSlots() && !remainder.isEmpty(); slot++)
            remainder = handler.insertItem(slot, remainder, false);
        return remainder;
    }

    static ItemStack findWandInInventory(ServerPlayer player)
    {
        for (ItemStack stack : player.getInventory().items)
            if (stack.getItem() instanceof ColonyLinkWandRS) return stack;
        return null;
    }
}