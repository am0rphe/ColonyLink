/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Direction
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.block.entity.BlockEntity
 */
package appeng.parts.networking;

import appeng.api.networking.GridFlags;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IManagedGridNode;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.api.util.AECableType;
import appeng.core.AppEng;
import appeng.items.parts.PartModels;
import appeng.me.energy.IEnergyOverlayGridConnection;
import appeng.me.service.EnergyService;
import appeng.parts.AEBasePart;
import appeng.parts.PartModel;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

public class QuartzFiberPart
extends AEBasePart {
    @PartModels
    private static final IPartModel MODELS = new PartModel(AppEng.makeId("part/quartz_fiber"));
    private final IManagedGridNode outerNode;

    public QuartzFiberPart(IPartItem<?> partItem) {
        super(partItem);
        this.getMainNode().setIdlePowerUsage(0.0).setFlags(GridFlags.CANNOT_CARRY).addService(IEnergyOverlayGridConnection.class, this::getTheirEnergyServices);
        this.outerNode = GridHelper.createManagedNode(this, AEBasePart.NodeListener.INSTANCE).setTagName("outer").setIdlePowerUsage(0.0).setVisualRepresentation(partItem).setFlags(GridFlags.CANNOT_CARRY).setInWorldNode(true).addService(IEnergyOverlayGridConnection.class, this::getOurEnergyServices);
    }

    private List<EnergyService> getOurEnergyServices() {
        IGrid grid = Objects.requireNonNull(this.getMainNode().getGrid());
        return Collections.singletonList((EnergyService)grid.getEnergyService());
    }

    private List<EnergyService> getTheirEnergyServices() {
        IGrid grid = Objects.requireNonNull(this.outerNode.getGrid());
        return Collections.singletonList((EnergyService)grid.getEnergyService());
    }

    @Override
    public void getBoxes(IPartCollisionHelper bch) {
        bch.addBox(6.0, 6.0, 10.0, 10.0, 10.0, 16.0);
    }

    @Override
    public void readFromNBT(CompoundTag extra, HolderLookup.Provider registries) {
        super.readFromNBT(extra, registries);
        this.outerNode.loadFromNBT(extra);
    }

    @Override
    public void writeToNBT(CompoundTag extra, HolderLookup.Provider registries) {
        super.writeToNBT(extra, registries);
        this.outerNode.saveToNBT(extra);
    }

    @Override
    public void removeFromWorld() {
        super.removeFromWorld();
        this.outerNode.destroy();
    }

    @Override
    public void addToWorld() {
        super.addToWorld();
        this.outerNode.create(this.getLevel(), this.getBlockEntity().getBlockPos());
    }

    @Override
    public void setPartHostInfo(Direction side, IPartHost host, BlockEntity blockEntity) {
        super.setPartHostInfo(side, host, blockEntity);
        this.outerNode.setExposedOnSides(EnumSet.of(side));
    }

    @Override
    public IGridNode getExternalFacingNode() {
        return this.outerNode.getNode();
    }

    @Override
    public float getCableConnectionLength(AECableType cable) {
        return 16.0f;
    }

    @Override
    public void onPlacement(Player player) {
        super.onPlacement(player);
        this.outerNode.setOwningPlayer(player);
    }

    @Override
    public IPartModel getStaticModels() {
        return MODELS;
    }
}

