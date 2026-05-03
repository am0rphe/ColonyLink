package com.colonylink.colonylink;

import appeng.api.config.Actionable;
import appeng.api.implementations.blockentities.IWirelessAccessPoint;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.networking.storage.IStorageService;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.KeyCounter;
import com.ldtteam.domumornamentum.block.IMateriallyTexturedBlock;
import com.ldtteam.domumornamentum.block.IMateriallyTexturedBlockComponent;
import com.ldtteam.domumornamentum.client.model.data.MaterialTextureData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DomumCraftHandler
{
    public static boolean isDomumItem(ItemStack stack)
    {
        if (stack.isEmpty()) return false;
        Item item = stack.getItem();
        if (!(item instanceof BlockItem blockItem)) return false;
        Block block = blockItem.getBlock();
        if (!(block instanceof IMateriallyTexturedBlock)) return false;
        MaterialTextureData data = MaterialTextureData.readFromItemStack(stack);
        return !data.isEmpty();
    }

    public static DomumStatus computeStatus(ItemStack stack, IGrid grid, int needed,
                                            BlockPos redirectorPos, ServerLevel level)
    {
        if (!isDomumItem(stack)) return null;

        Item item = stack.getItem();
        BlockItem blockItem = (BlockItem) item;
        Block block = blockItem.getBlock();
        IMateriallyTexturedBlock texturedBlock = (IMateriallyTexturedBlock) block;

        MaterialTextureData textureData = MaterialTextureData.readFromItemStack(stack);
        BlockItemStateProperties targetBlockState = stack.get(DataComponents.BLOCK_STATE);

        // Vérifie d'abord si l'item est déjà dans le buffer du redirector
        if (redirectorPos != null && level != null && !redirectorPos.equals(BlockPos.ZERO))
        {
            var be = level.getBlockEntity(redirectorPos);
            if (be instanceof ColonyLinkRedirectorBlockEntity redirector)
            {
                int foundInBuffer = 0;
                for (int slot = 0; slot < redirector.buffer.getSlots(); slot++)
                {
                    ItemStack inSlot = redirector.buffer.getStackInSlot(slot);
                    if (inSlot.isEmpty()) continue;
                    if (inSlot.getItem() != stack.getItem()) continue;

                    // Vérifie MaterialTextureData
                    if (!MaterialTextureData.readFromItemStack(inSlot).equals(textureData)) continue;

                    // Vérifie le blockstate (variant : Full, Panel, etc.)
                    BlockItemStateProperties slotBlockState = inSlot.get(DataComponents.BLOCK_STATE);
                    if (!Objects.equals(slotBlockState, targetBlockState)) continue;

                    foundInBuffer += inSlot.getCount();
                }
                if (foundInBuffer >= needed)
                    return new DomumStatus(ResourceStatus.AVAILABLE,
                            new ArrayList<>(), textureData, block);
            }
        }

        // Vérifie ME
        IStorageService storageService = grid.getStorageService();
        ICraftingService craftingService = grid.getCraftingService();
        KeyCounter inventory = storageService.getCachedInventory();

        List<ItemStack> components = new ArrayList<>();
        boolean anyMissing = false;
        boolean anyUnknown = false;
        boolean anyCrafting = false;

        for (IMateriallyTexturedBlockComponent component : texturedBlock.getComponents())
        {
            Block materialBlock = textureData.getTexturedComponents().get(component.getId());
            if (materialBlock == null)
            {
                if (!component.isOptional()) anyUnknown = true;
                continue;
            }

            ItemStack materialStack = new ItemStack(materialBlock, needed);
            AEItemKey aeKey = AEItemKey.of(materialStack);
            long inStorage = inventory.get(aeKey);

            if (inStorage >= needed)
                components.add(materialStack);
            else if (craftingService.isRequesting(aeKey))
            {
                anyCrafting = true;
                components.add(materialStack);
            }
            else if (craftingService.isCraftable(aeKey))
            {
                anyMissing = true;
                components.add(materialStack);
            }
            else
            {
                anyUnknown = true;
                components.add(materialStack);
            }
        }

        ResourceStatus status;
        if (anyUnknown)       status = ResourceStatus.NO_PATTERN;
        else if (anyCrafting) status = ResourceStatus.CRAFTING;
        else if (anyMissing)  status = ResourceStatus.MISSING;
        else                  status = ResourceStatus.CRAFTABLE;

        return new DomumStatus(status, components, textureData, block);
    }

    public static void handleMissingCraft(ServerPlayer player, ItemStack domumStack, int needed)
    {
        ItemStack wandStack = findWandInInventory(player);
        if (wandStack == null || !ColonyLinkWandLinkableHandler.isLinked(wandStack))
        {
            player.sendSystemMessage(Component.literal("§cWand not linked!"));
            return;
        }

        ServerLevel level = player.serverLevel();
        IWirelessAccessPoint wap = getWap(wandStack, level);
        if (wap == null) return;

        IGrid grid = wap.getGrid();
        if (grid == null) return;

        ICraftingService craftingService = grid.getCraftingService();
        IStorageService storageService = grid.getStorageService();
        KeyCounter inventory = storageService.getCachedInventory();

        Item item = domumStack.getItem();
        if (!(item instanceof BlockItem blockItem)) return;
        Block block = blockItem.getBlock();
        if (!(block instanceof IMateriallyTexturedBlock texturedBlock)) return;

        MaterialTextureData textureData = MaterialTextureData.readFromItemStack(domumStack);

        List<ItemStack> tocraft = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();

        for (IMateriallyTexturedBlockComponent component : texturedBlock.getComponents())
        {
            Block materialBlock = textureData.getTexturedComponents().get(component.getId());
            if (materialBlock == null) continue;

            AEItemKey aeKey = AEItemKey.of(new ItemStack(materialBlock));
            long inStorage = inventory.get(aeKey);

            if (inStorage < needed && craftingService.isCraftable(aeKey))
            {
                tocraft.add(new ItemStack(materialBlock));
                counts.add((int) (needed - inStorage));
            }
        }

        if (tocraft.isEmpty())
        {
            player.sendSystemMessage(Component.literal("§eNothing to craft — materials may already be incoming."));
            return;
        }

        int cpusAvailable = 0;
        for (var cpu : craftingService.getCpus())
            if (!cpu.isBusy()) cpusAvailable++;

        if (cpusAvailable < tocraft.size())
        {
            player.sendSystemMessage(Component.literal(
                    "§c[ColonyLink] Not enough free CPUs! Need " + tocraft.size() + ", available: " + cpusAvailable));
            if (cpusAvailable == 0) return;
        }

        CraftHandler.handleCraftRequests(player, tocraft, counts);
    }

    public static void handleDomumCraft(ServerPlayer player, ItemStack domumStack, int needed, BlockPos redirectorPos)
    {
        ItemStack wandStack = findWandInInventory(player);
        if (wandStack == null || !ColonyLinkWandLinkableHandler.isLinked(wandStack)) return;

        ServerLevel level = player.serverLevel();
        IWirelessAccessPoint wap = getWap(wandStack, level);
        if (wap == null) return;

        IGrid grid = wap.getGrid();
        if (grid == null) return;

        var be = level.getBlockEntity(redirectorPos);
        if (!(be instanceof ColonyLinkRedirectorBlockEntity redirector))
        {
            player.sendSystemMessage(Component.literal("§cRedirector not found!"));
            return;
        }

        Item item = domumStack.getItem();
        if (!(item instanceof BlockItem blockItem)) return;
        Block block = blockItem.getBlock();
        if (!(block instanceof IMateriallyTexturedBlock texturedBlock)) return;

        MaterialTextureData textureData = MaterialTextureData.readFromItemStack(domumStack);
        IStorageService storageService = grid.getStorageService();
        appeng.api.networking.security.IActionSource actionSource =
                appeng.api.networking.security.IActionSource.ofPlayer(player,
                        (appeng.api.networking.security.IActionHost) wap);

        List<AEItemKey> extractedKeys = new ArrayList<>();
        List<Long> extractedCounts = new ArrayList<>();

        for (IMateriallyTexturedBlockComponent component : texturedBlock.getComponents())
        {
            Block materialBlock = textureData.getTexturedComponents().get(component.getId());
            if (materialBlock == null)
            {
                if (!component.isOptional())
                {
                    player.sendSystemMessage(Component.literal(
                            "§cMissing required material for component: " + component.getId()));
                    refundExtracted(storageService, extractedKeys, extractedCounts, actionSource);
                    return;
                }
                continue;
            }

            AEItemKey aeKey = AEItemKey.of(new ItemStack(materialBlock));
            long extracted = storageService.getInventory().extract(aeKey, needed, Actionable.MODULATE, actionSource);

            if (extracted < needed)
            {
                player.sendSystemMessage(Component.literal(
                        "§cCould not extract enough " + new ItemStack(materialBlock).getDisplayName().getString()
                                + " from ME! Got " + extracted + "/" + needed));
                if (extracted > 0)
                {
                    extractedKeys.add(aeKey);
                    extractedCounts.add(extracted);
                }
                refundExtracted(storageService, extractedKeys, extractedCounts, actionSource);
                return;
            }

            extractedKeys.add(aeKey);
            extractedCounts.add(extracted);
        }

        // Construit le MaterialTextureData
        MaterialTextureData.Builder builder = MaterialTextureData.builder();
        for (IMateriallyTexturedBlockComponent component : texturedBlock.getComponents())
        {
            Block materialBlock = textureData.getTexturedComponents().get(component.getId());
            if (materialBlock != null)
                builder.setComponent(component.getId(), materialBlock);
        }

        // Copie l'ItemStack original COMPLET (blockstate variant + tous les components)
        // puis réécrit seulement le MaterialTextureData
        ItemStack result = domumStack.copy();
        result.setCount(needed);
        builder.writeToItemStack(result);

        // Insère dans le buffer du redirector
        IItemHandler bufferHandler = redirector.buffer;
        ItemStack remainder = result.copy();

        for (int slot = 0; slot < bufferHandler.getSlots() && !remainder.isEmpty(); slot++)
            remainder = bufferHandler.insertItem(slot, remainder, false);

        if (!remainder.isEmpty())
        {
            refundExtracted(storageService, extractedKeys, extractedCounts, actionSource);
            player.sendSystemMessage(Component.literal("§cRedirector buffer is full! Could not insert DO blocks."));
            return;
        }

        player.sendSystemMessage(Component.literal(
                "§a[ColonyLink] Crafted " + needed + "x " + domumStack.getDisplayName().getString()
                        + " → inserted into redirector buffer!"));
    }

    private static void refundExtracted(
            IStorageService storageService,
            List<AEItemKey> keys,
            List<Long> counts,
            appeng.api.networking.security.IActionSource actionSource)
    {
        for (int i = 0; i < keys.size(); i++)
            storageService.getInventory().insert(keys.get(i), counts.get(i), Actionable.MODULATE, actionSource);
    }

    private static ItemStack findWandInInventory(ServerPlayer player)
    {
        for (ItemStack stack : player.getInventory().items)
            if (stack.getItem() instanceof ColonyLinkWand)
                return stack;
        return null;
    }

    private static IWirelessAccessPoint getWap(ItemStack wandStack, ServerLevel level)
    {
        net.minecraft.core.GlobalPos linkedPos = ColonyLinkWandLinkableHandler.getLinkedPos(wandStack);
        if (linkedPos == null) return null;
        ServerLevel targetLevel = level.getServer().getLevel(linkedPos.dimension());
        if (targetLevel == null) return null;
        var blockEntity = targetLevel.getBlockEntity(linkedPos.pos());
        if (blockEntity instanceof IWirelessAccessPoint wap) return wap;
        return null;
    }

    public record DomumStatus(
            ResourceStatus status,
            List<ItemStack> materials,
            MaterialTextureData textureData,
            Block block
    ) {}
}