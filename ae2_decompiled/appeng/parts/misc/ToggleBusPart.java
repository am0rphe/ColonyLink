/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.block.entity.BlockEntity
 */
package appeng.parts.misc;

import appeng.api.networking.GridFlags;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IManagedGridNode;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.api.util.AECableType;
import appeng.core.AppEng;
import appeng.items.parts.PartModels;
import appeng.parts.AEBasePart;
import appeng.parts.PartModel;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ToggleBusPart
extends AEBasePart {
    @PartModels
    public static final ResourceLocation MODEL_BASE = AppEng.makeId("part/toggle_bus_base");
    @PartModels
    public static final ResourceLocation MODEL_STATUS_OFF = AppEng.makeId("part/toggle_bus_status_off");
    @PartModels
    public static final ResourceLocation MODEL_STATUS_ON = AppEng.makeId("part/toggle_bus_status_on");
    @PartModels
    public static final ResourceLocation MODEL_STATUS_HAS_CHANNEL = AppEng.makeId("part/toggle_bus_status_has_channel");
    public static final IPartModel MODELS_OFF = new PartModel(MODEL_BASE, MODEL_STATUS_OFF);
    public static final IPartModel MODELS_ON = new PartModel(MODEL_BASE, MODEL_STATUS_ON);
    public static final IPartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE, MODEL_STATUS_HAS_CHANNEL);
    private final IManagedGridNode outerNode = GridHelper.createManagedNode(this, AEBasePart.NodeListener.INSTANCE).setTagName("outer").setInWorldNode(true).setIdlePowerUsage(0.0).setFlags(GridFlags.PREFERRED);
    private IGridConnection connection;
    private boolean hasRedstone = false;
    private boolean clientSideEnabled;

    public ToggleBusPart(IPartItem<?> partItem) {
        super(partItem);
        this.getMainNode().setIdlePowerUsage(0.0);
        this.getMainNode().setFlags(GridFlags.PREFERRED);
    }

    @Override
    public void writeToStream(RegistryFriendlyByteBuf data) {
        super.writeToStream(data);
        data.writeBoolean(this.isEnabled());
    }

    @Override
    public boolean readFromStream(RegistryFriendlyByteBuf data) {
        boolean changed = super.readFromStream(data);
        boolean wasEnabled = this.clientSideEnabled;
        this.clientSideEnabled = data.readBoolean();
        return changed || wasEnabled != this.clientSideEnabled;
    }

    @Override
    public void writeVisualStateToNBT(CompoundTag data) {
        super.writeVisualStateToNBT(data);
        data.putBoolean("on", this.isEnabled());
    }

    @Override
    public void readVisualStateFromNBT(CompoundTag data) {
        super.readVisualStateFromNBT(data);
        this.clientSideEnabled = data.getBoolean("on");
    }

    protected boolean isEnabled() {
        if (this.isClientSide()) {
            return this.clientSideEnabled;
        }
        return this.getHost().hasRedstone();
    }

    @Override
    public void getBoxes(IPartCollisionHelper bch) {
        bch.addBox(6.0, 6.0, 11.0, 10.0, 10.0, 16.0);
    }

    @Override
    public void onNeighborChanged(BlockGetter level, BlockPos pos, BlockPos neighbor) {
        boolean oldHasRedstone = this.hasRedstone;
        this.hasRedstone = this.getHost().hasRedstone();
        if (this.hasRedstone != oldHasRedstone) {
            this.updateInternalState();
            this.getHost().markForUpdate();
        }
    }

    @Override
    public void readFromNBT(CompoundTag extra, HolderLookup.Provider registries) {
        super.readFromNBT(extra, registries);
        this.getOuterNode().loadFromNBT(extra);
    }

    @Override
    public void writeToNBT(CompoundTag extra, HolderLookup.Provider registries) {
        super.writeToNBT(extra, registries);
        this.getOuterNode().saveToNBT(extra);
    }

    @Override
    public void removeFromWorld() {
        super.removeFromWorld();
        this.getOuterNode().destroy();
    }

    @Override
    public void addToWorld() {
        super.addToWorld();
        this.getOuterNode().create(this.getLevel(), this.getBlockEntity().getBlockPos());
        this.hasRedstone = this.getHost().hasRedstone();
        this.updateInternalState();
    }

    @Override
    public void setPartHostInfo(Direction side, IPartHost host, BlockEntity blockEntity) {
        super.setPartHostInfo(side, host, blockEntity);
        this.outerNode.setExposedOnSides(EnumSet.of(side));
    }

    @Override
    public IGridNode getExternalFacingNode() {
        return this.getOuterNode().getNode();
    }

    @Override
    public float getCableConnectionLength(AECableType cable) {
        return 5.0f;
    }

    @Override
    public void onPlacement(Player player) {
        super.onPlacement(player);
        this.getOuterNode().setOwningPlayer(player);
    }

    private void updateInternalState() {
        boolean intention = this.isEnabled();
        if (intention == (this.connection == null) && this.getMainNode().getNode() != null && this.getOuterNode().getNode() != null) {
            if (intention) {
                this.connection = GridHelper.createConnection(this.getMainNode().getNode(), this.getOuterNode().getNode());
            } else {
                this.connection.destroy();
                this.connection = null;
            }
        }
    }

    IManagedGridNode getOuterNode() {
        return this.outerNode;
    }

    @Override
    public IPartModel getStaticModels() {
        if (this.isEnabled() && this.isActive() && this.isPowered()) {
            return MODELS_HAS_CHANNEL;
        }
        if (this.isEnabled() && this.isPowered()) {
            return MODELS_ON;
        }
        return MODELS_OFF;
    }
}

