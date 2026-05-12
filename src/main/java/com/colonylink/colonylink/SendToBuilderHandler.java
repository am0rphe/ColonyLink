package com.colonylink.colonylink;

import appeng.api.config.Actionable;
import appeng.api.implementations.blockentities.IWirelessAccessPoint;
import appeng.api.networking.IGrid;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageService;
import appeng.api.stacks.AEItemKey;
import appeng.api.storage.MEStorage;
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

public class SendToBuilderHandler
{
    public static void handleSendToBuilder(ServerPlayer player, ItemStack stack,
                                           BlockPos builderPos, int realCount)
    {
        // ── v1.1.3 : Coût RF ──────────────────────────────────────────────────
        long sendCost = ColonyLinkConfig.SEND_COST_RF.get();
        if (sendCost > 0 && !ColonyLinkServerTicker.tryConsumeRF(player, sendCost))
        {
            player.sendSystemMessage(Component.literal(
                    "§c[ColonyLink] Not enough power! Need " + sendCost + " RF to send."));
            return;
        }

        ItemStack wandStack = findWandInInventory(player);
        if (wandStack == null || !ColonyLinkWandLinkableHandler.isLinked(wandStack))
        {
            player.sendSystemMessage(Component.literal("§cWand not found or not linked!"));
            return;
        }

        ServerLevel level = player.serverLevel();

        // ── v1.1.3 : Substitution d'outils ───────────────────────────────────
        // Si l'item est un outil, on cherche le meilleur tier disponible
        // selon le niveau du bâtiment du builder avant d'extraire du ME.
        if (BuilderToolHelper.isTool(stack))
        {
            IWirelessAccessPoint wap = getWap(wandStack, level);
            if (wap != null && wap.getGrid() != null)
            {
                IGrid grid = wap.getGrid();
                appeng.api.stacks.KeyCounter inv =
                        grid.getStorageService().getCachedInventory();
                appeng.api.networking.crafting.ICraftingService cs =
                        grid.getCraftingService();

                // Cherche le niveau du bâtiment
                int buildingLevel = 0;
                com.minecolonies.api.colony.IColony colony =
                        com.minecolonies.api.colony.IColonyManager.getInstance()
                                .getClosestColony(level, builderPos);
                if (colony != null)
                {
                    for (com.minecolonies.api.colony.buildings.IBuilding b :
                            colony.getServerBuildingManager().getBuildings().values())
                    {
                        if (b.getPosition().equals(builderPos))
                        {
                            buildingLevel = b.getBuildingLevel();
                            break;
                        }
                    }
                }

                BuilderToolHelper.SubstituteResult sub =
                        BuilderToolHelper.findBestTool(stack, buildingLevel, inv, cs);

                if (sub.action() == BuilderToolHelper.SubstituteAction.SEND)
                {
                    // Substitue l'item par le meilleur trouvé en ME
                    stack = sub.displayStack().copyWithCount(stack.getCount());
                    player.sendSystemMessage(Component.literal(
                            "§6[ColonyLink] Tool upgraded: §f"
                                    + stack.getDisplayName().getString()));
                }
                else if (sub.action() == BuilderToolHelper.SubstituteAction.CRAFT)
                {
                    // Lance le craft du meilleur tier disponible
                    CraftHandler.handleCraftRequest(player, sub.displayStack(), 1);
                    player.sendSystemMessage(Component.literal(
                            "§a[ColonyLink] Crafting best tool: §f"
                                    + sub.displayStack().getDisplayName().getString()));
                    return;
                }
            }
        }
        // ─────────────────────────────────────────────────────────────────────

        BlockPos redirectorPos = ColonyLinkWandLinkableHandler.getActiveRedirectorPos(wandStack);
        if (redirectorPos == null)
        {
            player.sendSystemMessage(Component.literal("§cNo Redirector linked to this wand!"));
            player.sendSystemMessage(Component.literal("§7Sneak + Right-click a Colony Link Redirector with the wand."));
            return;
        }

        ColonyLinkRedirectorBlockEntity redirector = null;
        var be = level.getBlockEntity(redirectorPos);
        if (be instanceof ColonyLinkRedirectorBlockEntity r) redirector = r;

        if (redirector == null)
        {
            player.sendSystemMessage(Component.literal("§cRedirector not found at stored position!"));
            return;
        }

        var node = redirector.getManagedGridNode().getNode();
        if (node == null || !node.isActive())
        {
            player.sendSystemMessage(Component.literal("§cRedirector is not connected to the AE2 network!"));
            return;
        }

        BlockPos targetPos = redirector.getTargetInventoryPos();
        if (targetPos == null)
        {
            player.sendSystemMessage(Component.literal("§cRedirector has no target inventory linked!"));
            return;
        }

        IItemHandler targetHandler = level.getCapability(Capabilities.ItemHandler.BLOCK, targetPos, null);
        if (targetHandler == null)
        {
            player.sendSystemMessage(Component.literal("§cTarget inventory not found at " + targetPos.toShortString()));
            return;
        }

        IWirelessAccessPoint wap = getWap(wandStack, level);
        if (wap == null) { player.sendSystemMessage(Component.literal("§cCannot connect to AE2 network!")); return; }

        IGrid grid = wap.getGrid();
        if (grid == null) { player.sendSystemMessage(Component.literal("§cAE2 network is offline!")); return; }

        if (redirector.getState() == ColonyLinkRedirectorBlockEntity.RedirectorState.STANDBY)
        {
            player.sendSystemMessage(Component.literal("§6Redirector is in STANDBY - target inventory is full!"));
            return;
        }

        IStorageService storageService = grid.getStorageService();
        MEStorage inventory = storageService.getInventory();
        IActionSource actionSource = IActionSource.ofPlayer(player,
                (appeng.api.networking.security.IActionHost) wap);

        boolean isDomum = DomumCraftHandler.isDomumItem(stack);
        AEItemKey aeKey = AEItemKey.of(stack);

        long totalInserted = 0;
        int remaining = realCount;

        // ── Étape 1 : buffer DO si applicable ────────────────────────────────
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

        // ── Étape 2 : extraction selon priorité ───────────────────────────────
        boolean warehousePriority = redirector.hasWarehouseCard() && redirector.isWarehousePriority();

        if (warehousePriority && !isDomum)
        {
            remaining = extractFromWarehouseThenMe(level, stack, aeKey, remaining,
                    inventory, actionSource, redirector, targetPos);
            totalInserted = realCount - remaining;
        }
        else
        {
            while (remaining > 0)
            {
                int batchSize = Math.min(remaining, 64);
                long extracted = inventory.extract(aeKey, batchSize, Actionable.MODULATE, actionSource);
                if (extracted <= 0) break;

                ItemStack toInsert = aeKey.toStack((int) extracted);
                ItemStack leftOver = insertIntoHandler(targetHandler, toInsert);
                long sent = extracted - leftOver.getCount();
                totalInserted += sent;
                remaining -= (int) sent;

                if (!leftOver.isEmpty())
                {
                    inventory.insert(aeKey, leftOver.getCount(), Actionable.MODULATE, actionSource);
                    redirector.setState(ColonyLinkRedirectorBlockEntity.RedirectorState.STANDBY);
                    break;
                }
            }
        }

        // ── Feedback ──────────────────────────────────────────────────────────
        if (totalInserted > 0)
        {
            player.sendSystemMessage(Component.literal(
                    "§a[ColonyLink] Sent " + totalInserted + "x "
                            + stack.getDisplayName().getString() + " to builder!"));
            if (remaining > 0)
                player.sendSystemMessage(Component.literal(
                        "§6[ColonyLink] Target inventory full — " + remaining + "x not sent."));
        }
        else
        {
            player.sendSystemMessage(Component.literal(
                    "§c[ColonyLink] Could not send "
                            + stack.getDisplayName().getString()
                            + " — not enough in ME or inventory full!"));
        }
    }

    private static int extractFromWarehouseThenMe(
            ServerLevel level, ItemStack stack, AEItemKey aeKey, int remaining,
            MEStorage inventory, IActionSource actionSource,
            ColonyLinkRedirectorBlockEntity redirector, BlockPos targetPos)
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
                            if (targetHandler == null)
                            {
                                for (int s = 0; s < rackHandler.getSlots() && !took.isEmpty(); s++)
                                    took = rackHandler.insertItem(s, took, false);
                                return remaining;
                            }

                            ItemStack leftOver = insertIntoHandler(targetHandler, took);
                            int sent = took.getCount() - leftOver.getCount();
                            remaining -= sent;

                            if (!leftOver.isEmpty())
                            {
                                for (int s2 = 0; s2 < rackHandler.getSlots() && !leftOver.isEmpty(); s2++)
                                    leftOver = rackHandler.insertItem(s2, leftOver, false);
                                redirector.setState(ColonyLinkRedirectorBlockEntity.RedirectorState.STANDBY);
                                return remaining;
                            }
                        }
                    }
                }
                catch (Exception e)
                {
                    ColonyLink.LOGGER.debug("[ColonyLink] Warehouse extraction error: {}", e.getMessage());
                }
                break;
            }
        }

        while (remaining > 0)
        {
            int batchSize = Math.min(remaining, 64);
            long extracted = inventory.extract(aeKey, batchSize, Actionable.MODULATE, actionSource);
            if (extracted <= 0) break;

            IItemHandler targetHandler = level.getCapability(
                    Capabilities.ItemHandler.BLOCK, targetPos, null);
            if (targetHandler == null) break;

            ItemStack toInsert = aeKey.toStack((int) extracted);
            ItemStack leftOver = insertIntoHandler(targetHandler, toInsert);
            int sent = (int) extracted - leftOver.getCount();
            remaining -= sent;

            if (!leftOver.isEmpty())
            {
                inventory.insert(aeKey, leftOver.getCount(), Actionable.MODULATE, actionSource);
                redirector.setState(ColonyLinkRedirectorBlockEntity.RedirectorState.STANDBY);
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