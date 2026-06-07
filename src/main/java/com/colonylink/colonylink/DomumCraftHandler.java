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
            player.sendSystemMessage(Component.translatable("colonylink.whc.clipboard_not_linked"));
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
            player.sendSystemMessage(Component.translatable("colonylink.domum_craft.nothing"));
            return;
        }

        int cpusAvailable = 0;
        for (var cpu : craftingService.getCpus())
            if (!cpu.isBusy()) cpusAvailable++;

        if (cpusAvailable < tocraft.size())
        {
            player.sendSystemMessage(Component.translatable("colonylink.domum_craft.not_enough_cpu", tocraft.size(), cpusAvailable));
            if (cpusAvailable == 0) return;
        }

        CraftHandler.handleCraftRequests(player, tocraft, counts);
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