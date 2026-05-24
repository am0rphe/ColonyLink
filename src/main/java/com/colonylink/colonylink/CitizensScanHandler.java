package com.colonylink.colonylink;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.request.RequestState;
import com.minecolonies.api.colony.requestsystem.requestable.IDeliverable;
import com.minecolonies.core.colony.buildings.AbstractBuildingStructureBuilder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.stacks.KeyCounter;
import appeng.api.implementations.blockentities.IWirelessAccessPoint;
import net.minecraft.core.GlobalPos;

import java.util.ArrayList;
import java.util.List;

/**
 * Scanne tous les citoyens de la colonie du joueur (hors builders)
 * et envoie un CitizensPacket au client.
 */
public class CitizensScanHandler
{
    public static void sendCitizensPacket(ServerPlayer player)
    {
        ItemStack wandStack = findWandInInventory(player);
        if (wandStack == null) return;

        BuilderToolHelper.ToolInventoryView inventoryView = stack -> 0L;
        BuilderToolHelper.ToolCraftingView   craftingView  = stack -> false;

        try
        {
            GlobalPos linkedPos = ColonyLinkWandLinkableHandler.getLinkedPos(wandStack);
            if (linkedPos != null)
            {
                ServerLevel targetLevel = player.server.getLevel(linkedPos.dimension());
                if (targetLevel != null)
                {
                    var be = targetLevel.getBlockEntity(linkedPos.pos());
                    if (be instanceof IWirelessAccessPoint wap)
                    {
                        IGrid grid = wap.getGrid();
                        if (grid != null)
                        {
                            KeyCounter inv = grid.getStorageService().getCachedInventory();
                            ICraftingService cs = grid.getCraftingService();
                            inventoryView = BuilderToolHelper.fromAE2Inventory(inv);
                            craftingView  = BuilderToolHelper.fromAE2CraftingService(cs);
                        }
                    }
                }
            }
        }
        catch (Exception ignored) {}

        final BuilderToolHelper.ToolInventoryView finalInv = inventoryView;
        final BuilderToolHelper.ToolCraftingView   finalCs  = craftingView;

        List<BuilderEntry> entries = ColonyLinkWandLinkableHandler.getBuilderEntries(wandStack);
        if (entries.isEmpty()) return;

        ServerLevel level = player.serverLevel();

        IColony colony = null;
        for (BuilderEntry e : entries)
        {
            colony = IColonyManager.getInstance().getClosestColony(level, e.builderPos());
            if (colony != null) break;
        }
        if (colony == null) return;

        java.util.Set<net.minecraft.core.BlockPos> builderPositions = new java.util.HashSet<>();
        for (BuilderEntry e : entries)
            builderPositions.add(e.builderPos());

        List<CitizensPacket.CitizenRequestEntry> result = new ArrayList<>();

        for (ICitizenData citizen : colony.getCitizenManager().getCitizens())
        {
            IBuilding workBuilding = citizen.getWorkBuilding();

            if (workBuilding instanceof AbstractBuildingStructureBuilder) continue;
            if (workBuilding != null && builderPositions.contains(workBuilding.getPosition())) continue;

            if (citizen.getJob() != null)
            {
                String jobClass = citizen.getJob().getClass().getSimpleName().toLowerCase();
                if (jobClass.contains("builder")) continue;
            }

            String jobName = "Citizen";
            if (citizen.getJob() != null)
            {
                try
                {
                    String key = citizen.getJob().getJobRegistryEntry().getTranslationKey();
                    int lastDot = key.lastIndexOf('.');
                    String raw = lastDot >= 0 ? key.substring(lastDot + 1) : key;
                    jobName = raw.substring(0, 1).toUpperCase() + raw.substring(1);
                }
                catch (Exception ignored)
                {
                    String cls = citizen.getJob().getClass().getSimpleName();
                    jobName = cls.endsWith("Job") ? cls.substring(0, cls.length() - 3) : cls;
                }
            }

            try
            {
                var reqs = citizen.getJob() != null
                        ? workBuilding != null
                          ? workBuilding.getOpenRequests(citizen.getId())
                          : null
                        : null;
                if (reqs == null) continue;

                for (IRequest<?> req : reqs)
                {
                    if (req.getState() == RequestState.CANCELLED
                            || req.getState() == RequestState.OVERRULED) continue;
                    if (!(req.getRequest() instanceof IDeliverable del)) continue;

                    ItemStack display = ItemStack.EMPTY;
                    var displayStacks = req.getDisplayStacks();
                    if (displayStacks != null)
                        for (ItemStack s : displayStacks)
                            if (!s.isEmpty()) { display = s.copy(); break; }
                    if (display.isEmpty()) continue;

                    int count = del.getCount();
                    if (count <= 0) count = 1;
                    display.setCount(Math.min(count, 64));

                    ItemStack finalDisplay = display;
                    BuilderToolHelper.SubstituteAction finalAction = BuilderToolHelper.SubstituteAction.NONE;

                    // Niveau réel du bâtiment du citoyen (0 si pas de bâtiment)
                    int buildingLevel = workBuilding != null ? workBuilding.getBuildingLevel() : 0;

                    if (BuilderToolHelper.isArmor(display))
                    {
                        BuilderToolHelper.SubstituteResult sub =
                                BuilderToolHelper.findBestArmor(display, buildingLevel, finalInv, finalCs);
                        if (sub.action() != BuilderToolHelper.SubstituteAction.NONE)
                        {
                            finalDisplay = sub.displayStack();
                            finalAction  = sub.action();
                        }
                    }
                    else if (BuilderToolHelper.isTool(display))
                    {
                        BuilderToolHelper.SubstituteResult sub =
                                BuilderToolHelper.findBestTool(display, buildingLevel, finalInv, finalCs);
                        if (sub.action() != BuilderToolHelper.SubstituteAction.NONE)
                        {
                            finalDisplay = sub.displayStack();
                            finalAction  = sub.action();
                        }
                    }

                    boolean availableInME;
                    boolean craftableInME;
                    if (finalAction == BuilderToolHelper.SubstituteAction.SEND)
                    {
                        availableInME = true;
                        craftableInME = false;
                    }
                    else if (finalAction == BuilderToolHelper.SubstituteAction.CRAFT)
                    {
                        availableInME = false;
                        craftableInME = true;
                    }
                    else
                    {
                        availableInME = finalInv.get(finalDisplay) > 0;
                        craftableInME = !availableInME && finalCs.isCraftable(finalDisplay);
                    }

                    result.add(new CitizensPacket.CitizenRequestEntry(
                            finalDisplay, citizen.getName(), jobName, count, availableInME, craftableInME));
                }
            }
            catch (Exception e)
            {
                ColonyLink.LOGGER.debug("[CitizensScan] Error scanning citizen {}: {}", citizen.getName(), e.getMessage());
            }
        }

        PacketDistributor.sendToPlayer(player, new CitizensPacket(result));
    }

    private static ItemStack findWandInInventory(ServerPlayer player)
    {
        // Delegate to the shared implementation that also checks Curios slots.
        return ColonyLinkServerTicker.findWandInInventory(player);
    }
}