package com.colonylink.colonylink;

import appeng.api.config.Actionable;
import appeng.api.implementations.blockentities.IWirelessAccessPoint;
import appeng.api.networking.IGrid;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageService;
import appeng.api.stacks.AEItemKey;
import appeng.api.storage.MEStorage;
import com.ldtteam.domumornamentum.client.model.data.MaterialTextureData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

public class SendToBuilderHandler
{
    public static void handleSendToBuilder(ServerPlayer player, ItemStack stack, BlockPos builderPos)
    {
        ItemStack wandStack = findWandInInventory(player);
        if (wandStack == null || !ColonyLinkWandLinkableHandler.isLinked(wandStack))
        {
            player.sendSystemMessage(Component.literal("§cWand not found or not linked!"));
            return;
        }

        ServerLevel level = player.serverLevel();

        // Vérifie le redirector lié
        BlockPos redirectorPos = getLinkedRedirectorPos(wandStack);
        ColonyLinkRedirectorBlockEntity redirector = null;
        if (redirectorPos != null)
        {
            var be = level.getBlockEntity(redirectorPos);
            if (be instanceof ColonyLinkRedirectorBlockEntity r)
                redirector = r;
        }

        // Vérifie l'inventaire cible
        BlockPos targetPos = redirector != null ? redirector.getTargetInventoryPos() : null;
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

        // 1. Cherche d'abord dans le buffer du redirector
        boolean isDomum = DomumCraftHandler.isDomumItem(stack);
        ItemStack extracted = ItemStack.EMPTY;

        if (redirector != null && isDomum)
        {
            MaterialTextureData targetData = MaterialTextureData.readFromItemStack(stack);
            IItemHandler buffer = redirector.buffer;

            for (int slot = 0; slot < buffer.getSlots(); slot++)
            {
                ItemStack inSlot = buffer.getStackInSlot(slot);
                if (inSlot.isEmpty()) continue;
                if (inSlot.getItem() != stack.getItem()) continue;

                MaterialTextureData slotData = MaterialTextureData.readFromItemStack(inSlot);
                if (!slotData.equals(targetData)) continue;

                // Extrait ce qu'on peut
                int toExtract = Math.min(inSlot.getCount(), stack.getCount());
                ItemStack took = buffer.extractItem(slot, toExtract, false);
                if (!took.isEmpty())
                {
                    extracted = took;
                    break;
                }
            }
        }

        // 2. Si pas trouvé dans le buffer, cherche dans le ME
        if (extracted.isEmpty())
        {
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

            if (redirector != null && redirector.getState() == ColonyLinkRedirectorBlockEntity.RedirectorState.STANDBY)
            {
                player.sendSystemMessage(Component.literal("§6Redirector is in STANDBY - target inventory is full!"));
                return;
            }

            IStorageService storageService = grid.getStorageService();
            MEStorage inventory = storageService.getInventory();
            IActionSource actionSource = IActionSource.ofPlayer(player,
                    (appeng.api.networking.security.IActionHost) wap);

            AEItemKey aeKey = AEItemKey.of(stack);
            long extractedCount = inventory.extract(aeKey, stack.getCount(), Actionable.MODULATE, actionSource);

            if (extractedCount <= 0)
            {
                player.sendSystemMessage(Component.literal(
                        "§cCould not extract " + stack.getDisplayName().getString() + " from ME or redirector buffer!"));
                return;
            }

            extracted = stack.copy();
            extracted.setCount((int) extractedCount);
        }

        // 3. Insère dans l'inventaire cible
        ItemStack toInsert = extracted.copy();
        long inserted = 0;

        for (int i = 0; i < targetHandler.getSlots(); i++)
        {
            if (toInsert.isEmpty()) break;
            ItemStack remainder = targetHandler.insertItem(i, toInsert, false);
            inserted += toInsert.getCount() - remainder.getCount();
            toInsert = remainder;
        }

        if (!toInsert.isEmpty())
        {
            // Remet le reste dans le buffer ou le ME
            if (redirector != null && isDomum)
            {
                IItemHandler buffer = redirector.buffer;
                for (int slot = 0; slot < buffer.getSlots() && !toInsert.isEmpty(); slot++)
                    toInsert = buffer.insertItem(slot, toInsert, false);
            }
            else
            {
                IWirelessAccessPoint wap = getWap(wandStack, level);
                if (wap != null && wap.getGrid() != null)
                {
                    IActionSource actionSource = IActionSource.ofPlayer(player,
                            (appeng.api.networking.security.IActionHost) wap);
                    AEItemKey aeKey = AEItemKey.of(toInsert);
                    wap.getGrid().getStorageService().getInventory()
                            .insert(aeKey, toInsert.getCount(), Actionable.MODULATE, actionSource);
                }
            }

            if (redirector != null)
                redirector.setState(ColonyLinkRedirectorBlockEntity.RedirectorState.STANDBY);

            player.sendSystemMessage(Component.literal(
                    "§6Target inventory full! " + inserted + "x sent, " + toInsert.getCount() + "x returned."));
        }
        else
        {
            player.sendSystemMessage(Component.literal(
                    "§aSent " + inserted + "x " + stack.getDisplayName().getString() + " to target inventory!"));
        }
    }

    private static BlockPos getLinkedRedirectorPos(ItemStack wandStack)
    {
        var data = wandStack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
        if (data == null) return null;
        var tag = data.copyTag();
        if (!tag.contains("redirector_x")) return null;
        return new BlockPos(tag.getInt("redirector_x"), tag.getInt("redirector_y"), tag.getInt("redirector_z"));
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