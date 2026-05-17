/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.core.SectionPos
 *  net.minecraft.core.component.DataComponentMap
 *  net.minecraft.core.component.DataComponentMap$Builder
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.level.chunk.ChunkAccess
 *  net.minecraft.world.level.chunk.status.ChunkStatus
 *  org.jetbrains.annotations.Nullable
 */
package appeng.blockentity.networking;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.ids.AEComponents;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IAEPowerStorage;
import appeng.api.networking.events.GridPowerStorageStateChanged;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.util.AECableType;
import appeng.block.networking.EnergyCellBlock;
import appeng.blockentity.grid.AENetworkedBlockEntity;
import appeng.me.energy.StoredEnergyAmount;
import appeng.util.Platform;
import appeng.util.SettingsFrom;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.SectionPos;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import org.jetbrains.annotations.Nullable;

public class EnergyCellBlockEntity
extends AENetworkedBlockEntity
implements IAEPowerStorage,
IGridTickable {
    private final StoredEnergyAmount stored;
    private byte currentDisplayLevel;
    private boolean neighborChangePending;

    public EnergyCellBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
        this.getMainNode().setIdlePowerUsage(0.0).addService(IAEPowerStorage.class, this).addService(IGridTickable.class, this);
        EnergyCellBlock cellBlock = (EnergyCellBlock)this.getBlockState().getBlock();
        this.stored = new StoredEnergyAmount(0.0, cellBlock.getMaxPower(), this::emitPowerEvent);
    }

    @Override
    public AECableType getCableConnectionType(Direction dir) {
        return AECableType.COVERED;
    }

    @Override
    public void onReady() {
        super.onReady();
        int value = (Integer)this.level.getBlockState(this.worldPosition).getValue((Property)EnergyCellBlock.ENERGY_STORAGE);
        this.currentDisplayLevel = (byte)value;
        this.updateStateForPowerLevel();
    }

    public static int getStorageLevelFromFillFactor(double fillFactor) {
        return (int)Math.floor(4.0 * Mth.clamp((double)(fillFactor + 0.01), (double)0.0, (double)1.0));
    }

    private void updateStateForPowerLevel() {
        if (this.isRemoved()) {
            return;
        }
        int storageLevel = EnergyCellBlockEntity.getStorageLevelFromFillFactor(this.stored.getAmount() / this.stored.getMaximum());
        if (this.currentDisplayLevel != storageLevel) {
            this.currentDisplayLevel = (byte)storageLevel;
            this.level.setBlockAndUpdate(this.worldPosition, (BlockState)this.level.getBlockState(this.worldPosition).setValue((Property)EnergyCellBlock.ENERGY_STORAGE, (Comparable)Integer.valueOf(storageLevel)));
        }
    }

    private void setChangedNoTicketUpdate() {
        Level level = this.level;
        if (!(level instanceof ServerLevel)) {
            throw new IllegalArgumentException("Expected server level, not " + String.valueOf(this.level));
        }
        ServerLevel serverLevel = (ServerLevel)level;
        BlockPos pos = this.getBlockPos();
        ChunkAccess chunk = serverLevel.getChunkSource().getChunk(SectionPos.blockToSectionCoord((int)pos.getX()), SectionPos.blockToSectionCoord((int)pos.getZ()), ChunkStatus.FULL, false);
        if (chunk != null) {
            chunk.setUnsaved(true);
        }
    }

    private void onAmountChanged() {
        this.setChangedNoTicketUpdate();
        if (!this.neighborChangePending) {
            this.neighborChangePending = true;
            this.getMainNode().ifPresent((grid, node) -> grid.getTickManager().alertDevice((IGridNode)node));
        }
    }

    @Override
    public void saveAdditional(CompoundTag data, HolderLookup.Provider registries) {
        super.saveAdditional(data, registries);
        data.putDouble("internalCurrentPower", this.stored.getAmount());
        data.putBoolean("neighborChangePending", this.neighborChangePending);
    }

    @Override
    public void loadTag(CompoundTag data, HolderLookup.Provider registries) {
        super.loadTag(data, registries);
        this.stored.setStored(data.getDouble("internalCurrentPower"));
        this.neighborChangePending = data.getBoolean("neighborChangePending");
    }

    @Override
    public void importSettings(SettingsFrom mode, DataComponentMap input, @Nullable Player player) {
        Double storedEnergy;
        super.importSettings(mode, input, player);
        if (mode == SettingsFrom.DISMANTLE_ITEM && (storedEnergy = (Double)input.get(AEComponents.STORED_ENERGY)) != null) {
            this.stored.setStored(storedEnergy);
        }
    }

    @Override
    public void exportSettings(SettingsFrom from, DataComponentMap.Builder data, @Nullable Player player) {
        super.exportSettings(from, data, player);
        if (from == SettingsFrom.DISMANTLE_ITEM && this.stored.getAmount() > 0.0) {
            data.set(AEComponents.STORED_ENERGY, (Object)this.stored.getAmount());
        }
    }

    @Override
    public final double injectAEPower(double amt, Actionable mode) {
        double inserted = this.stored.insert(amt, mode == Actionable.MODULATE);
        if (mode == Actionable.MODULATE && inserted > 0.0) {
            this.onAmountChanged();
        }
        return amt - inserted;
    }

    @Override
    public final double extractAEPower(double amt, Actionable mode, PowerMultiplier pm) {
        return pm.divide(this.extractAEPower(pm.multiply(amt), mode));
    }

    private double extractAEPower(double amt, Actionable mode) {
        double extracted = this.stored.extract(amt, mode == Actionable.MODULATE);
        if (mode == Actionable.MODULATE && extracted > 0.0) {
            this.onAmountChanged();
        }
        return extracted;
    }

    @Override
    public double getAEMaxPower() {
        return this.stored.getMaximum();
    }

    @Override
    public double getAECurrentPower() {
        return this.stored.getAmount();
    }

    @Override
    public boolean isAEPublicPowerStorage() {
        return true;
    }

    @Override
    public AccessRestriction getPowerFlow() {
        return AccessRestriction.READ_WRITE;
    }

    @Override
    public int getPriority() {
        return ((EnergyCellBlock)this.getBlockState().getBlock()).getPriority();
    }

    private void emitPowerEvent(GridPowerStorageStateChanged.PowerEventType type) {
        this.getMainNode().ifPresent(grid -> grid.postEvent(new GridPowerStorageStateChanged(this, type)));
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(1, 20, !this.neighborChangePending);
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        if (Platform.areBlockEntitiesTicking(this.getLevel(), this.getBlockPos())) {
            if (this.neighborChangePending) {
                this.neighborChangePending = false;
                this.setChanged();
                this.updateStateForPowerLevel();
            }
            return TickRateModulation.SLEEP;
        }
        return TickRateModulation.IDLE;
    }
}

