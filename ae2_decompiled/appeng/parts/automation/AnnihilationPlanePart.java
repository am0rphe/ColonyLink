/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.DynamicOps
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.core.component.DataComponentMap
 *  net.minecraft.core.component.DataComponentMap$Builder
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.NbtOps
 *  net.minecraft.nbt.Tag
 *  net.minecraft.resources.RegistryOps
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.enchantment.ItemEnchantments
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.neoforged.neoforge.client.model.data.ModelData
 *  org.jetbrains.annotations.Nullable
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package appeng.parts.automation;

import appeng.api.behaviors.PickupStrategy;
import appeng.api.config.Actionable;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.storage.StorageHelper;
import appeng.api.util.AECableType;
import appeng.core.AEConfig;
import appeng.core.definitions.AEItems;
import appeng.core.settings.TickRates;
import appeng.items.parts.PartModels;
import appeng.me.helpers.MachineSource;
import appeng.parts.AEBasePart;
import appeng.parts.automation.PlaneConnectionHelper;
import appeng.parts.automation.PlaneConnections;
import appeng.parts.automation.PlaneModelData;
import appeng.parts.automation.PlaneModels;
import appeng.parts.automation.StackWorldBehaviors;
import appeng.util.SettingsFrom;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DynamicOps;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnnihilationPlanePart
extends AEBasePart
implements IGridTickable {
    private static final Logger LOG = LoggerFactory.getLogger(AnnihilationPlanePart.class);
    private static final PlaneModels MODELS = new PlaneModels("part/annihilation_plane", "part/annihilation_plane_on");
    private final IActionSource actionSource = new MachineSource(this);
    private final PlaneConnectionHelper connectionHelper = new PlaneConnectionHelper(this);
    @Nullable
    protected List<PickupStrategy> pickupStrategies;
    private ItemEnchantments enchantments = ItemEnchantments.EMPTY;
    private ContinuousGeneration continuousGeneration;
    private int continuousGenerationTicks;

    @PartModels
    public static List<IPartModel> getModels() {
        return MODELS.getModels();
    }

    public AnnihilationPlanePart(IPartItem<?> partItem) {
        super(partItem);
        this.getMainNode().addService(IGridTickable.class, this);
        this.getMainNode().setFlags(GridFlags.REQUIRE_CHANNEL);
    }

    @Override
    public void addToWorld() {
        super.addToWorld();
        BlockEntity host = this.getBlockEntity();
        int buildHeight = host.getLevel().getMaxBuildHeight();
        this.continuousGenerationTicks = 0;
        this.continuousGeneration = null;
        if (AEConfig.instance().isAnnihilationPlaneSkyDustGenerationEnabled() && host.getBlockPos().getY() + 1 >= buildHeight && this.getSide() == Direction.UP) {
            this.continuousGeneration = new ContinuousGeneration(AEItemKey.of(AEItems.SKY_DUST), 1L, 200);
        }
    }

    @Override
    public void readFromNBT(CompoundTag data, HolderLookup.Provider registries) {
        super.readFromNBT(data, registries);
        CompoundTag enchantmentsTag = data.getCompound("enchantments");
        RegistryOps ops = registries.createSerializationContext((DynamicOps)NbtOps.INSTANCE);
        this.enchantments = (ItemEnchantments)((Pair)ItemEnchantments.CODEC.decode((DynamicOps)ops, (Object)enchantmentsTag).ifError(err -> LOG.warn("Failed to load enchantments for part {}: {}", (Object)this, (Object)err.message())).getOrThrow()).getFirst();
    }

    @Override
    public void writeToNBT(CompoundTag data, HolderLookup.Provider registries) {
        CompoundTag compoundTag;
        super.writeToNBT(data, registries);
        RegistryOps ops = registries.createSerializationContext((DynamicOps)NbtOps.INSTANCE);
        Tag enchantmentsTag = (Tag)ItemEnchantments.CODEC.encodeStart((DynamicOps)ops, (Object)this.enchantments).getOrThrow();
        if (enchantmentsTag instanceof CompoundTag && !(compoundTag = (CompoundTag)enchantmentsTag).isEmpty()) {
            data.put("enchantments", enchantmentsTag);
        }
    }

    @Override
    public void importSettings(SettingsFrom mode, DataComponentMap data, @Nullable Player player) {
        super.importSettings(mode, data, player);
        if (mode == SettingsFrom.DISMANTLE_ITEM) {
            this.enchantments = (ItemEnchantments)data.get(DataComponents.ENCHANTMENTS);
        }
        this.pickupStrategies = null;
    }

    @Override
    public void exportSettings(SettingsFrom mode, DataComponentMap.Builder data) {
        super.exportSettings(mode, data);
        if (mode == SettingsFrom.DISMANTLE_ITEM) {
            data.set(DataComponents.ENCHANTMENTS, (Object)this.enchantments);
        }
    }

    public ItemEnchantments getEnchantments() {
        return this.enchantments;
    }

    protected List<PickupStrategy> getPickupStrategies() {
        if (this.pickupStrategies == null) {
            IGridNode node = this.getMainNode().getNode();
            if (node == null) {
                return List.of();
            }
            BlockEntity self = this.getHost().getBlockEntity();
            BlockPos pos = self.getBlockPos().relative(this.getSide());
            Direction side = this.getSide().getOpposite();
            UUID owner = node.getOwningPlayerProfileId();
            this.pickupStrategies = StackWorldBehaviors.createPickupStrategies((ServerLevel)self.getLevel(), pos, side, self, this.enchantments, owner);
        }
        return this.pickupStrategies;
    }

    @Override
    public void getBoxes(IPartCollisionHelper bch) {
        if (bch.isBBCollision()) {
            bch.addBox(0.0, 0.0, 14.0, 16.0, 16.0, 15.5);
            return;
        }
        this.connectionHelper.getBoxes(bch);
    }

    public PlaneConnections getConnections() {
        return this.connectionHelper.getConnections();
    }

    @Override
    public void onNeighborChanged(BlockGetter level, BlockPos pos, BlockPos neighbor) {
        if (pos.relative(this.getSide()).equals((Object)neighbor) && !this.isClientSide()) {
            this.refresh();
        }
    }

    @Override
    public void onUpdateShape(Direction side) {
        Direction ourSide = this.getSide();
        if (side.equals((Object)ourSide)) {
            if (!this.isClientSide()) {
                this.refresh();
            }
        } else if (ourSide.getAxis() != side.getAxis()) {
            this.connectionHelper.updateConnections();
        }
    }

    @Override
    public void onEntityCollision(Entity entity) {
        boolean capture;
        if (!entity.isAlive() || this.isClientSide() || !this.getMainNode().isActive()) {
            return;
        }
        IGrid grid = this.getMainNode().getGrid();
        if (grid == null) {
            return;
        }
        PickupStrategy strategy = null;
        for (PickupStrategy pickupStrategy : this.getPickupStrategies()) {
            if (!pickupStrategy.canPickUpEntity(entity)) continue;
            strategy = pickupStrategy;
            break;
        }
        if (strategy == null) {
            return;
        }
        BlockPos pos = this.getHost().getBlockEntity().getBlockPos();
        int planePosX = pos.getX();
        int planePosY = pos.getY();
        int planePosZ = pos.getZ();
        double posYMiddle = (entity.getBoundingBox().minY + entity.getBoundingBox().maxY) / 2.0;
        double entityPosX = entity.getX();
        double entityPosY = entity.getY();
        double entityPosZ = entity.getZ();
        boolean captureX = entityPosX > (double)planePosX && entityPosX < (double)(planePosX + 1);
        boolean captureY = posYMiddle > (double)planePosY && posYMiddle < (double)(planePosY + 1);
        boolean captureZ = entityPosZ > (double)planePosZ && entityPosZ < (double)(planePosZ + 1);
        switch (this.getSide()) {
            default: {
                throw new MatchException(null, null);
            }
            case DOWN: {
                boolean bl;
                if (captureX && captureZ && entityPosY < (double)planePosY + 0.1) {
                    bl = true;
                    break;
                }
                bl = false;
                break;
            }
            case UP: {
                boolean bl;
                if (captureX && captureZ && entityPosY > (double)planePosY + 0.9) {
                    bl = true;
                    break;
                }
                bl = false;
                break;
            }
            case SOUTH: {
                boolean bl;
                if (captureX && captureY && entityPosZ > (double)planePosZ + 0.9) {
                    bl = true;
                    break;
                }
                bl = false;
                break;
            }
            case NORTH: {
                boolean bl;
                if (captureX && captureY && entityPosZ < (double)planePosZ + 0.1) {
                    bl = true;
                    break;
                }
                bl = false;
                break;
            }
            case EAST: {
                boolean bl;
                if (captureZ && captureY && entityPosX > (double)planePosX + 0.9) {
                    bl = true;
                    break;
                }
                bl = false;
                break;
            }
            case WEST: {
                boolean bl = capture = captureZ && captureY && entityPosX < (double)planePosX + 0.1;
            }
        }
        if (capture && !strategy.pickUpEntity(grid.getEnergyService(), this::insertIntoGrid, entity)) {
            this.getMainNode().ifPresent((g, n) -> g.getTickManager().alertDevice((IGridNode)n));
        }
    }

    @Override
    public float getCableConnectionLength(AECableType cable) {
        return 1.0f;
    }

    @Override
    protected void onMainNodeStateChanged(IGridNodeListener.State reason) {
        super.onMainNodeStateChanged(reason);
        if (this.getMainNode().hasGridBooted()) {
            this.refresh();
        }
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(TickRates.AnnihilationPlane, false);
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        if (!this.isActive()) {
            return TickRateModulation.SLEEP;
        }
        IGrid grid = node.getGrid();
        if (this.continuousGeneration != null) {
            this.continuousGenerationTicks += ticksSinceLastCall;
            if (this.continuousGenerationTicks >= this.continuousGeneration.ticks) {
                long amount = this.continuousGenerationTicks / this.continuousGeneration.ticks;
                this.insertIntoGrid(this.continuousGeneration.what, amount, Actionable.MODULATE);
                this.continuousGenerationTicks = (int)((long)this.continuousGenerationTicks - amount * (long)this.continuousGeneration.ticks);
            }
            return TickRateModulation.IDLE;
        }
        for (PickupStrategy pickupStrategy : this.getPickupStrategies()) {
            pickupStrategy.reset();
        }
        for (PickupStrategy pickupStrategy : this.getPickupStrategies()) {
            PickupStrategy.Result pickupResult = pickupStrategy.tryPickup(grid.getEnergyService(), this::insertIntoGrid);
            if (pickupResult == PickupStrategy.Result.PICKED_UP) {
                return TickRateModulation.URGENT;
            }
            if (pickupResult != PickupStrategy.Result.CANT_STORE) continue;
            return TickRateModulation.IDLE;
        }
        return TickRateModulation.SLEEP;
    }

    private void refresh() {
        for (PickupStrategy pickupStrategy : this.getPickupStrategies()) {
            pickupStrategy.reset();
        }
        this.getMainNode().ifPresent((g, n) -> g.getTickManager().alertDevice((IGridNode)n));
    }

    private long insertIntoGrid(AEKey what, long amount, Actionable mode) {
        IGrid grid = this.getMainNode().getGrid();
        if (grid == null) {
            return 0L;
        }
        return StorageHelper.poweredInsert(grid.getEnergyService(), grid.getStorageService().getInventory(), what, amount, this.actionSource, mode);
    }

    @Override
    public IPartModel getStaticModels() {
        return MODELS.getModel(this.isPowered(), this.isActive());
    }

    @Override
    public ModelData getModelData() {
        return ModelData.builder().with(PlaneModelData.CONNECTIONS, (Object)this.getConnections()).build();
    }

    private record ContinuousGeneration(AEKey what, long amount, int ticks) {
    }
}

