package com.colonylink.colonylink;

import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.impl.node.SimpleNetworkNode;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.support.network.InWorldNetworkNodeContainer;
import com.refinedmods.refinedstorage.common.support.network.AbstractBaseNetworkNodeContainerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

/**
 * ColonyLink Redirector Block Entity — RS2 variant.
 *
 * Étend AbstractBaseNetworkNodeContainerBlockEntity<SimpleNetworkNode>.
 *
 * Corrections v1.1.4 (fix connexion câble RS2) :
 * - updateActiveness() override : synchronise la blockstate ACTIVE avec l'état
 *   réseau réel. Requis par NetworkNodeBlockEntityTicker pour que RS2 sache
 *   quand le nœud est connecté/déconnecté.
 * - calculateActive() override : retourne true si le nœud a un réseau actif.
 *   RS2 appelle cette méthode dans le ticker pour décider de l'état ACTIVE.
 * - La capability NetworkNodeContainerProvider est exposée automatiquement
 *   via getContainerProvider() (hérité) + enregistrement dans ColonyLinkRegistry.
 */
public class ColonyLinkRedirectorBlockEntityRS
        extends AbstractBaseNetworkNodeContainerBlockEntity<SimpleNetworkNode>
        implements MenuProvider
{
    public static final int BUFFER_ROWS = 10;
    public static final int BUFFER_COLS = 12;
    public static final int BUFFER_SIZE = BUFFER_ROWS * BUFFER_COLS;

    private BlockPos targetInventoryPos = null;
    private BlockPos linkedBuilderPos   = null;
    private boolean  warehousePriority  = false;

    // Cache client
    private boolean rs2ActiveClientCache         = false;
    private boolean warehousePriorityClientCache = false;

    public enum RedirectorState { NOT_LINKED, LINKED, STANDBY, NO_CONTROLLER }
    private RedirectorState state = RedirectorState.NO_CONTROLLER;

    public final ItemStackHandler buffer = new ItemStackHandler(BUFFER_SIZE)
    {
        @Override protected void onContentsChanged(int slot) { setChanged(); }
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

    // ── Constructeur ──────────────────────────────────────────────────────────

    public ColonyLinkRedirectorBlockEntityRS(BlockPos pos, BlockState state)
    {
        super(ColonyLinkRegistry.REDIRECTOR_BLOCK_ENTITY_RS.get(), pos, state,
                new SimpleNetworkNode(1L));
    }

    // ── Nameable — requis par la classe parente ───────────────────────────────

    @Override
    public Component getName()
    {
        return Component.literal("Colony Link Redirector RS");
    }

    // ── AbstractBaseNetworkNodeContainerBlockEntity ───────────────────────────

    @Override
    protected InWorldNetworkNodeContainer createMainContainer(SimpleNetworkNode node)
    {
        return RefinedStorageApi.INSTANCE
                .createNetworkNodeContainer(this, node)
                .name("colony_link_redirector_rs")
                .connectionStrategy(
                        new com.refinedmods.refinedstorage.common.support.network.SimpleConnectionStrategy(
                                worldPosition))
                .build();
    }

    /**
     * calculateActive() — indique à RS2 si ce nœud doit être considéré actif.
     *
     * RS2 appelle cette méthode dans NetworkNodeBlockEntityTicker.tick() pour
     * décider si la blockstate ACTIVE doit changer. Retourner true quand le
     * nœud a un réseau garantit que :
     *   - la propriété ACTIVE passe à true dès que le câble se connecte
     *   - updateActiveness() est appelé → blockstate mise à jour côté client
     */
    @Override
    protected boolean calculateActive()
    {
        return mainNetworkNode.getNetwork() != null;
    }

    /**
     * updateActiveness() — met à jour la blockstate ACTIVE en jeu.
     *
     * Appelé par le ticker RS2 quand calculateActive() change de valeur.
     * On délègue à la méthode parente en passant la BooleanProperty ACTIVE
     * de notre bloc, ce qui déclenche la mise à jour visuelle du bloc
     * (connexion câble, texture active, etc.).
     */
    @Override
    public void updateActiveness(net.minecraft.world.level.block.state.BlockState blockState,
                                 net.minecraft.world.level.block.state.properties.@Nullable BooleanProperty activenessProperty)
    {
        super.updateActiveness(blockState, ColonyLinkRedirectorBlockRS.ACTIVE);
    }

    // ── MenuProvider ──────────────────────────────────────────────────────────

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player)
    {
        return new ColonyLinkRedirectorMenuRS(containerId, playerInventory, this);
    }

    // ── État RS2 ──────────────────────────────────────────────────────────────

    public boolean isRs2Active()
    {
        if (level != null && !level.isClientSide())
            return mainNetworkNode.getNetwork() != null;
        return rs2ActiveClientCache;
    }

    public @Nullable Network getNetwork()
    {
        return mainNetworkNode.getNetwork();
    }

    // ── Warehouse card ────────────────────────────────────────────────────────

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

    // ── State machine ─────────────────────────────────────────────────────────

    public void updateState()
    {
        if (!isRs2Active())
            setState(RedirectorState.NO_CONTROLLER);
        else if (targetInventoryPos == null)
            setState(RedirectorState.NOT_LINKED);
        else if (isTargetInventoryFull())
            setState(RedirectorState.STANDBY);
        else
            setState(RedirectorState.LINKED);
    }

    public boolean isTargetInventoryFull()
    {
        if (targetInventoryPos == null || level == null) return false;
        IItemHandler handler = level.getCapability(
                Capabilities.ItemHandler.BLOCK, targetInventoryPos, null);
        if (handler == null) return true;
        for (int i = 0; i < handler.getSlots(); i++)
        {
            if (handler.getStackInSlot(i).isEmpty()) return false;
            if (handler.getStackInSlot(i).getCount() < handler.getStackInSlot(i).getMaxStackSize())
                return false;
        }
        return true;
    }

    public boolean insertItem(ItemStack stack)
    {
        if (targetInventoryPos == null || level == null) return false;
        IItemHandler handler = level.getCapability(
                Capabilities.ItemHandler.BLOCK, targetInventoryPos, null);
        if (handler == null) return false;
        ItemStack remainder = stack.copy();
        for (int i = 0; i < handler.getSlots(); i++)
        {
            remainder = handler.insertItem(i, remainder, false);
            if (remainder.isEmpty()) return true;
        }
        if (!remainder.isEmpty()) { setState(RedirectorState.STANDBY); return false; }
        return true;
    }

    // ── Getters / Setters ─────────────────────────────────────────────────────

    public BlockPos getTargetInventoryPos() { return targetInventoryPos; }
    public void setTargetInventoryPos(BlockPos pos)
    { this.targetInventoryPos = pos; markDirtyAndUpdate(); }

    public BlockPos getLinkedBuilderPos() { return linkedBuilderPos; }
    public void setLinkedBuilderPos(BlockPos pos)
    { this.linkedBuilderPos = pos; markDirtyAndUpdate(); }

    public RedirectorState getState() { return state; }
    public void setState(RedirectorState s) { this.state = s; markDirtyAndUpdate(); }

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
        tag.putBoolean("rs2_active", isRs2Active());
        tag.putString("cl_state", state.name());
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
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider provider)
    {
        super.handleUpdateTag(tag, provider);
        if (tag.contains("rs2_active"))
            rs2ActiveClientCache = tag.getBoolean("rs2_active");
        if (tag.contains("cl_state"))
            try { state = RedirectorState.valueOf(tag.getString("cl_state")); }
            catch (Exception ignored) {}
        if (tag.contains("warehouse_priority"))
            warehousePriorityClientCache = tag.getBoolean("warehouse_priority");
        if (tag.contains("target_x"))
            targetInventoryPos = new BlockPos(
                    tag.getInt("target_x"), tag.getInt("target_y"), tag.getInt("target_z"));
        if (tag.contains("builder_x"))
            linkedBuilderPos = new BlockPos(
                    tag.getInt("builder_x"), tag.getInt("builder_y"), tag.getInt("builder_z"));
    }

    // ── NBT persistance ───────────────────────────────────────────────────────

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider provider)
    {
        super.saveAdditional(tag, provider);
        tag.put("cl_buffer", buffer.serializeNBT(provider));
        tag.put("cl_warehouse_card", warehouseCardSlot.serializeNBT(provider));
        tag.putBoolean("cl_wh_priority", warehousePriority);
        tag.putString("cl_state", state.name());
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
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider)
    {
        super.loadAdditional(tag, provider);
        if (tag.contains("cl_buffer"))
            buffer.deserializeNBT(provider, tag.getCompound("cl_buffer"));
        if (tag.contains("cl_warehouse_card"))
            warehouseCardSlot.deserializeNBT(provider, tag.getCompound("cl_warehouse_card"));
        if (tag.contains("cl_wh_priority"))
            warehousePriority = tag.getBoolean("cl_wh_priority");
        if (tag.contains("cl_state"))
            try { state = RedirectorState.valueOf(tag.getString("cl_state")); }
            catch (Exception e) { state = RedirectorState.NO_CONTROLLER; }
        if (tag.contains("target_x"))
            targetInventoryPos = new BlockPos(
                    tag.getInt("target_x"), tag.getInt("target_y"), tag.getInt("target_z"));
        if (tag.contains("builder_x"))
            linkedBuilderPos = new BlockPos(
                    tag.getInt("builder_x"), tag.getInt("builder_y"), tag.getInt("builder_z"));
    }
}