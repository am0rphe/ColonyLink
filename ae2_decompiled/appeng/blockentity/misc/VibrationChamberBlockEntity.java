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
 *  net.minecraft.util.Mth
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockState
 *  org.jetbrains.annotations.Nullable
 */
package appeng.blockentity.misc;

import appeng.api.config.Actionable;
import appeng.api.inventories.ISegmentedInventory;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.upgrades.UpgradeInventories;
import appeng.api.util.AECableType;
import appeng.blockentity.grid.AENetworkedInvBlockEntity;
import appeng.core.AEConfig;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.settings.TickRates;
import appeng.util.Platform;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.FilteredInternalInventory;
import appeng.util.inv.filter.IAEItemFilter;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class VibrationChamberBlockEntity
extends AENetworkedInvBlockEntity
implements IGridTickable,
IUpgradeableObject {
    private final AppEngInternalInventory inv = new AppEngInternalInventory(this, 1);
    private final InternalInventory invExt = new FilteredInternalInventory(this.inv, new FuelSlotFilter());
    private final IUpgradeInventory upgrades;
    private double currentFuelTicksPerTick;
    private double remainingFuelTicks = 0.0;
    private double fuelItemFuelTicks = 0.0;
    private double minFuelTicksPerTick;
    private double maxFuelTicksPerTick;
    private double initialFuelTicksPerTick;
    public boolean isOn;
    private final double minEnergyRate;
    private final double baseMaxEnergyRate;
    private final double initialEnergyRate;

    public VibrationChamberBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
        this.getMainNode().setIdlePowerUsage(0.0).setFlags(new GridFlags[0]).addService(IGridTickable.class, this);
        this.upgrades = UpgradeInventories.forMachine(AEBlocks.VIBRATION_CHAMBER, 3, this::saveChanges);
        this.minEnergyRate = AEConfig.instance().getVibrationChamberMinEnergyPerGameTick();
        this.baseMaxEnergyRate = AEConfig.instance().getVibrationChamberMaxEnergyPerGameTick();
        this.initialEnergyRate = Mth.clamp((double)AEConfig.instance().getVibrationChamberBaseEnergyPerFuelTick(), (double)this.minEnergyRate, (double)this.baseMaxEnergyRate);
        this.minFuelTicksPerTick = this.minEnergyRate / this.getEnergyPerFuelTick();
        this.maxFuelTicksPerTick = this.baseMaxEnergyRate / this.getEnergyPerFuelTick();
        this.currentFuelTicksPerTick = this.initialFuelTicksPerTick = this.initialEnergyRate / this.getEnergyPerFuelTick();
    }

    @Override
    public AECableType getCableConnectionType(Direction dir) {
        return AECableType.COVERED;
    }

    @Override
    protected boolean readFromStream(RegistryFriendlyByteBuf data) {
        boolean c = super.readFromStream(data);
        boolean wasOn = this.isOn;
        this.isOn = data.readBoolean();
        return wasOn != this.isOn || c;
    }

    @Override
    protected void writeToStream(RegistryFriendlyByteBuf data) {
        super.writeToStream(data);
        this.isOn = this.getRemainingFuelTicks() > 0.0;
        data.writeBoolean(this.isOn);
    }

    @Override
    public void saveAdditional(CompoundTag data, HolderLookup.Provider registries) {
        super.saveAdditional(data, registries);
        this.upgrades.writeToNBT(data, "upgrades", registries);
        data.putDouble("burnTime", this.getRemainingFuelTicks());
        data.putDouble("maxBurnTime", this.getFuelItemFuelTicks());
        int speed = (int)(this.currentFuelTicksPerTick * 100.0 / this.maxFuelTicksPerTick);
        data.putInt("burnSpeed", speed);
    }

    @Override
    public void loadTag(CompoundTag data, HolderLookup.Provider registries) {
        super.loadTag(data, registries);
        this.upgrades.readFromNBT(data, "upgrades", registries);
        this.setRemainingFuelTicks(data.getDouble("burnTime"));
        this.setFuelItemFuelTicks(data.getDouble("maxBurnTime"));
        this.setCurrentFuelTicksPerTick((double)data.getInt("burnSpeed") * this.maxFuelTicksPerTick / 100.0);
    }

    @Override
    public void addAdditionalDrops(Level level, BlockPos pos, List<ItemStack> drops) {
        super.addAdditionalDrops(level, pos, drops);
        for (ItemStack upgrade : this.upgrades) {
            drops.add(upgrade);
        }
    }

    @Override
    public void clearContent() {
        super.clearContent();
        this.upgrades.clear();
    }

    @Override
    public IUpgradeInventory getUpgrades() {
        return this.upgrades;
    }

    @Override
    @Nullable
    public InternalInventory getSubInventory(ResourceLocation id) {
        if (id.equals((Object)ISegmentedInventory.STORAGE)) {
            return this.getInternalInventory();
        }
        if (id.equals((Object)ISegmentedInventory.UPGRADES)) {
            return this.upgrades;
        }
        return super.getSubInventory(id);
    }

    @Override
    protected InternalInventory getExposedInventoryForSide(Direction facing) {
        return this.invExt;
    }

    @Override
    public InternalInventory getInternalInventory() {
        return this.inv;
    }

    @Override
    public void onChangeInventory(AppEngInternalInventory inv, int slot) {
        if (this.getRemainingFuelTicks() <= 0.0 && this.canEatFuel()) {
            this.getMainNode().ifPresent((grid, node) -> grid.getTickManager().wakeDevice((IGridNode)node));
        }
    }

    private boolean canEatFuel() {
        int newBurnTime;
        ItemStack is = this.inv.getStackInSlot(0);
        return !is.isEmpty() && (newBurnTime = VibrationChamberBlockEntity.getBurnTime(is)) > 0 && is.getCount() > 0;
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        if (this.getRemainingFuelTicks() <= 0.0) {
            this.eatFuel();
        }
        return new TickingRequest(TickRates.VibrationChamber, this.getRemainingFuelTicks() <= 0.0);
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        this.minFuelTicksPerTick = this.minEnergyRate / this.getEnergyPerFuelTick();
        this.maxFuelTicksPerTick = this.getMaxFuelTicksPerTick();
        this.initialFuelTicksPerTick = this.initialEnergyRate / this.getEnergyPerFuelTick();
        if (this.getRemainingFuelTicks() <= 0.0) {
            this.eatFuel();
            if (this.getRemainingFuelTicks() > 0.0) {
                return TickRateModulation.URGENT;
            }
            this.setCurrentFuelTicksPerTick(this.initialFuelTicksPerTick);
            return TickRateModulation.SLEEP;
        }
        double fuelTicksConsumed = (double)ticksSinceLastCall * this.currentFuelTicksPerTick;
        this.setRemainingFuelTicks(this.getRemainingFuelTicks() - fuelTicksConsumed);
        if (this.getRemainingFuelTicks() < 0.0) {
            fuelTicksConsumed += this.getRemainingFuelTicks();
            this.setRemainingFuelTicks(0.0);
        }
        double speedScalingPerTick = (this.maxFuelTicksPerTick - this.minFuelTicksPerTick) / 100.0;
        double speedStep = (double)ticksSinceLastCall * speedScalingPerTick;
        IGrid grid = node.getGrid();
        IEnergyService energy = grid.getEnergyService();
        if (Math.abs(fuelTicksConsumed - 0.0) < 0.01) {
            if (energy.injectPower(1.0, Actionable.SIMULATE) == 0.0) {
                this.setCurrentFuelTicksPerTick(this.getCurrentFuelTicksPerTick() + speedStep);
                return TickRateModulation.FASTER;
            }
            return TickRateModulation.IDLE;
        }
        double newPower = fuelTicksConsumed * this.getEnergyPerFuelTick();
        double overFlow = energy.injectPower(newPower, Actionable.MODULATE);
        if (overFlow > 0.0) {
            this.setCurrentFuelTicksPerTick(this.getCurrentFuelTicksPerTick() - speedStep);
        } else {
            this.setCurrentFuelTicksPerTick(this.getCurrentFuelTicksPerTick() + speedStep);
        }
        return overFlow > 0.0 ? TickRateModulation.SLOWER : TickRateModulation.FASTER;
    }

    private void eatFuel() {
        int newBurnTime;
        ItemStack is = this.inv.getStackInSlot(0);
        if (!is.isEmpty() && (newBurnTime = VibrationChamberBlockEntity.getBurnTime(is)) > 0 && is.getCount() > 0) {
            this.setRemainingFuelTicks(this.getRemainingFuelTicks() + (double)newBurnTime);
            this.setFuelItemFuelTicks(this.getRemainingFuelTicks());
            Item fuelItem = is.getItem();
            if (is.getCount() <= 1) {
                this.inv.setItemDirect(0, fuelItem.getCraftingRemainingItem(is));
            } else {
                is.shrink(1);
                this.inv.setItemDirect(0, is);
            }
            this.saveChanges();
        }
        if (this.getRemainingFuelTicks() > 0.0) {
            this.getMainNode().ifPresent((grid, node) -> grid.getTickManager().wakeDevice((IGridNode)node));
        }
        if (!this.isOn && this.getRemainingFuelTicks() > 0.0 || this.isOn && this.getRemainingFuelTicks() <= 0.0) {
            this.isOn = this.getRemainingFuelTicks() > 0.0;
            this.markForUpdate();
            if (this.hasLevel()) {
                Platform.notifyBlocksOfNeighbors(this.level, this.worldPosition);
            }
        }
    }

    public static int getBurnTime(ItemStack is) {
        return is.getBurnTime(null);
    }

    public static boolean hasBurnTime(ItemStack is) {
        return VibrationChamberBlockEntity.getBurnTime(is) > 0;
    }

    public double getCurrentFuelTicksPerTick() {
        return this.currentFuelTicksPerTick;
    }

    private void setCurrentFuelTicksPerTick(double currentFuelTicksPerTick) {
        this.currentFuelTicksPerTick = Mth.clamp((double)currentFuelTicksPerTick, (double)this.minFuelTicksPerTick, (double)this.maxFuelTicksPerTick);
    }

    public double getFuelItemFuelTicks() {
        return this.fuelItemFuelTicks;
    }

    private void setFuelItemFuelTicks(double fuelItemFuelTicks) {
        this.fuelItemFuelTicks = fuelItemFuelTicks;
    }

    public double getRemainingFuelTicks() {
        return this.remainingFuelTicks;
    }

    private void setRemainingFuelTicks(double remainingFuelTicks) {
        this.remainingFuelTicks = remainingFuelTicks;
    }

    public double getEnergyPerFuelTick() {
        return AEConfig.instance().getVibrationChamberBaseEnergyPerFuelTick() * (double)(1.0f + (float)this.upgrades.getInstalledUpgrades(AEItems.ENERGY_CARD) / 2.0f);
    }

    public double getMinFuelTicksPerTick() {
        return this.minFuelTicksPerTick;
    }

    public double getMaxFuelTicksPerTick() {
        return this.getMaxEnergyRate() / this.getEnergyPerFuelTick();
    }

    public double getMaxEnergyRate() {
        return this.baseMaxEnergyRate + this.baseMaxEnergyRate * (double)this.upgrades.getInstalledUpgrades(AEItems.SPEED_CARD) / 2.0;
    }

    private static class FuelSlotFilter
    implements IAEItemFilter {
        private FuelSlotFilter() {
        }

        @Override
        public boolean allowExtract(InternalInventory inv, int slot, int amount) {
            return !VibrationChamberBlockEntity.hasBurnTime(inv.getStackInSlot(slot));
        }

        @Override
        public boolean allowInsert(InternalInventory inv, int slot, ItemStack stack) {
            return VibrationChamberBlockEntity.hasBurnTime(stack);
        }
    }
}

