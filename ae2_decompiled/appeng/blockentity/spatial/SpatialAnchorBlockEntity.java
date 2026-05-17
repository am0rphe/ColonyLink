/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Multiset
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.level.ChunkPos
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockState
 */
package appeng.blockentity.spatial;

import appeng.api.config.Setting;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.networking.GridFlags;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.events.statistics.GridChunkEvent;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import appeng.api.util.DimensionalBlockPos;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.blockentity.grid.AENetworkedBlockEntity;
import appeng.client.render.overlay.IOverlayDataSource;
import appeng.client.render.overlay.OverlayManager;
import appeng.me.service.StatisticsService;
import appeng.server.services.ChunkLoadingService;
import com.google.common.collect.Multiset;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class SpatialAnchorBlockEntity
extends AENetworkedBlockEntity
implements IGridTickable,
IConfigurableObject,
IOverlayDataSource {
    private static final int SPATIAL_TRANSFER_TEMPORARY_CHUNK_RANGE = 4;
    private final IConfigManager manager;
    private final Set<ChunkPos> chunks = new HashSet<ChunkPos>();
    private int powerlessTicks = 0;
    private boolean initialized = false;
    private boolean displayOverlay = false;
    private boolean isActive = false;

    public SpatialAnchorBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
        this.getMainNode().setFlags(GridFlags.REQUIRE_CHANNEL).addService(IGridTickable.class, this);
        this.manager = IConfigManager.builder(this::onSettingChanged).registerSetting(Settings.OVERLAY_MODE, YesNo.NO).build();
    }

    @Override
    public void saveAdditional(CompoundTag data, HolderLookup.Provider registries) {
        super.saveAdditional(data, registries);
        this.manager.writeToNBT(data, registries);
    }

    @Override
    public void loadTag(CompoundTag data, HolderLookup.Provider registries) {
        super.loadTag(data, registries);
        this.manager.readFromNBT(data, registries);
    }

    @Override
    protected void writeToStream(RegistryFriendlyByteBuf data) {
        super.writeToStream(data);
        data.writeBoolean(this.isActive());
        data.writeBoolean(this.displayOverlay);
        if (this.displayOverlay) {
            data.writeLongArray(this.chunks.stream().mapToLong(ChunkPos::toLong).toArray());
        }
    }

    @Override
    protected boolean readFromStream(RegistryFriendlyByteBuf data) {
        boolean ret = super.readFromStream(data);
        boolean isActive = data.readBoolean();
        ret = isActive != this.isActive || ret;
        this.isActive = isActive;
        boolean newDisplayOverlay = data.readBoolean();
        ret = newDisplayOverlay != this.displayOverlay || ret;
        this.displayOverlay = newDisplayOverlay;
        this.chunks.clear();
        OverlayManager.getInstance().removeHandlers(this);
        if (this.displayOverlay) {
            this.chunks.addAll(Arrays.stream(data.readLongArray(null)).mapToObj(ChunkPos::new).collect(Collectors.toSet()));
            OverlayManager.getInstance().showArea(this);
        }
        return ret;
    }

    @Override
    public AECableType getCableConnectionType(Direction dir) {
        return AECableType.SMART;
    }

    @Override
    public Set<ChunkPos> getOverlayChunks() {
        return this.chunks;
    }

    @Override
    public BlockEntity getOverlayBlockEntity() {
        return this;
    }

    @Override
    public DimensionalBlockPos getOverlaySourceLocation() {
        return new DimensionalBlockPos(this);
    }

    @Override
    public int getOverlayColor() {
        return Integer.MIN_VALUE | AEColor.TRANSPARENT.mediumVariant;
    }

    public void chunkAdded(GridChunkEvent.GridChunkAdded changed) {
        if (changed.getLevel() == this.getServerLevel()) {
            this.force(changed.getChunkPos());
        }
    }

    public void chunkRemoved(GridChunkEvent.GridChunkRemoved changed) {
        if (changed.getLevel() == this.getServerLevel()) {
            this.release(changed.getChunkPos(), true);
            this.wakeUp();
        }
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        if (reason != IGridNodeListener.State.GRID_BOOT) {
            this.markForUpdate();
        }
        this.wakeUp();
    }

    private void onSettingChanged(IConfigManager manager, Setting<?> setting) {
        if (setting == Settings.OVERLAY_MODE) {
            this.displayOverlay = manager.getSetting(setting) == YesNo.YES;
            this.markForUpdate();
        }
        this.saveChanges();
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (this.isClientSide()) {
            OverlayManager.getInstance().removeHandlers(this);
        } else {
            this.releaseAll();
        }
    }

    @Override
    public IConfigManager getConfigManager() {
        return this.manager;
    }

    private void wakeUp() {
        this.getMainNode().ifPresent((grid, node) -> grid.getTickManager().alertDevice((IGridNode)node));
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(20, 20, false);
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        if (!this.initialized) {
            if (!this.getMainNode().hasGridBooted()) {
                return TickRateModulation.SAME;
            }
            this.initialized = true;
        }
        this.cleanUp();
        if (this.powerlessTicks > 200) {
            if (!this.getMainNode().isOnline()) {
                this.releaseAll();
            }
            this.powerlessTicks = 0;
            return TickRateModulation.SLEEP;
        }
        if (!this.getMainNode().isOnline()) {
            this.powerlessTicks += ticksSinceLastCall;
            return TickRateModulation.SAME;
        }
        return TickRateModulation.SLEEP;
    }

    public Set<ChunkPos> getLoadedChunks() {
        return this.chunks;
    }

    public int countLoadedChunks() {
        return this.chunks.size();
    }

    public boolean isActive() {
        if (this.level != null && !this.level.isClientSide) {
            return this.getMainNode().isOnline();
        }
        return this.isActive;
    }

    public void registerChunk(ChunkPos chunkPos) {
        this.chunks.add(chunkPos);
        this.updatePowerConsumption();
    }

    private void updatePowerConsumption() {
        if (this.isRemoved()) {
            return;
        }
        int energy = 80 + this.chunks.size() * (this.chunks.size() + 1) / 2;
        this.getMainNode().setIdlePowerUsage(energy);
    }

    private void cleanUp() {
        IGrid grid = this.getMainNode().getGrid();
        if (grid == null) {
            return;
        }
        Multiset<ChunkPos> requiredChunks = grid.getService(StatisticsService.class).getChunks().get(this.getServerLevel());
        Iterator<ChunkPos> iterator = this.chunks.iterator();
        while (iterator.hasNext()) {
            ChunkPos chunkPos = iterator.next();
            if (requiredChunks.contains((Object)chunkPos)) continue;
            this.release(chunkPos, false);
            iterator.remove();
        }
        for (ChunkPos chunkPos : requiredChunks) {
            if (this.chunks.contains(chunkPos)) continue;
            this.force(chunkPos);
        }
    }

    private boolean force(ChunkPos chunkPos) {
        if (this.isRemoved()) {
            return false;
        }
        ServerLevel level = this.getServerLevel();
        boolean forced = ChunkLoadingService.getInstance().forceChunk(level, this.getBlockPos(), chunkPos);
        if (forced && this.chunks.add(chunkPos)) {
            this.updatePowerConsumption();
            this.markForClientUpdate();
        }
        return forced;
    }

    private boolean release(ChunkPos chunkPos, boolean remove) {
        ServerLevel level = this.getServerLevel();
        boolean removed = ChunkLoadingService.getInstance().releaseChunk(level, this.getBlockPos(), chunkPos);
        if (removed && remove && this.chunks.remove(chunkPos)) {
            this.updatePowerConsumption();
            this.markForClientUpdate();
        }
        return removed;
    }

    void releaseAll() {
        for (ChunkPos chunk : this.chunks) {
            this.release(chunk, false);
        }
        this.chunks.clear();
    }

    private ServerLevel getServerLevel() {
        if (this.getLevel() instanceof ServerLevel) {
            return (ServerLevel)this.getLevel();
        }
        throw new IllegalStateException("Cannot be called on a client");
    }

    void doneMoving() {
        this.initialized = false;
        int d = 4;
        ChunkPos center = new ChunkPos(this.getBlockPos());
        for (int x = center.x - d; x <= center.x + d; ++x) {
            for (int z = center.z - d; z <= center.z + d; ++z) {
                this.force(new ChunkPos(x, z));
            }
        }
    }

    static {
        GridHelper.addNodeOwnerEventHandler(GridChunkEvent.GridChunkAdded.class, SpatialAnchorBlockEntity.class, SpatialAnchorBlockEntity::chunkAdded);
        GridHelper.addNodeOwnerEventHandler(GridChunkEvent.GridChunkRemoved.class, SpatialAnchorBlockEntity.class, SpatialAnchorBlockEntity::chunkRemoved);
    }
}

