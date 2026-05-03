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
    /**
     * Bug 1 fix : envoie la totalité de la quantité manquante en une seule fois,
     * en bouclant sur les extractions ME par tranches de 64 jusqu'à épuisement.
     *
     * @param realCount quantité totale réelle demandée (peut dépasser 64)
     */
    public static void handleSendToBuilder(ServerPlayer player, ItemStack stack,
                                           BlockPos builderPos, int realCount)
    {
        ItemStack wandStack = findWandInInventory(player);
        if (wandStack == null || !ColonyLinkWandLinkableHandler.isLinked(wandStack))
        {
            player.sendSystemMessage(Component.literal("§cWand not found or not linked!"));
            return;
        }

        ServerLevel level = player.serverLevel();

        // Récupère le redirector lié
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

        IItemHandler targetHandler = level.getCapability(
                Capabilities.ItemHandler.BLOCK, targetPos, null);
        if (targetHandler == null)
        {
            player.sendSystemMessage(Component.literal(
                    "§cTarget inventory not found at " + targetPos.toShortString()));
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

        if (redirector.getState() == ColonyLinkRedirectorBlockEntity.RedirectorState.STANDBY)
        {
            player.sendSystemMessage(Component.literal(
                    "§6Redirector is in STANDBY - target inventory is full!"));
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

        // ── Étape 1 : puise dans le buffer DO si applicable ──────────────────
        if (isDomum && redirector != null)
        {
            MaterialTextureData targetData = MaterialTextureData.readFromItemStack(stack);
            IItemHandler buffer = redirector.buffer;

            for (int slot = 0; slot < buffer.getSlots() && remaining > 0; slot++)
            {
                ItemStack inSlot = buffer.getStackInSlot(slot);
                if (inSlot.isEmpty()) continue;
                if (inSlot.getItem() != stack.getItem()) continue;

                MaterialTextureData slotData = MaterialTextureData.readFromItemStack(inSlot);
                net.minecraft.world.item.component.BlockItemStateProperties slotBsp =
                        inSlot.get(net.minecraft.core.component.DataComponents.BLOCK_STATE);
                net.minecraft.world.item.component.BlockItemStateProperties targetBsp =
                        stack.get(net.minecraft.core.component.DataComponents.BLOCK_STATE);
                if (!slotData.equals(targetData)) continue;
                if (!java.util.Objects.equals(slotBsp, targetBsp)) continue;

                int toExtract = Math.min(inSlot.getCount(), remaining);
                ItemStack took = buffer.extractItem(slot, toExtract, false);
                if (took.isEmpty()) continue;

                ItemStack leftOver = insertIntoHandler(targetHandler, took);
                long sent = took.getCount() - leftOver.getCount();
                totalInserted += sent;
                remaining -= (int) sent;

                // Remet le trop-plein dans le buffer
                if (!leftOver.isEmpty())
                {
                    for (int s2 = 0; s2 < buffer.getSlots() && !leftOver.isEmpty(); s2++)
                        leftOver = buffer.insertItem(s2, leftOver, false);
                    break; // cible pleine
                }
            }
        }

        // ── Étape 2 : puise dans le ME pour le reste ─────────────────────────
        while (remaining > 0)
        {
            int batchSize = Math.min(remaining, 64);
            long extracted = inventory.extract(aeKey, batchSize, Actionable.MODULATE, actionSource);

            if (extracted <= 0) break; // plus rien en ME

            ItemStack toInsert = aeKey.toStack((int) extracted);
            ItemStack leftOver = insertIntoHandler(targetHandler, toInsert);
            long sent = extracted - leftOver.getCount();
            totalInserted += sent;
            remaining -= (int) sent;

            // Remet le trop-plein dans le ME
            if (!leftOver.isEmpty())
            {
                inventory.insert(aeKey, leftOver.getCount(), Actionable.MODULATE, actionSource);
                redirector.setState(ColonyLinkRedirectorBlockEntity.RedirectorState.STANDBY);
                break; // cible pleine
            }
        }

        // ── Feedback ─────────────────────────────────────────────────────────
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

    private static ItemStack insertIntoHandler(IItemHandler handler, ItemStack stack)
    {
        ItemStack remainder = stack.copy();
        for (int slot = 0; slot < handler.getSlots() && !remainder.isEmpty(); slot++)
            remainder = handler.insertItem(slot, remainder, false);
        return remainder;
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