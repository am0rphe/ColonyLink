/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.RedStoneWireBlock
 *  net.minecraft.world.level.block.state.BlockState
 */
package appeng.parts.p2p;

import appeng.api.networking.IGridNodeListener;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.core.AppEng;
import appeng.items.parts.PartModels;
import appeng.parts.p2p.P2PModels;
import appeng.parts.p2p.P2PTunnelPart;
import appeng.util.Platform;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;

public class RedstoneP2PTunnelPart
extends P2PTunnelPart<RedstoneP2PTunnelPart> {
    private static final P2PModels MODELS = new P2PModels(AppEng.makeId("part/p2p/p2p_tunnel_redstone"));
    private int power;
    private boolean recursive = false;

    @PartModels
    public static List<IPartModel> getModels() {
        return MODELS.getModels();
    }

    public RedstoneP2PTunnelPart(IPartItem<?> partItem) {
        super(partItem);
    }

    @Override
    protected float getPowerDrainPerTick() {
        return 0.5f;
    }

    private void setNetworkReady() {
        if (this.isOutput()) {
            RedstoneP2PTunnelPart in = (RedstoneP2PTunnelPart)this.getInput();
            if (in != null) {
                this.putInput(in.power);
            } else {
                this.putInput(0);
            }
        }
    }

    private void putInput(Object o) {
        if (this.recursive) {
            return;
        }
        this.recursive = true;
        if (this.isOutput()) {
            int newPower;
            int n = newPower = this.getMainNode().isActive() ? (Integer)o : 0;
            if (this.power != newPower) {
                this.power = newPower;
                this.notifyNeighbors();
            }
        }
        this.recursive = false;
    }

    private void notifyNeighbors() {
        Level level = this.getBlockEntity().getLevel();
        Platform.notifyBlocksOfNeighbors(level, this.getBlockEntity().getBlockPos());
        for (Direction face : Direction.values()) {
            Platform.notifyBlocksOfNeighbors(level, this.getBlockEntity().getBlockPos().relative(face));
        }
    }

    @Override
    protected void onMainNodeStateChanged(IGridNodeListener.State reason) {
        super.onMainNodeStateChanged(reason);
        if (this.getMainNode().hasGridBooted()) {
            this.setNetworkReady();
        }
    }

    @Override
    public void readFromNBT(CompoundTag tag, HolderLookup.Provider registries) {
        super.readFromNBT(tag, registries);
        this.power = tag.getInt("power");
    }

    @Override
    public void writeToNBT(CompoundTag tag, HolderLookup.Provider registries) {
        super.writeToNBT(tag, registries);
        tag.putInt("power", this.power);
    }

    @Override
    public void onTunnelNetworkChange() {
        this.setNetworkReady();
    }

    @Override
    public void onNeighborChanged(BlockGetter level, BlockPos pos, BlockPos neighbor) {
        if (!this.isOutput()) {
            BlockPos target = this.getBlockEntity().getBlockPos().relative(this.getSide());
            BlockState state = this.getBlockEntity().getLevel().getBlockState(target);
            Block b = state.getBlock();
            if (b != null && !this.isOutput()) {
                Direction srcSide = this.getSide();
                if (b instanceof RedStoneWireBlock) {
                    srcSide = Direction.UP;
                }
                this.power = state.getSignal((BlockGetter)this.getBlockEntity().getLevel(), target, srcSide);
                this.power = Math.max(this.power, state.getSignal((BlockGetter)this.getBlockEntity().getLevel(), target, srcSide));
                this.sendToOutput(this.power);
            } else {
                this.sendToOutput(0);
            }
        }
    }

    @Override
    public boolean canConnectRedstone() {
        return true;
    }

    @Override
    public int isProvidingStrongPower() {
        return this.isOutput() ? this.power : 0;
    }

    @Override
    public int isProvidingWeakPower() {
        return this.isOutput() ? this.power : 0;
    }

    private void sendToOutput(int power) {
        for (RedstoneP2PTunnelPart rs : this.getOutputs()) {
            rs.putInput(power);
        }
    }

    @Override
    public IPartModel getStaticModels() {
        return MODELS.getModel(this.isPowered(), this.isActive());
    }
}

