/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Object2LongMap$Entry
 *  net.minecraft.Util
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.neoforged.neoforge.client.model.data.ModelData
 */
package appeng.blockentity.crafting;

import appeng.api.implementations.IPowerChannelState;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridMultiblock;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.orientation.BlockOrientation;
import appeng.api.stacks.AEKey;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.block.crafting.AbstractCraftingUnitBlock;
import appeng.blockentity.crafting.CraftingCubeModelData;
import appeng.blockentity.grid.AENetworkedBlockEntity;
import appeng.core.definitions.AEBlocks;
import appeng.crafting.inv.ListCraftingInventory;
import appeng.me.cluster.IAEMultiBlock;
import appeng.me.cluster.implementations.CraftingCPUCalculator;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.util.NullConfigManager;
import appeng.util.Platform;
import appeng.util.iterators.ChainedIterator;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.neoforge.client.model.data.ModelData;

public class CraftingBlockEntity
extends AENetworkedBlockEntity
implements IAEMultiBlock<CraftingCPUCluster>,
IPowerChannelState,
IConfigurableObject {
    private final CraftingCPUCalculator calc = new CraftingCPUCalculator(this);
    private CompoundTag previousState = null;
    private boolean isCoreBlock = false;
    private CraftingCPUCluster cluster;

    public CraftingBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
        this.getMainNode().setFlags(GridFlags.MULTIBLOCK, GridFlags.REQUIRE_CHANNEL).addService(IGridMultiblock.class, this::getMultiblockNodes);
    }

    @Override
    protected Item getItemFromBlockEntity() {
        if (this.level == null) {
            return Items.AIR;
        }
        return this.getUnitBlock().type.getItemFromType();
    }

    @Override
    public void setName(String name) {
        super.setName(name);
        if (this.cluster != null) {
            this.cluster.updateName();
        }
    }

    public AbstractCraftingUnitBlock<?> getUnitBlock() {
        if (this.level == null || this.notLoaded() || this.isRemoved()) {
            return AEBlocks.CRAFTING_UNIT.block();
        }
        return (AbstractCraftingUnitBlock)this.level.getBlockState(this.worldPosition).getBlock();
    }

    public long getStorageBytes() {
        return this.getUnitBlock().type.getStorageBytes();
    }

    public int getAcceleratorThreads() {
        return this.getUnitBlock().type.getAcceleratorThreads();
    }

    @Override
    public void onReady() {
        super.onReady();
        this.getMainNode().setVisualRepresentation((ItemLike)this.getItemFromBlockEntity());
        Level level = this.level;
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            this.calc.calculateMultiblock(serverLevel, this.worldPosition);
        }
    }

    public void updateMultiBlock(BlockPos changedPos) {
        Level level = this.level;
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            this.calc.updateMultiblockAfterNeighborUpdate(serverLevel, this.worldPosition, changedPos);
        }
    }

    public void updateStatus(CraftingCPUCluster c) {
        if (this.cluster != null && this.cluster != c) {
            this.cluster.breakCluster();
        }
        this.cluster = c;
        this.updateSubType(true);
    }

    public void updateSubType(boolean updateFormed) {
        BlockState newState;
        if (this.level == null || this.notLoaded() || this.isRemoved()) {
            return;
        }
        boolean formed = this.isFormed();
        boolean power = this.getMainNode().isOnline();
        BlockState current = this.level.getBlockState(this.worldPosition);
        if (current.getBlock() instanceof AbstractCraftingUnitBlock && current != (newState = (BlockState)((BlockState)current.setValue((Property)AbstractCraftingUnitBlock.POWERED, (Comparable)Boolean.valueOf(power))).setValue((Property)AbstractCraftingUnitBlock.FORMED, (Comparable)Boolean.valueOf(formed)))) {
            this.level.setBlock(this.worldPosition, newState, 2);
        }
        if (updateFormed) {
            this.onGridConnectableSidesChanged();
        }
    }

    @Override
    public Set<Direction> getGridConnectableSides(BlockOrientation orientation) {
        if (this.isFormed()) {
            return EnumSet.allOf(Direction.class);
        }
        return EnumSet.noneOf(Direction.class);
    }

    public boolean isFormed() {
        if (this.isClientSide()) {
            return (Boolean)this.getBlockState().getValue((Property)AbstractCraftingUnitBlock.FORMED);
        }
        return this.cluster != null;
    }

    @Override
    public void saveAdditional(CompoundTag data, HolderLookup.Provider registries) {
        super.saveAdditional(data, registries);
        data.putBoolean("core", this.isCoreBlock());
        if (this.isCoreBlock() && this.cluster != null) {
            this.cluster.writeToNBT(data, registries);
        }
    }

    @Override
    public void loadTag(CompoundTag data, HolderLookup.Provider registries) {
        super.loadTag(data, registries);
        this.setCoreBlock(data.getBoolean("core"));
        if (this.isCoreBlock()) {
            if (this.cluster != null) {
                this.cluster.readFromNBT(data, registries);
            } else {
                this.setPreviousState(data.copy());
            }
        }
    }

    @Override
    public void disconnect(boolean update) {
        if (this.cluster != null) {
            this.cluster.destroy();
            if (update) {
                this.updateSubType(true);
            }
        }
    }

    @Override
    public CraftingCPUCluster getCluster() {
        return this.cluster;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        if (reason != IGridNodeListener.State.GRID_BOOT) {
            this.updateSubType(false);
        }
    }

    public void breakCluster() {
        if (this.cluster != null) {
            this.cluster.cancelJob();
            ListCraftingInventory inv = this.cluster.craftingLogic.getInventory();
            ArrayList<BlockPos> places = new ArrayList<BlockPos>();
            for (CraftingBlockEntity blockEntity : this.cluster::getBlockEntities) {
                if (this == blockEntity) {
                    places.add(this.worldPosition);
                    continue;
                }
                for (Direction d : Direction.values()) {
                    BlockPos p = blockEntity.worldPosition.relative(d);
                    if (!this.level.isEmptyBlock(p)) continue;
                    places.add(p);
                }
            }
            if (places.isEmpty()) {
                throw new IllegalStateException(String.valueOf(this.cluster) + " does not contain any kind of blocks, which were destroyed.");
            }
            for (Object2LongMap.Entry entry : inv.list) {
                BlockPos position = (BlockPos)Util.getRandom(places, (RandomSource)this.level.getRandom());
                ArrayList<ItemStack> stacks = new ArrayList<ItemStack>();
                ((AEKey)entry.getKey()).addDrops(entry.getLongValue(), stacks, this.level, position);
                Platform.spawnDrops(this.level, position, stacks);
            }
            inv.clear();
            this.cluster.destroy();
        }
    }

    @Override
    public boolean isPowered() {
        if (this.isClientSide()) {
            return (Boolean)this.level.getBlockState(this.worldPosition).getValue((Property)AbstractCraftingUnitBlock.POWERED);
        }
        return this.getMainNode().isActive();
    }

    @Override
    public boolean isActive() {
        if (!this.isClientSide()) {
            return this.getMainNode().isActive();
        }
        return this.isPowered() && this.isFormed();
    }

    public boolean isCoreBlock() {
        return this.isCoreBlock;
    }

    public void setCoreBlock(boolean isCoreBlock) {
        this.isCoreBlock = isCoreBlock;
    }

    public CompoundTag getPreviousState() {
        return this.previousState;
    }

    public void setPreviousState(CompoundTag previousState) {
        this.previousState = previousState;
    }

    @Override
    public ModelData getModelData() {
        return CraftingCubeModelData.create(this.getConnections());
    }

    protected EnumSet<Direction> getConnections() {
        if (this.level == null) {
            return EnumSet.noneOf(Direction.class);
        }
        EnumSet<Direction> connections = EnumSet.noneOf(Direction.class);
        for (Direction facing : Direction.values()) {
            if (!this.isConnected((BlockGetter)this.level, this.worldPosition, facing)) continue;
            connections.add(facing);
        }
        return connections;
    }

    private boolean isConnected(BlockGetter level, BlockPos pos, Direction side) {
        BlockPos adjacentPos = pos.relative(side);
        return level.getBlockState(adjacentPos).getBlock() instanceof AbstractCraftingUnitBlock;
    }

    @Override
    public void setBlockState(BlockState state) {
        super.setBlockState(state);
        this.requestModelDataUpdate();
    }

    private Iterator<IGridNode> getMultiblockNodes() {
        if (this.getCluster() == null) {
            return new ChainedIterator<IGridNode>(new IGridNode[0]);
        }
        ArrayList<IGridNode> nodes = new ArrayList<IGridNode>();
        Iterator<CraftingBlockEntity> it = this.getCluster().getBlockEntities();
        while (it.hasNext()) {
            IGridNode node = it.next().getGridNode();
            if (node == null) continue;
            nodes.add(node);
        }
        return nodes.iterator();
    }

    @Override
    public IConfigManager getConfigManager() {
        CraftingCPUCluster cluster = this.getCluster();
        if (cluster != null) {
            return this.getCluster().getConfigManager();
        }
        return NullConfigManager.INSTANCE;
    }
}

