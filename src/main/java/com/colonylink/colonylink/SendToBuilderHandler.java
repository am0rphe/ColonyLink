package com.colonylink.colonylink;

import appeng.api.config.Actionable;
import appeng.api.implementations.blockentities.IWirelessAccessPoint;
import appeng.api.networking.IGrid;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageService;
import appeng.api.stacks.AEItemKey;
import appeng.api.storage.MEStorage;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingWareHouse;
import com.minecolonies.core.colony.buildings.AbstractBuildingStructureBuilder;
import com.minecolonies.core.colony.buildings.utils.BuildingBuilderResource;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.List;

/**
 * Gère le « Send to Builder » : déplace des items vers le Builder's Hut MineColonies.
 *
 * v1.4.9 — Send unifié & zéro voiding :
 *   - L'extraction respecte la priorité du Redirector pour TOUS les items (Domum inclus) :
 *       • carte warehouse + priorité ON  → warehouse d'abord, puis ME
 *       • carte warehouse + priorité OFF → ME d'abord, puis warehouse (fallback)
 *       • pas de carte warehouse          → ME uniquement
 *     Un bloc Domum FINI présent dans la warehouse est ainsi livré directement au
 *     builder, sans passer par AE2 ni par le buffer du Redirector.
 *   - Les deux sources passent par des primitives void-safe (pullFromMe / pullFromWarehouse) :
 *     tout surplus non logé dans le builder revient à sa source (ME ou racks), puis à
 *     l'inventaire du joueur en ultime recours. Aucun item ne peut être détruit.
 *   - L'ancienne « étape buffer DO » a été retirée : le buffer du Redirector ne contient
 *     que des DomumPatternItem, jamais de blocs finis — cette étape ne faisait rien.
 */
public class SendToBuilderHandler
{
    public static void handleSendToBuilder(ServerPlayer player, ItemStack stack,
                                           BlockPos builderPos, int realCount)
    {
        // ── v1.4.9 — cross-dimension : refus propre AVANT de consommer du RF ──
        {
            ItemStack wandForDim = findWandInInventory(player);
            if (wandForDim != null)
            {
                ResourceKey<Level> builderDim =
                        ColonyLinkWandLinkableHandler.getBuilderDimension(wandForDim, builderPos);
                if (builderDim != null && !player.serverLevel().dimension().equals(builderDim))
                {
                    player.sendSystemMessage(Component.literal(
                            "§c[ColonyLink] This builder is in another dimension (§f"
                                    + builderDim.location().getPath()
                                    + "§c). The Clipboard cannot reach colonies across dimensions."));
                    return;
                }
            }
        }

        // ── Coût RF ───────────────────────────────────────────────────────────
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
            player.sendSystemMessage(Component.literal("§cClipboard not found or not linked!"));
            return;
        }

        ServerLevel level = player.serverLevel();

        // ── Substitution d'outils ─────────────────────────────────────────────
        if (BuilderToolHelper.isTool(stack))
        {
            IWirelessAccessPoint wap = getWap(wandStack, level);
            if (wap != null && wap.getGrid() != null)
            {
                IGrid grid = wap.getGrid();
                appeng.api.stacks.KeyCounter inv = grid.getStorageService().getCachedInventory();
                appeng.api.networking.crafting.ICraftingService cs = grid.getCraftingService();

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
                    stack = sub.displayStack().copyWithCount(stack.getCount());
                    player.sendSystemMessage(Component.literal(
                            "§6[ColonyLink] Tool upgraded: §f" + stack.getDisplayName().getString()));
                }
                else if (sub.action() == BuilderToolHelper.SubstituteAction.CRAFT)
                {
                    CraftHandler.handleCraftRequest(player, sub.displayStack(), 1);
                    player.sendSystemMessage(Component.literal(
                            "§a[ColonyLink] Crafting best tool: §f"
                                    + sub.displayStack().getDisplayName().getString()));
                    return;
                }
            }
        }

        BlockPos redirectorPos = ColonyLinkWandLinkableHandler.getActiveRedirectorPos(wandStack);
        if (redirectorPos == null)
        {
            player.sendSystemMessage(Component.literal("§cNo Redirector linked to this Clipboard!"));
            player.sendSystemMessage(Component.literal("§7Sneak + Right-click a Colony Link Redirector with the Clipboard."));
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

        // v1.4.9 — fail-off strict côté MineColonies : tout doit être chargé.
        // Le builder (destination d'insertion) est obligatoire ; le warehouse l'est aussi
        // dès qu'une carte est présente (le send peut y puiser). Sinon on annule proprement.
        IColony sendColony = IColonyManager.getInstance().getClosestColony(level, targetPos);
        if (!ColonyLinkChunkUtil.buildingFullyLoaded(level, sendColony, targetPos))
        {
            player.sendSystemMessage(Component.literal(
                    "§c[ColonyLink] The Builder's Hut is in unloaded chunks. " +
                            "Move closer or use a chunk loader, then try again."));
            return;
        }
        if (redirector.hasWarehouseCard()
                && !ColonyLinkChunkUtil.colonyWarehousesFullyLoaded(level, sendColony))
        {
            player.sendSystemMessage(Component.literal(
                    "§c[ColonyLink] Your colony's Warehouse is in unloaded chunks. " +
                            "Move closer or use a chunk loader, then try again."));
            return;
        }

        // ── Fix #3 : utilise getBuildingHandlers() au lieu de getCapability direct ──
        // Le Builder's Hut MineColonies n'expose pas d'IItemHandler via capability
        // sur sa position de bloc. On passe par getBuildingHandlers() qui interroge
        // IBuilding.getContainers() pour trouver les racks réels.
        List<IItemHandler> buildingHandlers = redirector.getBuildingHandlers();
        if (buildingHandlers.isEmpty())
        {
            // Dernier fallback : capability directe (non-MineColonies)
            IItemHandler direct = level.getCapability(Capabilities.ItemHandler.BLOCK, targetPos, null);
            if (direct == null)
            {
                player.sendSystemMessage(Component.literal(
                        "§c[ColonyLink] No inventory found for builder hut. " +
                                "Make sure the hut has at least one rack placed."));
                return;
            }
            buildingHandlers = List.of(direct);
        }
        // ─────────────────────────────────────────────────────────────────────

        if (redirector.getState() == ColonyLinkRedirectorBlockEntity.RedirectorState.STANDBY)
        {
            player.sendSystemMessage(Component.literal(
                    "§6[ColonyLink] Builder inventory is full — free up space or wait for the builder to use items."));
            return;
        }

        IWirelessAccessPoint wap = getWap(wandStack, level);
        if (wap == null) { player.sendSystemMessage(Component.literal("§cCannot connect to AE2 network!")); return; }

        IGrid grid = wap.getGrid();
        if (grid == null) { player.sendSystemMessage(Component.literal("§cAE2 network is offline!")); return; }

        IStorageService storageService = grid.getStorageService();
        MEStorage inventory = storageService.getInventory();
        IActionSource actionSource = IActionSource.ofPlayer(player,
                (appeng.api.networking.security.IActionHost) wap);

        AEItemKey aeKey = AEItemKey.of(stack);
        int remaining = realCount;

        // ── Extraction selon la priorité du Redirector (v1.4.9) ───────────────
        // S'applique à TOUS les items, Domum inclus : un bloc Domum fini présent
        // dans la warehouse est livré directement (sans AE2), via pullFromWarehouse.
        boolean warehouseEnabled  = redirector.hasWarehouseCard();
        boolean warehousePriority = warehouseEnabled && redirector.isWarehousePriority();

        if (warehouseEnabled && warehousePriority)
        {
            // Priorité warehouse : racks d'abord, puis ME.
            remaining = pullFromWarehouse(player, level, stack, remaining, redirector, buildingHandlers);
            remaining = pullFromMe(aeKey, remaining, inventory, actionSource, redirector, buildingHandlers);
        }
        else if (warehouseEnabled)
        {
            // Carte présente sans priorité : ME d'abord, puis warehouse en fallback.
            remaining = pullFromMe(aeKey, remaining, inventory, actionSource, redirector, buildingHandlers);
            remaining = pullFromWarehouse(player, level, stack, remaining, redirector, buildingHandlers);
        }
        else
        {
            // Pas de carte warehouse : ME uniquement.
            remaining = pullFromMe(aeKey, remaining, inventory, actionSource, redirector, buildingHandlers);
        }

        long totalInserted = realCount - remaining;

        // ── Feedback ──────────────────────────────────────────────────────────
        if (totalInserted > 0)
        {
            // Notify MineColonies that the builder hut inventory changed so the
            // builder re-evaluates its needed resources immediately — without this
            // the citizen stays blocked until a manual restart or item re-placement.
            try
            {
                IColony colony = IColonyManager.getInstance().getClosestColony(level, builderPos);
                if (colony != null)
                {
                    for (IBuilding b : colony.getServerBuildingManager().getBuildings().values())
                    {
                        if (!b.getPosition().equals(builderPos)) continue;
                        b.markDirty();
                        // v1.4.11 — crédite la ressource livrée dans le bilan du chantier
                        // (getNeededResources) pour que la ligne disparaisse immédiatement,
                        // même quand le builder est loin et ne recalcule pas. Voir le helper.
                        creditDeliveredResource(b, stack, (int) totalInserted);
                        break;
                    }
                }
            }
            catch (Exception e)
            {
                ColonyLink.LOGGER.debug("[ColonyLink] markDirty after send failed: {}", e.getMessage());
            }

            player.sendSystemMessage(Component.literal(
                    "§a[ColonyLink] Sent " + totalInserted + "x "
                            + stack.getDisplayName().getString() + " to builder!"));
            if (remaining > 0)
                player.sendSystemMessage(Component.literal(
                        "§6[ColonyLink] Builder inventory is full — free up space or wait for the builder to use items."));
        }
        else
        {
            // Si le redirector vient de passer en STANDBY, le message orange suffit — pas de doublon rouge.
            if (redirector.getState() == ColonyLinkRedirectorBlockEntity.RedirectorState.STANDBY)
                player.sendSystemMessage(Component.literal(
                        "§6[ColonyLink] Builder inventory is full — free up space or wait for the builder to use items."));
            else
                player.sendSystemMessage(Component.literal(
                        "§c[ColonyLink] Could not send " + stack.getDisplayName().getString()
                                + " — not available in ME or Warehouse."));
        }
    }

    /**
     * v1.4.11 — Crédite immédiatement la ressource livrée dans le bilan du chantier.
     *
     * La liste « Priority Request » du Clipboard vient de getNeededResources()
     * (miss = amount - available), où `available` = ce que le builder possède dans son hut.
     * MineColonies ne recalcule `available` que sur le TICK de travail du builder — gelé quand
     * le joueur est loin. Résultat : on dépose bien l'item dans le rack, mais `available` reste
     * périmé → la ligne ne disparaît pas (et on peut cascader des envois) tant qu'on n'est pas
     * revenu près de la colonie.
     *
     * Ici, dès le dépôt, on crédite `available` de la quantité réellement insérée (plafonné au
     * manque). getNeededResources() reflète alors le dépôt → le ticker (qui tourne même à
     * distance, sans gating) recalcule la liste → la ligne disparaît immédiatement et uniformément.
     *
     * Sûr : le recalcul natif du builder (au retour) fait setAvailable(0) puis ré-additionne le
     * contenu RÉEL des inventaires (rack inclus) → notre crédit anticipé est ÉCRASÉ par la valeur
     * exacte, jamais double-compté. La map est unmodifiable mais ses valeurs sont mutables.
     * 100% serveur, coût ponctuel au clic (hors tick).
     */
    private static void creditDeliveredResource(IBuilding building, ItemStack delivered, int insertedCount)
    {
        if (insertedCount <= 0 || delivered.isEmpty()) return;
        if (!(building instanceof AbstractBuildingStructureBuilder bb)) return;

        try
        {
            var needed = bb.getNeededResources();   // map vivante : valeurs mutables
            if (needed == null) return;

            for (BuildingBuilderResource res : needed.values())
            {
                if (!ItemStack.isSameItemSameComponents(res.getItemStack(), delivered)) continue;

                int missing = res.getAmount() - res.getAvailable();
                if (missing <= 0) return;            // déjà couvert, rien à créditer
                res.addAvailable(Math.min(missing, insertedCount));
                bb.markDirty();
                return;                              // une seule ressource correspond par item
            }
        }
        catch (Exception e)
        {
            ColonyLink.LOGGER.debug("[ColonyLink] needed-resource credit skipped: {}", e.getMessage());
        }
    }

    // ── Primitives d'extraction (void-safe) ───────────────────────────────────

    /**
     * v1.4.9 — Vide jusqu'à {@code remaining} items depuis le réseau ME vers les
     * handlers du builder. Tout surplus non logé revient dans le ME (jamais de void).
     *
     * @return le nouveau remaining
     */
    private static int pullFromMe(
            AEItemKey aeKey, int remaining, MEStorage inventory, IActionSource actionSource,
            ColonyLinkRedirectorBlockEntity redirector, List<IItemHandler> buildingHandlers)
    {
        if (aeKey == null) return remaining;

        while (remaining > 0)
        {
            int batchSize = Math.min(remaining, 64);
            long extracted = inventory.extract(aeKey, batchSize, Actionable.MODULATE, actionSource);
            if (extracted <= 0) break;

            ItemStack toInsert = aeKey.toStack((int) extracted);
            ItemStack leftOver = insertIntoHandlers(buildingHandlers, toInsert);
            int sent = (int) extracted - leftOver.getCount();
            remaining -= sent;

            if (!leftOver.isEmpty())
            {
                // Builder plein → on remet le surplus dans le ME (jamais de void).
                inventory.insert(aeKey, leftOver.getCount(), Actionable.MODULATE, actionSource);
                redirector.setState(ColonyLinkRedirectorBlockEntity.RedirectorState.STANDBY);
                break;
            }
        }
        return remaining;
    }

    /**
     * v1.4.9 — Vide jusqu'à {@code remaining} items (comparaison exacte des composants)
     * depuis les racks de la Warehouse vers les handlers du builder. Tout surplus non
     * logé revient dans les racks, puis dans l'inventaire du joueur en ultime recours.
     * Jamais de void.
     *
     * @return le nouveau remaining
     */
    private static int pullFromWarehouse(
            ServerPlayer player, ServerLevel level, ItemStack stack, int remaining,
            ColonyLinkRedirectorBlockEntity redirector, List<IItemHandler> buildingHandlers)
    {
        if (remaining <= 0) return remaining;

        IColony colony = IColonyManager.getInstance().getClosestColony(level, redirector.getBlockPos());
        if (colony == null) return remaining;

        for (IBuilding building : colony.getServerBuildingManager().getBuildings().values())
        {
            if (!(building instanceof BuildingWareHouse warehouse)) continue;
            try
            {
                var containerList = warehouse.getContainers();
                if (containerList == null || containerList.isEmpty()) break;

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

                        ItemStack leftOver = insertIntoHandlers(buildingHandlers, took);
                        int sent = took.getCount() - leftOver.getCount();
                        remaining -= sent;

                        if (!leftOver.isEmpty())
                        {
                            // Builder plein → surplus remis dans le rack, puis au joueur.
                            ItemStack back = leftOver.copy();
                            for (int s2 = 0; s2 < rackHandler.getSlots() && !back.isEmpty(); s2++)
                                back = rackHandler.insertItem(s2, back, false);
                            if (!back.isEmpty())
                                player.getInventory().placeItemBackInInventory(back);
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
        return remaining;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Insère un ItemStack dans une liste de handlers (racks MineColonies).
     * Retourne le surplus non inséré.
     */
    static ItemStack insertIntoHandlers(List<IItemHandler> handlers, ItemStack stack)
    {
        ItemStack remainder = stack.copy();
        for (IItemHandler handler : handlers)
        {
            for (int slot = 0; slot < handler.getSlots() && !remainder.isEmpty(); slot++)
                remainder = handler.insertItem(slot, remainder, false);
            if (remainder.isEmpty()) return ItemStack.EMPTY;
        }
        return remainder;
    }

    static ItemStack findWandInInventory(ServerPlayer player)
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