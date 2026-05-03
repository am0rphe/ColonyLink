package com.colonylink.colonylink;

import appeng.api.implementations.blockentities.IWirelessAccessPoint;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.networking.storage.IStorageService;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.KeyCounter;
import com.ldtteam.domumornamentum.block.IMateriallyTexturedBlock;
import com.ldtteam.domumornamentum.block.IMateriallyTexturedBlockComponent;
import com.ldtteam.domumornamentum.client.model.data.MaterialTextureData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.core.colony.buildings.AbstractBuildingStructureBuilder;
import com.minecolonies.core.colony.buildings.utils.BuildingBuilderResource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ColonyLinkServerTicker
{
    private static final Map<UUID, BlockPos> activeViewers = new HashMap<>();
    private static int tickCounter = 0;

    public static void addViewer(UUID playerUUID, BlockPos builderPos)
    {
        activeViewers.put(playerUUID, builderPos);
    }

    public static void removeViewer(UUID playerUUID)
    {
        activeViewers.remove(playerUUID);
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Pre event)
    {
        tickCounter++;
        if (tickCounter < 40) return; // 2 secondes au lieu de 1
        tickCounter = 0;

        if (activeViewers.isEmpty()) return;

        List<UUID> toRemove = new ArrayList<>();

        activeViewers.forEach((uuid, builderPos) ->
        {
            ServerPlayer player = event.getServer().getPlayerList().getPlayer(uuid);
            if (player == null)
            {
                toRemove.add(uuid);
                return;
            }

            ServerLevel level = player.serverLevel();

            ItemStack wandStack = findWandInInventory(player);
            if (wandStack == null || !ColonyLinkWandLinkableHandler.isLinked(wandStack)) return;

            IWirelessAccessPoint wap = getWap(wandStack, level);
            if (wap == null) return;

            IGrid grid = wap.getGrid();
            if (grid == null) return;

            IColony colony = IColonyManager.getInstance().getClosestColony(level, builderPos);
            if (colony == null) return;

            IBuilding building = null;
            for (IBuilding b : colony.getServerBuildingManager().getBuildings().values())
            {
                if (b.getPosition().equals(builderPos))
                {
                    building = b;
                    break;
                }
            }

            if (!(building instanceof AbstractBuildingStructureBuilder builderBuilding)) return;

            // Infos builder + statut worker
            String builderName = "N/A";
            String workerStatus = "IDLE";
            if (!builderBuilding.getAllAssignedCitizen().isEmpty())
            {
                var citizen = builderBuilding.getAllAssignedCitizen().iterator().next();
                builderName = citizen.getName();
                var visibleStatus = citizen.getStatus();
                if (visibleStatus != null)
                    workerStatus = Component.translatable(visibleStatus.getTranslationKey()).getString();
                else
                    workerStatus = citizen.getJobStatus().name();
            }

            // Infos work order
            String buildingName = "N/A";
            var workOrder = builderBuilding.getWorkOrder();
            if (workOrder != null)
            {
                buildingName = workOrder.getDisplayName().getString();
                if (workOrder.getStage() != null)
                    buildingName += " [" + workOrder.getStage().name() + "]";
            }

            // CPUs AE2
            ICraftingService craftingService = grid.getCraftingService();
            int availableCpus = 0;
            for (var cpu : craftingService.getCpus())
            {
                if (!cpu.isBusy()) availableCpus++;
            }

            // État redirector
            String redirectorState = "N/A";
            BlockPos redirectorPos = getLinkedRedirectorPos(wandStack);
            if (redirectorPos != null)
            {
                var be = level.getBlockEntity(redirectorPos);
                if (be instanceof ColonyLinkRedirectorBlockEntity redirector)
                    redirectorState = redirector.getState().name();
            }

            // Ressources
            Map<String, BuildingBuilderResource> neededResources = builderBuilding.getNeededResources();
            List<ColonyLinkPacket.ResourceEntry> entries = new ArrayList<>();

            if (neededResources != null && !neededResources.isEmpty())
            {
                IStorageService storageService = grid.getStorageService();
                KeyCounter inventory = storageService.getCachedInventory();
                BlockPos safeRedirectorPos = redirectorPos != null ? redirectorPos : BlockPos.ZERO;

                for (BuildingBuilderResource resource : neededResources.values())
                {
                    ItemStack stack = resource.getItemStack();
                    int needed = resource.getAmount();
                    int available = resource.getAvailable();
                    int missing = needed - available;

                    if (missing <= 0) continue;

                    ItemStack missingStack = stack.copy();
                    missingStack.setCount(Math.min(missing, 64));

                    // Détection Domum Ornamentum
                    if (DomumCraftHandler.isDomumItem(stack))
                    {
                        DomumCraftHandler.DomumStatus domumStatus =
                                DomumCraftHandler.computeStatus(stack, grid, missing, redirectorPos, level);

                        if (domumStatus != null)
                        {
                            List<String> tooltip = buildDomumTooltip(
                                    stack, domumStatus, inventory, craftingService, missing);
                            entries.add(new ColonyLinkPacket.ResourceEntry(
                                    missingStack, domumStatus.status(), missing, true, safeRedirectorPos, tooltip));
                            continue;
                        }
                    }

                    // Item standard AE2
                    AEItemKey aeKey = AEItemKey.of(stack);
                    ResourceStatus status;
                    long inStorage = inventory.get(aeKey);

                    if (inStorage >= missing)
                        status = ResourceStatus.AVAILABLE;
                    else if (craftingService.isRequesting(aeKey))
                        status = ResourceStatus.CRAFTING;
                    else if (craftingService.isCraftable(aeKey))
                        status = ResourceStatus.CRAFTABLE;
                    else
                        status = ResourceStatus.NO_PATTERN;

                    List<String> tooltip = buildStandardTooltip(stack, status, missing, inStorage);
                    entries.add(new ColonyLinkPacket.ResourceEntry(
                            missingStack, status, missing, false, safeRedirectorPos, tooltip));
                }
            }

            PacketDistributor.sendToPlayer(player, new ColonyLinkPacket(
                    entries, builderPos, builderName, buildingName, workerStatus, availableCpus, redirectorState));
        });

        toRemove.forEach(activeViewers::remove);
    }

    private static List<String> buildDomumTooltip(ItemStack stack, DomumCraftHandler.DomumStatus domumStatus,
                                                  KeyCounter inventory, ICraftingService craftingService, int missing)
    {
        List<String> lines = new ArrayList<>();

        if (!(stack.getItem() instanceof BlockItem blockItem)) return lines;
        Block block = blockItem.getBlock();
        if (!(block instanceof IMateriallyTexturedBlock texturedBlock)) return lines;

        MaterialTextureData textureData = MaterialTextureData.readFromItemStack(stack);

        lines.add("§b[Domum Ornamentum] §7Components:");

        for (IMateriallyTexturedBlockComponent component : texturedBlock.getComponents())
        {
            Block materialBlock = textureData.getTexturedComponents().get(component.getId());
            if (materialBlock == null)
            {
                lines.add("§c  - " + component.getId().getPath() + ": §4MISSING");
                continue;
            }

            ItemStack materialStack = new ItemStack(materialBlock);
            String matName = materialStack.getDisplayName().getString();
            AEItemKey aeKey = AEItemKey.of(materialStack);
            long inStorage = inventory.get(aeKey);

            String statusStr;
            if (inStorage >= missing)
                statusStr = "§a✔ " + inStorage + " in ME";
            else if (craftingService.isRequesting(aeKey))
                statusStr = "§6⟳ Crafting...";
            else if (craftingService.isCraftable(aeKey))
                statusStr = "§e⚒ Craftable (" + inStorage + "/" + missing + ")";
            else
                statusStr = "§c✘ No pattern (" + inStorage + "/" + missing + ")";

            lines.add("§7  - " + matName + ": " + statusStr);
        }

        return lines;
    }

    private static List<String> buildStandardTooltip(ItemStack stack, ResourceStatus status,
                                                     int missing, long inStorage)
    {
        List<String> lines = new ArrayList<>();
        String itemName = stack.getDisplayName().getString();

        switch (status)
        {
            case NO_PATTERN ->
            {
                lines.add("§c✘ No AE2 pattern for:");
                lines.add("§7  " + itemName);
                lines.add("§8  Needed: §f" + missing + "x");
                lines.add("§8  In ME: §f" + inStorage + "x");
            }
            case CRAFTABLE ->
            {
                lines.add("§a⚒ Craftable via AE2:");
                lines.add("§7  " + itemName);
                lines.add("§8  Needed: §f" + missing + "x");
                lines.add("§8  In ME: §f" + inStorage + "x");
            }
            case AVAILABLE ->
            {
                lines.add("§a✔ Available in ME:");
                lines.add("§7  " + itemName);
                lines.add("§8  Needed: §f" + missing + "x");
                lines.add("§8  In ME: §f" + inStorage + "x");
            }
            case CRAFTING ->
            {
                lines.add("§6⟳ Crafting in progress:");
                lines.add("§7  " + itemName);
                lines.add("§8  Needed: §f" + missing + "x");
            }
            case MISSING ->
            {
                lines.add("§e⚒ Missing raw materials:");
                lines.add("§7  " + itemName);
                lines.add("§8  Needed: §f" + missing + "x");
            }
        }

        return lines;
    }

    private static BlockPos getLinkedRedirectorPos(ItemStack wandStack)
    {
        var data = wandStack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
        if (data == null) return null;
        var tag = data.copyTag();
        if (!tag.contains("redirector_x")) return null;
        return new BlockPos(
                tag.getInt("redirector_x"),
                tag.getInt("redirector_y"),
                tag.getInt("redirector_z"));
    }

    private static ItemStack findWandInInventory(ServerPlayer player)
    {
        for (ItemStack stack : player.getInventory().items)
        {
            if (stack.getItem() instanceof ColonyLinkWand)
                return stack;
        }
        return null;
    }

    private static IWirelessAccessPoint getWap(ItemStack wandStack, ServerLevel level)
    {
        net.minecraft.core.GlobalPos linkedPos = ColonyLinkWandLinkableHandler.getLinkedPos(wandStack);
        if (linkedPos == null) return null;

        ServerLevel targetLevel = level.getServer().getLevel(linkedPos.dimension());
        if (targetLevel == null) return null;

        var blockEntity = targetLevel.getBlockEntity(linkedPos.pos());
        if (blockEntity instanceof IWirelessAccessPoint wap)
            return wap;

        return null;
    }
}