/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.stream.JsonWriter
 *  it.unimi.dsi.fastutil.objects.Reference2IntMap
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.redstone.NeighborUpdater
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.Vec3
 *  net.minecraft.world.phys.shapes.CollisionContext
 *  net.minecraft.world.phys.shapes.VoxelShape
 *  net.neoforged.neoforge.client.model.data.ModelData
 *  org.jetbrains.annotations.Nullable
 */
package appeng.blockentity.networking;

import appeng.api.networking.IGridNode;
import appeng.api.parts.IFacadeContainer;
import appeng.api.parts.IFacadePart;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartItem;
import appeng.api.parts.SelectedPart;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import appeng.api.util.DimensionalBlockPos;
import appeng.blockentity.AEBaseBlockEntity;
import appeng.client.render.cablebus.CableBusRenderState;
import appeng.core.AppEng;
import appeng.helpers.AEMultiBlockEntity;
import appeng.parts.CableBusContainer;
import appeng.util.IDebugExportable;
import appeng.util.Platform;
import com.google.gson.stream.JsonWriter;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.NeighborUpdater;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

public class CableBusBlockEntity
extends AEBaseBlockEntity
implements AEMultiBlockEntity {
    private CableBusContainer cb = new CableBusContainer(this);
    private int oldLV = -1;

    public CableBusBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
    }

    @Override
    public void loadTag(CompoundTag data, HolderLookup.Provider registries) {
        super.loadTag(data, registries);
        this.getCableBus().readFromNBT(data, registries);
    }

    @Override
    public void saveAdditional(CompoundTag data, HolderLookup.Provider registries) {
        super.saveAdditional(data, registries);
        this.getCableBus().writeToNBT(data, registries);
    }

    @Override
    protected boolean readFromStream(RegistryFriendlyByteBuf data) {
        boolean c = super.readFromStream(data);
        boolean ret = this.getCableBus().readFromStream(data);
        int newLV = this.getCableBus().getLightValue();
        if (newLV != this.oldLV) {
            this.oldLV = newLV;
            this.level.getLightEngine().checkBlock(this.worldPosition);
            ret = true;
        }
        this.updateBlockEntitySettings();
        return ret || c;
    }

    @Override
    protected void writeToStream(RegistryFriendlyByteBuf data) {
        super.writeToStream(data);
        this.getCableBus().writeToStream(data);
    }

    protected void updateBlockEntitySettings() {
    }

    public void setRemoved() {
        super.setRemoved();
        this.getCableBus().removeFromWorld();
    }

    public void clearRemoved() {
        super.clearRemoved();
        this.scheduleInit();
    }

    @Override
    public IGridNode getGridNode(Direction dir) {
        return this.getCableBus().getGridNode(dir);
    }

    @Override
    public AECableType getCableConnectionType(Direction side) {
        return this.getCableBus().getCableConnectionType(side);
    }

    @Override
    public float getCableConnectionLength(AECableType cable) {
        return this.getCableBus().getCableConnectionLength(cable);
    }

    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        this.getCableBus().removeFromWorld();
    }

    @Override
    public void markForUpdate() {
        if (this.level == null) {
            return;
        }
        int newLV = this.getCableBus().getLightValue();
        if (newLV != this.oldLV) {
            this.oldLV = newLV;
            this.level.getLightEngine().checkBlock(this.worldPosition);
        }
        super.markForUpdate();
    }

    @Override
    public void addAdditionalDrops(Level level, BlockPos pos, List<ItemStack> drops) {
        super.addAdditionalDrops(level, pos, drops);
        this.getCableBus().addAdditionalDrops(drops);
    }

    @Override
    public void clearContent() {
        super.clearContent();
        this.getCableBus().clearContent();
    }

    @Override
    public void onReady() {
        super.onReady();
        if (this.getCableBus().isEmpty()) {
            if (this.level.getBlockEntity(this.worldPosition) == this) {
                this.level.destroyBlock(this.worldPosition, true);
            }
        } else {
            this.getCableBus().addToWorld();
        }
    }

    @Override
    public IFacadeContainer getFacadeContainer() {
        return this.getCableBus().getFacadeContainer();
    }

    @Override
    @Nullable
    public IPart getPart(@Nullable Direction side) {
        return this.cb.getPart(side);
    }

    @Override
    public boolean canAddPart(ItemStack is, Direction side) {
        return this.getCableBus().canAddPart(is, side);
    }

    @Override
    @Nullable
    public <T extends IPart> T addPart(IPartItem<T> partItem, Direction side, @Nullable Player player) {
        return this.cb.addPart(partItem, side, player);
    }

    @Override
    @Nullable
    public <T extends IPart> T replacePart(IPartItem<T> partItem, @Nullable Direction side, Player owner, InteractionHand hand) {
        return this.cb.replacePart(partItem, side, owner, hand);
    }

    @Override
    public void removePartFromSide(@Nullable Direction side) {
        this.getCableBus().removePartFromSide(side);
    }

    @Override
    public boolean removePart(IPart part) {
        return this.getCableBus().removePart(part);
    }

    @Override
    public DimensionalBlockPos getLocation() {
        return new DimensionalBlockPos(this);
    }

    @Override
    public AEColor getColor() {
        return this.getCableBus().getColor();
    }

    @Override
    public void clearContainer() {
        this.setCableBus(new CableBusContainer(this));
    }

    @Override
    public boolean isBlocked(Direction side) {
        return false;
    }

    @Override
    public SelectedPart selectPartLocal(Vec3 pos) {
        return this.getCableBus().selectPartLocal(pos);
    }

    @Override
    public void markForSave() {
        this.saveChanges();
    }

    @Override
    public void partChanged() {
        this.notifyNeighbors();
    }

    @Override
    public boolean hasRedstone() {
        return this.getCableBus().hasRedstone();
    }

    @Override
    public boolean isEmpty() {
        return this.getCableBus().isEmpty();
    }

    @Override
    public void cleanup() {
        this.getLevel().removeBlock(this.worldPosition, false);
    }

    @Override
    public void notifyNeighbors() {
        if (this.level != null && this.level.hasChunkAt(this.worldPosition) && !CableBusContainer.isLoading()) {
            Platform.notifyBlocksOfNeighbors(this.level, this.worldPosition);
        }
    }

    @Override
    public void notifyNeighborNow(Direction side) {
        BlockState targetState;
        BlockPos targetPos = this.getBlockPos().relative(side);
        if (this.level != null && this.level.hasChunkAt(targetPos) && !CableBusContainer.isLoading() && !(targetState = this.level.getBlockState(targetPos)).isAir()) {
            NeighborUpdater.executeUpdate((Level)this.level, (BlockState)targetState, (BlockPos)targetPos, (Block)this.getBlockState().getBlock(), (BlockPos)this.getBlockPos(), (boolean)false);
        }
    }

    @Override
    public boolean isInWorld() {
        return this.getCableBus().isInWorld();
    }

    @Override
    public boolean recolourBlock(Direction side, AEColor colour, Player who) {
        return this.getCableBus().recolourBlock(side, colour, who);
    }

    public CableBusContainer getCableBus() {
        return this.cb;
    }

    private void setCableBus(CableBusContainer cb) {
        this.cb = cb;
    }

    @Override
    public ModelData getModelData() {
        Level level = this.getLevel();
        if (level == null) {
            return ModelData.EMPTY;
        }
        CableBusRenderState renderState = this.cb.getRenderState();
        renderState.setPos(this.worldPosition);
        return ModelData.builder().with(CableBusRenderState.PROPERTY, (Object)renderState).build();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public InteractionResult disassembleWithWrench(Player player, Level level, BlockHitResult hitResult, ItemStack wrench) {
        if (!level.isClientSide) {
            SelectedPart sp;
            ArrayList<ItemStack> is = new ArrayList<ItemStack>();
            AppEng.instance().setPartInteractionPlayer(player);
            try {
                sp = this.cb.selectPartWorld(hitResult.getLocation());
            }
            finally {
                AppEng.instance().setPartInteractionPlayer(null);
            }
            if (sp.part != null) {
                sp.part.addPartDrop(is, true);
                sp.part.addAdditionalDrops(is, true);
                if (this.remove) {
                    sp.part.clearContent();
                }
                if (sp.side == null) {
                    IFacadeContainer facades = this.getFacadeContainer();
                    for (Direction side : Direction.values()) {
                        IFacadePart facade = facades.getFacade(side);
                        if (facade == null) continue;
                        is.add(facade.getItemStack());
                        facades.removeFacade(this.cb, side);
                    }
                }
                this.cb.removePartFromSide(sp.side);
            } else if (sp.facade != null) {
                is.add(sp.facade.getItemStack());
                this.cb.getFacadeContainer().removeFacade(this.cb, sp.side);
                Platform.notifyBlocksOfNeighbors(level, this.getBlockPos());
            }
            for (ItemStack item : is) {
                player.getInventory().placeItemBackInInventory(item);
            }
        }
        return InteractionResult.sidedSuccess((boolean)level.isClientSide());
    }

    @Override
    public VoxelShape getCollisionShape(CollisionContext context) {
        return this.cb.getCollisionShape(context);
    }

    @Override
    public void debugExport(JsonWriter writer, HolderLookup.Provider registries, Reference2IntMap<Object> machineIds, Reference2IntMap<IGridNode> nodeIds) throws IOException {
        super.debugExport(writer, registries, machineIds, nodeIds);
        writer.name("parts");
        writer.beginObject();
        for (Direction side : Platform.DIRECTIONS_WITH_NULL) {
            IPart part = this.getPart(side);
            if (part == null) continue;
            writer.name(side == null ? "center" : side.getSerializedName());
            writer.beginObject();
            if (part instanceof IDebugExportable) {
                IDebugExportable exportable = (IDebugExportable)((Object)part);
                exportable.debugExport(writer, registries, machineIds, nodeIds);
            }
            writer.endObject();
        }
        writer.endObject();
    }
}

