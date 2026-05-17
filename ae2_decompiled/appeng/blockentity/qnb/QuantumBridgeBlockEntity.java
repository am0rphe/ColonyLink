/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockState
 *  net.neoforged.neoforge.client.model.data.ModelData
 *  net.neoforged.neoforge.client.model.data.ModelProperty
 */
package appeng.blockentity.qnb;

import appeng.api.ids.AEComponents;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.orientation.BlockOrientation;
import appeng.api.util.AECableType;
import appeng.block.qnb.QnbFormedState;
import appeng.block.qnb.QuantumRingBlock;
import appeng.blockentity.ServerTickingBlockEntity;
import appeng.blockentity.grid.AENetworkedInvBlockEntity;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.BlockDefinition;
import appeng.me.cluster.IAEMultiBlock;
import appeng.me.cluster.implementations.QuantumCalculator;
import appeng.me.cluster.implementations.QuantumCluster;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.FilteredInternalInventory;
import appeng.util.inv.filter.IAEItemFilter;
import java.util.Date;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;

public class QuantumBridgeBlockEntity
extends AENetworkedInvBlockEntity
implements IAEMultiBlock<QuantumCluster>,
ServerTickingBlockEntity {
    public static final ModelProperty<QnbFormedState> FORMED_STATE = new ModelProperty();
    private static int singularitySeed = 0;
    private final byte corner = (byte)16;
    private final AppEngInternalInventory internalInventory = new AppEngInternalInventory(this, 1, 1);
    private final FilteredInternalInventory externalInventory = new FilteredInternalInventory(this.internalInventory, new EntangledSingularityFilter());
    private final byte hasSingularity = (byte)32;
    private final byte powered = (byte)64;
    private final QuantumCalculator calc = new QuantumCalculator(this);
    private byte constructed = (byte)-1;
    private QuantumCluster cluster;
    private boolean updateStatus = false;

    public QuantumBridgeBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
        this.getMainNode().setFlags(GridFlags.DENSE_CAPACITY);
        this.getMainNode().setIdlePowerUsage(22.0);
        this.onGridConnectableSidesChanged();
    }

    @Override
    public Set<Direction> getGridConnectableSides(BlockOrientation orientation) {
        if (!this.isFormed()) {
            return EnumSet.noneOf(Direction.class);
        }
        if (this.isCorner() || this.isCenter()) {
            return this.getAdjacentQuantumBridges();
        }
        return EnumSet.allOf(Direction.class);
    }

    @Override
    public void serverTick() {
        if (this.updateStatus) {
            this.updateStatus = false;
            if (this.cluster != null) {
                this.cluster.updateStatus(true);
            }
            this.markForUpdate();
            this.neighborUpdate(this.getBlockPos());
        }
    }

    @Override
    protected void writeToStream(RegistryFriendlyByteBuf data) {
        super.writeToStream(data);
        int out = this.constructed;
        if (!this.internalInventory.getStackInSlot(0).isEmpty() && this.constructed != -1) {
            out |= this.hasSingularity;
        }
        if (this.getMainNode().isActive() && this.constructed != -1) {
            out |= this.powered;
        }
        data.writeByte((byte)out);
    }

    @Override
    protected boolean readFromStream(RegistryFriendlyByteBuf data) {
        boolean c = super.readFromStream(data);
        byte oldValue = this.constructed;
        this.constructed = data.readByte();
        return this.constructed != oldValue || c;
    }

    @Override
    public InternalInventory getInternalInventory() {
        return this.internalInventory;
    }

    @Override
    public void onChangeInventory(AppEngInternalInventory inv, int slot) {
        if (this.cluster != null) {
            this.cluster.updateStatus(true);
        }
    }

    @Override
    public InternalInventory getExposedInventoryForSide(Direction side) {
        if (this.isCenter()) {
            return this.externalInventory;
        }
        return InternalInventory.empty();
    }

    private boolean isCenter() {
        return this.getBlockState().is((Block)AEBlocks.QUANTUM_LINK.block());
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        this.updateStatus = true;
    }

    @Override
    public void onChunkUnloaded() {
        this.disconnect(false);
        super.onChunkUnloaded();
    }

    @Override
    public void onReady() {
        super.onReady();
        BlockDefinition<QuantumRingBlock> quantumRing = AEBlocks.QUANTUM_RING;
        if (this.getBlockState().getBlock() == quantumRing.block()) {
            this.getMainNode().setVisualRepresentation(quantumRing.stack());
        }
        this.updateStatus = true;
    }

    @Override
    public void setRemoved() {
        this.disconnect(false);
        super.setRemoved();
    }

    @Override
    public void disconnect(boolean affectWorld) {
        if (this.cluster != null) {
            if (!affectWorld) {
                this.cluster.setUpdateStatus(false);
            }
            this.cluster.destroy();
        }
        this.cluster = null;
        if (affectWorld) {
            this.onGridConnectableSidesChanged();
        }
    }

    @Override
    public QuantumCluster getCluster() {
        return this.cluster;
    }

    @Override
    public boolean isValid() {
        return !this.isRemoved();
    }

    public void updateStatus(QuantumCluster c, byte flags, boolean affectWorld) {
        this.cluster = c;
        if (affectWorld) {
            if (this.constructed != flags) {
                this.constructed = flags;
                this.markForUpdate();
            }
            this.onGridConnectableSidesChanged();
        }
    }

    public boolean isCorner() {
        return (this.constructed & this.getCorner()) == this.getCorner() && this.constructed != -1;
    }

    public EnumSet<Direction> getAdjacentQuantumBridges() {
        EnumSet<Direction> set = EnumSet.noneOf(Direction.class);
        if (this.level != null) {
            for (Direction d : Direction.values()) {
                BlockEntity te = this.level.getBlockEntity(this.worldPosition.relative(d));
                if (!(te instanceof QuantumBridgeBlockEntity)) continue;
                set.add(d);
            }
        }
        return set;
    }

    public long getQEFrequency() {
        ItemStack is = this.internalInventory.getStackInSlot(0);
        if (!is.isEmpty()) {
            return (Long)is.getOrDefault(AEComponents.ENTANGLED_SINGULARITY_ID, (Object)0L);
        }
        return 0L;
    }

    public boolean isPowered() {
        if (this.isClientSide()) {
            return (this.constructed & this.powered) == this.powered && this.constructed != -1;
        }
        IGridNode node = this.getMainNode().getNode();
        return node != null && node.isPowered();
    }

    public boolean isFormed() {
        return this.constructed != -1;
    }

    @Override
    public AECableType getCableConnectionType(Direction dir) {
        return AECableType.DENSE_SMART;
    }

    public void neighborUpdate(BlockPos fromPos) {
        Level level = this.level;
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            this.calc.updateMultiblockAfterNeighborUpdate(serverLevel, this.worldPosition, fromPos);
        }
    }

    public boolean hasQES() {
        if (this.constructed == -1) {
            return false;
        }
        return (this.constructed & this.hasSingularity) == this.hasSingularity;
    }

    public void breakClusterOnRemove() {
        if (this.cluster != null) {
            this.remove = true;
            this.cluster.destroy();
            this.remove = false;
        }
    }

    public byte getCorner() {
        return this.corner;
    }

    @Override
    public ModelData getModelData() {
        return ModelData.builder().with(FORMED_STATE, (Object)new QnbFormedState(this.getAdjacentQuantumBridges(), this.isCorner(), this.isPowered())).build();
    }

    public static boolean isValidEntangledSingularity(ItemStack stack) {
        return stack.has(AEComponents.ENTANGLED_SINGULARITY_ID);
    }

    public static void assignFrequency(ItemStack stack) {
        long frequency = new Date().getTime() * 100L + (long)(singularitySeed++ % 100);
        stack.set(AEComponents.ENTANGLED_SINGULARITY_ID, (Object)frequency);
    }

    private static class EntangledSingularityFilter
    implements IAEItemFilter {
        private EntangledSingularityFilter() {
        }

        @Override
        public boolean allowInsert(InternalInventory inv, int slot, ItemStack stack) {
            return QuantumBridgeBlockEntity.isValidEntangledSingularity(stack);
        }
    }
}

