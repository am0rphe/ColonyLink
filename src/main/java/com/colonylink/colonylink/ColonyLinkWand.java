package com.colonylink.colonylink;

import appeng.api.implementations.blockentities.IWirelessAccessPoint;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.networking.storage.IStorageService;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.KeyCounter;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.core.colony.buildings.AbstractBuildingStructureBuilder;
import com.minecolonies.core.colony.buildings.utils.BuildingBuilderResource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ColonyLinkWand extends Item
{
    public ColonyLinkWand(Properties properties)
    {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag)
    {
        boolean linkedAE2 = ColonyLinkWandLinkableHandler.isLinked(stack);
        boolean linkedRedirector = isLinkedToRedirector(stack);
        BlockPos lastBuilder = getLastBuilderPos(stack);
        BlockPos redirectorPos = getLinkedRedirectorPos(stack);

        if (!linkedAE2)
        {
            tooltip.add(Component.literal("§c✘ Not linked to AE2"));
            tooltip.add(Component.literal("§8  → Insert into a §fWireless Access Point"));
        }
        else
        {
            tooltip.add(Component.literal("§a✔ Linked to AE2 network"));

            if (!linkedRedirector)
            {
                tooltip.add(Component.literal("§e⚠ No Redirector linked"));
                tooltip.add(Component.literal("§8  → §fSneak + Right-click §8a Colony Link Redirector"));
            }
            else
            {
                tooltip.add(Component.literal("§b✔ Redirector linked"));
                if (redirectorPos != null)
                    tooltip.add(Component.literal("§8  at " + redirectorPos.toShortString()));
            }

            if (lastBuilder == null)
            {
                tooltip.add(Component.literal("§e⚠ No Builder's Hut linked"));
                tooltip.add(Component.literal("§8  → §fSneak + Right-click §8a Builder's Hut to link"));
            }
            else
            {
                tooltip.add(Component.literal("§a✔ Builder linked at §f" + lastBuilder.toShortString()));
                tooltip.add(Component.literal("§8  → §fRight-click §8(air) to open GUI"));
            }
        }

        tooltip.add(Component.literal("§8──────────────────"));
        tooltip.add(Component.literal("§7Right-click §8(air) → open resource GUI"));
        tooltip.add(Component.literal("§7Sneak + Right-click §8Builder's Hut → link builder"));
        tooltip.add(Component.literal("§7Sneak + Right-click §8Redirector → link redirector"));
    }

    private boolean isLinkedToRedirector(ItemStack stack)
    {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return false;
        return data.copyTag().contains("redirector_x");
    }

    private BlockPos getLinkedRedirectorPos(ItemStack stack)
    {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return null;
        var tag = data.copyTag();
        if (!tag.contains("redirector_x")) return null;
        return new BlockPos(tag.getInt("redirector_x"), tag.getInt("redirector_y"), tag.getInt("redirector_z"));
    }

    private void setLinkedRedirectorPos(ItemStack stack, BlockPos pos)
    {
        stack.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, data -> {
            var tag = data.copyTag();
            if (pos == null)
            {
                tag.remove("redirector_x");
                tag.remove("redirector_y");
                tag.remove("redirector_z");
            }
            else
            {
                tag.putInt("redirector_x", pos.getX());
                tag.putInt("redirector_y", pos.getY());
                tag.putInt("redirector_z", pos.getZ());
            }
            return CustomData.of(tag);
        });
    }

    public static BlockPos getLastBuilderPos(ItemStack stack)
    {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return null;
        var tag = data.copyTag();
        if (!tag.contains("last_builder_x")) return null;
        return new BlockPos(tag.getInt("last_builder_x"), tag.getInt("last_builder_y"), tag.getInt("last_builder_z"));
    }

    private void setLastBuilderPos(ItemStack stack, BlockPos pos)
    {
        stack.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, data -> {
            var tag = data.copyTag();
            if (pos == null)
            {
                tag.remove("last_builder_x");
                tag.remove("last_builder_y");
                tag.remove("last_builder_z");
            }
            else
            {
                tag.putInt("last_builder_x", pos.getX());
                tag.putInt("last_builder_y", pos.getY());
                tag.putInt("last_builder_z", pos.getZ());
            }
            return CustomData.of(tag);
        });
    }

    /**
     * Clic droit AIR uniquement → ouvre le GUI.
     * Sneak + clic droit AIR → ignoré (réservé à useOn sur les blocs).
     */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand)
    {
        ItemStack wandStack = player.getItemInHand(hand);

        if (level.isClientSide()) return InteractionResultHolder.pass(wandStack);

        // Sneak réservé à useOn (Builder's Hut / Redirector) — on ne fait rien ici
        if (player.isShiftKeyDown()) return InteractionResultHolder.pass(wandStack);

        if (!ColonyLinkWandLinkableHandler.isLinked(wandStack))
        {
            player.sendSystemMessage(Component.literal("§cThis wand is not linked to an AE2 network!"));
            return InteractionResultHolder.fail(wandStack);
        }

        BlockPos lastBuilderPos = getLastBuilderPos(wandStack);
        if (lastBuilderPos == null)
        {
            player.sendSystemMessage(Component.literal("§eNo Builder's Hut linked yet. Sneak + Right-click a Builder's Hut first."));
            return InteractionResultHolder.fail(wandStack);
        }

        ServerLevel serverLevel = (ServerLevel) level;
        return openGUI(wandStack, player, serverLevel, lastBuilderPos)
                ? InteractionResultHolder.success(wandStack)
                : InteractionResultHolder.fail(wandStack);
    }

    /**
     * Clic droit sur un BLOC.
     * - Sneak + clic sur Redirector → lie la wand au redirector
     * - Sneak + clic sur Builder's Hut → lie le builder
     * - Clic simple sur bloc → PASS silencieux (use() gère le clic air)
     */
    @Override
    public net.minecraft.world.InteractionResult useOn(UseOnContext context)
    {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        BlockPos pos = context.getClickedPos();

        if (level.isClientSide() || player == null) return net.minecraft.world.InteractionResult.PASS;

        ItemStack wandStack = context.getItemInHand();

        // Si le joueur clique sur le redirector SANS sneak, on consomme l'action (SUCCESS)
        // pour empêcher NeoForge d'appeler ensuite useWithoutItem du bloc,
        // qui ouvrirait le GUI buffer alors qu'on tient la wand.
        var be = level.getBlockEntity(pos);
        if (be instanceof ColonyLinkRedirectorBlockEntity && !player.isShiftKeyDown())
            return net.minecraft.world.InteractionResult.SUCCESS;

        // Seul le SNEAK est actif sur les autres blocs
        if (!player.isShiftKeyDown()) return net.minecraft.world.InteractionResult.PASS;

        // Sneak + clic sur Redirector → lie la wand
        if (be instanceof ColonyLinkRedirectorBlockEntity redirector)
        {
            if (redirector.getManagedGridNode().getNode() == null)
            {
                player.sendSystemMessage(Component.literal("§cRedirector is not adjacent to a ME Controller!"));
                return net.minecraft.world.InteractionResult.FAIL;
            }

            // Récupère le dernier builder lié à la wand
            BlockPos builderPosForRedirector = getLastBuilderPos(wandStack);
            if (builderPosForRedirector == null)
            {
                player.sendSystemMessage(Component.literal("§cNo Builder's Hut linked to this wand!"));
                player.sendSystemMessage(Component.literal("§7Sneak + Right-click a Builder's Hut first."));
                return net.minecraft.world.InteractionResult.FAIL;
            }

            // Lie le redirector au builder (targetInventory = inventaire du builder's hut)
            redirector.setTargetInventoryPos(builderPosForRedirector);
            redirector.setLinkedBuilderPos(builderPosForRedirector);
            redirector.updateState();

            // Lie la wand au redirector
            setLinkedRedirectorPos(wandStack, pos);

            player.sendSystemMessage(Component.literal("§aWand linked to Colony Link Redirector!"));
            player.sendSystemMessage(Component.literal("§7Builder at " + builderPosForRedirector.toShortString() + " → Redirector linked."));
            return net.minecraft.world.InteractionResult.SUCCESS;
        }

        // Sneak + clic sur Builder's Hut → lie le builder et ouvre le GUI
        if (!ColonyLinkWandLinkableHandler.isLinked(wandStack))
        {
            player.sendSystemMessage(Component.literal("§cThis wand is not linked to an AE2 network!"));
            return net.minecraft.world.InteractionResult.FAIL;
        }

        ServerLevel serverLevel = (ServerLevel) level;

        IWirelessAccessPoint wap = getWap(wandStack, serverLevel);
        if (wap == null)
        {
            player.sendSystemMessage(Component.literal("§cCannot connect to AE2 network!"));
            return net.minecraft.world.InteractionResult.FAIL;
        }

        IGrid grid = wap.getGrid();
        if (grid == null)
        {
            player.sendSystemMessage(Component.literal("§cAE2 network is offline!"));
            return net.minecraft.world.InteractionResult.FAIL;
        }

        IColony colony = IColonyManager.getInstance().getClosestColony(serverLevel, pos);
        if (colony == null) return net.minecraft.world.InteractionResult.PASS;

        IBuilding building = null;
        for (IBuilding b : colony.getServerBuildingManager().getBuildings().values())
        {
            if (b.getPosition().equals(pos))
            {
                building = b;
                break;
            }
        }

        if (building == null) return net.minecraft.world.InteractionResult.PASS;

        if (!building.getBuildingType().getRegistryName().getPath().contains("builder"))
            return net.minecraft.world.InteractionResult.PASS;

        if (!(building instanceof AbstractBuildingStructureBuilder))
            return net.minecraft.world.InteractionResult.PASS;

        setLastBuilderPos(wandStack, pos);

        BlockPos redirectorPos = getLinkedRedirectorPos(wandStack);
        if (redirectorPos != null)
        {
            var redirectorBe = serverLevel.getBlockEntity(redirectorPos);
            if (redirectorBe instanceof ColonyLinkRedirectorBlockEntity r)
                r.setLinkedBuilderPos(pos);
        }

        boolean success = openGUI(wandStack, player, serverLevel, pos);
        return success
                ? net.minecraft.world.InteractionResult.SUCCESS
                : net.minecraft.world.InteractionResult.FAIL;
    }

    private boolean openGUI(ItemStack wandStack, Player player, ServerLevel serverLevel, BlockPos builderPos)
    {
        IWirelessAccessPoint wap = getWap(wandStack, serverLevel);
        if (wap == null)
        {
            player.sendSystemMessage(Component.literal("§cCannot connect to AE2 network!"));
            return false;
        }

        IGrid grid = wap.getGrid();
        if (grid == null)
        {
            player.sendSystemMessage(Component.literal("§cAE2 network is offline!"));
            return false;
        }

        IColony colony = IColonyManager.getInstance().getClosestColony(serverLevel, builderPos);
        if (colony == null)
        {
            player.sendSystemMessage(Component.literal("§cNo colony found nearby!"));
            return false;
        }

        IBuilding building = null;
        for (IBuilding b : colony.getServerBuildingManager().getBuildings().values())
        {
            if (b.getPosition().equals(builderPos))
            {
                building = b;
                break;
            }
        }

        if (!(building instanceof AbstractBuildingStructureBuilder builderBuilding))
        {
            player.sendSystemMessage(Component.literal("§cBuilder's Hut not found — Sneak + Right-click it to re-link."));
            return false;
        }

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

        String buildingName = "N/A";
        var workOrder = builderBuilding.getWorkOrder();
        if (workOrder != null)
        {
            buildingName = workOrder.getDisplayName().getString();
            if (workOrder.getStage() != null)
                buildingName += " [" + workOrder.getStage().name() + "]";
        }

        ICraftingService craftingService = grid.getCraftingService();
        int availableCpus = 0;
        for (var cpu : craftingService.getCpus())
        {
            if (!cpu.isBusy()) availableCpus++;
        }

        String redirectorState = "N/A";
        BlockPos redirectorPos = getLinkedRedirectorPos(wandStack);
        if (redirectorPos != null)
        {
            var rbe = serverLevel.getBlockEntity(redirectorPos);
            if (rbe instanceof ColonyLinkRedirectorBlockEntity redirector)
            {
                appeng.api.networking.IGridNode rnode = redirector.getManagedGridNode().getNode();
                if (rnode != null)
                {
                    ColonyLinkRedirectorBlockEntity.RedirectorState s = redirector.getState();
                    if (s == ColonyLinkRedirectorBlockEntity.RedirectorState.STANDBY)
                        redirectorState = "STANDBY";
                    else if (s == ColonyLinkRedirectorBlockEntity.RedirectorState.NOT_LINKED)
                        redirectorState = "NOT_LINKED";
                    else
                        redirectorState = "LINKED";
                }
                else
                {
                    redirectorState = "NOT_LINKED";
                }
            }
        }

        Map<String, BuildingBuilderResource> neededResources = builderBuilding.getNeededResources();
        List<ColonyLinkPacket.ResourceEntry> entries = new ArrayList<>();

        if (neededResources != null && !neededResources.isEmpty())
        {
            IStorageService storageService = grid.getStorageService();
            KeyCounter inventory = storageService.getCachedInventory();

            for (BuildingBuilderResource resource : neededResources.values())
            {
                ItemStack stack = resource.getItemStack();
                int needed = resource.getAmount();
                int available = resource.getAvailable();
                int missing = needed - available;

                if (missing <= 0) continue;

                ItemStack missingStack = stack.copy();
                missingStack.setCount(Math.min(missing, 64));

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

                entries.add(new ColonyLinkPacket.ResourceEntry(
                        missingStack, status, missing, false, BlockPos.ZERO, new java.util.ArrayList<>()));
            }
        }

        PacketDistributor.sendToPlayer((ServerPlayer) player, new ColonyLinkPacket(
                entries, builderPos, builderName, buildingName, workerStatus, availableCpus, redirectorState,
                ColonyLinkPacket.BuilderRequest.NONE, false, false));
        return true;
    }

    private IWirelessAccessPoint getWap(ItemStack wandStack, ServerLevel level)
    {
        GlobalPos linkedPos = ColonyLinkWandLinkableHandler.getLinkedPos(wandStack);
        if (linkedPos == null) return null;

        ServerLevel targetLevel = level.getServer().getLevel(linkedPos.dimension());
        if (targetLevel == null) return null;

        var blockEntity = targetLevel.getBlockEntity(linkedPos.pos());
        if (blockEntity instanceof IWirelessAccessPoint wap)
            return wap;

        return null;
    }
}