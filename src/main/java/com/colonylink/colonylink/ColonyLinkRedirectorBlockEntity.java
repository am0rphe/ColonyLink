package com.colonylink.colonylink;

import appeng.api.config.Actionable;
import appeng.api.networking.GridFlags;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import com.google.common.collect.ImmutableSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public class ColonyLinkRedirectorBlockEntity extends BlockEntity implements IInWorldGridNodeHost, ICraftingRequester, MenuProvider
{
    public static final int BUFFER_ROWS = 10;
    public static final int BUFFER_COLS = 12;
    public static final int BUFFER_SIZE = BUFFER_ROWS * BUFFER_COLS;

    private BlockPos targetInventoryPos = null;
    private BlockPos linkedBuilderPos = null;
    private final Set<ICraftingLink> craftingLinks = new HashSet<>();

    // Etat AE2 synchronisé côté client via getUpdateTag/handleUpdateTag
    private boolean ae2ActiveClientCache = false;

    public final ItemStackHandler buffer = new ItemStackHandler(BUFFER_SIZE)
    {
        @Override
        protected void onContentsChanged(int slot)
        {
            setChanged();
        }
    };

    public enum RedirectorState
    {
        NOT_LINKED,
        LINKED,
        STANDBY,
        NO_CONTROLLER
    }

    private RedirectorState state = RedirectorState.NO_CONTROLLER;

    private final IManagedGridNode gridNode = GridHelper.createManagedNode(this, new IGridNodeListener<ColonyLinkRedirectorBlockEntity>()
            {
                @Override
                public void onSaveChanges(ColonyLinkRedirectorBlockEntity owner, IGridNode node)
                {
                    owner.setChanged();
                }

                @Override
                public void onStateChanged(ColonyLinkRedirectorBlockEntity owner, IGridNode node, IGridNodeListener.State state)
                {
                    owner.onGridStateChanged();
                }
            })
            .setInWorldNode(true)
            .setFlags(GridFlags.REQUIRE_CHANNEL)
            .setVisualRepresentation(ColonyLinkRegistry.REDIRECTOR_BLOCK_ITEM.get())
            .setIdlePowerUsage(1.0)
            .addService(ICraftingRequester.class, this);

    public ColonyLinkRedirectorBlockEntity(BlockPos pos, BlockState state)
    {
        super(ColonyLinkRegistry.REDIRECTOR_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    public Component getDisplayName()
    {
        return Component.literal("Colony Link Redirector");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player)
    {
        return new ColonyLinkRedirectorMenu(containerId, playerInventory, this);
    }

    private void onGridStateChanged()
    {
        if (level == null) return;
        // Synchronise l'état vers le client et notifie les câbles adjacents
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        setChanged();
    }

    @Override
    public void onLoad()
    {
        super.onLoad();
        if (level != null && !level.isClientSide())
        {
            // loadFromNBT doit être appelé avant create()
            gridNode.create(level, worldPosition);
            // Notifie les voisins qu'un nouveau nœud est disponible
            // Le délai d'un tick est nécessaire pour que le niveau
            // soit complètement chargé avant la notification
            level.scheduleTick(worldPosition, getBlockState().getBlock(), 1);
        }
    }

    @Override
    public void onChunkUnloaded()
    {
        super.onChunkUnloaded();
        gridNode.destroy();
    }

    @Override
    public void setRemoved()
    {
        super.setRemoved();
        gridNode.destroy();
    }

    @Override
    public @Nullable IGridNode getGridNode(Direction dir)
    {
        // AE2 appelle cette méthode sur les blocs adjacents pour former des connexions.
        // On expose le nœud sur toutes les faces (null = pas de connexion dans cette direction).
        return gridNode.getNode();
    }

    @Override
    public ImmutableSet<ICraftingLink> getRequestedJobs()
    {
        return ImmutableSet.copyOf(craftingLinks);
    }

    @Override
    public long insertCraftedItems(ICraftingLink link, AEKey what, long amount, Actionable mode)
    {
        if (!(what instanceof AEItemKey itemKey)) return 0;
        if (targetInventoryPos == null || level == null) return 0;

        IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, targetInventoryPos, null);
        if (handler == null) return 0;

        ItemStack toInsert = itemKey.toStack((int) Math.min(amount, Integer.MAX_VALUE));
        long inserted = 0;

        for (int i = 0; i < handler.getSlots(); i++)
        {
            if (toInsert.isEmpty()) break;
            ItemStack remainder = handler.insertItem(i, toInsert, mode == Actionable.SIMULATE);
            inserted += toInsert.getCount() - remainder.getCount();
            toInsert = remainder;
        }

        if (!toInsert.isEmpty() && mode == Actionable.MODULATE)
            setState(RedirectorState.STANDBY);

        return inserted;
    }

    @Override
    public void jobStateChange(ICraftingLink link)
    {
        if (link.isCanceled() || link.isDone())
            craftingLinks.remove(link);
    }

    @Override
    public IGridNode getActionableNode()
    {
        return gridNode.getNode();
    }

    public void addCraftingLink(ICraftingLink link)
    {
        craftingLinks.add(link);
    }

    /**
     * Tick statique appelé par ColonyLinkRedirectorBlock.getTicker().
     * Déclenché 1 tick après onLoad pour notifier les voisins une fois
     * que le niveau est complètement initialisé.
     */
    public static <T extends BlockEntity> BlockEntityTicker<T> createTicker(
            Level level, BlockEntityType<T> serverType)
    {
        if (level.isClientSide()) return null;
        return (lvl, pos, blockState, be) ->
        {
            if (be instanceof ColonyLinkRedirectorBlockEntity redirector)
                redirector.onFirstTick(lvl, pos);
        };
    }

    private boolean firstTickDone = false;

    private void onFirstTick(Level level, BlockPos pos)
    {
        if (firstTickDone) return;
        firstTickDone = true;
        // Notifie les 6 faces adjacentes pour que les câbles AE2 se connectent
        for (Direction dir : Direction.values())
            level.updateNeighborsAt(pos.relative(dir), level.getBlockState(pos.relative(dir)).getBlock());
    }

    /**
     * Vérifie si le nœud AE2 est actif.
     * Côté serveur : lit directement le nœud.
     * Côté client : utilise le cache synchronisé via getUpdateTag.
     */
    public boolean isAe2Active()
    {
        if (level != null && !level.isClientSide())
            return gridNode.getNode() != null && gridNode.getNode().isActive();
        return ae2ActiveClientCache;
    }

    public boolean isAdjacentToController()
    {
        return isAe2Active();
    }

    public boolean isTargetInventoryFull()
    {
        if (targetInventoryPos == null || level == null) return false;
        IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, targetInventoryPos, null);
        if (handler == null) return true;
        for (int i = 0; i < handler.getSlots(); i++)
        {
            if (handler.getStackInSlot(i).isEmpty()) return false;
            if (handler.getStackInSlot(i).getCount() < handler.getStackInSlot(i).getMaxStackSize()) return false;
        }
        return true;
    }

    public boolean insertItem(ItemStack stack)
    {
        if (targetInventoryPos == null || level == null) return false;
        IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, targetInventoryPos, null);
        if (handler == null) return false;

        ItemStack remainder = stack.copy();
        for (int i = 0; i < handler.getSlots(); i++)
        {
            remainder = handler.insertItem(i, remainder, false);
            if (remainder.isEmpty()) return true;
        }

        if (!remainder.isEmpty())
        {
            setState(RedirectorState.STANDBY);
            return false;
        }
        return true;
    }

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
    }

    public BlockPos getTargetInventoryPos() { return targetInventoryPos; }
    public void setTargetInventoryPos(BlockPos pos) { this.targetInventoryPos = pos; markDirtyAndUpdate(); }

    public BlockPos getLinkedBuilderPos() { return linkedBuilderPos; }
    public void setLinkedBuilderPos(BlockPos pos) { this.linkedBuilderPos = pos; markDirtyAndUpdate(); }

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

    // ── Synchronisation client ────────────────────────────────────────────────

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider)
    {
        CompoundTag tag = super.getUpdateTag(provider);
        boolean ae2Active = gridNode.getNode() != null && gridNode.getNode().isActive();
        tag.putBoolean("ae2_active", ae2Active);
        tag.putString("state", state.name());
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
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider provider)
    {
        super.handleUpdateTag(tag, provider);
        if (tag.contains("ae2_active"))
            ae2ActiveClientCache = tag.getBoolean("ae2_active");
        if (tag.contains("state"))
        {
            try { state = RedirectorState.valueOf(tag.getString("state")); }
            catch (Exception ignored) {}
        }
        if (tag.contains("target_x"))
            targetInventoryPos = new BlockPos(tag.getInt("target_x"), tag.getInt("target_y"), tag.getInt("target_z"));
        if (tag.contains("builder_x"))
            linkedBuilderPos = new BlockPos(tag.getInt("builder_x"), tag.getInt("builder_y"), tag.getInt("builder_z"));
    }

    // ── NBT persistance ───────────────────────────────────────────────────────

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider)
    {
        super.saveAdditional(tag, provider);
        gridNode.saveToNBT(tag);
        tag.put("buffer", buffer.serializeNBT(provider));
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
        tag.putString("state", state.name());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider)
    {
        super.loadAdditional(tag, provider);
        gridNode.loadFromNBT(tag);
        if (tag.contains("buffer"))
            buffer.deserializeNBT(provider, tag.getCompound("buffer"));
        if (tag.contains("target_x"))
            targetInventoryPos = new BlockPos(tag.getInt("target_x"), tag.getInt("target_y"), tag.getInt("target_z"));
        if (tag.contains("builder_x"))
            linkedBuilderPos = new BlockPos(tag.getInt("builder_x"), tag.getInt("builder_y"), tag.getInt("builder_z"));
        if (tag.contains("state"))
        {
            try { state = RedirectorState.valueOf(tag.getString("state")); }
            catch (Exception e) { state = RedirectorState.NO_CONTROLLER; }
        }
    }
}