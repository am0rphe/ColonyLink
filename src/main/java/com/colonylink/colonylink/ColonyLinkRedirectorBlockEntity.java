package com.colonylink.colonylink;

import appeng.api.config.Actionable;
import appeng.api.networking.GridFlags;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import com.google.common.collect.ImmutableSet;
import com.ldtteam.domumornamentum.block.IMateriallyTexturedBlock;
import com.ldtteam.domumornamentum.block.IMateriallyTexturedBlockComponent;
import com.ldtteam.domumornamentum.client.model.data.MaterialTextureData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * ColonyLinkRedirectorBlockEntity — v1.4.2
 *
 * Ajouts v1.4.2 :
 *   - Implémente ICraftingProvider → expose les DomumPatterns du buffer au réseau AE2
 *   - pushPattern() → met le craft Domum en queue, exécuté au tick suivant
 *   - isBusy() → true si une craft est en cours (1 craft à la fois par Redirector)
 *   - buffer.isItemValid() → filtre : accepte uniquement DomumPatternItem
 *     (les Encoded Patterns AE2 sont rejetés)
 *   - notifyPatternChange() → appelle ICraftingProvider.requestUpdate() quand
 *     le buffer change, pour que AE2 rescanne les patterns disponibles
 */
public class ColonyLinkRedirectorBlockEntity extends BlockEntity
        implements IInWorldGridNodeHost, ICraftingRequester, ICraftingProvider, MenuProvider
{
    // ── Constantes buffer ─────────────────────────────────────────────────────

    public static int BUFFER_ROWS() { return ColonyLinkConfig.REDIRECTOR_BUFFER_ROWS.get(); }
    public static int BUFFER_COLS() { return ColonyLinkConfig.REDIRECTOR_BUFFER_COLS.get(); }
    public static int BUFFER_SIZE() { return BUFFER_ROWS() * BUFFER_COLS(); }

    public static final int BUFFER_ROWS = 3;
    public static final int BUFFER_COLS = 9;
    public static final int BUFFER_SIZE = BUFFER_ROWS * BUFFER_COLS;

    // ── État ──────────────────────────────────────────────────────────────────

    private BlockPos targetInventoryPos = null;
    private BlockPos linkedBuilderPos   = null;
    private String  linkedBuilderName  = "N/A";
    private final Set<ICraftingLink> craftingLinks = new HashSet<>();

    private boolean ae2ActiveClientCache = false;
    private boolean warehousePriority = false;
    private boolean warehousePriorityClientCache = false;

    /** true si un craft Domum est en cours d'exécution ce tick. */
    private boolean craftBusy = false;

    /** File d'attente des crafts Domum à exécuter (un par tick). */
    private final Queue<PendingDomumCraft> craftQueue = new ConcurrentLinkedQueue<>();

    // ── Buffer — accepte UNIQUEMENT les DomumPatternItem ─────────────────────

    public final ItemStackHandler buffer = new ItemStackHandler(BUFFER_SIZE())
    {
        @Override
        public boolean isItemValid(int slot, ItemStack stack)
        {
            // Seuls les DomumPatternItem sont acceptés dans ce buffer.
            // Les Encoded Patterns AE2 standard sont rejetés.
            return stack.isEmpty() || stack.getItem() instanceof DomumPatternItem;
        }

        @Override
        protected void onContentsChanged(int slot)
        {
            setChanged();
            // Notifie AE2 que la liste de patterns disponibles a changé
            notifyPatternChange();
        }
    };

    public final ItemStackHandler warehouseCardSlot = new ItemStackHandler(1)
    {
        @Override
        public boolean isItemValid(int slot, ItemStack stack)
        { return stack.getItem() instanceof WarehouseLinkCard; }

        @Override
        protected void onContentsChanged(int slot)
        {
            if (warehouseCardSlot.getStackInSlot(0).isEmpty())
                warehousePriority = false;
            setChanged();
            if (level != null)
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    };

    // ── États Redirector ──────────────────────────────────────────────────────

    public enum RedirectorState { NOT_LINKED, LINKED, STANDBY, NO_CONTROLLER }

    private RedirectorState state = RedirectorState.NO_CONTROLLER;

    // ── Grid node ─────────────────────────────────────────────────────────────

    private final IManagedGridNode gridNode = GridHelper.createManagedNode(this,
                    new IGridNodeListener<ColonyLinkRedirectorBlockEntity>()
                    {
                        @Override
                        public void onSaveChanges(ColonyLinkRedirectorBlockEntity owner, IGridNode node)
                        { owner.setChanged(); }

                        @Override
                        public void onStateChanged(ColonyLinkRedirectorBlockEntity owner, IGridNode node,
                                                   IGridNodeListener.State state)
                        { owner.onGridStateChanged(); }
                    })
            .setInWorldNode(true)
            .setFlags(GridFlags.REQUIRE_CHANNEL)
            .setVisualRepresentation(ColonyLinkRegistry.REDIRECTOR_BLOCK_ITEM.get())
            .setIdlePowerUsage(1.0)
            // ICraftingRequester : enregistre les crafts demandés au réseau
            .addService(ICraftingRequester.class, this)
            // ICraftingProvider : expose les DomumPatterns au réseau AE2
            .addService(ICraftingProvider.class, this);

    // ── Constructeur ──────────────────────────────────────────────────────────

    public ColonyLinkRedirectorBlockEntity(BlockPos pos, BlockState state)
    {
        super(ColonyLinkRegistry.REDIRECTOR_BLOCK_ENTITY.get(), pos, state);
    }

    // ── MenuProvider ──────────────────────────────────────────────────────────

    @Override
    public Component getDisplayName() { return Component.literal("Colony Link Redirector"); }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player)
    { return new ColonyLinkRedirectorMenu(containerId, playerInventory, this); }

    // ── ICraftingProvider — exposition des patterns au réseau AE2 ─────────────

    /**
     * Retourne la liste de tous les DomumPatternDetails disponibles dans le buffer.
     * AE2 appelle cette méthode pour construire le plan de craft.
     * Appelée depuis le server thread uniquement.
     */
    @Override
    public List<IPatternDetails> getAvailablePatterns()
    {
        if (level == null || level.isClientSide()) return List.of();

        List<IPatternDetails> patterns = new ArrayList<>();
        HolderLookup.Provider provider = level.registryAccess();

        for (int slot = 0; slot < buffer.getSlots(); slot++)
        {
            ItemStack stack = buffer.getStackInSlot(slot);
            if (stack.isEmpty()) continue;
            if (!(stack.getItem() instanceof DomumPatternItem)) continue;

            DomumPatternDetails details = new DomumPatternDetails(stack, provider);
            if (details.isValid())
                patterns.add(details);
        }

        return patterns;
    }

    /**
     * AE2 envoie un craft à exécuter.
     * inputs[0] = les matériaux extraits du ME (déjà prélevés par AE2).
     * On met en queue pour exécution au prochain tick.
     *
     * @return true si le craft a été accepté, false si busy ou invalide
     */
    @Override
    public boolean pushPattern(IPatternDetails patternDetails, KeyCounter[] inputs)
    {
        if (craftBusy) return false;
        if (!(patternDetails instanceof DomumPatternDetails domumPattern)) return false;
        if (!domumPattern.isValid()) return false;
        if (level == null || level.isClientSide()) return false;

        // Construit la liste des matériaux extraits depuis les KeyCounters
        // KeyCounter implémente Iterable<Object2LongMap.Entry<AEKey>> — pas de forEach(BiConsumer)
        List<ItemStack> extractedMaterials = new ArrayList<>();
        for (KeyCounter counter : inputs)
        {
            for (var entry : counter)
            {
                AEKey key = entry.getKey();
                long amount = entry.getLongValue();
                if (key instanceof AEItemKey itemKey)
                    extractedMaterials.add(itemKey.toStack((int) Math.min(amount, Integer.MAX_VALUE)));
            }
        }

        // Lit le count depuis les outputs du pattern (ex: shingles=4)
        long outputCount = 1L;
        if (!domumPattern.getOutputs().isEmpty())
            outputCount = domumPattern.getOutputs().get(0).amount();

        craftQueue.add(new PendingDomumCraft(
                domumPattern.getTargetStack(),
                extractedMaterials,
                (int) Math.max(1L, outputCount)
        ));

        craftBusy = true;
        return true;
    }

    /**
     * AE2 demande si ce medium est disponible pour recevoir un craft.
     * On refuse si un craft est déjà en queue.
     */
    @Override
    public boolean isBusy()
    {
        return craftBusy || !craftQueue.isEmpty();
    }

    // ── ICraftingRequester ────────────────────────────────────────────────────

    @Override
    public ImmutableSet<ICraftingLink> getRequestedJobs()
    { return ImmutableSet.copyOf(craftingLinks); }

    @Override
    public long insertCraftedItems(ICraftingLink link, AEKey what, long amount, Actionable mode)
    {
        if (!(what instanceof AEItemKey itemKey)) return 0;
        if (targetInventoryPos == null || level == null) return 0;

        ItemStack toInsert = itemKey.toStack((int) Math.min(amount, Integer.MAX_VALUE));
        long inserted = 0;

        List<IItemHandler> handlers = getBuildingHandlers();
        for (IItemHandler handler : handlers)
        {
            if (toInsert.isEmpty()) break;
            for (int i = 0; i < handler.getSlots() && !toInsert.isEmpty(); i++)
            {
                ItemStack remainder = handler.insertItem(i, toInsert, mode == Actionable.SIMULATE);
                inserted += toInsert.getCount() - remainder.getCount();
                toInsert = remainder;
            }
        }

        if (!toInsert.isEmpty() && mode == Actionable.MODULATE)
            setState(RedirectorState.STANDBY);

        return inserted;
    }

    @Override
    public void jobStateChange(ICraftingLink link)
    {
        if (link.isCanceled() || link.isDone()) craftingLinks.remove(link);
    }

    @Override
    public IGridNode getActionableNode() { return gridNode.getNode(); }

    public void addCraftingLink(ICraftingLink link) { craftingLinks.add(link); }

    // ── Ticker — exécution des crafts en queue ────────────────────────────────

    public static <T extends BlockEntity> BlockEntityTicker<T> createTicker(Level level, BlockEntityType<T> serverType)
    {
        if (level.isClientSide()) return null;
        return (lvl, pos, blockState, be) ->
        {
            if (be instanceof ColonyLinkRedirectorBlockEntity redirector)
            {
                redirector.onFirstTick(lvl, pos);
                redirector.processCraftQueue(lvl);
            }
        };
    }

    /**
     * Exécute un craft Domum en attente depuis la queue.
     * Appelé chaque tick depuis le server thread.
     * Un seul craft par tick pour éviter les surcharges.
     */
    private void processCraftQueue(Level level)
    {
        if (craftQueue.isEmpty())
        {
            craftBusy = false;
            return;
        }

        PendingDomumCraft pending = craftQueue.poll();
        if (pending == null) { craftBusy = false; return; }

        ItemStack targetStack = pending.targetStack();
        List<ItemStack> materials = pending.materials();

        if (!DomumCraftHandler.isDomumItem(targetStack))
        {
            ColonyLink.LOGGER.warn("[DomumPattern] pushPattern: target is not a Domum item, skipping.");
            craftBusy = false;
            return;
        }

        // Les matériaux ont déjà été extraits du ME par AE2 avant pushPattern().
        // On produit directement le bloc Domum en mémoire.
        ItemStack result = buildDomumResult(targetStack, materials, level.registryAccess());

        if (result.isEmpty())
        {
            ColonyLink.LOGGER.error("[DomumPattern] Failed to build Domum result for: {}",
                    targetStack.getDisplayName().getString());
            craftBusy = false;
            return;
        }

        // Applique le count de la recette (ex: shingles=4, panels=4, framed panes=6)
        result.setCount(pending.outputCount());

        // Les crafts Domum vont toujours dans le ME.
        injectIntoME(result, level);

        craftBusy = false;

        // S'il y a d'autres crafts en queue, on reste "busy" pour le prochain tick
        if (!craftQueue.isEmpty())
            craftBusy = true;
    }

    /**
     * Construit l'ItemStack Domum résultat à partir des matériaux fournis.
     * Reproduit la logique de DomumCraftHandler.handleDomumCraft() mais sans
     * extraction ME (déjà faite par AE2).
     */
    private ItemStack buildDomumResult(ItemStack targetStack, List<ItemStack> materials,
                                       HolderLookup.Provider provider)
    {
        if (!(targetStack.getItem() instanceof BlockItem bi)) return ItemStack.EMPTY;
        Block block = bi.getBlock();
        if (!(block instanceof IMateriallyTexturedBlock texturedBlock)) return ItemStack.EMPTY;

        MaterialTextureData textureData = MaterialTextureData.readFromItemStack(targetStack);
        Map<ResourceLocation, Block> components = textureData.getTexturedComponents();

        // Reconstruit le MaterialTextureData depuis les components connus
        MaterialTextureData.Builder builder = MaterialTextureData.builder();
        for (IMateriallyTexturedBlockComponent component : texturedBlock.getComponents())
        {
            Block materialBlock = components.get(component.getId());
            if (materialBlock != null)
                builder.setComponent(component.getId(), materialBlock);
            else if (!component.isOptional())
            {
                ColonyLink.LOGGER.warn("[DomumPattern] Required component missing: {}", component.getId());
                return ItemStack.EMPTY;
            }
        }

        // Copie l'ItemStack complet (préserve BlockItemStateProperties + tous DataComponents)
        ItemStack result = targetStack.copy();
        builder.writeToItemStack(result);

        return result;
    }

    /**
     * Réinjecte un ItemStack dans le réseau ME du Redirector.
     */
    private void injectIntoME(ItemStack stack, Level level)
    {
        IGridNode node = gridNode.getNode();
        if (node == null || !node.isActive()) return;

        AEItemKey key = AEItemKey.of(stack);
        if (key == null) return;

        appeng.api.networking.security.IActionSource src =
                appeng.api.networking.security.IActionSource.ofMachine(this);

        node.getGrid().getStorageService()
                .getInventory()
                .insert(key, stack.getCount(), Actionable.MODULATE, src);
    }

    /**
     * Notifie AE2 que la liste de patterns a changé (buffer modifié).
     * AE2 va rescanner getAvailablePatterns() au prochain cycle.
     */
    public void notifyPatternChange()
    {
        if (level != null && !level.isClientSide())
            ICraftingProvider.requestUpdate(gridNode);
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @Override
    public void onLoad()
    {
        super.onLoad();
        if (level != null && !level.isClientSide())
        {
            gridNode.create(level, worldPosition);
            level.scheduleTick(worldPosition, getBlockState().getBlock(), 1);
        }
    }

    @Override
    public void onChunkUnloaded() { super.onChunkUnloaded(); gridNode.destroy(); }

    @Override
    public void setRemoved() { super.setRemoved(); gridNode.destroy(); }

    @Override
    public @Nullable IGridNode getGridNode(Direction dir) { return gridNode.getNode(); }

    private boolean firstTickDone = false;

    private void onFirstTick(Level level, BlockPos pos)
    {
        if (firstTickDone) return;
        firstTickDone = true;
        for (Direction dir : Direction.values())
            level.updateNeighborsAt(pos.relative(dir), level.getBlockState(pos.relative(dir)).getBlock());
        // Annonce initiale des patterns au réseau
        notifyPatternChange();
    }

    private void onGridStateChanged()
    {
        if (level == null) return;
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        setChanged();
    }

    // ── Warehouse helpers ─────────────────────────────────────────────────────

    public boolean hasWarehouseCard()
    { return !warehouseCardSlot.getStackInSlot(0).isEmpty(); }

    public boolean isWarehousePriority()
    {
        if (level != null && !level.isClientSide()) return warehousePriority;
        return warehousePriorityClientCache;
    }

    public void toggleWarehousePriority()
    {
        warehousePriority = !warehousePriority;
        markDirtyAndUpdate();
    }

    public boolean isAe2Active()
    {
        if (level != null && !level.isClientSide())
            return gridNode.getNode() != null && gridNode.getNode().isActive();
        return ae2ActiveClientCache;
    }

    public boolean isAdjacentToController() { return isAe2Active(); }

    // ── MineColonies building handlers ────────────────────────────────────────

    public List<IItemHandler> getBuildingHandlers()
    {
        List<IItemHandler> handlers = new ArrayList<>();
        if (targetInventoryPos == null || level == null) return handlers;

        try
        {
            var colony = com.minecolonies.api.colony.IColonyManager.getInstance()
                    .getClosestColony(level, targetInventoryPos);
            if (colony == null) return handlers;

            for (var b : colony.getServerBuildingManager().getBuildings().values())
            {
                if (!b.getPosition().equals(targetInventoryPos)) continue;
                for (BlockPos containerPos : b.getContainers())
                {
                    IItemHandler h = level.getCapability(
                            Capabilities.ItemHandler.BLOCK, containerPos, null);
                    if (h != null) handlers.add(h);
                }
                break;
            }
        }
        catch (Exception ignored) {}

        return handlers;
    }

    public boolean isTargetInventoryFull()
    {
        if (targetInventoryPos == null || level == null) return false;
        List<IItemHandler> handlers = getBuildingHandlers();
        if (handlers.isEmpty()) return false;
        for (IItemHandler handler : handlers)
            if (hasSpace(handler)) return false;
        return true;
    }

    private static boolean hasSpace(IItemHandler handler)
    {
        for (int i = 0; i < handler.getSlots(); i++)
        {
            ItemStack inSlot = handler.getStackInSlot(i);
            if (inSlot.isEmpty()) return true;
            if (inSlot.getCount() < inSlot.getMaxStackSize()) return true;
        }
        return false;
    }

    public boolean insertItem(ItemStack stack)
    {
        if (targetInventoryPos == null || level == null) return false;

        List<IItemHandler> handlers = getBuildingHandlers();
        if (handlers.isEmpty())
        {
            IItemHandler direct = level.getCapability(
                    Capabilities.ItemHandler.BLOCK, targetInventoryPos, null);
            if (direct == null) return false;
            handlers = List.of(direct);
        }

        ItemStack remainder = stack.copy();
        for (IItemHandler handler : handlers)
        {
            for (int i = 0; i < handler.getSlots() && !remainder.isEmpty(); i++)
                remainder = handler.insertItem(i, remainder, false);
            if (remainder.isEmpty()) return true;
        }

        if (!remainder.isEmpty()) { setState(RedirectorState.STANDBY); return false; }
        return true;
    }

    // ── State ─────────────────────────────────────────────────────────────────

    public void updateState()
    {
        if (!isAdjacentToController())
            setState(RedirectorState.NO_CONTROLLER);
        else if (targetInventoryPos == null)
            setState(RedirectorState.NOT_LINKED);
        else if (isTargetInventoryFull())
            setState(RedirectorState.STANDBY);
        else
            setState(RedirectorState.LINKED);

        if ((linkedBuilderName == null || linkedBuilderName.equals("N/A"))
                && linkedBuilderPos != null && level != null && !level.isClientSide())
        {
            try
            {
                var colony = com.minecolonies.api.colony.IColonyManager.getInstance()
                        .getClosestColony(level, linkedBuilderPos);
                if (colony != null)
                {
                    for (var b : colony.getServerBuildingManager().getBuildings().values())
                    {
                        if (b.getPosition().equals(linkedBuilderPos)
                                && b instanceof com.minecolonies.core.colony.buildings.AbstractBuildingStructureBuilder bb
                                && !bb.getAllAssignedCitizen().isEmpty())
                        {
                            linkedBuilderName = bb.getAllAssignedCitizen().iterator().next().getName();
                            setChanged();
                            break;
                        }
                    }
                }
            }
            catch (Exception ignored) {}
        }
    }

    // ── Getters / Setters ─────────────────────────────────────────────────────

    public BlockPos getTargetInventoryPos() { return targetInventoryPos; }
    public void setTargetInventoryPos(BlockPos pos) { this.targetInventoryPos = pos; markDirtyAndUpdate(); }

    public BlockPos getLinkedBuilderPos() { return linkedBuilderPos; }
    public void setLinkedBuilderPos(BlockPos pos) { this.linkedBuilderPos = pos; markDirtyAndUpdate(); }

    public String getLinkedBuilderName() { return linkedBuilderName; }
    public void setLinkedBuilderName(String name)
    { this.linkedBuilderName = (name != null && !name.isBlank()) ? name : "N/A"; markDirtyAndUpdate(); }

    public RedirectorState getState() { return state; }
    public void setState(RedirectorState state) { this.state = state; markDirtyAndUpdate(); }

    public boolean isLinked() { return targetInventoryPos != null; }

    public IManagedGridNode getManagedGridNode() { return gridNode; }

    private void markDirtyAndUpdate()
    {
        setChanged();
        if (level != null)
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    // ── Sync client ───────────────────────────────────────────────────────────

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider)
    {
        CompoundTag tag = super.getUpdateTag(provider);
        boolean ae2Active = gridNode.getNode() != null && gridNode.getNode().isActive();
        tag.putBoolean("ae2_active", ae2Active);
        tag.putString("state", state.name());
        tag.putBoolean("has_warehouse_card", hasWarehouseCard());
        tag.putBoolean("warehouse_priority", warehousePriority);
        if (targetInventoryPos != null)
        {
            tag.putInt("target_x", targetInventoryPos.getX());
            tag.putInt("target_y", targetInventoryPos.getY());
            tag.putInt("target_z", targetInventoryPos.getZ());
        }
        if (linkedBuilderPos != null)
        {
            tag.putInt("builder_x", linkedBuilderPos.getX());
            tag.putInt("builder_y", linkedBuilderPos.getY());
            tag.putInt("builder_z", linkedBuilderPos.getZ());
        }
        tag.putString("linked_builder_name", linkedBuilderName);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider provider)
    {
        super.handleUpdateTag(tag, provider);
        if (tag.contains("ae2_active"))    ae2ActiveClientCache = tag.getBoolean("ae2_active");
        if (tag.contains("state"))
        {
            try { state = RedirectorState.valueOf(tag.getString("state")); }
            catch (Exception ignored) {}
        }
        if (tag.contains("warehouse_priority")) warehousePriorityClientCache = tag.getBoolean("warehouse_priority");
        if (tag.contains("target_x"))
            targetInventoryPos = new BlockPos(tag.getInt("target_x"), tag.getInt("target_y"), tag.getInt("target_z"));
        if (tag.contains("builder_x"))
            linkedBuilderPos = new BlockPos(tag.getInt("builder_x"), tag.getInt("builder_y"), tag.getInt("builder_z"));
        if (tag.contains("linked_builder_name")) linkedBuilderName = tag.getString("linked_builder_name");
    }

    // ── NBT ───────────────────────────────────────────────────────────────────

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider)
    {
        super.saveAdditional(tag, provider);
        gridNode.saveToNBT(tag);
        tag.put("buffer", buffer.serializeNBT(provider));
        tag.put("warehouse_card_slot", warehouseCardSlot.serializeNBT(provider));
        tag.putBoolean("warehouse_priority", warehousePriority);
        if (targetInventoryPos != null)
        {
            tag.putInt("target_x", targetInventoryPos.getX());
            tag.putInt("target_y", targetInventoryPos.getY());
            tag.putInt("target_z", targetInventoryPos.getZ());
        }
        if (linkedBuilderPos != null)
        {
            tag.putInt("builder_x", linkedBuilderPos.getX());
            tag.putInt("builder_y", linkedBuilderPos.getY());
            tag.putInt("builder_z", linkedBuilderPos.getZ());
        }
        tag.putString("linked_builder_name", linkedBuilderName);
        tag.putString("state", state.name());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider)
    {
        super.loadAdditional(tag, provider);
        gridNode.loadFromNBT(tag);
        if (tag.contains("buffer"))          buffer.deserializeNBT(provider, tag.getCompound("buffer"));
        if (tag.contains("warehouse_card_slot")) warehouseCardSlot.deserializeNBT(provider, tag.getCompound("warehouse_card_slot"));
        if (tag.contains("warehouse_priority")) warehousePriority = tag.getBoolean("warehouse_priority");
        if (tag.contains("target_x"))
            targetInventoryPos = new BlockPos(tag.getInt("target_x"), tag.getInt("target_y"), tag.getInt("target_z"));
        if (tag.contains("builder_x"))
            linkedBuilderPos = new BlockPos(tag.getInt("builder_x"), tag.getInt("builder_y"), tag.getInt("builder_z"));
        if (tag.contains("linked_builder_name")) linkedBuilderName = tag.getString("linked_builder_name");
        if (tag.contains("state"))
        {
            try { state = RedirectorState.valueOf(tag.getString("state")); }
            catch (Exception e) { state = RedirectorState.NO_CONTROLLER; }
        }
    }

    // ── PendingDomumCraft record ──────────────────────────────────────────────

    /**
     * Représente un craft Domum en attente d'exécution.
     * Créé dans pushPattern(), consommé dans processCraftQueue().
     *
     * @param targetStack  L'ItemStack Domum cible à produire
     * @param materials    Les matériaux déjà extraits du ME par AE2
     */
    private record PendingDomumCraft(
            ItemStack targetStack,
            List<ItemStack> materials,
            int outputCount
    ) {}
}