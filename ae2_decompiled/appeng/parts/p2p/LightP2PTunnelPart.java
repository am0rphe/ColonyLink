/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.entity.BlockEntity
 */
package appeng.parts.p2p;

import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.core.AppEng;
import appeng.core.settings.TickRates;
import appeng.items.parts.PartModels;
import appeng.parts.p2p.P2PModels;
import appeng.parts.p2p.P2PTunnelPart;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class LightP2PTunnelPart
extends P2PTunnelPart<LightP2PTunnelPart>
implements IGridTickable {
    private static final P2PModels MODELS = new P2PModels(AppEng.makeId("part/p2p/p2p_tunnel_light"));
    private int lastValue = 0;
    private int opacity = -1;

    @PartModels
    public static List<IPartModel> getModels() {
        return MODELS.getModels();
    }

    public LightP2PTunnelPart(IPartItem<?> partItem) {
        super(partItem);
        this.getMainNode().addService(IGridTickable.class, this);
    }

    @Override
    protected float getPowerDrainPerTick() {
        return 0.5f;
    }

    @Override
    protected void onMainNodeStateChanged(IGridNodeListener.State reason) {
        super.onMainNodeStateChanged(reason);
        if (this.getMainNode().hasGridBooted()) {
            this.onTunnelNetworkChange();
        }
    }

    @Override
    public void writeToStream(RegistryFriendlyByteBuf data) {
        super.writeToStream(data);
        data.writeInt(this.isOutput() ? this.lastValue : 0);
    }

    @Override
    public boolean readFromStream(RegistryFriendlyByteBuf data) {
        boolean changed = super.readFromStream(data);
        int oldValue = this.lastValue;
        this.lastValue = data.readInt();
        this.setOutput(this.lastValue > 0);
        return changed || this.lastValue != oldValue;
    }

    private boolean doWork() {
        if (this.isOutput()) {
            return false;
        }
        BlockEntity te = this.getBlockEntity();
        Level level = te.getLevel();
        int newLevel = level.getMaxLocalRawBrightness(te.getBlockPos().relative(this.getSide()));
        if (this.lastValue != newLevel && this.getMainNode().isActive()) {
            this.lastValue = newLevel;
            for (LightP2PTunnelPart out : this.getOutputs()) {
                out.setLightLevel(this.lastValue);
            }
            return true;
        }
        return false;
    }

    @Override
    public void onNeighborChanged(BlockGetter level, BlockPos pos, BlockPos neighbor) {
        if (this.isOutput() && pos.relative(this.getSide()).equals((Object)neighbor)) {
            this.opacity = -1;
            this.getHost().markForUpdate();
        } else {
            this.doWork();
        }
    }

    @Override
    public int getLightLevel() {
        if (this.isOutput() && this.isPowered() && this.getInput() != null) {
            return this.blockLight(this.lastValue);
        }
        return 0;
    }

    private void setLightLevel(int out) {
        this.lastValue = out;
        this.getHost().markForUpdate();
    }

    private int blockLight(int emit) {
        if (this.opacity == -1) {
            BlockEntity be = this.getHost().getBlockEntity();
            Level level = be.getLevel();
            BlockPos pos = be.getBlockPos();
            this.opacity = level.getMaxLocalRawBrightness(pos.relative(this.getSide()));
        }
        return Math.max(0, emit - this.opacity);
    }

    @Override
    public void readFromNBT(CompoundTag tag, HolderLookup.Provider registries) {
        super.readFromNBT(tag, registries);
        this.lastValue = tag.getInt("lastValue");
    }

    @Override
    public void writeToNBT(CompoundTag tag, HolderLookup.Provider registries) {
        super.writeToNBT(tag, registries);
        tag.putInt("lastValue", this.lastValue);
    }

    @Override
    public void onTunnelConfigChange() {
        this.onTunnelNetworkChange();
    }

    @Override
    public void onTunnelNetworkChange() {
        if (this.isOutput()) {
            LightP2PTunnelPart src = (LightP2PTunnelPart)this.getInput();
            if (src != null && src.getMainNode().isActive()) {
                this.setLightLevel(src.lastValue);
            } else {
                this.getHost().markForUpdate();
            }
        } else {
            this.doWork();
        }
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(TickRates.LightTunnel, false);
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        return this.doWork() ? TickRateModulation.URGENT : TickRateModulation.SLOWER;
    }

    @Override
    public IPartModel getStaticModels() {
        return MODELS.getModel(this.isPowered(), this.isActive());
    }
}

