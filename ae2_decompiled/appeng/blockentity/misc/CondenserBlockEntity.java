/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockState
 *  net.neoforged.neoforge.fluids.FluidStack
 *  net.neoforged.neoforge.fluids.IFluidTank
 *  net.neoforged.neoforge.fluids.capability.IFluidHandler
 *  net.neoforged.neoforge.fluids.capability.IFluidHandler$FluidAction
 */
package appeng.blockentity.misc;

import appeng.api.config.CondenserOutput;
import appeng.api.config.Settings;
import appeng.api.implementations.items.IStorageComponent;
import appeng.api.inventories.BaseInternalInventory;
import appeng.api.inventories.InternalInventory;
import appeng.api.stacks.AEFluidKey;
import appeng.api.storage.MEStorage;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.blockentity.AEBaseInvBlockEntity;
import appeng.blockentity.misc.CondenserMEStorage;
import appeng.core.definitions.AEItems;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.CombinedInternalInventory;
import appeng.util.inv.FilteredInternalInventory;
import appeng.util.inv.filter.AEItemFilters;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public class CondenserBlockEntity
extends AEBaseInvBlockEntity
implements IConfigurableObject {
    public static final int BYTE_MULTIPLIER = 8;
    private final IConfigManager cm = IConfigManager.builder(() -> {
        this.saveChanges();
        this.addPower(0.0);
    }).registerSetting(Settings.CONDENSER_OUTPUT, CondenserOutput.TRASH).build();
    private final AppEngInternalInventory outputSlot = new AppEngInternalInventory(this, 1);
    private final AppEngInternalInventory storageSlot = new AppEngInternalInventory(this, 1);
    private final InternalInventory inputSlot = new CondenseItemHandler();
    private final IFluidHandler fluidHandler = new FluidHandler();
    private final CondenserMEStorage meStorage = new CondenserMEStorage(this);
    private final InternalInventory externalInv = new CombinedInternalInventory(this.inputSlot, new FilteredInternalInventory(this.outputSlot, AEItemFilters.EXTRACT_ONLY));
    private final InternalInventory combinedInv = new CombinedInternalInventory(this.inputSlot, this.outputSlot, this.storageSlot);
    private double storedPower = 0.0;

    public CondenserBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
    }

    @Override
    public void saveAdditional(CompoundTag data, HolderLookup.Provider registries) {
        super.saveAdditional(data, registries);
        this.cm.writeToNBT(data, registries);
        data.putDouble("storedPower", this.getStoredPower());
    }

    @Override
    public void loadTag(CompoundTag data, HolderLookup.Provider registries) {
        super.loadTag(data, registries);
        this.cm.readFromNBT(data, registries);
        this.setStoredPower(data.getDouble("storedPower"));
    }

    public double getStorage() {
        IStorageComponent sc;
        Item item;
        ItemStack is = this.storageSlot.getStackInSlot(0);
        if (!is.isEmpty() && (item = is.getItem()) instanceof IStorageComponent && (sc = (IStorageComponent)item).isStorageComponent(is)) {
            return sc.getBytes(is) * 8;
        }
        return 0.0;
    }

    public void addPower(double rawPower) {
        this.setStoredPower(this.getStoredPower() + rawPower);
        this.setStoredPower(Math.max(0.0, Math.min(this.getStorage(), this.getStoredPower())));
        this.fillOutput();
    }

    private void fillOutput() {
        double requiredPower = this.getRequiredPower();
        while (requiredPower <= this.getStoredPower() && !this.getOutput().isEmpty() && requiredPower > 0.0 && this.canAddOutput()) {
            this.setStoredPower(this.getStoredPower() - requiredPower);
            this.addOutput();
        }
    }

    boolean canAddOutput() {
        return this.outputSlot.insertItem(0, this.getOutput(), true).isEmpty();
    }

    private void addOutput() {
        this.outputSlot.insertItem(0, this.getOutput(), false);
    }

    InternalInventory getOutputSlot() {
        return this.outputSlot;
    }

    private ItemStack getOutput() {
        return switch (this.cm.getSetting(Settings.CONDENSER_OUTPUT)) {
            case CondenserOutput.MATTER_BALLS -> AEItems.MATTER_BALL.stack();
            case CondenserOutput.SINGULARITY -> AEItems.SINGULARITY.stack();
            default -> ItemStack.EMPTY;
        };
    }

    public double getRequiredPower() {
        return this.cm.getSetting(Settings.CONDENSER_OUTPUT).requiredPower;
    }

    @Override
    public InternalInventory getInternalInventory() {
        return this.combinedInv;
    }

    @Override
    public void onChangeInventory(AppEngInternalInventory inv, int slot) {
        if (inv == this.outputSlot) {
            this.fillOutput();
        }
    }

    @Override
    public IConfigManager getConfigManager() {
        return this.cm;
    }

    public double getStoredPower() {
        return this.storedPower;
    }

    private void setStoredPower(double storedPower) {
        this.storedPower = storedPower;
        this.setChanged();
    }

    public InternalInventory getExternalInv() {
        return this.externalInv;
    }

    public IFluidHandler getFluidHandler() {
        return this.fluidHandler;
    }

    public MEStorage getMEStorage() {
        return this.meStorage;
    }

    private class CondenseItemHandler
    extends BaseInternalInventory {
        private CondenseItemHandler() {
        }

        @Override
        public int size() {
            return 1;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return ItemStack.EMPTY;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return CondenserBlockEntity.this.canAddOutput();
        }

        @Override
        public void setItemDirect(int slotIndex, ItemStack stack) {
            if (!stack.isEmpty()) {
                CondenserBlockEntity.this.addPower(stack.getCount());
            }
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (!CondenserBlockEntity.this.canAddOutput()) {
                return stack;
            }
            if (!simulate && !stack.isEmpty()) {
                CondenserBlockEntity.this.addPower(stack.getCount());
            }
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }
    }

    private class FluidHandler
    implements IFluidTank,
    IFluidHandler {
        private FluidHandler() {
        }

        public FluidStack getFluid() {
            return FluidStack.EMPTY;
        }

        public int getFluidAmount() {
            return 0;
        }

        public int getCapacity() {
            return 1000;
        }

        public boolean isFluidValid(FluidStack stack) {
            return !stack.isEmpty();
        }

        public int fill(FluidStack resource, IFluidHandler.FluidAction action) {
            AEFluidKey what;
            int amount;
            int n = amount = resource.isEmpty() ? 0 : Math.min(resource.getAmount(), 1000);
            if (action == IFluidHandler.FluidAction.EXECUTE && (what = AEFluidKey.of(resource)) != null) {
                double transferFactor = what.getAmountPerOperation();
                CondenserBlockEntity.this.addPower((double)amount / transferFactor);
            }
            return amount;
        }

        public FluidStack drain(int maxDrain, IFluidHandler.FluidAction action) {
            return FluidStack.EMPTY;
        }

        public FluidStack drain(FluidStack resource, IFluidHandler.FluidAction action) {
            return FluidStack.EMPTY;
        }

        public int getTanks() {
            return 1;
        }

        public FluidStack getFluidInTank(int tank) {
            return FluidStack.EMPTY;
        }

        public int getTankCapacity(int tank) {
            return tank == 0 ? this.getCapacity() : 0;
        }

        public boolean isFluidValid(int tank, FluidStack stack) {
            return tank == 0 && this.isFluidValid(stack);
        }
    }
}

