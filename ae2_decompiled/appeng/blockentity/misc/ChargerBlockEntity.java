/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockState
 *  org.jetbrains.annotations.Nullable
 */
package appeng.blockentity.misc;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.PowerUnit;
import appeng.api.implementations.blockentities.ICrankable;
import appeng.api.implementations.items.IAEItemPowerStorage;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.orientation.BlockOrientation;
import appeng.api.orientation.RelativeSide;
import appeng.api.stacks.AEItemKey;
import appeng.api.util.AECableType;
import appeng.blockentity.grid.AENetworkedPoweredBlockEntity;
import appeng.blockentity.misc.ChargerRecipes;
import appeng.core.AEConfig;
import appeng.core.settings.TickRates;
import appeng.util.Platform;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.filter.IAEItemFilter;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class ChargerBlockEntity
extends AENetworkedPoweredBlockEntity
implements IGridTickable {
    public static final int POWER_MAXIMUM_AMOUNT = 1600;
    private static final int POWER_THRESHOLD = 1599;
    private boolean working;
    private final AppEngInternalInventory inv = new AppEngInternalInventory(this, 1, 1, new ChargerInvFilter(this));

    public ChargerBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
        this.getMainNode().setFlags(new GridFlags[0]).setIdlePowerUsage(0.0).addService(IGridTickable.class, this);
        this.setInternalMaxPower(1600.0);
        this.setPowerSides(this.getGridConnectableSides(this.getOrientation()));
    }

    @Override
    public AECableType getCableConnectionType(Direction dir) {
        return AECableType.COVERED;
    }

    @Override
    public Set<Direction> getGridConnectableSides(BlockOrientation orientation) {
        return EnumSet.complementOf(EnumSet.of(orientation.getSide(RelativeSide.FRONT)));
    }

    @Override
    protected boolean readFromStream(RegistryFriendlyByteBuf data) {
        boolean changed = super.readFromStream(data);
        this.working = data.readBoolean();
        if (data.readBoolean()) {
            AEItemKey item = AEItemKey.fromPacket(data);
            this.inv.setItemDirect(0, item.toStack());
        } else {
            this.inv.setItemDirect(0, ItemStack.EMPTY);
        }
        return changed;
    }

    @Override
    protected void writeToStream(RegistryFriendlyByteBuf data) {
        super.writeToStream(data);
        data.writeBoolean(this.working);
        AEItemKey is = AEItemKey.of(this.inv.getStackInSlot(0));
        data.writeBoolean(is != null);
        if (is != null) {
            is.writeToPacket(data);
        }
    }

    @Override
    protected void onOrientationChanged(BlockOrientation orientation) {
        super.onOrientationChanged(orientation);
        this.setPowerSides(this.getGridConnectableSides(orientation));
    }

    @Override
    public InternalInventory getInternalInventory() {
        return this.inv;
    }

    @Override
    public void onChangeInventory(AppEngInternalInventory inv, int slot) {
        this.getMainNode().ifPresent((grid, node) -> grid.getTickManager().wakeDevice((IGridNode)node));
        this.markForUpdate();
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(TickRates.Charger, false);
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        this.doWork(ticksSinceLastCall);
        return TickRateModulation.FASTER;
    }

    private void doWork(int ticksSinceLastCall) {
        boolean wasWorking = this.working;
        this.working = false;
        boolean changed = false;
        ItemStack myItem = this.inv.getStackInSlot(0);
        if (!myItem.isEmpty()) {
            if (Platform.isChargeable(myItem)) {
                double maxPower;
                IAEItemPowerStorage ps = (IAEItemPowerStorage)myItem.getItem();
                double currentPower = ps.getAECurrentPower(myItem);
                if (currentPower < (maxPower = ps.getAEMaxPower(myItem))) {
                    double chargeRate = ps.getChargeRate(myItem) * (double)ticksSinceLastCall * AEConfig.instance().getChargerChargeRate();
                    double extractedAmount = this.extractAEPower(chargeRate, Actionable.MODULATE, PowerMultiplier.CONFIG);
                    double missingChargeRate = chargeRate - extractedAmount;
                    double missingAEPower = maxPower - currentPower;
                    double toExtract = Math.min(missingChargeRate, missingAEPower);
                    IGrid grid2 = this.getMainNode().getGrid();
                    if (grid2 != null) {
                        extractedAmount += grid2.getEnergyService().extractAEPower(toExtract, Actionable.MODULATE, PowerMultiplier.ONE);
                    }
                    if (extractedAmount > 0.0) {
                        double adjustment = ps.injectAEPower(myItem, extractedAmount, Actionable.MODULATE);
                        this.setInternalCurrentPower(this.getInternalCurrentPower() + adjustment);
                        this.working = true;
                        changed = true;
                    }
                }
            } else if (this.getInternalCurrentPower() >= 1599.0 && ChargerRecipes.findRecipe(this.level, myItem) != null) {
                this.working = true;
                if (this.level != null && this.level.getRandom().nextFloat() > 0.8f) {
                    this.extractAEPower(this.getInternalMaxPower(), Actionable.MODULATE, PowerMultiplier.CONFIG);
                    ItemStack charged = Objects.requireNonNull(ChargerRecipes.findRecipe((Level)this.level, (ItemStack)myItem)).result;
                    this.inv.setItemDirect(0, charged.copy());
                    changed = true;
                }
            }
        }
        if (this.getInternalCurrentPower() < 1599.0) {
            this.getMainNode().ifPresent(grid -> {
                double toExtract = Math.min(800.0, this.getInternalMaxPower() - this.getInternalCurrentPower());
                double extracted = grid.getEnergyService().extractAEPower(toExtract, Actionable.MODULATE, PowerMultiplier.ONE);
                this.injectExternalPower(PowerUnit.AE, extracted, Actionable.MODULATE);
            });
            changed = true;
        }
        if (changed || this.working != wasWorking) {
            this.markForUpdate();
        }
    }

    public boolean isWorking() {
        return this.working;
    }

    @Nullable
    public ICrankable getCrankable(Direction direction) {
        if (direction != this.getFront()) {
            return new AENetworkedPoweredBlockEntity.Crankable(this);
        }
        return null;
    }

    private record ChargerInvFilter(ChargerBlockEntity chargerBlockEntity) implements IAEItemFilter
    {
        @Override
        public boolean allowInsert(InternalInventory inv, int i, ItemStack itemstack) {
            return Platform.isChargeable(itemstack) || ChargerRecipes.allowInsert(this.chargerBlockEntity.level, itemstack);
        }

        @Override
        public boolean allowExtract(InternalInventory inv, int slotIndex, int amount) {
            IAEItemPowerStorage ips;
            ItemStack extractedItem = inv.getStackInSlot(slotIndex);
            if (Platform.isChargeable(extractedItem) && (ips = (IAEItemPowerStorage)extractedItem.getItem()).getAECurrentPower(extractedItem) >= ips.getAEMaxPower(extractedItem)) {
                return true;
            }
            return ChargerRecipes.allowExtract(this.chargerBlockEntity.level, extractedItem);
        }
    }
}

